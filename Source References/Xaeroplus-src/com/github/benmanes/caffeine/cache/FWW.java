package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.ReferenceQueue;

class FWW<K, V> extends FW<K, V> {
   protected static final VarHandle WRITE_TIME;
   volatile long writeTime;
   Node<K, V> previousInWriteOrder;
   Node<K, V> nextInWriteOrder;

   FWW() {
   }

   FWW(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
      WRITE_TIME.set(this, var6 & -2L);
   }

   FWW(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
      WRITE_TIME.set(this, var5 & -2L);
   }

   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInWriteOrder;
   }

   public void setPreviousInVariableOrder(Node<K, V> var1) {
      this.previousInWriteOrder = var1;
   }

   public Node<K, V> getNextInVariableOrder() {
      return this.nextInWriteOrder;
   }

   public void setNextInVariableOrder(Node<K, V> var1) {
      this.nextInWriteOrder = var1;
   }

   public long getVariableTime() {
      return WRITE_TIME.getOpaque(this);
   }

   public void setVariableTime(long var1) {
      WRITE_TIME.setOpaque(this, var1);
   }

   public boolean casVariableTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet(this, var1, var3);
   }

   public final long getWriteTime() {
      return WRITE_TIME.getOpaque(this);
   }

   public final void setWriteTime(long var1) {
      WRITE_TIME.set(this, var1);
   }

   public final Node<K, V> getPreviousInWriteOrder() {
      return this.previousInWriteOrder;
   }

   public final void setPreviousInWriteOrder(Node<K, V> var1) {
      this.previousInWriteOrder = var1;
   }

   public final Node<K, V> getNextInWriteOrder() {
      return this.nextInWriteOrder;
   }

   public final void setNextInWriteOrder(Node<K, V> var1) {
      this.nextInWriteOrder = var1;
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FWW<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FWW<K, V>(var1, var2, var3, var4, var5);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         WRITE_TIME = var0.findVarHandle(FWW.class, "writeTime", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
