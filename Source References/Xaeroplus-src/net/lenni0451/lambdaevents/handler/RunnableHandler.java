package net.lenni0451.lambdaevents.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;

public class RunnableHandler extends AHandler {
   @Nonnull
   private final Runnable runnable;

   public RunnableHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation, Runnable runnable) {
      super(owner, instance, annotation);
      this.runnable = runnable;
   }

   @Nonnull
   public Runnable getRunnable() {
      return this.runnable;
   }

   public void call(Object event) {
      this.runnable.run();
   }

   public String toString() {
      return "runnable: " + this.owner.getName() + " -> " + this.runnable.getClass().getName();
   }
}
