package net.lenni0451.lambdaevents.generator;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.IGenerator;
import net.lenni0451.lambdaevents.handler.ConsumerHandler;
import net.lenni0451.lambdaevents.handler.RunnableHandler;
import net.lenni0451.lambdaevents.utils.LookupUtils;

public class LambdaMetaFactoryGenerator implements IGenerator {
   private final MethodHandles.Lookup lookup;

   public LambdaMetaFactoryGenerator() {
      this(MethodHandles.lookup());
   }

   public LambdaMetaFactoryGenerator(@Nonnull MethodHandles.Lookup lookup) {
      this.lookup = lookup;
   }

   @Nonnull
   public AHandler generate(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, Class<?> arg) {
      Consumer<?> consumer = (Consumer)this.generate(owner, instance, method, Consumer.class, "accept", MethodType.methodType(Void.TYPE, Object.class));
      return new ConsumerHandler(owner, instance, annotation, consumer);
   }

   @Nonnull
   public AHandler generateVirtual(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method) {
      Runnable runnable = (Runnable)this.generate(owner, instance, method, Runnable.class, "run", MethodType.methodType(Void.TYPE));
      return new RunnableHandler(owner, instance, annotation, runnable);
   }

   private <T> T generate(Class<?> owner, @Nullable Object instance, Method method, Class<T> interfaceClass, String interfaceMethod, MethodType interfaceType) {
      try {
         MethodHandles.Lookup lookup = LookupUtils.resolveLookup(this.lookup, owner);
         MethodHandle handle = lookup.unreflect(method);
         return (T)(instance == null ? LambdaMetafactory.metafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass), interfaceType, handle, handle.type()).getTarget().invoke() : LambdaMetafactory.metafactory(lookup, interfaceMethod, MethodType.methodType(interfaceClass, instance.getClass()), interfaceType, handle, handle.type().dropParameterTypes(0, 1)).getTarget().invoke(instance));
      } catch (Throwable $ex) {
         throw $ex;
      }
   }
}
