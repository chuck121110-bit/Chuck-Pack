package net.lenni0451.lambdaevents;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGenerator {
   @Nonnull
   AHandler generate(Class<?> var1, @Nullable Object var2, EventHandler var3, Method var4, Class<?> var5);

   @Nonnull
   AHandler generateVirtual(Class<?> var1, @Nullable Object var2, EventHandler var3, Method var4);
}
