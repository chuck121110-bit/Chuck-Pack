package xaero.hud.minimap.controls.key.function;

import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;

public class ManualCaveModeFunction extends KeyMappingFunction {
   protected ManualCaveModeFunction() {
      super(false);
   }

   public void onPress() {
      ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getProcessor().toggleManualCaveMode();
   }

   public void onRelease() {
   }
}
