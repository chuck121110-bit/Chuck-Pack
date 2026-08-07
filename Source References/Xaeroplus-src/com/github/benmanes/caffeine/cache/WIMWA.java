package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class WIMWA<K, V> extends WIMW<K, V> {
   static final LocalCacheFactory FACTORY = WIMWA::new;
   protected static final VarHandle EXPIRES_AFTER_ACCESS_NANOS;
   final Ticker ticker;
   final Expiry<K, V> expiry;
   final TimerWheel<K, V> timerWheel;
   volatile long expiresAfterAccessNanos;
   final Pacer pacer;

   WIMWA(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.ticker = var1.getTicker();
      this.expiry = var1.getExpiry(this.isAsync);
      this.timerWheel = var1.expiresVariable() ? new TimerWheel() : null;
      this.setExpiresAfterAccessNanos(var1.getExpiresAfterAccessNanos());
      this.pacer = var1.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(var1.getScheduler());
   }

   public final Ticker expirationTicker() {
      return this.ticker;
   }

   protected final boolean expiresVariable() {
      return this.timerWheel != null;
   }

   public final Expiry<K, V> expiry() {
      return this.expiry;
   }

   protected final TimerWheel<K, V> timerWheel() {
      return this.timerWheel;
   }

   protected final long expiresAfterAccessNanos() {
      return EXPIRES_AFTER_ACCESS_NANOS.getAcquire(this);
   }

   protected final void setExpiresAfterAccessNanos(long var1) {
      EXPIRES_AFTER_ACCESS_NANOS.setRelease(this, var1);
   }

   protected final boolean expiresAfterAccess() {
      return this.timerWheel == null;
   }

   public final Pacer pacer() {
      return this.pacer;
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         EXPIRES_AFTER_ACCESS_NANOS = var0.findVarHandle(WIMWA.class, "expiresAfterAccessNanos", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
