package com.github.benmanes.caffeine.cache.stats;

import com.github.benmanes.caffeine.cache.RemovalCause;
import java.util.Objects;

enum DisabledStatsCounter implements StatsCounter {
   INSTANCE;

   public void recordHits(int count) {
   }

   public void recordMisses(int count) {
   }

   public void recordLoadSuccess(long loadTime) {
   }

   public void recordLoadFailure(long loadTime) {
   }

   public void recordEviction(int weight, RemovalCause cause) {
      Objects.requireNonNull(cause);
   }

   public CacheStats snapshot() {
      return CacheStats.empty();
   }

   public String toString() {
      return this.snapshot().toString();
   }

   // $FF: synthetic method
   private static DisabledStatsCounter[] $values() {
      return new DisabledStatsCounter[]{INSTANCE};
   }
}
