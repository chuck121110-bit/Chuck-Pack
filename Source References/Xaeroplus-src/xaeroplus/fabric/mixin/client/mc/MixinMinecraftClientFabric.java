package xaeroplus.fabric.mixin.client.mc;

import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaeroplus.fabric.util.compat.IncompatibleMinimapWarningScreen;

@Mixin({class_310.class})
public class MixinMinecraftClientFabric {
   @Shadow
   public class_437 field_1755;

   @Inject(
      method = {"method_1507"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void preventMinimapIncompatibleScreenFromClosing(final class_437 guiScreen, final CallbackInfo ci) {
      if (this.field_1755 instanceof IncompatibleMinimapWarningScreen) {
         ci.cancel();
      }

   }
}
