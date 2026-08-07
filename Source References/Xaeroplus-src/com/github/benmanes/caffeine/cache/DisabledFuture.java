package com.github.benmanes.caffeine.cache;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

enum DisabledFuture implements Future<@Nullable Void> {
   INSTANCE;

   static Future<? extends @Nullable Object> instance() {
      return INSTANCE;
   }

   public boolean isDone() {
      return true;
   }

   public boolean isCancelled() {
      return false;
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   public @Nullable Void get(long timeout, TimeUnit unit) {
      Objects.requireNonNull(unit);
      return null;
   }

   public @Nullable Void get() {
      return null;
   }

   // $FF: synthetic method
   private static DisabledFuture[] $values() {
      return new DisabledFuture[]{INSTANCE};
   }
}
