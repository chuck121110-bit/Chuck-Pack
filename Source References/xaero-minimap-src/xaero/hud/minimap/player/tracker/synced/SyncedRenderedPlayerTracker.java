package xaero.hud.minimap.player.tracker.synced;

import java.util.Iterator;
import net.minecraft.class_310;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.mcworld.MinimapClientWorldData;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.common.server.radar.tracker.SyncedTrackedPlayer;
import xaero.hud.minimap.player.tracker.system.IRenderedPlayerTracker;
import xaero.hud.minimap.player.tracker.system.ITrackedPlayerReader;

public class SyncedRenderedPlayerTracker implements IRenderedPlayerTracker<SyncedTrackedPlayer> {
   private final SyncedTrackedPlayerReader reader = new SyncedTrackedPlayerReader();

   public ITrackedPlayerReader<SyncedTrackedPlayer> getReader() {
      return this.reader;
   }

   public Iterator<SyncedTrackedPlayer> getTrackedPlayerIterator() {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      if (minimapSession == null) {
         return null;
      } else {
         if (class_310.method_1551().method_1576() == null) {
            MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
            if (worldData.serverLevelId == null) {
               return null;
            }
         }

         ClientSyncedTrackedPlayerManager manager = minimapSession.getMinimapProcessor().getSyncedTrackedPlayerManager();
         return manager.getPlayers().iterator();
      }
   }

   public boolean shouldUseWorldMapTrackedPlayers(XaeroMinimapSession minimapSession) {
      return !minimapSession.getMinimapProcessor().serverHasMod();
   }
}
