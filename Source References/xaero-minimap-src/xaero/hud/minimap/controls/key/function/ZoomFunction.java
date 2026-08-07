package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.channel.ConfigChannel;

public class ZoomFunction extends KeyMappingFunction {
   private final boolean in;

   protected ZoomFunction(boolean in) {
      super(false);
      this.in = in;
   }

   public void onPress() {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         ConfigChannel channel = HudMod.INSTANCE.getHudConfigs();
         ClientConfigManager configManager = channel.getClientConfigManager();
         if (!minimapSession.getProcessor().isEnlargedMap() || (Integer)configManager.getEffective(MinimapProfiledConfigOptions.ZOOM_ENLARGED) == 0) {
            int zoomChange = this.in ? 1 : -1;
            MinimapConfigClientUtils.changeZoom(zoomChange);
            channel.getClientConfigProfileIO().save(configManager.getCurrentProfile());
         }
      }
   }

   public void onRelease() {
   }
}
