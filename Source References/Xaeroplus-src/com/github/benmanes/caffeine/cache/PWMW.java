package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class PWMW<K, V> extends PW<K, V> {
   int queueType;
   int weight;
   int policyWeight;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   PWMW() {
   }

   PWMW(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
      this.weight = var5;
   }

   PWMW(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
      this.weight = var4;
   }

   public int getQueueType() {
      return this.queueType;
   }

   public void setQueueType(int var1) {
      this.queueType = var1;
   }

   public int getWeight() {
      return this.weight;
   }

   public void setWeight(int var1) {
      this.weight = var1;
   }

   public int getPolicyWeight() {
      return this.policyWeight;
   }

   public void setPolicyWeight(int var1) {
      this.policyWeight = var1;
   }

   public Node<K, V> getPreviousInAccessOrder() {
      return this.previousInAccessOrder;
   }

   public void setPreviousInAccessOrder(Node<K, V> var1) {
      this.previousInAccessOrder = var1;
   }

   public Node<K, V> getNextInAccessOrder() {
      return this.nextInAccessOrder;
   }

   public void setNextInAccessOrder(Node<K, V> var1) {
      this.nextInAccessOrder = var1;
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PWMW<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PWMW<K, V>(var1, var2, var3, var4, var5);
   }
}
