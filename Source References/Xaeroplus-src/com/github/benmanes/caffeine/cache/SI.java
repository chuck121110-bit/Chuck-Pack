package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class SI<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SI::new;
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue();

   SI(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
   }

   protected final ReferenceQueue<V> valueReferenceQueue() {
      return this.valueReferenceQueue;
   }

   public final boolean collectValues() {
      return true;
   }
}
