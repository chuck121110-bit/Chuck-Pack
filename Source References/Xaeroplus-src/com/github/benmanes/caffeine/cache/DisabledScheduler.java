package com.github.benmanes.caffeine.cache;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

enum DisabledScheduler implements Scheduler {
   INSTANCE;

   public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
      Objects.requireNonNull(executor);
      Objects.requireNonNull(command);
      Objects.requireNonNull(unit);
      return DisabledFuture.instance();
   }

   // $FF: synthetic method
   private static DisabledScheduler[] $values() {
      return new DisabledScheduler[]{INSTANCE};
   }
}
