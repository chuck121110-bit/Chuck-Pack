package com.github.benmanes.caffeine.cache;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;

final class ExpiryAfterAccess<K, V> implements Expiry<K, V>, Serializable {
   private static final long serialVersionUID = 1L;
   final BiFunction<K, V, Duration> function;

   public ExpiryAfterAccess(BiFunction<K, V, Duration> calculator) {
      this.function = (BiFunction)Objects.requireNonNull(calculator);
   }

   public long expireAfterCreate(K key, V value, long currentTime) {
      return Caffeine.toNanosSaturated((Duration)this.function.apply(key, value));
   }

   public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
      return Caffeine.toNanosSaturated((Duration)this.function.apply(key, value));
   }

   public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
      return Caffeine.toNanosSaturated((Duration)this.function.apply(key, value));
   }
}
