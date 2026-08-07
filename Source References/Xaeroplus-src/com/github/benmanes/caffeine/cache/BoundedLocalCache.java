package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;

abstract class BoundedLocalCache<K, V> extends BLCHeader.DrainStatusRef implements LocalCache<K, V> {
   static final System.Logger logger = System.getLogger(BoundedLocalCache.class.getName());
   static final int NCPU = Runtime.getRuntime().availableProcessors();
   static final int WRITE_BUFFER_MIN = 4;
   static final int WRITE_BUFFER_MAX;
   static final int WRITE_BUFFER_RETRIES = 100;
   static final long MAXIMUM_CAPACITY = 9223372034707292160L;
   static final double PERCENT_MAIN = 0.99;
   static final double PERCENT_MAIN_PROTECTED = 0.8;
   static final double HILL_CLIMBER_RESTART_THRESHOLD = 0.05;
   static final double HILL_CLIMBER_STEP_PERCENT = (double)0.0625F;
   static final double HILL_CLIMBER_STEP_DECAY_RATE = 0.98;
   static final int ADMIT_HASHDOS_THRESHOLD = 6;
   static final int QUEUE_TRANSFER_THRESHOLD = 1000;
   static final long EXPIRE_TOLERANCE;
   static final long MAXIMUM_EXPIRY = 4611686018427387903L;
   static final long WARN_AFTER_LOCK_WAIT_NANOS;
   static final int MAX_PUT_SPIN_WAIT_ATTEMPTS = 1023;
   static final VarHandle REFRESHES;
   final @Nullable RemovalListener<K, V> evictionListener;
   final @Nullable AsyncCacheLoader<K, V> cacheLoader;
   final MpscGrowableArrayQueue<Runnable> writeBuffer;
   final ConcurrentHashMap<Object, Node<K, V>> data;
   final PerformCleanupTask drainBuffersTask;
   final Consumer<Node<K, V>> accessPolicy;
   final Buffer<Node<K, V>> readBuffer;
   final NodeFactory<K, V> nodeFactory;
   final ReentrantLock evictionLock;
   final Weigher<K, V> weigher;
   final Executor executor;
   final boolean isWeighted;
   final boolean isAsync;
   @Nullable Set<K> keySet;
   @Nullable Collection<V> values;
   @Nullable Set<Map.Entry<K, V>> entrySet;
   volatile @Nullable ConcurrentMap<Object, CompletableFuture<?>> refreshes;

   protected BoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<K, V> cacheLoader, boolean isAsync) {
      this.isAsync = isAsync;
      this.cacheLoader = cacheLoader;
      this.executor = builder.getExecutor();
      this.isWeighted = builder.isWeighted();
      this.evictionLock = new ReentrantLock();
      this.weigher = builder.<K, V>getWeigher(isAsync);
      this.drainBuffersTask = new PerformCleanupTask(this);
      this.nodeFactory = NodeFactory.<K, V>newFactory(builder, isAsync);
      this.evictionListener = builder.<K, V>getEvictionListener(isAsync);
      this.data = new ConcurrentHashMap(builder.getInitialCapacity());
      this.readBuffer = (Buffer<Node<K, V>>)(!this.evicts() && !this.collectKeys() && !this.collectValues() && !this.expiresAfterAccess() ? Buffer.disabled() : new BoundedBuffer());
      this.accessPolicy = !this.evicts() && !this.expiresAfterAccess() ? (e) -> {
      } : this::onAccess;
      this.writeBuffer = new MpscGrowableArrayQueue<Runnable>(4, WRITE_BUFFER_MAX);
      if (this.evicts()) {
         this.setMaximumSize(builder.getMaximum());
      }

   }

   void requireIsAlive(Object key, Node<?, ?> node) {
      if (!node.isAlive()) {
         throw new IllegalStateException(this.brokenEqualityMessage(key, node));
      }
   }

   void logIfAlive(Node<?, ?> node) {
      if (node.isAlive()) {
         String message = this.brokenEqualityMessage(node.getKeyReference(), node);
         logger.log(Level.ERROR, message, new IllegalStateException());
      }

   }

   String brokenEqualityMessage(Object key, Node<?, ?> node) {
      return String.format(Locale.US, "An invalid state was detected, occurring when the key's equals or hashCode was modified while residing in the cache. This violation of the Map contract can lead to non-deterministic behavior (key: %s, key type: %s, node type: %s, cache type: %s).", key, key.getClass().getName(), node.getClass().getSimpleName(), this.getClass().getSimpleName());
   }

   static RuntimeException toUncheckedException(Throwable t) {
      if (t instanceof Error) {
         throw (Error)t;
      } else {
         return (RuntimeException)(t instanceof RuntimeException ? (RuntimeException)t : new CompletionException(t));
      }
   }

   public boolean isAsync() {
      return this.isAsync;
   }

   final boolean isComputingAsync(@Nullable V value) {
      return this.isAsync && !Async.isReady((CompletableFuture)value);
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      throw new UnsupportedOperationException();
   }

   public final Executor executor() {
      return this.executor;
   }

   public ConcurrentMap<Object, CompletableFuture<?>> refreshes() {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending == null) {
         pending = new ConcurrentHashMap();
         if (!REFRESHES.compareAndSet(this, (Void)null, pending)) {
            pending = (ConcurrentMap)Objects.requireNonNull(this.refreshes);
         }
      }

      return pending;
   }

   void discardRefresh(Object keyReference) {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending != null && pending.containsKey(keyReference)) {
         pending.remove(keyReference);
      }

   }

   public Object referenceKey(K key) {
      return this.nodeFactory.newLookupKey(key);
   }

   public boolean isPendingEviction(K key) {
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         return false;
      } else {
         V value = node.getValue();
         return value == null || this.hasExpired(node, this.expirationTicker().read(), value);
      }
   }

   public boolean isRecordingStats() {
      return false;
   }

   public StatsCounter statsCounter() {
      return StatsCounter.disabledStatsCounter();
   }

   public Ticker statsTicker() {
      return Ticker.disabledTicker();
   }

   protected @Nullable RemovalListener<K, V> removalListener() {
      return null;
   }

   public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
      RemovalListener<K, V> removalListener = this.removalListener();
      if (removalListener != null) {
         Runnable task = () -> {
            try {
               removalListener.onRemoval(key, value, cause);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown by removal listener", t);
            }

         };

         try {
            this.executor.execute(task);
         } catch (Throwable t) {
            logger.log(Level.ERROR, "Exception thrown when submitting removal listener", t);
            task.run();
         }

      }
   }

   void notifyEviction(@Nullable K key, @Nullable V value, RemovalCause cause) {
      if (this.evictionListener != null) {
         try {
            this.evictionListener.onRemoval(key, value, cause);
         } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by eviction listener", t);
         }

      }
   }

   public boolean collectKeys() {
      return false;
   }

   protected boolean collectValues() {
      return false;
   }

   protected ReferenceQueue<K> keyReferenceQueue() {
      return null;
   }

   protected ReferenceQueue<V> valueReferenceQueue() {
      return null;
   }

   protected @Nullable Pacer pacer() {
      return null;
   }

   protected boolean expiresVariable() {
      return false;
   }

   protected boolean expiresAfterAccess() {
      return false;
   }

   protected long expiresAfterAccessNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setExpiresAfterAccessNanos(long expireAfterAccessNanos) {
      throw new UnsupportedOperationException();
   }

   protected boolean expiresAfterWrite() {
      return false;
   }

   protected long expiresAfterWriteNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setExpiresAfterWriteNanos(long expireAfterWriteNanos) {
      throw new UnsupportedOperationException();
   }

   protected boolean refreshAfterWrite() {
      return false;
   }

   protected long refreshAfterWriteNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setRefreshAfterWriteNanos(long refreshAfterWriteNanos) {
      throw new UnsupportedOperationException();
   }

   public Expiry<K, V> expiry() {
      return null;
   }

   public Ticker expirationTicker() {
      return Ticker.disabledTicker();
   }

   protected TimerWheel<K, V> timerWheel() {
      throw new UnsupportedOperationException();
   }

   protected boolean evicts() {
      return false;
   }

   protected boolean isWeighted() {
      return this.weigher != Weigher.singletonWeigher();
   }

   protected FrequencySketch frequencySketch() {
      throw new UnsupportedOperationException();
   }

   protected boolean fastpath() {
      return false;
   }

   protected long maximum() {
      throw new UnsupportedOperationException();
   }

   protected long maximumAcquire() {
      throw new UnsupportedOperationException();
   }

   protected long windowMaximum() {
      throw new UnsupportedOperationException();
   }

   protected long mainProtectedMaximum() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWindowMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMainProtectedMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   protected long weightedSize() {
      throw new UnsupportedOperationException();
   }

   protected long weightedSizeAcquire() {
      throw new UnsupportedOperationException();
   }

   protected long windowWeightedSize() {
      throw new UnsupportedOperationException();
   }

   protected long mainProtectedWeightedSize() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWindowWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMainProtectedWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   protected long hitsInSample() {
      throw new UnsupportedOperationException();
   }

   protected long missesInSample() {
      throw new UnsupportedOperationException();
   }

   protected double stepSize() {
      throw new UnsupportedOperationException();
   }

   protected double previousSampleHitRate() {
      throw new UnsupportedOperationException();
   }

   protected long adjustment() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setHitsInSample(long hitCount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMissesInSample(long missCount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setStepSize(double stepSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setPreviousSampleHitRate(double hitRate) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setAdjustment(long amount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   void setMaximumSize(long maximum) {
      Caffeine.requireArgument(maximum >= 0L, "maximum must not be negative");
      if (maximum != this.maximum()) {
         long max = Math.min(maximum, 9223372034707292160L);
         long window = max - (long)(0.99 * (double)max);
         long mainProtected = (long)(0.8 * (double)(max - window));
         this.setMaximum(max);
         this.setWindowMaximum(window);
         this.setMainProtectedMaximum(mainProtected);
         this.setHitsInSample(0L);
         this.setMissesInSample(0L);
         this.setStepSize((double)-0.0625F * (double)max);
         if (this.frequencySketch() != null && !this.isWeighted() && this.weightedSize() >= max >>> 1) {
            this.frequencySketch().ensureCapacity(max);
         }

      }
   }

   @GuardedBy("evictionLock")
   void evictEntries() {
      if (this.evicts()) {
         Node<K, V> candidate = this.evictFromWindow();
         this.evictFromMain(candidate);
      }
   }

   @GuardedBy("evictionLock")
   @Nullable Node<K, V> evictFromWindow() {
      Node<K, V> first = null;

      Node<K, V> next;
      for(Node<K, V> node = (Node)this.accessOrderWindowDeque().peekFirst(); this.windowWeightedSize() > this.windowMaximum() && node != null; node = next) {
         next = node.getNextInAccessOrder();
         if (node.getPolicyWeight() != 0) {
            node.makeMainProbation();
            this.accessOrderWindowDeque().remove((AccessOrderDeque.AccessOrder)node);
            this.accessOrderProbationDeque().offerLast(node);
            if (first == null) {
               first = node;
            }

            this.setWindowWeightedSize(this.windowWeightedSize() - (long)node.getPolicyWeight());
         }
      }

      return first;
   }

   @GuardedBy("evictionLock")
   void evictFromMain(@Var @Nullable Node<K, V> candidate) {
      int victimQueue = 1;
      int candidateQueue = 1;
      Node<K, V> victim = (Node)this.accessOrderProbationDeque().peekFirst();

      while(this.weightedSize() > this.maximum()) {
         if (candidate == null && candidateQueue == 1) {
            candidate = (Node)this.accessOrderWindowDeque().peekFirst();
            candidateQueue = 0;
         }

         if (candidate == null && victim == null) {
            if (victimQueue == 1) {
               victim = (Node)this.accessOrderProtectedDeque().peekFirst();
               victimQueue = 2;
            } else {
               if (victimQueue != 2) {
                  break;
               }

               victim = (Node)this.accessOrderWindowDeque().peekFirst();
               victimQueue = 0;
            }
         } else if (victim != null && victim.getPolicyWeight() == 0) {
            victim = victim.getNextInAccessOrder();
         } else if (candidate != null && candidate.getPolicyWeight() == 0) {
            candidate = candidate.getNextInAccessOrder();
         } else if (victim == null) {
            Objects.requireNonNull(candidate);
            Node<K, V> previous = candidate.getNextInAccessOrder();
            Node<K, V> evict = candidate;
            candidate = previous;
            this.evictEntry(evict, RemovalCause.SIZE, 0L);
         } else if (candidate == null) {
            Node<K, V> evict = victim;
            victim = victim.getNextInAccessOrder();
            this.evictEntry(evict, RemovalCause.SIZE, 0L);
         } else if (candidate == victim) {
            victim = victim.getNextInAccessOrder();
            this.evictEntry(candidate, RemovalCause.SIZE, 0L);
            candidate = null;
         } else {
            Object victimKeyRef = victim.getKeyReferenceOrNull();
            Object candidateKeyRef = candidate.getKeyReferenceOrNull();
            if (victimKeyRef == null) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.COLLECTED, 0L);
            } else if (candidateKeyRef == null) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.COLLECTED, 0L);
            } else if (!victim.isAlive()) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if (!candidate.isAlive()) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if ((long)candidate.getPolicyWeight() > this.maximum()) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if (this.admit(candidateKeyRef, victimKeyRef)) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
               candidate = candidate.getNextInAccessOrder();
            } else {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            }
         }
      }

   }

   @GuardedBy("evictionLock")
   boolean admit(Object candidateKeyRef, Object victimKeyRef) {
      int candidateFreq = this.frequencySketch().frequency(candidateKeyRef);
      int victimFreq = this.frequencySketch().frequency(victimKeyRef);
      if (candidateFreq > victimFreq) {
         return true;
      } else if (candidateFreq >= 6) {
         int random = ThreadLocalRandom.current().nextInt();
         return (random & 127) == 0;
      } else {
         return false;
      }
   }

   @GuardedBy("evictionLock")
   void expireEntries() {
      long now = this.expirationTicker().read();
      this.expireAfterAccessEntries(now);
      this.expireAfterWriteEntries(now);
      this.expireVariableEntries(now);
      Pacer pacer = this.pacer();
      if (pacer != null) {
         long delay = this.getExpirationDelay(now);
         if (delay == Long.MAX_VALUE) {
            pacer.cancel();
         } else {
            pacer.schedule(this.executor, this.drainBuffersTask, now, delay);
         }
      }

   }

   @GuardedBy("evictionLock")
   void expireAfterAccessEntries(long now) {
      if (this.expiresAfterAccess()) {
         this.expireAfterAccessEntries(now, this.accessOrderWindowDeque());
         if (this.evicts()) {
            this.expireAfterAccessEntries(now, this.accessOrderProbationDeque());
            this.expireAfterAccessEntries(now, this.accessOrderProtectedDeque());
         }

      }
   }

   @GuardedBy("evictionLock")
   void expireAfterAccessEntries(long now, AccessOrderDeque<Node<K, V>> accessOrderDeque) {
      Node<K, V> head = (Node)accessOrderDeque.peekFirst();
      if (head != null) {
         long duration = this.expiresAfterAccessNanos();
         Node<K, V> last = (Node)Objects.requireNonNull((Node)accessOrderDeque.peekLast());
         Node<K, V> node = head;

         while(node != null) {
            Node<K, V> next = node == last ? null : node.getNextInAccessOrder();
            if (now - node.getAccessTime() < duration) {
               boolean stalePosition = last.getAccessTime() < node.getAccessTime();
               if (!stalePosition && !this.isComputingAsync(node.getValue())) {
                  return;
               }

               accessOrderDeque.moveToBack(node);
               node = next;
            } else {
               this.evictEntry(node, RemovalCause.EXPIRED, now);
               node = next;
            }
         }

      }
   }

   @GuardedBy("evictionLock")
   void expireAfterWriteEntries(long now) {
      if (this.expiresAfterWrite()) {
         Node<K, V> head = (Node)this.writeOrderDeque().peekFirst();
         if (head != null) {
            long duration = this.expiresAfterWriteNanos();
            Node<K, V> last = (Node)Objects.requireNonNull((Node)this.writeOrderDeque().peekLast());
            Node<K, V> node = head;

            while(node != null) {
               Node<K, V> next = node == last ? null : node.getNextInWriteOrder();
               if (now - node.getWriteTime() < duration) {
                  boolean stalePosition = last.getWriteTime() < node.getWriteTime();
                  if (!stalePosition && !this.isComputingAsync(node.getValue())) {
                     return;
                  }

                  this.writeOrderDeque().moveToBack(node);
                  node = next;
               } else {
                  this.evictEntry(node, RemovalCause.EXPIRED, now);
                  node = next;
               }
            }

         }
      }
   }

   @GuardedBy("evictionLock")
   void expireVariableEntries(long now) {
      if (this.expiresVariable()) {
         this.timerWheel().advance(this, now);
      }

   }

   @GuardedBy("evictionLock")
   long getExpirationDelay(long now) {
      long delay = Long.MAX_VALUE;
      if (this.expiresAfterAccess()) {
         Node<K, V> node = (Node)this.accessOrderWindowDeque().peekFirst();
         if (node != null) {
            long age = Math.max(0L, now - node.getAccessTime());
            delay = Math.min(delay, this.expiresAfterAccessNanos() - age);
         }

         if (this.evicts()) {
            node = (Node)this.accessOrderProbationDeque().peekFirst();
            if (node != null) {
               long age = Math.max(0L, now - node.getAccessTime());
               delay = Math.min(delay, this.expiresAfterAccessNanos() - age);
            }

            node = (Node)this.accessOrderProtectedDeque().peekFirst();
            if (node != null) {
               long age = Math.max(0L, now - node.getAccessTime());
               delay = Math.min(delay, this.expiresAfterAccessNanos() - age);
            }
         }
      }

      if (this.expiresAfterWrite()) {
         Node<K, V> node = (Node)this.writeOrderDeque().peekFirst();
         if (node != null) {
            long age = Math.max(0L, now - node.getWriteTime());
            delay = Math.min(delay, this.expiresAfterWriteNanos() - age);
         }
      }

      if (this.expiresVariable()) {
         delay = Math.min(delay, this.timerWheel().getExpirationDelay());
      }

      return delay;
   }

   boolean hasExpired(Node<K, V> node, long now, V value) {
      return this.isComputingAsync(value) ? false : (this.expiresAfterAccess() && now - node.getAccessTime() >= this.expiresAfterAccessNanos()) | (this.expiresAfterWrite() && now - node.getWriteTime() >= this.expiresAfterWriteNanos()) | (this.expiresVariable() && now - node.getVariableTime() >= 0L);
   }

   @GuardedBy("evictionLock")
   boolean evictEntry(Node<K, V> node, RemovalCause cause, long now) {
      K key = node.getKey();
      EvictContext<V> ctx = new EvictContext<V>();
      Object keyReference = node.getKeyReference();
      this.data.computeIfPresent(keyReference, (k, n) -> {
         if (n != node) {
            return n;
         } else {
            synchronized(node) {
               ctx.value = node.getValue();
               if (key != null && ctx.value != null) {
                  if (cause == RemovalCause.COLLECTED) {
                     ctx.resurrect = true;
                     return node;
                  }

                  ctx.cause = cause;
               } else {
                  ctx.cause = RemovalCause.COLLECTED;
               }

               if (ctx.cause == RemovalCause.EXPIRED) {
                  boolean expired = false;
                  if (this.expiresAfterAccess()) {
                     expired |= now - node.getAccessTime() >= this.expiresAfterAccessNanos();
                  }

                  if (this.expiresAfterWrite()) {
                     expired |= now - node.getWriteTime() >= this.expiresAfterWriteNanos();
                  }

                  if (this.expiresVariable()) {
                     expired |= now - node.getVariableTime() >= 0L;
                  }

                  if (!expired) {
                     ctx.resurrect = true;
                     return node;
                  }

                  if (this.isComputingAsync(ctx.value)) {
                     long sentinel = now + 6917529027641081854L;
                     this.setVariableTime(node, sentinel);
                     this.setAccessTime(node, sentinel);
                     this.setWriteTime(node, sentinel);
                     ctx.resurrect = true;
                     return node;
                  }
               } else if (ctx.cause == RemovalCause.SIZE) {
                  int weight = node.getWeight();
                  if (weight == 0) {
                     ctx.resurrect = true;
                     return node;
                  }
               }

               this.notifyEviction(key, ctx.value, ctx.cause);
               this.discardRefresh(keyReference);
               ctx.removed = true;
               node.retire();
               return null;
            }
         }
      });
      if (ctx.resurrect) {
         return false;
      } else {
         if (!node.inWindow() || !this.evicts() && !this.expiresAfterAccess()) {
            if (this.evicts()) {
               if (node.inMainProbation()) {
                  this.accessOrderProbationDeque().remove((AccessOrderDeque.AccessOrder)node);
               } else {
                  this.accessOrderProtectedDeque().remove((AccessOrderDeque.AccessOrder)node);
               }
            }
         } else {
            this.accessOrderWindowDeque().remove((AccessOrderDeque.AccessOrder)node);
         }

         if (this.expiresAfterWrite()) {
            this.writeOrderDeque().remove((WriteOrderDeque.WriteOrder)node);
         } else if (this.expiresVariable()) {
            this.timerWheel().deschedule(node);
         }

         synchronized(node) {
            this.logIfAlive(node);
            this.makeDead(node);
         }

         if (ctx.removed) {
            RemovalCause removeCause = (RemovalCause)Objects.requireNonNull(ctx.cause);
            this.statsCounter().recordEviction(node.getWeight(), removeCause);
            this.notifyRemoval(key, ctx.value, removeCause);
         }

         return true;
      }
   }

   @GuardedBy("evictionLock")
   void climb() {
      if (this.evicts()) {
         this.determineAdjustment();
         this.demoteFromMainProtected();
         long amount = this.adjustment();
         if (amount != 0L) {
            if (amount > 0L) {
               this.increaseWindow();
            } else {
               this.decreaseWindow();
            }

         }
      }
   }

   @GuardedBy("evictionLock")
   void determineAdjustment() {
      if (this.frequencySketch().isNotInitialized()) {
         this.setPreviousSampleHitRate((double)0.0F);
         this.setMissesInSample(0L);
         this.setHitsInSample(0L);
      } else {
         long requestCount = this.hitsInSample() + this.missesInSample();
         if (requestCount >= (long)this.frequencySketch().sampleSize) {
            double hitRate = (double)this.hitsInSample() / (double)requestCount;
            double hitRateChange = hitRate - this.previousSampleHitRate();
            double amount = hitRateChange >= (double)0.0F ? this.stepSize() : -this.stepSize();
            double nextStepSize = Math.abs(hitRateChange) >= 0.05 ? (double)0.0625F * (double)this.maximum() * (double)(amount >= (double)0.0F ? 1 : -1) : 0.98 * amount;
            this.setPreviousSampleHitRate(hitRate);
            this.setAdjustment((long)amount);
            this.setStepSize(nextStepSize);
            this.setMissesInSample(0L);
            this.setHitsInSample(0L);
         }
      }
   }

   @GuardedBy("evictionLock")
   void increaseWindow() {
      if (this.mainProtectedMaximum() != 0L) {
         long quota = Math.min(this.adjustment(), this.mainProtectedMaximum());
         this.setMainProtectedMaximum(this.mainProtectedMaximum() - quota);
         this.setWindowMaximum(this.windowMaximum() + quota);
         this.demoteFromMainProtected();

         for(int i = 0; i < 1000; ++i) {
            Node<K, V> candidate = (Node)this.accessOrderProbationDeque().peekFirst();
            boolean probation = true;
            if (candidate == null || quota < (long)candidate.getPolicyWeight()) {
               candidate = (Node)this.accessOrderProtectedDeque().peekFirst();
               probation = false;
            }

            if (candidate == null) {
               break;
            }

            int weight = candidate.getPolicyWeight();
            if (quota < (long)weight) {
               break;
            }

            quota -= (long)weight;
            if (probation) {
               this.accessOrderProbationDeque().remove((AccessOrderDeque.AccessOrder)candidate);
            } else {
               this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() - (long)weight);
               this.accessOrderProtectedDeque().remove((AccessOrderDeque.AccessOrder)candidate);
            }

            this.setWindowWeightedSize(this.windowWeightedSize() + (long)weight);
            this.accessOrderWindowDeque().offerLast(candidate);
            candidate.makeWindow();
         }

         this.setMainProtectedMaximum(this.mainProtectedMaximum() + quota);
         this.setWindowMaximum(this.windowMaximum() - quota);
         this.setAdjustment(quota);
      }
   }

   @GuardedBy("evictionLock")
   void decreaseWindow() {
      if (this.windowMaximum() > 1L) {
         long quota = Math.min(-this.adjustment(), Math.max(0L, this.windowMaximum() - 1L));
         this.setMainProtectedMaximum(this.mainProtectedMaximum() + quota);
         this.setWindowMaximum(this.windowMaximum() - quota);

         for(int i = 0; i < 1000; ++i) {
            Node<K, V> candidate = (Node)this.accessOrderWindowDeque().peekFirst();
            if (candidate == null) {
               break;
            }

            int weight = candidate.getPolicyWeight();
            if (quota < (long)weight) {
               break;
            }

            quota -= (long)weight;
            this.setWindowWeightedSize(this.windowWeightedSize() - (long)weight);
            this.accessOrderWindowDeque().remove((AccessOrderDeque.AccessOrder)candidate);
            this.accessOrderProbationDeque().offerLast(candidate);
            candidate.makeMainProbation();
         }

         this.setMainProtectedMaximum(this.mainProtectedMaximum() - quota);
         this.setWindowMaximum(this.windowMaximum() + quota);
         this.setAdjustment(-quota);
      }
   }

   @GuardedBy("evictionLock")
   void demoteFromMainProtected() {
      long mainProtectedMaximum = this.mainProtectedMaximum();
      long mainProtectedWeightedSize = this.mainProtectedWeightedSize();
      if (mainProtectedWeightedSize > mainProtectedMaximum) {
         for(int i = 0; i < 1000 && mainProtectedWeightedSize > mainProtectedMaximum; ++i) {
            Node<K, V> demoted = (Node)this.accessOrderProtectedDeque().pollFirst();
            if (demoted == null) {
               break;
            }

            demoted.makeMainProbation();
            this.accessOrderProbationDeque().offerLast(demoted);
            mainProtectedWeightedSize -= (long)demoted.getPolicyWeight();
         }

         this.setMainProtectedWeightedSize(mainProtectedWeightedSize);
      }
   }

   @Nullable V afterRead(Node<K, V> node, long now, boolean recordHit) {
      if (recordHit) {
         this.statsCounter().recordHits(1);
      }

      boolean delayable = this.skipReadBuffer() || this.readBuffer.offer(node) != 1;
      if (this.shouldDrainBuffers(delayable)) {
         this.scheduleDrainBuffers();
      }

      return (V)this.refreshIfNeeded(node, now);
   }

   boolean skipReadBuffer() {
      return this.fastpath() && this.frequencySketch().isNotInitialized();
   }

   V refreshIfNeeded(Node<K, V> node, long now) {
      if (!this.refreshAfterWrite()) {
         return null;
      } else {
         long writeTime = node.getWriteTime();
         long refreshWriteTime = writeTime | 1L;
         K key;
         V oldValue;
         Object keyReference;
         ConcurrentMap<Object, CompletableFuture<?>> refreshes;
         if (now - writeTime > this.refreshAfterWriteNanos() && (key = node.getKey()) != null && (oldValue = node.getValue()) != null && !this.isComputingAsync(oldValue) && (writeTime & 1L) == 0L && !(refreshes = this.refreshes()).containsKey(keyReference = node.getKeyReference()) && node.isAlive() && node.casWriteTime(writeTime, refreshWriteTime)) {
            long[] startTime = new long[1];
            CompletableFuture<? extends V>[] refreshFuture = new CompletableFuture[1];

            try {
               refreshes.computeIfAbsent(keyReference, (k) -> {
                  try {
                     startTime[0] = this.statsTicker().read();
                     if (this.isAsync) {
                        CompletableFuture<V> future = (CompletableFuture)oldValue;
                        if (!Async.isReady(future)) {
                           return null;
                        }

                        Objects.requireNonNull(this.cacheLoader);
                        CompletableFuture<? extends V> refresh = this.cacheLoader.asyncReload(key, future.join(), this.executor);
                        refreshFuture[0] = (CompletableFuture)Objects.requireNonNull(refresh, "Null future");
                     } else {
                        Objects.requireNonNull(this.cacheLoader);
                        CompletableFuture<? extends V> refresh = this.cacheLoader.asyncReload(key, oldValue, this.executor);
                        refreshFuture[0] = (CompletableFuture)Objects.requireNonNull(refresh, "Null future");
                     }

                     return refreshFuture[0];
                  } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     logger.log(Level.WARNING, "Exception thrown when submitting refresh task", e);
                     return null;
                  } catch (Throwable e) {
                     logger.log(Level.WARNING, "Exception thrown when submitting refresh task", e);
                     return null;
                  }
               });
            } finally {
               node.casWriteTime(refreshWriteTime, writeTime);
            }

            if (refreshFuture[0] == null) {
               return null;
            } else {
               CompletableFuture<V> refreshed = refreshFuture[0].handle((newValue, error) -> {
                  long loadTime = this.statsTicker().read() - startTime[0];
                  if (error != null) {
                     if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                        logger.log(Level.WARNING, "Exception thrown during refresh", error);
                     }

                     refreshes.remove(keyReference, refreshFuture[0]);
                     this.statsCounter().recordLoadFailure(loadTime);
                     return null;
                  } else {
                     V value = (V)(this.isAsync && newValue != null ? refreshFuture[0] : newValue);
                     RemovalCause[] cause = new RemovalCause[1];
                     boolean[] preserveTimestamps = new boolean[1];

                     V result;
                     try {
                        result = (V)this.compute(key, (Object k, Object currentValue) -> {
                           boolean removed = refreshes.remove(keyReference, refreshFuture[0]);
                           if (currentValue == null) {
                              if (value != null) {
                                 cause[0] = RemovalCause.EXPLICIT;
                              }

                              return null;
                           } else if (currentValue == value) {
                              return currentValue;
                           } else if (this.isAsync && newValue == Async.getIfReady((CompletableFuture)currentValue)) {
                              return currentValue;
                           } else if (removed && currentValue == oldValue && node.getWriteTime() == writeTime) {
                              return value;
                           } else {
                              if (value != null) {
                                 cause[0] = RemovalCause.REPLACED;
                              }

                              if (currentValue != oldValue || node.getWriteTime() != writeTime) {
                                 preserveTimestamps[0] = true;
                              }

                              return currentValue;
                           }
                        }, this.expiry(), false, true, preserveTimestamps);
                     } catch (Throwable t) {
                        logger.log(Level.WARNING, "Exception thrown during refresh", t);
                        this.statsCounter().recordLoadFailure(loadTime);
                        return null;
                     }

                     if (cause[0] != null) {
                        this.notifyRemoval(key, value, cause[0]);
                     }

                     if (newValue == null) {
                        this.statsCounter().recordLoadFailure(loadTime);
                     } else {
                        this.statsCounter().recordLoadSuccess(loadTime);
                     }

                     return result;
                  }
               });
               return (V)Async.getIfReady(refreshed);
            }
         } else {
            return null;
         }
      }
   }

   long expireAfterCreate(K key, V value, @Nullable Expiry<? super K, ? super V> expiry, long now) {
      if (this.expiresVariable()) {
         Objects.requireNonNull(expiry);
         long duration = Math.max(0L, expiry.expireAfterCreate(key, value, now));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   long expireAfterUpdate(Node<K, V> node, K key, V value, @Nullable Expiry<? super K, ? super V> expiry, long now) {
      if (this.expiresVariable()) {
         Objects.requireNonNull(expiry);
         long currentDuration = Math.max(1L, node.getVariableTime() - now);
         long duration = Math.max(0L, expiry.expireAfterUpdate(key, value, now, currentDuration));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   long expireAfterRead(Node<K, V> node, K key, V value, Expiry<K, V> expiry, long now) {
      if (this.expiresVariable()) {
         long currentDuration = Math.max(0L, node.getVariableTime() - now);
         long duration = Math.max(0L, expiry.expireAfterRead(key, value, now, currentDuration));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   void tryExpireAfterRead(Node<K, V> node, K key, V value, Expiry<K, V> expiry, long now) {
      if (this.expiresVariable()) {
         long variableTime = node.getVariableTime();
         long currentDuration = Math.max(1L, variableTime - now);
         if (!this.isAsync || currentDuration <= 4611686018427387903L) {
            long tolerance = EXPIRE_TOLERANCE;
            long duration = Math.max(0L, expiry.expireAfterRead(key, value, now, currentDuration));
            long expirationTime = this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
            if (duration <= tolerance || Math.abs(expirationTime - variableTime) > tolerance) {
               node.casVariableTime(variableTime, expirationTime);
            }

         }
      }
   }

   void setVariableTime(Node<K, V> node, long expirationTime) {
      if (this.expiresVariable()) {
         node.setVariableTime(expirationTime);
      }

   }

   void setWriteTime(Node<K, V> node, long now) {
      if (this.expiresAfterWrite() || this.refreshAfterWrite()) {
         node.setWriteTime(now & -2L);
      }

   }

   void setAccessTime(Node<K, V> node, long now) {
      if (this.expiresAfterAccess()) {
         long tolerance = EXPIRE_TOLERANCE;
         long accessTime = node.getAccessTime();
         if (this.expiresAfterAccessNanos() <= tolerance || Math.abs(now - accessTime) > tolerance) {
            node.setAccessTime(now);
         }

      }
   }

   boolean exceedsWriteTimeTolerance(Node<K, V> node, long varTime, long now) {
      long variableTime = node.getVariableTime();
      long writeTime = node.getWriteTime();
      long tolerance = EXPIRE_TOLERANCE;
      return this.expiresAfterWrite() && (this.expiresAfterWriteNanos() <= tolerance || Math.abs(now - writeTime) > tolerance) || this.refreshAfterWrite() && (this.refreshAfterWriteNanos() <= tolerance || Math.abs(now - writeTime) > tolerance) || this.expiresVariable() && Math.abs(varTime - variableTime) > tolerance;
   }

   void afterWrite(Runnable task) {
      for(int i = 0; i < 100; ++i) {
         if (this.writeBuffer.offer(task)) {
            this.scheduleAfterWrite();
            return;
         }

         this.scheduleDrainBuffers();
         Thread.onSpinWait();
      }

      this.lock();

      try {
         this.maintenance(task);
      } catch (RuntimeException e) {
         logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", e);
      } finally {
         this.evictionLock.unlock();
      }

      this.rescheduleCleanUpIfIncomplete();
   }

   void lock() {
      long remainingNanos = WARN_AFTER_LOCK_WAIT_NANOS;
      long end = System.nanoTime() + remainingNanos;
      boolean interrupted = false;

      while(true) {
         try {
            if (!this.evictionLock.tryLock(remainingNanos, TimeUnit.NANOSECONDS)) {
               logger.log(Level.WARNING, "The cache is experiencing excessive wait times for acquiring the eviction lock. This may indicate that a long-running computation has halted eviction when trying to remove the victim entry. Consider using AsyncCache to decouple the computation from the map operation.", new TimeoutException());
               this.evictionLock.lock();
               return;
            }
         } catch (InterruptedException var10) {
            remainingNanos = end - System.nanoTime();
            interrupted = true;
            continue;
         } finally {
            if (interrupted) {
               Thread.currentThread().interrupt();
            }

         }

         return;
      }
   }

   void scheduleAfterWrite() {
      int drainStatus = this.drainStatusOpaque();

      while(true) {
         switch (drainStatus) {
            case 0:
               this.casDrainStatus(0, 1);
               this.scheduleDrainBuffers();
               return;
            case 1:
               this.scheduleDrainBuffers();
               return;
            case 2:
               if (this.casDrainStatus(2, 3)) {
                  return;
               }

               drainStatus = this.drainStatusAcquire();
               break;
            case 3:
               return;
            default:
               throw new IllegalStateException("Invalid drain status: " + drainStatus);
         }
      }
   }

   void scheduleDrainBuffers() {
      if (this.drainStatusOpaque() < 2) {
         if (this.evictionLock.tryLock()) {
            try {
               int drainStatus = this.drainStatusOpaque();
               if (drainStatus < 2) {
                  this.setDrainStatusRelease(2);
                  this.executor.execute(this.drainBuffersTask);
                  return;
               }
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown when submitting maintenance task", t);
               this.maintenance((Runnable)null);
               return;
            } finally {
               this.evictionLock.unlock();
            }

         }
      }
   }

   public void cleanUp() {
      try {
         this.performCleanUp((Runnable)null);
      } catch (RuntimeException e) {
         logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", e);
      }

   }

   void performCleanUp(@Nullable Runnable task) {
      this.evictionLock.lock();

      try {
         this.maintenance(task);
      } finally {
         this.evictionLock.unlock();
      }

      this.rescheduleCleanUpIfIncomplete();
   }

   void rescheduleCleanUpIfIncomplete() {
      if (this.drainStatusOpaque() == 1) {
         if (this.executor == ForkJoinPool.commonPool()) {
            this.scheduleDrainBuffers();
         } else {
            Pacer pacer = this.pacer();
            if (pacer != null && !pacer.isScheduled() && this.evictionLock.tryLock()) {
               try {
                  if (this.drainStatusOpaque() == 1 && !pacer.isScheduled()) {
                     pacer.schedule(this.executor, this.drainBuffersTask, this.expirationTicker().read(), Pacer.TOLERANCE);
                  }
               } finally {
                  this.evictionLock.unlock();
               }
            }

         }
      }
   }

   @GuardedBy("evictionLock")
   void maintenance(@Nullable Runnable task) {
      this.setDrainStatusRelease(2);

      try {
         this.drainReadBuffer();
         this.drainWriteBuffer();
         if (task != null) {
            task.run();
         }

         this.drainKeyReferences();
         this.drainValueReferences();
         this.expireEntries();
         this.evictEntries();
         this.climb();
      } finally {
         if (this.drainStatusOpaque() != 2 || !this.casDrainStatus(2, 0)) {
            this.setDrainStatusOpaque(1);
         }

      }

   }

   @GuardedBy("evictionLock")
   void drainKeyReferences() {
      if (this.collectKeys()) {
         Reference<? extends K> keyRef;
         while((keyRef = this.keyReferenceQueue().poll()) != null) {
            Node<K, V> node = (Node)this.data.get(keyRef);
            if (node != null) {
               this.evictEntry(node, RemovalCause.COLLECTED, 0L);
            }
         }

      }
   }

   @GuardedBy("evictionLock")
   void drainValueReferences() {
      if (this.collectValues()) {
         Reference<? extends V> valueRef;
         while((valueRef = this.valueReferenceQueue().poll()) != null) {
            References.InternalReference<V> ref = (References.InternalReference)valueRef;
            Node<K, V> node = (Node)this.data.get(ref.getKeyReference());
            if (node != null && valueRef == node.getValueReference()) {
               this.evictEntry(node, RemovalCause.COLLECTED, 0L);
            }
         }

      }
   }

   @GuardedBy("evictionLock")
   void drainReadBuffer() {
      if (!this.skipReadBuffer()) {
         this.readBuffer.drainTo(this.accessPolicy);
      }

   }

   @GuardedBy("evictionLock")
   void onAccess(Node<K, V> node) {
      if (this.evicts()) {
         Object keyRef = node.getKeyReferenceOrNull();
         if (keyRef == null || !node.isAlive()) {
            return;
         }

         this.frequencySketch().increment(keyRef);
         if (node.inWindow()) {
            reorder(this.accessOrderWindowDeque(), node);
         } else if (node.inMainProbation()) {
            this.reorderProbation(node);
         } else {
            reorder(this.accessOrderProtectedDeque(), node);
         }

         this.setHitsInSample(this.hitsInSample() + 1L);
      } else if (this.expiresAfterAccess()) {
         reorder(this.accessOrderWindowDeque(), node);
      }

      if (this.expiresVariable()) {
         this.timerWheel().reschedule(node);
      }

   }

   @GuardedBy("evictionLock")
   void reorderProbation(Node<K, V> node) {
      if (this.accessOrderProbationDeque().contains((AccessOrderDeque.AccessOrder)node)) {
         if ((long)node.getPolicyWeight() > this.mainProtectedMaximum()) {
            reorder(this.accessOrderProbationDeque(), node);
         } else {
            this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() + (long)node.getPolicyWeight());
            this.accessOrderProbationDeque().remove((AccessOrderDeque.AccessOrder)node);
            this.accessOrderProtectedDeque().offerLast(node);
            node.makeMainProtected();
         }
      }
   }

   static <K, V> void reorder(LinkedDeque<Node<K, V>> deque, Node<K, V> node) {
      if (deque.contains(node)) {
         deque.moveToBack(node);
      }

   }

   @GuardedBy("evictionLock")
   void drainWriteBuffer() {
      for(int i = 0; i <= WRITE_BUFFER_MAX; ++i) {
         Runnable task = (Runnable)this.writeBuffer.poll();
         if (task == null) {
            return;
         }

         task.run();
      }

      this.setDrainStatusOpaque(3);
   }

   @GuardedBy("evictionLock")
   void makeDead(Node<K, V> node) {
      synchronized(node) {
         if (!node.isDead()) {
            if (this.evicts()) {
               if (node.inWindow()) {
                  this.setWindowWeightedSize(this.windowWeightedSize() - (long)node.getWeight());
               } else if (node.inMainProtected()) {
                  this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() - (long)node.getWeight());
               }

               this.setWeightedSize(this.weightedSize() - (long)node.getWeight());
            }

            node.die();
         }
      }
   }

   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   public int size() {
      return this.data.size();
   }

   public long estimatedSize() {
      return this.data.mappingCount();
   }

   public void clear() {
      this.evictionLock.lock();

      Deque<Node<K, V>> entries;
      try {
         this.readBuffer.drainTo((e) -> {
         });

         Runnable task;
         while((task = (Runnable)this.writeBuffer.poll()) != null) {
            task.run();
         }

         Pacer pacer = this.pacer();
         if (pacer != null) {
            pacer.cancel();
         }

         long now = this.expirationTicker().read();
         int threshold = WRITE_BUFFER_MAX / 2;
         entries = new ArrayDeque(this.data.values());

         while(!entries.isEmpty() && this.writeBuffer.size() < threshold) {
            this.removeNode((Node)entries.pollFirst(), now);
         }
      } finally {
         this.evictionLock.unlock();
      }

      boolean cleanUp = false;

      for(Node<K, V> node : entries) {
         K key = node.getKey();
         if (key == null) {
            cleanUp = true;
         } else {
            this.remove(key);
         }
      }

      if (this.collectKeys() && cleanUp) {
         this.cleanUp();
      }

   }

   @GuardedBy("evictionLock")
   void removeNode(Node<K, V> node, long now) {
      K key = node.getKey();
      EvictContext<V> ctx = new EvictContext<V>();
      Object keyReference = node.getKeyReference();
      this.data.computeIfPresent(keyReference, (k, n) -> {
         if (n != node) {
            return n;
         } else {
            synchronized(node) {
               ctx.value = node.getValue();
               ctx.oldWeight = node.getWeight();
               if (key != null && ctx.value != null) {
                  if (this.hasExpired(node, now, ctx.value)) {
                     ctx.cause = RemovalCause.EXPIRED;
                  } else {
                     ctx.cause = RemovalCause.EXPLICIT;
                  }
               } else {
                  ctx.cause = RemovalCause.COLLECTED;
               }

               if (ctx.cause.wasEvicted()) {
                  this.notifyEviction(key, ctx.value, ctx.cause);
               }

               this.discardRefresh(node.getKeyReference());
               node.retire();
               return null;
            }
         }
      });
      if (!node.inWindow() || !this.evicts() && !this.expiresAfterAccess()) {
         if (this.evicts()) {
            if (node.inMainProbation()) {
               this.accessOrderProbationDeque().remove((AccessOrderDeque.AccessOrder)node);
            } else {
               this.accessOrderProtectedDeque().remove((AccessOrderDeque.AccessOrder)node);
            }
         }
      } else {
         this.accessOrderWindowDeque().remove((AccessOrderDeque.AccessOrder)node);
      }

      if (this.expiresAfterWrite()) {
         this.writeOrderDeque().remove((WriteOrderDeque.WriteOrder)node);
      } else if (this.expiresVariable()) {
         this.timerWheel().deschedule(node);
      }

      synchronized(node) {
         this.logIfAlive(node);
         this.makeDead(node);
      }

      if (ctx.cause != null) {
         if (ctx.cause.wasEvicted()) {
            this.statsCounter().recordEviction(ctx.oldWeight, ctx.cause);
         }

         this.notifyRemoval(key, ctx.value, ctx.cause);
      }

   }

   public boolean containsKey(Object key) {
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         return false;
      } else {
         V value = node.getValue();
         return value != null && !this.hasExpired(node, this.expirationTicker().read(), value);
      }
   }

   public boolean containsValue(Object value) {
      Objects.requireNonNull(value);
      long now = this.expirationTicker().read();

      for(Node<K, V> node : this.data.values()) {
         V nodeValue = node.getValue();
         if (node.isAlive() && node.getKey() != null && nodeValue != null && node.containsValue(value) && !this.hasExpired(node, now, nodeValue)) {
            return true;
         }
      }

      return false;
   }

   public @Nullable V get(Object key) {
      return (V)this.getIfPresent(key, false);
   }

   public @Nullable V getIfPresent(Object key, boolean recordStats) {
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         if (recordStats) {
            this.statsCounter().recordMisses(1);
         }

         if (this.drainStatusOpaque() == 1) {
            this.scheduleDrainBuffers();
         }

         return null;
      } else {
         V value = node.getValue();
         long now = this.expirationTicker().read();
         if (value != null && !this.hasExpired(node, now, value)) {
            if (!this.isComputingAsync(value)) {
               this.setAccessTime(node, now);
               this.tryExpireAfterRead(node, key, value, this.expiry(), now);
            }

            V refreshed = (V)this.afterRead(node, now, recordStats);
            return (V)(refreshed == null ? value : refreshed);
         } else {
            if (recordStats) {
               this.statsCounter().recordMisses(1);
            }

            this.scheduleDrainBuffers();
            return null;
         }
      }
   }

   public @Nullable V getIfPresentQuietly(Object key) {
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      V value;
      return (V)(node != null && (value = node.getValue()) != null && !this.hasExpired(node, this.expirationTicker().read(), value) ? value : null);
   }

   public @Nullable K getKey(K key) {
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         if (this.drainStatusOpaque() == 1) {
            this.scheduleDrainBuffers();
         }

         return null;
      } else {
         this.afterRead(node, 0L, false);
         return node.getKey();
      }
   }

   public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
      LinkedHashMap<K, V> result = new LinkedHashMap(Caffeine.calculateHashMapCapacity(keys));

      for(K key : keys) {
         result.put(key, (Object)null);
      }

      int uniqueKeys = result.size();
      long now = this.expirationTicker().read();
      Iterator<Map.Entry<K, V>> iter = result.entrySet().iterator();

      while(iter.hasNext()) {
         Map.Entry<K, V> entry = (Map.Entry)iter.next();
         Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(entry.getKey()));
         V value;
         if (node != null && (value = node.getValue()) != null && !this.hasExpired(node, now, value)) {
            this.setAccessTime(node, now);
            this.tryExpireAfterRead(node, entry.getKey(), value, this.expiry(), now);
            V refreshed = (V)this.afterRead(node, now, false);
            entry.setValue(refreshed == null ? value : refreshed);
         } else {
            iter.remove();
         }
      }

      this.statsCounter().recordHits(result.size());
      this.statsCounter().recordMisses(uniqueKeys - result.size());
      Map<K, V> unmodifiable = Collections.unmodifiableMap(result);
      return unmodifiable;
   }

   public void putAll(Map<? extends K, ? extends V> map) {
      map.forEach(this::put);
   }

   public @Nullable V put(K key, V value) {
      return (V)this.put(key, value, this.expiry(), false);
   }

   public @Nullable V putIfAbsent(K key, V value) {
      return (V)this.put(key, value, this.expiry(), true);
   }

   @Nullable V put(K key, V value, Expiry<K, V> expiry, boolean onlyIfAbsent) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      int newWeight = -1;
      Node<K, V> node = null;
      long now = this.expirationTicker().read();
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      int attempts = 1;

      Node<K, V> prior;
      int oldWeight;
      boolean expired;
      boolean mayUpdate;
      boolean exceedsTolerance;
      V oldValue;
      while(true) {
         prior = (Node)this.data.get(lookupKey);
         if (prior == null) {
            if (node == null) {
               if (newWeight < 0) {
                  newWeight = this.weigher.weigh(key, value);
               }

               node = this.nodeFactory.newNode(key, this.keyReferenceQueue(), value, this.valueReferenceQueue(), newWeight, now);
               long expirationTime = this.isComputingAsync(value) ? now + 6917529027641081854L : now;
               this.setVariableTime(node, this.expireAfterCreate(key, value, expiry, now));
               this.setAccessTime(node, expirationTime);
               this.setWriteTime(node, expirationTime);
            }

            prior = (Node)this.data.putIfAbsent(node.getKeyReference(), node);
            if (prior == null) {
               this.afterWrite(new AddTask(node, newWeight));
               return null;
            }

            if (onlyIfAbsent) {
               oldValue = prior.getValue();
               if (oldValue != null && !this.hasExpired(prior, now, oldValue)) {
                  if (!this.isComputingAsync(oldValue)) {
                     this.tryExpireAfterRead(prior, key, oldValue, expiry, now);
                     this.setAccessTime(prior, now);
                  }

                  this.afterRead(prior, now, false);
                  return oldValue;
               }
            }
         } else if (onlyIfAbsent) {
            oldValue = prior.getValue();
            if (oldValue != null && !this.hasExpired(prior, now, oldValue)) {
               if (!this.isComputingAsync(oldValue)) {
                  this.tryExpireAfterRead(prior, key, oldValue, expiry, now);
                  this.setAccessTime(prior, now);
               }

               this.afterRead(prior, now, false);
               return oldValue;
            }
         }

         if (!prior.isAlive()) {
            if ((attempts & 1023) != 0) {
               Thread.onSpinWait();
            } else {
               this.data.computeIfPresent(lookupKey, (k, n) -> {
                  this.requireIsAlive(key, n);
                  return n;
               });
            }
         } else {
            expired = false;
            mayUpdate = true;
            exceedsTolerance = false;
            if (newWeight < 0) {
               newWeight = this.weigher.weigh(key, value);
            }

            synchronized(prior) {
               if (prior.isAlive()) {
                  oldValue = prior.getValue();
                  oldWeight = prior.getWeight();
                  long varTime;
                  if (oldValue == null) {
                     varTime = this.expireAfterCreate(key, value, expiry, now);
                     this.notifyEviction(key, (Object)null, RemovalCause.COLLECTED);
                  } else if (this.hasExpired(prior, now, oldValue)) {
                     expired = true;
                     varTime = this.expireAfterCreate(key, value, expiry, now);
                     this.notifyEviction(key, oldValue, RemovalCause.EXPIRED);
                  } else if (onlyIfAbsent) {
                     mayUpdate = false;
                     varTime = this.expireAfterRead(prior, key, oldValue, expiry, now);
                  } else {
                     varTime = this.expireAfterUpdate(prior, key, value, expiry, now);
                  }

                  long expirationTime = this.isComputingAsync(mayUpdate ? value : oldValue) ? now + 6917529027641081854L : now;
                  if (mayUpdate) {
                     exceedsTolerance = this.exceedsWriteTimeTolerance(prior, varTime, expirationTime);
                     if (expired || exceedsTolerance) {
                        this.setWriteTime(prior, expirationTime);
                     }

                     prior.setValue(value, this.valueReferenceQueue());
                     prior.setWeight(newWeight);
                     this.discardRefresh(prior.getKeyReference());
                  }

                  this.setVariableTime(prior, varTime);
                  this.setAccessTime(prior, expirationTime);
                  break;
               }
            }
         }

         ++attempts;
      }

      if (expired) {
         this.statsCounter().recordEviction(oldWeight, RemovalCause.EXPIRED);
         this.notifyRemoval(key, oldValue, RemovalCause.EXPIRED);
      } else if (oldValue == null) {
         this.statsCounter().recordEviction(oldWeight, RemovalCause.COLLECTED);
         this.notifyRemoval(key, (Object)null, RemovalCause.COLLECTED);
      } else if (mayUpdate) {
         this.notifyOnReplace(key, oldValue, value);
      }

      int weightedDifference = mayUpdate ? newWeight - oldWeight : 0;
      if (oldValue != null && weightedDifference == 0 && !expired) {
         if (!onlyIfAbsent && exceedsTolerance) {
            this.afterWrite(new UpdateTask(prior, weightedDifference));
         } else {
            this.afterRead(prior, now, false);
         }
      } else {
         this.afterWrite(new UpdateTask(prior, weightedDifference));
      }

      return (V)(expired ? null : oldValue);
   }

   public @Nullable V remove(Object key) {
      RemoveContext<K, V> ctx = new RemoveContext<K, V>();
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      this.data.computeIfPresent(lookupKey, (k, n) -> {
         synchronized(n) {
            this.requireIsAlive(key, n);
            ctx.oldKey = (K)n.getKey();
            ctx.oldValue = (V)n.getValue();
            ctx.oldWeight = n.getWeight();
            RemovalCause actualCause;
            if (ctx.oldKey != null && ctx.oldValue != null) {
               if (this.hasExpired(n, this.expirationTicker().read(), ctx.oldValue)) {
                  actualCause = RemovalCause.EXPIRED;
               } else {
                  actualCause = RemovalCause.EXPLICIT;
               }
            } else {
               actualCause = RemovalCause.COLLECTED;
            }

            if (actualCause.wasEvicted()) {
               this.notifyEviction(ctx.oldKey, ctx.oldValue, actualCause);
            }

            ctx.cause = actualCause;
            this.discardRefresh(k);
            ctx.node = n;
            n.retire();
            return null;
         }
      });
      if (ctx.cause != null) {
         this.afterWrite(new RemovalTask((Node)Objects.requireNonNull(ctx.node)));
         if (ctx.cause.wasEvicted()) {
            this.statsCounter().recordEviction(ctx.oldWeight, ctx.cause);
         }

         this.notifyRemoval(ctx.oldKey, ctx.oldValue, ctx.cause);
      }

      return (V)(ctx.cause == RemovalCause.EXPLICIT ? ctx.oldValue : null);
   }

   public boolean remove(Object key, @Nullable Object value) {
      Objects.requireNonNull(key);
      if (value == null) {
         return false;
      } else {
         RemoveContext<K, V> ctx = new RemoveContext<K, V>();
         Object lookupKey = this.nodeFactory.newLookupKey(key);
         this.data.computeIfPresent(lookupKey, (kR, node) -> {
            synchronized(node) {
               this.requireIsAlive(key, node);
               ctx.oldKey = (K)node.getKey();
               ctx.oldValue = (V)node.getValue();
               ctx.oldWeight = node.getWeight();
               if (ctx.oldKey != null && ctx.oldValue != null) {
                  if (this.hasExpired(node, this.expirationTicker().read(), ctx.oldValue)) {
                     ctx.cause = RemovalCause.EXPIRED;
                  } else {
                     if (!node.containsValue(value)) {
                        return node;
                     }

                     ctx.cause = RemovalCause.EXPLICIT;
                  }
               } else {
                  ctx.cause = RemovalCause.COLLECTED;
               }

               if (ctx.cause.wasEvicted()) {
                  this.notifyEviction(ctx.oldKey, ctx.oldValue, ctx.cause);
               }

               this.discardRefresh(kR);
               ctx.node = node;
               node.retire();
               return null;
            }
         });
         if (ctx.node == null) {
            return false;
         } else {
            RemovalCause removeCause = (RemovalCause)Objects.requireNonNull(ctx.cause);
            this.afterWrite(new RemovalTask(ctx.node));
            if (removeCause.wasEvicted()) {
               this.statsCounter().recordEviction(ctx.oldWeight, removeCause);
            }

            this.notifyRemoval(ctx.oldKey, ctx.oldValue, removeCause);
            return removeCause == RemovalCause.EXPLICIT;
         }
      }
   }

   public @Nullable V replace(K key, V value) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      ReplaceContext<K, V> ctx = new ReplaceContext<K, V>();
      int weight = this.weigher.weigh(key, value);
      Node<K, V> node = (Node)this.data.computeIfPresent(this.nodeFactory.newLookupKey(key), (k, n) -> {
         synchronized(n) {
            this.requireIsAlive(key, n);
            ctx.nodeKey = (K)n.getKey();
            ctx.oldValue = (V)n.getValue();
            ctx.oldWeight = n.getWeight();
            if (ctx.nodeKey != null && ctx.oldValue != null && !this.hasExpired(n, ctx.now = this.expirationTicker().read(), ctx.oldValue)) {
               long varTime = this.expireAfterUpdate(n, key, value, this.expiry(), ctx.now);
               n.setValue(value, this.valueReferenceQueue());
               n.setWeight(weight);
               long expirationTime = this.isComputingAsync(value) ? ctx.now + 6917529027641081854L : ctx.now;
               ctx.exceedsTolerance = this.exceedsWriteTimeTolerance(n, varTime, expirationTime);
               if (ctx.exceedsTolerance) {
                  this.setWriteTime(n, expirationTime);
               }

               this.setAccessTime(n, expirationTime);
               this.setVariableTime(n, varTime);
               this.discardRefresh(k);
               return n;
            } else {
               ctx.oldValue = null;
               return n;
            }
         }
      });
      if (node != null && ctx.nodeKey != null && ctx.oldValue != null) {
         int weightedDifference = weight - ctx.oldWeight;
         if (!ctx.exceedsTolerance && weightedDifference == 0) {
            this.afterRead(node, ctx.now, false);
         } else {
            this.afterWrite(new UpdateTask(node, weightedDifference));
         }

         this.notifyOnReplace(ctx.nodeKey, ctx.oldValue, value);
         return ctx.oldValue;
      } else {
         if (node != null) {
            this.scheduleDrainBuffers();
         }

         return null;
      }
   }

   public boolean replace(K key, V oldValue, V newValue) {
      return this.replace(key, oldValue, newValue, true);
   }

   public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(oldValue);
      Objects.requireNonNull(newValue);
      ReplaceContext<K, V> ctx = new ReplaceContext<K, V>();
      int weight = this.weigher.weigh(key, newValue);
      Node<K, V> node = (Node)this.data.computeIfPresent(this.nodeFactory.newLookupKey(key), (k, n) -> {
         synchronized(n) {
            this.requireIsAlive(key, n);
            ctx.nodeKey = (K)n.getKey();
            ctx.oldValue = (V)n.getValue();
            ctx.oldWeight = n.getWeight();
            if (ctx.nodeKey != null && ctx.oldValue != null && n.containsValue(oldValue) && !this.hasExpired(n, ctx.now = this.expirationTicker().read(), ctx.oldValue)) {
               long varTime = this.expireAfterUpdate(n, key, newValue, this.expiry(), ctx.now);
               n.setValue(newValue, this.valueReferenceQueue());
               n.setWeight(weight);
               long expirationTime = this.isComputingAsync(newValue) ? ctx.now + 6917529027641081854L : ctx.now;
               ctx.exceedsTolerance = this.exceedsWriteTimeTolerance(n, varTime, expirationTime);
               if (ctx.exceedsTolerance) {
                  this.setWriteTime(n, expirationTime);
               }

               this.setAccessTime(n, expirationTime);
               this.setVariableTime(n, varTime);
               if (shouldDiscardRefresh) {
                  this.discardRefresh(k);
               }

               return n;
            } else {
               ctx.oldValue = null;
               return n;
            }
         }
      });
      if (node != null && ctx.nodeKey != null && ctx.oldValue != null) {
         int weightedDifference = weight - ctx.oldWeight;
         if (!ctx.exceedsTolerance && weightedDifference == 0) {
            this.afterRead(node, ctx.now, false);
         } else {
            this.afterWrite(new UpdateTask(node, weightedDifference));
         }

         this.notifyOnReplace(ctx.nodeKey, ctx.oldValue, newValue);
         return true;
      } else {
         if (node != null) {
            this.scheduleDrainBuffers();
         }

         return false;
      }
   }

   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      Objects.requireNonNull(function);
      BiFunction<K, V, V> remappingFunction = (keyx, oldValue) -> Objects.requireNonNull(function.apply(keyx, oldValue));

      for(K key : this.keySet()) {
         Object lookupKey = this.nodeFactory.newLookupKey(key);
         this.remap(key, lookupKey, remappingFunction, this.expiry(), new ComputeContext(this.expirationTicker().read()), false);
      }

   }

   public V computeIfAbsent(K key, @Var Function<? super K, ? extends V> mappingFunction, boolean recordStats, boolean recordLoad) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(mappingFunction);
      long now = this.expirationTicker().read();
      Node<K, V> node = (Node)this.data.get(this.nodeFactory.newLookupKey(key));
      if (node != null) {
         V value = node.getValue();
         if (value != null && !this.hasExpired(node, now, value)) {
            if (!this.isComputingAsync(value)) {
               this.tryExpireAfterRead(node, key, value, this.expiry(), now);
               this.setAccessTime(node, now);
            }

            V refreshed = (V)this.afterRead(node, now, recordStats);
            return (V)(refreshed == null ? value : refreshed);
         }
      }

      if (recordStats) {
         mappingFunction = this.statsAware(mappingFunction, recordLoad);
      }

      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      return (V)this.doComputeIfAbsent(key, keyRef, mappingFunction, new ComputeContext(now), recordStats);
   }

   @Nullable V doComputeIfAbsent(K key, Object keyRef, Function<? super K, ? extends @Nullable V> mappingFunction, ComputeContext<K, V> ctx, boolean recordStats) {
      Node<K, V> node = (Node)this.data.compute(keyRef, (k, n) -> {
         if (n == null) {
            ctx.newValue = (V)mappingFunction.apply(key);
            if (ctx.newValue == null) {
               this.discardRefresh(k);
               return null;
            } else {
               ctx.now = this.expirationTicker().read();
               ctx.newWeight = this.weigher.weigh(key, ctx.newValue);
               Node<K, V> created = this.nodeFactory.newNode(k, ctx.newValue, this.valueReferenceQueue(), ctx.newWeight, ctx.now);
               long expirationTime = this.isComputingAsync(ctx.newValue) ? ctx.now + 6917529027641081854L : ctx.now;
               this.setVariableTime(created, this.expireAfterCreate(key, ctx.newValue, this.expiry(), ctx.now));
               this.setAccessTime(created, expirationTime);
               this.setWriteTime(created, expirationTime);
               this.discardRefresh(k);
               return created;
            }
         } else {
            synchronized(n) {
               this.requireIsAlive(key, n);
               ctx.nodeKey = (K)n.getKey();
               ctx.oldValue = (V)n.getValue();
               ctx.oldWeight = n.getWeight();
               RemovalCause actualCause;
               if (ctx.nodeKey != null && ctx.oldValue != null) {
                  if (!this.hasExpired(n, ctx.now, ctx.oldValue)) {
                     return n;
                  }

                  actualCause = RemovalCause.EXPIRED;
               } else {
                  actualCause = RemovalCause.COLLECTED;
               }

               ctx.cause = actualCause;
               this.notifyEviction(ctx.nodeKey, ctx.oldValue, actualCause);

               Node var10000;
               try {
                  ctx.newValue = (V)mappingFunction.apply(key);
                  if (ctx.newValue == null) {
                     this.discardRefresh(k);
                     ctx.removed = n;
                     n.retire();
                     var10000 = null;
                     return var10000;
                  }

                  ctx.now = this.expirationTicker().read();
                  ctx.newWeight = this.weigher.weigh(key, ctx.newValue);
                  long varTime = this.expireAfterCreate(key, ctx.newValue, this.expiry(), ctx.now);
                  n.setValue(ctx.newValue, this.valueReferenceQueue());
                  n.setWeight(ctx.newWeight);
                  long expirationTime = this.isComputingAsync(ctx.newValue) ? ctx.now + 6917529027641081854L : ctx.now;
                  this.setAccessTime(n, expirationTime);
                  this.setWriteTime(n, expirationTime);
                  this.setVariableTime(n, varTime);
                  this.discardRefresh(k);
                  var10000 = n;
               } catch (Throwable e) {
                  ctx.newValue = null;
                  this.discardRefresh(k);
                  ctx.exception = e;
                  ctx.removed = n;
                  n.retire();
                  return null;
               }

               return var10000;
            }
         }
      });
      if (ctx.cause != null) {
         this.statsCounter().recordEviction(ctx.oldWeight, ctx.cause);
         this.notifyRemoval(ctx.nodeKey, ctx.oldValue, ctx.cause);
      }

      if (node == null) {
         if (ctx.removed != null) {
            this.afterWrite(new RemovalTask(ctx.removed));
         }

         if (ctx.exception != null) {
            throw toUncheckedException(ctx.exception);
         } else {
            return null;
         }
      } else if (ctx.oldValue != null && ctx.newValue == null) {
         if (!this.isComputingAsync(ctx.oldValue)) {
            this.tryExpireAfterRead(node, key, ctx.oldValue, this.expiry(), ctx.now);
            this.setAccessTime(node, ctx.now);
         }

         this.afterRead(node, ctx.now, recordStats);
         return ctx.oldValue;
      } else {
         if (ctx.oldValue == null && ctx.cause == null) {
            this.afterWrite(new AddTask(node, ctx.newWeight));
         } else {
            int weightedDifference = ctx.newWeight - ctx.oldWeight;
            this.afterWrite(new UpdateTask(node, weightedDifference));
         }

         return ctx.newValue;
      }
   }

   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(remappingFunction);
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      Node<K, V> node = (Node)this.data.get(lookupKey);
      if (node == null) {
         return null;
      } else {
         V value = node.getValue();
         long now;
         if (value != null && !this.hasExpired(node, now = this.expirationTicker().read(), value)) {
            BiFunction<? super K, ? super V, ? extends V> statsAwareRemappingFunction = this.statsAware(remappingFunction, true, true);
            return (V)this.remap(key, lookupKey, statsAwareRemappingFunction, this.expiry(), new ComputeContext(now), false);
         } else {
            this.scheduleDrainBuffers();
            return null;
         }
      }
   }

   public @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, boolean recordLoad, boolean recordLoadFailure, boolean @Nullable [] preserveTimestamps) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(remappingFunction);
      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      BiFunction<? super K, ? super V, ? extends V> statsAwareRemappingFunction = this.statsAware(remappingFunction, recordLoad, recordLoadFailure);
      ComputeContext<K, V> ctx = new ComputeContext<K, V>(this.expirationTicker().read());
      ctx.preserveTimestamps = preserveTimestamps;
      return (V)this.remap(key, keyRef, statsAwareRemappingFunction, expiry, ctx, true);
   }

   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      Objects.requireNonNull(remappingFunction);
      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      BiFunction<? super K, ? super V, ? extends V> mergeFunction = (k, oldValue) -> oldValue == null ? value : this.statsAware(remappingFunction).apply(oldValue, value);
      return (V)this.remap(key, keyRef, mergeFunction, this.expiry(), new ComputeContext(this.expirationTicker().read()), true);
   }

   @Nullable V remap(K key, Object keyRef, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, ComputeContext<K, V> ctx, boolean computeIfAbsent) {
      Node<K, V> node = (Node)this.data.compute(keyRef, (kr, n) -> {
         if (n == null) {
            if (!computeIfAbsent) {
               return null;
            } else {
               ctx.newValue = (V)remappingFunction.apply(key, (Object)null);
               if (ctx.newValue == null) {
                  return null;
               } else {
                  ctx.now = this.expirationTicker().read();
                  ctx.newWeight = this.weigher.weigh(key, ctx.newValue);
                  long varTime = this.expireAfterCreate(key, ctx.newValue, expiry, ctx.now);
                  Node<K, V> created = this.nodeFactory.newNode(keyRef, ctx.newValue, this.valueReferenceQueue(), ctx.newWeight, ctx.now);
                  long expirationTime = this.isComputingAsync(ctx.newValue) ? ctx.now + 6917529027641081854L : ctx.now;
                  this.setAccessTime(created, expirationTime);
                  this.setWriteTime(created, expirationTime);
                  this.setVariableTime(created, varTime);
                  this.discardRefresh(kr);
                  return created;
               }
            }
         } else {
            synchronized(n) {
               this.requireIsAlive(key, n);
               ctx.nodeKey = (K)n.getKey();
               ctx.oldValue = (V)n.getValue();
               ctx.oldWeight = n.getWeight();
               if (ctx.nodeKey != null && ctx.oldValue != null) {
                  if (this.hasExpired(n, this.expirationTicker().read(), ctx.oldValue)) {
                     ctx.cause = RemovalCause.EXPIRED;
                  }
               } else {
                  ctx.cause = RemovalCause.COLLECTED;
               }

               if (ctx.cause != null) {
                  this.notifyEviction(ctx.nodeKey, ctx.oldValue, ctx.cause);
                  if (!computeIfAbsent) {
                     this.discardRefresh(kr);
                     ctx.removed = n;
                     n.retire();
                     return null;
                  }
               }

               boolean wasEvicted = ctx.cause != null;

               Node var10000;
               try {
                  ctx.newValue = (V)remappingFunction.apply(ctx.nodeKey, ctx.cause == null ? ctx.oldValue : null);
                  if (ctx.newValue == null) {
                     if (ctx.cause == null) {
                        ctx.cause = RemovalCause.EXPLICIT;
                     }

                     this.discardRefresh(kr);
                     ctx.removed = n;
                     n.retire();
                     var10000 = null;
                     return var10000;
                  }

                  if (ctx.preserveTimestamps != null && ctx.preserveTimestamps[0] && ctx.newValue == ctx.oldValue && ctx.cause == null) {
                     this.discardRefresh(kr);
                     var10000 = n;
                     return var10000;
                  }

                  ctx.newWeight = this.weigher.weigh(key, ctx.newValue);
                  ctx.now = this.expirationTicker().read();
                  long varTime;
                  if (ctx.cause == null) {
                     if (ctx.newValue != ctx.oldValue) {
                        ctx.cause = RemovalCause.REPLACED;
                     }

                     varTime = this.expireAfterUpdate(n, key, ctx.newValue, expiry, ctx.now);
                  } else {
                     varTime = this.expireAfterCreate(key, ctx.newValue, expiry, ctx.now);
                  }

                  if (ctx.newValue != ctx.oldValue) {
                     n.setValue(ctx.newValue, this.valueReferenceQueue());
                  }

                  n.setWeight(ctx.newWeight);
                  long expirationTime = this.isComputingAsync(ctx.newValue) ? ctx.now + 6917529027641081854L : ctx.now;
                  ctx.exceedsTolerance = this.exceedsWriteTimeTolerance(n, varTime, expirationTime);
                  if (ctx.cause != null && ctx.cause.wasEvicted() || ctx.exceedsTolerance) {
                     this.setWriteTime(n, expirationTime);
                  }

                  this.setAccessTime(n, expirationTime);
                  this.setVariableTime(n, varTime);
                  this.discardRefresh(kr);
                  var10000 = n;
               } catch (Throwable e) {
                  if (!wasEvicted) {
                     throw e;
                  }

                  ctx.newValue = null;
                  this.discardRefresh(kr);
                  ctx.exception = e;
                  ctx.removed = n;
                  n.retire();
                  return null;
               }

               return var10000;
            }
         }
      });
      if (ctx.cause != null) {
         if (ctx.cause == RemovalCause.REPLACED) {
            Objects.requireNonNull(ctx.newValue);
            this.notifyOnReplace(key, ctx.oldValue, ctx.newValue);
         } else {
            if (ctx.cause.wasEvicted()) {
               this.statsCounter().recordEviction(ctx.oldWeight, ctx.cause);
            }

            this.notifyRemoval(ctx.nodeKey, ctx.oldValue, ctx.cause);
         }
      }

      if (ctx.removed != null) {
         this.afterWrite(new RemovalTask(ctx.removed));
      } else if (node != null && (ctx.preserveTimestamps == null || !ctx.preserveTimestamps[0])) {
         if (ctx.oldValue == null && ctx.cause == null) {
            this.afterWrite(new AddTask(node, ctx.newWeight));
         } else {
            int weightedDifference = ctx.newWeight - ctx.oldWeight;
            if (!ctx.exceedsTolerance && weightedDifference == 0) {
               this.afterRead(node, ctx.now, false);
               if (ctx.cause != null && ctx.cause.wasEvicted()) {
                  this.scheduleDrainBuffers();
               }
            } else {
               this.afterWrite(new UpdateTask(node, weightedDifference));
            }
         }
      }

      if (ctx.exception != null) {
         throw toUncheckedException(ctx.exception);
      } else {
         return ctx.newValue;
      }
   }

   public void forEach(BiConsumer<? super K, ? super V> action) {
      Objects.requireNonNull(action);
      EntryIterator<K, V> iterator = new EntryIterator<K, V>(this);

      while(iterator.hasNext()) {
         action.accept(iterator.key, iterator.value);
         iterator.advance();
      }

   }

   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks == null ? (this.keySet = new KeySetView(this)) : ks;
   }

   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs == null ? (this.values = new ValuesView(this)) : vs;
   }

   public Set<Map.Entry<K, V>> entrySet() {
      Set<Map.Entry<K, V>> es = this.entrySet;
      return es == null ? (this.entrySet = new EntrySetView(this)) : es;
   }

   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Map)) {
         return false;
      } else {
         Map<?, ?> map = (Map)o;
         if (this.size() != map.size()) {
            return false;
         } else {
            long now = this.expirationTicker().read();

            for(Node<K, V> node : this.data.values()) {
               K key = node.getKey();
               V value = node.getValue();
               if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now, value)) {
                  Object val = map.get(key);
                  if (val != null && (val == value || val.equals(value))) {
                     continue;
                  }

                  return false;
               }

               this.scheduleDrainBuffers();
               return false;
            }

            return true;
         }
      }
   }

   public int hashCode() {
      int hash = 0;
      boolean drain = false;
      long now = this.expirationTicker().read();

      for(Node<K, V> node : this.data.values()) {
         K key = node.getKey();
         V value = node.getValue();
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now, value)) {
            hash += key.hashCode() ^ value.hashCode();
         } else {
            drain = true;
         }
      }

      if (drain) {
         this.scheduleDrainBuffers();
      }

      return hash;
   }

   public String toString() {
      boolean drain = false;
      long now = this.expirationTicker().read();
      StringBuilder result = (new StringBuilder()).append('{');

      for(Node<K, V> node : this.data.values()) {
         K key = node.getKey();
         V value = node.getValue();
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now, value)) {
            if (result.length() != 1) {
               result.append(',').append(' ');
            }

            result.append(key == this ? "(this Map)" : key);
            result.append('=');
            result.append(value == this ? "(this Map)" : value);
         } else {
            drain = true;
         }
      }

      if (drain) {
         this.scheduleDrainBuffers();
      }

      return result.append('}').toString();
   }

   <T> T evictionOrder(boolean hottest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Comparator<Node<K, V>> comparator = Comparator.comparingInt((node) -> {
         Object keyRef = node.getKeyReferenceOrNull();
         return keyRef == null ? 0 : this.frequencySketch().frequency(keyRef);
      });
      Iterable<Node<K, V>> iterable;
      if (hottest) {
         iterable = () -> {
            LinkedDeque.PeekingIterator<Node<K, V>> secondary = LinkedDeque.PeekingIterator.<Node<K, V>>comparing(this.accessOrderProbationDeque().descendingIterator(), this.accessOrderWindowDeque().descendingIterator(), comparator);
            return LinkedDeque.PeekingIterator.concat(this.accessOrderProtectedDeque().descendingIterator(), secondary);
         };
      } else {
         iterable = () -> {
            LinkedDeque.PeekingIterator<Node<K, V>> primary = LinkedDeque.PeekingIterator.<Node<K, V>>comparing(this.accessOrderWindowDeque().iterator(), this.accessOrderProbationDeque().iterator(), comparator.reversed());
            return LinkedDeque.PeekingIterator.concat(primary, this.accessOrderProtectedDeque().iterator());
         };
      }

      return (T)this.snapshot(iterable, transformer, mappingFunction);
   }

   <T> T expireAfterAccessOrder(boolean oldest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Iterable<Node<K, V>> iterable;
      if (this.evicts()) {
         iterable = () -> {
            Comparator<Node<K, V>> comparator = Comparator.comparingLong(Node::getAccessTime);
            LinkedDeque.PeekingIterator<Node<K, V>> first;
            LinkedDeque.PeekingIterator<Node<K, V>> second;
            LinkedDeque.PeekingIterator<Node<K, V>> third;
            if (oldest) {
               first = this.accessOrderWindowDeque().iterator();
               second = this.accessOrderProbationDeque().iterator();
               third = this.accessOrderProtectedDeque().iterator();
            } else {
               comparator = comparator.reversed();
               first = this.accessOrderWindowDeque().descendingIterator();
               second = this.accessOrderProbationDeque().descendingIterator();
               third = this.accessOrderProtectedDeque().descendingIterator();
            }

            return LinkedDeque.PeekingIterator.comparing(LinkedDeque.PeekingIterator.comparing(first, second, comparator), third, comparator);
         };
      } else {
         Object var10000;
         if (oldest) {
            var10000 = this.accessOrderWindowDeque();
         } else {
            AccessOrderDeque var5 = this.accessOrderWindowDeque();
            Objects.requireNonNull(var5);
            var10000 = var5::descendingIterator;
         }

         iterable = (Iterable<Node<K, V>>)var10000;
      }

      return (T)this.snapshot(iterable, transformer, mappingFunction);
   }

   <T> T snapshot(Iterable<Node<K, V>> iterable, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      Objects.requireNonNull(transformer);
      Objects.requireNonNull(iterable);
      this.evictionLock.lock();

      Object var5;
      try {
         this.maintenance((Runnable)null);
         Stream<Node<K, V>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(), 1297), false);

         try {
            var5 = mappingFunction.apply(stream.map((node) -> this.nodeToCacheEntry(node, transformer, node.getPolicyWeight())).filter(Objects::nonNull));
         } catch (Throwable var12) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stream != null) {
            stream.close();
         }
      } finally {
         this.evictionLock.unlock();
         this.rescheduleCleanUpIfIncomplete();
      }

      return (T)var5;
   }

   Policy.@Nullable CacheEntry<K, V> nodeToCacheEntry(Node<K, V> node, Function<@Nullable V, @Nullable V> transformer, int weight) {
      V rawValue = node.getValue();
      if (rawValue == null) {
         return null;
      } else {
         V value = (V)transformer.apply(rawValue);
         K key = node.getKey();
         long now;
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now = this.expirationTicker().read(), rawValue)) {
            long expiresAfter = Long.MAX_VALUE;
            if (this.expiresAfterAccess()) {
               expiresAfter = Math.min(expiresAfter, this.expiresAfterAccessNanos() - (now - node.getAccessTime()));
            }

            if (this.expiresAfterWrite()) {
               expiresAfter = Math.min(expiresAfter, this.expiresAfterWriteNanos() - ((now & -2L) - (node.getWriteTime() & -2L)));
            }

            if (this.expiresVariable()) {
               expiresAfter = node.getVariableTime() - now;
            }

            long refreshableAt = this.refreshAfterWrite() ? (node.getWriteTime() & -2L) + this.refreshAfterWriteNanos() : now + Long.MAX_VALUE;
            return SnapshotEntry.<K, V>forEntry(key, value, now, weight, now + expiresAfter, refreshableAt);
         } else {
            return null;
         }
      }
   }

   static <K, V> SerializationProxy<K, V> makeSerializationProxy(BoundedLocalCache<?, ?> cache) {
      SerializationProxy<K, V> proxy = new SerializationProxy<K, V>();
      proxy.weakKeys = cache.collectKeys();
      proxy.weakValues = cache.nodeFactory.weakValues();
      proxy.softValues = cache.nodeFactory.softValues();
      proxy.isRecordingStats = cache.isRecordingStats();
      proxy.evictionListener = cache.evictionListener;
      proxy.removalListener = cache.removalListener();
      proxy.ticker = cache.expirationTicker();
      if (cache.expiresAfterAccess()) {
         proxy.expiresAfterAccessNanos = cache.expiresAfterAccessNanos();
      }

      if (cache.expiresAfterWrite()) {
         proxy.expiresAfterWriteNanos = cache.expiresAfterWriteNanos();
      }

      if (cache.expiresVariable()) {
         proxy.expiry = cache.expiry();
      }

      if (cache.refreshAfterWrite()) {
         proxy.refreshAfterWriteNanos = cache.refreshAfterWriteNanos();
      }

      if (cache.evicts()) {
         if (cache.isWeighted) {
            proxy.weigher = cache.weigher;
            proxy.maximumWeight = cache.maximum();
         } else {
            proxy.maximumSize = cache.maximum();
         }
      }

      proxy.cacheLoader = cache.cacheLoader;
      proxy.async = cache.isAsync;
      return proxy;
   }

   static {
      WRITE_BUFFER_MAX = 128 * Caffeine.ceilingPowerOfTwo(NCPU);
      EXPIRE_TOLERANCE = TimeUnit.SECONDS.toNanos(1L);
      WARN_AFTER_LOCK_WAIT_NANOS = TimeUnit.SECONDS.toNanos(30L);
      REFRESHES = findVarHandle(BoundedLocalCache.class, "refreshes", ConcurrentMap.class);
   }

   final class AddTask implements Runnable {
      final Node<K, V> node;
      final int weight;

      AddTask(Node<K, V> node, int weight) {
         this.weight = weight;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         if (BoundedLocalCache.this.evicts()) {
            BoundedLocalCache.this.setWeightedSize(BoundedLocalCache.this.weightedSize() + (long)this.weight);
            BoundedLocalCache.this.setWindowWeightedSize(BoundedLocalCache.this.windowWeightedSize() + (long)this.weight);
            this.node.setPolicyWeight(this.node.getPolicyWeight() + this.weight);
            long maximum = BoundedLocalCache.this.maximum();
            if (BoundedLocalCache.this.weightedSize() >= maximum >>> 1) {
               if (BoundedLocalCache.this.weightedSize() > 9223372034707292160L) {
                  BoundedLocalCache.this.evictEntries();
               } else {
                  long capacity = BoundedLocalCache.this.isWeighted() ? BoundedLocalCache.this.data.mappingCount() : maximum;
                  BoundedLocalCache.this.frequencySketch().ensureCapacity(capacity);
               }
            }

            Object keyRef = this.node.getKeyReferenceOrNull();
            if (keyRef != null) {
               BoundedLocalCache.this.frequencySketch().increment(keyRef);
            }

            BoundedLocalCache.this.setMissesInSample(BoundedLocalCache.this.missesInSample() + 1L);
         }

         boolean isAlive;
         synchronized(this.node) {
            isAlive = this.node.isAlive();
         }

         if (isAlive) {
            if (BoundedLocalCache.this.expiresAfterWrite()) {
               BoundedLocalCache.this.writeOrderDeque().offerLast(this.node);
            }

            if (BoundedLocalCache.this.expiresVariable()) {
               BoundedLocalCache.this.timerWheel().schedule(this.node);
            }

            if (BoundedLocalCache.this.evicts()) {
               if ((long)this.weight > BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               } else if ((long)this.weight > BoundedLocalCache.this.windowMaximum()) {
                  BoundedLocalCache.this.accessOrderWindowDeque().offerFirst(this.node);
               } else {
                  BoundedLocalCache.this.accessOrderWindowDeque().offerLast(this.node);
               }
            } else if (BoundedLocalCache.this.expiresAfterAccess()) {
               BoundedLocalCache.this.accessOrderWindowDeque().offerLast(this.node);
            }
         }

      }
   }

   final class RemovalTask implements Runnable {
      final Node<K, V> node;

      RemovalTask(Node<K, V> node) {
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         if (!this.node.inWindow() || !BoundedLocalCache.this.evicts() && !BoundedLocalCache.this.expiresAfterAccess()) {
            if (BoundedLocalCache.this.evicts()) {
               if (this.node.inMainProbation()) {
                  BoundedLocalCache.this.accessOrderProbationDeque().remove((AccessOrderDeque.AccessOrder)this.node);
               } else {
                  BoundedLocalCache.this.accessOrderProtectedDeque().remove((AccessOrderDeque.AccessOrder)this.node);
               }
            }
         } else {
            BoundedLocalCache.this.accessOrderWindowDeque().remove((AccessOrderDeque.AccessOrder)this.node);
         }

         if (BoundedLocalCache.this.expiresAfterWrite()) {
            BoundedLocalCache.this.writeOrderDeque().remove((WriteOrderDeque.WriteOrder)this.node);
         } else if (BoundedLocalCache.this.expiresVariable()) {
            BoundedLocalCache.this.timerWheel().deschedule(this.node);
         }

         BoundedLocalCache.this.makeDead(this.node);
      }
   }

   final class UpdateTask implements Runnable {
      final int weightDifference;
      final Node<K, V> node;

      public UpdateTask(Node<K, V> node, int weightDifference) {
         this.weightDifference = weightDifference;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      public void run() {
         if (BoundedLocalCache.this.expiresAfterWrite()) {
            BoundedLocalCache.reorder(BoundedLocalCache.this.writeOrderDeque(), this.node);
         } else if (BoundedLocalCache.this.expiresVariable()) {
            BoundedLocalCache.this.timerWheel().reschedule(this.node);
         }

         if (BoundedLocalCache.this.evicts()) {
            int oldWeightedSize = this.node.getPolicyWeight();
            this.node.setPolicyWeight(oldWeightedSize + this.weightDifference);
            if (this.node.inWindow()) {
               BoundedLocalCache.this.setWindowWeightedSize(BoundedLocalCache.this.windowWeightedSize() + (long)this.weightDifference);
               if ((long)this.node.getPolicyWeight() > BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               } else if ((long)this.node.getPolicyWeight() <= BoundedLocalCache.this.windowMaximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else if (BoundedLocalCache.this.accessOrderWindowDeque().contains((AccessOrderDeque.AccessOrder)this.node)) {
                  BoundedLocalCache.this.accessOrderWindowDeque().moveToFront(this.node);
               }
            } else if (this.node.inMainProbation()) {
               if ((long)this.node.getPolicyWeight() <= BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               }
            } else {
               BoundedLocalCache.this.setMainProtectedWeightedSize(BoundedLocalCache.this.mainProtectedWeightedSize() + (long)this.weightDifference);
               if ((long)this.node.getPolicyWeight() <= BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               }
            }

            BoundedLocalCache.this.setWeightedSize(BoundedLocalCache.this.weightedSize() + (long)this.weightDifference);
            if (BoundedLocalCache.this.weightedSize() > 9223372034707292160L) {
               BoundedLocalCache.this.evictEntries();
            }
         } else if (BoundedLocalCache.this.expiresAfterAccess()) {
            BoundedLocalCache.this.onAccess(this.node);
         }

      }
   }

   static final class EvictContext<V> {
      @Nullable RemovalCause cause;
      @Nullable V value;
      boolean resurrect;
      boolean removed;
      int oldWeight;
   }

   static final class RemoveContext<K, V> {
      @Nullable K oldKey;
      @Nullable V oldValue;
      @Nullable Node<K, V> node;
      @Nullable RemovalCause cause;
      int oldWeight;
   }

   static final class ReplaceContext<K, V> {
      @Nullable K nodeKey;
      @Nullable V oldValue;
      long now;
      int oldWeight;
      boolean exceedsTolerance;
   }

   static final class ComputeContext<K, V> {
      @Nullable K nodeKey;
      @Nullable V oldValue;
      @Nullable V newValue;
      @Nullable Node<K, V> removed;
      @Nullable RemovalCause cause;
      @Nullable Throwable exception;
      boolean @Nullable [] preserveTimestamps;
      long now;
      int oldWeight;
      int newWeight;
      boolean exceedsTolerance;

      ComputeContext(long now) {
         this.now = now;
      }
   }

   static final class SizeLimiter<K, V> implements Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> {
      private final int expectedSize;
      private final long limit;

      SizeLimiter(int expectedSize, long limit) {
         Caffeine.requireArgument(limit >= 0L);
         this.expectedSize = expectedSize;
         this.limit = limit;
      }

      public Map<K, V> apply(Stream<Policy.CacheEntry<K, V>> stream) {
         LinkedHashMap<K, V> map = new LinkedHashMap(Caffeine.calculateHashMapCapacity(this.expectedSize));
         stream.limit(this.limit).forEach((entry) -> map.put(entry.getKey(), entry.getValue()));
         return Collections.unmodifiableMap(map);
      }
   }

   static final class WeightLimiter<K, V> implements Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> {
      private final long weightLimit;
      private long weightedSize;

      WeightLimiter(long weightLimit) {
         Caffeine.requireArgument(weightLimit >= 0L);
         this.weightLimit = weightLimit;
      }

      public Map<K, V> apply(Stream<Policy.CacheEntry<K, V>> stream) {
         LinkedHashMap<K, V> map = new LinkedHashMap();
         stream.takeWhile((entry) -> {
            this.weightedSize = Math.addExact(this.weightedSize, (long)entry.weight());
            return this.weightedSize <= this.weightLimit;
         }).forEach((entry) -> map.put(entry.getKey(), entry.getValue()));
         return Collections.unmodifiableMap(map);
      }
   }

   static final class KeySetView<K, V> extends AbstractSet<K> {
      final BoundedLocalCache<K, V> cache;

      KeySetView(BoundedLocalCache<K, V> cache) {
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         return this.cache.containsKey(o);
      }

      public boolean removeAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;
         if (this.cache.collectKeys() || collection instanceof Set && collection.size() > this.size()) {
            for(K key : this) {
               if (collection.contains(key)) {
                  modified |= this.remove(key);
               }
            }
         } else {
            for(Object item : collection) {
               modified |= item != null && this.remove(item);
            }
         }

         return modified;
      }

      public boolean remove(Object o) {
         return this.cache.remove(o) != null;
      }

      public boolean removeIf(Predicate<? super K> filter) {
         Objects.requireNonNull(filter);
         boolean modified = false;

         for(K key : this) {
            if (filter.test(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      public boolean retainAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;

         for(K key : this) {
            if (!collection.contains(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      public Iterator<K> iterator() {
         return new KeyIterator(this.cache);
      }

      public Spliterator<K> spliterator() {
         return new KeySpliterator(this.cache);
      }
   }

   static final class KeyIterator<K, V> implements Iterator<K> {
      final EntryIterator<K, V> iterator;

      KeyIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = new EntryIterator<K, V>(cache);
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public K next() {
         return this.iterator.nextKey();
      }

      public void remove() {
         this.iterator.remove();
      }
   }

   static final class KeySpliterator<K, V> implements Spliterator<K> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      KeySpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      KeySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public void forEachRemaining(Consumer<? super K> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(key);
            }

         };
         this.spliterator.forEachRemaining(consumer);
      }

      public boolean tryAdvance(Consumer<? super K> action) {
         Objects.requireNonNull(action);
         boolean[] advanced = new boolean[]{false};
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(key);
               advanced[0] = true;
            }

         };

         while(this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      public @Nullable Spliterator<K> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new KeySpliterator(this.cache, split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4353;
      }
   }

   static final class ValuesView<K, V> extends AbstractCollection<V> {
      final BoundedLocalCache<K, V> cache;

      ValuesView(BoundedLocalCache<K, V> cache) {
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         return this.cache.containsValue(o);
      }

      public boolean removeAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;

         for(EntryIterator<K, V> iterator = new EntryIterator<K, V>(this.cache); iterator.hasNext(); iterator.advance()) {
            K key = (K)Objects.requireNonNull(iterator.key);
            V value = (V)Objects.requireNonNull(iterator.value);
            if (collection.contains(value) && this.cache.remove(key, value)) {
               modified = true;
            }
         }

         return modified;
      }

      public boolean remove(@Nullable Object o) {
         if (o == null) {
            return false;
         } else {
            EntryIterator<K, V> iterator = new EntryIterator<K, V>(this.cache);

            while(iterator.hasNext()) {
               K key = (K)Objects.requireNonNull(iterator.key);
               Node<K, V> node = (Node)Objects.requireNonNull(iterator.next);
               V value = (V)Objects.requireNonNull(iterator.value);
               if (node.containsValue(o) && this.cache.remove(key, value)) {
                  return true;
               }

               iterator.advance();
            }

            return false;
         }
      }

      public boolean removeIf(Predicate<? super V> filter) {
         Objects.requireNonNull(filter);
         boolean modified = false;

         for(EntryIterator<K, V> iterator = new EntryIterator<K, V>(this.cache); iterator.hasNext(); iterator.advance()) {
            V value = (V)Objects.requireNonNull(iterator.value);
            if (filter.test(value)) {
               K key = (K)Objects.requireNonNull(iterator.key);
               modified |= this.cache.remove(key, value);
            }
         }

         return modified;
      }

      public boolean retainAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;

         for(EntryIterator<K, V> iterator = new EntryIterator<K, V>(this.cache); iterator.hasNext(); iterator.advance()) {
            K key = (K)Objects.requireNonNull(iterator.key);
            V value = (V)Objects.requireNonNull(iterator.value);
            if (!collection.contains(value) && this.cache.remove(key, value)) {
               modified = true;
            }
         }

         return modified;
      }

      public Iterator<V> iterator() {
         return new ValueIterator(this.cache);
      }

      public Spliterator<V> spliterator() {
         return new ValueSpliterator(this.cache);
      }
   }

   static final class ValueIterator<K, V> implements Iterator<V> {
      final EntryIterator<K, V> iterator;

      ValueIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = new EntryIterator<K, V>(cache);
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public V next() {
         return this.iterator.nextValue();
      }

      public void remove() {
         this.iterator.remove();
      }
   }

   static final class ValueSpliterator<K, V> implements Spliterator<V> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      ValueSpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      ValueSpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public void forEachRemaining(Consumer<? super V> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(value);
            }

         };
         this.spliterator.forEachRemaining(consumer);
      }

      public boolean tryAdvance(Consumer<? super V> action) {
         Objects.requireNonNull(action);
         boolean[] advanced = new boolean[]{false};
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(value);
               advanced[0] = true;
            }

         };

         while(this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      public @Nullable Spliterator<V> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new ValueSpliterator(this.cache, split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4352;
      }
   }

   static final class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {
      final BoundedLocalCache<K, V> cache;

      EntrySetView(BoundedLocalCache<K, V> cache) {
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public int size() {
         return this.cache.size();
      }

      public void clear() {
         this.cache.clear();
      }

      public boolean contains(Object o) {
         if (!(o instanceof Map.Entry)) {
            return false;
         } else {
            Map.Entry<?, ?> entry = (Map.Entry)o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
               Node<K, V> node = (Node)this.cache.data.get(this.cache.nodeFactory.newLookupKey(key));
               if (node == null) {
                  return false;
               } else {
                  V nodeValue = node.getValue();
                  return nodeValue != null && node.containsValue(value) && !this.cache.hasExpired(node, this.cache.expirationTicker().read(), nodeValue);
               }
            } else {
               return false;
            }
         }
      }

      public boolean removeAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;
         if (this.cache.collectKeys() || collection instanceof Set && collection.size() > this.size()) {
            for(Map.Entry<K, V> entry : this) {
               if (collection.contains(entry)) {
                  modified |= this.remove(entry);
               }
            }
         } else {
            for(Object item : collection) {
               modified |= item != null && this.remove(item);
            }
         }

         return modified;
      }

      public boolean remove(Object o) {
         if (!(o instanceof Map.Entry)) {
            return false;
         } else {
            Map.Entry<?, ?> entry = (Map.Entry)o;
            Object key = entry.getKey();
            return key != null && this.cache.remove(key, entry.getValue());
         }
      }

      public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
         Objects.requireNonNull(filter);
         boolean modified = false;

         for(Map.Entry<K, V> entry : this) {
            if (filter.test(entry)) {
               modified |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return modified;
      }

      public boolean retainAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;

         for(Map.Entry<K, V> entry : this) {
            if (!collection.contains(entry) && this.remove(entry)) {
               modified = true;
            }
         }

         return modified;
      }

      public Iterator<Map.Entry<K, V>> iterator() {
         return new EntryIterator(this.cache);
      }

      public Spliterator<Map.Entry<K, V>> spliterator() {
         return new EntrySpliterator(this.cache);
      }
   }

   static final class EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
      final BoundedLocalCache<K, V> cache;
      final Iterator<Node<K, V>> iterator;
      @Nullable K key;
      @Nullable V value;
      @Nullable K removalKey;
      @Nullable Node<K, V> next;

      EntryIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = cache.data.values().iterator();
         this.cache = cache;
      }

      public boolean hasNext() {
         if (this.next != null) {
            return true;
         } else {
            for(long now = this.cache.expirationTicker().read(); this.iterator.hasNext(); this.advance()) {
               this.next = (Node)this.iterator.next();
               this.value = this.next.getValue();
               this.key = this.next.getKey();
               boolean evictable = this.key == null || this.value == null || this.cache.hasExpired(this.next, now, this.value);
               if (!evictable && this.next.isAlive()) {
                  return true;
               }

               if (evictable) {
                  this.cache.scheduleDrainBuffers();
               }
            }

            return false;
         }
      }

      void advance() {
         this.value = null;
         this.next = null;
         this.key = null;
      }

      K nextKey() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.removalKey = this.key;
            this.advance();
            return (K)Objects.requireNonNull(this.removalKey);
         }
      }

      V nextValue() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.removalKey = this.key;
            V val = this.value;
            this.advance();
            return (V)Objects.requireNonNull(val);
         }
      }

      public Map.Entry<K, V> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            WriteThroughEntry<K, V> entry = new WriteThroughEntry<K, V>(this.cache, Objects.requireNonNull(this.key), Objects.requireNonNull(this.value));
            this.removalKey = this.key;
            this.advance();
            return entry;
         }
      }

      public void remove() {
         if (this.removalKey == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.removalKey);
            this.removalKey = null;
         }
      }
   }

   static final class EntrySpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      EntrySpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      EntrySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         this.cache = (BoundedLocalCache)Objects.requireNonNull(cache);
      }

      public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(new WriteThroughEntry(this.cache, key, value));
            }

         };
         this.spliterator.forEachRemaining(consumer);
      }

      public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
         Objects.requireNonNull(action);
         boolean[] advanced = new boolean[]{false};
         Consumer<Node<K, V>> consumer = (node) -> {
            K key = (K)node.getKey();
            V value = (V)node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now, value)) {
               action.accept(new WriteThroughEntry(this.cache, key, value));
               advanced[0] = true;
            }

         };

         while(this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      public @Nullable Spliterator<Map.Entry<K, V>> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new EntrySpliterator(this.cache, split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4353;
      }
   }

   static final class PerformCleanupTask extends ForkJoinTask<@Nullable Void> implements Runnable {
      private static final long serialVersionUID = 1L;
      final WeakReference<BoundedLocalCache<?, ?>> reference;

      PerformCleanupTask(BoundedLocalCache<?, ?> cache) {
         this.reference = new WeakReference(cache);
      }

      public boolean exec() {
         try {
            this.run();
         } catch (Throwable t) {
            BoundedLocalCache.logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", t);
         }

         return false;
      }

      public void run() {
         BoundedLocalCache<?, ?> cache = (BoundedLocalCache)this.reference.get();
         if (cache != null) {
            cache.performCleanUp((Runnable)null);
         }

      }

      public void complete(@Nullable Void value) {
      }

      public void setRawResult(@Nullable Void value) {
      }

      public @Nullable Void getRawResult() {
         return null;
      }

      public void completeExceptionally(@Nullable Throwable t) {
      }

      public boolean cancel(boolean mayInterruptIfRunning) {
         return false;
      }
   }

   static class BoundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, V> cache;
      @Nullable Policy<K, V> policy;

      BoundedLocalManualCache(Caffeine<K, V> builder) {
         this(builder, (CacheLoader)null);
      }

      BoundedLocalManualCache(Caffeine<K, V> builder, @Nullable CacheLoader<? super K, V> loader) {
         this.cache = LocalCacheFactory.<K, V>newBoundedLocalCache(builder, loader, false);
      }

      public final BoundedLocalCache<K, V> cache() {
         return this.cache;
      }

      public final Policy<K, V> policy() {
         if (this.policy == null) {
            Function<V, V> identity = (v) -> v;
            this.policy = new BoundedPolicy<K, V>(this.cache, identity, this.cache.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedPolicy<K, V> implements Policy<K, V> {
      final Function<@Nullable V, @Nullable V> transformer;
      final BoundedLocalCache<K, V> cache;
      final boolean isWeighted;
      @Nullable Optional<Policy.Eviction<K, V>> eviction;
      @Nullable Optional<Policy.FixedRefresh<K, V>> refreshes;
      @Nullable Optional<Policy.FixedExpiration<K, V>> afterWrite;
      @Nullable Optional<Policy.FixedExpiration<K, V>> afterAccess;
      @Nullable Optional<Policy.VarExpiration<K, V>> variable;

      BoundedPolicy(BoundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer, boolean isWeighted) {
         this.transformer = transformer;
         this.isWeighted = isWeighted;
         this.cache = cache;
      }

      public boolean isRecordingStats() {
         return this.cache.isRecordingStats();
      }

      public @Nullable V getIfPresentQuietly(K key) {
         return (V)this.transformer.apply(this.cache.getIfPresentQuietly(key));
      }

      public Policy.@Nullable CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
         Node<K, V> node = (Node)this.cache.data.get(this.cache.nodeFactory.newLookupKey(key));
         return node == null ? null : this.cache.nodeToCacheEntry(node, this.transformer, node.getWeight());
      }

      public Map<K, CompletableFuture<V>> refreshes() {
         ConcurrentMap<Object, CompletableFuture<?>> refreshes = this.cache.refreshes;
         if (refreshes != null && !refreshes.isEmpty()) {
            if (this.cache.collectKeys()) {
               IdentityHashMap<K, CompletableFuture<V>> inFlight = new IdentityHashMap(refreshes.size());

               for(Map.Entry<Object, CompletableFuture<?>> entry : refreshes.entrySet()) {
                  K key = (K)((References.InternalReference)entry.getKey()).get();
                  CompletableFuture<V> future = (CompletableFuture)entry.getValue();
                  if (key != null) {
                     inFlight.put(key, future);
                  }
               }

               return Collections.unmodifiableMap(inFlight);
            } else {
               Map<K, CompletableFuture<V>> castedRefreshes = (Map)refreshes;
               return Collections.unmodifiableMap(new HashMap(castedRefreshes));
            }
         } else {
            Map<K, CompletableFuture<V>> emptyMap = Collections.unmodifiableMap(Collections.emptyMap());
            return emptyMap;
         }
      }

      public Optional<Policy.Eviction<K, V>> eviction() {
         return this.cache.evicts() ? (this.eviction == null ? (this.eviction = Optional.of(new BoundedEviction())) : this.eviction) : Optional.empty();
      }

      public Optional<Policy.FixedExpiration<K, V>> expireAfterAccess() {
         if (!this.cache.expiresAfterAccess()) {
            return Optional.empty();
         } else {
            return this.afterAccess == null ? (this.afterAccess = Optional.of(new BoundedExpireAfterAccess())) : this.afterAccess;
         }
      }

      public Optional<Policy.FixedExpiration<K, V>> expireAfterWrite() {
         if (!this.cache.expiresAfterWrite()) {
            return Optional.empty();
         } else {
            return this.afterWrite == null ? (this.afterWrite = Optional.of(new BoundedExpireAfterWrite())) : this.afterWrite;
         }
      }

      public Optional<Policy.VarExpiration<K, V>> expireVariably() {
         if (!this.cache.expiresVariable()) {
            return Optional.empty();
         } else {
            return this.variable == null ? (this.variable = Optional.of(new BoundedVarExpiration())) : this.variable;
         }
      }

      public Optional<Policy.FixedRefresh<K, V>> refreshAfterWrite() {
         if (!this.cache.refreshAfterWrite()) {
            return Optional.empty();
         } else {
            return this.refreshes == null ? (this.refreshes = Optional.of(new BoundedRefreshAfterWrite())) : this.refreshes;
         }
      }

      final class BoundedEviction implements Policy.Eviction<K, V> {
         public boolean isWeighted() {
            return BoundedPolicy.this.isWeighted;
         }

         public OptionalInt weightOf(K key) {
            Objects.requireNonNull(key);
            if (!BoundedPolicy.this.isWeighted) {
               return OptionalInt.empty();
            } else {
               Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(BoundedPolicy.this.cache.nodeFactory.newLookupKey(key));
               if (node == null) {
                  return OptionalInt.empty();
               } else {
                  V value = node.getValue();
                  if (value != null && !BoundedPolicy.this.cache.hasExpired(node, BoundedPolicy.this.cache.expirationTicker().read(), value)) {
                     synchronized(node) {
                        return node.isAlive() ? OptionalInt.of(node.getWeight()) : OptionalInt.empty();
                     }
                  } else {
                     return OptionalInt.empty();
                  }
               }
            }
         }

         public OptionalLong weightedSize() {
            return BoundedPolicy.this.isWeighted ? OptionalLong.of(Math.max(0L, BoundedPolicy.this.cache.weightedSizeAcquire())) : OptionalLong.empty();
         }

         public long getMaximum() {
            return BoundedPolicy.this.cache.maximumAcquire();
         }

         public void setMaximum(long maximum) {
            BoundedPolicy.this.cache.evictionLock.lock();

            try {
               BoundedPolicy.this.cache.setMaximumSize(maximum);
               BoundedPolicy.this.cache.maintenance((Runnable)null);
            } finally {
               BoundedPolicy.this.cache.evictionLock.unlock();
               BoundedPolicy.this.cache.rescheduleCleanUpIfIncomplete();
            }

         }

         public Map<K, V> coldest(int limit) {
            int expectedSize = Math.min(limit, BoundedPolicy.this.cache.size());
            SizeLimiter<K, V> limiter = new SizeLimiter<K, V>(expectedSize, (long)limit);
            return (Map)BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, limiter);
         }

         public Map<K, V> coldestWeighted(long weightLimit) {
            Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> limiter = (Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>>)(this.isWeighted() ? new WeightLimiter(weightLimit) : new SizeLimiter((int)Math.min(weightLimit, (long)BoundedPolicy.this.cache.size()), weightLimit));
            return (Map)BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, limiter);
         }

         public <T> T coldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return (T)BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, mappingFunction);
         }

         public Map<K, V> hottest(int limit) {
            int expectedSize = Math.min(limit, BoundedPolicy.this.cache.size());
            SizeLimiter<K, V> limiter = new SizeLimiter<K, V>(expectedSize, (long)limit);
            return (Map)BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, limiter);
         }

         public Map<K, V> hottestWeighted(long weightLimit) {
            Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> limiter = (Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>>)(this.isWeighted() ? new WeightLimiter(weightLimit) : new SizeLimiter((int)Math.min(weightLimit, (long)BoundedPolicy.this.cache.size()), weightLimit));
            return (Map)BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, limiter);
         }

         public <T> T hottest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return (T)BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedExpireAfterAccess implements Policy.FixedExpiration<K, V> {
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            } else {
               V value = node.getValue();
               if (value == null) {
                  return OptionalLong.empty();
               } else {
                  long now = BoundedPolicy.this.cache.expirationTicker().read();
                  return BoundedPolicy.this.cache.hasExpired(node, now, value) ? OptionalLong.empty() : OptionalLong.of(unit.convert(now - node.getAccessTime(), TimeUnit.NANOSECONDS));
               }
            }
         }

         public long getExpiresAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.expiresAfterAccessNanos(), TimeUnit.NANOSECONDS);
         }

         public void setExpiresAfter(long duration, TimeUnit unit) {
            Caffeine.requireArgument(duration >= 0L);
            BoundedPolicy.this.cache.setExpiresAfterAccessNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }

         public Map<K, V> oldest(int limit) {
            return (Map)this.oldest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return (T)BoundedPolicy.this.cache.expireAfterAccessOrder(true, BoundedPolicy.this.transformer, mappingFunction);
         }

         public Map<K, V> youngest(int limit) {
            return (Map)this.youngest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return (T)BoundedPolicy.this.cache.expireAfterAccessOrder(false, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedExpireAfterWrite implements Policy.FixedExpiration<K, V> {
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            } else {
               V value = node.getValue();
               if (value == null) {
                  return OptionalLong.empty();
               } else {
                  long now = BoundedPolicy.this.cache.expirationTicker().read();
                  return BoundedPolicy.this.cache.hasExpired(node, now, value) ? OptionalLong.empty() : OptionalLong.of(unit.convert((now & -2L) - (node.getWriteTime() & -2L), TimeUnit.NANOSECONDS));
               }
            }
         }

         public long getExpiresAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.expiresAfterWriteNanos(), TimeUnit.NANOSECONDS);
         }

         public void setExpiresAfter(long duration, TimeUnit unit) {
            Caffeine.requireArgument(duration >= 0L);
            BoundedPolicy.this.cache.setExpiresAfterWriteNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }

         public Map<K, V> oldest(int limit) {
            return (Map)this.oldest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return (T)BoundedPolicy.this.cache.snapshot(BoundedPolicy.this.cache.writeOrderDeque(), BoundedPolicy.this.transformer, mappingFunction);
         }

         public Map<K, V> youngest(int limit) {
            return (Map)this.youngest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            BoundedLocalCache var10000 = BoundedPolicy.this.cache;
            WriteOrderDeque var10001 = BoundedPolicy.this.cache.writeOrderDeque();
            Objects.requireNonNull(var10001);
            return (T)var10000.snapshot(var10001::descendingIterator, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedVarExpiration implements Policy.VarExpiration<K, V> {
         public OptionalLong getExpiresAfter(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            } else {
               V value = node.getValue();
               if (value == null) {
                  return OptionalLong.empty();
               } else {
                  long now = BoundedPolicy.this.cache.expirationTicker().read();
                  return BoundedPolicy.this.cache.hasExpired(node, now, value) ? OptionalLong.empty() : OptionalLong.of(unit.convert(node.getVariableTime() - now, TimeUnit.NANOSECONDS));
               }
            }
         }

         public void setExpiresAfter(K key, long duration, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Caffeine.requireArgument(duration >= 0L);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(lookupKey);
            if (node != null) {
               long durationNanos = TimeUnit.NANOSECONDS.convert(duration, unit);
               long now;
               synchronized(node) {
                  now = BoundedPolicy.this.cache.expirationTicker().read();
                  V value = node.getValue();
                  if (value == null || BoundedPolicy.this.cache.isComputingAsync(value) || BoundedPolicy.this.cache.hasExpired(node, now, value)) {
                     return;
                  }

                  node.setVariableTime(now + Math.min(durationNanos, 4611686018427387903L));
               }

               BoundedPolicy.this.cache.afterRead(node, now, false);
            }

         }

         public @Nullable V put(K key, V value, long duration, TimeUnit unit) {
            Objects.requireNonNull(unit);
            Objects.requireNonNull(value);
            Caffeine.requireArgument(duration >= 0L);
            return (V)(BoundedPolicy.this.cache.isAsync ? this.putAsync(key, value, duration, unit) : this.putSync(key, value, duration, unit, false));
         }

         public @Nullable V putIfAbsent(K key, V value, long duration, TimeUnit unit) {
            Objects.requireNonNull(unit);
            Objects.requireNonNull(value);
            Caffeine.requireArgument(duration >= 0L);
            return (V)(BoundedPolicy.this.cache.isAsync ? this.putIfAbsentAsync(key, value, duration, unit) : this.putSync(key, value, duration, unit, true));
         }

         @Nullable V putSync(K key, V value, long duration, TimeUnit unit, boolean onlyIfAbsent) {
            FixedExpireAfterWrite<K, V> expiry = new FixedExpireAfterWrite<K, V>(duration, unit);
            return BoundedPolicy.this.cache.put(key, value, expiry, onlyIfAbsent);
         }

         @Nullable V putIfAbsentAsync(K key, V value, long duration, TimeUnit unit) {
            Expiry<K, V> expiry = new Async.AsyncExpiry<K, V>(new FixedExpireAfterWrite(duration, unit));
            V asyncValue = (V)CompletableFuture.completedFuture(value);

            while(true) {
               CompletableFuture<V> priorFuture = (CompletableFuture)BoundedPolicy.this.cache.getIfPresent(key, false);
               if (priorFuture != null) {
                  if (!priorFuture.isDone()) {
                     Async.getWhenSuccessful(priorFuture);
                     continue;
                  }

                  V prior = (V)Async.getWhenSuccessful(priorFuture);
                  if (prior != null) {
                     return prior;
                  }
               }

               boolean[] added = new boolean[]{false};
               CompletableFuture<V> computed = (CompletableFuture)BoundedPolicy.this.cache.compute(key, (Object k, Object oldValue) -> {
                  CompletableFuture<V> oldValueFuture = (CompletableFuture)oldValue;
                  added[0] = oldValueFuture == null || oldValueFuture.isDone() && Async.getIfReady(oldValueFuture) == null;
                  return added[0] ? asyncValue : oldValue;
               }, expiry, false, false);
               if (added[0]) {
                  return null;
               }

               V prior = (V)Async.getWhenSuccessful(computed);
               if (prior != null) {
                  return prior;
               }
            }
         }

         @Nullable V putAsync(K key, V value, long duration, TimeUnit unit) {
            Expiry<K, V> expiry = new Async.AsyncExpiry<K, V>(new FixedExpireAfterWrite(duration, unit));
            V asyncValue = (V)CompletableFuture.completedFuture(value);
            CompletableFuture<V> oldValueFuture = (CompletableFuture)BoundedPolicy.this.cache.put(key, asyncValue, expiry, false);
            return (V)Async.getWhenSuccessful(oldValueFuture);
         }

         public @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Duration duration) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(duration);
            Objects.requireNonNull(remappingFunction);
            Caffeine.requireArgument(!duration.isNegative(), "duration cannot be negative: %s", duration);
            FixedExpireAfterWrite<K, V> expiry = new FixedExpireAfterWrite<K, V>(Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
            return (V)(BoundedPolicy.this.cache.isAsync ? this.computeAsync(key, remappingFunction, expiry) : BoundedPolicy.this.cache.compute(key, remappingFunction, expiry, true, true));
         }

         V computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Expiry<? super K, ? super V> expiry) {
            LocalCache<K, CompletableFuture<V>> delegate = BoundedPolicy.this.cache;
            V[] newValue = (V[])(new Object[1]);

            CompletableFuture<V> valueFuture;
            do {
               Async.getWhenSuccessful(delegate.getIfPresentQuietly(key));
               valueFuture = delegate.compute(key, (k, oldValueFuture) -> {
                  if (oldValueFuture != null && !oldValueFuture.isDone()) {
                     return oldValueFuture;
                  } else {
                     V oldValue = (V)Async.getIfReady(oldValueFuture);
                     BiFunction<? super K, ? super V, ? extends V> function = delegate.<K, V, V>statsAware(remappingFunction, true, true);
                     newValue[0] = function.apply(key, oldValue);
                     return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
                  }
               }, new Async.AsyncExpiry(expiry), false, false);
               if (newValue[0] != null) {
                  return (V)newValue[0];
               }
            } while(valueFuture != null);

            return null;
         }

         public Map<K, V> oldest(int limit) {
            return (Map)this.oldest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return (T)BoundedPolicy.this.cache.snapshot(BoundedPolicy.this.cache.timerWheel(), BoundedPolicy.this.transformer, mappingFunction);
         }

         public Map<K, V> youngest(int limit) {
            return (Map)this.youngest(new SizeLimiter(Math.min(limit, BoundedPolicy.this.cache.size()), (long)limit));
         }

         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            BoundedLocalCache var10000 = BoundedPolicy.this.cache;
            TimerWheel var10001 = BoundedPolicy.this.cache.timerWheel();
            Objects.requireNonNull(var10001);
            return (T)var10000.snapshot(var10001::descendingIterator, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      static final class FixedExpireAfterWrite<K, V> implements Expiry<K, V> {
         final long duration;
         final TimeUnit unit;

         FixedExpireAfterWrite(long duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
         }

         public long expireAfterCreate(K key, V value, long currentTime) {
            return this.unit.toNanos(this.duration);
         }

         public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
            return this.unit.toNanos(this.duration);
         }

         @CanIgnoreReturnValue
         public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
            return currentDuration;
         }
      }

      final class BoundedRefreshAfterWrite implements Policy.FixedRefresh<K, V> {
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = (Node)BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            } else {
               V value = node.getValue();
               if (value == null) {
                  return OptionalLong.empty();
               } else {
                  long now = BoundedPolicy.this.cache.expirationTicker().read();
                  return BoundedPolicy.this.cache.hasExpired(node, now, value) ? OptionalLong.empty() : OptionalLong.of(unit.convert((now & -2L) - (node.getWriteTime() & -2L), TimeUnit.NANOSECONDS));
               }
            }
         }

         public long getRefreshesAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.refreshAfterWriteNanos(), TimeUnit.NANOSECONDS);
         }

         public void setRefreshesAfter(long duration, TimeUnit unit) {
            Objects.requireNonNull(unit);
            Caffeine.requireArgument(duration > 0L, "duration must be positive: %s %s", duration, unit);
            BoundedPolicy.this.cache.setRefreshAfterWriteNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }
      }
   }

   static final class BoundedLocalLoadingCache<K, V> extends BoundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final Function<K, @Nullable V> mappingFunction;
      final @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

      BoundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> loader) {
         super(builder, loader);
         Objects.requireNonNull(loader);
         this.mappingFunction = LocalLoadingCache.<K, V>newMappingFunction(loader);
         this.bulkMappingFunction = LocalLoadingCache.newBulkMappingFunction(loader);
      }

      public AsyncCacheLoader<? super K, V> cacheLoader() {
         return this.cache.cacheLoader;
      }

      public Function<K, @Nullable V> mappingFunction() {
         return this.mappingFunction;
      }

      public @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
         return this.bulkMappingFunction;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, CompletableFuture<V>> cache;
      final boolean isWeighted;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      LocalAsyncCache.@Nullable CacheView<K, V> cacheView;
      @Nullable Policy<K, V> policy;

      BoundedLocalAsyncCache(Caffeine<K, V> builder) {
         this.cache = LocalCacheFactory.<K, CompletableFuture<V>>newBoundedLocalCache(builder, (AsyncCacheLoader)null, true);
         this.isWeighted = builder.isWeighted();
      }

      public BoundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<K, CompletableFuture<V>>(this)) : this.mapView;
      }

      public Cache<K, V> synchronous() {
         return this.cacheView == null ? (this.cacheView = new LocalAsyncCache.CacheView<K, V>(this)) : this.cacheView;
      }

      public Policy<K, V> policy() {
         if (this.policy == null) {
            BoundedLocalCache<K, V> castCache = this.cache;
            Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
            this.policy = new BoundedPolicy<K, V>(castCache, transformer, this.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, CompletableFuture<V>> cache;
      final boolean isWeighted;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable Policy<K, V> policy;

      BoundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
         super(loader);
         this.isWeighted = builder.isWeighted();
         this.cache = LocalCacheFactory.<K, CompletableFuture<V>>newBoundedLocalCache(builder, loader, true);
      }

      public BoundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<K, CompletableFuture<V>>(this)) : this.mapView;
      }

      public Policy<K, V> policy() {
         if (this.policy == null) {
            BoundedLocalCache<K, V> castCache = this.cache;
            Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
            this.policy = new BoundedPolicy<K, V>(castCache, transformer, this.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }
}
