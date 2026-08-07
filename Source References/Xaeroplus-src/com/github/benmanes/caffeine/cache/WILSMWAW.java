package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class WILSMWAW<K, V> extends WILSMWA<K, V> {
   static final LocalCacheFactory FACTORY = WILSMWAW::new;
   protected static final VarHandle EXPIRES_AFTER_WRITE_NANOS;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque = new WriteOrderDeque<Node<K, V>>();
   volatile long expiresAfterWriteNanos;

   WILSMWAW(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.setExpiresAfterWriteNanos(var1.getExpiresAfterWriteNanos());
   }

   protected final WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      return this.writeOrderDeque;
   }

   protected final boolean expiresAfterWrite() {
      return true;
   }

   protected final long expiresAfterWriteNanos() {
      return EXPIRES_AFTER_WRITE_NANOS.getAcquire(this);
   }

   protected final void setExpiresAfterWriteNanos(long var1) {
      EXPIRES_AFTER_WRITE_NANOS.setRelease(this, var1);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         EXPIRES_AFTER_WRITE_NANOS = var0.findVarHandle(WILSMWAW.class, "expiresAfterWriteNanos", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
