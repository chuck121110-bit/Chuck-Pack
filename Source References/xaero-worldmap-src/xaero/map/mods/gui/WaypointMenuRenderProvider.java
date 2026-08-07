package xaero.map.mods.gui;

import java.util.ArrayList;
import java.util.Iterator;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.mods.SupportXaeroMinimap;

public class WaypointMenuRenderProvider extends ElementRenderProvider<Waypoint, WaypointMenuRenderContext> {
   private final SupportXaeroMinimap minimap;
   private Iterator<Waypoint> iterator;

   public WaypointMenuRenderProvider(SupportXaeroMinimap minimap) {
      this.minimap = minimap;
   }

   public void begin(ElementRenderLocation location, WaypointMenuRenderContext context) {
      ArrayList<Waypoint> sortedList = this.minimap.getWaypointsSorted();
      if (sortedList == null) {
         this.iterator = null;
      } else {
         this.iterator = this.minimap.getWaypointsSorted().iterator();
      }

   }

   public boolean hasNext(ElementRenderLocation location, WaypointMenuRenderContext context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public Waypoint getNext(ElementRenderLocation location, WaypointMenuRenderContext context) {
      return (Waypoint)this.iterator.next();
   }

   public void end(ElementRenderLocation location, WaypointMenuRenderContext context) {
   }
}
