package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class WSSMWW<K, V> extends WSSMW<K, V> {
   static final LocalCacheFactory FACTORY = WSSMWW::new;
   protected static final VarHandle EXPIRES_AFTER_WRITE_NANOS;
   final Ticker ticker;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque;
   volatile long expiresAfterWriteNanos;
   final Pacer pacer;

   WSSMWW(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.writeOrderDeque = new WriteOrderDeque<Node<K, V>>();
      this.setExpiresAfterWriteNanos(var1.getExpiresAfterWriteNanos());
      this.pacer = var1.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(var1.getScheduler());
   }

   public final Ticker expirationTicker() {
      return this.ticker;
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

   public final Pacer pacer() {
      return this.pacer;
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         EXPIRES_AFTER_WRITE_NANOS = var0.findVarHandle(WSSMWW.class, "expiresAfterWriteNanos", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
