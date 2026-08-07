package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.ReferenceQueue;

class FDR<K, V> extends FD<K, V> {
   protected static final VarHandle WRITE_TIME;
   volatile long writeTime;

   FDR() {
   }

   FDR(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
      WRITE_TIME.set(this, var6 & -2L);
   }

   FDR(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
      WRITE_TIME.set(this, var5 & -2L);
   }

   public final long getWriteTime() {
      return WRITE_TIME.getOpaque(this);
   }

   public final void setWriteTime(long var1) {
      WRITE_TIME.set(this, var1);
   }

   public final boolean casWriteTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet(this, var1, var3);
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FDR<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FDR<K, V>(var1, var2, var3, var4, var5);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         WRITE_TIME = var0.findVarHandle(FDR.class, "writeTime", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
