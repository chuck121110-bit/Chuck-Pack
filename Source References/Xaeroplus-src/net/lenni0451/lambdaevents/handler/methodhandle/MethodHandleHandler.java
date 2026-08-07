package net.lenni0451.lambdaevents.handler.methodhandle;

import java.lang.invoke.MethodHandle;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.utils.EventUtils;

public class MethodHandleHandler extends AHandler {
   private final MethodHandle methodHandle;

   public MethodHandleHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation, MethodHandle methodHandle) {
      super(owner, instance, annotation);
      this.methodHandle = methodHandle;
   }

   public void call(Object event) {
      try {
         this.methodHandle.invoke(event);
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   public String toString() {
      return "methodHandle: " + this.owner.getName() + " -> " + EventUtils.toString(this.methodHandle);
   }
}
