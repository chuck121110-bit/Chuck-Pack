package com.github.benmanes.caffeine.cache;

enum SystemTicker implements Ticker {
   INSTANCE;

   public long read() {
      return System.nanoTime();
   }

   // $FF: synthetic method
   private static SystemTicker[] $values() {
      return new SystemTicker[]{INSTANCE};
   }
}
