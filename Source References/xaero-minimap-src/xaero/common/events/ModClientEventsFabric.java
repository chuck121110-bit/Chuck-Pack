package xaero.common.events;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_2960;
import xaero.common.IXaeroMinimap;

public class ModClientEventsFabric extends ModClientEvents {
   public ModClientEventsFabric(IXaeroMinimap modMain) {
      super(modMain);
   }

   public void register() {
      HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, class_2960.method_60655("xaerohud", "hud"), this::handleRenderModOverlay);
   }
}
