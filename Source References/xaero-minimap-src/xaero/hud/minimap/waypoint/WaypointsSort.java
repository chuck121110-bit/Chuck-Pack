package xaero.hud.minimap.waypoint;

public enum WaypointsSort {
   NONE("gui.xaero_sort_unsorted"),
   NAME("gui.xaero_sort_name"),
   SYMBOL("gui.xaero_sort_symbol"),
   COLOR("gui.xaero_sort_color"),
   DISTANCE("gui.xaero_sort_distance"),
   ANGLE("gui.xaero_sort_angle");

   public final String optionName;

   private WaypointsSort(String optionName) {
      this.optionName = optionName;
   }

   // $FF: synthetic method
   private static WaypointsSort[] $values() {
      return new WaypointsSort[]{NONE, NAME, SYMBOL, COLOR, DISTANCE, ANGLE};
   }
}
