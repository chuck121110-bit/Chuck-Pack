package xaeroplus.util;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.calc.IPath;
import baritone.api.process.IElytraProcess;
import baritone.process.ElytraProcess;
import baritone.process.elytra.ElytraBehavior;
import baritone.process.elytra.NetherPath;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_2338;
import xaeroplus.XaeroPlus;

public class BaritonePathHelper {
   public static List<class_2338> getBaritonePath() {
      if (BaritoneHelper.isBaritoneElytraPresent() && BaritoneHelper.isBaritoneDeobf() && BaritoneHelper.isElytraPathAccessible()) {
         IElytraProcess iElytraProcess = BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess();
         class_2338 elytraGoalPos = iElytraProcess.currentDestination();
         if (elytraGoalPos != null) {
            return getElytraPath();
         }
      }

      return (List)BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getPath().map(IPath::positions).map((bpsList) -> bpsList).orElse(Collections.emptyList());
   }

   static List<class_2338> getElytraPath() {
      try {
         ElytraProcess elytraProcess = (ElytraProcess)BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess();
         Field behaviorField = elytraProcess.getClass().getDeclaredField("behavior");
         behaviorField.setAccessible(true);
         ElytraBehavior elytraBehavior = (ElytraBehavior)behaviorField.get(elytraProcess);
         if (elytraBehavior == null) {
            return Collections.emptyList();
         } else {
            ElytraBehavior.PathManager pathManager = elytraBehavior.pathManager;
            if (pathManager == null) {
               return Collections.emptyList();
            } else {
               NetherPath path = pathManager.getPath();
               return (List<class_2338>)(path == null ? Collections.emptyList() : path);
            }
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed getting elytra path", e);
         return Collections.emptyList();
      }
   }
}
