package xaero.hud.minimap.waypoint.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

public class WaypointIO {
   private final WaypointOldIO oldIO;

   public WaypointIO(HudMod modMain) {
      this.oldIO = new WaypointOldIO(modMain.getWaypointsFile());
   }

   public WaypointOldIO getOldIO() {
      return this.oldIO;
   }

   public boolean checkLine(String[] args, MinimapWorld world) {
      if (args[0].equalsIgnoreCase("sets")) {
         world.setCurrentWaypointSetId(args[1]);

         for(int i = 1; i < args.length; ++i) {
            if (world.getWaypointSet(args[i]) == null) {
               world.addWaypointSet(WaypointSet.Builder.begin().setName(args[i]).build());
            }
         }

         return true;
      } else if (args[0].equalsIgnoreCase("waypoint")) {
         String setName = args[9];
         WaypointSet waypoints = world.getWaypointSet(setName);
         if (waypoints == null) {
            world.addWaypointSet(waypoints = WaypointSet.Builder.begin().setName(setName).build());
         }

         boolean yIncluded = !args[4].equals("~");
         int yCoord = 0;
         if (yIncluded) {
            yCoord = Integer.parseInt(args[4]);
         }

         Waypoint loadWaypoint = new Waypoint(Integer.parseInt(args[3]), yCoord, Integer.parseInt(args[5]), Waypoint.getStringFromStringSafe(args[1], "§§"), Waypoint.getStringFromStringSafe(args[2], "§§"), WaypointColor.fromIndex(Integer.parseInt(args[6])), WaypointPurpose.NORMAL, false, yIncluded);
         loadWaypoint.setDisabled(args[7].equals("true"));
         loadWaypoint.setType(Integer.parseInt(args[8]));
         if (args.length > 10) {
            loadWaypoint.setRotation(args[10].equals("true"));
         }

         if (args.length > 11) {
            loadWaypoint.setYaw(Integer.parseInt(args[11]));
         }

         if (args.length > 12) {
            String visibilityTypeString = args[12];
            WaypointVisibilityType visibilityType = WaypointVisibilityType.LOCAL;
            if (visibilityTypeString.equals("true")) {
               visibilityType = WaypointVisibilityType.GLOBAL;
            } else if (!visibilityTypeString.equals("false")) {
               try {
                  visibilityType = WaypointVisibilityType.valueOf(visibilityTypeString);
               } catch (IllegalArgumentException var13) {
                  try {
                     int visibilityIndex = Integer.parseInt(visibilityTypeString);
                     visibilityType = WaypointVisibilityType.values()[visibilityIndex];
                  } catch (Exception var12) {
                  }
               }
            }

            loadWaypoint.setVisibility(visibilityType);
         }

         if (args.length > 13) {
            String destinationString = args[13];
            if (destinationString.equals("true")) {
               loadWaypoint.setOneoffDestination(true);
            }
         }

         waypoints.add(loadWaypoint);
         return true;
      } else {
         return false;
      }
   }

   public void saveWaypoints(MinimapWorld world, OutputStreamWriter output) throws IOException {
      if (world.getSetCount() > 1) {
         output.write("sets:" + world.getCurrentWaypointSetId());

         for(WaypointSet set : world.getIterableWaypointSets()) {
            if (!set.getName().equals(world.getCurrentWaypointSetId())) {
               output.write(":" + set.getName());
            }
         }

         output.write("\n");
      }

      output.write("#\n");
      output.write("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination\n");
      output.write("#\n");

      for(WaypointSet set : world.getIterableWaypointSets()) {
         for(Waypoint w : set.getWaypoints()) {
            if (!w.isTemporary()) {
               output.write("waypoint:");
               output.write(w.getNameSafe("§§"));
               output.write(":");
               output.write(w.getInitialsSafe("§§"));
               output.write(":");
               output.write(String.valueOf(w.getX()));
               output.write(":");
               output.write(w.isYIncluded() ? String.valueOf(w.getY()) : "~");
               output.write(":");
               output.write(String.valueOf(w.getZ()));
               output.write(":");
               output.write(String.valueOf(w.getWaypointColor().ordinal()));
               output.write(":");
               output.write(String.valueOf(w.isDisabled()));
               output.write(":");
               output.write(String.valueOf(w.getWaypointType()));
               output.write(":");
               output.write(set.getName());
               output.write(":");
               output.write(String.valueOf(w.isRotation()));
               output.write(":");
               output.write(String.valueOf(w.getYaw()));
               output.write(":");
               output.write(String.valueOf(w.getVisibilityType()));
               output.write(":");
               output.write(String.valueOf(w.isDestination()));
               output.write("\n");
            }
         }
      }

   }
}
