package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Immutable;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@Immutable(
   containerOf = {"K", "V"}
)
class SnapshotEntry<K, V> implements Policy.CacheEntry<K, V> {
   private final long snapshot;
   private final V value;
   private final K key;

   SnapshotEntry(K key, V value, long snapshot) {
      this.snapshot = snapshot;
      this.key = (K)Objects.requireNonNull(key);
      this.value = (V)Objects.requireNonNull(value);
   }

   public final K getKey() {
      return this.key;
   }

   public final V getValue() {
      return this.value;
   }

   public V setValue(V value) {
      throw new UnsupportedOperationException();
   }

   public int weight() {
      return 1;
   }

   public long expiresAt() {
      return this.snapshot + Long.MAX_VALUE;
   }

   public long refreshableAt() {
      return this.snapshot + Long.MAX_VALUE;
   }

   public final long snapshotAt() {
      return this.snapshot;
   }

   public final boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Map.Entry)) {
         return false;
      } else {
         Map.Entry<?, ?> entry = (Map.Entry)o;
         return this.key.equals(entry.getKey()) && this.value.equals(entry.getValue());
      }
   }

   public final int hashCode() {
      return this.key.hashCode() ^ this.value.hashCode();
   }

   public final String toString() {
      String var10000 = String.valueOf(this.key);
      return var10000 + "=" + String.valueOf(this.value);
   }

   public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value) {
      return new SnapshotEntry<K, V>(key, value, 0L);
   }

   public static <K, V> SnapshotEntry<K, V> forEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
      long unsetTicks = snapshot + Long.MAX_VALUE;
      boolean refresh = refreshableAt != unsetTicks;
      boolean expires = expiresAt != unsetTicks;
      boolean weights = weight != 1;
      int features = (weights ? 1 : 0) | (expires ? 2 : 0) | (refresh ? 4 : 0);
      switch (features) {
         case 0:
            return new SnapshotEntry<K, V>(key, value, snapshot);
         case 1:
            return new WeightedEntry<K, V>(key, value, snapshot, weight);
         case 2:
            return new ExpirableEntry<K, V>(key, value, snapshot, expiresAt);
         case 3:
            return new ExpirableWeightedEntry<K, V>(key, value, snapshot, weight, expiresAt);
         case 4:
         case 5:
         default:
            return new CompleteEntry<K, V>(key, value, snapshot, weight, expiresAt, refreshableAt);
         case 6:
            return new RefreshableExpirableEntry<K, V>(key, value, snapshot, expiresAt, refreshableAt);
      }
   }

   static class WeightedEntry<K, V> extends SnapshotEntry<K, V> {
      final int weight;

      WeightedEntry(K key, V value, long snapshot, int weight) {
         super(key, value, snapshot);
         this.weight = weight;
      }

      public final int weight() {
         return this.weight;
      }
   }

   static class ExpirableEntry<K, V> extends SnapshotEntry<K, V> {
      final long expiresAt;

      ExpirableEntry(K key, V value, long snapshot, long expiresAt) {
         super(key, value, snapshot);
         this.expiresAt = expiresAt;
      }

      public final long expiresAt() {
         return this.expiresAt;
      }
   }

   static class ExpirableWeightedEntry<K, V> extends WeightedEntry<K, V> {
      final long expiresAt;

      ExpirableWeightedEntry(K key, V value, long snapshot, int weight, long expiresAt) {
         super(key, value, snapshot, weight);
         this.expiresAt = expiresAt;
      }

      public final long expiresAt() {
         return this.expiresAt;
      }
   }

   static class RefreshableExpirableEntry<K, V> extends ExpirableEntry<K, V> {
      final long refreshableAt;

      RefreshableExpirableEntry(K key, V value, long snapshot, long expiresAt, long refreshableAt) {
         super(key, value, snapshot, expiresAt);
         this.refreshableAt = refreshableAt;
      }

      public final long refreshableAt() {
         return this.refreshableAt;
      }
   }

   static final class CompleteEntry<K, V> extends ExpirableWeightedEntry<K, V> {
      final long refreshableAt;

      CompleteEntry(K key, V value, long snapshot, int weight, long expiresAt, long refreshableAt) {
         super(key, value, snapshot, weight, expiresAt);
         this.refreshableAt = refreshableAt;
      }

      public long refreshableAt() {
         return this.refreshableAt;
      }
   }
}
