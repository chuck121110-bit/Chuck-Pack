package net.lenni0451.lambdaevents;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.handler.ConsumerHandler;
import net.lenni0451.lambdaevents.handler.RunnableHandler;
import net.lenni0451.lambdaevents.types.ICancellableEvent;
import net.lenni0451.lambdaevents.utils.EventUtils;

public class LambdaManager {
   private final Map<Class<?>, List<AHandler>> handlers;
   private final Map<Class<?>, AHandler[]> handlerArrays;
   private final Map<Class<?>, Class<?>[]> parentsCache;
   private final Supplier<List<AHandler>> listSupplier;
   private final IGenerator generator;
   @Nullable
   private IEventFilter eventFilter = null;
   private IExceptionHandler exceptionHandler = IExceptionHandler.infoPrint();
   private boolean registerSuperHandler = false;
   private boolean alwaysCallParents = false;

   public static LambdaManager basic(IGenerator generator) {
      return new LambdaManager(HashMap::new, ArrayList::new, generator);
   }

   public static LambdaManager threadSafe(IGenerator generator) {
      return new LambdaManager(ConcurrentHashMap::new, CopyOnWriteArrayList::new, generator);
   }

   public LambdaManager(Supplier<Map> mapSupplier, Supplier<List<AHandler>> listSupplier, IGenerator generator) {
      this.handlers = (Map)mapSupplier.get();
      this.handlerArrays = (Map)mapSupplier.get();
      this.parentsCache = (Map)mapSupplier.get();
      this.listSupplier = listSupplier;
      this.generator = generator;
   }

   public LambdaManager setEventFilter(@Nullable IEventFilter eventFilter) {
      this.eventFilter = eventFilter;
      return this;
   }

   public LambdaManager setExceptionHandler(IExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
      return this;
   }

   public LambdaManager setRegisterSuperHandler(boolean registerSuperHandler) {
      this.registerSuperHandler = registerSuperHandler;
      return this;
   }

   public LambdaManager setAlwaysCallParents(boolean alwaysCallParents) {
      this.alwaysCallParents = alwaysCallParents;
      return this;
   }

   @Nonnull
   public <T> T call(T event) {
      if (this.alwaysCallParents) {
         return (T)this.callParents(event);
      } else if (this.eventFilter != null && !this.eventFilter.check(event.getClass(), IEventFilter.CheckType.CALL)) {
         return event;
      } else {
         this.call(event.getClass(), event);
         return event;
      }
   }

   @Nonnull
   public <T> T callParents(T event) {
      if (this.eventFilter != null && !this.eventFilter.check(event.getClass(), IEventFilter.CheckType.CALL)) {
         return event;
      } else {
         for(Class<?> clazz : (Class[])this.parentsCache.computeIfAbsent(event.getClass(), (clazzx) -> {
            Set<Class<?>> parents = new LinkedHashSet();
            EventUtils.getSuperClasses(parents, clazzx);
            return (Class[])parents.toArray(new Class[0]);
         })) {
            this.call(clazz, event);
         }

         return event;
      }
   }

   private <T> void call(Class<?> clazz, T event) {
      AHandler[] handlers = (AHandler[])this.handlerArrays.get(clazz);
      if (handlers != null) {
         ICancellableEvent cancellable = event instanceof ICancellableEvent ? (ICancellableEvent)event : null;

         for(AHandler handler : handlers) {
            if (cancellable == null || handler.shouldHandleCancelled() || !cancellable.isCancelled()) {
               try {
                  handler.call(event);
               } catch (StopCall var10) {
                  return;
               } catch (Throwable t) {
                  this.exceptionHandler.handle(handler, event, t);
               }
            }
         }

      }
   }

   public void register(Class<?> owner) {
      this.register((Class)null, owner);
   }

   public void register(@Nullable Class<?> event, Class<?> owner) {
      this.register(event, owner, (Object)null, true, false);
   }

   public void register(Object owner) {
      this.register((Class)null, owner);
   }

   public void register(@Nullable Class<?> event, Object owner) {
      this.register(event, owner.getClass(), owner, false, this.registerSuperHandler);
   }

   public void registerSuper(Object owner) {
      this.registerSuper((Class)null, owner);
   }

   public void registerSuper(@Nullable Class<?> event, Object owner) {
      this.register(event, owner.getClass(), owner, false, true);
   }

   public void registerRunnable(Runnable runnable, Class<?>... events) {
      this.registerRunnable(runnable, 0, events);
   }

   public void registerRunnable(Runnable runnable, int priority, Class<?>... events) {
      if (events.length == 0) {
         throw new IllegalArgumentException("No events specified");
      } else {
         synchronized(this.handlers) {
            for(Class<?> event : events) {
               if (this.eventFilter == null || this.eventFilter.check(event, IEventFilter.CheckType.EXPLICIT_REGISTER)) {
                  List<AHandler> handlers = (List)this.handlers.computeIfAbsent(event, (key) -> (List)this.listSupplier.get());
                  handlers.add(new RunnableHandler(runnable.getClass(), runnable, EventUtils.newEventHandler(priority), runnable));
                  this.checkCallChain(event, handlers);
               }
            }

         }
      }
   }

   public void registerConsumer(Consumer<?> consumer, Class<?>... events) {
      this.registerConsumer(consumer, 0, events);
   }

   public void registerConsumer(Consumer<?> consumer, int priority, Class<?>... events) {
      if (events.length == 0) {
         throw new IllegalArgumentException("No events specified");
      } else {
         synchronized(this.handlers) {
            for(Class<?> event : events) {
               if (this.eventFilter == null || this.eventFilter.check(event, IEventFilter.CheckType.EXPLICIT_REGISTER)) {
                  List<AHandler> handlers = (List)this.handlers.computeIfAbsent(event, (key) -> (List)this.listSupplier.get());
                  handlers.add(new ConsumerHandler(consumer.getClass(), consumer, EventUtils.newEventHandler(priority), consumer));
                  this.checkCallChain(event, handlers);
               }
            }

         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void register(Runnable runnable, Class<?>... events) {
      this.registerRunnable(runnable, events);
   }

   /** @deprecated */
   @Deprecated
   public void register(Runnable runnable, int priority, Class<?>... events) {
      this.registerRunnable(runnable, priority, events);
   }

   /** @deprecated */
   @Deprecated
   public void register(Consumer<?> consumer, Class<?>... events) {
      this.registerConsumer(consumer, events);
   }

   /** @deprecated */
   @Deprecated
   public void register(Consumer<?> consumer, int priority, Class<?>... events) {
      this.registerConsumer(consumer, priority, events);
   }

   private void register(@Nullable Class<?> event, Class<?> owner, @Nullable Object instance, boolean isStatic, boolean registerSuperHandler) {
      Predicate<Class<?>> eventFilter;
      if (event == null) {
         eventFilter = (e) -> this.eventFilter == null || this.eventFilter.check(e, IEventFilter.CheckType.REGISTER);
      } else {
         if (this.eventFilter != null && !this.eventFilter.check(event, IEventFilter.CheckType.EXPLICIT_REGISTER)) {
            return;
         }

         eventFilter = (e) -> e.equals(event);
      }

      for(EventUtils.MethodHandler handler : EventUtils.getMethods(owner, (methodx) -> Modifier.isStatic(methodx.getModifiers()) == isStatic, registerSuperHandler)) {
         EventHandler annotation = handler.getAnnotation();
         Method method = handler.getMethod();
         EventUtils.verify(handler.getOwner(), annotation, method);

         for(Class<?> eventClass : EventUtils.getEvents(annotation, method, eventFilter)) {
            this.registerMethod(handler.getOwner(), instance, annotation, method, eventClass, method.getParameterCount() == 0);
         }
      }

      for(EventUtils.FieldHandler handler : EventUtils.getFields(owner, (fieldx) -> Modifier.isStatic(fieldx.getModifiers()) == isStatic, registerSuperHandler)) {
         EventHandler annotation = handler.getAnnotation();
         Field field = handler.getField();
         EventUtils.verify(handler.getOwner(), annotation, field);

         for(Class<?> eventClass : EventUtils.getEvents(annotation, field, eventFilter)) {
            this.registerField(handler.getOwner(), instance, annotation, field, eventClass);
         }
      }

   }

   private void registerMethod(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, Class<?> event, boolean virtual) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.computeIfAbsent(event, (key) -> (List)this.listSupplier.get());
         AHandler handler;
         if (virtual) {
            handler = this.generator.generateVirtual(owner, instance, annotation, method);
         } else {
            handler = this.generator.generate(owner, instance, annotation, method, event);
         }

         handlers.add(handler);
         this.checkCallChain(event, handlers);
      }
   }

   private void registerField(Class<?> owner, @Nullable Object instance, EventHandler annotation, Field field, Class<?> event) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.computeIfAbsent(event, (key) -> (List)this.listSupplier.get());

         AHandler handler;
         try {
            if (Runnable.class.isAssignableFrom(field.getType())) {
               handler = new RunnableHandler(owner, instance, annotation, (Runnable)field.get(instance));
            } else {
               handler = new ConsumerHandler(owner, instance, annotation, (Consumer)field.get(instance));
            }
         } catch (Throwable t) {
            throw new RuntimeException("Failed to register field '" + field.getName() + "' in class '" + owner.getName() + "'", t);
         }

         handlers.add(handler);
         this.checkCallChain(event, handlers);
      }
   }

   public void unregister(Class<?> owner) {
      synchronized(this.handlers) {
         Map<Class<?>, List<AHandler>> checked = new HashMap();

         for(Map.Entry<Class<?>, List<AHandler>> entry : this.handlers.entrySet()) {
            List<AHandler> handlers = (List)entry.getValue();
            handlers.removeIf((handler) -> handler.isStatic() && handler.getOwner().equals(owner));
            checked.put(entry.getKey(), handlers);
         }

         for(Map.Entry<Class<?>, List<AHandler>> entry : checked.entrySet()) {
            this.checkCallChain((Class)entry.getKey(), (List)entry.getValue());
         }

      }
   }

   public void unregister(Class<?> event, Class<?> owner) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.get(event);
         if (handlers != null) {
            handlers.removeIf((handler) -> handler.isStatic() && handler.getOwner().equals(owner));
            this.checkCallChain(event, handlers);
         }
      }
   }

   public void unregister(Object owner) {
      synchronized(this.handlers) {
         Map<Class<?>, List<AHandler>> checked = new HashMap();

         for(Map.Entry<Class<?>, List<AHandler>> entry : this.handlers.entrySet()) {
            List<AHandler> handlers = (List)entry.getValue();
            handlers.removeIf((handler) -> !handler.isStatic() && owner.equals(handler.getInstance()));
            checked.put(entry.getKey(), handlers);
         }

         for(Map.Entry<Class<?>, List<AHandler>> entry : checked.entrySet()) {
            this.checkCallChain((Class)entry.getKey(), (List)entry.getValue());
         }

      }
   }

   public void unregister(Class<?> event, Object owner) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.get(event);
         if (handlers != null) {
            handlers.removeIf((handler) -> !handler.isStatic() && owner.equals(handler.getInstance()));
            this.checkCallChain(event, handlers);
         }
      }
   }

   public void unregisterRunnable(Runnable runnable) {
      synchronized(this.handlers) {
         Map<Class<?>, List<AHandler>> checked = new HashMap();

         for(Map.Entry<Class<?>, List<AHandler>> entry : this.handlers.entrySet()) {
            List<AHandler> handlers = (List)entry.getValue();
            handlers.removeIf((handler) -> handler instanceof RunnableHandler && ((RunnableHandler)handler).getRunnable().equals(runnable));
            checked.put(entry.getKey(), handlers);
         }

         for(Map.Entry<Class<?>, List<AHandler>> entry : checked.entrySet()) {
            this.checkCallChain((Class)entry.getKey(), (List)entry.getValue());
         }

      }
   }

   public void unregisterRunnable(Runnable runnable, Class<?>... events) {
      if (events.length == 0) {
         this.unregisterRunnable(runnable);
      } else {
         synchronized(this.handlers) {
            for(Class<?> event : events) {
               List<AHandler> handlers = (List)this.handlers.get(event);
               if (handlers != null) {
                  handlers.removeIf((handler) -> handler instanceof RunnableHandler && ((RunnableHandler)handler).getRunnable().equals(runnable));
                  this.checkCallChain(event, handlers);
               }
            }

         }
      }
   }

   public void unregisterConsumer(Consumer<?> consumer) {
      synchronized(this.handlers) {
         Map<Class<?>, List<AHandler>> checked = new HashMap();

         for(Map.Entry<Class<?>, List<AHandler>> entry : this.handlers.entrySet()) {
            List<AHandler> handlers = (List)entry.getValue();
            handlers.removeIf((handler) -> handler instanceof ConsumerHandler && ((ConsumerHandler)handler).getConsumer().equals(consumer));
            checked.put(entry.getKey(), handlers);
         }

         for(Map.Entry<Class<?>, List<AHandler>> entry : checked.entrySet()) {
            this.checkCallChain((Class)entry.getKey(), (List)entry.getValue());
         }

      }
   }

   public void unregisterConsumer(Consumer<?> consumer, Class<?>... events) {
      if (events.length == 0) {
         this.unregisterConsumer(consumer);
      } else {
         synchronized(this.handlers) {
            for(Class<?> event : events) {
               List<AHandler> handlers = (List)this.handlers.get(event);
               if (handlers != null) {
                  handlers.removeIf((handler) -> handler instanceof ConsumerHandler && ((ConsumerHandler)handler).getConsumer().equals(consumer));
                  this.checkCallChain(event, handlers);
               }
            }

         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void unregister(Runnable runnable) {
      this.unregisterRunnable(runnable);
   }

   /** @deprecated */
   @Deprecated
   public void unregister(Runnable runnable, Class<?>... events) {
      this.unregisterRunnable(runnable, events);
   }

   /** @deprecated */
   @Deprecated
   public void unregister(Consumer<?> consumer) {
      this.unregisterConsumer(consumer);
   }

   /** @deprecated */
   @Deprecated
   public void unregister(Consumer<?> consumer, Class<?>... events) {
      this.unregisterConsumer(consumer, events);
   }

   public void unregisterAll(Class<?> event) {
      synchronized(this.handlers) {
         this.checkCallChain(event, Collections.emptyList());
      }
   }

   public void unregisterAll(Class<?> event, Predicate<Class<?>> filter) {
      synchronized(this.handlers) {
         this.unregisterAll(event, filter, false);
         this.unregisterAll(event, filter, true);
      }
   }

   public void unregisterAll(Class<?> event, Predicate<Class<?>> filter, boolean staticHandlers) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.get(event);
         if (handlers != null) {
            handlers.removeIf((handler) -> handler.isStatic() != staticHandlers ? false : filter.test(handler.getOwner()));
            this.checkCallChain(event, handlers);
         }
      }
   }

   public void unregisterAll(Class<?> event, BiPredicate<Class<?>, Optional<Object>> filter) {
      synchronized(this.handlers) {
         List<AHandler> handlers = (List)this.handlers.get(event);
         if (handlers != null) {
            handlers.removeIf((handler) -> filter.test(handler.getOwner(), Optional.ofNullable(handler.getInstance())));
            this.checkCallChain(event, handlers);
         }
      }
   }

   private void checkCallChain(Class<?> event, List<AHandler> handlers) {
      if (handlers.isEmpty()) {
         this.handlers.remove(event);
         this.handlerArrays.remove(event);
      } else {
         if (handlers.size() > 1) {
            handlers.sort(Comparator.comparingInt((o) -> o.getAnnotation().priority()).reversed());
         }

         this.handlerArrays.put(event, handlers.toArray(new AHandler[0]));
      }
   }

   public String toString() {
      StringBuilder out = new StringBuilder("LambdaManager{\n");

      for(Map.Entry<Class<?>, AHandler[]> entry : this.handlerArrays.entrySet()) {
         out.append("\t").append(((Class)entry.getKey()).getName()).append("[\n");

         for(AHandler handler : (AHandler[])entry.getValue()) {
            out.append("\t\t").append(handler.toString()).append("\n");
         }

         out.append("\t]\n");
      }

      return out.append("}").toString();
   }
}
