package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@FunctionalInterface
public interface AsyncCacheLoader<K, V extends @Nullable Object> {
   CompletableFuture<? extends V> asyncLoad(K key, Executor executor) throws Exception;

   default CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>> asyncLoadAll(Set<? extends K> keys, Executor executor) throws Exception {
      throw new UnsupportedOperationException();
   }

   default CompletableFuture<? extends V> asyncReload(K key, @NonNull V oldValue, Executor executor) throws Exception {
      return this.asyncLoad(key, executor);
   }

   static <K, V extends @Nullable Object> AsyncCacheLoader<K, V> bulk(Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends @NonNull V>> mappingFunction) {
      return CacheLoader.<K, V>bulk(mappingFunction);
   }

   static <K, V extends @Nullable Object> AsyncCacheLoader<K, V> bulk(final BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends @NonNull V>>> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      return new AsyncCacheLoader<K, V>() {
         public CompletableFuture<V> asyncLoad(K key, Executor executor) {
            return this.asyncLoadAll(Set.of(key), executor).thenApply((results) -> results.get(key));
         }

         public CompletableFuture<Map<K, V>> asyncLoadAll(Set<? extends K> keys, Executor executor) {
            Objects.requireNonNull(keys);
            Objects.requireNonNull(executor);
            CompletableFuture<Map<K, V>> future = (CompletableFuture)mappingFunction.apply(keys, executor);
            return future;
         }
      };
   }
}
