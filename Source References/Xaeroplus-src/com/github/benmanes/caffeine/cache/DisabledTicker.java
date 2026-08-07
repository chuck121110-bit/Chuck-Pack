package com.github.benmanes.caffeine.cache;

enum DisabledTicker implements Ticker {
   INSTANCE;

   public long read() {
      return 0L;
   }

   // $FF: synthetic method
   private static DisabledTicker[] $values() {
      return new DisabledTicker[]{INSTANCE};
   }
}
