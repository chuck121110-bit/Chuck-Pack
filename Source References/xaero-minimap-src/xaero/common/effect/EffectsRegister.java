package xaero.common.effect;

import java.util.function.Function;
import net.minecraft.class_1291;
import net.minecraft.class_6880;

public class EffectsRegister {
   public void registerEffects(Function<MinimapStatusEffect, class_6880<class_1291>> registrar) {
      Effects.init();
      Effects.NO_MINIMAP = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_MINIMAP_UNHELD);
      Effects.NO_MINIMAP_HARMFUL = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_MINIMAP_HARMFUL_UNHELD);
      Effects.NO_RADAR = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_RADAR_UNHELD);
      Effects.NO_RADAR_HARMFUL = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_RADAR_HARMFUL_UNHELD);
      Effects.NO_WAYPOINTS = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_WAYPOINTS_UNHELD);
      Effects.NO_WAYPOINTS_HARMFUL = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_WAYPOINTS_HARMFUL_UNHELD);
      Effects.NO_CAVE_MAPS = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_CAVE_MAPS_UNHELD);
      Effects.NO_CAVE_MAPS_HARMFUL = (class_6880)registrar.apply((MinimapStatusEffect)Effects.NO_CAVE_MAPS_HARMFUL_UNHELD);
   }
}
