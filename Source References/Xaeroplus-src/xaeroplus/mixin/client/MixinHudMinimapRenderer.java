package xaeroplus.mixin.client;

import net.minecraft.class_310;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.hud.minimap.module.MinimapRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaeroplus.feature.extensions.DrawOrderScreen;

@Mixin(
   value = {MinimapRenderer.class},
   remap = false
)
public class MixinHudMinimapRenderer {
   @Redirect(
      method = {"render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/class_332;F)V"},
      at = @At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/module/MinimapSession;getHideMinimapUnderScreen()Z"
),
      remap = true
   )
   public boolean allowMinimapRenderOnTopOf(final MinimapSession instance) {
      boolean original = instance.getHideMinimapUnderScreen();
      if (!original) {
         return original;
      } else {
         class_437 screen = class_310.method_1551().field_1755;
         return screen instanceof DrawOrderScreen ? false : original;
      }
   }
}
