package xaero.hud.minimap.controls.key.function;

import net.minecraft.class_310;
import xaero.common.HudMod;
import xaero.common.effect.Effects;
import xaero.common.misc.Misc;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;

public class ToggleMapFunction extends KeyMappingFunction {
   protected ToggleMapFunction() {
      super(false);
   }

   public void onPress() {
      class_310 mc = class_310.method_1551();
      if (!Misc.hasEffect(mc.field_1724, Effects.NO_MINIMAP) && !Misc.hasEffect(mc.field_1724, Effects.NO_MINIMAP_HARMFUL)) {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         boolean currentDisplayMinimap = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.DISPLAY_MINIMAP);
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_MINIMAP, !currentDisplayMinimap);
         HudMod.INSTANCE.getHudConfigs().getClientConfigProfileIO().save(configManager.getCurrentProfile());
      }
   }

   public void onRelease() {
   }
}
