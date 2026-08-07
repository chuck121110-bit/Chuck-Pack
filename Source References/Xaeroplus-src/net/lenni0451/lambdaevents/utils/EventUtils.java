package net.lenni0451.lambdaevents.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.lenni0451.lambdaevents.EventHandler;

public class EventUtils {
   @Nonnull
   public static List<MethodHandler> getMethods(Class<?> owner, Predicate<Method> accept, boolean registerSuperHandler) {
      List<MethodHandler> handler = new ArrayList();
      Set<Class<?>> classes = new LinkedHashSet();
      if (registerSuperHandler) {
         getSuperClasses(classes, owner);
      } else {
         classes.add(owner);
      }

      Set<MethodID> methodIDs = new HashSet();

      for(Class<?> current : classes) {
         for(Method method : current.getDeclaredMethods()) {
            EventHandler annotation = (EventHandler)method.getDeclaredAnnotation(EventHandler.class);
            if (annotation != null && accept.test(method)) {
               MethodID id = new MethodID(method.getName(), method.getParameterTypes());
               if (Modifier.isPrivate(method.getModifiers()) || methodIDs.add(id)) {
                  handler.add(new MethodHandler(current, annotation, method));
               }
            }
         }
      }

      return handler;
   }

   @Nonnull
   public static List<FieldHandler> getFields(Class<?> owner, Predicate<Field> accept, boolean registerSuperHandler) {
      List<FieldHandler> handler = new ArrayList();
      Set<Class<?>> classes = new LinkedHashSet();
      if (registerSuperHandler) {
         getSuperClasses(classes, owner);
      } else {
         classes.add(owner);
      }

      for(Class<?> current : classes) {
         for(Field field : current.getDeclaredFields()) {
            EventHandler annotation = (EventHandler)field.getDeclaredAnnotation(EventHandler.class);
            if (annotation != null && accept.test(field)) {
               handler.add(new FieldHandler(current, annotation, field));
            }
         }
      }

      return handler;
   }

   public static void verify(Class<?> owner, EventHandler annotation, Method method) {
      if (Modifier.isAbstract(method.getModifiers())) {
         throw new IllegalStateException("Method '" + method.getName() + "' in class '" + owner.getName() + "' is abstract");
      } else if (Modifier.isNative(method.getModifiers())) {
         throw new IllegalStateException("Method '" + method.getName() + "' in class '" + owner.getName() + "' is native");
      } else if (annotation.events().length == 0 && method.getParameterCount() != 1) {
         throw new IllegalStateException("Method '" + method.getName() + "' in class '" + owner.getName() + "' has no virtual events and not exactly 1 parameter");
      } else if (annotation.events().length > 0 && method.getParameterCount() != 0) {
         throw new IllegalStateException("Method '" + method.getName() + "' in class '" + owner.getName() + "' has virtual events and more than 0 parameters");
      } else if (!method.getReturnType().equals(Void.TYPE)) {
         throw new IllegalStateException("Method '" + method.getName() + "' in class '" + owner.getName() + "' has a return type");
      }
   }

   public static void verify(Class<?> owner, EventHandler annotation, Field field) {
      if (Runnable.class.isAssignableFrom(field.getType())) {
         if (annotation.events().length == 0) {
            throw new IllegalStateException("Field '" + field.getName() + "' in class '" + owner.getName() + "' has no virtual events");
         }
      } else {
         if (!Consumer.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Field '" + field.getName() + "' in class '" + owner.getName() + "' is not a Runnable or Consumer");
         }

         if (annotation.events().length == 0) {
            if (!(field.getGenericType() instanceof ParameterizedType)) {
               throw new IllegalStateException("Field '" + field.getName() + "' in class '" + owner.getName() + "' has no virtual events and no generic type");
            }

            ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
            if (parameterizedType.getActualTypeArguments().length != 1) {
               throw new IllegalStateException("Field '" + field.getName() + "' in class '" + owner.getName() + "' has no virtual events and more than 1 generic type");
            }
         }
      }

   }

   @Nonnull
   public static Class<?>[] getEvents(EventHandler annotation, Method method, Predicate<Class<?>> accept) {
      if (method.getParameterCount() == 1) {
         Class<?> param = method.getParameterTypes()[0];
         return !accept.test(param) ? new Class[0] : new Class[]{param};
      } else {
         return (Class[])Arrays.stream(annotation.events()).filter(accept).toArray((x$0) -> new Class[x$0]);
      }
   }

   @Nonnull
   public static Class<?>[] getEvents(EventHandler annotation, Field field, Predicate<Class<?>> accept) {
      List<Class<?>> events = new ArrayList();
      Collections.addAll(events, annotation.events());
      if (Consumer.class.isAssignableFrom(field.getType()) && events.isEmpty() && field.getGenericType() instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
         events.add((Class)parameterizedType.getActualTypeArguments()[0]);
      }

      return (Class[])events.stream().filter(accept).toArray((x$0) -> new Class[x$0]);
   }

   @Nonnull
   public static EventHandler newEventHandler(final int priority) {
      return new EventHandler() {
         public Class<? extends Annotation> annotationType() {
            return EventHandler.class;
         }

         public int priority() {
            return priority;
         }

         public Class<?>[] events() {
            return new Class[0];
         }

         public boolean handleCancelled() {
            return true;
         }
      };
   }

   public static String toString(Method method) {
      Class<?> returnType = method.getReturnType();
      Class<?>[] params = method.getParameterTypes();
      StringBuilder out = (new StringBuilder()).append(method.getName()).append('(');

      for(int i = 0; i < params.length; ++i) {
         out.append(params[i].getSimpleName());
         if (i != params.length - 1) {
            out.append(", ");
         }
      }

      return out.append(")").append(returnType.getSimpleName()).toString();
   }

   public static String toString(MethodHandle methodHandle) {
      MethodType type = methodHandle.type();
      StringBuilder out = (new StringBuilder()).append('(');

      for(int i = 0; i < type.parameterCount(); ++i) {
         out.append(type.parameterType(i).getSimpleName());
         if (i != type.parameterCount() - 1) {
            out.append(", ");
         }
      }

      return out.append(")").append(type.returnType().getSimpleName()).toString();
   }

   public static void getSuperClasses(Set<Class<?>> classes, Class<?> clazz) {
      classes.add(clazz);
      Class<?> superClass = clazz.getSuperclass();
      Class<?>[] interfaces = clazz.getInterfaces();
      if (superClass != null && !classes.contains(superClass)) {
         getSuperClasses(classes, superClass);
      }

      for(Class<?> anInterface : interfaces) {
         if (!classes.contains(anInterface)) {
            getSuperClasses(classes, anInterface);
         }
      }

   }

   private static class MethodID {
      @Nonnull
      private final String name;
      @Nonnull
      private final Class<?>[] params;

      public MethodID(@Nonnull String name, @Nonnull Class<?>[] params) {
         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         } else if (params == null) {
            throw new NullPointerException("params is marked non-null but is null");
         } else {
            this.name = name;
            this.params = params;
         }
      }

      @Nonnull
      public String getName() {
         return this.name;
      }

      @Nonnull
      public Class<?>[] getParams() {
         return this.params;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof MethodID)) {
            return false;
         } else {
            MethodID other = (MethodID)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$name = this.getName();
               Object other$name = other.getName();
               if (this$name == null) {
                  if (other$name != null) {
                     return false;
                  }
               } else if (!this$name.equals(other$name)) {
                  return false;
               }

               if (!Arrays.deepEquals(this.getParams(), other.getParams())) {
                  return false;
               } else {
                  return true;
               }
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof MethodID;
      }

      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $name = this.getName();
         result = result * 59 + ($name == null ? 43 : $name.hashCode());
         result = result * 59 + Arrays.deepHashCode(this.getParams());
         return result;
      }

      public String toString() {
         return "EventUtils.MethodID(name=" + this.getName() + ", params=" + Arrays.deepToString(this.getParams()) + ")";
      }
   }

   public static class MethodHandler {
      @Nonnull
      private final Class<?> owner;
      @Nonnull
      private final EventHandler annotation;
      @Nonnull
      private final Method method;

      public MethodHandler(@Nonnull Class<?> owner, @Nonnull EventHandler annotation, @Nonnull Method method) {
         if (owner == null) {
            throw new NullPointerException("owner is marked non-null but is null");
         } else if (annotation == null) {
            throw new NullPointerException("annotation is marked non-null but is null");
         } else if (method == null) {
            throw new NullPointerException("method is marked non-null but is null");
         } else {
            this.owner = owner;
            this.annotation = annotation;
            this.method = method;
         }
      }

      @Nonnull
      public Class<?> getOwner() {
         return this.owner;
      }

      @Nonnull
      public EventHandler getAnnotation() {
         return this.annotation;
      }

      @Nonnull
      public Method getMethod() {
         return this.method;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof MethodHandler)) {
            return false;
         } else {
            MethodHandler other = (MethodHandler)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$owner = this.getOwner();
               Object other$owner = other.getOwner();
               if (this$owner == null) {
                  if (other$owner != null) {
                     return false;
                  }
               } else if (!this$owner.equals(other$owner)) {
                  return false;
               }

               Object this$annotation = this.getAnnotation();
               Object other$annotation = other.getAnnotation();
               if (this$annotation == null) {
                  if (other$annotation != null) {
                     return false;
                  }
               } else if (!this$annotation.equals(other$annotation)) {
                  return false;
               }

               Object this$method = this.getMethod();
               Object other$method = other.getMethod();
               if (this$method == null) {
                  if (other$method != null) {
                     return false;
                  }
               } else if (!this$method.equals(other$method)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof MethodHandler;
      }

      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $owner = this.getOwner();
         result = result * 59 + ($owner == null ? 43 : $owner.hashCode());
         Object $annotation = this.getAnnotation();
         result = result * 59 + ($annotation == null ? 43 : $annotation.hashCode());
         Object $method = this.getMethod();
         result = result * 59 + ($method == null ? 43 : $method.hashCode());
         return result;
      }

      public String toString() {
         return "EventUtils.MethodHandler(owner=" + this.getOwner() + ", annotation=" + this.getAnnotation() + ", method=" + this.getMethod() + ")";
      }
   }

   public static class FieldHandler {
      @Nonnull
      private final Class<?> owner;
      @Nonnull
      private final EventHandler annotation;
      @Nonnull
      private final Field field;

      public FieldHandler(@Nonnull Class<?> owner, @Nonnull EventHandler annotation, @Nonnull Field field) {
         if (owner == null) {
            throw new NullPointerException("owner is marked non-null but is null");
         } else if (annotation == null) {
            throw new NullPointerException("annotation is marked non-null but is null");
         } else if (field == null) {
            throw new NullPointerException("field is marked non-null but is null");
         } else {
            this.owner = owner;
            this.annotation = annotation;
            this.field = field;
         }
      }

      @Nonnull
      public Class<?> getOwner() {
         return this.owner;
      }

      @Nonnull
      public EventHandler getAnnotation() {
         return this.annotation;
      }

      @Nonnull
      public Field getField() {
         return this.field;
      }

      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof FieldHandler)) {
            return false;
         } else {
            FieldHandler other = (FieldHandler)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$owner = this.getOwner();
               Object other$owner = other.getOwner();
               if (this$owner == null) {
                  if (other$owner != null) {
                     return false;
                  }
               } else if (!this$owner.equals(other$owner)) {
                  return false;
               }

               Object this$annotation = this.getAnnotation();
               Object other$annotation = other.getAnnotation();
               if (this$annotation == null) {
                  if (other$annotation != null) {
                     return false;
                  }
               } else if (!this$annotation.equals(other$annotation)) {
                  return false;
               }

               Object this$field = this.getField();
               Object other$field = other.getField();
               if (this$field == null) {
                  if (other$field != null) {
                     return false;
                  }
               } else if (!this$field.equals(other$field)) {
                  return false;
               }

               return true;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof FieldHandler;
      }

      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $owner = this.getOwner();
         result = result * 59 + ($owner == null ? 43 : $owner.hashCode());
         Object $annotation = this.getAnnotation();
         result = result * 59 + ($annotation == null ? 43 : $annotation.hashCode());
         Object $field = this.getField();
         result = result * 59 + ($field == null ? 43 : $field.hashCode());
         return result;
      }

      public String toString() {
         return "EventUtils.FieldHandler(owner=" + this.getOwner() + ", annotation=" + this.getAnnotation() + ", field=" + this.getField() + ")";
      }
   }
}
