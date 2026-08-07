package xaero.common.gui.component;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.class_2477;
import net.minecraft.class_2561;

public class CachedComponentSupplier {
   private final Function<Object[], class_2561> factory;
   private Object[] registeredArgs;
   private class_2561 cachedComponent;
   private WeakReference<class_2477> registeredLanguage;

   public CachedComponentSupplier(Function<Object[], class_2561> factory) {
      this.factory = factory;
   }

   public class_2561 get(Object... args) {
      if (this.cachedComponent == null || !Arrays.equals(this.registeredArgs, args) || this.registeredLanguage.get() != class_2477.method_10517()) {
         this.registeredLanguage = new WeakReference(class_2477.method_10517());
         this.cachedComponent = (class_2561)this.factory.apply(args);
         this.registeredArgs = args;
      }

      return this.cachedComponent;
   }
}
