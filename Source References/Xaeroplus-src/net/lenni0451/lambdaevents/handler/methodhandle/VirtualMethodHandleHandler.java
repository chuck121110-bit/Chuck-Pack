package net.lenni0451.lambdaevents.handler.methodhandle;

import java.lang.invoke.MethodHandle;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.utils.EventUtils;

public class VirtualMethodHandleHandler extends AHandler {
   private final MethodHandle methodHandle;

   public VirtualMethodHandleHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation, MethodHandle methodHandle) {
      super(owner, instance, annotation);
      this.methodHandle = methodHandle;
   }

   public void call(Object event) {
      try {
         this.methodHandle.invokeExact();
      } catch (Throwable $ex) {
         throw $ex;
      }
   }

   public String toString() {
      return "virtualMethodHandle: " + this.owner.getName() + " -> " + EventUtils.toString(this.methodHandle);
   }
}
