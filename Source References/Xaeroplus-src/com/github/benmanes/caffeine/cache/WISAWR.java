package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class WISAWR<K, V> extends WISAW<K, V> {
   static final LocalCacheFactory FACTORY = WISAWR::new;
   private static final VarHandle REFRESH_AFTER_WRITE_NANOS;
   volatile long refreshAfterWriteNanos;

   WISAWR(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.setRefreshAfterWriteNanos(var1.getRefreshAfterWriteNanos());
   }

   protected boolean refreshAfterWrite() {
      return true;
   }

   protected long refreshAfterWriteNanos() {
      return REFRESH_AFTER_WRITE_NANOS.getAcquire(this);
   }

   protected void setRefreshAfterWriteNanos(long var1) {
      REFRESH_AFTER_WRITE_NANOS.setRelease(this, var1);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         REFRESH_AFTER_WRITE_NANOS = var0.findVarHandle(WISAWR.class, "refreshAfterWriteNanos", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
