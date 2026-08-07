package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;

class WSS<K, V> extends WS<K, V> {
   static final LocalCacheFactory FACTORY = WSS::new;
   final StatsCounter statsCounter;

   WSS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.statsCounter = (StatsCounter)var1.getStatsCounterSupplier().get();
   }

   public final boolean isRecordingStats() {
      return true;
   }

   public final Ticker statsTicker() {
      return Ticker.systemTicker();
   }

   public final StatsCounter statsCounter() {
      return this.statsCounter;
   }
}
