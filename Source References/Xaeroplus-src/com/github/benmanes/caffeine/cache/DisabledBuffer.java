package com.github.benmanes.caffeine.cache;

import java.util.function.Consumer;

enum DisabledBuffer implements Buffer<Object> {
   INSTANCE;

   public int offer(Object e) {
      return 0;
   }

   public void drainTo(Consumer<Object> consumer) {
   }

   public long size() {
      return 0L;
   }

   public long reads() {
      return 0L;
   }

   public long writes() {
      return 0L;
   }

   // $FF: synthetic method
   private static DisabledBuffer[] $values() {
      return new DisabledBuffer[]{INSTANCE};
   }
}
