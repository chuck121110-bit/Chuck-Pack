package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Var;
import java.lang.ref.ReferenceQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

final class TimerWheel<K, V> implements Iterable<Node<K, V>> {
   static final int[] BUCKETS = new int[]{64, 64, 32, 4, 1};
   static final long[] SPANS;
   static final long[] SHIFT;
   final Node<K, V>[][] wheel;
   long nanos;

   TimerWheel() {
      this.wheel = new Node[BUCKETS.length][];

      for(int i = 0; i < this.wheel.length; ++i) {
         this.wheel[i] = new Node[BUCKETS[i]];

         for(int j = 0; j < this.wheel[i].length; ++j) {
            this.wheel[i][j] = new Sentinel();
         }
      }

   }

   public void advance(BoundedLocalCache<K, V> cache, long currentTimeNanos) {
      long previousTimeNanos = this.nanos;
      this.nanos = currentTimeNanos;
      long previousDelta = previousTimeNanos;
      long currentDelta = currentTimeNanos;
      if (previousTimeNanos < 0L && currentTimeNanos >= 0L) {
         previousDelta = previousTimeNanos + Long.MAX_VALUE;
         currentDelta = currentTimeNanos + Long.MAX_VALUE;
      }

      try {
         for(int i = 0; i < SHIFT.length; ++i) {
            long delta = (currentDelta >>> (int)SHIFT[i]) - (previousDelta >>> (int)SHIFT[i]);
            if (delta <= 0L) {
               break;
            }

            long previousTicks = previousTimeNanos >>> (int)SHIFT[i];
            this.expire(cache, i, previousTicks, delta);
         }

      } catch (Throwable t) {
         this.nanos = previousTimeNanos;
         throw t;
      }
   }

   void expire(BoundedLocalCache<K, V> cache, int index, long previousTicks, long delta) {
      Node<K, V>[] timerWheel = this.wheel[index];
      int mask = timerWheel.length - 1;
      int steps = (int)Math.min(1L + delta, (long)timerWheel.length);
      int start = (int)(previousTicks & (long)mask);
      int end = start + steps;

      for(int i = start; i < end; ++i) {
         Node<K, V> sentinel = timerWheel[i & mask];
         Node<K, V> prev = sentinel.getPreviousInVariableOrder();
         Node<K, V> node = sentinel.getNextInVariableOrder();
         sentinel.setPreviousInVariableOrder(sentinel);
         sentinel.setNextInVariableOrder(sentinel);

         while(node != sentinel) {
            Node<K, V> next = node.getNextInVariableOrder();
            node.setPreviousInVariableOrder((Node)null);
            node.setNextInVariableOrder((Node)null);

            try {
               if (node.getVariableTime() - this.nanos > 0L || !cache.evictEntry(node, RemovalCause.EXPIRED, this.nanos)) {
                  this.schedule(node);
               }

               node = next;
            } catch (Throwable t) {
               node.setPreviousInVariableOrder(sentinel.getPreviousInVariableOrder());
               node.setNextInVariableOrder(next);
               sentinel.getPreviousInVariableOrder().setNextInVariableOrder(node);
               sentinel.setPreviousInVariableOrder(prev);
               throw t;
            }
         }
      }

   }

   public void schedule(Node<K, V> node) {
      Node<K, V> sentinel = this.findBucket(node.getVariableTime());
      this.link(sentinel, node);
   }

   public void reschedule(Node<K, V> node) {
      if (node.getNextInVariableOrder() != null) {
         this.unlink(node);
         this.schedule(node);
      }

   }

   public void deschedule(Node<K, V> node) {
      this.unlink(node);
      node.setNextInVariableOrder((Node)null);
      node.setPreviousInVariableOrder((Node)null);
   }

   Node<K, V> findBucket(@Var long time) {
      long duration = Math.max(0L, time - this.nanos);
      if (duration == 0L) {
         time = this.nanos;
      }

      int length = this.wheel.length - 1;

      for(int i = 0; i < length; ++i) {
         if (duration < SPANS[i + 1]) {
            long ticks = time >>> (int)SHIFT[i];
            int index = (int)(ticks & (long)(this.wheel[i].length - 1));
            return this.wheel[i][index];
         }
      }

      return this.wheel[length][0];
   }

   void link(Node<K, V> sentinel, Node<K, V> node) {
      node.setPreviousInVariableOrder(sentinel.getPreviousInVariableOrder());
      node.setNextInVariableOrder(sentinel);
      sentinel.getPreviousInVariableOrder().setNextInVariableOrder(node);
      sentinel.setPreviousInVariableOrder(node);
   }

   void unlink(Node<K, V> node) {
      Node<K, V> next = node.getNextInVariableOrder();
      if (next != null) {
         Node<K, V> prev = node.getPreviousInVariableOrder();
         next.setPreviousInVariableOrder(prev);
         prev.setNextInVariableOrder(next);
      }

   }

   public long getExpirationDelay() {
      for(int i = 0; i < SHIFT.length; ++i) {
         Node<K, V>[] timerWheel = this.wheel[i];
         long ticks = this.nanos >>> (int)SHIFT[i];
         long spanMask = SPANS[i] - 1L;
         int mask = timerWheel.length - 1;
         int start = (int)(ticks & (long)mask);
         int end = start + timerWheel.length;

         for(int j = start; j < end; ++j) {
            Node<K, V> sentinel = timerWheel[j & mask];
            Node<K, V> next = sentinel.getNextInVariableOrder();
            if (next != sentinel) {
               long buckets = (long)(j - start);
               long delay = (buckets << (int)SHIFT[i]) - (this.nanos & spanMask);
               delay = delay > 0L ? delay : SPANS[i];

               for(int k = i + 1; k < SHIFT.length; ++k) {
                  long nextDelay = this.peekAhead(k);
                  delay = Math.min(delay, nextDelay);
               }

               return delay;
            }
         }
      }

      return Long.MAX_VALUE;
   }

   long peekAhead(int index) {
      long ticks = this.nanos >>> (int)SHIFT[index];
      Node<K, V>[] timerWheel = this.wheel[index];
      long spanMask = SPANS[index] - 1L;
      int mask = timerWheel.length - 1;
      int probe = (int)(ticks + 1L & (long)mask);
      Node<K, V> sentinel = timerWheel[probe];
      Node<K, V> next = sentinel.getNextInVariableOrder();
      return next == sentinel ? Long.MAX_VALUE : SPANS[index] - (this.nanos & spanMask);
   }

   public Iterator<Node<K, V>> iterator() {
      return new AscendingIterator();
   }

   public Iterator<Node<K, V>> descendingIterator() {
      return new DescendingIterator();
   }

   static {
      SPANS = new long[]{Caffeine.ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1L)), Caffeine.ceilingPowerOfTwo(TimeUnit.MINUTES.toNanos(1L)), Caffeine.ceilingPowerOfTwo(TimeUnit.HOURS.toNanos(1L)), Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L)), (long)BUCKETS[3] * Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L)), (long)BUCKETS[3] * Caffeine.ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1L))};
      SHIFT = new long[]{(long)Long.numberOfTrailingZeros(SPANS[0]), (long)Long.numberOfTrailingZeros(SPANS[1]), (long)Long.numberOfTrailingZeros(SPANS[2]), (long)Long.numberOfTrailingZeros(SPANS[3]), (long)Long.numberOfTrailingZeros(SPANS[4])};
   }

   abstract class Traverser implements Iterator<Node<K, V>> {
      final long expectedNanos;
      @Nullable Node<K, V> current;
      @Nullable Node<K, V> next;

      Traverser() {
         this.expectedNanos = TimerWheel.this.nanos;
      }

      public boolean hasNext() {
         if (TimerWheel.this.nanos != this.expectedNanos) {
            throw new ConcurrentModificationException();
         } else if (this.next != null) {
            return true;
         } else if (this.isDone()) {
            return false;
         } else {
            this.next = this.computeNext();
            return this.next != null;
         }
      }

      public Node<K, V> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.current = this.next;
            this.next = null;
            return (Node)Objects.requireNonNull(this.current);
         }
      }

      @Nullable Node<K, V> computeNext() {
         Node<K, V> node = this.current == null ? this.sentinel() : this.current;

         do {
            node = this.traverse(node);
            if (node != this.sentinel()) {
               return node;
            }
         } while((node = this.goToNextBucket()) != null || (node = this.goToNextWheel()) != null);

         return null;
      }

      abstract boolean isDone();

      abstract Node<K, V> sentinel();

      abstract Node<K, V> traverse(Node<K, V> node);

      abstract @Nullable Node<K, V> goToNextBucket();

      abstract @Nullable Node<K, V> goToNextWheel();
   }

   final class AscendingIterator extends TimerWheel<K, V>.Traverser {
      int wheelIndex;
      int steps;

      boolean isDone() {
         return this.wheelIndex == TimerWheel.this.wheel.length;
      }

      Node<K, V> sentinel() {
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      Node<K, V> traverse(Node<K, V> node) {
         return node.getNextInVariableOrder();
      }

      @Nullable Node<K, V> goToNextBucket() {
         return ++this.steps < TimerWheel.this.wheel[this.wheelIndex].length ? TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()] : null;
      }

      @Nullable Node<K, V> goToNextWheel() {
         if (++this.wheelIndex == TimerWheel.this.wheel.length) {
            return null;
         } else {
            this.steps = 0;
            return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
         }
      }

      int bucketIndex() {
         int ticks = (int)(TimerWheel.this.nanos >>> (int)TimerWheel.SHIFT[this.wheelIndex]);
         int bucketMask = TimerWheel.this.wheel[this.wheelIndex].length - 1;
         int bucketOffset = (ticks & bucketMask) + 1;
         return bucketOffset + this.steps & bucketMask;
      }
   }

   final class DescendingIterator extends TimerWheel<K, V>.Traverser {
      int wheelIndex;
      int steps;

      DescendingIterator() {
         this.wheelIndex = TimerWheel.this.wheel.length - 1;
      }

      boolean isDone() {
         return this.wheelIndex == -1;
      }

      Node<K, V> sentinel() {
         return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
      }

      @Nullable Node<K, V> goToNextBucket() {
         return ++this.steps < TimerWheel.this.wheel[this.wheelIndex].length ? TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()] : null;
      }

      @Nullable Node<K, V> goToNextWheel() {
         if (--this.wheelIndex < 0) {
            return null;
         } else {
            this.steps = 0;
            return TimerWheel.this.wheel[this.wheelIndex][this.bucketIndex()];
         }
      }

      Node<K, V> traverse(Node<K, V> node) {
         return node.getPreviousInVariableOrder();
      }

      int bucketIndex() {
         int ticks = (int)(TimerWheel.this.nanos >>> (int)TimerWheel.SHIFT[this.wheelIndex]);
         int bucketMask = TimerWheel.this.wheel[this.wheelIndex].length - 1;
         int bucketOffset = ticks & bucketMask;
         return bucketOffset - this.steps & bucketMask;
      }
   }

   static final class Sentinel<K, V> extends Node<K, V> {
      Node<K, V> prev;
      Node<K, V> next;

      Sentinel() {
         this.prev = this.next = this;
      }

      public Node<K, V> getPreviousInVariableOrder() {
         return this.prev;
      }

      public void setPreviousInVariableOrder(@Nullable Node<K, V> prev) {
         this.prev = prev;
      }

      public Node<K, V> getNextInVariableOrder() {
         return this.next;
      }

      public void setNextInVariableOrder(@Nullable Node<K, V> next) {
         this.next = next;
      }

      public @Nullable K getKey() {
         return null;
      }

      public Object getKeyReference() {
         throw new UnsupportedOperationException();
      }

      public Object getKeyReferenceOrNull() {
         throw new UnsupportedOperationException();
      }

      public @Nullable V getValue() {
         return null;
      }

      public Object getValueReference() {
         throw new UnsupportedOperationException();
      }

      public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
      }

      public boolean containsValue(Object value) {
         return false;
      }

      public boolean isAlive() {
         return false;
      }

      public boolean isRetired() {
         return false;
      }

      public boolean isDead() {
         return false;
      }

      public void retire() {
      }

      public void die() {
      }
   }
}
