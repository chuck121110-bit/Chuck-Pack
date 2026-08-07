package xaero.common.server;

import net.minecraft.server.MinecraftServer;
import xaero.common.IXaeroMinimap;
import xaero.common.server.radar.tracker.SyncedPlayerTracker;
import xaero.common.server.radar.tracker.SyncedPlayerTrackerSystemManager;

public class MineraftServerDataInitializer {
   public void init(MinecraftServer server, IXaeroMinimap modMain) {
      SyncedPlayerTrackerSystemManager syncedPlayerTrackerSystemManager = new SyncedPlayerTrackerSystemManager();
      if (modMain.getSupportServerMods().hasFtbTeams()) {
         syncedPlayerTrackerSystemManager.register("ftb_teams", modMain.getSupportServerMods().getFtbTeams().getSyncedPlayerTrackerSystem());
      }

      if (modMain.getSupportServerMods().hasArgonauts()) {
         syncedPlayerTrackerSystemManager.register("argonauts", modMain.getSupportServerMods().getArgonauts().getSyncedPlayerTrackerSystem());
      }

      SyncedPlayerTracker syncedPlayerTracker = new SyncedPlayerTracker(modMain);
      MinecraftServerData data = new MinecraftServerData(syncedPlayerTrackerSystemManager, syncedPlayerTracker, modMain);
      ((IMinecraftServer)server).setXaeroMinimapServerData(data);
   }
}
