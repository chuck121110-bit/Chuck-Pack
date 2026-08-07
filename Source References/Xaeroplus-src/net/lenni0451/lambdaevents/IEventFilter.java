package net.lenni0451.lambdaevents;

public interface IEventFilter {
   boolean check(Class<?> var1, CheckType var2);

   public static enum CheckType {
      CALL,
      REGISTER,
      EXPLICIT_REGISTER;
   }
}
