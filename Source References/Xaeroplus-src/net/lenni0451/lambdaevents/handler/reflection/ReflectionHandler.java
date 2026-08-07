package net.lenni0451.lambdaevents.handler.reflection;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.utils.EventUtils;

public class ReflectionHandler extends AHandler {
   private final Method method;

   public ReflectionHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method) {
      super(owner, instance, annotation);
      this.method = method;
   }

   public void call(Object event) {
      try {
         this.method.invoke(this.instance, event);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   public String toString() {
      return "reflection: " + this.owner.getName() + " -> " + EventUtils.toString(this.method);
   }
}
