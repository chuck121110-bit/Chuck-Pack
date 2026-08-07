package net.lenni0451.lambdaevents;

public class StopCall extends RuntimeException {
   public static final StopCall INSTANCE = new StopCall();

   private StopCall() {
   }

   public synchronized Throwable fillInStackTrace() {
      return this;
   }

   public StackTraceElement[] getStackTrace() {
      return new StackTraceElement[0];
   }
}
