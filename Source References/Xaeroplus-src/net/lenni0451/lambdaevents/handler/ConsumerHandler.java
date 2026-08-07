package net.lenni0451.lambdaevents.handler;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;

public class ConsumerHandler extends AHandler {
   @Nonnull
   private final Consumer<Object> consumer;

   public ConsumerHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation, Consumer consumer) {
      super(owner, instance, annotation);
      this.consumer = consumer;
   }

   @Nonnull
   public Consumer<Object> getConsumer() {
      return this.consumer;
   }

   public void call(Object event) {
      this.consumer.accept(event);
   }

   public String toString() {
      return "consumer: " + this.owner.getName() + " -> " + this.consumer.getClass().getName();
   }
}
