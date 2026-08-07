package com.github.benmanes.caffeine.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface AsyncLoadingCache<K, V extends @Nullable Object> extends AsyncCache<K, V> {
   CompletableFuture<V> get(K key);

   CompletableFuture<Map<K, @NonNull V>> getAll(Iterable<? extends K> keys);

   LoadingCache<K, V> synchronous();
}
