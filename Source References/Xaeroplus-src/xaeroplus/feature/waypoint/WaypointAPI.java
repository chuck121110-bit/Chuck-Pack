package xaeroplus.feature.waypoint;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.path.XaeroPath;
import xaeroplus.XaeroPlus;
import xaeroplus.mixin.client.AccessorWaypointSet;

public class WaypointAPI {
   public static MinimapWorld getMinimapWorld(class_5321<class_1937> dim) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession == null) {
         return null;
      } else {
         MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
         if (currentWorld == null) {
            return null;
         } else if (currentWorld.getDimId() == dim) {
            return currentWorld;
         } else {
            MinimapWorldRootContainer rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();

            for(MinimapWorld world : rootContainer.getWorlds()) {
               if (world.getDimId() == dim) {
                  return world;
               }
            }

            String dimensionDirectoryName = minimapSession.getDimensionHelper().getDimensionDirectoryName(dim);
            String worldNode = minimapSession.getWorldStateUpdater().getPotentialWorldNode(dim, true);
            XaeroPath containerPath = minimapSession.getWorldState().getAutoRootContainerPath().resolve(dimensionDirectoryName).resolve(worldNode);
            return minimapSession.getWorldManager().getWorld(containerPath);
         }
      }
   }

   public static WaypointSet getOrCreateWaypointSetInWorld(MinimapWorld minimapWorld, String setName) {
      WaypointSet waypointSet = minimapWorld.getWaypointSet(setName);
      if (waypointSet == null) {
         minimapWorld.addWaypointSet(setName);
         waypointSet = minimapWorld.getWaypointSet(setName);
      }

      return waypointSet;
   }

   public static void forEachWaypointSetInCurrentContainer(Consumer<List<Waypoint>> consumer) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         MinimapWorldRootContainer rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();

         for(MinimapWorld world : rootContainer.getWorlds()) {
            for(WaypointSet set : world.getIterableWaypointSets()) {
               consumer.accept(((AccessorWaypointSet)set).getList());
            }
         }

         for(MinimapWorldContainer subContainer : rootContainer.getSubContainers()) {
            for(MinimapWorld world : subContainer.getWorlds()) {
               for(WaypointSet set : world.getIterableWaypointSets()) {
                  consumer.accept(((AccessorWaypointSet)set).getList());
               }
            }
         }

      }
   }

   public static void forEachMinimapWorld(Consumer<MinimapWorld> consumer) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         MinimapWorldRootContainer rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();

         for(MinimapWorld world : rootContainer.getWorlds()) {
            consumer.accept(world);
         }

         for(MinimapWorldContainer subContainer : rootContainer.getSubContainers()) {
            for(MinimapWorld world : subContainer.getWorlds()) {
               consumer.accept(world);
            }
         }

      }
   }

   public static void forEachWaypointSetInAllMinimapWorlds(Consumer<WaypointSet> consumer) {
      forEachMinimapWorld((world) -> {
         for(WaypointSet set : world.getIterableWaypointSets()) {
            consumer.accept(set);
         }

      });
   }

   public static WaypointSet getCurrentWaypointSet() {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession == null) {
         return null;
      } else {
         MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
         return currentWorld == null ? null : currentWorld.getCurrentWaypointSet();
      }
   }

   public static void switchWaypointDimension(final class_5321<class_1937> dimension) {
      if (dimension != null) {
         try {
            MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
            if (minimapSession == null) {
               return;
            }

            MinimapWorldManager worldManager = minimapSession.getWorldManager();
            if (worldManager == null) {
               return;
            }

            MinimapWorld currentWorld = worldManager.getCurrentWorld();
            if (currentWorld == null) {
               return;
            }

            if (currentWorld.getDimId() == dimension) {
               return;
            }

            MinimapWorld minimapWorld = getMinimapWorld(dimension);
            if (minimapWorld == null) {
               return;
            }

            if (minimapWorld.getFullPath() == null) {
               return;
            }

            MinimapWorld autoWorld = worldManager.getAutoWorld();
            if (autoWorld != null && autoWorld == minimapWorld) {
               minimapSession.getWorldState().setCustomWorldPath((XaeroPath)null);
            } else {
               minimapSession.getWorldState().setCustomWorldPath(minimapWorld.getFullPath());
            }
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Failed switching waypoint dimension: {}", dimension, e);
         }

      }
   }

   public static ThirdPartyWaypointManager getThirdPartyWaypointManager(final class_5321<class_1937> dimension) {
      MinimapWorld minimapWorld = getMinimapWorld(dimension);
      return minimapWorld == null ? null : minimapWorld.getContainer().getThirdPartyWaypointManager();
   }

   public static ThirdPartyWaypoints getThirdPartyWaypoints(final class_5321<class_1937> dimension, class_2960 origin) {
      ThirdPartyWaypointManager manager = getThirdPartyWaypointManager(dimension);
      return manager == null ? null : manager.get(origin);
   }
}
