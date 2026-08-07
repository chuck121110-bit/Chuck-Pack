package xaero.hud.minimap.player.tracker.synced;

import java.util.UUID;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaero.common.server.radar.tracker.SyncedTrackedPlayer;
import xaero.hud.minimap.player.tracker.system.ITrackedPlayerReader;

public class SyncedTrackedPlayerReader implements ITrackedPlayerReader<SyncedTrackedPlayer> {
   public UUID getId(SyncedTrackedPlayer player) {
      return player.getId();
   }

   public double getX(SyncedTrackedPlayer player) {
      return player.getX();
   }

   public double getY(SyncedTrackedPlayer player) {
      return player.getY();
   }

   public double getZ(SyncedTrackedPlayer player) {
      return player.getZ();
   }

   public class_5321<class_1937> getDimension(SyncedTrackedPlayer player) {
      return player.getDimensionKey();
   }
}
