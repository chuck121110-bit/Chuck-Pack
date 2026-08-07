package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;

public class ToggledEnlargeMapFunction extends KeyMappingFunction {
   protected ToggledEnlargeMapFunction() {
      super(false);
   }

   public void onPress() {
      if ((Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.TOGGLED_ENLARGED)) {
         MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
         session.getProcessor().setEnlargedMap(!session.getProcessor().isEnlargedMap());
         session.getProcessor().setToResetImage(true);
         session.getProcessor().instantZoom();
      }
   }

   public void onRelease() {
   }
}
