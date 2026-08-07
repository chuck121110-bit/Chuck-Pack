package com.github.benmanes.caffeine.cache;

import java.util.AbstractMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

final class WriteThroughEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
   private static final long serialVersionUID = 1L;
   private final ConcurrentMap<K, V> map;

   WriteThroughEntry(ConcurrentMap<K, V> map, K key, V value) {
      super(key, value);
      this.map = (ConcurrentMap)Objects.requireNonNull(map);
   }

   public V setValue(V value) {
      this.map.put(this.getKey(), value);
      return (V)super.setValue(value);
   }

   Object writeReplace() {
      return new AbstractMap.SimpleEntry(this);
   }
}
