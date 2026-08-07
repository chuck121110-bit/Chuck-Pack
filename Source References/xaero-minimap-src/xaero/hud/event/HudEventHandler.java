package xaero.hud.event;

import xaero.common.XaeroMinimapSession;
import xaero.hud.Hud;
import xaero.hud.module.HudModule;

public class HudEventHandler {
   private Hud hud;

   public void setHud(Hud hud) {
      if (this.hud != null) {
         throw new IllegalStateException();
      } else {
         this.hud = hud;
      }
   }

   public void handleRenderGameOverlayEventPost() {
      if (XaeroMinimapSession.getCurrentSession() != null) {
         for(HudModule<?> module : this.hud.getModuleManager().getModules()) {
            module.getCurrentSession().onPostGameOverlay();
         }

      }
   }
}
