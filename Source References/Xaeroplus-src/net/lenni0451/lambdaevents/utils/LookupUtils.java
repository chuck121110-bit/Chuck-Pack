package net.lenni0451.lambdaevents.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;

public class LookupUtils {
   private static final Map<ClassLoader, LookupGetterLoader> loaders = Collections.synchronizedMap(new WeakHashMap());

   @Nonnull
   public static MethodHandles.Lookup getIn(ClassLoader classLoader) {
      try {
         LookupGetterLoader loader = (LookupGetterLoader)loaders.computeIfAbsent(classLoader, LookupGetterLoader::new);
         Class<?> lookupGetter;
         if (!loader.isDefined(LookupGetter.class.getName())) {
            InputStream is = LookupGetter.class.getClassLoader().getResourceAsStream(LookupGetter.class.getName().replace('.', '/') + ".class");
            Throwable var4 = null;

            try {
               if (is == null) {
                  throw new ClassNotFoundException(LookupGetter.class.getName());
               }

               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               byte[] buf = new byte[1024];

               int len;
               while((len = is.read(buf)) != -1) {
                  baos.write(buf, 0, len);
               }

               lookupGetter = loader.define(LookupGetter.class.getName(), baos.toByteArray());
            } catch (Throwable var16) {
               var4 = var16;
               throw var16;
            } finally {
               if (is != null) {
                  if (var4 != null) {
                     try {
                        is.close();
                     } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                     }
                  } else {
                     is.close();
                  }
               }

            }
         } else {
            lookupGetter = loader.loadClass(LookupGetter.class.getName());
         }

         return (MethodHandles.Lookup)lookupGetter.getDeclaredMethod("get").invoke((Object)null);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   @Nonnull
   public static MethodHandles.Lookup resolveLookup(MethodHandles.Lookup lookup, Class<?> accessed) {
      if (canAccess(lookup, accessed)) {
         return lookup;
      } else {
         lookup = lookup.in(accessed);
         if (canAccess(lookup, accessed)) {
            return lookup;
         } else {
            lookup = getIn(accessed.getClassLoader());
            if (canAccess(lookup, accessed)) {
               return lookup;
            } else {
               lookup = lookup.in(accessed);
               if (canAccess(lookup, accessed)) {
                  return lookup;
               } else {
                  throw new IllegalStateException("Could not resolve lookup for " + accessed.getName());
               }
            }
         }
      }
   }

   public static boolean canAccess(MethodHandles.Lookup lookup, Class<?> clazz) {
      return canAccess(clazz, lookup.lookupClass()) && (lookup.lookupModes() & 2) != 0;
   }

   public static boolean canAccess(Class<?> wanted, Class<?> clazz) {
      if (wanted == clazz) {
         return true;
      } else {
         while(wanted.isArray()) {
            wanted = wanted.getComponentType();
         }

         if (wanted.isPrimitive()) {
            return true;
         } else if (wanted == Object.class) {
            return true;
         } else {
            ClassLoader wantedLoader = wanted.getClassLoader();
            ClassLoader clazzLoader = clazz.getClassLoader();
            if (wantedLoader == clazzLoader) {
               return true;
            } else if (wantedLoader != null && clazzLoader == null) {
               return false;
            } else if (wantedLoader == null && wanted.getName().startsWith("java.")) {
               return true;
            } else {
               try {
                  return Class.forName(wanted.getName(), false, clazzLoader) == wanted;
               } catch (Throwable var5) {
                  return false;
               }
            }
         }
      }
   }

   private static class LookupGetterLoader extends ClassLoader {
      protected LookupGetterLoader(ClassLoader parent) {
         super(parent);
      }

      protected Object getClassLoadingLock(String name) {
         return super.getClassLoadingLock(name);
      }

      protected Class<?> define(String name, byte[] bytes) {
         synchronized(this.getClassLoadingLock(name)) {
            Class<?> clazz = this.defineClass(name, bytes, 0, bytes.length);
            this.resolveClass(clazz);
            return clazz;
         }
      }

      protected boolean isDefined(String name) {
         synchronized(this.getClassLoadingLock(name)) {
            return this.findLoadedClass(name) != null;
         }
      }

      static {
         ClassLoader.registerAsParallelCapable();
      }
   }
}
