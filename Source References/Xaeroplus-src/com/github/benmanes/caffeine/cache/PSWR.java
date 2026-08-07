package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class PSWR<K, V> extends PSW<K, V> {
   PSWR() {
   }

   PSWR(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super(var1, var2, var3, var4, var5, var6);
   }

   PSWR(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, var2, var3, var4, var5);
   }

   public final boolean casWriteTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet(this, var1, var3);
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PSWR<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PSWR<K, V>(var1, var2, var3, var4, var5);
   }
}
