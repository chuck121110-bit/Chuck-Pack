package xaero.hud.minimap.player.tracker.system;

import java.util.Iterator;

public interface IRenderedPlayerTracker<P> {
   ITrackedPlayerReader<P> getReader();

   Iterator<P> getTrackedPlayerIterator();
}
