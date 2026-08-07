package xaero.hud.minimap.controls.key.function;

import net.minecraft.class_310;
import xaero.common.effect.Effects;
import xaero.common.misc.Misc;
import xaero.common.misc.OptimizedMath;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;

public class TemporaryWaypointFunction extends KeyMappingFunction {
   protected TemporaryWaypointFunction() {
      super(false);
   }

   public void onPress() {
      class_310 mc = class_310.method_1551();
      if (!Misc.hasEffect(mc.field_1724, Effects.NO_WAYPOINTS) && !Misc.hasEffect(mc.field_1724, Effects.NO_WAYPOINTS_HARMFUL)) {
         MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
         session.getWaypointSession().getTemporaryHandler().createTemporaryWaypoint(session.getWorldManager().getCurrentWorld(), OptimizedMath.myFloor(mc.method_1560().method_23317()), OptimizedMath.myFloor(mc.method_1560().method_23318() + (double)0.0625F), OptimizedMath.myFloor(mc.method_1560().method_23321()));
      }
   }

   public void onRelease() {
   }
}
