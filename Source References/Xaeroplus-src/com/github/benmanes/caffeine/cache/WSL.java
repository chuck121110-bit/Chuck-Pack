package com.github.benmanes.caffeine.cache;

class WSL<K, V> extends WS<K, V> {
   static final LocalCacheFactory FACTORY = WSL::new;
   final RemovalListener<K, V> removalListener;

   WSL(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.removalListener = var1.<K, V>getRemovalListener(var3);
   }

   public final RemovalListener<K, V> removalListener() {
      return this.removalListener;
   }
}
