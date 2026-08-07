package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

interface LocalManualCache<K, V> extends Cache<K, V> {
   LocalCache<K, V> cache();

   default long estimatedSize() {
      return this.cache().estimatedSize();
   }

   default void cleanUp() {
      this.cache().cleanUp();
   }

   default @Nullable V getIfPresent(K key) {
      return (V)this.cache().getIfPresent(key, true);
   }

   default @Nullable V get(K key, Function<? super K, ? extends V> mappingFunction) {
      return (V)this.cache().computeIfAbsent(key, mappingFunction);
   }

   default Map<K, V> getAllPresent(Iterable<? extends K> keys) {
      return this.cache().getAllPresent(keys);
   }

   default Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      Map<K, V> found = this.cache().getAllPresent(keys);
      int initialCapacity = Caffeine.calculateHashMapCapacity(keys);
      LinkedHashSet<K> keysToLoad = new LinkedHashSet(initialCapacity);
      LinkedHashMap<K, V> result = new LinkedHashMap(initialCapacity);

      for(K key : keys) {
         V value = (V)found.get(key);
         if (value == null) {
            keysToLoad.add(key);
         }

         result.put(key, value);
      }

      if (keysToLoad.isEmpty()) {
         return found;
      } else {
         this.bulkLoad(keysToLoad, result, mappingFunction);
         Map<K, V> unmodifiable = Collections.unmodifiableMap(result);
         return unmodifiable;
      }
   }

   default void bulkLoad(Set<K> keysToLoad, Map<K, @Nullable V> result, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
      long startTime = this.cache().statsTicker().read();
      boolean success = false;
      boolean var15 = false;

      try {
         var15 = true;
         Map<? extends K, ? extends V> loaded = (Map)mappingFunction.apply(Collections.unmodifiableSet(keysToLoad));
         LocalCache var10001 = this.cache();
         Objects.requireNonNull(var10001);
         loaded.forEach(var10001::put);

         for(K key : keysToLoad) {
            V value = (V)loaded.get(key);
            if (value == null) {
               result.remove(key);
            } else {
               result.put(key, value);
            }
         }

         success = !loaded.isEmpty();
         var15 = false;
      } finally {
         if (var15) {
            long loadTime = this.cache().statsTicker().read() - startTime;
            if (success) {
               this.cache().statsCounter().recordLoadSuccess(loadTime);
            } else {
               this.cache().statsCounter().recordLoadFailure(loadTime);
            }

         }
      }

      long loadTime = this.cache().statsTicker().read() - startTime;
      if (success) {
         this.cache().statsCounter().recordLoadSuccess(loadTime);
      } else {
         this.cache().statsCounter().recordLoadFailure(loadTime);
      }

   }

   default void put(K key, V value) {
      this.cache().put(key, value);
   }

   default void putAll(Map<? extends K, ? extends V> map) {
      this.cache().putAll(map);
   }

   default void invalidate(K key) {
      this.cache().remove(key);
   }

   default void invalidateAll(Iterable<? extends K> keys) {
      this.cache().invalidateAll(keys);
   }

   default void invalidateAll() {
      this.cache().clear();
   }

   default CacheStats stats() {
      return this.cache().statsCounter().snapshot();
   }

   default ConcurrentMap<K, V> asMap() {
      return this.cache();
   }
}
