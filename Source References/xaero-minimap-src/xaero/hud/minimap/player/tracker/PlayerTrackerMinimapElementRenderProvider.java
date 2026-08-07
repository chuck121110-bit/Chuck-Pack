package xaero.hud.minimap.player.tracker;

import java.util.Iterator;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;

public class PlayerTrackerMinimapElementRenderProvider<C> extends MinimapElementRenderProvider<PlayerTrackerMinimapElement<?>, C> {
   private PlayerTrackerMinimapElementCollector collector;
   private Iterator<PlayerTrackerMinimapElement<?>> iterator;

   public PlayerTrackerMinimapElementRenderProvider(PlayerTrackerMinimapElementCollector collector) {
      this.collector = collector;
   }

   public void begin(MinimapElementRenderLocation location, C context) {
      this.iterator = this.collector.getElements().iterator();
   }

   public boolean hasNext(MinimapElementRenderLocation location, C context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public PlayerTrackerMinimapElement<?> getNext(MinimapElementRenderLocation location, C context) {
      return (PlayerTrackerMinimapElement)this.iterator.next();
   }

   public void end(MinimapElementRenderLocation location, C context) {
      this.iterator = null;
   }
}
