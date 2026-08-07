package xaero.minimap;

import xaero.common.HudClientOnlyBase;
import xaero.common.HudMod;
import xaero.hud.xminimap.preset.BuiltInHudPresets;

public class MinimapClientOnly extends HudClientOnlyBase {
   public void preInit(String modId, HudMod modMain) {
      super.preInit(modId, modMain);
   }

   public void preLoadLater(HudMod modMain) {
      super.preLoadLater(modMain);
      BuiltInHudPresets.addAll(modMain.getHud().getPresetManager());
      BuiltInHudPresets.TOP_LEFT.apply();
   }
}
