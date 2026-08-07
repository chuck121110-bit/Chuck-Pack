package xaero.common.events;

import java.nio.file.Path;
import java.util.Set;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;
import xaero.common.HudMod;
import xaero.common.server.MinecraftServerData;
import xaero.common.server.MineraftServerDataInitializer;
import xaero.common.server.level.LevelMapProperties;
import xaero.common.server.player.IServerPlayer;
import xaero.common.server.player.ServerPlayerData;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.packet.basic.ClientboundRulesPacket;
import xaero.hud.packet.basic.HandshakePacket;
import xaero.hud.packet.tracker.ClientboundPlayerTrackerResetPacket;
import xaero.lib.common.config.profile.ConfigProfile;
import xaero.lib.common.config.server.ServerConfigManager;

public abstract class CommonEvents {
   private final HudMod modMain;

   public CommonEvents(HudMod modMain) {
      this.modMain = modMain;
   }

   void onPlayerClone(class_1657 oldPlayer, class_1657 newPlayer, boolean alive) {
      if (oldPlayer instanceof class_3222) {
         ((IServerPlayer)newPlayer).setXaeroMinimapPlayerData(ServerPlayerData.get((class_3222)oldPlayer));
      }

   }

   public void onServerStarting(MinecraftServer server) {
      if (this.modMain.isFirstStageLoaded()) {
         (new MineraftServerDataInitializer()).init(server, this.modMain);
      }
   }

   public void onServerStopped(MinecraftServer server) {
   }

   public void onPlayerLogIn(class_1657 player) {
      if (this.modMain.isFirstStageLoaded()) {
         if (player instanceof class_3222) {
            class_3222 serverPlayer = (class_3222)player;
            this.modMain.getMessageHandler().sendToPlayer(serverPlayer, new ClientboundPlayerTrackerResetPacket());
         }

      }
   }

   public void onPlayerWorldJoin(class_3222 player) {
      if (this.modMain.isFirstStageLoaded()) {
         this.modMain.getMessageHandler().sendToPlayer(player, new HandshakePacket());
         ServerConfigManager configManager = this.modMain.getHudConfigs().getServerConfigManager();
         ConfigProfile defaultEnforcedProfile = configManager.getDefaultEnforcedProfile();
         boolean caveModeConfig = !Boolean.FALSE.equals(defaultEnforcedProfile.get(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED));
         Set<class_2960> caveModeDimensionsConfig = (Set)defaultEnforcedProfile.get(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
         boolean allowOverworldCaveModeOnServer = caveModeConfig && (caveModeDimensionsConfig == null || caveModeDimensionsConfig.isEmpty() || caveModeDimensionsConfig.contains(class_1937.field_25179.method_29177()));
         boolean allowNetherCaveModeOnServer = caveModeConfig && (caveModeDimensionsConfig == null || caveModeDimensionsConfig.isEmpty() || caveModeDimensionsConfig.contains(class_1937.field_25180.method_29177()));
         boolean radarConfig = !Boolean.FALSE.equals(defaultEnforcedProfile.get(MinimapProfiledConfigOptions.DISPLAY_RADAR));
         this.modMain.getMessageHandler().sendToPlayer(player, new ClientboundRulesPacket(allowOverworldCaveModeOnServer, allowNetherCaveModeOnServer, radarConfig));
         Path propertiesPath = player.method_51469().method_8503().method_27050(class_5218.field_24184).getParent().resolve("xaeromap.txt");

         try {
            MinecraftServerData serverData = MinecraftServerData.get(player.method_51469().method_8503());
            LevelMapProperties properties = serverData.getLevelProperties(propertiesPath);
            this.modMain.getMessageHandler().sendToPlayer(player, properties);
         } catch (Throwable t) {
            MinimapLogs.LOGGER.error("suppressed exception", t);
            player.field_13987.method_52396(class_2561.method_43471("gui.xaero_error_loading_properties"));
         }

      }
   }

   public void handlePlayerTickStart(class_1657 player) {
      if (this.modMain.isFirstStageLoaded()) {
         if (player instanceof class_3222) {
            this.modMain.getServerPlayerTickHandler().tick((class_3222)player);
         } else {
            if (HudMod.INSTANCE.getEvents() != null) {
               HudMod.INSTANCE.getEvents().handlePlayerTickStart(player);
            }

         }
      }
   }
}
