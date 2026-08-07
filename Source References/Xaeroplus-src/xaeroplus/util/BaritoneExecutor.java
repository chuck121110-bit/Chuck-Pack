package xaeroplus.util;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.Globals;

public final class BaritoneExecutor {
   private BaritoneExecutor() {
   }

   public static GoalXZ getBaritoneGoalXZ(int x, int z) {
      class_5321<class_1937> customDim = Globals.getCurrentDimensionId();
      class_5321<class_1937> actualDim = ChunkUtils.getActualDimension();
      double customDimDiv = (double)1.0F;
      if (customDim != actualDim) {
         if (customDim == class_1937.field_25180 && actualDim == class_1937.field_25179) {
            customDimDiv = (double)8.0F;
         } else if (customDim == class_1937.field_25179 && actualDim == class_1937.field_25180) {
            customDimDiv = (double)0.125F;
         }
      }

      int goalX = (int)((double)x * customDimDiv);
      int goalZ = (int)((double)z * customDimDiv);
      return new GoalXZ(goalX, goalZ);
   }

   public static GoalBlock getBaritoneGoalBlock(int x, int y, int z) {
      class_5321<class_1937> customDim = Globals.getCurrentDimensionId();
      class_5321<class_1937> actualDim = ChunkUtils.getActualDimension();
      double customDimDiv = (double)1.0F;
      if (customDim != actualDim) {
         if (customDim == class_1937.field_25180 && actualDim == class_1937.field_25179) {
            customDimDiv = (double)8.0F;
         } else if (customDim == class_1937.field_25179 && actualDim == class_1937.field_25180) {
            customDimDiv = (double)0.125F;
         }
      }

      int goalX = (int)((double)x * customDimDiv);
      int goalZ = (int)((double)z * customDimDiv);
      return new GoalBlock(goalX, y, goalZ);
   }

   public static void goal(int x, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(getBaritoneGoalXZ(x, z));
      }
   }

   public static void goal(int x, int y, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(getBaritoneGoalBlock(x, y, z));
      }
   }

   public static void path(int x, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(getBaritoneGoalXZ(x, z));
      }
   }

   public static void path(int x, int y, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(getBaritoneGoalBlock(x, y, z));
      }
   }

   public static void elytra(int x, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         if (BaritoneHelper.isBaritoneElytraPresent()) {
            BaritoneAPI.getSettings().elytraTermsAccepted.value = true;
            GoalXZ goal = getBaritoneGoalXZ(x, z);
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(goal);
            BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().pathTo(goal);
         }
      }
   }

   public static void elytra(int x, int y, int z) {
      if (BaritoneHelper.isBaritonePresent()) {
         if (BaritoneHelper.isBaritoneElytraPresent()) {
            BaritoneAPI.getSettings().elytraTermsAccepted.value = true;
            GoalBlock goal = getBaritoneGoalBlock(x, y, z);
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(goal);
            BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().pathTo(goal);
         }
      }
   }
}
