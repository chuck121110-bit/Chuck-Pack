package xaero.common.server.mods.argonauts;

import xaero.common.server.radar.tracker.ISyncedPlayerTrackerSystem;

public class SupportArgonautsServer {
   private final ISyncedPlayerTrackerSystem syncedPlayerTrackerSystem = new ArgonautsSyncedPlayerTrackerSystem();

   public ISyncedPlayerTrackerSystem getSyncedPlayerTrackerSystem() {
      return this.syncedPlayerTrackerSystem;
   }
}
