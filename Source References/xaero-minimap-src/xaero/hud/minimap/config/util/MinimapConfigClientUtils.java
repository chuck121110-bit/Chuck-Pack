package xaero.hud.minimap.config.util;

import java.util.List;
import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_638;
import net.minecraft.server.MinecraftServer;
import xaero.common.HudMod;
import xaero.common.effect.Effects;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.misc.Misc;
import xaero.hud.gui.util.GuiUtils;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.option.IndexedConfigOption;
import xaero.lib.common.config.profile.ConfigProfile;

public class MinimapConfigClientUtils {
   public static float getUIScale(ClientConfigManager configManager, IndexedConfigOption<Integer> option) {
      List<Integer> validValues = option.getValidValues();
      return getUIScale(configManager, option, (Integer)validValues.get(0), (Integer)validValues.get(validValues.size() - 1), (double)1.0F);
   }

   public static float getUIScale(ClientConfigManager configManager, IndexedConfigOption<Integer> option, double autoScale) {
      List<Integer> validValues = option.getValidValues();
      return getUIScale(configManager, option, (Integer)validValues.get(0), (Integer)validValues.get(validValues.size() - 1), autoScale);
   }

   public static float getUIScale(ClientConfigManager configManager, ConfigOption<Integer> option, int auto, int max, double autoScale) {
      int configValue = (Integer)configManager.getEffective(option);
      float configBasedScale = GuiUtils.getUIScale(configValue, auto, max);
      return configValue == auto && autoScale != (double)1.0F ? (float)Math.ceil((double)configBasedScale * autoScale) : configBasedScale;
   }

   public static double getWaypointsClampDepth(ClientConfigManager configManager, double fov, int height) {
      int baseIconScale = (int)getUIScale(configManager, MinimapProfiledConfigOptions.WAYPOINT_ICON_SCALE_IN_WORLD);
      double ingameCloseScale = (Double)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_CLOSE_SCALE_IN_WORLD);
      double frameSizeAtClampDepth = ingameCloseScale * (double)0.021333335F * (double)height / (double)baseIconScale;
      double fovMultiplier = (double)2.0F * Math.tan(Math.toRadians(fov / (double)2.0F));
      return frameSizeAtClampDepth / fovMultiplier;
   }

   public static void addAutoUIScaleValueToComponent(class_5250 component, double autoScale) {
      List var10000 = component.method_10855();
      double var10001 = Math.ceil((double)GuiUtils.getAutoUIScale() * autoScale);
      var10000.add(class_2561.method_43470(" (" + (int)var10001 + ")"));
   }

   public static void addAutoMCScaleValueToComponent(class_5250 component) {
      component.method_10855().add(class_2561.method_43470(" (" + class_310.method_1551().method_22683().method_4495() + ")"));
   }

   public static int getAutoMinimapSize() {
      int height = class_310.method_1551().method_22683().method_4506();
      int width = class_310.method_1551().method_22683().method_4489();
      int size = (int)((float)(height <= width ? height : width) / GuiUtils.getMinimapScale(HudMod.INSTANCE.getHudConfigs().getClientConfigManager()));
      return Math.min(Math.max(55, 2 * size * 130 / 1080), 250);
   }

   public static int getEffectiveMinimapSize() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      int minimapSizeConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.SIZE);
      return minimapSizeConfig > 0 ? minimapSizeConfig : getAutoMinimapSize();
   }

   public static boolean getEffectiveNorthLocked(int mapSize, int shape) {
      if (mapSize > 180 && shape == 0) {
         return true;
      } else {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         boolean northLockedConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.NORTH_LOCKED);
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession == null) {
            return northLockedConfig;
         } else {
            return northLockedConfig || !(Boolean)configManager.getEffective(MinimapProfiledConfigOptions.KEEP_ENLARGED_UNLOCKED) && minimapSession.getProcessor().isEnlargedMap();
         }
      }
   }

   private static void changeZoomUnchecked(ClientConfigManager configManager, int direction) {
      ConfigProfile currentProfile = configManager.getCurrentProfile();
      currentProfile.set(MinimapProfiledConfigOptions.ZOOM, 1 + class_3532.method_15387((Integer)currentProfile.get(MinimapProfiledConfigOptions.ZOOM) - 1 + direction, 6));
   }

   public static void changeZoom(int direction) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession == null) {
         changeZoomUnchecked(configManager, direction);
      } else {
         double targetBefore = minimapSession.getProcessor().getTargetZoom();
         int attempts = 0;

         do {
            changeZoomUnchecked(configManager, direction);
            ++attempts;
         } while(attempts < 6 && targetBefore == minimapSession.getProcessor().getTargetZoom());

         if (attempts == 6) {
            changeZoomUnchecked(configManager, direction);
         }

      }
   }

   public static boolean getEffectiveSlimeChunks(MinimapSession session) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      boolean slimeChunksConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.SLIME_CHUNKS);
      if (session == null) {
         return slimeChunksConfig;
      } else if (!slimeChunksConfig) {
         return false;
      } else if (class_310.method_1551().method_1576() != null) {
         return true;
      } else {
         MinimapWorld autoWorld = session.getWorldManager().getAutoWorld();
         return autoWorld != null && autoWorld.getSlimeChunkSeed() != null;
      }
   }

   public static Long getEffectiveSlimeChunksSeed(MinimapWorld currentWorld) {
      MinecraftServer server = class_310.method_1551().method_1576();
      if (server != null) {
         return class_310.method_1551().field_1687.method_27983() != class_1937.field_25179 ? null : server.method_3847(class_1937.field_25179).method_8412();
      } else {
         return currentWorld == null ? null : currentWorld.getSlimeChunkSeed();
      }
   }

   public static boolean isFairPlayForCaveMode() {
      boolean fairplay = HudMod.INSTANCE.isFairPlay();
      if (!fairplay) {
         return false;
      } else {
         class_638 level = class_310.method_1551().field_1687;
         if (level == null) {
            return true;
         } else {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session == null) {
               return true;
            } else {
               MinimapProcessor processor = session.getProcessor();
               return !processor.isConsideringNetherFairPlayMessage() || level.method_27983() != class_1937.field_25180;
            }
         }
      }
   }

   public static boolean hasNoCaveModeEffect() {
      class_310 mc = class_310.method_1551();
      return Misc.hasEffect(mc.field_1724, Effects.NO_CAVE_MAPS) || Misc.hasEffect(mc.field_1724, Effects.NO_CAVE_MAPS_HARMFUL);
   }

   public static boolean getEffectiveCaveModeAllowed() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      if (!(Boolean)configManager.getEffective(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED)) {
         return false;
      } else {
         Set<class_2960> allowedDimensionsLocal = (Set)configManager.getEffective(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
         Set<class_2960> allowedDimensionsServer = (Set)configManager.getServerSynced().getEffective(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
         boolean ignoringServer = configManager.shouldIgnoreServerEnforcement(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS);
         class_1937 level = class_310.method_1551().field_1687;
         if (level == null) {
            return allowedDimensionsLocal.isEmpty() && (ignoringServer || allowedDimensionsServer.isEmpty());
         } else {
            class_2960 currentDimensionId = level.method_27983().method_29177();
            if (!allowedDimensionsLocal.isEmpty() && !allowedDimensionsLocal.contains(currentDimensionId)) {
               return false;
            } else if (ignoringServer) {
               return true;
            } else {
               return allowedDimensionsServer == null || allowedDimensionsServer.isEmpty() || allowedDimensionsServer.contains(currentDimensionId);
            }
         }
      }
   }

   public static double getActualZoom(ConfigOption<Integer> option) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      int v = (Integer)configManager.getEffective(option);
      return getActualZoom(v);
   }

   public static double getActualZoom(int v) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      if ((Boolean)configManager.getEffective(MinimapProfiledConfigOptions.FURTHER_ZOOMOUT)) {
         --v;
         if (v == 0) {
            return (double)0.5F;
         }

         if (v < 0) {
            return (double)0.0F;
         }
      }

      return (double)v;
   }

   public static boolean areDiscoveredWaystonesEnabled() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.DISCOVERED_WAYSTONE_WAYPOINTS);
   }

   public static boolean areOtherWaystonesEnabled() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.OTHER_WAYSTONE_WAYPOINTS);
   }
}
