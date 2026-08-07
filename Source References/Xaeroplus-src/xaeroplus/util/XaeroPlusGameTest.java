package xaeroplus.util;

import org.spongepowered.asm.mixin.MixinEnvironment;

public class XaeroPlusGameTest {
   public static void applyMixinsTest() {
      MixinEnvironment.getCurrentEnvironment().audit();
   }
}
