package xaero.hud.minimap.waypoint;

import net.minecraft.class_1297;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.render.WaypointDeleter;
import xaero.hud.minimap.world.MinimapWorld;

public class DestinationHandler {
   private final MinimapSession session;
   private final WaypointDeleter waypointReachDeleter;
   private class_1297 renderEntity;
   private MinimapWorld world;
   private boolean allSets;
   private boolean deathpoints;
   private double dimDiv;

   public DestinationHandler(MinimapSession session, WaypointDeleter waypointReachDeleter) {
      this.session = session;
      this.waypointReachDeleter = waypointReachDeleter;
   }

   public void begin(class_1297 renderEntity, MinimapWorld world, boolean allSets, boolean deathpoints) {
      this.waypointReachDeleter.begin();
      this.renderEntity = renderEntity;
      this.world = world;
      this.allSets = allSets;
      this.deathpoints = deathpoints;
      this.dimDiv = this.session.getDimensionHelper().getDimensionDivision(world);
   }

   public void handle(Waypoint waypoint) {
      if (waypoint.isDestination()) {
         if (this.deathpoints || !waypoint.getPurpose().isDeath()) {
            if (System.currentTimeMillis() - waypoint.getCreatedAt() > 5000L) {
               double correctOffX = this.renderEntity.method_23317() - (double)waypoint.getX(this.dimDiv);
               double correctOffY = this.renderEntity.method_23318() - (double)waypoint.getY();
               if (!waypoint.isYIncluded()) {
                  correctOffY = (double)0.0F;
               }

               double correctOffZ = this.renderEntity.method_23321() - (double)waypoint.getZ(this.dimDiv);
               double correctDistance = Math.sqrt(correctOffX * correctOffX + correctOffY * correctOffY + correctOffZ * correctOffZ);
               if (correctDistance < (double)4.0F) {
                  this.waypointReachDeleter.add(waypoint);
               }

            }
         }
      }
   }

   public void end() {
      this.waypointReachDeleter.deleteCollected(this.session, this.world, this.allSets);
   }
}
