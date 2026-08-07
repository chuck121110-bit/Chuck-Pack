package com.github.benmanes.caffeine.cache;

class SS<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SS::new;

   SS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
   }
}
