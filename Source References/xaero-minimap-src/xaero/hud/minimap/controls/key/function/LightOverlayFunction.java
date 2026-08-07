package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.profile.ConfigProfile;

public class LightOverlayFunction extends KeyMappingFunction {
   protected LightOverlayFunction() {
      super(false);
   }

   public void onPress() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      ConfigProfile currentProfile = configManager.getCurrentProfile();
      int lightOverlayTypeConfig = (Integer)currentProfile.get(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE);
      if (lightOverlayTypeConfig == 0) {
         currentProfile.set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE, 1);
      } else {
         currentProfile.set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE, -lightOverlayTypeConfig);
      }

      HudMod.INSTANCE.getHudConfigs().getClientConfigProfileIO().save(currentProfile);
   }

   public void onRelease() {
   }
}
