package xaero.map.config.util;

import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.profile.ConfigProfile;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.mcworld.WorldMapClientWorldData;
import xaero.map.mcworld.WorldMapClientWorldDataHelper;
import xaero.map.mods.SupportMods;

public class WorldMapClientConfigUtils {
   public static boolean isFairPlay() {
      boolean defaultValue = false;
      if (SupportMods.minimap() && SupportMods.xaeroMinimap.isFairPlay()) {
         defaultValue = true;
      }

      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return defaultValue;
      } else if (!worldmapSession.getMapProcessor().fairplayMessageWasReceived()) {
         return defaultValue;
      } else {
         return !worldmapSession.getMapProcessor().isConsideringNetherFairPlay() || worldmapSession.getMapProcessor().getMapWorld().getCurrentDimensionId() != class_1937.field_25180;
      }
   }

   public static boolean isCaveModeDisabledLegacy() {
      if (WorldMap.INSTANCE.getConfigs().getClientConfigManager().getServerSynced().isChannelPresentOnServer()) {
         return false;
      } else if (class_310.method_1551().field_1687 == null) {
         return false;
      } else {
         WorldMapClientWorldData clientData = WorldMapClientWorldDataHelper.getCurrentWorldData();
         return !clientData.getSyncedRules().allowCaveModeOnServer && class_310.method_1551().field_1687.method_27983() != class_1937.field_25180 || !clientData.getSyncedRules().allowNetherCaveModeOnServer && class_310.method_1551().field_1687.method_27983() == class_1937.field_25180;
      }
   }

   public static boolean getEffectiveCaveModeAllowed() {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      if (!(Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.CAVE_MODE_ALLOWED)) {
         return false;
      } else {
         WorldMapSession session = WorldMapSession.getCurrentSession();
         if (session == null) {
            return true;
         } else {
            class_5321<class_1937> currentDimKey = session.getMapProcessor().getMapWorld().getCurrentDimensionId();
            if (currentDimKey == null) {
               return false;
            } else {
               class_2960 currentDimId = currentDimKey.method_29177();
               Set<class_2960> localCaveModeDimensions = (Set)configManager.getEffective(WorldMapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
               if (!localCaveModeDimensions.isEmpty() && !localCaveModeDimensions.contains(currentDimId)) {
                  return false;
               } else if (configManager.shouldIgnoreServerEnforcement(WorldMapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS)) {
                  return true;
               } else {
                  Set<class_2960> serverCaveModeDimensions = (Set)configManager.getServerSynced().getEffective(WorldMapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
                  return serverCaveModeDimensions != null && !serverCaveModeDimensions.isEmpty() ? serverCaveModeDimensions.contains(currentDimId) : true;
               }
            }
         }
      }
   }

   public static boolean getDebug() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession != null) {
         return worldmapSession.getMapProcessor().isDebugConfig();
      } else {
         ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
         SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
         return (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.DEBUG);
      }
   }

   public static void togglePrimaryOption(ConfigOption<Boolean> option) {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      primaryConfigManager.getConfig().set(option, !(Boolean)primaryConfigManager.getConfig().get(option));
      WorldMap.INSTANCE.getConfigs().getPrimaryClientConfigManagerIO().save();
   }

   public static void tryTogglingCurrentProfileOption(ConfigOption<Boolean> option) {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      if (!isOptionServerEnforced(option)) {
         ConfigProfile currentProfile = configManager.getCurrentProfile();
         currentProfile.set(option, !(Boolean)currentProfile.get(option));
         WorldMap.INSTANCE.getConfigs().getClientConfigProfileIO().save(currentProfile);
      }
   }

   public static boolean isOptionServerEnforced(ConfigOption<Boolean> option) {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      if (configManager.shouldIgnoreServerEnforcement(option)) {
         return false;
      } else {
         return configManager.getServerSynced().getEffective(option) != null;
      }
   }
}
