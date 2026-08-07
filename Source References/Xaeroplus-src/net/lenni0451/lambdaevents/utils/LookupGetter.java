package net.lenni0451.lambdaevents.utils;

import java.lang.invoke.MethodHandles;
import javax.annotation.Nonnull;

public class LookupGetter {
   @Nonnull
   public static MethodHandles.Lookup get() {
      return MethodHandles.lookup();
   }
}
