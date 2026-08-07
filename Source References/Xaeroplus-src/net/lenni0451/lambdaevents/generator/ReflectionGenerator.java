package net.lenni0451.lambdaevents.generator;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.IGenerator;
import net.lenni0451.lambdaevents.handler.reflection.ReflectionHandler;
import net.lenni0451.lambdaevents.handler.reflection.VirtualReflectionHandler;

public class ReflectionGenerator implements IGenerator {
   @Nonnull
   public AHandler generate(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, Class<?> arg) {
      method.setAccessible(true);
      return new ReflectionHandler(owner, instance, annotation, method);
   }

   @Nonnull
   public AHandler generateVirtual(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method) {
      method.setAccessible(true);
      return new VirtualReflectionHandler(owner, instance, annotation, method);
   }
}
