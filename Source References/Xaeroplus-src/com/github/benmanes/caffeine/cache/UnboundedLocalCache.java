package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

final class UnboundedLocalCache<K, V> implements LocalCache<K, V> {
   static final System.Logger logger = System.getLogger(UnboundedLocalCache.class.getName());
   static final VarHandle REFRESHES = findVarHandle(UnboundedLocalCache.class, "refreshes", ConcurrentMap.class);
   final @Nullable RemovalListener<K, V> removalListener;
   final ConcurrentHashMap<K, V> data;
   final StatsCounter statsCounter;
   final boolean isRecordingStats;
   final Executor executor;
   final boolean isAsync;
   @Nullable Set<K> keySet;
   @Nullable Collection<V> values;
   @Nullable Set<Map.Entry<K, V>> entrySet;
   volatile @Nullable ConcurrentMap<Object, CompletableFuture<?>> refreshes;

   UnboundedLocalCache(Caffeine<? super K, ? super V> builder, boolean isAsync) {
      this.data = new ConcurrentHashMap(builder.getInitialCapacity());
      this.statsCounter = (StatsCounter)builder.getStatsCounterSupplier().get();
      this.removalListener = builder.<K, V>getRemovalListener(isAsync);
      this.isRecordingStats = builder.isRecordingStats();
      this.executor = builder.getExecutor();
      this.isAsync = isAsync;
   }

   static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
      try {
         return MethodHandles.lookup().findVarHandle(recv, name, type);
      } catch (ReflectiveOperationException e) {
         throw new ExceptionInInitializerError(e);
      }
   }

   public boolean isAsync() {
      return this.isAsync;
   }

   public @Nullable Expiry<K, V> expiry() {
      return null;
   }

   public boolean collectKeys() {
      return false;
   }

   @CanIgnoreReturnValue
   public Object referenceKey(K key) {
      return key;
   }

   public boolean isPendingEviction(K key) {
      return false;
   }

   public @Nullable V getIfPresent(Object key, boolean recordStats) {
      V value = (V)this.data.get(key);
      if (recordStats) {
         if (value == null) {
            this.statsCounter.recordMisses(1);
         } else {
            this.statsCounter.recordHits(1);
         }
      }

      return value;
   }

   public @Nullable V getIfPresentQuietly(Object key) {
      return (V)this.data.get(key);
   }

   public long estimatedSize() {
      return this.data.mappingCount();
   }

   public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
      LinkedHashMap<K, V> result = new LinkedHashMap(Caffeine.calculateHashMapCapacity(keys));

      for(K key : keys) {
         result.put(key, (Object)null);
      }

      int uniqueKeys = result.size();
      Iterator<Map.Entry<K, V>> iter = result.entrySet().iterator();

      while(iter.hasNext()) {
         Map.Entry<K, V> entry = (Map.Entry)iter.next();
         V value = (V)this.data.get(entry.getKey());
         if (value == null) {
            iter.remove();
         } else {
            entry.setValue(value);
         }
      }

      this.statsCounter.recordHits(result.size());
      this.statsCounter.recordMisses(uniqueKeys - result.size());
      Map<K, V> unmodifiable = Collections.unmodifiableMap(result);
      return unmodifiable;
   }

   public void cleanUp() {
   }

   public StatsCounter statsCounter() {
      return this.statsCounter;
   }

   public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
      if (this.removalListener != null) {
         Runnable task = () -> {
            try {
               this.removalListener.onRemoval(key, value, cause);
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

   public boolean isRecordingStats() {
      return this.isRecordingStats;
   }

   public Executor executor() {
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
      if (pending != null) {
         pending.remove(keyReference);
      }

   }

   public Ticker statsTicker() {
      return this.isRecordingStats ? Ticker.systemTicker() : Ticker.disabledTicker();
   }

   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.data.forEach(action);
   }

   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      Objects.requireNonNull(function);
      BiFunction<K, V, V> remappingFunction = (keyx, oldValue) -> oldValue == null ? null : Objects.requireNonNull(function.apply(keyx, oldValue));

      for(K key : this.data.keySet()) {
         this.remap(key, remappingFunction);
      }

   }

   public @Nullable V computeIfAbsent(K key, Function<? super K, ? extends @Nullable V> mappingFunction, boolean recordStats, boolean recordLoad) {
      Objects.requireNonNull(mappingFunction);
      V value = (V)this.data.get(key);
      if (value != null) {
         if (recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      } else {
         boolean[] missed = new boolean[1];
         value = (V)this.data.computeIfAbsent(key, (k) -> {
            missed[0] = true;
            V computed = (V)(recordStats ? this.statsAware(mappingFunction, recordLoad).apply(k) : mappingFunction.apply(k));
            this.discardRefresh(k);
            return computed;
         });
         if (!missed[0] && recordStats) {
            this.statsCounter.recordHits(1);
         }

         return value;
      }
   }

   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      if (!this.data.containsKey(key)) {
         return null;
      } else {
         V[] oldValue = (V[])(new Object[1]);
         boolean[] replaced = new boolean[1];
         V nv = (V)this.data.computeIfPresent(key, (k, value) -> {
            BiFunction<? super K, ? super V, ? extends V> function = this.statsAware(remappingFunction, true, true);
            V newValue = (V)function.apply(k, value);
            replaced[0] = newValue != null;
            if (newValue != value) {
               oldValue[0] = value;
            }

            this.discardRefresh(k);
            return newValue;
         });
         if (replaced[0]) {
            this.notifyOnReplace(key, oldValue[0], nv);
         } else if (oldValue[0] != null) {
            this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
         }

         return nv;
      }
   }

   public @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, @Nullable Expiry<? super K, ? super V> expiry, boolean recordLoad, boolean recordLoadFailure, boolean @Nullable [] preserveTimestamps) {
      Objects.requireNonNull(remappingFunction);
      return (V)this.remap(key, this.statsAware(remappingFunction, recordLoad, recordLoadFailure));
   }

   public @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(remappingFunction);
      Objects.requireNonNull(value);
      return (V)this.remap(key, (Object k, Object oldValue) -> oldValue == null ? value : this.statsAware(remappingFunction).apply(oldValue, value));
   }

   V remap(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      V[] oldValue = (V[])(new Object[1]);
      boolean[] replaced = new boolean[1];
      V nv = (V)this.data.compute(key, (k, value) -> {
         V newValue = (V)remappingFunction.apply(k, value);
         if (value == null && newValue == null) {
            return null;
         } else {
            replaced[0] = newValue != null;
            if (newValue != value) {
               oldValue[0] = value;
            }

            this.discardRefresh(k);
            return newValue;
         }
      });
      if (replaced[0]) {
         this.notifyOnReplace(key, oldValue[0], nv);
      } else if (oldValue[0] != null) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
      }

      return nv;
   }

   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   public int size() {
      return this.data.size();
   }

   public void clear() {
      for(K key : this.removalListener == null ? this.data.keySet() : List.copyOf(this.data.keySet())) {
         this.remove(key);
      }

   }

   public boolean containsKey(Object key) {
      return this.data.containsKey(key);
   }

   public boolean containsValue(Object value) {
      return this.data.containsValue(value);
   }

   public @Nullable V get(Object key) {
      return (V)this.getIfPresent(key, false);
   }

   public V put(K key, V value) {
      Objects.requireNonNull(value);
      V[] oldValue = (V[])(new Object[1]);
      this.data.compute(key, (k, v) -> {
         this.discardRefresh(k);
         oldValue[0] = v;
         return value;
      });
      if (oldValue[0] != null) {
         this.notifyOnReplace(key, oldValue[0], value);
      }

      return (V)oldValue[0];
   }

   public @Nullable V putIfAbsent(K key, V value) {
      Objects.requireNonNull(value);
      V v = (V)this.data.get(key);
      if (v != null) {
         return v;
      } else {
         boolean[] added = new boolean[1];
         V val = (V)this.data.computeIfAbsent(key, (k) -> {
            this.discardRefresh(k);
            added[0] = true;
            return value;
         });
         return (V)(added[0] ? null : val);
      }
   }

   public void putAll(Map<? extends K, ? extends V> map) {
      map.forEach(this::put);
   }

   public V remove(Object key) {
      V[] oldValue = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         this.discardRefresh(k);
         oldValue[0] = v;
         return null;
      });
      if (oldValue[0] != null) {
         this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
      }

      return (V)oldValue[0];
   }

   public boolean remove(Object key, Object value) {
      if (value == null) {
         Objects.requireNonNull(key);
         return false;
      } else {
         V[] oldValue = (V[])(new Object[1]);
         this.data.computeIfPresent(key, (k, v) -> {
            if (Objects.equals(v, value)) {
               this.discardRefresh(k);
               oldValue[0] = v;
               return null;
            } else {
               return v;
            }
         });
         if (oldValue[0] != null) {
            this.notifyRemoval(key, oldValue[0], RemovalCause.EXPLICIT);
            return true;
         } else {
            return false;
         }
      }
   }

   public V replace(K key, V value) {
      Objects.requireNonNull(value);
      V[] oldValue = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         this.discardRefresh(k);
         oldValue[0] = v;
         return value;
      });
      if (oldValue[0] != null && oldValue[0] != value) {
         this.notifyOnReplace(key, oldValue[0], value);
      }

      return (V)oldValue[0];
   }

   public boolean replace(K key, V oldValue, V newValue) {
      return this.replace(key, oldValue, newValue, true);
   }

   public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
      Objects.requireNonNull(oldValue);
      Objects.requireNonNull(newValue);
      V[] prev = (V[])(new Object[1]);
      this.data.computeIfPresent(key, (k, v) -> {
         if (Objects.equals(v, oldValue)) {
            if (shouldDiscardRefresh) {
               this.discardRefresh(k);
            }

            prev[0] = v;
            return newValue;
         } else {
            return v;
         }
      });
      boolean replaced = prev[0] != null;
      if (replaced && prev[0] != newValue) {
         this.notifyOnReplace(key, prev[0], newValue);
      }

      return replaced;
   }

   public boolean equals(@Nullable Object o) {
      return o == this || this.data.equals(o);
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public String toString() {
      StringBuilder result = (new StringBuilder(50)).append('{');
      this.data.forEach((key, value) -> {
         if (result.length() != 1) {
            result.append(", ");
         }

         result.append(key == this ? "(this Map)" : key).append('=').append(value == this ? "(this Map)" : value);
      });
      return result.append('}').toString();
   }

   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks == null ? (this.keySet = new KeySetView<K>(this)) : ks;
   }

   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs == null ? (this.values = new ValuesView(this)) : vs;
   }

   public Set<Map.Entry<K, V>> entrySet() {
      Set<Map.Entry<K, V>> es = this.entrySet;
      return es == null ? (this.entrySet = new EntrySetView(this)) : es;
   }

   static final class KeySetView<K> extends AbstractSet<K> {
      final UnboundedLocalCache<K, ?> cache;

      KeySetView(UnboundedLocalCache<K, ?> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
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
         if (collection instanceof Set && collection.size() > this.size()) {
            for(K key : this) {
               if (collection.contains(key)) {
                  modified |= this.remove(key);
               }
            }
         } else {
            for(Object o : collection) {
               modified |= o != null && this.remove(o);
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

      public void forEach(Consumer<? super K> action) {
         this.cache.data.keySet().forEach(action);
      }

      public Iterator<K> iterator() {
         return new KeyIterator<K>(this.cache);
      }

      public Spliterator<K> spliterator() {
         return new KeySpliterator(this.cache);
      }

      public Object[] toArray() {
         return this.cache.data.keySet().toArray();
      }

      public <T> T[] toArray(T[] array) {
         return (T[])this.cache.data.keySet().toArray(array);
      }
   }

   static final class KeyIterator<K> implements Iterator<K> {
      final UnboundedLocalCache<K, ?> cache;
      final Iterator<K> iterator;
      @Nullable K current;

      KeyIterator(UnboundedLocalCache<K, ?> cache) {
         this.iterator = cache.data.keySet().iterator();
         this.cache = cache;
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public K next() {
         this.current = (K)this.iterator.next();
         return this.current;
      }

      public void remove() {
         if (this.current == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.current);
            this.current = null;
         }
      }
   }

   static final class KeySpliterator<K, V> implements Spliterator<K> {
      final Spliterator<K> spliterator;

      KeySpliterator(UnboundedLocalCache<K, V> cache) {
         this(cache.data.keySet().spliterator());
      }

      KeySpliterator(Spliterator<K> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
      }

      public void forEachRemaining(Consumer<? super K> action) {
         Objects.requireNonNull(action);
         this.spliterator.forEachRemaining(action);
      }

      public boolean tryAdvance(Consumer<? super K> action) {
         Objects.requireNonNull(action);
         return this.spliterator.tryAdvance(action);
      }

      public @Nullable KeySpliterator<K, V> trySplit() {
         Spliterator<K> split = this.spliterator.trySplit();
         return split == null ? null : new KeySpliterator(split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4353;
      }
   }

   static final class ValuesView<K, V> extends AbstractCollection<V> {
      final UnboundedLocalCache<K, V> cache;

      ValuesView(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
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

         for(Map.Entry<K, V> entry : this.cache.data.entrySet()) {
            if (collection.contains(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
               modified = true;
            }
         }

         return modified;
      }

      public boolean remove(@Nullable Object o) {
         if (o == null) {
            return false;
         } else {
            for(Map.Entry<K, V> entry : this.cache.data.entrySet()) {
               if (o.equals(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean removeIf(Predicate<? super V> filter) {
         Objects.requireNonNull(filter);
         boolean removed = false;

         for(Map.Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry.getValue())) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
      }

      public boolean retainAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;

         for(Map.Entry<K, V> entry : this.cache.data.entrySet()) {
            if (!collection.contains(entry.getValue()) && this.cache.remove(entry.getKey(), entry.getValue())) {
               modified = true;
            }
         }

         return modified;
      }

      public void forEach(Consumer<? super V> action) {
         this.cache.data.values().forEach(action);
      }

      public Iterator<V> iterator() {
         return new ValueIterator(this.cache);
      }

      public Spliterator<V> spliterator() {
         return new ValueSpliterator(this.cache);
      }

      public Object[] toArray() {
         return this.cache.data.values().toArray();
      }

      public <T> T[] toArray(T[] array) {
         return (T[])this.cache.data.values().toArray(array);
      }
   }

   static final class ValueIterator<K, V> implements Iterator<V> {
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Map.Entry<K, V>> iterator;
      Map.@Nullable Entry<K, V> entry;

      ValueIterator(UnboundedLocalCache<K, V> cache) {
         this.iterator = cache.data.entrySet().iterator();
         this.cache = cache;
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public V next() {
         this.entry = (Map.Entry)this.iterator.next();
         return (V)this.entry.getValue();
      }

      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.entry.getKey());
            this.entry = null;
         }
      }
   }

   static final class ValueSpliterator<K, V> implements Spliterator<V> {
      final Spliterator<V> spliterator;

      ValueSpliterator(UnboundedLocalCache<K, V> cache) {
         this(cache.data.values().spliterator());
      }

      ValueSpliterator(Spliterator<V> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
      }

      public void forEachRemaining(Consumer<? super V> action) {
         Objects.requireNonNull(action);
         this.spliterator.forEachRemaining(action);
      }

      public boolean tryAdvance(Consumer<? super V> action) {
         Objects.requireNonNull(action);
         return this.spliterator.tryAdvance(action);
      }

      public @Nullable ValueSpliterator<K, V> trySplit() {
         Spliterator<V> split = this.spliterator.trySplit();
         return split == null ? null : new ValueSpliterator(split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4352;
      }
   }

   static final class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {
      final UnboundedLocalCache<K, V> cache;

      EntrySetView(UnboundedLocalCache<K, V> cache) {
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public boolean isEmpty() {
         return this.cache.isEmpty();
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
               V cachedValue = this.cache.get(key);
               return cachedValue != null && cachedValue.equals(value);
            } else {
               return false;
            }
         }
      }

      public boolean removeAll(Collection<?> collection) {
         Objects.requireNonNull(collection);
         boolean modified = false;
         if (collection instanceof Set && collection.size() > this.size()) {
            for(Map.Entry<K, V> entry : this) {
               if (collection.contains(entry)) {
                  modified |= this.remove(entry);
               }
            }
         } else {
            for(Object o : collection) {
               modified |= this.remove(o);
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
         boolean removed = false;

         for(Map.Entry<K, V> entry : this.cache.data.entrySet()) {
            if (filter.test(entry)) {
               removed |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return removed;
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
      final UnboundedLocalCache<K, V> cache;
      final Iterator<Map.Entry<K, V>> iterator;
      Map.@Nullable Entry<K, V> entry;

      EntryIterator(UnboundedLocalCache<K, V> cache) {
         this.iterator = cache.data.entrySet().iterator();
         this.cache = cache;
      }

      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public Map.Entry<K, V> next() {
         this.entry = (Map.Entry)this.iterator.next();
         return new WriteThroughEntry<K, V>(this.cache, this.entry.getKey(), this.entry.getValue());
      }

      public void remove() {
         if (this.entry == null) {
            throw new IllegalStateException();
         } else {
            this.cache.remove(this.entry.getKey());
            this.entry = null;
         }
      }
   }

   static final class EntrySpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
      final Spliterator<Map.Entry<K, V>> spliterator;
      final UnboundedLocalCache<K, V> cache;

      EntrySpliterator(UnboundedLocalCache<K, V> cache) {
         this(cache, cache.data.entrySet().spliterator());
      }

      EntrySpliterator(UnboundedLocalCache<K, V> cache, Spliterator<Map.Entry<K, V>> spliterator) {
         this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         this.cache = (UnboundedLocalCache)Objects.requireNonNull(cache);
      }

      public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
         Objects.requireNonNull(action);
         this.spliterator.forEachRemaining((entry) -> {
            WriteThroughEntry<K, V> e = new WriteThroughEntry<K, V>(this.cache, entry.getKey(), entry.getValue());
            action.accept(e);
         });
      }

      public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
         Objects.requireNonNull(action);
         return this.spliterator.tryAdvance((entry) -> {
            WriteThroughEntry<K, V> e = new WriteThroughEntry<K, V>(this.cache, entry.getKey(), entry.getValue());
            action.accept(e);
         });
      }

      public @Nullable EntrySpliterator<K, V> trySplit() {
         Spliterator<Map.Entry<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new EntrySpliterator(this.cache, split);
      }

      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      public int characteristics() {
         return 4353;
      }
   }

   static class UnboundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, V> cache;
      @Nullable Policy<K, V> policy;

      UnboundedLocalManualCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<K, V>(builder, false);
      }

      public final UnboundedLocalCache<K, V> cache() {
         return this.cache;
      }

      public final Policy<K, V> policy() {
         if (this.policy == null) {
            Function<V, V> identity = (v) -> v;
            this.policy = new UnboundedPolicy<K, V>(this.cache, identity);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<K, V>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         return proxy;
      }
   }

   static final class UnboundedPolicy<K, V> implements Policy<K, V> {
      final Function<@Nullable V, @Nullable V> transformer;
      final UnboundedLocalCache<K, V> cache;

      UnboundedPolicy(UnboundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer) {
         this.transformer = transformer;
         this.cache = cache;
      }

      public boolean isRecordingStats() {
         return this.cache.isRecordingStats;
      }

      public @Nullable V getIfPresentQuietly(K key) {
         return (V)this.transformer.apply(this.cache.data.get(key));
      }

      public Policy.@Nullable CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
         V value = (V)this.transformer.apply(this.cache.data.get(key));
         return value == null ? null : SnapshotEntry.forEntry(key, value);
      }

      public Map<K, CompletableFuture<V>> refreshes() {
         ConcurrentMap<Object, CompletableFuture<?>> refreshes = this.cache.refreshes;
         if (refreshes != null && !refreshes.isEmpty()) {
            Map<K, CompletableFuture<V>> castedRefreshes = (Map)refreshes;
            return Collections.unmodifiableMap(new HashMap(castedRefreshes));
         } else {
            Map<K, CompletableFuture<V>> emptyMap = Collections.unmodifiableMap(Collections.emptyMap());
            return emptyMap;
         }
      }

      public Optional<Policy.Eviction<K, V>> eviction() {
         return Optional.empty();
      }

      public Optional<Policy.FixedExpiration<K, V>> expireAfterAccess() {
         return Optional.empty();
      }

      public Optional<Policy.FixedExpiration<K, V>> expireAfterWrite() {
         return Optional.empty();
      }

      public Optional<Policy.VarExpiration<K, V>> expireVariably() {
         return Optional.empty();
      }

      public Optional<Policy.FixedRefresh<K, V>> refreshAfterWrite() {
         return Optional.empty();
      }
   }

   static final class UnboundedLocalLoadingCache<K, V> extends UnboundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final Function<K, @Nullable V> mappingFunction;
      final CacheLoader<? super K, V> cacheLoader;
      final @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

      UnboundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader) {
         super(builder);
         this.cacheLoader = cacheLoader;
         this.mappingFunction = LocalLoadingCache.<K, V>newMappingFunction(cacheLoader);
         this.bulkMappingFunction = LocalLoadingCache.newBulkMappingFunction(cacheLoader);
      }

      public AsyncCacheLoader<? super K, V> cacheLoader() {
         return this.cacheLoader;
      }

      public Function<K, @Nullable V> mappingFunction() {
         return this.mappingFunction;
      }

      public @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
         return this.bulkMappingFunction;
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = (SerializationProxy)super.writeReplace();
         proxy.cacheLoader = this.cacheLoader;
         return proxy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }
   }

   static final class UnboundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      LocalAsyncCache.@Nullable CacheView<K, V> cacheView;
      @Nullable Policy<K, V> policy;

      UnboundedLocalAsyncCache(Caffeine<K, V> builder) {
         this.cache = new UnboundedLocalCache<K, CompletableFuture<V>>(builder, true);
      }

      public UnboundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<K, CompletableFuture<V>>(this)) : this.mapView;
      }

      public Cache<K, V> synchronous() {
         return this.cacheView == null ? (this.cacheView = new LocalAsyncCache.CacheView<K, V>(this)) : this.cacheView;
      }

      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         return this.policy == null ? (this.policy = new UnboundedPolicy<K, V>(castCache, transformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<K, V>();
         proxy.isRecordingStats = this.cache.isRecordingStats;
         proxy.removalListener = this.cache.removalListener;
         proxy.async = true;
         return proxy;
      }
   }

   static final class UnboundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final UnboundedLocalCache<K, CompletableFuture<V>> cache;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable Policy<K, V> policy;

      UnboundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
         super(loader);
         this.cache = new UnboundedLocalCache<K, CompletableFuture<V>>(builder, true);
      }

      public LocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<K, CompletableFuture<V>>(this)) : this.mapView;
      }

      public Policy<K, V> policy() {
         UnboundedLocalCache<K, V> castCache = this.cache;
         Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
         return this.policy == null ? (this.policy = new UnboundedPolicy<K, V>(castCache, transformer)) : this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         SerializationProxy<K, V> proxy = new SerializationProxy<K, V>();
         proxy.isRecordingStats = this.cache.isRecordingStats();
         proxy.removalListener = this.cache.removalListener;
         proxy.cacheLoader = this.cacheLoader;
         proxy.async = true;
         return proxy;
      }
   }
}
