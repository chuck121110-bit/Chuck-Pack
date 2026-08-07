package xaero.hud.minimap.controls.key.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1074;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.common.util.KeySortableByOther;

public class SwitchWaypointSetFunction extends KeyMappingFunction {
   protected SwitchWaypointSetFunction() {
      super(false);
   }

   public void onPress() {
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      MinimapWorld currentWorld = session.getWorldManager().getCurrentWorld();
      if (currentWorld != null) {
         List<KeySortableByOther<String>> keysList = new ArrayList();

         for(WaypointSet set : currentWorld.getIterableWaypointSets()) {
            String key = set.getName();
            keysList.add(new KeySortableByOther(key, new Comparable[]{class_1074.method_4662(key, new Object[0]).toLowerCase()}));
         }

         Collections.sort(keysList);
         boolean foundCurrent = false;
         String firstSetKey = null;

         for(KeySortableByOther<String> sortedSet : keysList) {
            String setKey = (String)sortedSet.getKey();
            if (firstSetKey == null) {
               firstSetKey = setKey;
            }

            if (setKey != null && setKey.equals(currentWorld.getCurrentWaypointSetId())) {
               foundCurrent = true;
            } else if (foundCurrent) {
               foundCurrent = false;
               currentWorld.setCurrentWaypointSetId(setKey);
               break;
            }
         }

         if (foundCurrent) {
            currentWorld.setCurrentWaypointSetId(firstSetKey);
         }

         session.getWorldStateUpdater().update();
         session.getWaypointSession().setSetChangedTime(System.currentTimeMillis());

         try {
            session.getWorldManagerIO().saveWorld(currentWorld);
         } catch (IOException e) {
            MinimapLogs.LOGGER.error("suppressed exception", e);
         }

      }
   }

   public void onRelease() {
   }
}
