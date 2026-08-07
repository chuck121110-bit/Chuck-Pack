package com.github.benmanes.caffeine.cache;

import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.jspecify.annotations.Nullable;

final class Async {
   static final long ASYNC_EXPIRY = 6917529027641081854L;
   static final System.Logger logger = System.getLogger(Async.class.getName());

   private Async() {
   }

   static boolean isReady(@Nullable CompletableFuture<?> future) {
      return future != null && future.isDone() && !future.isCompletedExceptionally() && future.join() != null;
   }

   static <V> @Nullable V getIfReady(@Nullable CompletableFuture<V> future) {
      return (V)(isReady(future) ? ((CompletableFuture)Objects.requireNonNull(future)).join() : null);
   }

   static <V> @Nullable V getWhenSuccessful(@Nullable CompletableFuture<V> future) {
      try {
         return (V)(future == null ? null : future.join());
      } catch (CompletionException | CancellationException var2) {
         return null;
      }
   }

   static final class AsyncRemovalListener<K, V> implements RemovalListener<K, CompletableFuture<@Nullable V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final RemovalListener<K, V> delegate;
      final Executor executor;

      AsyncRemovalListener(RemovalListener<K, V> delegate, Executor executor) {
         this.delegate = (RemovalListener)Objects.requireNonNull(delegate);
         this.executor = (Executor)Objects.requireNonNull(executor);
      }

      public void onRemoval(@Nullable K key, @Nullable CompletableFuture<@Nullable V> future, RemovalCause cause) {
         if (future != null) {
            future.thenAccept((value) -> {
               if (value != null) {
                  Runnable task = () -> {
                     try {
                        this.delegate.onRemoval(key, value, cause);
                     } catch (Throwable t) {
                        Async.logger.log(Level.WARNING, "Exception thrown by removal listener", t);
                     }

                  };

                  try {
                     this.executor.execute(task);
                  } catch (Throwable t) {
                     Async.logger.log(Level.ERROR, "Exception thrown when submitting removal listener", t);
                     task.run();
                  }
               }

            });
         }

      }

      Object writeReplace() {
         return this.delegate;
      }
   }

   static final class AsyncEvictionListener<K, V> implements RemovalListener<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final RemovalListener<K, V> delegate;

      AsyncEvictionListener(RemovalListener<K, V> delegate) {
         this.delegate = (RemovalListener)Objects.requireNonNull(delegate);
      }

      public void onRemoval(@Nullable K key, @Nullable CompletableFuture<V> future, RemovalCause cause) {
         V value = (V)Async.getIfReady(future);
         if (value != null) {
            this.delegate.onRemoval(key, value, cause);
         }

      }

      Object writeReplace() {
         return this.delegate;
      }
   }

   static final class AsyncWeigher<K, V> implements Weigher<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final Weigher<K, V> delegate;

      AsyncWeigher(Weigher<K, V> delegate) {
         this.delegate = (Weigher)Objects.requireNonNull(delegate);
      }

      public int weigh(K key, CompletableFuture<V> future) {
         return Async.isReady(future) ? this.delegate.weigh(key, future.join()) : 0;
      }

      Object writeReplace() {
         return this.delegate;
      }
   }

   static final class AsyncExpiry<K, V> implements Expiry<K, CompletableFuture<V>>, Serializable {
      private static final long serialVersionUID = 1L;
      final Expiry<? super K, ? super V> delegate;

      AsyncExpiry(Expiry<? super K, ? super V> delegate) {
         this.delegate = (Expiry)Objects.requireNonNull(delegate);
      }

      public long expireAfterCreate(K key, CompletableFuture<V> future, long currentTime) {
         if (Async.isReady(future)) {
            long duration = this.delegate.expireAfterCreate(key, future.join(), currentTime);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      public long expireAfterUpdate(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
         if (Async.isReady(future)) {
            long duration = currentDuration > 4611686018427387903L ? this.delegate.expireAfterCreate(key, future.join(), currentTime) : this.delegate.expireAfterUpdate(key, future.join(), currentTime, currentDuration);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      public long expireAfterRead(K key, CompletableFuture<V> future, long currentTime, long currentDuration) {
         if (Async.isReady(future)) {
            long duration = this.delegate.expireAfterRead(key, future.join(), currentTime, currentDuration);
            return Math.min(duration, 4611686018427387903L);
         } else {
            return 6917529027641081854L;
         }
      }

      Object writeReplace() {
         return this.delegate;
      }
   }
}
