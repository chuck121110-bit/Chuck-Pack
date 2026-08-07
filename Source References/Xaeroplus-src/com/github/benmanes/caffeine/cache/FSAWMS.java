package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class FSAWMS<K, V> extends FSAW<K, V> {
   int queueType;

   FSAWMS() {
   }

   FSAWMS(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
   }

   FSAWMS(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
   }

   public int getQueueType() {
      return this.queueType;
   }

   public void setQueueType(int var1) {
      this.queueType = var1;
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FSAWMS<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FSAWMS<K, V>(var1, var2, var3, var4, var5);
   }
}
