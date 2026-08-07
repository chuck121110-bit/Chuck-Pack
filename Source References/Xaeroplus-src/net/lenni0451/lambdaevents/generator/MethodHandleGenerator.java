package net.lenni0451.lambdaevents.generator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.IGenerator;
import net.lenni0451.lambdaevents.handler.methodhandle.MethodHandleHandler;
import net.lenni0451.lambdaevents.handler.methodhandle.VirtualMethodHandleHandler;
import net.lenni0451.lambdaevents.utils.LookupUtils;

public class MethodHandleGenerator implements IGenerator {
   private final MethodHandles.Lookup lookup;

   public MethodHandleGenerator() {
      this(MethodHandles.lookup());
   }

   public MethodHandleGenerator(MethodHandles.Lookup lookup) {
      this.lookup = lookup;
   }

   @Nonnull
   public AHandler generate(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, Class<?> arg) {
      MethodHandle handle = this.getHandle(owner, instance, method);
      return new MethodHandleHandler(owner, instance, annotation, handle);
   }

   @Nonnull
   public AHandler generateVirtual(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method) {
      MethodHandle handle = this.getHandle(owner, instance, method);
      return new VirtualMethodHandleHandler(owner, instance, annotation, handle);
   }

   private MethodHandle getHandle(Class<?> owner, @Nullable Object instance, Method method) {
      try {
         MethodHandle handle = LookupUtils.resolveLookup(this.lookup, owner).unreflect(method);
         if (instance != null) {
            handle = handle.bindTo(instance);
         }

         return handle;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }
}
