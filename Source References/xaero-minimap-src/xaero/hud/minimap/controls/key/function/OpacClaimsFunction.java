package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.profile.ConfigProfile;

public class OpacClaimsFunction extends KeyMappingFunction {
   protected OpacClaimsFunction() {
      super(false);
   }

   public void onPress() {
      HudMod modMain = HudMod.INSTANCE;
      if (modMain.getSupportMods().worldmap() && modMain.getSupportMods().shouldUseWorldMapChunks()) {
         modMain.getSupportMods().worldmapSupport.toggleChunkClaims();
      } else {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         ConfigProfile currentProfile = configManager.getCurrentProfile();
         currentProfile.set(MinimapProfiledConfigOptions.OPAC_CLAIMS, !(Boolean)currentProfile.get(MinimapProfiledConfigOptions.OPAC_CLAIMS));
         HudMod.INSTANCE.getHudConfigs().getClientConfigProfileIO().save(currentProfile);
      }
   }

   public void onRelease() {
   }
}
