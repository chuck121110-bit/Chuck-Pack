package com.github.benmanes.caffeine.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Policy<K, V> {
   boolean isRecordingStats();

   @Nullable V getIfPresentQuietly(K key);

   default @Nullable CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
      throw new UnsupportedOperationException();
   }

   Map<K, CompletableFuture<V>> refreshes();

   Optional<Eviction<K, V>> eviction();

   Optional<FixedExpiration<K, V>> expireAfterAccess();

   Optional<FixedExpiration<K, V>> expireAfterWrite();

   Optional<VarExpiration<K, V>> expireVariably();

   Optional<FixedRefresh<K, V>> refreshAfterWrite();

   public interface Eviction<K, V> {
      boolean isWeighted();

      OptionalInt weightOf(K key);

      OptionalLong weightedSize();

      long getMaximum();

      void setMaximum(long maximum);

      Map<K, V> coldest(int limit);

      default Map<K, V> coldestWeighted(long weightLimit) {
         throw new UnsupportedOperationException();
      }

      default <T extends @Nullable Object> T coldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }

      Map<K, V> hottest(int limit);

      default Map<K, V> hottestWeighted(long weightLimit) {
         throw new UnsupportedOperationException();
      }

      default <T extends @Nullable Object> T hottest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }
   }

   public interface FixedExpiration<K, V> {
      OptionalLong ageOf(K key, TimeUnit unit);

      default Optional<Duration> ageOf(K key) {
         OptionalLong duration = this.ageOf(key, TimeUnit.NANOSECONDS);
         return duration.isPresent() ? Optional.of(Duration.ofNanos(duration.getAsLong())) : Optional.empty();
      }

      long getExpiresAfter(TimeUnit unit);

      default Duration getExpiresAfter() {
         return Duration.ofNanos(this.getExpiresAfter(TimeUnit.NANOSECONDS));
      }

      void setExpiresAfter(long duration, TimeUnit unit);

      default void setExpiresAfter(Duration duration) {
         this.setExpiresAfter(Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
      }

      Map<K, V> oldest(int limit);

      default <T extends @Nullable Object> T oldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }

      Map<K, V> youngest(int limit);

      default <T extends @Nullable Object> T youngest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }
   }

   public interface VarExpiration<K, V> {
      OptionalLong getExpiresAfter(K key, TimeUnit unit);

      default Optional<Duration> getExpiresAfter(K key) {
         OptionalLong duration = this.getExpiresAfter(key, TimeUnit.NANOSECONDS);
         return duration.isPresent() ? Optional.of(Duration.ofNanos(duration.getAsLong())) : Optional.empty();
      }

      void setExpiresAfter(K key, long duration, TimeUnit unit);

      default void setExpiresAfter(K key, Duration duration) {
         this.setExpiresAfter(key, Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
      }

      @Nullable V putIfAbsent(K key, V value, long duration, TimeUnit unit);

      default @Nullable V putIfAbsent(K key, V value, Duration duration) {
         return (V)this.putIfAbsent(key, value, Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
      }

      @Nullable V put(K key, V value, long duration, TimeUnit unit);

      default @Nullable V put(K key, V value, Duration duration) {
         return (V)this.put(key, value, Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
      }

      default @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction, Duration duration) {
         throw new UnsupportedOperationException();
      }

      Map<K, V> oldest(int limit);

      default <T extends @Nullable Object> T oldest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }

      Map<K, V> youngest(int limit);

      default <T extends @Nullable Object> T youngest(Function<Stream<CacheEntry<K, V>>, T> mappingFunction) {
         throw new UnsupportedOperationException();
      }
   }

   public interface FixedRefresh<K, V> {
      OptionalLong ageOf(K key, TimeUnit unit);

      default Optional<Duration> ageOf(K key) {
         OptionalLong duration = this.ageOf(key, TimeUnit.NANOSECONDS);
         return duration.isPresent() ? Optional.of(Duration.ofNanos(duration.getAsLong())) : Optional.empty();
      }

      long getRefreshesAfter(TimeUnit unit);

      default Duration getRefreshesAfter() {
         return Duration.ofNanos(this.getRefreshesAfter(TimeUnit.NANOSECONDS));
      }

      void setRefreshesAfter(long duration, TimeUnit unit);

      default void setRefreshesAfter(Duration duration) {
         this.setRefreshesAfter(Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS);
      }
   }

   @NullMarked
   public interface CacheEntry<K, V> extends Map.Entry<K, V> {
      int weight();

      long expiresAt();

      default Duration expiresAfter() {
         return Duration.ofNanos(this.expiresAt() - this.snapshotAt());
      }

      long refreshableAt();

      default Duration refreshableAfter() {
         return Duration.ofNanos(this.refreshableAt() - this.snapshotAt());
      }

      long snapshotAt();
   }
}
