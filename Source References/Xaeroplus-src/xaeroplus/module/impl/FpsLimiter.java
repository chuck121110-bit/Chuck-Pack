package xaeroplus.module.impl;

import net.lenni0451.lambdaevents.EventHandler;
import xaeroplus.event.MinimapRenderEvent;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;

public class FpsLimiter extends Module {
   private long nextRenderCapture = System.currentTimeMillis();

   @EventHandler
   public void onMinimapRenderEvent(MinimapRenderEvent event) {
      if (System.currentTimeMillis() < this.nextRenderCapture) {
         event.cancelled = true;
      } else {
         this.nextRenderCapture = System.currentTimeMillis() + (long)(1000 / Settings.REGISTRY.minimapFpsLimit.getAsInt());
      }
   }
}
