package xaero.common;

import net.minecraft.class_2378;
import net.minecraft.class_7923;
import xaero.common.effect.EffectsRegister;
import xaero.common.events.CommonEventsFabric;
import xaero.hud.minimap.common.config.primary.option.MinimapPrimaryCommonConfigOptions;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;

public class PlatformContextLoaderCommonFabric extends PlatformContextLoaderCommon {
   public void setup(IXaeroMinimap modMain) {
      ((CommonEventsFabric)modMain.getCommonEvents()).register();
      modMain.getSupportServerMods().registerEvents();
      SingleConfigManager<Config> primaryConfig = HudMod.INSTANCE.getHudConfigs().getPrimaryCommonConfigManager();
      if ((Boolean)primaryConfig.getEffective(MinimapPrimaryCommonConfigOptions.REGISTER_EFFECTS)) {
         (new EffectsRegister()).registerEffects((effect) -> class_2378.method_47985(class_7923.field_41174, effect.getRegistryName(), effect));
      }

   }
}
