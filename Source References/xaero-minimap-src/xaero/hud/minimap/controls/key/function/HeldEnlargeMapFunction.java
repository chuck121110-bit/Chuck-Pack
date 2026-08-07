package xaero.hud.minimap.controls.key.function;

import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;

public class HeldEnlargeMapFunction extends KeyMappingFunction {
   private boolean active;

   protected HeldEnlargeMapFunction() {
      super(true);
   }

   public void onPress() {
      if (!(Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.TOGGLED_ENLARGED)) {
         if (!this.active) {
            this.active = true;
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            session.getProcessor().setEnlargedMap(true);
            session.getProcessor().setToResetImage(true);
            session.getProcessor().instantZoom();
         }
      }
   }

   public void onRelease() {
      this.active = false;
      if (!(Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.TOGGLED_ENLARGED)) {
         MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
         session.getProcessor().setEnlargedMap(false);
         session.getProcessor().setToResetImage(true);
         session.getProcessor().instantZoom();
      }
   }
}
