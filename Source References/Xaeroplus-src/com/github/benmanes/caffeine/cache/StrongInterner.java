package com.github.benmanes.caffeine.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class StrongInterner<E> implements Interner<E> {
   final ConcurrentMap<E, E> map = new ConcurrentHashMap();

   public E intern(E sample) {
      E canonical = (E)this.map.get(sample);
      if (canonical != null) {
         return canonical;
      } else {
         E value = (E)this.map.putIfAbsent(sample, sample);
         return (E)(value == null ? sample : value);
      }
   }
}
