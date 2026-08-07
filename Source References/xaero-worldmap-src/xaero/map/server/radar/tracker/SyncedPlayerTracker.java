package xaero.map.server.radar.tracker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import xaero.lib.XaeroLib;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.primary.option.LibPrimaryCommonConfigOptions;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.map.WorldMap;
import xaero.map.message.tracker.ClientboundTrackedPlayerPacket;
import xaero.map.server.MinecraftServerData;
import xaero.map.server.mods.SupportServerMods;
import xaero.map.server.player.ServerPlayerData;

public class SyncedPlayerTracker {
   public void onTick(MinecraftServer server, class_3222 player, MinecraftServerData serverData, ServerPlayerData playerData) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - playerData.getLastTrackedPlayerSync() >= 250L) {
         playerData.setLastTrackedPlayerSync(currentTime);
         boolean playerHasMod = playerData.hasMod();
         boolean shouldSyncToPlayer = playerHasMod;
         if (SupportServerMods.hasMinimap() && SupportServerMods.getMinimap().supportsTrackedPlayers() && SupportServerMods.getMinimap().playerSupportsTrackedPlayers(player)) {
            if (playerData.getCurrentlySyncedPlayers() != null && !playerData.getCurrentlySyncedPlayers().isEmpty()) {
               for(UUID id : playerData.getCurrentlySyncedPlayers()) {
                  this.sendRemovePacket(player, playerData, id);
               }

               playerData.getCurrentlySyncedPlayers().clear();
            }

            shouldSyncToPlayer = false;
         }

         SingleConfigManager<Config> primaryCommonConfig = WorldMap.INSTANCE.getConfigs().getPrimaryCommonConfigManager();
         SingleConfigManager<Config> libPrimaryCommonConfig = XaeroLib.INSTANCE.getLibConfigChannel().getPrimaryCommonConfigManager();
         boolean everyoneIsTracked = (Boolean)libPrimaryCommonConfig.getEffective(LibPrimaryCommonConfigOptions.EVERYONE_TRACKS_EVERYONE);
         Iterable<ISyncedPlayerTrackerSystem> playerTrackerSystems = serverData.getSyncedPlayerTrackerSystemManager().getSystems();
         Set<UUID> syncedPlayers = playerData.ensureCurrentlySyncedPlayers();
         Set<UUID> leftoverPlayers = new HashSet(syncedPlayers);
         SyncedTrackedPlayer toSync = playerData.getLastSyncedData();
         boolean shouldSyncToOthers = toSync == null || !toSync.matchesEnough(player, (double)0.0F);
         if (shouldSyncToOthers) {
            toSync = playerData.ensureLastSyncedData();
            toSync.update(player);
         }

         boolean opacReceiveParty = shouldSyncToPlayer && SupportServerMods.hasOpac() && SupportServerMods.getOpac().getReceiveLocationsFromPartyConfigValue(player);
         boolean opacReceiveMutualAllies = shouldSyncToPlayer && SupportServerMods.hasOpac() && SupportServerMods.getOpac().getReceiveLocationsFromMutualAlliesConfigValue(player);
         if (SupportServerMods.hasOpac()) {
            SupportServerMods.getOpac().updateShareLocationConfigValues(player, playerData);
         }

         for(class_3222 otherPlayer : server.method_3760().method_14571()) {
            if (otherPlayer != player) {
               leftoverPlayers.remove(otherPlayer.method_5667());
               ServerPlayerData otherPlayerData = ServerPlayerData.get(otherPlayer);
               if (shouldSyncToOthers) {
                  Set<UUID> otherPlayerSyncedPlayers = otherPlayerData.getCurrentlySyncedPlayers();
                  if (otherPlayerSyncedPlayers != null && otherPlayerSyncedPlayers.contains(player.method_5667())) {
                     this.sendTrackedPlayerPacket(otherPlayer, otherPlayerData, toSync);
                  }
               }

               if (shouldSyncToPlayer) {
                  boolean tracked = everyoneIsTracked;
                  if (!everyoneIsTracked) {
                     boolean opacConfigsAllowPartySync = !SupportServerMods.hasOpac() || SupportServerMods.getOpac().isPositionSyncAllowed(2, otherPlayerData, opacReceiveParty);
                     boolean opacConfigsAllowAllySync = !SupportServerMods.hasOpac() || SupportServerMods.getOpac().isPositionSyncAllowed(1, otherPlayerData, opacReceiveMutualAllies);

                     for(ISyncedPlayerTrackerSystem system : playerTrackerSystems) {
                        int trackingLevel = system.getTrackingLevel(player, otherPlayer);
                        if (trackingLevel > 0 && (!system.isPartySystem() || trackingLevel == 1 && opacConfigsAllowAllySync || trackingLevel > 1 && opacConfigsAllowPartySync)) {
                           tracked = true;
                           break;
                        }
                     }
                  }

                  boolean alreadySynced = syncedPlayers.contains(otherPlayer.method_5667());
                  if (!tracked) {
                     if (alreadySynced) {
                        syncedPlayers.remove(otherPlayer.method_5667());
                        this.sendRemovePacket(player, playerData, otherPlayer.method_5667());
                     }
                  } else if (!alreadySynced && otherPlayerData.getLastSyncedData() != null) {
                     syncedPlayers.add(otherPlayer.method_5667());
                     this.sendTrackedPlayerPacket(player, playerData, otherPlayerData.getLastSyncedData());
                  }
               }
            }
         }

         for(UUID offlineId : leftoverPlayers) {
            syncedPlayers.remove(offlineId);
            this.sendRemovePacket(player, playerData, offlineId);
         }

      }
   }

   private void sendRemovePacket(class_3222 player, ServerPlayerData playerData, UUID toRemove) {
      WorldMap.messageHandler.sendToPlayer(player, new ClientboundTrackedPlayerPacket(true, toRemove, (double)0.0F, (double)0.0F, (double)0.0F, (class_2960)null, playerData.getClientModNetworkVersion()));
   }

   private void sendTrackedPlayerPacket(class_3222 player, ServerPlayerData playerData, SyncedTrackedPlayer tracked) {
      WorldMap.messageHandler.sendToPlayer(player, new ClientboundTrackedPlayerPacket(false, tracked.getId(), tracked.getX(), tracked.getY(), tracked.getZ(), tracked.getDimension().method_29177(), playerData.getClientModNetworkVersion()));
   }
}
