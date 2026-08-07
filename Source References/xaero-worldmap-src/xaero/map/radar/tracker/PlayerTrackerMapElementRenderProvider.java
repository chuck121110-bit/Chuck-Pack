package xaero.map.radar.tracker;

import java.util.Iterator;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;

public class PlayerTrackerMapElementRenderProvider<C> extends ElementRenderProvider<PlayerTrackerMapElement<?>, C> {
   private PlayerTrackerMapElementCollector collector;
   private Iterator<PlayerTrackerMapElement<?>> iterator;

   public PlayerTrackerMapElementRenderProvider(PlayerTrackerMapElementCollector collector) {
      this.collector = collector;
   }

   public void begin(ElementRenderLocation location, C context) {
      this.iterator = this.collector.getElements().iterator();
   }

   public boolean hasNext(ElementRenderLocation location, C context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public PlayerTrackerMapElement<?> getNext(ElementRenderLocation location, C context) {
      return (PlayerTrackerMapElement)this.iterator.next();
   }

   public void end(ElementRenderLocation location, C context) {
      this.iterator = null;
   }
}
