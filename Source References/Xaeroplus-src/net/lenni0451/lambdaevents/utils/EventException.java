package net.lenni0451.lambdaevents.utils;

public class EventException extends Exception {
   public EventException(String message, Throwable cause) {
      super(message, cause);
   }

   public synchronized Throwable fillInStackTrace() {
      return this;
   }

   public StackTraceElement[] getStackTrace() {
      return new StackTraceElement[0];
   }
}
