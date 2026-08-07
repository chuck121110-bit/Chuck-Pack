package com.github.benmanes.caffeine.cache;

enum SingletonWeigher implements Weigher<Object, Object> {
   INSTANCE;

   public int weigh(Object key, Object value) {
      return 1;
   }

   // $FF: synthetic method
   private static SingletonWeigher[] $values() {
      return new SingletonWeigher[]{INSTANCE};
   }
}
