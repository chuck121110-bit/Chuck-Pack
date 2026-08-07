package xaeroplus.module.impl;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.utils.interfaces.IGoalRenderPos;
import java.lang.ref.WeakReference;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_5321;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.misc.OptimizedMath;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.feature.extensions.SyncedWaypoint;
import xaeroplus.module.Module;
import xaeroplus.util.BaritoneGoalHelper;
import xaeroplus.util.BaritoneHelper;
import xaeroplus.util.ChunkUtils;

public class BaritoneGoalSync extends Module {
   private static final WeakReference nullRef = new WeakReference((Object)null);
   private WeakReference<Waypoint> baritoneWpRef;
   private WeakReference<WaypointSet> baritoneWpSetRef;
   private WeakReference<MinimapWorld> baritoneWpMinimapWorldRef;
   private WeakReference<class_2338> baritoneGoalPos;
   private class_5321<class_1937> baritoneWpDimension;

   public BaritoneGoalSync() {
      this.baritoneWpRef = nullRef;
      this.baritoneWpSetRef = nullRef;
      this.baritoneWpMinimapWorldRef = nullRef;
      this.baritoneGoalPos = nullRef;
      this.baritoneWpDimension = class_1937.field_25179;
   }

   @EventHandler
   public void syncGoal(final ClientTickEvent.Post event) {
      if (BaritoneHelper.isBaritonePresent()) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
            if (currentWorld != null) {
               WaypointSet currentWpSet = currentWorld.getCurrentWaypointSet();
               if (currentWpSet != null) {
                  try {
                     Goal goal = BaritoneGoalHelper.getBaritoneGoal();
                     if (goal == null) {
                        this.clearBaritoneWpAndState();
                        return;
                     }

                     class_2338 baritoneGoalBlockPos = this.getBaritoneGoalBlockPos(goal);
                     if (baritoneGoalBlockPos == null) {
                        this.clearBaritoneWpAndState();
                        return;
                     }

                     if (this.baritoneGoalPos.get() == null) {
                        this.baritoneGoalPos = new WeakReference(baritoneGoalBlockPos);
                     }

                     if (this.baritoneGoalPos.get() != null && !((class_2338)this.baritoneGoalPos.get()).equals(baritoneGoalBlockPos)) {
                        this.clearBaritoneWpAndState();
                        this.baritoneGoalPos = new WeakReference(baritoneGoalBlockPos);
                     }

                     if (this.baritoneWpMinimapWorldRef.get() == null || currentWorld != this.baritoneWpMinimapWorldRef.get() || this.baritoneWpSetRef.get() == null || this.baritoneWpSetRef.get() != currentWpSet) {
                        if (this.baritoneWpSetRef.get() != null && this.baritoneWpRef.get() != null) {
                           ((WaypointSet)this.baritoneWpSetRef.get()).remove((Waypoint)this.baritoneWpRef.get());
                        }

                        this.baritoneWpRef = nullRef;
                        this.initBaritoneWpWorld(currentWorld);
                     }

                     double customDimDiv = this.getBaritoneWpDimDiv();
                     int x = (int)((double)OptimizedMath.myFloor((double)baritoneGoalBlockPos.method_10263()) * customDimDiv);
                     int z = (int)((double)OptimizedMath.myFloor((double)baritoneGoalBlockPos.method_10260()) * customDimDiv);
                     Waypoint baritoneWp = (Waypoint)this.baritoneWpRef.get();
                     if (baritoneWp != null) {
                        if (baritoneWp.getX() != x || baritoneWp.getZ() != z) {
                           baritoneWp.setX(x);
                           baritoneWp.setY(baritoneGoalBlockPos.method_10264());
                           baritoneWp.setZ(z);
                           SupportMods.xaeroMinimap.requestWaypointsRefresh();
                        }
                     } else {
                        baritoneWp = SyncedWaypoint.create(x, baritoneGoalBlockPos.method_10264(), z, "Baritone Goal", "B", WaypointColor.GREEN);
                        this.baritoneWpRef = new WeakReference(baritoneWp);
                        ((WaypointSet)this.baritoneWpSetRef.get()).add(baritoneWp);
                        SupportMods.xaeroMinimap.requestWaypointsRefresh();
                     }
                  } catch (Exception e) {
                     XaeroPlus.LOGGER.error("Error in Baritone goal sync", e);
                  }

               }
            }
         }
      }
   }

   private double getBaritoneWpDimDiv() {
      if (this.baritoneWpMinimapWorldRef.get() == null) {
         return (double)1.0F;
      } else {
         class_5321<class_1937> baritoneWpMinimapWorldDimId = ((MinimapWorld)this.baritoneWpMinimapWorldRef.get()).getDimId();
         double customDimDiv = (double)1.0F;
         if (baritoneWpMinimapWorldDimId != this.baritoneWpDimension) {
            if (baritoneWpMinimapWorldDimId == class_1937.field_25180 && this.baritoneWpDimension == class_1937.field_25179) {
               customDimDiv = (double)0.125F;
            } else if (baritoneWpMinimapWorldDimId == class_1937.field_25179 && this.baritoneWpDimension == class_1937.field_25180) {
               customDimDiv = (double)8.0F;
            }
         }

         return customDimDiv;
      }
   }

   private void initBaritoneWpWorld(final MinimapWorld currentWorld) {
      this.baritoneWpMinimapWorldRef = new WeakReference(currentWorld);
      WaypointSet waypointSet = currentWorld.getCurrentWaypointSet();
      this.baritoneWpSetRef = new WeakReference(waypointSet);
   }

   private void clearBaritoneWpAndState() {
      if (this.baritoneWpRef.get() != null) {
         ((WaypointSet)this.baritoneWpSetRef.get()).remove((Waypoint)this.baritoneWpRef.get());
      }

      this.baritoneWpRef = nullRef;
      this.baritoneWpSetRef = nullRef;
      this.baritoneWpMinimapWorldRef = nullRef;
      this.baritoneGoalPos = nullRef;
      this.baritoneWpDimension = ChunkUtils.getActualDimension();
   }

   private class_2338 getBaritoneGoalBlockPos(Goal goal) {
      if (goal instanceof GoalXZ) {
         return new class_2338(((GoalXZ)goal).getX(), 64, ((GoalXZ)goal).getZ());
      } else {
         return goal instanceof IGoalRenderPos ? ((IGoalRenderPos)goal).getGoalPos() : null;
      }
   }
}
