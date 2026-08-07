package net.lenni0451.lambdaevents.utils;

import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.IExceptionHandler;

public class ThrowingExceptionHandler implements IExceptionHandler {
   public void handle(AHandler handler, Object event, Throwable t) {
      try {
         throw t;
      } catch (Throwable $ex) {
         throw $ex;
      }
   }
}
