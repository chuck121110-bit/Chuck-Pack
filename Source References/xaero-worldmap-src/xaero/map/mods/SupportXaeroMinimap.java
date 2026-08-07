package xaero.map.mods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.class_1074;
import net.minecraft.class_11909;
import net.minecraft.class_1937;
import net.minecraft.class_2378;
import net.minecraft.class_2874;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5321;
import xaero.common.HudMod;
import xaero.common.effect.Effects;
import xaero.common.gui.GuiAddWaypoint;
import xaero.common.gui.GuiWaypoints;
import xaero.common.gui.GuiWorldTpCommand;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.common.mods.SupportXaeroWorldmap;
import xaero.hud.controls.key.KeyMappingController;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.render.element.RadarRenderer;
import xaero.hud.minimap.waypoint.WaypointSession;
import xaero.hud.minimap.waypoint.WaypointTeleport;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.world.MinimapDimensionHelper;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.minimap.world.state.MinimapWorldState;
import xaero.hud.minimap.world.state.MinimapWorldStateUpdater;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.lib.common.util.KeySortableByOther;
import xaero.map.WorldMap;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.element.HoveredMapElementHolder;
import xaero.map.element.MapElementGraphics;
import xaero.map.gui.GuiMap;
import xaero.map.misc.Misc;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointMenuRenderContext;
import xaero.map.mods.gui.WaypointMenuRenderProvider;
import xaero.map.mods.gui.WaypointMenuRenderer;
import xaero.map.mods.gui.WaypointRenderer;
import xaero.map.mods.minimap.element.MinimapElementGraphicsWrapper;
import xaero.map.mods.minimap.element.RadarRendererWrapperHelper;
import xaero.map.mods.minimap.tracker.system.MinimapSyncedPlayerTrackerSystem;
import xaero.map.radar.tracker.system.IPlayerTrackerSystem;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;

public class SupportXaeroMinimap {
   HudMod modMain;
   public int compatibilityVersion;
   private boolean deathpoints = true;
   private boolean refreshWaypoints = true;
   private MinimapWorld waypointWorld;
   private MinimapWorld mapWaypointWorld;
   private class_5321<class_1937> mapDimId;
   private double dimDiv;
   private WaypointSet waypointSet;
   private boolean allSets;
   private ArrayList<Waypoint> waypoints;
   private ArrayList<Waypoint> waypointsSorted;
   private WaypointMenuRenderer waypointMenuRenderer;
   private final WaypointRenderer waypointRenderer;
   private IPlayerTrackerSystem<?> minimapSyncedPlayerTrackerSystem;
   private MinimapWorld mouseBlockWaypointWorld;
   private MinimapWorld rightClickWaypointWorld;
   private MinimapElementGraphicsWrapper elementGraphicsWrapper;

   public SupportXaeroMinimap() {
      try {
         Class mmClassTest = Class.forName("xaero.pvp.BetterPVP");
         this.modMain = HudMod.INSTANCE;
         WorldMap.LOGGER.info("Xaero's WorldMap Mod: Better PVP found!");
      } catch (ClassNotFoundException var5) {
         try {
            Class mmClassTest = Class.forName("xaero.minimap.XaeroMinimap");
            this.modMain = HudMod.INSTANCE;
            WorldMap.LOGGER.info("Xaero's WorldMap Mod: Xaero's minimap found!");
         } catch (ClassNotFoundException var4) {
         }
      }

      if (this.modMain != null) {
         try {
            this.compatibilityVersion = SupportXaeroWorldmap.WORLDMAP_COMPATIBILITY_VERSION;
         } catch (NoSuchFieldError var3) {
         }

         if (this.compatibilityVersion < 3) {
            throw new RuntimeException("Xaero's Minimap 20.23.0 or newer required!");
         }

         this.elementGraphicsWrapper = new MinimapElementGraphicsWrapper();
      }

      this.waypointRenderer = WaypointRenderer.Builder.begin().setMinimap(this).setSymbolCreator(WorldMap.waypointSymbolCreator).build();
   }

   public void register() {
      WorldMap.playerTrackerSystemManager.register("minimap_synced", this.getMinimapSyncedPlayerTrackerSystem());
   }

   public ArrayList<Waypoint> convertWaypoints(double dimDiv) {
      if (this.waypointSet == null) {
         return null;
      } else {
         ArrayList<Waypoint> result = new ArrayList();
         if (!this.allSets) {
            this.convertSet(this.waypointSet, result, dimDiv);
         } else {
            for(WaypointSet set : this.waypointWorld.getIterableWaypointSets()) {
               this.convertSet(set, result, dimDiv);
            }
         }

         if (this.allSets || "gui.xaero_default".equals(this.waypointWorld.getCurrentWaypointSetId())) {
            for(ThirdPartyWaypoints thirdPartyWaypoints : this.waypointWorld.getContainer().getThirdPartyWaypointManager().getAll()) {
               if (thirdPartyWaypoints.isEnabled()) {
                  for(xaero.common.minimap.waypoints.Waypoint w : thirdPartyWaypoints.getWaypoints().values()) {
                     if (!w.isThirdPartyDeleted()) {
                        Waypoint converted = this.convertWaypoint(w, true, "gui.xaero_default", dimDiv);
                        result.add(converted);
                     }
                  }
               }
            }
         }

         ClientConfigManager minimapConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         this.deathpoints = (Boolean)minimapConfigManager.getEffective(MinimapProfiledConfigOptions.DEATHPOINTS);
         return result;
      }
   }

   private void convertSet(WaypointSet set, ArrayList<Waypoint> result, double dimDiv) {
      String setName = set.getName();
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      boolean showingDisabled = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.DISPLAY_DISABLED_WAYPOINTS);

      for(xaero.common.minimap.waypoints.Waypoint w : set.getWaypoints()) {
         if (showingDisabled || !w.isDisabled()) {
            result.add(this.convertWaypoint(w, true, setName, dimDiv));
         }
      }

   }

   public Waypoint convertWaypoint(xaero.common.minimap.waypoints.Waypoint w, boolean editable, String setName, double dimDiv) {
      return new Waypoint(w, editable, setName, dimDiv);
   }

   public void openWaypoint(GuiMap parent, Waypoint waypoint) {
      if (waypoint.isEditable()) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         ArrayList<xaero.common.minimap.waypoints.Waypoint> waypointsEdited = new ArrayList();
         waypointsEdited.add((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal());
         class_437 addScreen = new GuiAddWaypoint(this.modMain, minimapSession, parent, parent, waypointsEdited, this.waypointWorld.getContainer().getRoot().getPath(), this.waypointWorld, waypoint.getSetName(), false);
         class_310.method_1551().method_1507(addScreen);
      }
   }

   public void createWaypoint(GuiMap parent, int x, int y, int z, double coordDimensionScale, boolean rightClick) {
      if (this.waypointWorld != null) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         MinimapWorld coordSourceWaypointWorld = rightClick ? this.rightClickWaypointWorld : this.mouseBlockWaypointWorld;
         class_437 addScreen = new GuiAddWaypoint(this.modMain, minimapSession, parent, parent, new ArrayList(), this.waypointWorld.getContainer().getRoot().getPath(), this.waypointWorld, this.waypointWorld.getCurrentWaypointSetId(), true, true, x, y, z, coordDimensionScale, coordSourceWaypointWorld);
         class_310.method_1551().method_1507(addScreen);
      }
   }

   public void createTempWaypoint(int x, int y, int z, double mapDimensionScale, boolean rightClick) {
      if (this.waypointWorld != null) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         MinimapWorld coordSourceWaypointWorld = rightClick ? this.rightClickWaypointWorld : this.mouseBlockWaypointWorld;
         minimapSession.getWaypointSession().getTemporaryHandler().createTemporaryWaypoint(this.waypointWorld, x, y, z, y != 32767 && coordSourceWaypointWorld == this.waypointWorld, mapDimensionScale);
         this.requestWaypointsRefresh();
      }
   }

   public boolean canTeleport(MinimapWorld world) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      WaypointSession waypointSession = minimapSession.getWaypointSession();
      WaypointTeleport waypointTeleport = waypointSession.getTeleport();
      return world != null && waypointTeleport.canTeleport(waypointTeleport.isWorldTeleportable(world), world);
   }

   public void teleportToWaypoint(class_437 screen, Waypoint w) {
      this.teleportToWaypoint(screen, w, this.waypointWorld);
   }

   public void teleportToWaypoint(class_437 screen, Waypoint w, MinimapWorld world) {
      if (world != null) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         WaypointSession waypointSession = minimapSession.getWaypointSession();
         WaypointTeleport waypointTeleport = waypointSession.getTeleport();
         waypointTeleport.teleportToWaypoint((xaero.common.minimap.waypoints.Waypoint)w.getOriginal(), world, screen);
      }
   }

   public void disableWaypoint(Waypoint waypoint) {
      ((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal()).setDisabled(!((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal()).isDisabled());
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();

      try {
         minimapSession.getWorldManagerIO().saveWorld(this.waypointWorld);
      } catch (IOException e) {
         WorldMap.LOGGER.error("suppressed exception", e);
      }

      if (waypoint.isThirdParty()) {
         MinimapWorldRootContainer rootContainer = this.waypointWorld.getContainer().getRoot();
         rootContainer.getSession().getWorldManagerIO().getRootConfigIO().save(rootContainer);
      }
   }

   public void deleteWaypoint(Waypoint waypoint) {
      if (!waypoint.isThirdParty()) {
         if (!this.allSets) {
            this.waypointSet.remove((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal());
         } else {
            for(WaypointSet set : this.waypointWorld.getIterableWaypointSets()) {
               set.remove((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal());
            }
         }

         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();

         try {
            minimapSession.getWorldManagerIO().saveWorld(this.waypointWorld);
         } catch (IOException e) {
            WorldMap.LOGGER.error("suppressed exception", e);
         }
      } else {
         ((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal()).setThirdPartyDeleted(true);
         MinimapWorldRootContainer rootContainer = this.waypointWorld.getContainer().getRoot();
         rootContainer.getSession().getWorldManagerIO().getRootConfigIO().save(rootContainer);
      }

      this.waypoints.remove(waypoint);
      this.waypointsSorted.remove(waypoint);
      this.waypointMenuRenderer.updateFilteredList();
   }

   public void checkWaypoints(boolean multiplayer, class_5321<class_1937> dimId, String multiworldId, int width, int height, GuiMap screen, MapWorld mapWorld, class_2378<class_2874> dimensionTypes) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      MinimapWorldManager worldManager = minimapSession.getWorldManager();
      MinimapWorldState worldState = minimapSession.getWorldState();
      MinimapWorldStateUpdater worldStateUpdater = minimapSession.getWorldStateUpdater();
      MinimapDimensionHelper dimensionHelper = minimapSession.getDimensionHelper();
      XaeroPath containerPath = worldState.getAutoRootContainerPath().resolve(dimensionHelper.getDimensionDirectoryName(dimId));
      XaeroPath mapBasedWorldPath = containerPath.resolve(!multiplayer ? "waypoints" : multiworldId);
      this.mapWaypointWorld = worldManager.getWorld(mapBasedWorldPath);
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      MinimapWorld checkingWaypointWorld;
      if ((Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.ONLY_CURRENT_MAP_WAYPOINTS)) {
         checkingWaypointWorld = this.mapWaypointWorld;
      } else {
         checkingWaypointWorld = worldManager.getCurrentWorld();
      }

      class_310 mc = class_310.method_1551();
      if (Misc.hasEffect(mc.field_1724, Effects.NO_WAYPOINTS) || Misc.hasEffect(mc.field_1724, Effects.NO_WAYPOINTS_HARMFUL)) {
         checkingWaypointWorld = null;
      }

      boolean shouldRefresh = this.refreshWaypoints;
      if (dimId != this.mapDimId) {
         shouldRefresh = true;
         this.mapDimId = dimId;
      }

      if (checkingWaypointWorld != this.waypointWorld) {
         this.waypointWorld = checkingWaypointWorld;
         screen.closeRightClick();
         if (screen.waypointMenu) {
            screen.method_25423(width, height);
         }

         shouldRefresh = true;
      }

      WaypointSet checkingSet = checkingWaypointWorld == null ? null : checkingWaypointWorld.getCurrentWaypointSet();
      if (checkingSet != this.waypointSet) {
         this.waypointSet = checkingSet;
         shouldRefresh = true;
      }

      ClientConfigManager minimapConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      boolean renderAllSetsConfig = (Boolean)minimapConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINTS_ALL_SETS);
      if (this.allSets != renderAllSetsConfig) {
         this.allSets = renderAllSetsConfig;
         shouldRefresh = true;
      }

      if (shouldRefresh) {
         this.dimDiv = this.waypointWorld == null ? (double)1.0F : this.getDimensionDivision(mapWorld, dimensionTypes, dimensionHelper, this.waypointWorld.getContainer().getPath(), dimId);
         this.waypoints = this.convertWaypoints(this.dimDiv);
         if (this.waypoints == null) {
            this.waypointsSorted = null;
         } else {
            Collections.sort(this.waypoints);
            this.waypointsSorted = new ArrayList();
            ArrayList<KeySortableByOther<Waypoint>> sortingList = new ArrayList();

            for(Waypoint w : this.waypoints) {
               sortingList.add(new KeySortableByOther(w, new Comparable[]{w.getComparisonName(), w.getName()}));
            }

            Collections.sort(sortingList);

            for(KeySortableByOther<Waypoint> e : sortingList) {
               this.waypointsSorted.add((Waypoint)e.getKey());
            }
         }

         this.waypointMenuRenderer.updateFilteredList();
      }

      this.refreshWaypoints = false;
   }

   private double getDimensionDivision(MapWorld mapWorld, class_2378<class_2874> dimensionTypes, MinimapDimensionHelper dimensionHelper, XaeroPath worldContainerID, class_5321<class_1937> mapDimId) {
      if (worldContainerID != null && class_310.method_1551().field_1687 != null) {
         String dimPart = worldContainerID.getLastNode();
         class_5321<class_1937> waypointDimId = dimensionHelper.getDimensionKeyForDirectoryName(dimPart);
         MapDimension waypointMapDimension = mapWorld.getDimension(waypointDimId);
         MapDimension mapDimension = mapWorld.getDimension(mapDimId);
         class_2874 waypointDimType = MapDimension.getDimensionType(waypointMapDimension, waypointDimId, dimensionTypes);
         class_2874 mapDimType = MapDimension.getDimensionType(mapDimension, mapDimId, dimensionTypes);
         double waypointDimScale = waypointDimType == null ? (double)1.0F : waypointDimType.comp_646();
         double mapDimScale = mapDimType == null ? (double)1.0F : mapDimType.comp_646();
         return mapDimScale / waypointDimScale;
      } else {
         return (double)1.0F;
      }
   }

   public HoveredMapElementHolder<?, ?> renderWaypointsMenu(class_332 guiGraphics, GuiMap gui, double scale, int width, int height, int mouseX, int mouseY, boolean leftMousePressed, boolean leftMouseClicked, HoveredMapElementHolder<?, ?> hovered, class_310 mc) {
      return this.waypointMenuRenderer.renderMenu(guiGraphics, gui, scale, width, height, mouseX, mouseY, leftMousePressed, leftMouseClicked, hovered, mc);
   }

   public void requestWaypointsRefresh() {
      this.refreshWaypoints = true;
   }

   public class_304 getWaypointKeyBinding() {
      return MinimapKeyMappings.ADD_WAYPOINT;
   }

   public class_304 getTempWaypointKeyBinding() {
      return MinimapKeyMappings.TEMPORARY_WAYPOINT;
   }

   public class_304 getTempWaypointsMenuKeyBinding() {
      return MinimapKeyMappings.WAYPOINT_MENU;
   }

   public void onMapKeyPressed(class_3675.class_307 type, int code, GuiMap screen) {
      class_304 kb = null;
      if (KeyMappingUtils.inputMatches(type, code, this.getToggleRadarKey(), 0)) {
         screen.onRadarButton(screen.getRadarButton());
      }

      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.TOGGLE_MAP_WAYPOINTS, 0)) {
         screen.onRenderWaypointsButton((class_4185)null);
      }

      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.REVERSE_ENTITY_RADAR, 0)) {
         MinimapKeyMappings.REVERSE_ENTITY_RADAR.method_23481(true);
      }

      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.SWITCH_WAYPOINT_SET, 0)) {
         kb = MinimapKeyMappings.SWITCH_WAYPOINT_SET;
      }

      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.RENDER_ALL_SETS, 0)) {
         kb = MinimapKeyMappings.RENDER_ALL_SETS;
      }

      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.WAYPOINT_MENU, 0)) {
         kb = MinimapKeyMappings.WAYPOINT_MENU;
      }

      class_304 minimapSettingsKB = (class_304)this.modMain.getSettingsKey();
      if (KeyMappingUtils.inputMatches(type, code, minimapSettingsKB, 0)) {
         kb = minimapSettingsKB;
      }

      class_304 listPlayerAlternative = this.getMinimapListPlayersAlternative();
      if (listPlayerAlternative != null && KeyMappingUtils.inputMatches(type, code, listPlayerAlternative, 0)) {
         listPlayerAlternative.method_23481(true);
      }

      class_310 mc = class_310.method_1551();
      if (kb != null) {
         if (kb == MinimapKeyMappings.WAYPOINT_MENU) {
            this.openWaypointsMenu(mc, screen);
            return;
         }

         if (minimapSettingsKB != null && kb == minimapSettingsKB) {
            mc.method_1507(this.getSettingsScreen(screen));
            return;
         }

         this.handleMinimapKeyBinding(kb, screen);
      }

   }

   public boolean onMapKeyReleased(class_3675.class_307 type, int code, GuiMap screen) {
      boolean result = false;
      if (KeyMappingUtils.inputMatches(type, code, MinimapKeyMappings.REVERSE_ENTITY_RADAR, 0)) {
         MinimapKeyMappings.REVERSE_ENTITY_RADAR.method_23481(false);
         result = true;
      }

      class_304 listPlayerAlternative = this.getMinimapListPlayersAlternative();
      if (listPlayerAlternative != null && KeyMappingUtils.inputMatches(type, code, listPlayerAlternative, 0)) {
         listPlayerAlternative.method_23481(false);
         result = true;
      }

      return result;
   }

   public void handleMinimapKeyBinding(class_304 kb, GuiMap screen) {
      KeyMappingController controller = this.modMain.getKeyMappingControllers().getController(kb);

      for(KeyMappingFunction keyFunction : controller) {
         if (!keyFunction.isHeld()) {
            keyFunction.onPress();
         }
      }

      for(KeyMappingFunction keyFunction : controller) {
         if (!keyFunction.isHeld()) {
            keyFunction.onRelease();
         }
      }

      if ((kb == MinimapKeyMappings.SWITCH_WAYPOINT_SET || kb == MinimapKeyMappings.RENDER_ALL_SETS) && screen.waypointMenu) {
         screen.method_25423(screen.field_22789, screen.field_22790);
      }

   }

   public void drawSetChange(class_332 guiGraphics) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      this.modMain.getMinimap().getWaypointMapRenderer().drawSetChange(minimapSession, guiGraphics, class_310.method_1551().method_22683());
   }

   public class_437 getSettingsScreen(class_437 current) {
      return this.modMain.getGuiHelper().getMinimapSettingsFromScreen(current);
   }

   public String getControlsTooltip() {
      return class_1074.method_4662("gui.xaero_box_controls_minimap", new Object[]{KeyMappingUtils.getKeyName(MinimapKeyMappings.ADD_WAYPOINT), KeyMappingUtils.getKeyName(MinimapKeyMappings.TEMPORARY_WAYPOINT), KeyMappingUtils.getKeyName(MinimapKeyMappings.SWITCH_WAYPOINT_SET), KeyMappingUtils.getKeyName(MinimapKeyMappings.RENDER_ALL_SETS), KeyMappingUtils.getKeyName(MinimapKeyMappings.WAYPOINT_MENU)});
   }

   public void onMapMouseRelease(class_11909 event) {
      this.waypointMenuRenderer.onMapMouseRelease(event);
   }

   public void onMapConstruct() {
      this.waypointMenuRenderer = new WaypointMenuRenderer(new WaypointMenuRenderContext(), new WaypointMenuRenderProvider(this), this.waypointRenderer);
   }

   public void onMapInit(GuiMap mapScreen, class_310 mc, int width, int height) {
      this.waypointMenuRenderer.onMapInit(mapScreen, mc, width, height, this.waypointWorld, this.modMain, (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession());
   }

   public ArrayList<Waypoint> getWaypointsSorted() {
      return this.waypointsSorted;
   }

   public boolean waypointExists(Waypoint w) {
      return this.waypoints != null && this.waypoints.contains(w);
   }

   public void toggleTemporaryWaypoint(Waypoint waypoint) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      ((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal()).setTemporary(!((xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal()).isTemporary());
      if (!waypoint.isThirdParty()) {
         try {
            minimapSession.getWorldManagerIO().saveWorld(this.waypointWorld);
         } catch (IOException e) {
            WorldMap.LOGGER.error("suppressed exception", e);
         }

      }
   }

   public void openWaypointsMenu(class_310 mc, GuiMap screen) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession.getWorldState().getAutoWorldPath() != null) {
         mc.method_1507(new GuiWaypoints(this.modMain, minimapSession, screen, screen));
      }
   }

   public boolean hidingWaypointCoordinates() {
      ClientConfigManager minimapConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return (Boolean)minimapConfigManager.getEffective(MinimapProfiledConfigOptions.HIDE_WAYPOINT_COORDINATES);
   }

   public void shareWaypoint(Waypoint waypoint, GuiMap screen, MinimapWorld world) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      minimapSession.getWaypointSession().getSharing().shareWaypoint(screen, (xaero.common.minimap.waypoints.Waypoint)waypoint.getOriginal(), world);
   }

   public void shareLocation(GuiMap guiMap, int rightClickX, int rightClickY, int rightClickZ) {
      int wpColor = (int)((double)MinimapConfigConstants.COLORS.length * Math.random());
      xaero.common.minimap.waypoints.Waypoint minimapLocationWaypoint = new xaero.common.minimap.waypoints.Waypoint(rightClickX, rightClickY == 32767 ? 0 : rightClickY, rightClickZ, "Shared Location", "S", wpColor, 0, false, rightClickY != 32767);
      Waypoint locationWaypoint = this.convertWaypoint(minimapLocationWaypoint, false, "", (double)1.0F);
      this.shareWaypoint(locationWaypoint, guiMap, this.rightClickWaypointWorld);
   }

   public MinimapWorld getMapWaypointWorld() {
      return this.mapWaypointWorld;
   }

   public MinimapWorld getWaypointWorld() {
      return this.waypointWorld;
   }

   public double getDimDiv() {
      return this.dimDiv;
   }

   public int getArrowColorIndex() {
      ClientConfigManager minimapConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return (Integer)minimapConfigManager.getEffective(MinimapProfiledConfigOptions.ARROW_COLOR);
   }

   public float[] getArrowColor() {
      int arrowColour = this.getArrowColorIndex();
      return arrowColour >= 0 && arrowColour < MinimapConfigConstants.ARROW_COLORS.length ? MinimapConfigConstants.ARROW_COLORS[arrowColour] : null;
   }

   public String getSubWorldNameToRender() {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      boolean onlyCurrentMapWaypoints = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.ONLY_CURRENT_MAP_WAYPOINTS);
      if (!onlyCurrentMapWaypoints && this.waypointWorld != null) {
         return this.waypointWorld != this.mapWaypointWorld ? class_1074.method_4662("gui.xaero_wm_using_custom_subworld", new Object[]{this.waypointWorld.getContainer().getSubName()}) : null;
      } else {
         return null;
      }
   }

   public void registerMinimapHighlighters(Object highlighterRegistry) {
   }

   public ArrayList<Waypoint> getWaypoints() {
      return this.waypoints;
   }

   public boolean getDeathpoints() {
      return this.deathpoints;
   }

   public WaypointRenderer getWaypointRenderer() {
      return this.waypointRenderer;
   }

   public WaypointMenuRenderer getWaypointMenuRenderer() {
      return this.waypointMenuRenderer;
   }

   public void onClearHighlightHash(int regionX, int regionZ) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         DimensionHighlighterHandler highlightHandler = minimapSession.getProcessor().getMinimapWriter().getDimensionHighlightHandler();
         if (highlightHandler != null) {
            highlightHandler.requestRefresh(regionX, regionZ);
         }
      }

   }

   public void createRadarRendererWrapper(Object radarRenderer) {
      (new RadarRendererWrapperHelper()).createWrapper(this.modMain, (RadarRenderer)radarRenderer);
   }

   public class_304 getToggleRadarKey() {
      return MinimapKeyMappings.TOGGLE_RADAR;
   }

   public void onClearHighlightHashes() {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         DimensionHighlighterHandler highlightHandler = minimapSession.getProcessor().getMinimapWriter().getDimensionHighlightHandler();
         if (highlightHandler != null) {
            highlightHandler.requestRefresh();
         }
      }

   }

   public class_304 getToggleAllyPlayersKey() {
      return MinimapKeyMappings.TOGGLE_TRACKED_PLAYERS_MAP;
   }

   public class_304 getToggleClaimsKey() {
      return MinimapKeyMappings.TOGGLE_OPAC_CLAIMS;
   }

   public class_304 getToggleWaypointsKeybinding() {
      return MinimapKeyMappings.TOGGLE_MAP_WAYPOINTS;
   }

   public void onSessionFinalized() {
      this.waypointWorld = null;
      this.mapWaypointWorld = null;
   }

   public void openWaypointWorldTeleportCommandScreen(class_437 parent, class_437 escape) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         XaeroPath containerId = minimapSession.getWorldState().getAutoRootContainerPath();
         MinimapWorldRootContainer container = minimapSession.getWorldManager().getWorldContainerNullable(containerId).getRoot();
         if (container != null) {
            class_310.method_1551().method_1507(new GuiWorldTpCommand(this.modMain, parent, escape, container));
         }

      }
   }

   public class_304 getMinimapListPlayersAlternative() {
      return MinimapKeyMappings.ALTERNATIVE_LIST_PLAYERS;
   }

   public int getCaveStart(int defaultWorldMapStart, boolean isMapScreen) {
      if (!this.modMain.getSettings().getMinimap()) {
         return defaultWorldMapStart;
      } else if (!MinimapConfigClientUtils.getEffectiveCaveModeAllowed()) {
         return isMapScreen ? defaultWorldMapStart : Integer.MAX_VALUE;
      } else {
         int usedCaving = this.getUsedCaving();
         if (usedCaving == Integer.MAX_VALUE) {
            ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
            SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
            return (Integer)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.CAVE_MODE_START);
         } else {
            return usedCaving;
         }
      }
   }

   public int getUsedCaving() {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      return minimapSession != null ? minimapSession.getProcessor().getMinimapWriter().getLoadedCaving() : Integer.MAX_VALUE;
   }

   public boolean isFairPlay() {
      return this.modMain.isFairPlay();
   }

   public IPlayerTrackerSystem<?> getMinimapSyncedPlayerTrackerSystem() {
      if (this.minimapSyncedPlayerTrackerSystem == null) {
         this.minimapSyncedPlayerTrackerSystem = new MinimapSyncedPlayerTrackerSystem(this);
      }

      return this.minimapSyncedPlayerTrackerSystem;
   }

   public void onBlockHover() {
      this.mouseBlockWaypointWorld = this.mapWaypointWorld;
   }

   public void onRightClick() {
      this.rightClickWaypointWorld = this.mouseBlockWaypointWorld;
   }

   public MinimapElementGraphicsWrapper wrapElementGraphics(MapElementGraphics graphics) {
      return this.elementGraphicsWrapper.setGraphics(graphics);
   }
}
