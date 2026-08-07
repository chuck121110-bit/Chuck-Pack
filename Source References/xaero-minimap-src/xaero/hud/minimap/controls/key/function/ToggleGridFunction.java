package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.profile.ConfigProfile;

public class ToggleGridFunction extends KeyMappingFunction {
   protected ToggleGridFunction() {
      super(false);
   }

   public void onPress() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      ConfigProfile currentProfile = configManager.getCurrentProfile();
      currentProfile.set(MinimapProfiledConfigOptions.CHUNK_GRID, -(Integer)currentProfile.get(MinimapProfiledConfigOptions.CHUNK_GRID) - 1);
      HudMod.INSTANCE.getHudConfigs().getClientConfigProfileIO().save(currentProfile);
   }

   public void onRelease() {
   }
}
