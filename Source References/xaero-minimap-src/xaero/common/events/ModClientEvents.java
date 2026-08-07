package xaero.common.events;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import xaero.common.IXaeroMinimap;
import xaero.common.anim.MultiplyAnimationHelper;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

public abstract class ModClientEvents {
   protected IXaeroMinimap modMain;

   public ModClientEvents(IXaeroMinimap modMain) {
      this.modMain = modMain;
   }

   public void handleRenderModOverlay(class_332 guiGraphics, class_9779 deltaTracker) {
      MultiplyAnimationHelper.tick();
      if (!class_310.method_1551().field_1690.field_1842) {
         ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            this.modMain.getHudRenderer().render(this.modMain.getHud(), guiGraphics, deltaTracker.method_60637(true));
            this.modMain.getMinimap().getWaypointMapRenderer().drawSetChange(minimapSession, guiGraphics, class_310.method_1551().method_22683());
         }

      }
   }
}
