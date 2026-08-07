package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface LocalCacheFactory {
   MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
   MethodType FACTORY = MethodType.methodType(Void.TYPE, Caffeine.class, AsyncCacheLoader.class, Boolean.TYPE);
   MethodType FACTORY_CALL = FACTORY.changeReturnType(BoundedLocalCache.class);
   ConcurrentMap<String, LocalCacheFactory> FACTORIES = new ConcurrentHashMap();
   String EXPIRES_AFTER_ACCESS_NANOS = "expiresAfterAccessNanos";
   String EXPIRES_AFTER_WRITE_NANOS = "expiresAfterWriteNanos";
   String REFRESH_AFTER_WRITE_NANOS = "refreshAfterWriteNanos";
   String WEIGHTED_SIZE = "weightedSize";
   String MAXIMUM = "maximum";

   <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) throws Throwable;

   static <K, V> BoundedLocalCache<K, V> newBoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) {
      String className = getClassName(builder);
      LocalCacheFactory factory = loadFactory(className);

      try {
         return factory.<K, V>newInstance(builder, cacheLoader, isAsync);
      } catch (Error | RuntimeException e) {
         throw e;
      } catch (Throwable t) {
         throw new IllegalStateException(className, t);
      }
   }

   static String getClassName(Caffeine<?, ?> builder) {
      StringBuilder className = new StringBuilder();
      if (builder.isStrongKeys()) {
         className.append('S');
      } else {
         className.append('W');
      }

      if (builder.isStrongValues()) {
         className.append('S');
      } else {
         className.append('I');
      }

      if (builder.removalListener != null) {
         className.append('L');
      }

      if (builder.isRecordingStats()) {
         className.append('S');
      }

      if (builder.evicts()) {
         className.append('M');
         if (builder.isWeighted()) {
            className.append('W');
         } else {
            className.append('S');
         }
      }

      if (builder.expiresAfterAccess() || builder.expiresVariable()) {
         className.append('A');
      }

      if (builder.expiresAfterWrite()) {
         className.append('W');
      }

      if (builder.refreshAfterWrite()) {
         className.append('R');
      }

      return className.toString();
   }

   static LocalCacheFactory loadFactory(String className) {
      LocalCacheFactory factory = (LocalCacheFactory)FACTORIES.get(className);
      if (factory == null) {
         factory = (LocalCacheFactory)FACTORIES.computeIfAbsent(className, LocalCacheFactory::newFactory);
      }

      return factory;
   }

   static LocalCacheFactory newFactory(String className) {
      try {
         MethodHandles.Lookup var10000 = LOOKUP;
         String var10001 = LocalCacheFactory.class.getPackageName();
         Class<?> clazz = var10000.findClass(var10001 + "." + className);

         try {
            return LOOKUP.findStaticVarHandle(clazz, "FACTORY", LocalCacheFactory.class).get();
         } catch (NoSuchFieldException var3) {
            return new MethodHandleBasedFactory(clazz);
         }
      } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException t) {
         throw new IllegalStateException(className, t);
      }
   }

   public static final class MethodHandleBasedFactory implements LocalCacheFactory {
      final MethodHandle methodHandle;

      MethodHandleBasedFactory(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
         this.methodHandle = LOOKUP.findConstructor(clazz, FACTORY).asType(FACTORY_CALL);
      }

      public <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean async) throws Throwable {
         return this.methodHandle.invokeExact(builder, cacheLoader, async);
      }
   }
}
