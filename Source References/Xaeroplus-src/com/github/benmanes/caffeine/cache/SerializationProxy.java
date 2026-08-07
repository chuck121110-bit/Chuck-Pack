package com.github.benmanes.caffeine.cache;

import java.io.Serializable;
import java.time.Duration;
import org.jspecify.annotations.Nullable;

final class SerializationProxy<K, V> implements Serializable {
   private static final long serialVersionUID = 1L;
   boolean async;
   boolean weakKeys;
   boolean weakValues;
   boolean softValues;
   boolean isRecordingStats;
   long maximumSize = -1L;
   long maximumWeight = -1L;
   long refreshAfterWriteNanos = -1L;
   long expiresAfterWriteNanos = -1L;
   long expiresAfterAccessNanos = -1L;
   @Nullable Ticker ticker;
   @Nullable Expiry<?, ?> expiry;
   @Nullable Weigher<?, ?> weigher;
   @Nullable AsyncCacheLoader<?, ?> cacheLoader;
   @Nullable RemovalListener<?, ?> removalListener;
   @Nullable RemovalListener<?, ?> evictionListener;

   Caffeine<Object, Object> recreateCaffeine() {
      Caffeine<Object, Object> builder = Caffeine.newBuilder();
      if (this.ticker != null) {
         builder.ticker(this.ticker);
      }

      if (this.isRecordingStats) {
         builder.recordStats();
      }

      if (this.maximumSize != -1L) {
         builder.maximumSize(this.maximumSize);
      }

      if (this.weigher != null) {
         Weigher<Object, Object> castedWeigher = this.weigher;
         builder.maximumWeight(this.maximumWeight);
         builder.weigher(castedWeigher);
      }

      if (this.expiry != null) {
         builder.expireAfter(this.expiry);
      }

      if (this.expiresAfterWriteNanos != -1L) {
         builder.expireAfterWrite(Duration.ofNanos(this.expiresAfterWriteNanos));
      }

      if (this.expiresAfterAccessNanos != -1L) {
         builder.expireAfterAccess(Duration.ofNanos(this.expiresAfterAccessNanos));
      }

      if (this.refreshAfterWriteNanos != -1L) {
         builder.refreshAfterWrite(Duration.ofNanos(this.refreshAfterWriteNanos));
      }

      if (this.weakKeys) {
         builder.weakKeys();
      }

      if (this.weakValues) {
         builder.weakValues();
      }

      if (this.softValues) {
         builder.softValues();
      }

      if (this.removalListener != null) {
         builder.removalListener(this.removalListener);
      }

      if (this.evictionListener != null) {
         builder.evictionListener(this.evictionListener);
      }

      return builder;
   }

   Object readResolve() {
      Caffeine<Object, Object> builder = this.recreateCaffeine();
      if (this.async) {
         if (this.cacheLoader == null) {
            return builder.buildAsync();
         } else {
            AsyncCacheLoader<K, V> loader = this.cacheLoader;
            return builder.buildAsync(loader);
         }
      } else if (this.cacheLoader == null) {
         return builder.build();
      } else {
         CacheLoader<K, V> loader = (CacheLoader)this.cacheLoader;
         return builder.build(loader);
      }
   }
}
