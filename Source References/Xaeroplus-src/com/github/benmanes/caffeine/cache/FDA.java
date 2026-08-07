package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.ReferenceQueue;

class FDA<K, V> extends FD<K, V> {
   protected static final VarHandle ACCESS_TIME;
   volatile long accessTime;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   FDA() {
   }

   FDA(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
      ACCESS_TIME.set(this, var6);
   }

   FDA(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
      ACCESS_TIME.set(this, var5);
   }

   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInAccessOrder;
   }

   public void setPreviousInVariableOrder(Node<K, V> var1) {
      this.previousInAccessOrder = var1;
   }

   public Node<K, V> getNextInVariableOrder() {
      return this.nextInAccessOrder;
   }

   public void setNextInVariableOrder(Node<K, V> var1) {
      this.nextInAccessOrder = var1;
   }

   public long getVariableTime() {
      return ACCESS_TIME.getOpaque(this);
   }

   public void setVariableTime(long var1) {
      ACCESS_TIME.setOpaque(this, var1);
   }

   public boolean casVariableTime(long var1, long var3) {
      return this.accessTime == var1 && ACCESS_TIME.compareAndSet(this, var1, var3);
   }

   public final long getAccessTime() {
      return ACCESS_TIME.getOpaque(this);
   }

   public final void setAccessTime(long var1) {
      ACCESS_TIME.setOpaque(this, var1);
   }

   public final Node<K, V> getPreviousInAccessOrder() {
      return this.previousInAccessOrder;
   }

   public final void setPreviousInAccessOrder(Node<K, V> var1) {
      this.previousInAccessOrder = var1;
   }

   public final Node<K, V> getNextInAccessOrder() {
      return this.nextInAccessOrder;
   }

   public final void setNextInAccessOrder(Node<K, V> var1) {
      this.nextInAccessOrder = var1;
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FDA<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FDA<K, V>(var1, var2, var3, var4, var5);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         ACCESS_TIME = var0.findVarHandle(FDA.class, "accessTime", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
