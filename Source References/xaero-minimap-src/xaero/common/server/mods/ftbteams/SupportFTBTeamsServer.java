package xaero.common.server.mods.ftbteams;

import xaero.common.server.radar.tracker.ISyncedPlayerTrackerSystem;

public class SupportFTBTeamsServer {
   private final ISyncedPlayerTrackerSystem syncedPlayerTrackerSystem = new FTBTeamsSyncedPlayerTrackerSystem();

   public ISyncedPlayerTrackerSystem getSyncedPlayerTrackerSystem() {
      return this.syncedPlayerTrackerSystem;
   }
}
