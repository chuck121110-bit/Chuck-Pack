package xaeroplus.util;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.class_2338;

public class BaritoneGoalHelper {
   public static Goal getBaritoneGoal() {
      if (BaritoneHelper.isBaritoneElytraPresent()) {
         class_2338 elytraGoalPos = BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination();
         if (elytraGoalPos != null) {
            return new GoalXZ(elytraGoalPos.method_10263(), elytraGoalPos.method_10260());
         }
      }

      return BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal();
   }
}
