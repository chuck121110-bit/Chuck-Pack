package net.lenni0451.lambdaevents;

import net.lenni0451.lambdaevents.utils.EventException;
import net.lenni0451.lambdaevents.utils.ThrowingExceptionHandler;

public interface IExceptionHandler {
   static IExceptionHandler simplePrint() {
      return (handler, event, t) -> t.printStackTrace();
   }

   static IExceptionHandler infoPrint() {
      return (handler, event, t) -> (new EventException("Exception occurred in '" + event.getClass().getSimpleName() + "' handler in '" + handler.getOwner().getName() + "'", t)).printStackTrace();
   }

   static IExceptionHandler throwing() {
      return new ThrowingExceptionHandler();
   }

   static IExceptionHandler ignore() {
      return (handler, event, t) -> {
      };
   }

   static IExceptionHandler stopCall() {
      return (handler, event, t) -> {
         throw StopCall.INSTANCE;
      };
   }

   void handle(AHandler var1, Object var2, Throwable var3);
}
