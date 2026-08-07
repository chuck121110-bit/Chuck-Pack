package net.lenni0451.lambdaevents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AHandler {
   @Nonnull
   protected final Class<?> owner;
   @Nullable
   protected final Object instance;
   @Nonnull
   protected final EventHandler annotation;
   private final boolean handleCancelled;

   public AHandler(Class<?> owner, @Nullable Object instance, EventHandler annotation) {
      this.owner = owner;
      this.instance = instance;
      this.annotation = annotation;
      this.handleCancelled = annotation.handleCancelled();
   }

   @Nonnull
   public Class<?> getOwner() {
      return this.owner;
   }

   @Nullable
   public Object getInstance() {
      return this.instance;
   }

   @Nonnull
   public EventHandler getAnnotation() {
      return this.annotation;
   }

   public boolean isStatic() {
      return this.instance == null;
   }

   public boolean shouldHandleCancelled() {
      return this.handleCancelled;
   }

   public abstract void call(@Nonnull Object var1);

   public abstract String toString();
}
