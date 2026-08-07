package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class WS<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = WS::new;
   final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue();

   WS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
   }

   protected final ReferenceQueue<K> keyReferenceQueue() {
      return this.keyReferenceQueue;
   }

   public final boolean collectKeys() {
      return true;
   }
}
