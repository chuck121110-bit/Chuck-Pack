package xaero.hud.minimap.waypoint;

import net.minecraft.class_2561;

public enum WaypointVisibilityType {
   LOCAL(class_2561.method_43471("gui.xaero_waypoints_local"), false),
   GLOBAL(class_2561.method_43471("gui.xaero_waypoints_global"), true),
   WORLD_MAP_LOCAL(class_2561.method_43471("gui.xaero_waypoint_visibility_type_world_map_local"), false),
   WORLD_MAP_GLOBAL(class_2561.method_43471("gui.xaero_waypoint_visibility_type_world_map_global"), true);

   private final class_2561 translation;
   private final boolean global;

   private WaypointVisibilityType(class_2561 translation, boolean global) {
      this.translation = translation;
      this.global = global;
   }

   public class_2561 getTranslation() {
      return this.translation;
   }

   public boolean isGlobal() {
      return this.global;
   }

   // $FF: synthetic method
   private static WaypointVisibilityType[] $values() {
      return new WaypointVisibilityType[]{LOCAL, GLOBAL, WORLD_MAP_LOCAL, WORLD_MAP_GLOBAL};
   }
}
