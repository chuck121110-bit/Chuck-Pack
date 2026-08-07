package xaero.common.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import net.minecraft.class_1074;
import net.minecraft.class_1937;
import net.minecraft.class_304;
import net.minecraft.class_310;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.hud.gui.util.GuiUtils;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.category.EntityRadarBackwardsCompatibilityConfig;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.path.XaeroPath;
import xaero.hud.path.XaeroPathReader;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.primary.option.LibPrimaryCommonConfigOptions;

public class ModSettings {
   public static int defaultSettings;
   public static final String format = "§";
   protected IXaeroMinimap modMain;
   private EntityRadarBackwardsCompatibilityConfig entityRadarBackwardsCompatibilityConfig;
   private boolean foundOldRadarSettings;
   public boolean needsLegacySlimeSeedResave;
   private String loadedWaypointLines;
   public static int serverSettings;
   private static HashMap<XaeroPath, Long> legacyServerSlimeSeeds = new HashMap();
   private static final String[] SHOW_LIGHT_LEVEL_NAMES = new String[]{"gui.xaero_off", "gui.xaero_light_block", "gui.xaero_light_sky", "gui.xaero_light_all", "gui.xaero_light_both2"};
   private static int[] OLD_MINIMAP_SIZES = new int[]{57, 85, 113, 169};
   public Boolean showCoordsLegacy;
   public Boolean showBiomeLegacy;
   public Integer showLightLevelLegacy;
   public Integer showTimeLegacy;
   public Boolean showAnglesLegacy;
   public Boolean showDimensionNameLegacy;
   public Boolean displayWeatherInfoLegacy;

   public ModSettings(IXaeroMinimap modMain) {
      this.modMain = modMain;
      this.entityRadarBackwardsCompatibilityConfig = new EntityRadarBackwardsCompatibilityConfig();
      defaultSettings = modMain.getVersionID().endsWith("fair") ? 16188159 : Integer.MAX_VALUE;
      if (serverSettings == 0) {
         serverSettings = defaultSettings;
      }

   }

   /** @deprecated */
   @Deprecated
   public boolean isKeyRepeat(class_304 kb) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public boolean getMinimap() {
      return (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.DISPLAY_MINIMAP);
   }

   /** @deprecated */
   @Deprecated
   public void setSlimeChunksSeed(long seed, XaeroPath fullWorldID) {
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (session == null) {
         legacyServerSlimeSeeds.put(fullWorldID, seed);
      } else {
         MinimapWorld minimapWorld = session.getWorldManager().getWorld(fullWorldID);
         if (minimapWorld == null) {
            legacyServerSlimeSeeds.put(fullWorldID, seed);
         } else {
            minimapWorld.setSlimeChunkSeed(seed);
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public Long getSlimeChunksSeed(XaeroPath fullWorldID) {
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (session == null) {
         return this.getLegacySlimeChunksSeed(fullWorldID);
      } else {
         MinimapWorld minimapWorld = session.getWorldManager().getWorld(fullWorldID);
         return minimapWorld == null ? this.getLegacySlimeChunksSeed(fullWorldID) : minimapWorld.getSlimeChunkSeed();
      }
   }

   /** @deprecated */
   @Deprecated
   public Long getLegacySlimeChunksSeed(XaeroPath fullWorldID) {
      return (Long)legacyServerSlimeSeeds.get(fullWorldID);
   }

   public Long removeLegacySlimeChunksSeed(XaeroPath fullWorldID) {
      this.needsLegacySlimeSeedResave = true;
      return (Long)legacyServerSlimeSeeds.remove(fullWorldID);
   }

   /** @deprecated */
   @Deprecated
   public boolean getSlimeChunks(MinimapSession session) {
      return MinimapConfigClientUtils.getEffectiveSlimeChunks(session);
   }

   public boolean waypointsGUI(MinimapSession waypointSession) {
      MinimapProcessor processor = waypointSession.getProcessor();
      return class_310.method_1551().field_1724 != null && waypointSession.getWorldState().getAutoWorldPath() != null && (processor.getMinimapItem() == null || class_310.method_1551().field_1724 == null || MinimapProcessor.hasMinimapItem(class_310.method_1551().field_1724));
   }

   public float getMinimapScale() {
      return GuiUtils.getMinimapScale(HudMod.INSTANCE.getHudConfigs().getClientConfigManager());
   }

   public float getUIScale(int optionValue, int min, int max) {
      return GuiUtils.getUIScale(optionValue, min, max);
   }

   public int getAutoUIScale() {
      return GuiUtils.getAutoUIScale();
   }

   private int getMaxWaypointsDistance(int exp) {
      return exp <= 0 ? 0 : (int)Math.pow((double)2.0F, (double)(2 + exp));
   }

   private boolean assumeUsingFBO() {
      return this.modMain.getMinimap().getMinimapFBORenderer().assumeUsingFBO();
   }

   public boolean isIgnoreHeightmaps() {
      if (this.modMain.getSupportMods().shouldUseWorldMapChunks()) {
         return this.modMain.getSupportMods().worldmapSupport.getWorldMapIgnoreHeightmaps();
      } else {
         MinimapWorldRootContainer currentRootContainer = ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getWorldManager().getAutoRootContainer();
         return currentRootContainer.getConfig().isIgnoreHeightmaps();
      }
   }

   public void writeSettings(PrintWriter writer) {
   }

   public void saveSettings() throws IOException {
      PrintWriter writer = null;

      try {
         writer = new PrintWriter(new FileWriter(this.modMain.getConfigFile().toFile()));
         this.writeSettings(writer);
         Object[] keys = legacyServerSlimeSeeds.keySet().toArray();
         Object[] values = legacyServerSlimeSeeds.values().toArray();

         for(int i = 0; i < keys.length; ++i) {
            String var10001 = String.valueOf(keys[i]);
            writer.println("seed:" + var10001 + ":" + String.valueOf(values[i]));
         }

         if (this.loadedWaypointLines != null && !this.loadedWaypointLines.isEmpty()) {
            writer.print(this.loadedWaypointLines);
         }

         this.modMain.getHudIO().save(writer);
      } finally {
         if (writer != null) {
            writer.close();
         }

      }

      this.needsLegacySlimeSeedResave = false;
   }

   public void readSetting(String[] args) {
      String valueString = args.length < 2 ? "" : args[1];
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      if (args[0].equalsIgnoreCase("ignoreUpdate")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.IGNORED_UPDATE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("updateNotification")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.UPDATE_NOTIFICATIONS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("minimapItemId")) {
         String minimapItemId = valueString + ":" + args[2];
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.MINIMAP_ITEM, minimapItemId);
      } else if (args[0].equalsIgnoreCase("allowWrongWorldTeleportation")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.WRONG_WORLD_TELEPORT, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("differentiateByServerAddress")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.DIFFERENTIATE_BY_SERVER_ADDRESS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("debugEntityIcons")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.DEBUG_ENTITY_ICONS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("debugEntityVariantIds")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.DEBUG_ENTITY_VARIANT_IDS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("radarHideInvisibleEntities")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_HIDE_INVISIBLE, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("allowInternetAccess")) {
         boolean savedAllowInternetAccess = valueString.equals("true");
         if (!savedAllowInternetAccess) {
            XaeroLib.INSTANCE.getLibConfigChannel().getPrimaryCommonConfigManager().getConfig().set(LibPrimaryCommonConfigOptions.ALLOW_INTERNET, false);
            XaeroLib.INSTANCE.getLibConfigChannel().getPrimaryCommonConfigManagerIO().save();
         }
      } else if (args[0].equalsIgnoreCase("minimap")) {
         BuiltInHudModules.MINIMAP.setActive(configManager, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("caveMaps")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.AUTO_CAVE_MODE, valueString.equals("true") ? 1 : (valueString.equals("false") ? 0 : Integer.parseInt(valueString)));
      } else if (args[0].equalsIgnoreCase("caveZoom")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CAVE_ZOOM, valueString.equals("true") ? 3 : (valueString.equals("false") ? 1 : Integer.parseInt(valueString) + 1));
      } else if (args[0].equalsIgnoreCase("showWaypoints")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINTS_ON_MINIMAP, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("deathpoints")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DEATHPOINTS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("oldDeathpoints")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OLD_DEATHPOINTS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("showIngameWaypoints")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINTS_IN_WORLD, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("displayRedstone")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_REDSTONE, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("distance")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_IN_WORLD, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("showCoords")) {
         this.showCoordsLegacy = valueString.equals("true");
      } else if (args[0].equalsIgnoreCase("lockNorth")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.NORTH_LOCKED, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("zoom")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ZOOM, Integer.parseInt(valueString) + 1);
      } else if (args[0].equalsIgnoreCase("mapSize")) {
         int oldSize = Integer.parseInt(valueString);
         if (oldSize == -1) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SIZE, 0);
         } else {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SIZE, OLD_MINIMAP_SIZES[oldSize]);
         }
      } else if (args[0].equalsIgnoreCase("minimapSize")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SIZE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("chunkGrid")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CHUNK_GRID, valueString.equals("true") ? 0 : (valueString.equals("false") ? -1 : Integer.parseInt(valueString)));
      } else if (args[0].equalsIgnoreCase("slimeChunks")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SLIME_CHUNKS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("mapSafeMode")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SAFE_MODE, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("minimapOpacity")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPACITY, Double.valueOf(valueString).intValue());
      } else if (args[0].equalsIgnoreCase("waypointsIngameIconScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_ICON_SCALE_IN_WORLD, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("waypointsIngameDistanceScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_SCALE_IN_WORLD, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("waypointsIngameNameScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_NAME_SCALE_IN_WORLD, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("waypointsIngameCloseScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_CLOSE_SCALE_IN_WORLD, Double.valueOf(valueString));
      } else if (args[0].equalsIgnoreCase("antiAliasing")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ANTI_ALIASING, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("blockColours")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.BLOCK_COLORS, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lighting")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LIGHTING, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("dotsStyle")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_DOTS_STYLE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("dotNameScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_NAME_SCALE, Double.valueOf(valueString));
      } else if (args[0].equalsIgnoreCase("compassOverEverything")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_OVER_EVERYTHING, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("showBiome")) {
         this.showBiomeLegacy = valueString.equals("true");
      } else if (args[0].equalsIgnoreCase("showFlowers")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_FLOWERS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("keepWaypointNames")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_NAME_IN_WORLD, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("waypointsDistance")) {
         double oldValue = Double.valueOf(valueString);
         int exp = oldValue <= (double)0.0F ? 0 : (int)Math.max((double)3.0F, Math.ceil(Math.log(oldValue) / Math.log((double)2.0F))) - 2;
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE, this.getMaxWaypointsDistance(exp));
      } else if (args[0].equalsIgnoreCase("waypointsDistanceExp")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE, this.getMaxWaypointsDistance(Integer.parseInt(valueString)));
      } else if (args[0].equalsIgnoreCase("waypointsDistanceMin")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_MIN_DISTANCE_IN_WORLD, Double.valueOf(valueString));
      } else if (args[0].equalsIgnoreCase("waypointTp")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT, "/" + valueString + " {x} {y} {z}");
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT, "/" + valueString + " {x} {y} {z} {yaw} ~");
      } else if (args[0].equalsIgnoreCase("waypointTPCommand")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT, valueString.replace("^col^", ":") + " {x} {y} {z}");
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT, valueString.replace("^col^", ":") + " {x} {y} {z} {yaw} ~");
      } else if (args[0].equalsIgnoreCase("defaultWaypointTPCommandFormat")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT, valueString.replace("^col^", ":"));
      } else if (args[0].equalsIgnoreCase("defaultWaypointTPCommandRotationFormat")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT, valueString.replace("^col^", ":"));
      } else if (args[0].equalsIgnoreCase("arrowScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ARROW_SCALE, Double.valueOf(valueString));
      } else if (args[0].equalsIgnoreCase("arrowColour")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ARROW_COLOR, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("seed")) {
         legacyServerSlimeSeeds.put((new XaeroPathReader()).read(valueString), Long.parseLong(args[2]));
      } else if (args[0].equalsIgnoreCase("smoothDots")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_SMOOTH_DOTS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("worldMap")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_WORLD_MAP_CHUNKS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("terrainDepth")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TERRAIN_DEPTH, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("terrainSlopes")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TERRAIN_SLOPES, valueString.equals("true") ? 2 : (valueString.equals("false") ? 0 : Integer.parseInt(valueString)));
      } else if (args[0].equalsIgnoreCase("alwaysArrow") && valueString.equals("true")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_MAIN_ENTITY, 2);
      } else if (args[0].equalsIgnoreCase("mainEntityAs")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_MAIN_ENTITY, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("blockTransparency")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.BLOCK_TRANSPARENCY, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("waypointOpacityIngame")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_OPACITY_IN_WORLD, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("waypointOpacityMap")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_OPACITY_ON_MINIMAP, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("hideWorldNames")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.HIDE_WORLD_NAMES, valueString.equals("true") ? 2 : (valueString.equals("false") ? 1 : Integer.parseInt(valueString)));
      } else if (args[0].equalsIgnoreCase("openSlimeSettings")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPEN_SLIME_CHUNKS_SCREEN, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("alwaysShowDistance")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_SHORT_DISTANCE_IN_WORLD, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("showLightLevel")) {
         this.showLightLevelLegacy = valueString.equals("true") ? 1 : (valueString.equals("false") ? 0 : Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("renderLayerIndex")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RENDER_LAYER, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("crossDimensionalTp")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_TELEPORT_CROSS_DIMENSION, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("showTime")) {
         this.showTimeLegacy = Integer.parseInt(valueString);
      } else if (args[0].equalsIgnoreCase("biomeColorsVanillaMode")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.BIOMES_IN_VANILLA_COLORS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("lookingAtAngle")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_HORIZONTAL_POINTING_ANGLE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lookingAtAngleVertical")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_VERTICAL_POINTING_ANGLE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("centeredEnlarged")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CENTERED_ENLARGED, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("zoomedOutEnlarged")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ZOOM_ENLARGED, valueString.equals("true") ? 1 : 0);
      } else if (args[0].equalsIgnoreCase("zoomOnEnlarged")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ZOOM_ENLARGED, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("minimapTextAlign")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.INFO_DISPLAY_ALIGNMENT, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("showAngles")) {
         this.showAnglesLegacy = valueString.equals("true");
      } else if (args[0].equalsIgnoreCase("waypointsMutualEdit")) {
         configManager.getPrimaryConfigManager().getConfig().set(MinimapPrimaryClientConfigOptions.WAYPOINT_MUTUAL_EDIT, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("compass")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_LOCATION, valueString.equals("true") ? 1 : 0);
      } else if (args[0].equalsIgnoreCase("compassLocation")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_LOCATION, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("compassDirectionScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_SCALE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("caveMapsDepth")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CAVE_MODE_DEPTH, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("hideWaypointCoordinates")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.HIDE_WAYPOINT_COORDINATES, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("renderAllSets")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINTS_ALL_SETS, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("playerArrowOpacity")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ARROW_OPACITY, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("waypointsBottom")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.NEW_WAYPOINTS_TO_BOTTOM, valueString.equals("true"));
      } else if (args[0].equalsIgnoreCase("minimapShape")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.SHAPE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lightOverlayType")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lightOverlayMaxLight")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_MAX_LIGHT, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lightOverlayMinLight")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_MIN_LIGHT, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("lightOverlayColor")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LIGHT_OVERLAY_COLOR, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("uiScale")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.UI_SCALE, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("bossHealthPushBox")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.BOSS_HEALTH_PUSH_BOX, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("potionEffectPushBox")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.POTION_EFFECT_PUSH_BOX, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("minimapFrame")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.FRAME, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("minimapFrameColor")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.FRAME_COLOR, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("compassColor")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_SHADOW_COLOR, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("northCompassColor")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.COMPASS_NORTH_SHADOW_COLOR, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("showDimensionName")) {
         this.showDimensionNameLegacy = valueString.equals("true");
      } else if (args[0].equalsIgnoreCase("displayMultipleWaypointInfo")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.MULTIPLE_WAYPOINTS_INFO, Integer.parseInt(valueString));
      } else if (args[0].equalsIgnoreCase("entityRadar")) {
         configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_RADAR, valueString.equals("true"));
      } else {
         if (this.entityRadarBackwardsCompatibilityConfig.readSetting(args)) {
            this.foundOldRadarSettings = true;
            return;
         }

         if (args[0].equalsIgnoreCase("adjustHeightForCarpetLikeBlocks")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.ADJUST_HEIGHT_FOR_SHORT_BLOCKS, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("autoConvertWaypointDistanceToKmThreshold")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_CONVERT_DISTANCE_TO_KM_AT, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("waypointDistancePrecision")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_PRECISION, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("mainDotSize")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.RADAR_MAIN_DOT_SIZE, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("partialYTeleportation")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_PARTIAL_Y_TELEPORT, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("deleteReachedDeathpoints")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DELETE_REACHED_DEATHPOINTS, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("hideMinimapUnderScreen")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.HIDE_UNDER_SCREEN, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("manualCaveModeStart")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("manualCaveModeStartAuto")) {
            boolean savedManualCaveModeStartAuto = valueString.equals("true");
            if (savedManualCaveModeStartAuto) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START, 0);
            }
         } else if (args[0].equalsIgnoreCase("chunkGridLineWidth")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CHUNK_GRID_LINE_WIDTH, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("hideMinimapUnderF3")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.HIDE_UNDER_F3, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("temporaryWaypointsGlobal")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TEMPORARY_WAYPOINTS_GLOBAL, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("keepUnlockedWhenEnlarged")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.KEEP_ENLARGED_UNLOCKED, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("enlargedMinimapAToggle")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TOGGLED_ENLARGED, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("displayStainedGlass")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.DISPLAY_STAINED_GLASS, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("waypointOnMapScale")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_ICON_SCALE_ON_MINIMAP, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("switchToAutoOnDeath")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.AUTO_WAYPOINTS_ON_DEATH, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("displayWeatherInfo")) {
            this.displayWeatherInfoLegacy = valueString.equals("true");
         } else if (args[0].equalsIgnoreCase("infoDisplayBackgroundOpacity")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.INFO_DISPLAY_BG_OPACITY, Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("caveModeToggleTimer")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.CAVE_MODE_TOGGLE_TIMER, (double)Integer.parseInt(valueString));
         } else if (args[0].equalsIgnoreCase("legibleCaveMaps")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.LEGIBLE_CAVE_MAPS, valueString.equals("true"));
         } else if (args[0].equalsIgnoreCase("biomeBlending")) {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.BIOME_BLENDING, valueString.equals("true"));
         } else if (!args[0].equalsIgnoreCase("displayPacPlayers") && !args[0].equalsIgnoreCase("displayTrackedPlayers")) {
            if (args[0].equalsIgnoreCase("displayTrackedPlayersOnMap")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYERS_ON_MINIMAP, valueString.equals("true"));
            } else if (args[0].equalsIgnoreCase("displayTrackedPlayersInWorld")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYERS_IN_WORLD, valueString.equals("true"));
            } else if (args[0].equalsIgnoreCase("dimensionScaledMaxWaypointDistance")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE_DIMENSION_SCALE, valueString.equals("true"));
            } else if (args[0].equalsIgnoreCase("trackedPlayerWorldIconScale")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_ICON_SCALE, Integer.parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("trackedPlayerWorldNameScale")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_NAME_SCALE, Integer.parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("trackedPlayerMinimapIconScale")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYER_MINIMAP_ICON_SCALE, Integer.parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("displayClaims")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CLAIMS, valueString.equals("true"));
            } else if (args[0].equalsIgnoreCase("displayCurrentClaim")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CURRENT_CLAIM, valueString.equals("true"));
            } else if (args[0].equalsIgnoreCase("claimsOpacity")) {
               int borderOpacity = Integer.parseInt(valueString);
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY, borderOpacity);
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY, borderOpacity * 58 / 100);
            } else if (args[0].equalsIgnoreCase("claimsBorderOpacity")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY, Integer.parseInt(valueString));
            } else if (args[0].equalsIgnoreCase("claimsFillOpacity")) {
               configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY, Integer.parseInt(valueString));
            }
         } else {
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYERS_ON_MINIMAP, valueString.equals("true"));
            configManager.getCurrentProfile().set(MinimapProfiledConfigOptions.TRACKED_PLAYERS_IN_WORLD, valueString.equals("true"));
         }
      }

   }

   public void loadDefaultSettings(boolean shouldLoadLegacySettings) throws IOException {
      Path mainConfigFile = this.modMain.getConfigFile();
      Path legacyDefaultConfigFile = mainConfigFile.getParent().resolveSibling("defaultconfigs").resolve(HudMod.INSTANCE.getOldConfigFileName());
      if (Files.exists(legacyDefaultConfigFile, new LinkOption[0])) {
         this.loadSettingsFile(legacyDefaultConfigFile.toFile(), shouldLoadLegacySettings);
      }

      File defaultConfigFile = mainConfigFile.getParent().resolveSibling("defaultconfigs").resolve(mainConfigFile.getFileName()).toFile();
      if (defaultConfigFile.exists()) {
         this.loadSettingsFile(defaultConfigFile, shouldLoadLegacySettings);
      }

   }

   public void loadSettings(boolean shouldLoadLegacySettings) throws IOException {
      this.loadDefaultSettings(shouldLoadLegacySettings);
      Path mainConfigFile = this.modMain.getConfigFile();
      Path configFolderPath = mainConfigFile.getParent();
      if (!Files.exists(configFolderPath, new LinkOption[0])) {
         Files.createDirectories(configFolderPath);
      }

      if (Files.exists(mainConfigFile, new LinkOption[0])) {
         this.loadSettingsFile(mainConfigFile.toFile(), shouldLoadLegacySettings);
      }

      this.saveSettings();
   }

   private void loadSettingsFile(File file, boolean shouldLoadLegacySettings) throws IOException {
      BufferedReader reader = null;

      try {
         reader = new BufferedReader(new FileReader(file));
         StringBuilder legacyWaypointLinesBuilder = new StringBuilder();
         StringBuilder legacyEncodedInfoDisplayConfigBuilder = new StringBuilder();

         String s;
         while((s = reader.readLine()) != null) {
            if (!this.modMain.getHudIO().load(s, shouldLoadLegacySettings)) {
               String[] args = s.split(":");
               if (!args[0].equals("waypoint") && !args[0].equals("world")) {
                  if (shouldLoadLegacySettings) {
                     try {
                        if (args[0].equalsIgnoreCase("interface") && args[1].equals("gui.xaero_minimap")) {
                           BuiltInHudModules.MINIMAP.setTransform(this.modMain.getHud().getOldSystemCompatibility().loadOldTransform(args));
                        } else if (!args[0].equals("infoDisplayOrder") && !args[0].equals("infoDisplay")) {
                           this.readSetting(args);
                        } else {
                           legacyEncodedInfoDisplayConfigBuilder.append(s).append("\n");
                        }
                     } catch (Exception var12) {
                        MinimapLogs.LOGGER.info("Skipping setting:" + args[0]);
                     }
                  }
               } else {
                  legacyWaypointLinesBuilder.append(s).append("\n");
               }
            }
         }

         if (!legacyEncodedInfoDisplayConfigBuilder.isEmpty()) {
            InfoDisplayManagerConfigData legacyInfoDisplayConfig = HudMod.INSTANCE.getMinimap().getInfoDisplays().getIo().decode(legacyEncodedInfoDisplayConfigBuilder.toString());
            HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getCurrentProfile().set(MinimapProfiledConfigOptions.INFO_DISPLAY_CONFIG, legacyInfoDisplayConfig);
         }

         if (!legacyWaypointLinesBuilder.isEmpty()) {
            this.loadedWaypointLines = legacyWaypointLinesBuilder.toString();
         }
      } finally {
         if (reader != null) {
            reader.close();
         }

      }

   }

   public static String getTranslation(boolean o) {
      return class_1074.method_4662("gui.xaero_" + (o ? "on" : "off"), new Object[0]);
   }

   /** @deprecated */
   @Deprecated
   private void refreshScreen() {
      GuiUtils.refreshScreenBase();
   }

   public boolean minimapDisabled() {
      return (serverSettings & 1) != 1;
   }

   public boolean caveMapsDisabled() {
      if (HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getServerSynced().isChannelPresentOnServer()) {
         return false;
      } else {
         return (serverSettings & 16384) != 16384 || class_310.method_1551().field_1687 != null && (!MinimapClientWorldDataHelper.getCurrentWorldData().getSyncedRules().allowCaveModeOnServer && class_310.method_1551().field_1687.method_27983() != class_1937.field_25180 || !MinimapClientWorldDataHelper.getCurrentWorldData().getSyncedRules().allowNetherCaveModeOnServer && class_310.method_1551().field_1687.method_27983() == class_1937.field_25180);
      }
   }

   public boolean showWaypointsDisabled() {
      return (serverSettings & 65536) != 65536;
   }

   public boolean deathpointsDisabled() {
      return (serverSettings & 2097152) == 0;
   }

   public void resetServerSettings() {
      serverSettings = defaultSettings;
   }

   public static void setServerSettings() {
   }

   public static boolean canEditIngameSettings() {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      return minimapSession != null && minimapSession.getWorldState().getAutoWorldPath() != null;
   }

   public EntityRadarBackwardsCompatibilityConfig getEntityRadarBackwardsCompatibilityConfig() {
      return this.entityRadarBackwardsCompatibilityConfig;
   }

   public void resetEntityRadarBackwardsCompatibilityConfig() {
      this.entityRadarBackwardsCompatibilityConfig = new EntityRadarBackwardsCompatibilityConfig();
      this.foundOldRadarSettings = false;
   }

   public boolean foundOldRadarSettings() {
      return this.foundOldRadarSettings;
   }

   public String getLoadedWaypointLines() {
      return this.loadedWaypointLines;
   }

   public void removeLoadedWaypointLines() {
      this.loadedWaypointLines = null;
   }
}
