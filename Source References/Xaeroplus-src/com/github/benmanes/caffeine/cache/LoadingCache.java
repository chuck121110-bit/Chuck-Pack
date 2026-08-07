package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface LoadingCache<K, V extends @Nullable Object> extends Cache<K, V> {
   V get(K key);

   Map<K, @NonNull V> getAll(Iterable<? extends K> keys);

   @CanIgnoreReturnValue
   CompletableFuture<V> refresh(K key);

   @CanIgnoreReturnValue
   CompletableFuture<Map<K, @NonNull V>> refreshAll(Iterable<? extends K> keys);
}
