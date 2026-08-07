package xaero.common.events;

import net.minecraft.class_332;
import xaero.common.HudMod;

public class ClientEventsFabric extends ClientEvents {
   public ClientEventsFabric(HudMod modMain) {
      super(modMain);
   }

   public void handleRenderGameOverlayEventPre(class_332 guiGraphics, float partialTicks) {
      super.handleRenderGameOverlayEventPre(guiGraphics, partialTicks);
   }

   public void handleRenderGameOverlayEventPost() {
      super.handleRenderGameOverlayEventPost();
   }

   protected void handleTextureStitchEventPost_onReset() {
      super.handleTextureStitchEventPost_onReset();
      this.modMain.getMinimap().getMinimapFBORenderer().resetEntityIconsResources();
   }
}
