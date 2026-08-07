package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class PSAWR<K, V> extends PSAW<K, V> {
   PSAWR() {
   }

   PSAWR(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
   }

   PSAWR(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
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
      return ACCESS_TIME.getOpaque(this);
   }

   public void setVariableTime(long var1) {
      ACCESS_TIME.setOpaque(this, var1);
   }

   public boolean casVariableTime(long var1, long var3) {
      return this.accessTime == var1 && ACCESS_TIME.compareAndSet(this, var1, var3);
   }

   public final boolean casWriteTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet(this, var1, var3);
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PSAWR<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PSAWR<K, V>(var1, var2, var3, var4, var5);
   }
}
