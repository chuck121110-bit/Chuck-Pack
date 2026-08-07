package xaeroplus.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public class Wait {
   public static void wait(int seconds) {
      try {
         Thread.sleep(TimeUnit.SECONDS.toMillis((long)seconds));
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   public static void waitMs(int milliseconds) {
      try {
         Thread.sleep((long)milliseconds);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   public static void waitSpinLoop() {
      while(true) {
         try {
            Thread.sleep(2147483647L);
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         }
      }
   }

   public static boolean waitUntil(final Supplier<Boolean> conditionSupplier, int secondsToWait) {
      return waitUntil(conditionSupplier, 50, (long)secondsToWait, TimeUnit.SECONDS);
   }

   public static boolean waitUntil(final Supplier<Boolean> conditionSupplier, int checkIntervalMs, long timeout, TimeUnit unit) {
      long beforeTime = System.nanoTime();

      while(!(Boolean)conditionSupplier.get() && TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeTime) < unit.toMillis(timeout)) {
         if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Wait Interrupted");
         }

         LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos((long)checkIntervalMs));
      }

      return (Boolean)conditionSupplier.get();
   }

   public static void waitRandomMs(final int ms) {
      waitMs((int)ThreadLocalRandom.current().nextDouble((double)ms));
   }
}
