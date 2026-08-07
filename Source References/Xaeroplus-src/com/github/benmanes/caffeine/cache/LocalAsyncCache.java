package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

interface LocalAsyncCache<K, V> extends AsyncCache<K, V> {
   System.Logger logger = System.getLogger(LocalAsyncCache.class.getName());

   LocalCache<K, CompletableFuture<V>> cache();

   Policy<K, V> policy();

   default @Nullable CompletableFuture<V> getIfPresent(K key) {
      return (CompletableFuture)this.cache().getIfPresent(key, true);
   }

   default CompletableFuture<V> get(K key, Function<? super K, ? extends V> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      return this.get(key, (BiFunction)((k, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(k), executor)));
   }

   default CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction) {
      return this.get(key, mappingFunction, true);
   }

   default CompletableFuture<V> get(K key, BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction, boolean recordStats) {
      long startTime = this.cache().statsTicker().read();
      CompletableFuture<? extends V>[] result = new CompletableFuture[1];
      CompletableFuture<V> future = (CompletableFuture)this.cache().computeIfAbsent(key, (k) -> {
         CompletableFuture<V> castedResult = (CompletableFuture)mappingFunction.apply(key, this.cache().executor());
         result[0] = castedResult;
         return (CompletableFuture)Objects.requireNonNull(castedResult);
      }, recordStats, false);
      if (result[0] != null) {
         this.handleCompletion(key, result[0], startTime, false);
      }

      return (CompletableFuture)Objects.requireNonNull(future);
   }

   default CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      return this.getAll(keys, (BiFunction)((keysToLoad, executor) -> CompletableFuture.supplyAsync(() -> (Map)mappingFunction.apply(keysToLoad), executor)));
   }

   default CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys, BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      Objects.requireNonNull(keys);
      int initialCapacity = Caffeine.calculateHashMapCapacity(keys);
      LinkedHashMap<K, CompletableFuture<V>> futures = new LinkedHashMap(initialCapacity);
      LinkedHashMap<K, CompletableFuture<V>> proxies = new LinkedHashMap(initialCapacity);

      for(K key : keys) {
         if (!futures.containsKey(key)) {
            CompletableFuture<V> future = (CompletableFuture)this.cache().getIfPresent(key, false);
            if (future == null) {
               CompletableFuture<V> proxy = new CompletableFuture();
               future = (CompletableFuture)this.cache().putIfAbsent(key, proxy);
               if (future == null) {
                  future = proxy;
                  proxies.put(key, proxy);
               }
            }

            futures.put(key, future);
         }
      }

      this.cache().statsCounter().recordMisses(proxies.size());
      this.cache().statsCounter().recordHits(futures.size() - proxies.size());
      if (proxies.isEmpty()) {
         return composeResult(futures);
      } else {
         AsyncBulkCompleter<K, V> completer = new AsyncBulkCompleter<K, V>(this.cache(), proxies);

         try {
            CompletableFuture<? extends Map<? extends K, ? extends V>> loader = (CompletableFuture)mappingFunction.apply(Collections.unmodifiableSet(proxies.keySet()), this.cache().executor());
            return loader.handle(completer).thenCompose((ignored) -> composeResult(futures));
         } catch (Throwable t) {
            throw completer.error(t);
         }
      }
   }

   static <K, V> CompletableFuture<Map<K, V>> composeResult(Map<K, CompletableFuture<@Nullable V>> futures) {
      if (futures.isEmpty()) {
         Map<K, V> emptyMap = Collections.unmodifiableMap(Collections.emptyMap());
         return CompletableFuture.completedFuture(emptyMap);
      } else {
         CompletableFuture<?>[] array = (CompletableFuture[])futures.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(array).thenApply((ignored) -> {
            LinkedHashMap<K, V> result = new LinkedHashMap(Caffeine.calculateHashMapCapacity(futures.size()));
            futures.forEach((key, future) -> {
               V value = (V)future.getNow((Object)null);
               if (value != null) {
                  result.put(key, value);
               }

            });
            return Collections.unmodifiableMap(result);
         });
      }
   }

   default void put(K key, CompletableFuture<? extends @Nullable V> valueFuture) {
      if (valueFuture.isDone() && Async.getWhenSuccessful(valueFuture) == null) {
         this.cache().statsCounter().recordLoadFailure(0L);
         this.cache().remove(key);
      } else {
         long startTime = this.cache().statsTicker().read();
         CompletableFuture<V> prior = (CompletableFuture)this.cache().put(key, valueFuture);
         if (prior != valueFuture) {
            this.handleCompletion(key, valueFuture, startTime, false);
         }

      }
   }

   default void handleCompletion(K key, CompletableFuture<? extends V> valueFuture, long startTime, boolean recordMiss) {
      valueFuture.whenComplete((value, error) -> {
         long loadTime = this.cache().statsTicker().read() - startTime;
         if (value == null) {
            if (error != null && !(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
               logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
            }

            this.cache().statsCounter().recordLoadFailure(loadTime);
            this.cache().remove(key, valueFuture);
         } else if (!Async.isReady(valueFuture)) {
            logger.log(Level.ERROR, String.format(Locale.US, "An invalid state was detected, occurring when the future's dependent action has completed successfully with a value, but the future remains either in-flight or in a failed completion state. This may occur when using a custom future that does not abide by the CompletableFuture contract (key: %s, key type: %s, value type: %s, future: %s, cache type: %s).", key, key.getClass().getName(), value.getClass().getName(), valueFuture, this.cache().getClass().getSimpleName()), new IllegalStateException());
            this.cache().statsCounter().recordLoadFailure(loadTime);
            this.cache().remove(key, valueFuture);
         } else {
            CompletableFuture<V> castedFuture = valueFuture;

            try {
               this.cache().replace(key, castedFuture, castedFuture, false);
               this.cache().statsCounter().recordLoadSuccess(loadTime);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
               this.cache().statsCounter().recordLoadFailure(loadTime);
               this.cache().remove(key, valueFuture);
            }
         }

         if (recordMiss) {
            this.cache().statsCounter().recordMisses(1);
         }

      });
   }

   public static final class AsyncBulkCompleter<K, V> implements BiFunction<Map<? extends K, ? extends V>, Throwable, Map<? extends K, ? extends V>> {
      private final Map<K, CompletableFuture<@Nullable V>> proxies;
      private final LocalCache<K, CompletableFuture<V>> cache;
      private final long startTime;

      AsyncBulkCompleter(LocalCache<K, CompletableFuture<V>> cache, Map<K, CompletableFuture<@Nullable V>> proxies) {
         this.startTime = cache.statsTicker().read();
         this.proxies = proxies;
         this.cache = cache;
      }

      @CanIgnoreReturnValue
      public @Nullable Map<? extends K, ? extends V> apply(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
         long loadTime = this.cache.statsTicker().read() - this.startTime;
         Throwable failure = this.handleResponse(result, error);
         if (failure == null) {
            this.cache.statsCounter().recordLoadSuccess(loadTime);
            return result;
         } else {
            this.cache.statsCounter().recordLoadFailure(loadTime);
            if (failure instanceof RuntimeException) {
               throw (RuntimeException)failure;
            } else if (failure instanceof Error) {
               throw (Error)failure;
            } else {
               throw new CompletionException(failure);
            }
         }
      }

      public CompletionException error(Throwable error) {
         long loadTime = this.cache.statsTicker().read() - this.startTime;
         Throwable failure = this.handleResponse((Map)null, error);
         this.cache.statsCounter().recordLoadFailure(loadTime);
         if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
         } else if (failure instanceof Error) {
            throw (Error)failure;
         } else {
            return new CompletionException(failure);
         }
      }

      private @Nullable Throwable handleResponse(@Nullable Map<? extends K, ? extends V> result, @Nullable Throwable error) {
         if (result != null) {
            Throwable failure = this.fillProxies(result);
            return this.addNewEntries(result, failure);
         } else {
            Throwable failure = (Throwable)(error == null ? new NullMapCompletionException() : error);

            for(Map.Entry<K, CompletableFuture<V>> entry : this.proxies.entrySet()) {
               this.cache.remove(entry.getKey(), entry.getValue());
               ((CompletableFuture)entry.getValue()).obtrudeException(failure);
            }

            if (!(failure instanceof CancellationException) && !(failure instanceof TimeoutException)) {
               LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", failure);
            }

            return failure;
         }
      }

      private @Nullable Throwable fillProxies(Map<? extends K, ? extends V> result) {
         Throwable error = null;

         for(Map.Entry<K, CompletableFuture<V>> entry : this.proxies.entrySet()) {
            K key = (K)entry.getKey();
            V value = (V)result.get(key);
            CompletableFuture<V> future = (CompletableFuture)entry.getValue();
            future.obtrudeValue(value);
            if (value == null) {
               this.cache.remove(key, future);
            } else {
               try {
                  this.cache.replace(key, future, future);
               } catch (Throwable t) {
                  LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                  this.cache.remove(key, future);
                  if (error == null) {
                     error = t;
                  } else {
                     error.addSuppressed(t);
                  }
               }
            }
         }

         return error;
      }

      private Throwable addNewEntries(Map<? extends K, ? extends V> result, Throwable failure) {
         Throwable error = failure;

         for(Map.Entry<? extends K, ? extends V> entry : result.entrySet()) {
            K key = (K)entry.getKey();
            V value = (V)entry.getValue();
            if (!this.proxies.containsKey(key) && value != null) {
               try {
                  this.cache.put(key, CompletableFuture.completedFuture(value));
               } catch (Throwable t) {
                  LocalAsyncCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", t);
                  if (error == null) {
                     error = t;
                  } else {
                     error.addSuppressed(t);
                  }
               }
            }
         }

         return error;
      }

      static final class NullMapCompletionException extends CompletionException {
         private static final long serialVersionUID = 1L;
      }
   }

   public static final class AsyncAsMapView<K, V> implements ConcurrentMap<K, CompletableFuture<V>> {
      final LocalAsyncCache<K, V> asyncCache;

      AsyncAsMapView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = (LocalAsyncCache)Objects.requireNonNull(asyncCache);
      }

      public boolean isEmpty() {
         return this.asyncCache.cache().isEmpty();
      }

      public int size() {
         return this.asyncCache.cache().size();
      }

      public void clear() {
         this.asyncCache.cache().clear();
      }

      public boolean containsKey(Object key) {
         return this.asyncCache.cache().containsKey(key);
      }

      public boolean containsValue(Object value) {
         return this.asyncCache.cache().containsValue(value);
      }

      public @Nullable CompletableFuture<V> get(Object key) {
         return (CompletableFuture)this.asyncCache.cache().get(key);
      }

      public @Nullable CompletableFuture<V> putIfAbsent(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().putIfAbsent(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior == null) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public @Nullable CompletableFuture<V> put(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().put(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior != value) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public void putAll(Map<? extends K, ? extends CompletableFuture<V>> map) {
         map.forEach(this::put);
      }

      public @Nullable CompletableFuture<V> replace(K key, CompletableFuture<V> value) {
         CompletableFuture<V> prior = (CompletableFuture)this.asyncCache.cache().replace(key, value);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (prior != null && prior != value) {
            this.asyncCache.handleCompletion(key, value, startTime, false);
         }

         return prior;
      }

      public boolean replace(K key, CompletableFuture<V> oldValue, CompletableFuture<V> newValue) {
         boolean replaced = this.asyncCache.cache().replace(key, oldValue, newValue);
         long startTime = this.asyncCache.cache().statsTicker().read();
         if (replaced && newValue != oldValue) {
            this.asyncCache.handleCompletion(key, newValue, startTime, false);
         }

         return replaced;
      }

      public CompletableFuture<V> remove(Object key) {
         return (CompletableFuture)this.asyncCache.cache().remove(key);
      }

      public boolean remove(Object key, Object value) {
         return this.asyncCache.cache().remove(key, value);
      }

      public CompletableFuture<V> computeIfAbsent(K key, Function<? super K, ? extends CompletableFuture<V>> mappingFunction) {
         Objects.requireNonNull(mappingFunction);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         Function<K, CompletableFuture<V>> function = (k) -> {
            result[0] = (CompletableFuture)mappingFunction.apply(k);
            return result[0];
         };
         CompletableFuture<V> future = this.asyncCache.cache().computeIfAbsent(key, function, false, false);
         if (result[0] == null) {
            if (future != null && this.asyncCache.cache().isRecordingStats()) {
               future.whenComplete((r, e) -> {
                  if (r != null || e == null) {
                     this.asyncCache.cache().statsCounter().recordHits(1);
                  }

               });
            }
         } else {
            this.asyncCache.handleCompletion(key, result[0], startTime, true);
         }

         return future;
      }

      public CompletableFuture<V> computeIfPresent(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         CompletableFuture<V>[] prior = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (Object k, CompletableFuture oldValue) -> {
            result[0] = oldValue == null ? null : (CompletableFuture)remappingFunction.apply(k, oldValue);
            prior[0] = oldValue;
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null && result[0] != prior[0]) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> compute(K key, BiFunction<? super K, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         CompletableFuture<V>[] prior = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (k, oldValue) -> {
            result[0] = (CompletableFuture)remappingFunction.apply(k, oldValue);
            prior[0] = oldValue;
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null && result[0] != prior[0]) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public CompletableFuture<V> merge(K key, CompletableFuture<V> value, BiFunction<? super CompletableFuture<V>, ? super CompletableFuture<V>, ? extends CompletableFuture<V>> remappingFunction) {
         Objects.requireNonNull(value);
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V>[] result = new CompletableFuture[1];
         CompletableFuture<V>[] prior = new CompletableFuture[1];
         long startTime = this.asyncCache.cache().statsTicker().read();
         this.asyncCache.cache().compute(key, (Object k, CompletableFuture oldValue) -> {
            result[0] = oldValue == null ? value : (CompletableFuture)remappingFunction.apply(oldValue, value);
            prior[0] = oldValue;
            return result[0];
         }, this.asyncCache.cache().expiry(), false, false);
         if (result[0] != null && result[0] != prior[0]) {
            this.asyncCache.handleCompletion(key, result[0], startTime, false);
         }

         return result[0];
      }

      public void forEach(BiConsumer<? super K, ? super CompletableFuture<V>> action) {
         this.asyncCache.cache().forEach(action);
      }

      public Set<K> keySet() {
         return this.asyncCache.cache().keySet();
      }

      public Collection<CompletableFuture<V>> values() {
         return this.asyncCache.cache().values();
      }

      public Set<Map.Entry<K, CompletableFuture<V>>> entrySet() {
         return this.asyncCache.cache().entrySet();
      }

      public boolean equals(@Nullable Object o) {
         return this.asyncCache.cache().equals(o);
      }

      public int hashCode() {
         return this.asyncCache.cache().hashCode();
      }

      public String toString() {
         return this.asyncCache.cache().toString();
      }
   }

   public static final class CacheView<K, V> extends AbstractCacheView<K, V> {
      private static final long serialVersionUID = 1L;
      final LocalAsyncCache<K, V> asyncCache;

      CacheView(LocalAsyncCache<K, V> asyncCache) {
         this.asyncCache = (LocalAsyncCache)Objects.requireNonNull(asyncCache);
      }

      LocalAsyncCache<K, V> asyncCache() {
         return this.asyncCache;
      }
   }

   public abstract static class AbstractCacheView<K, V> implements Cache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      transient @Nullable ConcurrentMap<K, V> asMapView;

      abstract LocalAsyncCache<K, V> asyncCache();

      public @Nullable V getIfPresent(K key) {
         CompletableFuture<V> future = (CompletableFuture)this.asyncCache().cache().getIfPresent(key, true);
         return (V)Async.getIfReady(future);
      }

      public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
         LinkedHashMap<K, V> result = new LinkedHashMap(Caffeine.calculateHashMapCapacity(keys));

         for(K key : keys) {
            result.put(key, (Object)null);
         }

         int uniqueKeys = result.size();
         Iterator<Map.Entry<K, V>> iter = result.entrySet().iterator();

         while(iter.hasNext()) {
            Map.Entry<K, V> entry = (Map.Entry)iter.next();
            CompletableFuture<V> future = (CompletableFuture)this.asyncCache().cache().get(entry.getKey());
            V value = (V)Async.getIfReady(future);
            if (value == null) {
               iter.remove();
            } else {
               entry.setValue(value);
            }
         }

         this.asyncCache().cache().statsCounter().recordHits(result.size());
         this.asyncCache().cache().statsCounter().recordMisses(uniqueKeys - result.size());
         Map<K, V> unmodifiable = Collections.unmodifiableMap(result);
         return unmodifiable;
      }

      public V get(K key, Function<? super K, ? extends V> mappingFunction) {
         return (V)resolve(this.asyncCache().get(key, mappingFunction));
      }

      public Map<K, V> getAll(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
         return (Map)resolve(this.asyncCache().getAll(keys, mappingFunction));
      }

      protected static <T> T resolve(CompletableFuture<T> future) {
         try {
            return (T)future.join();
         } catch (AsyncBulkCompleter.NullMapCompletionException var2) {
            throw new NullPointerException("null map");
         } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException) {
               throw (RuntimeException)e.getCause();
            } else if (e.getCause() instanceof Error) {
               throw (Error)e.getCause();
            } else {
               throw e;
            }
         }
      }

      public void put(K key, V value) {
         Objects.requireNonNull(value);
         this.asyncCache().cache().put(key, CompletableFuture.completedFuture(value));
      }

      public void putAll(Map<? extends K, ? extends V> map) {
         map.forEach(this::put);
      }

      public void invalidate(K key) {
         this.asyncCache().cache().remove(key);
      }

      public void invalidateAll(Iterable<? extends K> keys) {
         this.asyncCache().cache().invalidateAll(keys);
      }

      public void invalidateAll() {
         this.asyncCache().cache().clear();
      }

      public long estimatedSize() {
         return this.asyncCache().cache().estimatedSize();
      }

      public CacheStats stats() {
         return this.asyncCache().cache().statsCounter().snapshot();
      }

      public void cleanUp() {
         this.asyncCache().cache().cleanUp();
      }

      public Policy<K, V> policy() {
         return this.asyncCache().policy();
      }

      public ConcurrentMap<K, V> asMap() {
         return this.asMapView == null ? (this.asMapView = new AsMapView<K, V>(this.asyncCache().cache())) : this.asMapView;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private void readObjectNoData() throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      Object writeReplace() {
         return new SyncViewProxy(this.asyncCache());
      }
   }

   public static final class AsMapView<K, V> implements ConcurrentMap<K, V> {
      final LocalCache<K, CompletableFuture<V>> delegate;
      @Nullable Set<K> keys;
      @Nullable Collection<V> values;
      @Nullable Set<Map.Entry<K, V>> entries;

      AsMapView(LocalCache<K, CompletableFuture<V>> delegate) {
         this.delegate = delegate;
      }

      public boolean isEmpty() {
         return this.delegate.isEmpty();
      }

      public int size() {
         return this.delegate.size();
      }

      public void clear() {
         this.delegate.clear();
      }

      public boolean containsKey(Object key) {
         return Async.isReady(this.delegate.getIfPresentQuietly(key));
      }

      public boolean containsValue(Object value) {
         Objects.requireNonNull(value);

         for(CompletableFuture<V> valueFuture : this.delegate.values()) {
            if (value.equals(Async.getIfReady(valueFuture))) {
               return true;
            }
         }

         return false;
      }

      public @Nullable V get(Object key) {
         return (V)Async.getIfReady((CompletableFuture)this.delegate.get(key));
      }

      public @Nullable V putIfAbsent(K key, V value) {
         Objects.requireNonNull(value);
         CompletableFuture<V> priorFuture = null;

         while(true) {
            priorFuture = priorFuture == null ? (CompletableFuture)this.delegate.get(key) : (CompletableFuture)this.delegate.getIfPresentQuietly(key);
            if (priorFuture != null) {
               if (!priorFuture.isDone()) {
                  Async.getWhenSuccessful(priorFuture);
                  continue;
               }

               V prior = (V)Async.getWhenSuccessful(priorFuture);
               if (prior != null) {
                  return prior;
               }
            }

            boolean[] added = new boolean[]{false};
            CompletableFuture<V> computed = this.delegate.compute(key, (Object k, CompletableFuture valueFuture) -> {
               added[0] = valueFuture == null || valueFuture.isDone() && Async.getIfReady(valueFuture) == null;
               return added[0] ? CompletableFuture.completedFuture(value) : valueFuture;
            }, this.delegate.expiry(), false, false);
            if (added[0]) {
               return null;
            }

            V prior = (V)Async.getWhenSuccessful(computed);
            if (prior != null) {
               return prior;
            }
         }
      }

      public void putAll(Map<? extends K, ? extends V> map) {
         map.forEach(this::put);
      }

      public @Nullable V put(K key, V value) {
         Objects.requireNonNull(value);
         CompletableFuture<V> oldValueFuture = (CompletableFuture)this.delegate.put(key, CompletableFuture.completedFuture(value));
         return (V)Async.getWhenSuccessful(oldValueFuture);
      }

      public @Nullable V remove(Object key) {
         CompletableFuture<V> oldValueFuture = (CompletableFuture)this.delegate.remove(key);
         return (V)Async.getWhenSuccessful(oldValueFuture);
      }

      public boolean remove(Object key, @Nullable Object value) {
         Objects.requireNonNull(key);
         if (value == null) {
            return false;
         } else {
            K castedKey = (K)key;
            boolean[] done = new boolean[]{false};
            boolean[] removed = new boolean[]{false};
            CompletableFuture<V> future = null;

            do {
               future = future == null ? (CompletableFuture)this.delegate.get(castedKey) : (CompletableFuture)this.delegate.getIfPresentQuietly(castedKey);
               if (!Async.isReady(future)) {
                  return false;
               }

               Async.getWhenSuccessful(future);
               this.delegate.compute(castedKey, (Object k, CompletableFuture oldValueFuture) -> {
                  if (oldValueFuture == null) {
                     done[0] = true;
                     return null;
                  } else if (!oldValueFuture.isDone()) {
                     return oldValueFuture;
                  } else {
                     done[0] = true;
                     V oldValue = (V)Async.getIfReady(oldValueFuture);
                     removed[0] = Objects.equals(value, oldValue);
                     return oldValue != null && !removed[0] ? oldValueFuture : null;
                  }
               }, this.delegate.expiry(), false, true);
            } while(!done[0]);

            return removed[0];
         }
      }

      public V replace(K key, V value) {
         Objects.requireNonNull(value);
         V[] oldValue = (V[])(new Object[1]);
         boolean[] done = new boolean[]{false};

         do {
            CompletableFuture<V> future = this.delegate.getIfPresentQuietly(key);
            if (future == null || future.isCompletedExceptionally()) {
               return null;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (Object k, CompletableFuture oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               } else if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  done[0] = true;
                  oldValue[0] = Async.getIfReady(oldValueFuture);
                  return oldValue[0] == null ? null : CompletableFuture.completedFuture(value);
               }
            }, this.delegate.expiry(), false, false);
         } while(!done[0]);

         return (V)oldValue[0];
      }

      public boolean replace(K key, V oldValue, V newValue) {
         Objects.requireNonNull(oldValue);
         Objects.requireNonNull(newValue);
         boolean[] done = new boolean[]{false};
         boolean[] replaced = new boolean[]{false};

         do {
            CompletableFuture<V> future = this.delegate.getIfPresentQuietly(key);
            if (future == null || future.isCompletedExceptionally()) {
               return false;
            }

            Async.getWhenSuccessful(future);
            this.delegate.compute(key, (Object k, CompletableFuture oldValueFuture) -> {
               if (oldValueFuture == null) {
                  done[0] = true;
                  return null;
               } else if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  done[0] = true;
                  replaced[0] = Objects.equals(oldValue, Async.getIfReady(oldValueFuture));
                  return replaced[0] ? CompletableFuture.completedFuture(newValue) : oldValueFuture;
               }
            }, this.delegate.expiry(), false, false);
         } while(!done[0]);

         return replaced[0];
      }

      public @Nullable V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
         Objects.requireNonNull(mappingFunction);
         CompletableFuture<V> priorFuture = null;

         while(true) {
            while(true) {
               priorFuture = priorFuture == null ? (CompletableFuture)this.delegate.get(key) : (CompletableFuture)this.delegate.getIfPresentQuietly(key);
               if (priorFuture == null) {
                  break;
               }

               if (priorFuture.isDone()) {
                  V prior = (V)Async.getWhenSuccessful(priorFuture);
                  if (prior != null) {
                     this.delegate.statsCounter().recordHits(1);
                     return prior;
                  }
                  break;
               }

               Async.getWhenSuccessful(priorFuture);
            }

            CompletableFuture<V>[] future = new CompletableFuture[1];
            CompletableFuture<V> computed = this.delegate.compute(key, (Object k, CompletableFuture valueFuture) -> {
               if (valueFuture == null || valueFuture.isDone() && Async.getIfReady(valueFuture) == null) {
                  V newValue = (V)this.delegate.statsAware(mappingFunction, true).apply(key);
                  if (newValue == null) {
                     return null;
                  } else {
                     future[0] = CompletableFuture.completedFuture(newValue);
                     return future[0];
                  }
               } else {
                  return valueFuture;
               }
            }, this.delegate.expiry(), false, false);
            V result = (V)Async.getWhenSuccessful(computed);
            if (computed == future[0] || result != null) {
               return result;
            }
         }
      }

      public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         Objects.requireNonNull(remappingFunction);
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            valueFuture = (CompletableFuture)this.delegate.computeIfPresent(key, (k, oldValueFuture) -> {
               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  V oldValue = (V)Async.getIfReady(oldValueFuture);
                  if (oldValue == null) {
                     return null;
                  } else {
                     newValue[0] = remappingFunction.apply(key, oldValue);
                     return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
                  }
               }
            });
            if (newValue[0] != null) {
               return (V)newValue[0];
            }
         } while(valueFuture != null);

         return null;
      }

      public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
         Objects.requireNonNull(remappingFunction);
         V[] newValue = (V[])(new Object[1]);

         CompletableFuture<V> valueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            valueFuture = this.delegate.compute(key, (k, oldValueFuture) -> {
               if (oldValueFuture != null && !oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  V oldValue = (V)Async.getIfReady(oldValueFuture);
                  BiFunction<? super K, ? super V, ? extends V> function = this.delegate.<K, V, V>statsAware(remappingFunction, true, true);
                  newValue[0] = function.apply(key, oldValue);
                  return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
               }
            }, this.delegate.expiry(), false, false);
            if (newValue[0] != null) {
               return (V)newValue[0];
            }
         } while(valueFuture != null);

         return null;
      }

      public @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends @Nullable V> remappingFunction) {
         Objects.requireNonNull(value);
         Objects.requireNonNull(remappingFunction);
         CompletableFuture<V> newValueFuture = CompletableFuture.completedFuture(value);
         boolean[] merged = new boolean[]{false};

         CompletableFuture<V> mergedValueFuture;
         do {
            Async.getWhenSuccessful(this.delegate.getIfPresentQuietly(key));
            mergedValueFuture = (CompletableFuture)this.delegate.merge(key, newValueFuture, (oldValueFuture, valueFuture) -> {
               if (!oldValueFuture.isDone()) {
                  return oldValueFuture;
               } else {
                  merged[0] = true;
                  V oldValue = (V)Async.getIfReady(oldValueFuture);
                  if (oldValue == null) {
                     return valueFuture;
                  } else {
                     V mergedValue = (V)remappingFunction.apply(oldValue, value);
                     if (mergedValue == null) {
                        return null;
                     } else if (mergedValue == oldValue) {
                        return oldValueFuture;
                     } else {
                        return mergedValue == value ? valueFuture : CompletableFuture.completedFuture(mergedValue);
                     }
                  }
               }
            });
         } while(!merged[0] && mergedValueFuture != newValueFuture);

         return (V)Async.getWhenSuccessful(mergedValueFuture);
      }

      public Set<K> keySet() {
         return this.keys == null ? (this.keys = new KeySet()) : this.keys;
      }

      public Collection<V> values() {
         return this.values == null ? (this.values = new Values()) : this.values;
      }

      public Set<Map.Entry<K, V>> entrySet() {
         return this.entries == null ? (this.entries = new EntrySet()) : this.entries;
      }

      public boolean equals(@Nullable Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Map)) {
            return false;
         } else {
            Map<?, ?> map = (Map)o;
            int expectedSize = this.size();
            if (map.size() != expectedSize) {
               return false;
            } else {
               int count = 0;

               for(AsMapView<K, V>.EntryIterator iterator = new EntryIterator(); iterator.hasNext(); ++count) {
                  Map.Entry<K, V> entry = iterator.next();
                  Object value = map.get(entry.getKey());
                  if (value == null || value != entry.getValue() && !value.equals(entry.getValue())) {
                     return false;
                  }
               }

               return count == expectedSize;
            }
         }
      }

      public int hashCode() {
         int hash = 0;

         Map.Entry<K, V> entry;
         for(AsMapView<K, V>.EntryIterator iterator = new EntryIterator(); iterator.hasNext(); hash += entry.hashCode()) {
            entry = iterator.next();
         }

         return hash;
      }

      public String toString() {
         StringBuilder result = (new StringBuilder(50)).append('{');
         AsMapView<K, V>.EntryIterator iterator = new EntryIterator();

         while(iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            result.append(entry.getKey() == this ? "(this Map)" : entry.getKey()).append('=').append(entry.getValue() == this ? "(this Map)" : entry.getValue());
            if (iterator.hasNext()) {
               result.append(", ");
            }
         }

         return result.append('}').toString();
      }

      private final class KeySet extends AbstractSet<K> {
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         public int size() {
            return AsMapView.this.size();
         }

         public void clear() {
            AsMapView.this.clear();
         }

         public boolean contains(Object o) {
            return AsMapView.this.containsKey(o);
         }

         public boolean removeAll(Collection<?> collection) {
            return AsMapView.this.delegate.keySet().removeAll(collection);
         }

         public boolean remove(Object o) {
            return AsMapView.this.delegate.keySet().remove(o);
         }

         public boolean removeIf(Predicate<? super K> filter) {
            return AsMapView.this.delegate.keySet().removeIf(filter);
         }

         public boolean retainAll(Collection<?> collection) {
            return AsMapView.this.delegate.keySet().retainAll(collection);
         }

         public Iterator<K> iterator() {
            return new KeyIterator(AsMapView.this.new EntryIterator());
         }

         public Spliterator<K> spliterator() {
            return new KeySpliterator(AsMapView.this.new EntrySpliterator());
         }
      }

      private static final class KeyIterator<K, V> implements Iterator<K> {
         private final Iterator<Map.Entry<K, V>> iterator;

         KeyIterator(Iterator<Map.Entry<K, V>> iterator) {
            this.iterator = (Iterator)Objects.requireNonNull(iterator);
         }

         public boolean hasNext() {
            return this.iterator.hasNext();
         }

         public K next() {
            return (K)((Map.Entry)this.iterator.next()).getKey();
         }

         public void remove() {
            this.iterator.remove();
         }
      }

      private static final class KeySpliterator<K, V> implements Spliterator<K> {
         private final Spliterator<Map.Entry<K, V>> spliterator;

         KeySpliterator(Spliterator<Map.Entry<K, V>> iterator) {
            this.spliterator = (Spliterator)Objects.requireNonNull(iterator);
         }

         public boolean tryAdvance(Consumer<? super K> action) {
            Objects.requireNonNull(action);
            return this.spliterator.tryAdvance((entry) -> action.accept(entry.getKey()));
         }

         public @Nullable Spliterator<K> trySplit() {
            Spliterator<Map.Entry<K, V>> split = this.spliterator.trySplit();
            return split == null ? null : new KeySpliterator(split);
         }

         public long estimateSize() {
            return this.spliterator.estimateSize();
         }

         public int characteristics() {
            return 4353;
         }
      }

      private final class Values extends AbstractCollection<V> {
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         public int size() {
            return AsMapView.this.size();
         }

         public void clear() {
            AsMapView.this.clear();
         }

         public boolean contains(Object o) {
            return AsMapView.this.containsValue(o);
         }

         public boolean removeAll(Collection<?> collection) {
            Objects.requireNonNull(collection);
            boolean modified = false;

            for(Map.Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
               V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
               if (value != null && collection.contains(value) && AsMapView.this.remove(entry.getKey(), value)) {
                  modified = true;
               }
            }

            return modified;
         }

         public boolean remove(@Nullable Object o) {
            if (o == null) {
               return false;
            } else {
               for(Map.Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
                  V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
                  if (value != null && value.equals(o) && AsMapView.this.remove(entry.getKey(), value)) {
                     return true;
                  }
               }

               return false;
            }
         }

         public boolean removeIf(Predicate<? super V> filter) {
            Objects.requireNonNull(filter);
            return AsMapView.this.delegate.values().removeIf((future) -> {
               V value = (V)Async.getIfReady(future);
               return value != null && filter.test(value);
            });
         }

         public boolean retainAll(Collection<?> collection) {
            Objects.requireNonNull(collection);
            boolean modified = false;

            for(Map.Entry<K, CompletableFuture<V>> entry : AsMapView.this.delegate.entrySet()) {
               V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
               if (value != null && !collection.contains(value) && AsMapView.this.remove(entry.getKey(), value)) {
                  modified = true;
               }
            }

            return modified;
         }

         public void forEach(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            AsMapView.this.delegate.values().forEach((future) -> {
               V value = (V)Async.getIfReady(future);
               if (value != null) {
                  action.accept(value);
               }

            });
         }

         public Iterator<V> iterator() {
            return new ValueIterator(AsMapView.this.new EntryIterator());
         }

         public Spliterator<V> spliterator() {
            return new ValueSpliterator(AsMapView.this.new EntrySpliterator());
         }
      }

      private static final class ValueIterator<K, V> implements Iterator<V> {
         private final Iterator<Map.Entry<K, V>> iterator;

         ValueIterator(Iterator<Map.Entry<K, V>> iterator) {
            this.iterator = (Iterator)Objects.requireNonNull(iterator);
         }

         public boolean hasNext() {
            return this.iterator.hasNext();
         }

         public V next() {
            return (V)((Map.Entry)this.iterator.next()).getValue();
         }

         public void remove() {
            this.iterator.remove();
         }
      }

      private static final class ValueSpliterator<K, V> implements Spliterator<V> {
         private final Spliterator<Map.Entry<K, V>> spliterator;

         ValueSpliterator(Spliterator<Map.Entry<K, V>> iterator) {
            this.spliterator = (Spliterator)Objects.requireNonNull(iterator);
         }

         public boolean tryAdvance(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            return this.spliterator.tryAdvance((entry) -> action.accept(entry.getValue()));
         }

         public @Nullable Spliterator<V> trySplit() {
            Spliterator<Map.Entry<K, V>> split = this.spliterator.trySplit();
            return split == null ? null : new ValueSpliterator(split);
         }

         public long estimateSize() {
            return this.spliterator.estimateSize();
         }

         public int characteristics() {
            return 4352;
         }
      }

      private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
         public boolean isEmpty() {
            return AsMapView.this.isEmpty();
         }

         public int size() {
            return AsMapView.this.size();
         }

         public void clear() {
            AsMapView.this.clear();
         }

         public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
               return false;
            } else {
               Map.Entry<?, ?> entry = (Map.Entry)o;
               Object key = entry.getKey();
               Object value = entry.getValue();
               if (key != null && value != null) {
                  V cachedValue = (V)AsMapView.this.get(key);
                  return cachedValue != null && cachedValue.equals(value);
               } else {
                  return false;
               }
            }
         }

         public boolean removeAll(Collection<?> collection) {
            Objects.requireNonNull(collection);
            boolean modified = false;
            if (AsMapView.this.delegate.collectKeys() || collection instanceof Set && collection.size() > this.size()) {
               for(Map.Entry<K, V> entry : this) {
                  if (collection.contains(entry)) {
                     modified |= this.remove(entry);
                  }
               }
            } else {
               for(Object o : collection) {
                  modified |= this.remove(o);
               }
            }

            return modified;
         }

         public boolean remove(Object obj) {
            if (!(obj instanceof Map.Entry)) {
               return false;
            } else {
               Map.Entry<?, ?> entry = (Map.Entry)obj;
               Object key = entry.getKey();
               return key != null && AsMapView.this.remove(key, entry.getValue());
            }
         }

         public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
            Objects.requireNonNull(filter);
            boolean modified = false;

            for(Map.Entry<K, V> entry : this) {
               if (filter.test(entry)) {
                  modified |= AsMapView.this.remove(entry.getKey(), entry.getValue());
               }
            }

            return modified;
         }

         public boolean retainAll(Collection<?> collection) {
            Objects.requireNonNull(collection);
            boolean modified = false;

            for(Map.Entry<K, V> entry : this) {
               if (!collection.contains(entry) && this.remove(entry)) {
                  modified = true;
               }
            }

            return modified;
         }

         public Iterator<Map.Entry<K, V>> iterator() {
            return AsMapView.this.new EntryIterator();
         }

         public Spliterator<Map.Entry<K, V>> spliterator() {
            return AsMapView.this.new EntrySpliterator();
         }
      }

      private final class EntryIterator implements Iterator<Map.Entry<K, V>> {
         final Iterator<Map.Entry<K, CompletableFuture<V>>> iterator;
         Map.@Nullable Entry<K, V> cursor;
         @Nullable K removalKey;

         EntryIterator() {
            this.iterator = AsMapView.this.delegate.entrySet().iterator();
         }

         public boolean hasNext() {
            while(this.cursor == null && this.iterator.hasNext()) {
               Map.Entry<K, CompletableFuture<V>> entry = (Map.Entry)this.iterator.next();
               V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
               if (value != null) {
                  this.cursor = new WriteThroughEntry<K, V>(AsMapView.this, entry.getKey(), value);
               }
            }

            return this.cursor != null;
         }

         public Map.Entry<K, V> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               K key = (K)((Map.Entry)Objects.requireNonNull(this.cursor)).getKey();
               Map.Entry<K, V> entry = this.cursor;
               this.removalKey = key;
               this.cursor = null;
               return entry;
            }
         }

         public void remove() {
            Caffeine.requireState(this.removalKey != null);
            AsMapView.this.delegate.remove(this.removalKey);
            this.removalKey = (K)null;
         }
      }

      private final class EntrySpliterator implements Spliterator<Map.Entry<K, V>> {
         final Spliterator<Map.Entry<K, CompletableFuture<V>>> spliterator;

         EntrySpliterator() {
            this(AsMapView.this.delegate.entrySet().spliterator());
         }

         EntrySpliterator(Spliterator<Map.Entry<K, CompletableFuture<V>>> spliterator) {
            this.spliterator = (Spliterator)Objects.requireNonNull(spliterator);
         }

         public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            this.spliterator.forEachRemaining((entry) -> {
               V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
               if (value != null) {
                  WriteThroughEntry<K, V> e = new WriteThroughEntry<K, V>(AsMapView.this, entry.getKey(), value);
                  action.accept(e);
               }

            });
         }

         public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            boolean[] advanced = new boolean[]{false};
            Consumer<? super Map.Entry<K, CompletableFuture<V>>> consumer = (entry) -> {
               V value = (V)Async.getIfReady((CompletableFuture)entry.getValue());
               if (value != null) {
                  WriteThroughEntry<K, V> e = new WriteThroughEntry<K, V>(AsMapView.this, entry.getKey(), value);
                  action.accept(e);
                  advanced[0] = true;
               }

            };

            while(this.spliterator.tryAdvance(consumer)) {
               if (advanced[0]) {
                  return true;
               }
            }

            return false;
         }

         public @Nullable Spliterator<Map.Entry<K, V>> trySplit() {
            Spliterator<Map.Entry<K, CompletableFuture<V>>> split = this.spliterator.trySplit();
            return split == null ? null : AsMapView.this.new EntrySpliterator(split);
         }

         public long estimateSize() {
            return this.spliterator.estimateSize();
         }

         public int characteristics() {
            return 4353;
         }
      }
   }

   public static final class SyncViewProxy<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final AsyncCache<K, V> asyncCache;

      SyncViewProxy(AsyncCache<K, V> asyncCache) {
         this.asyncCache = (AsyncCache)Objects.requireNonNull(asyncCache);
      }

      Object readResolve() {
         return this.asyncCache.synchronous();
      }
   }
}
