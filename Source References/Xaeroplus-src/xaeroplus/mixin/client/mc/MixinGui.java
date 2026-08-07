package xaeroplus.mixin.client.mc;

import net.minecraft.class_329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaeroplus.settings.Settings;
import xaeroplus.util.GuiMapHelper;

@Mixin({class_329.class})
public class MixinGui {
   @Inject(
      method = {"method_1753"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void cancelGuiRenderWhileInTransparentWorldMap(final CallbackInfo ci) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get() && GuiMapHelper.isGuiMapLoaded()) {
         ci.cancel();
      }

   }
}
