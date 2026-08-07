package xaero.map.gui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10366;
import net.minecraft.class_1044;
import net.minecraft.class_1074;
import net.minecraft.class_10799;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1921;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_287;
import net.minecraft.class_2874;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_3675;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_5321;
import net.minecraft.class_6379;
import net.minecraft.class_6599;
import net.minecraft.class_8030;
import net.minecraft.class_3675.class_307;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.client.graphics.IGuiGraphics;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.shader.WorldMapShaderHelper;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.graphics.util.TextureUtils;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.lib.common.util.MathUtils;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.animation.Animation;
import xaero.map.animation.SinAnimation;
import xaero.map.animation.SlowingAnimation;
import xaero.map.common.config.WorldMapConfigConstants;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.controls.ControlsRegister;
import xaero.map.core.IWorldMapClientPlayNetHandler;
import xaero.map.core.IWorldMapMinecraftClient;
import xaero.map.effects.Effects;
import xaero.map.element.HoveredMapElementHolder;
import xaero.map.element.MapElementMenuHitbox;
import xaero.map.entity.util.EntityUtil;
import xaero.map.graphics.CustomRenderTypes;
import xaero.map.graphics.ImprovedFramebuffer;
import xaero.map.graphics.MapRenderHelper;
import xaero.map.graphics.OpenGlHelper;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.gui.dropdown.rightclick.GuiRightClickMenu;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.gui.util.GuiUtils;
import xaero.map.misc.Misc;
import xaero.map.misc.OptimizedMath;
import xaero.map.mods.SupportMods;
import xaero.map.mods.gui.Waypoint;
import xaero.map.radar.tracker.PlayerTeleporter;
import xaero.map.radar.tracker.PlayerTrackerMapElement;
import xaero.map.region.BranchLeveledRegion;
import xaero.map.region.LayeredRegionManager;
import xaero.map.region.LeveledRegion;
import xaero.map.region.MapBlock;
import xaero.map.region.MapRegion;
import xaero.map.region.MapTile;
import xaero.map.region.MapTileChunk;
import xaero.map.region.Overlay;
import xaero.map.region.texture.RegionTexture;
import xaero.map.teleport.MapTeleporter;
import xaero.map.util.DistanceUtils;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;

public class GuiMap extends ScreenBase implements IRightClickableElement {
   private static final class_2561 FULL_RELOAD_IN_PROGRESS = class_2561.method_43471("gui.xaero_full_reload_in_progress");
   private static final class_2561 UNKNOWN_DIMENSION_TYPE1 = class_2561.method_43471("gui.xaero_unknown_dimension_type1");
   private static final class_2561 UNKNOWN_DIMENSION_TYPE2 = class_2561.method_43471("gui.xaero_unknown_dimension_type2");
   private static final double ZOOM_STEP = 1.2;
   private static final int white = -1;
   private static final int black = -16777216;
   private static int lastAmountOfRegionsViewed = 1;
   private long loadingAnimationStart;
   private class_1297 player;
   private double screenScale = (double)0.0F;
   private int mouseDownPosX = -1;
   private int mouseDownPosY = -1;
   private double mouseDownCameraX = (double)-1.0F;
   private double mouseDownCameraZ = (double)-1.0F;
   private int mouseCheckPosX = -1;
   private int mouseCheckPosY = -1;
   private long mouseCheckTimeNano = -1L;
   private int prevMouseCheckPosX = -1;
   private int prevMouseCheckPosY = -1;
   private long prevMouseCheckTimeNano = -1L;
   private double cameraX = (double)0.0F;
   private double cameraZ = (double)0.0F;
   private boolean shouldResetCameraPos;
   private int[] cameraDestination = null;
   private SlowingAnimation cameraDestinationAnimX = null;
   private SlowingAnimation cameraDestinationAnimZ = null;
   private double scale;
   private double userScale;
   private static double destScale = (double)3.0F;
   private boolean pauseZoomKeys;
   private int lastZoomMethod;
   private double prevPlayerDimDiv;
   private HoveredMapElementHolder<?, ?> viewed = null;
   private boolean viewedInList;
   private HoveredMapElementHolder<?, ?> viewedOnMousePress = null;
   private boolean overWaypointsMenu;
   private Animation zoomAnim;
   public boolean waypointMenu = false;
   private boolean overPlayersMenu;
   public boolean playersMenu = false;
   private static ImprovedFramebuffer primaryScaleFBO = null;
   private static ImprovedFramebuffer immediateRenderFBO = null;
   private float[] colourBuffer = new float[4];
   private ArrayList<MapRegion> regionBuffer = new ArrayList();
   private ArrayList<BranchLeveledRegion> branchRegionBuffer = new ArrayList();
   private boolean prevWaitingForBranchCache = true;
   private boolean prevLoadingLeaves = true;
   private class_5321<class_1937> lastNonNullViewedDimensionId;
   private class_5321<class_1937> lastViewedDimensionId;
   private String lastViewedMultiworldId;
   private int mouseBlockPosX;
   private int mouseBlockPosY;
   private int mouseBlockPosZ;
   private class_5321<class_1937> mouseBlockDim;
   private double mouseBlockCoordinateScale = (double)1.0F;
   private long lastStartTime;
   private final GuiMapSwitching mapSwitchingGui;
   private MapMouseButtonPress leftMouseButton;
   private MapMouseButtonPress rightMouseButton;
   private MapProcessor mapProcessor;
   private MapDimension dimensionOnInit;
   private MapDimension futureDimensionOnInit;
   public boolean noUploadingLimits;
   private boolean[] waitingForBranchCache = new boolean[1];
   private class_4185 settingsButton;
   private class_4185 exportButton;
   private class_4185 waypointsButton;
   private class_4185 renderWaypointsButton;
   private class_4185 playersButton;
   private class_4185 radarButton;
   private class_4185 claimsButton;
   private class_4185 zoomInButton;
   private class_4185 zoomOutButton;
   private class_4185 keybindingsButton;
   private class_4185 caveModeButton;
   private class_4185 dimensionToggleButton;
   private class_4185 hopButton;
   private class_4185 attachedCameraButton;
   private class_4185 buttonPressed;
   private GuiRightClickMenu rightClickMenu;
   private int rightClickScreenWidth;
   private int rightClickScreenHeight;
   private int rightClickX;
   private int rightClickY;
   private int rightClickZ;
   private class_5321<class_1937> rightClickDim;
   private double rightClickCoordinateScale;
   private boolean lastFrameRenderedRootTextures;
   private MapTileSelection mapTileSelection;
   private boolean tabPressed;
   private GuiCaveModeOptions caveModeOptions;
   private int currentUpdateSliceIndex;
   private boolean hopMenu;
   private class_342 hopInputBox;
   private static boolean attachedCamera;
   public boolean shouldReinit;
   public static boolean hiddenUI;
   private static final Matrix4f identityMatrix = new Matrix4f();

   public GuiMap(class_437 parent, class_437 escape, MapProcessor mapProcessor, class_1297 player) {
      super(parent, escape, class_2561.method_43471("gui.xaero_world_map_screen"));
      this.player = player;
      this.shouldResetCameraPos = true;
      this.leftMouseButton = new MapMouseButtonPress();
      this.rightMouseButton = new MapMouseButtonPress();
      this.mapSwitchingGui = new GuiMapSwitching(mapProcessor);
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      boolean openingAnimationConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.OPENING_ANIMATION);
      this.userScale = destScale * (double)(openingAnimationConfig ? 1.5F : 1.0F);
      this.zoomAnim = new SlowingAnimation(this.userScale, destScale, 0.88, destScale * 0.001);
      this.mapProcessor = mapProcessor;
      this.caveModeOptions = new GuiCaveModeOptions();
      if (SupportMods.minimap()) {
         SupportMods.xaeroMinimap.onMapConstruct();
      }

   }

   private double getScaleMultiplier(int screenShortSide) {
      return screenShortSide <= 1080 ? (double)1.0F : (double)screenShortSide / (double)1080.0F;
   }

   public <T extends class_364 & class_4068 & class_6379> T method_37063(T guiEventListener) {
      return (T)super.method_37063(guiEventListener);
   }

   public <T extends class_364 & class_4068 & class_6379> T addButton(T guiEventListener) {
      return (T)this.method_37063(guiEventListener);
   }

   public <T extends class_364 & class_6379> T method_25429(T guiEventListener) {
      return (T)super.method_25429(guiEventListener);
   }

   public void method_25426() {
      super.method_25426();
      this.shouldReinit = false;
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      MapWorld mapWorld = this.mapProcessor.getMapWorld();
      this.dimensionOnInit = mapWorld == null ? null : mapWorld.getCurrentDimension();
      this.futureDimensionOnInit = mapWorld == null ? null : mapWorld.getFutureDimension();
      this.tabPressed = false;
      boolean waypointsEnabled = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINTS);
      this.waypointMenu = this.waypointMenu && waypointsEnabled;
      this.mapSwitchingGui.init(this, this.field_22787, this.field_22789, this.field_22790);
      boolean effectiveCaveModeAllowed = WorldMapClientConfigUtils.getEffectiveCaveModeAllowed();
      Tooltip caveModeButtonTooltip = new Tooltip(class_2561.method_43471(effectiveCaveModeAllowed ? "gui.xaero_box_cave_mode" : "gui.xaero_box_cave_mode_not_allowed"));
      this.caveModeButton = new GuiTexturedButton(0, this.field_22790 - 40, 20, 20, 229, 64, 16, 16, WorldMap.guiTextures, this::onCaveModeButton, () -> caveModeButtonTooltip, 256, 256);
      this.caveModeButton.field_22763 = effectiveCaveModeAllowed;
      this.addButton(this.caveModeButton);
      this.caveModeOptions.onInit(this, this.mapProcessor);
      Tooltip dimensionToggleButtonTooltip = new Tooltip(class_2561.method_43469("gui.xaero_dimension_toggle_button", new Object[]{KeyMappingUtils.getKeyName(ControlsRegister.keyToggleDimension)}));
      this.dimensionToggleButton = new GuiTexturedButton(0, this.field_22790 - 60, 20, 20, 197, 80, 16, 16, WorldMap.guiTextures, this::onDimensionToggleButton, () -> dimensionToggleButtonTooltip, 256, 256);
      this.addButton(this.dimensionToggleButton);
      Tooltip hopButtonTooltip;
      if (!this.hopMenu) {
         hopButtonTooltip = new Tooltip(class_2561.method_43471("gui.xaero_hop_button"));
      } else {
         hopButtonTooltip = new Tooltip(class_2561.method_43471("gui.xaero_hop_field_tooltip"));
      }

      this.hopButton = new GuiTexturedButton(0, this.field_22790 - 100, 20, 20, 213, 80, 16, 16, WorldMap.guiTextures, this::onHopButton, () -> hopButtonTooltip, 256, 256);
      this.addButton(this.hopButton);
      this.hopInputBox = null;
      if (this.hopMenu) {
         this.hopInputBox = new class_342(this.field_22793, 20, this.field_22790 - 100, 75, 20, class_2561.method_43471("gui.xaero_hop_field"));
         this.method_37063(this.hopInputBox);
      }

      Tooltip attachedCameraButtonTooltip;
      if (attachedCamera) {
         attachedCameraButtonTooltip = new Tooltip(class_2561.method_43471("gui.xaero_attached_camera_button_enabled"));
      } else {
         attachedCameraButtonTooltip = new Tooltip(class_2561.method_43471("gui.xaero_attached_camera_button_disabled"));
      }

      this.attachedCameraButton = new GuiTexturedButton(0, this.field_22790 - 120, 20, 20, 229, 80 + (attachedCamera ? 0 : 16), 16, 16, WorldMap.guiTextures, this::onAttachedCameraButton, () -> attachedCameraButtonTooltip, 256, 256);
      this.addButton(this.attachedCameraButton);
      this.loadingAnimationStart = System.currentTimeMillis();
      if (SupportMods.minimap()) {
         SupportMods.xaeroMinimap.requestWaypointsRefresh();
      }

      this.screenScale = (double)class_310.method_1551().method_22683().method_4495();
      this.pauseZoomKeys = false;
      Tooltip openSettingsTooltip = new Tooltip(class_2561.method_43469("gui.xaero_box_open_settings", new Object[]{KeyMappingUtils.getKeyName(ControlsRegister.keyOpenSettings)}));
      this.addButton(this.settingsButton = new GuiTexturedButton(0, 0, 30, 30, 113, 0, 20, 20, WorldMap.guiTextures, this::onSettingsButton, () -> openSettingsTooltip, 256, 256));
      Tooltip waypointsTooltip;
      if (waypointsEnabled) {
         waypointsTooltip = new Tooltip(this.waypointMenu ? "gui.xaero_box_close_waypoints" : "gui.xaero_box_open_waypoints");
      } else {
         waypointsTooltip = new Tooltip(!SupportMods.minimap() ? "gui.xaero_box_waypoints_minimap_required" : "gui.xaero_box_waypoints_disabled");
      }

      Tooltip playersTooltip = new Tooltip(this.playersMenu ? "gui.xaero_box_close_players" : "gui.xaero_box_open_players");
      boolean displayClaimsConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.OPAC_CLAIMS);
      Tooltip claimsTooltip;
      if (SupportMods.pac()) {
         claimsTooltip = new Tooltip(class_2561.method_43469(displayClaimsConfig ? "gui.xaero_box_pac_displaying_claims" : "gui.xaero_box_pac_not_displaying_claims", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(SupportMods.xaeroPac.getPacClaimsKeyBinding())).method_27692(class_124.field_1077)}));
      } else {
         claimsTooltip = new Tooltip(class_2561.method_43471("gui.xaero_box_claims_pac_required"));
      }

      this.addButton(this.waypointsButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 20, 20, 20, 213, 0, 16, 16, WorldMap.guiTextures, this::onWaypointsButton, () -> waypointsTooltip, 256, 256));
      this.waypointsButton.field_22763 = waypointsEnabled;
      boolean renderWaypoints = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.RENDER_WAYPOINTS);
      boolean waypointsRenderServerEnforced = WorldMapClientConfigUtils.isOptionServerEnforced(WorldMapProfiledConfigOptions.RENDER_WAYPOINTS);
      Tooltip renderingWaypointsTooltip;
      if (waypointsRenderServerEnforced) {
         renderingWaypointsTooltip = new Tooltip(class_2561.method_43471("gui.xaero_box_rendering_waypoints_server_enforced").method_27692(class_124.field_1079), true);
      } else if (waypointsEnabled) {
         renderingWaypointsTooltip = new Tooltip(class_2561.method_43469(renderWaypoints ? "gui.xaero_box_rendering_waypoints" : "gui.xaero_box_not_rendering_waypoints", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(SupportMods.xaeroMinimap.getToggleWaypointsKeybinding())).method_27692(class_124.field_1077)}), true);
      } else {
         renderingWaypointsTooltip = new Tooltip(!SupportMods.minimap() ? "gui.xaero_box_waypoints_minimap_required" : "gui.xaero_box_waypoints_disabled");
      }

      this.addButton(this.renderWaypointsButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 40, 20, 20, renderWaypoints ? 229 : 213, 48, 16, 16, WorldMap.guiTextures, this::onRenderWaypointsButton, () -> renderingWaypointsTooltip, 256, 256));
      this.renderWaypointsButton.field_22763 = !waypointsRenderServerEnforced && waypointsEnabled;
      this.addButton(this.playersButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 60, 20, 20, 197, 32, 16, 16, WorldMap.guiTextures, this::onPlayersButton, () -> playersTooltip, 256, 256));
      boolean minimapRadarConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.MINIMAP_RADAR);
      Tooltip radarButtonTooltip = new Tooltip(class_2561.method_43469(minimapRadarConfig ? "gui.xaero_box_minimap_radar" : "gui.xaero_box_no_minimap_radar", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(SupportMods.minimap() ? SupportMods.xaeroMinimap.getToggleRadarKey() : null)).method_27692(class_124.field_1077)}));
      this.addButton(this.radarButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 80, 20, 20, minimapRadarConfig ? 213 : 229, 32, 16, 16, WorldMap.guiTextures, this::onRadarButton, () -> radarButtonTooltip, 256, 256));
      this.getRadarButton().field_22763 = SupportMods.minimap() && !WorldMapClientConfigUtils.isOptionServerEnforced(WorldMapProfiledConfigOptions.MINIMAP_RADAR);
      this.addButton(this.claimsButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 100, 20, 20, displayClaimsConfig ? 197 : 213, 64, 16, 16, WorldMap.guiTextures, this::onClaimsButton, () -> claimsTooltip, 256, 256));
      this.claimsButton.field_22763 = SupportMods.pac() && !WorldMapClientConfigUtils.isOptionServerEnforced(WorldMapProfiledConfigOptions.OPAC_CLAIMS);
      Tooltip exportButtonTooltip = new Tooltip("gui.xaero_box_export");
      this.addButton(this.exportButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 120, 20, 20, 133, 0, 16, 16, WorldMap.guiTextures, this::onExportButton, () -> exportButtonTooltip, 256, 256));
      Object[] var10003 = new Object[1];
      String var10006 = SupportMods.minimap() ? SupportMods.xaeroMinimap.getControlsTooltip() : "";
      var10003[0] = var10006 + (SupportMods.pac() ? SupportMods.xaeroPac.getControlsTooltip() : "");
      Tooltip controlsButtonTooltip = new Tooltip(class_1074.method_4662("gui.xaero_box_controls", var10003));
      controlsButtonTooltip.setStartWidth(400);
      this.addButton(this.keybindingsButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 140, 20, 20, 197, 0, 16, 16, WorldMap.guiTextures, this::onKeybindingsButton, () -> controlsButtonTooltip, 256, 256));
      Tooltip zoomInButtonTooltip = new Tooltip(class_2561.method_43469("gui.xaero_box_zoom_in", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(ControlsRegister.keyZoomIn)).method_27692(class_124.field_1077)}));
      this.zoomInButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 180, 20, 20, 165, 0, 16, 16, WorldMap.guiTextures, this::onZoomInButton, () -> zoomInButtonTooltip, 256, 256);
      Tooltip zoomOutButtonTooltip = new Tooltip(class_2561.method_43469("gui.xaero_box_zoom_out", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(ControlsRegister.keyZoomOut)).method_27692(class_124.field_1077)}));
      this.zoomOutButton = new GuiTexturedButton(this.field_22789 - 20, this.field_22790 - 160, 20, 20, 181, 0, 16, 16, WorldMap.guiTextures, this::onZoomOutButton, () -> zoomOutButtonTooltip, 256, 256);
      if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.ZOOM_BUTTONS)) {
         this.addButton(this.zoomOutButton);
         this.addButton(this.zoomInButton);
      }

      if (this.rightClickMenu != null) {
         if (this.field_22789 == this.rightClickScreenWidth && this.field_22790 == this.rightClickScreenHeight) {
            super.onDropdownOpen(this.rightClickMenu);
         } else {
            this.rightClickMenu.setClosed(true);
            this.rightClickMenu = null;
            this.rightClickScreenWidth = 0;
            this.rightClickScreenHeight = 0;
         }
      }

      if (SupportMods.minimap() && this.waypointMenu) {
         SupportMods.xaeroMinimap.onMapInit(this, this.field_22787, this.field_22789, this.field_22790);
      }

      if (this.playersMenu) {
         WorldMap.trackedPlayerMenuRenderer.onMapInit(this, this.field_22787, this.field_22789, this.field_22790);
      }

   }

   protected void method_56131() {
   }

   public void onRenderWaypointsButton(class_4185 b) {
      WorldMapClientConfigUtils.tryTogglingCurrentProfileOption(WorldMapProfiledConfigOptions.RENDER_WAYPOINTS);
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.renderWaypointsButton);
   }

   private void onCaveModeButton(class_4185 b) {
      this.caveModeOptions.toggle(this);
      this.method_25395(this.caveModeButton);
   }

   private void onDimensionToggleButton(class_4185 b) {
      this.mapProcessor.getMapWorld().toggleDimension(!hasShiftDown());
      String messageType = this.mapProcessor.getMapWorld().getCustomDimensionId() == null ? "gui.xaero_switched_to_current_dimension" : "gui.xaero_switched_to_dimension";
      class_2960 messageDimLoc = this.mapProcessor.getMapWorld().getFutureDimensionId() == null ? null : this.mapProcessor.getMapWorld().getFutureDimensionId().method_29177();
      this.mapProcessor.getMessageBox().addMessage(class_2561.method_43469(messageType, new Object[]{messageDimLoc.toString()}));
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.dimensionToggleButton);
   }

   private void onSettingsButton(class_4185 b) {
      this.field_22787.method_1507(new GuiWorldMapSettings(this, this, BuiltInEditConfigScreenContexts.CLIENT));
   }

   private void onKeybindingsButton(class_4185 b) {
      this.field_22787.method_1507(new class_6599(this, this.field_22787.field_1690));
   }

   private void onExportButton(class_4185 b) {
      this.field_22787.method_1507(new ExportScreen(this, this, this.mapProcessor, this.mapTileSelection));
   }

   private void toggleWaypointMenu() {
      if (this.playersMenu) {
         this.togglePlayerMenu();
      }

      this.waypointMenu = !this.waypointMenu;
      if (!this.waypointMenu) {
         SupportMods.xaeroMinimap.getWaypointMenuRenderer().onMenuClosed();
         this.unfocusAll();
      }

   }

   private void togglePlayerMenu() {
      if (this.waypointMenu) {
         this.toggleWaypointMenu();
      }

      this.playersMenu = !this.playersMenu;
      if (!this.playersMenu) {
         WorldMap.trackedPlayerMenuRenderer.onMenuClosed();
         this.unfocusAll();
      }

   }

   private void onPlayersButton(class_4185 b) {
      this.togglePlayerMenu();
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.playersButton);
   }

   public void onClaimsButton(class_4185 unused) {
      WorldMapClientConfigUtils.tryTogglingCurrentProfileOption(WorldMapProfiledConfigOptions.OPAC_CLAIMS);
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.claimsButton);
   }

   private void onWaypointsButton(class_4185 b) {
      this.toggleWaypointMenu();
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.waypointsButton);
   }

   public void onRadarButton(class_4185 b) {
      WorldMapClientConfigUtils.tryTogglingCurrentProfileOption(WorldMapProfiledConfigOptions.MINIMAP_RADAR);
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.radarButton);
   }

   private void onZoomInButton(class_4185 b) {
      this.buttonPressed = this.buttonPressed == null ? b : null;
   }

   private void onZoomOutButton(class_4185 b) {
      this.buttonPressed = this.buttonPressed == null ? b : null;
   }

   private void onHopButton(class_4185 button) {
      this.hopMenu = !this.hopMenu;
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.hopButton);
   }

   private void activateHopToCoords() {
      String rawValue = this.hopInputBox.method_1882();
      this.hopInputBox.method_1852("");
      String trimmedValue = rawValue.trim();
      if (trimmedValue.isEmpty()) {
         this.hopMenu = false;
         this.method_25423(this.field_22789, this.field_22790);
      } else {
         String[] args = trimmedValue.split("[ ,;:]+");
         class_2561 errorSource = class_2561.method_43471("gui.xaero_hop_error_source");
         if (args.length > 3) {
            this.mapProcessor.getMessageBox().addMessageWithSource(errorSource, class_2561.method_43471("gui.xaero_hop_error_too_many").method_27692(class_124.field_1079));
         } else {
            int zIndex = args.length - 1;
            String allowedCoordChars = "[^0-9-.]";
            String xString = args[0].replaceAll(allowedCoordChars, "").trim();
            String zString = args[zIndex].replaceAll(allowedCoordChars, "").trim();

            try {
               int x = (int)Math.floor(Double.parseDouble(xString));
               int z = (int)Math.floor(Double.parseDouble(zString));
               this.cameraDestination = new int[]{x, z};
               this.hopMenu = false;
               attachedCamera = false;
               this.method_25423(this.field_22789, this.field_22790);
            } catch (NumberFormatException var11) {
               this.mapProcessor.getMessageBox().addMessageWithSource(errorSource, class_2561.method_43471("gui.xaero_hop_error_not_numbers").method_27692(class_124.field_1079));
            }

         }
      }
   }

   private void onAttachedCameraButton(class_4185 button) {
      attachedCamera = !attachedCamera;
      this.method_25423(this.field_22789, this.field_22790);
      this.method_25395(this.attachedCameraButton);
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      if (this.isMapNavigationInput(class_307.field_1672, event.method_74245())) {
         return true;
      } else {
         boolean toReturn = !hiddenUI && super.method_25402(event, doubleClick);
         if (!toReturn) {
            if (event.method_74245() == 0) {
               if (this.method_25399() instanceof class_342) {
                  this.unfocusAll();
               }

               this.leftMouseButton.isDown = this.leftMouseButton.clicked = true;
               this.leftMouseButton.pressedAtX = (int)Misc.getMouseX(this.field_22787, SupportMods.vivecraft);
               this.leftMouseButton.pressedAtY = (int)Misc.getMouseY(this.field_22787, SupportMods.vivecraft);
            } else if (event.method_74245() == 1) {
               this.rightMouseButton.isDown = this.rightMouseButton.clicked = true;
               this.rightMouseButton.pressedAtX = (int)Misc.getMouseX(this.field_22787, SupportMods.vivecraft);
               this.rightMouseButton.pressedAtY = (int)Misc.getMouseY(this.field_22787, SupportMods.vivecraft);
               this.viewedOnMousePress = this.viewed;
               this.rightClickX = this.mouseBlockPosX;
               this.rightClickY = this.mouseBlockPosY;
               this.rightClickZ = this.mouseBlockPosZ;
               this.rightClickDim = this.mouseBlockDim;
               this.rightClickCoordinateScale = this.mouseBlockCoordinateScale;
               if (SupportMods.minimap()) {
                  SupportMods.xaeroMinimap.onRightClick();
               }

               if (this.viewedOnMousePress == null || !this.viewedOnMousePress.isRightClickValid()) {
                  this.mapTileSelection = new MapTileSelection(this.rightClickX >> 4, this.rightClickZ >> 4);
               }
            } else {
               toReturn = this.onInputPress(class_307.field_1672, event.method_74245());
            }

            if (!toReturn && this.caveModeOptions.isEnabled()) {
               this.caveModeOptions.toggle(this);
               toReturn = true;
            }
         }

         return toReturn;
      }
   }

   public boolean method_25406(class_11909 event) {
      this.buttonPressed = null;
      int mouseX = (int)Misc.getMouseX(this.field_22787, SupportMods.vivecraft);
      int mouseY = (int)Misc.getMouseY(this.field_22787, SupportMods.vivecraft);
      if (this.leftMouseButton.isDown && event.method_74245() == 0) {
         this.leftMouseButton.isDown = false;
         if (Math.abs(this.leftMouseButton.pressedAtX - mouseX) < 5 && Math.abs(this.leftMouseButton.pressedAtY - mouseY) < 5) {
            this.mapClicked(0, this.leftMouseButton.pressedAtX, this.leftMouseButton.pressedAtY);
         }

         this.leftMouseButton.pressedAtX = -1;
         this.leftMouseButton.pressedAtY = -1;
      }

      if (this.rightMouseButton.isDown && event.method_74245() == 1) {
         this.rightMouseButton.isDown = false;
         this.mapClicked(1, mouseX, mouseY);
         this.rightMouseButton.pressedAtX = -1;
         this.rightMouseButton.pressedAtY = -1;
      }

      if (this.waypointMenu) {
         SupportMods.xaeroMinimap.onMapMouseRelease(event);
      }

      if (this.playersMenu) {
         WorldMap.trackedPlayerMenuRenderer.onMapMouseRelease(event);
      }

      boolean toReturn = super.method_25406(event);
      if (!toReturn) {
         toReturn = this.onInputRelease(class_307.field_1672, event.method_74245());
      }

      return toReturn;
   }

   public boolean method_25401(double par1, double par2, double g, double wheel) {
      int direction = wheel > (double)0.0F ? 1 : -1;
      if (this.waypointMenu && this.overWaypointsMenu) {
         SupportMods.xaeroMinimap.getWaypointMenuRenderer().mouseScrolled(direction);
      } else if (this.playersMenu && this.overPlayersMenu) {
         WorldMap.trackedPlayerMenuRenderer.mouseScrolled(direction);
      } else {
         this.changeZoom(wheel, 0);
      }

      return super.method_25401(par1, par2, g, wheel);
   }

   private void changeZoom(double factor, int zoomMethod) {
      this.closeDropdowns();
      this.lastZoomMethod = zoomMethod;
      this.cameraDestinationAnimX = null;
      this.cameraDestinationAnimZ = null;
      if (hasControlDown()) {
         double destScaleBefore = destScale;
         if (destScale >= (double)1.0F) {
            if (factor > (double)0.0F) {
               destScale = Math.ceil(destScale);
            } else {
               destScale = Math.floor(destScale);
            }

            if (destScaleBefore == destScale) {
               destScale += factor > (double)0.0F ? (double)1.0F : (double)-1.0F;
            }

            if (destScale == (double)0.0F) {
               destScale = (double)0.5F;
            }
         } else {
            double reversedScale = (double)1.0F / destScale;
            double log2 = Math.log(reversedScale) / Math.log((double)2.0F);
            if (factor > (double)0.0F) {
               log2 = Math.floor(log2);
            } else {
               log2 = Math.ceil(log2);
            }

            destScale = (double)1.0F / Math.pow((double)2.0F, log2);
            if (destScaleBefore == destScale) {
               destScale = (double)1.0F / Math.pow((double)2.0F, log2 + (double)(factor > (double)0.0F ? -1 : 1));
            }
         }
      } else {
         destScale *= Math.pow(1.2, factor);
      }

      this.applyZoomLimits();
   }

   private void applyZoomLimits() {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      double minScale = (double)0.0625F;
      if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.UNLIMITED_ZOOM_OUT)) {
         minScale = (double)0.001953125F;
      }

      if (destScale < minScale) {
         destScale = minScale;
      } else {
         double maxScale = (double)50.0F;
         if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.UNLIMITED_ZOOM_IN)) {
            maxScale = (double)1000000.0F;
         }

         if (destScale > maxScale) {
            destScale = maxScale;
         }

      }
   }

   public void method_25432() {
      super.method_25432();
      this.leftMouseButton.isDown = false;
      this.rightMouseButton.isDown = false;
   }

   public void method_25394(class_332 guiGraphics, int scaledMouseX, int scaledMouseY, float delta) {
      if (this.shouldReinit) {
         this.method_25423(this.field_22789, this.field_22790);
      }

      float partialTicks = class_310.method_1551().method_1493() ? 1.0F : class_310.method_1551().method_61966().method_60637(true);
      OpenGlHelper.clearErrors(false, "GuiMap.render");
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      class_310 mc = class_310.method_1551();
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = WorldMap.INSTANCE.getConfigs().getPrimaryClientConfigManager();
      long startTime = System.currentTimeMillis();
      double playerDimDiv = this.prevPlayerDimDiv;
      synchronized(this.mapProcessor.renderThreadPauseSync) {
         if (!this.mapProcessor.isRenderingPaused()) {
            class_2378<class_2874> dimTypes = this.mapProcessor.getWorldDimensionTypeRegistry();
            if (dimTypes != null) {
               playerDimDiv = this.mapProcessor.getMapWorld().getCurrentDimension().calculateDimDiv(dimTypes, this.player.method_73183().method_8597());
            }
         }
      }

      double scaledPlayerX = EntityUtil.getEntityX(this.player, partialTicks) / playerDimDiv;
      double scaledPlayerZ = EntityUtil.getEntityZ(this.player, partialTicks) / playerDimDiv;
      if (!this.shouldResetCameraPos && !attachedCamera) {
         if (this.prevPlayerDimDiv != (double)0.0F && playerDimDiv != this.prevPlayerDimDiv) {
            double oldScaledPlayerX = this.player.method_23317() / this.prevPlayerDimDiv;
            double oldScaledPlayerZ = this.player.method_23321() / this.prevPlayerDimDiv;
            this.cameraX = this.cameraX - oldScaledPlayerX + scaledPlayerX;
            this.cameraZ = this.cameraZ - oldScaledPlayerZ + scaledPlayerZ;
            this.cameraDestinationAnimX = null;
            this.cameraDestinationAnimZ = null;
            this.cameraDestination = null;
         }
      } else {
         this.cameraX = scaledPlayerX;
         this.cameraZ = scaledPlayerZ;
         this.shouldResetCameraPos = false;
         this.cameraDestinationAnimX = null;
         this.cameraDestinationAnimZ = null;
         this.cameraDestination = null;
      }

      this.prevPlayerDimDiv = playerDimDiv;
      double cameraXBefore = this.cameraX;
      double cameraZBefore = this.cameraZ;
      double scaleBefore = this.scale;
      this.mapSwitchingGui.preMapRender(this, this.field_22787, this.field_22789, this.field_22790);
      long passed = this.lastStartTime == 0L ? 16L : startTime - this.lastStartTime;
      double passedScrolls = (double)((float)passed / 64.0F);
      int direction = this.buttonPressed != this.zoomInButton && !KeyMappingUtils.isPhysicallyDown(ControlsRegister.keyZoomIn) ? (this.buttonPressed != this.zoomOutButton && !KeyMappingUtils.isPhysicallyDown(ControlsRegister.keyZoomOut) ? 0 : -1) : 1;
      if (direction != 0) {
         boolean ctrlKey = hasControlDown();
         if (!ctrlKey || !this.pauseZoomKeys) {
            this.changeZoom((double)direction * passedScrolls, this.buttonPressed != this.zoomInButton && this.buttonPressed != this.zoomOutButton ? 1 : 2);
            if (ctrlKey) {
               this.pauseZoomKeys = true;
            }
         }
      } else {
         this.pauseZoomKeys = false;
      }

      this.lastStartTime = startTime;
      if (this.cameraDestination != null) {
         this.cameraDestinationAnimX = new SlowingAnimation(this.cameraX, (double)this.cameraDestination[0], 0.9, 0.01);
         this.cameraDestinationAnimZ = new SlowingAnimation(this.cameraZ, (double)this.cameraDestination[1], 0.9, 0.01);
         this.cameraDestination = null;
      }

      if (this.cameraDestinationAnimX != null) {
         this.cameraX = this.cameraDestinationAnimX.getCurrent();
         if (this.cameraX == this.cameraDestinationAnimX.getDestination()) {
            this.cameraDestinationAnimX = null;
         }
      }

      if (this.cameraDestinationAnimZ != null) {
         this.cameraZ = this.cameraDestinationAnimZ.getCurrent();
         if (this.cameraZ == this.cameraDestinationAnimZ.getDestination()) {
            this.cameraDestinationAnimZ = null;
         }
      }

      this.lastViewedDimensionId = null;
      this.lastViewedMultiworldId = null;
      this.mouseBlockPosY = 32767;
      boolean discoveredForHighlights = false;
      synchronized(this.mapProcessor.renderThreadPauseSync) {
         if (!this.mapProcessor.isRenderingPaused()) {
            MapDimension currentDim = !this.mapProcessor.isMapWorldUsable() ? null : this.mapProcessor.getMapWorld().getCurrentDimension();
            MapDimension futureDimension = !this.mapProcessor.isMapWorldUsable() ? null : this.mapProcessor.getMapWorld().getFutureDimension();
            if (currentDim != this.dimensionOnInit || futureDimension != this.futureDimensionOnInit) {
               boolean dimButtonFocused = this.method_25399() == this.dimensionToggleButton;
               boolean dimDropdownFocused = this.method_25399() == this.mapSwitchingGui.getCreatedDimensionDropdown();
               this.method_25423(this.field_22789, this.field_22790);
               if (dimDropdownFocused) {
                  this.method_25395(this.mapSwitchingGui.getCreatedDimensionDropdown());
               } else if (dimButtonFocused) {
                  this.method_25395(this.dimensionToggleButton);
               }
            }

            boolean mapLoaded = this.mapProcessor.getCurrentWorldId() != null && !this.mapProcessor.isWaitingForWorldUpdate() && this.mapProcessor.getMapSaveLoad().isRegionDetectionComplete();
            boolean noWorldMapEffect = mc.field_1724 == null || Misc.hasEffect(mc.field_1724, Effects.NO_WORLD_MAP) || Misc.hasEffect(mc.field_1724, Effects.NO_WORLD_MAP_HARMFUL);
            class_1792 mapItem = this.mapProcessor.getMapItem();
            boolean allowedBasedOnItem = mapItem == null || mc.field_1724 != null && Misc.hasItem(mc.field_1724, mapItem);
            boolean isLocked = this.mapProcessor.isCurrentMapLocked();
            if (mapLoaded && !noWorldMapEffect && allowedBasedOnItem && !isLocked) {
               GpuBufferSlice projectionBU = RenderSystem.getProjectionMatrixBuffer();
               class_10366 projectionTypeBU = RenderSystem.getProjectionType();
               Misc.minecraftOrtho(this.field_22787, false);
               RenderSystem.getModelViewStack().pushMatrix();
               RenderSystem.getModelViewStack().identity();
               RenderSystem.getModelViewStack().translate(0.0F, 0.0F, -11000.0F);
               if (SupportMods.vivecraft) {
                  TextureUtils.clearRenderTarget(this.field_22787.method_1522(), -16777216);
               }

               boolean skipWorldRenderConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.SKIP_WORLD_RENDER);
               if (!skipWorldRenderConfig) {
                  if (immediateRenderFBO != null && immediateRenderFBO.field_1482 == mc.method_22683().method_4489() && immediateRenderFBO.field_1481 == mc.method_22683().method_4506()) {
                     TextureUtils.clearRenderTargetDepth(immediateRenderFBO, 1.0F);
                  } else {
                     immediateRenderFBO = new ImprovedFramebuffer(mc.method_22683().method_4489(), mc.method_22683().method_4506(), true);
                     TextureUtils.clearRenderTarget(immediateRenderFBO, -16777216, 1.0F);
                  }

                  guiGraphics.method_51448().pushMatrix();
                  guiGraphics.method_51448().scale((float)((double)1.0F / this.screenScale), (float)((double)1.0F / this.screenScale));
                  ((IGuiGraphics)guiGraphics).xaero_lib_getGuiRenderState().method_70919(new class_11241(XaeroRenderType.RP_MAP_FRAME, class_11231.method_70900(immediateRenderFBO.method_71639(), RenderSystem.getSamplerCache().method_75297(FilterMode.NEAREST)), new Matrix3x2f(guiGraphics.method_51448()), 0, 0, immediateRenderFBO.field_1482, immediateRenderFBO.field_1481, 0.0F, 1.0F, 1.0F, 0.0F, -1, (class_8030)null));
                  guiGraphics.method_51448().popMatrix();
               } else if (immediateRenderFBO != null) {
                  immediateRenderFBO.method_1238();
                  immediateRenderFBO = null;
               }

               this.mapProcessor.updateCaveStart();
               this.lastNonNullViewedDimensionId = this.lastViewedDimensionId = this.mapProcessor.getMapWorld().getCurrentDimension().getDimId();
               this.lastViewedMultiworldId = this.mapProcessor.getMapWorld().getCurrentDimension().getCurrentMultiworld();
               if (SupportMods.minimap()) {
                  SupportMods.xaeroMinimap.checkWaypoints(this.mapProcessor.getMapWorld().isMultiplayer(), this.lastViewedDimensionId, this.lastViewedMultiworldId, this.field_22789, this.field_22790, this, this.mapProcessor.getMapWorld(), this.mapProcessor.getWorldDimensionTypeRegistry());
               }

               int mouseXPos = (int)Misc.getMouseX(mc, false);
               int mouseYPos = (int)Misc.getMouseY(mc, false);
               double scaleMultiplier = this.getScaleMultiplier(Math.min(mc.method_22683().method_4489(), mc.method_22683().method_4506()));
               this.scale = this.userScale * scaleMultiplier;
               if (this.mouseCheckPosX == -1 || System.nanoTime() - this.mouseCheckTimeNano > 30000000L) {
                  this.prevMouseCheckPosX = this.mouseCheckPosX;
                  this.prevMouseCheckPosY = this.mouseCheckPosY;
                  this.prevMouseCheckTimeNano = this.mouseCheckTimeNano;
                  this.mouseCheckPosX = mouseXPos;
                  this.mouseCheckPosY = mouseYPos;
                  this.mouseCheckTimeNano = System.nanoTime();
               }

               if (!this.leftMouseButton.isDown) {
                  if (this.mouseDownPosX != -1) {
                     this.mouseDownPosX = -1;
                     this.mouseDownPosY = -1;
                     if (this.prevMouseCheckTimeNano != -1L) {
                        double downTime = (double)0.0F;
                        int draggedX = 0;
                        int draggedY = 0;
                        downTime = (double)(System.nanoTime() - this.prevMouseCheckTimeNano);
                        draggedX = mouseXPos - this.prevMouseCheckPosX;
                        draggedY = mouseYPos - this.prevMouseCheckPosY;
                        double frameTime60FPS = 1.6666666666666666E7;
                        double speedScale = downTime / frameTime60FPS;
                        double speed_x = (double)(-draggedX) / this.scale / speedScale;
                        double speed_z = (double)(-draggedY) / this.scale / speedScale;
                        double speed = Math.sqrt(speed_x * speed_x + speed_z * speed_z);
                        if (speed > (double)0.0F) {
                           double cos = speed_x / speed;
                           double sin = speed_z / speed;
                           double maxSpeed = (double)500.0F / this.userScale;
                           speed = Math.abs(speed) > maxSpeed ? Math.copySign(maxSpeed, speed) : speed;
                           double speed_factor = 0.9;
                           double ln = Math.log(speed_factor);
                           double move_distance = -speed / ln;
                           double moveX = cos * move_distance;
                           double moveZ = sin * move_distance;
                           this.cameraDestinationAnimX = new SlowingAnimation(this.cameraX, this.cameraX + moveX, 0.9, 0.01);
                           this.cameraDestinationAnimZ = new SlowingAnimation(this.cameraZ, this.cameraZ + moveZ, 0.9, 0.01);
                        }
                     }
                  }
               } else if (this.viewed == null || !this.viewedInList || this.mouseDownPosX != -1) {
                  if (this.mouseDownPosX != -1) {
                     this.cameraX = (double)(this.mouseDownPosX - mouseXPos) / this.scale + this.mouseDownCameraX;
                     this.cameraZ = (double)(this.mouseDownPosY - mouseYPos) / this.scale + this.mouseDownCameraZ;
                  } else {
                     this.mouseDownPosX = mouseXPos;
                     this.mouseDownPosY = mouseYPos;
                     this.mouseDownCameraX = this.cameraX;
                     this.mouseDownCameraZ = this.cameraZ;
                     this.cameraDestinationAnimX = null;
                     this.cameraDestinationAnimZ = null;
                     if (attachedCamera) {
                        attachedCamera = false;
                        this.shouldReinit = true;
                     }
                  }
               }

               int mouseFromCentreX = mouseXPos - mc.method_22683().method_4489() / 2;
               int mouseFromCentreY = mouseYPos - mc.method_22683().method_4506() / 2;
               double oldMousePosX = (double)mouseFromCentreX / this.scale + this.cameraX;
               double oldMousePosZ = (double)mouseFromCentreY / this.scale + this.cameraZ;
               double preScale = this.scale;
               this.applyZoomLimits();
               if (destScale != this.userScale) {
                  if (this.zoomAnim != null) {
                     this.userScale = this.zoomAnim.getCurrent();
                     this.scale = this.userScale * scaleMultiplier;
                  }

                  if (this.zoomAnim == null || MathUtils.round(this.zoomAnim.getDestination(), 4) != MathUtils.round(destScale, 4)) {
                     this.zoomAnim = new SinAnimation(this.userScale, destScale, 100L);
                  }
               }

               if (!attachedCamera && this.scale > preScale && this.lastZoomMethod != 2) {
                  this.cameraX = oldMousePosX - (double)mouseFromCentreX / this.scale;
                  this.cameraZ = oldMousePosZ - (double)mouseFromCentreY / this.scale;
               }

               int textureLevel = 0;
               double fboScale;
               if (this.scale >= (double)1.0F) {
                  fboScale = Math.max((double)1.0F, Math.floor(this.scale));
               } else {
                  fboScale = this.scale;
               }

               if (this.userScale < (double)1.0F) {
                  double reversedScale = (double)1.0F / this.userScale;
                  double log2 = Math.floor(Math.log(reversedScale) / Math.log((double)2.0F));
                  textureLevel = Math.min((int)log2, 3);
               }

               this.mapProcessor.getMapSaveLoad().mainTextureLevel = textureLevel;
               int leveledRegionShift = 9 + textureLevel;
               double secondaryScale = this.scale / fboScale;
               class_4587 matrixStack = WorldMap.worldMapClientOnly.getMapScreenPoseStack();
               matrixStack.method_22903();
               matrixStack.method_46416(0.0F, 0.0F, 100.0F);
               double mousePosX = (double)mouseFromCentreX / this.scale + this.cameraX;
               double mousePosZ = (double)mouseFromCentreY / this.scale + this.cameraZ;
               this.mouseBlockPosX = (int)Math.floor(mousePosX);
               this.mouseBlockPosZ = (int)Math.floor(mousePosZ);
               this.mouseBlockDim = this.mapProcessor.getMapWorld().getCurrentDimension().getDimId();
               this.mouseBlockCoordinateScale = this.getCurrentMapCoordinateScale();
               if (SupportMods.minimap()) {
                  SupportMods.xaeroMinimap.onBlockHover();
               }

               int mouseRegX = this.mouseBlockPosX >> leveledRegionShift;
               int mouseRegZ = this.mouseBlockPosZ >> leveledRegionShift;
               int renderedCaveLayer = this.mapProcessor.getCurrentCaveLayer();
               LeveledRegion<?> reg = this.mapProcessor.getLeveledRegion(renderedCaveLayer, mouseRegX, mouseRegZ, textureLevel);
               int maxRegBlockCoord = (1 << leveledRegionShift) - 1;
               int mouseRegPixelX = (this.mouseBlockPosX & maxRegBlockCoord) >> textureLevel;
               int mouseRegPixelZ = (this.mouseBlockPosZ & maxRegBlockCoord) >> textureLevel;
               this.mouseBlockPosX = (mouseRegX << leveledRegionShift) + (mouseRegPixelX << textureLevel);
               this.mouseBlockPosZ = (mouseRegZ << leveledRegionShift) + (mouseRegPixelZ << textureLevel);
               if (this.mapTileSelection != null && this.rightClickMenu == null) {
                  this.mapTileSelection.setEnd(this.mouseBlockPosX >> 4, this.mouseBlockPosZ >> 4);
               }

               MapRegion leafRegion = this.mapProcessor.getLeafMapRegion(renderedCaveLayer, this.mouseBlockPosX >> 9, this.mouseBlockPosZ >> 9, false);
               MapTileChunk chunk = leafRegion == null ? null : leafRegion.getChunk(this.mouseBlockPosX >> 6 & 7, this.mouseBlockPosZ >> 6 & 7);
               int debugTextureX = this.mouseBlockPosX >> leveledRegionShift - 3 & 7;
               int debugTextureY = this.mouseBlockPosZ >> leveledRegionShift - 3 & 7;
               RegionTexture tex = reg != null && reg.hasTextures() ? reg.getTexture(debugTextureX, debugTextureY) : null;
               boolean debugConfig = this.mapProcessor.isDebugConfig();
               if (debugConfig && !hiddenUI) {
                  if (reg != null) {
                     List<String> debugLines = new ArrayList();
                     if (tex != null) {
                        tex.addDebugLines(debugLines);
                        MapTile mouseTile = chunk == null ? null : chunk.getTile(this.mouseBlockPosX >> 4 & 3, this.mouseBlockPosZ >> 4 & 3);
                        if (mouseTile != null) {
                           MapBlock block = mouseTile.getBlock(this.mouseBlockPosX & 15, this.mouseBlockPosZ & 15);
                           if (block != null) {
                              guiGraphics.method_25300(mc.field_1772, block.toRenderString(leafRegion.getBiomeRegistry()), this.field_22789 / 2, 22, -1);
                              if (block.getNumberOfOverlays() != 0) {
                                 for(int i = 0; i < block.getOverlays().size(); ++i) {
                                    guiGraphics.method_25300(mc.field_1772, ((Overlay)block.getOverlays().get(i)).toRenderString(), this.field_22789 / 2, 32 + i * 10, -1);
                                 }
                              }
                           }
                        }
                     }

                     debugLines.add("");
                     debugLines.add(reg.toString());
                     reg.addDebugLines(debugLines, this.mapProcessor, debugTextureX, debugTextureY);

                     for(int i = 0; i < debugLines.size(); ++i) {
                        guiGraphics.method_25303(mc.field_1772, (String)debugLines.get(i), 5, 15 + 10 * i, -1);
                     }
                  }

                  class_2874 dimType = this.mapProcessor.getMapWorld().getCurrentDimension().getDimensionType(this.mapProcessor.getWorldDimensionTypeRegistry());
                  class_2960 dimTypeId = this.mapProcessor.getMapWorld().getCurrentDimension().getDimensionTypeId();
                  guiGraphics.method_25303(mc.field_1772, "MultiWorld ID: " + this.mapProcessor.getMapWorld().getCurrentMultiworld() + " Dim Type: " + String.valueOf(dimType == null ? "unknown" : dimTypeId), 5, 265, -1);
                  LayeredRegionManager regions = this.mapProcessor.getMapWorld().getCurrentDimension().getLayeredMapRegions();
                  guiGraphics.method_25303(mc.field_1772, String.format("regions: %d loaded: %d processed: %d viewed: %d benchmarks %s", regions.size(), regions.loadedCount(), this.mapProcessor.getProcessedCount(), lastAmountOfRegionsViewed, WorldMap.textureUploadBenchmark.getTotalsString()), 5, 275, -1);
                  guiGraphics.method_25303(mc.field_1772, String.format("toLoad: %d toSave: %d tile pool: %d overlays: %d toLoadBranchCache: %d buffers: %d", this.mapProcessor.getMapSaveLoad().getSizeOfToLoad(), this.mapProcessor.getMapSaveLoad().getToSave().size(), this.mapProcessor.getTilePool().size(), this.mapProcessor.getOverlayManager().getNumberOfUniqueOverlays(), this.mapProcessor.getMapSaveLoad().getSizeOfToLoadBranchCache(), WorldMap.textureDirectBufferPool.size()), 5, 285, -1);
                  long i = Runtime.getRuntime().maxMemory();
                  long j = Runtime.getRuntime().totalMemory();
                  long k = Runtime.getRuntime().freeMemory();
                  long l = j - k;
                  int debugFPS = ((IWorldMapMinecraftClient)mc).getXaeroWorldMap_fps();
                  guiGraphics.method_25303(mc.field_1772, String.format("FPS: %d", debugFPS), 5, 295, -1);
                  guiGraphics.method_25303(mc.field_1772, String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)), 5, 315, -1);
                  guiGraphics.method_25303(mc.field_1772, String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), 5, 325, -1);
                  guiGraphics.method_25303(mc.field_1772, String.format("Available VRAM: %dMB", this.mapProcessor.getMapLimiter().getAvailableVRAM() / 1024), 5, 335, -1);
               }

               int pixelInsideTexX = mouseRegPixelX & 63;
               int pixelInsideTexZ = mouseRegPixelZ & 63;
               boolean hasAmbiguousHeight = false;
               int mouseBlockBottomY = 32767;
               int mouseBlockTopY = 32767;
               class_5321<class_1959> pointedAtBiome = null;
               if (tex != null) {
                  mouseBlockBottomY = this.mouseBlockPosY = tex.getHeight(pixelInsideTexX, pixelInsideTexZ);
                  mouseBlockTopY = tex.getTopHeight(pixelInsideTexX, pixelInsideTexZ);
                  hasAmbiguousHeight = this.mouseBlockPosY != mouseBlockTopY;
                  pointedAtBiome = tex.getBiome(pixelInsideTexX, pixelInsideTexZ);
               }

               if (hasAmbiguousHeight) {
                  if (mouseBlockTopY != 32767) {
                     this.mouseBlockPosY = mouseBlockTopY;
                  } else if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.DETECT_AMBIGUOUS_Y)) {
                     this.mouseBlockPosY = 32767;
                  }
               }

               if (primaryScaleFBO == null || primaryScaleFBO.field_1482 != mc.method_22683().method_4489() || primaryScaleFBO.field_1481 != mc.method_22683().method_4506()) {
                  primaryScaleFBO = new ImprovedFramebuffer(mc.method_22683().method_4489(), mc.method_22683().method_4506(), true);
               }

               if (primaryScaleFBO.method_30277() == null || primaryScaleFBO.method_30278() == null) {
                  matrixStack.method_22909();
                  RenderSystem.setProjectionMatrix(projectionBU, projectionTypeBU);
                  RenderSystem.getModelViewStack().popMatrix();
                  return;
               }

               primaryScaleFBO.bindAsMainTarget(false);
               TextureUtils.clearRenderTarget(primaryScaleFBO, -16777216, 1.0F);
               matrixStack.method_22905((float)((double)1.0F / this.screenScale), (float)((double)1.0F / this.screenScale), 1.0F);
               matrixStack.method_46416((float)(mc.method_22683().method_4489() / 2), (float)(mc.method_22683().method_4506() / 2), 0.0F);
               matrixStack.method_22903();
               int flooredCameraX = (int)Math.floor(this.cameraX);
               int flooredCameraZ = (int)Math.floor(this.cameraZ);
               double primaryOffsetX = (double)0.0F;
               double primaryOffsetY = (double)0.0F;
               double secondaryOffsetX;
               double secondaryOffsetY;
               if (fboScale < (double)1.0F) {
                  double pixelInBlocks = (double)1.0F / fboScale;
                  int xInFullPixels = (int)Math.floor(this.cameraX / pixelInBlocks);
                  int zInFullPixels = (int)Math.floor(this.cameraZ / pixelInBlocks);
                  double fboOffsetX = (double)xInFullPixels * pixelInBlocks;
                  double fboOffsetZ = (double)zInFullPixels * pixelInBlocks;
                  flooredCameraX = (int)Math.floor(fboOffsetX);
                  flooredCameraZ = (int)Math.floor(fboOffsetZ);
                  primaryOffsetX = fboOffsetX - (double)flooredCameraX;
                  primaryOffsetY = fboOffsetZ - (double)flooredCameraZ;
                  secondaryOffsetX = (this.cameraX - fboOffsetX) * fboScale;
                  secondaryOffsetY = (this.cameraZ - fboOffsetZ) * fboScale;
               } else {
                  secondaryOffsetX = (this.cameraX - (double)flooredCameraX) * fboScale;
                  secondaryOffsetY = (this.cameraZ - (double)flooredCameraZ) * fboScale;
                  if (secondaryOffsetX >= (double)1.0F) {
                     int offset = (int)secondaryOffsetX;
                     matrixStack.method_46416((float)(-offset), 0.0F, 0.0F);
                     secondaryOffsetX -= (double)offset;
                  }

                  if (secondaryOffsetY >= (double)1.0F) {
                     int offset = (int)secondaryOffsetY;
                     matrixStack.method_46416(0.0F, (float)offset, 0.0F);
                     secondaryOffsetY -= (double)offset;
                  }
               }

               matrixStack.method_22905((float)fboScale, (float)(-fboScale), 1.0F);
               matrixStack.method_22904(-primaryOffsetX, -primaryOffsetY, (double)0.0F);
               double leftBorder = this.cameraX - (double)(mc.method_22683().method_4489() / 2) / this.scale;
               double rightBorder = leftBorder + (double)mc.method_22683().method_4489() / this.scale;
               double topBorder = this.cameraZ - (double)(mc.method_22683().method_4506() / 2) / this.scale;
               double bottomBorder = topBorder + (double)mc.method_22683().method_4506() / this.scale;
               int minRegX = (int)Math.floor(leftBorder) >> leveledRegionShift;
               int maxRegX = (int)Math.floor(rightBorder) >> leveledRegionShift;
               int minRegZ = (int)Math.floor(topBorder) >> leveledRegionShift;
               int maxRegZ = (int)Math.floor(bottomBorder) >> leveledRegionShift;
               int blockToTextureConversion = 6 + textureLevel;
               int minTextureX = (int)Math.floor(leftBorder) >> blockToTextureConversion;
               int maxTextureX = (int)Math.floor(rightBorder) >> blockToTextureConversion;
               int minTextureZ = (int)Math.floor(topBorder) >> blockToTextureConversion;
               int maxTextureZ = (int)Math.floor(bottomBorder) >> blockToTextureConversion;
               int minLeafRegX = minTextureX << blockToTextureConversion >> 9;
               int maxLeafRegX = (maxTextureX + 1 << blockToTextureConversion) - 1 >> 9;
               int minLeafRegZ = minTextureZ << blockToTextureConversion >> 9;
               int maxLeafRegZ = (maxTextureZ + 1 << blockToTextureConversion) - 1 >> 9;
               int leveledWidthInRegions = maxRegX - minRegX + 1;
               int leveledHeightInRegions = maxRegZ - minRegZ + 1;
               lastAmountOfRegionsViewed = leveledWidthInRegions * leveledHeightInRegions;
               this.mapProcessor.getMapLimiter().regionsFitOnScreen(lastAmountOfRegionsViewed);
               this.regionBuffer.clear();
               this.branchRegionBuffer.clear();
               float brightness = this.mapProcessor.getBrightness();
               int globalRegionCacheHashCode = WorldMap.settings.getRegionCacheHashCode();
               int globalCaveStart = this.mapProcessor.getMapWorld().getCurrentDimension().getLayeredMapRegions().getLayer(renderedCaveLayer).getCaveStart();
               int globalCaveDepth = this.mapProcessor.getCaveModeDepthConfig();
               boolean reloadEverything = this.mapProcessor.isReloadViewedConfig();
               int globalReloadVersion = this.mapProcessor.getReloadViewedVersionConfig();
               int globalVersion = this.mapProcessor.getGlobalVersion();
               Matrix4f matrix = matrixStack.method_23760().method_23761();
               XaeroBufferProvider renderTypeBuffers = XaeroLib.INSTANCE.getClient().getBufferProvider();
               MultiTextureRenderTypeRendererProvider rendererProvider = this.mapProcessor.getMultiTextureRenderTypeRenderers();
               MultiTextureRenderTypeRenderer withLightRenderer = rendererProvider.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, CustomRenderTypes.MAP);
               MultiTextureRenderTypeRenderer noLightRenderer = rendererProvider.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, CustomRenderTypes.MAP);
               class_4588 overlayBuffer = renderTypeBuffers.getBuffer(CustomRenderTypes.MAP_COLOR_OVERLAY);
               LeveledRegion.setComparison(this.mouseBlockPosX >> leveledRegionShift, this.mouseBlockPosZ >> leveledRegionShift, textureLevel, this.mouseBlockPosX >> 9, this.mouseBlockPosZ >> 9);
               LeveledRegion<?> lastUpdatedRootLeveledRegion = null;
               boolean cacheOnlyMode = this.mapProcessor.getMapWorld().isCacheOnlyMode();
               boolean frameRenderedRootTextures = false;
               boolean loadingLeaves = false;
               int leveledUpdateSliceSize = 100;
               int levelUpdateSliceCount = (lastAmountOfRegionsViewed + leveledUpdateSliceSize - 1) / leveledUpdateSliceSize;
               if (this.currentUpdateSliceIndex >= levelUpdateSliceCount) {
                  this.currentUpdateSliceIndex = 0;
                  this.prevWaitingForBranchCache = this.waitingForBranchCache[0];
                  this.waitingForBranchCache[0] = false;
               }

               int leveledRegionCounter = 0;
               boolean prevWaitingForBranchCache = this.prevWaitingForBranchCache;

               for(int leveledRegX = minRegX; leveledRegX <= maxRegX; ++leveledRegX) {
                  for(int leveledRegZ = minRegZ; leveledRegZ <= maxRegZ; ++leveledRegZ) {
                     int updateSliceIndex = leveledRegionCounter / leveledUpdateSliceSize;
                     boolean isInUpdateSlice = updateSliceIndex == this.currentUpdateSliceIndex;
                     ++leveledRegionCounter;
                     int leveledSideInRegions = 1 << textureLevel;
                     int leveledSideInBlocks = leveledSideInRegions * 512;
                     int leafRegionMinX = leveledRegX * leveledSideInRegions;
                     int leafRegionMinZ = leveledRegZ * leveledSideInRegions;
                     LeveledRegion<?> leveledRegion = null;
                     if (!isInUpdateSlice) {
                        leveledRegion = this.mapProcessor.getLeveledRegion(renderedCaveLayer, leveledRegX, leveledRegZ, textureLevel);
                     }

                     for(int leafX = 0; leafX < leveledSideInRegions && isInUpdateSlice; ++leafX) {
                        for(int leafZ = 0; leafZ < leveledSideInRegions; ++leafZ) {
                           int regX = leafRegionMinX + leafX;
                           if (regX >= minLeafRegX && regX <= maxLeafRegX) {
                              int regZ = leafRegionMinZ + leafZ;
                              if (regZ >= minLeafRegZ && regZ <= maxLeafRegZ) {
                                 MapRegion region = this.mapProcessor.getLeafMapRegion(renderedCaveLayer, regX, regZ, false);
                                 if (region == null) {
                                    region = this.mapProcessor.getLeafMapRegion(renderedCaveLayer, regX, regZ, this.mapProcessor.regionExists(renderedCaveLayer, regX, regZ));
                                 }

                                 if (region != null) {
                                    if (leveledRegion == null) {
                                       leveledRegion = this.mapProcessor.getLeveledRegion(renderedCaveLayer, leveledRegX, leveledRegZ, textureLevel);
                                    }

                                    if (!prevWaitingForBranchCache) {
                                       synchronized(region) {
                                          if (textureLevel != 0 && region.getLoadState() == 0 && region.loadingNeededForBranchLevel != 0 && region.loadingNeededForBranchLevel != textureLevel) {
                                             region.loadingNeededForBranchLevel = 0;
                                             region.getParent().setShouldCheckForUpdatesRecursive(true);
                                          }

                                          if (region.canRequestReload_unsynced() && (!cacheOnlyMode && (reloadEverything && region.getReloadVersion() != globalReloadVersion || region.getCacheHashCode() != globalRegionCacheHashCode || region.caveStartOutdated(globalCaveStart, globalCaveDepth) || region.getVersion() != globalVersion || region.getLoadState() != 2 && region.shouldCache()) || region.getLoadState() == 0 && (!region.isMetaLoaded() || textureLevel == 0 || region.loadingNeededForBranchLevel == textureLevel) || (region.isMetaLoaded() || region.getLoadState() != 0 || !region.hasHadTerrain()) && region.getHighlightsHash() != region.getDim().getHighlightHandler().getRegionHash(region.getRegionX(), region.getRegionZ()))) {
                                             loadingLeaves = true;
                                             region.calculateSortingDistance();
                                             Misc.addToListOfSmallest(10, this.regionBuffer, region);
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }

                     if (leveledRegion != null) {
                        LeveledRegion<?> rootLeveledRegion = leveledRegion.getRootRegion();
                        if (rootLeveledRegion == leveledRegion) {
                           rootLeveledRegion = null;
                        }

                        if (rootLeveledRegion != null && !rootLeveledRegion.isLoaded()) {
                           if (isInUpdateSlice) {
                              if (!rootLeveledRegion.recacheHasBeenRequested() && !rootLeveledRegion.reloadHasBeenRequested()) {
                                 rootLeveledRegion.calculateSortingDistance();
                                 Misc.addToListOfSmallest(10, this.branchRegionBuffer, rootLeveledRegion);
                              }

                              this.waitingForBranchCache[0] = true;
                           }

                           rootLeveledRegion = null;
                        }

                        if (isInUpdateSlice) {
                           if (!this.mapProcessor.isUploadingPaused() && !WorldMap.pauseRequests) {
                              if (leveledRegion instanceof BranchLeveledRegion) {
                                 BranchLeveledRegion branchRegion = (BranchLeveledRegion)leveledRegion;
                                 branchRegion.checkForUpdates(this.mapProcessor, prevWaitingForBranchCache, this.waitingForBranchCache, this.branchRegionBuffer, textureLevel, minLeafRegX, minLeafRegZ, maxLeafRegX, maxLeafRegZ);
                              }

                              if ((textureLevel != 0 && !prevWaitingForBranchCache || textureLevel == 0 && !this.prevLoadingLeaves) && this.lastFrameRenderedRootTextures && rootLeveledRegion != null && rootLeveledRegion != lastUpdatedRootLeveledRegion) {
                                 BranchLeveledRegion branchRegion = (BranchLeveledRegion)rootLeveledRegion;
                                 branchRegion.checkForUpdates(this.mapProcessor, prevWaitingForBranchCache, this.waitingForBranchCache, this.branchRegionBuffer, textureLevel, minLeafRegX, minLeafRegZ, maxLeafRegX, maxLeafRegZ);
                                 lastUpdatedRootLeveledRegion = rootLeveledRegion;
                              }

                              this.mapProcessor.getMapWorld().getCurrentDimension().getLayeredMapRegions().bumpLoadedRegion(leveledRegion);
                              if (rootLeveledRegion != null) {
                                 this.mapProcessor.getMapWorld().getCurrentDimension().getLayeredMapRegions().bumpLoadedRegion(rootLeveledRegion);
                              }
                           } else {
                              this.waitingForBranchCache[0] = prevWaitingForBranchCache;
                           }
                        }

                        int minXBlocks = leveledRegX * leveledSideInBlocks;
                        int minZBlocks = leveledRegZ * leveledSideInBlocks;
                        int textureSize = 64 * leveledSideInRegions;
                        int firstTextureX = leveledRegX << 3;
                        int firstTextureZ = leveledRegZ << 3;
                        int levelDiff = 3 - textureLevel;
                        int rootSize = 1 << levelDiff;
                        int maxInsideCoord = rootSize - 1;
                        int firstRootTextureX = firstTextureX >> levelDiff & 7;
                        int firstRootTextureZ = firstTextureZ >> levelDiff & 7;
                        int firstInsideTextureX = firstTextureX & maxInsideCoord;
                        int firstInsideTextureZ = firstTextureZ & maxInsideCoord;
                        boolean hasTextures = leveledRegion.hasTextures();
                        boolean rootHasTextures = rootLeveledRegion != null && rootLeveledRegion.hasTextures();
                        if (hasTextures || rootHasTextures) {
                           for(int o = 0; o < 8; ++o) {
                              int textureX = minXBlocks + o * textureSize;
                              if (!((double)textureX > rightBorder) && !((double)(textureX + textureSize) < leftBorder)) {
                                 for(int p = 0; p < 8; ++p) {
                                    int textureZ = minZBlocks + p * textureSize;
                                    if (!((double)textureZ > bottomBorder) && !((double)(textureZ + textureSize) < topBorder)) {
                                       RegionTexture<?> regionTexture = hasTextures ? leveledRegion.getTexture(o, p) : null;
                                       if (regionTexture != null && regionTexture.getGlColorTexture() != null) {
                                          GpuTextureAndView texture = regionTexture.getGlColorTexture();
                                          if (texture != null) {
                                             boolean hasLight = regionTexture.getTextureHasLight();
                                             renderTexturedModalRectWithLighting3(matrix, (float)(textureX - flooredCameraX), (float)(textureZ - flooredCameraZ), (float)textureSize, (float)textureSize, texture.texture, hasLight, hasLight ? withLightRenderer : noLightRenderer);
                                          }
                                       } else if (rootHasTextures) {
                                          int insideX = firstInsideTextureX + o;
                                          int insideZ = firstInsideTextureZ + p;
                                          int rootTextureX = firstRootTextureX + (insideX >> levelDiff);
                                          int rootTextureZ = firstRootTextureZ + (insideZ >> levelDiff);
                                          regionTexture = rootLeveledRegion.getTexture(rootTextureX, rootTextureZ);
                                          if (regionTexture != null) {
                                             GpuTextureAndView texture = regionTexture.getGlColorTexture();
                                             if (texture != null) {
                                                frameRenderedRootTextures = true;
                                                int insideTextureX = insideX & maxInsideCoord;
                                                int insideTextureZ = insideZ & maxInsideCoord;
                                                float textureX1 = (float)insideTextureX / (float)rootSize;
                                                float textureX2 = (float)(insideTextureX + 1) / (float)rootSize;
                                                float textureY1 = (float)insideTextureZ / (float)rootSize;
                                                float textureY2 = (float)(insideTextureZ + 1) / (float)rootSize;
                                                boolean hasLight = regionTexture.getTextureHasLight();
                                                renderTexturedModalSubRectWithLighting(matrix, (float)(textureX - flooredCameraX), (float)(textureZ - flooredCameraZ), textureX1, textureY1, textureX2, textureY2, (float)textureSize, (float)textureSize, texture.texture, hasLight, hasLight ? withLightRenderer : noLightRenderer);
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (leveledRegion.loadingAnimation()) {
                           matrixStack.method_22903();
                           matrixStack.method_22904((double)leveledSideInBlocks * ((double)leveledRegX + (double)0.5F) - (double)flooredCameraX, (double)leveledSideInBlocks * ((double)leveledRegZ + (double)0.5F) - (double)flooredCameraZ, (double)0.0F);
                           float loadingAnimationPassed = (float)(System.currentTimeMillis() - this.loadingAnimationStart);
                           if (loadingAnimationPassed > 0.0F) {
                              int period = 2000;
                              int numbersOfActors = 3;
                              float loadingAnimation = loadingAnimationPassed % (float)period / (float)period * 360.0F;
                              float step = 360.0F / (float)numbersOfActors;
                              OptimizedMath.rotatePose(matrixStack, loadingAnimation, OptimizedMath.ZP);
                              int numberOfVisibleActors = 1 + (int)loadingAnimationPassed % (3 * period) / period;
                              matrixStack.method_22905((float)leveledSideInRegions, (float)leveledSideInRegions, 1.0F);

                              for(int i = 0; i < numberOfVisibleActors; ++i) {
                                 OptimizedMath.rotatePose(matrixStack, step, OptimizedMath.ZP);
                                 MapRenderHelper.fillIntoExistingBuffer(matrixStack.method_23760().method_23761(), overlayBuffer, 16, -8, 32, 8, 1.0F, 1.0F, 1.0F, 1.0F);
                              }
                           }

                           matrixStack.method_22909();
                        }

                        if (debugConfig && leveledRegion instanceof MapRegion) {
                           MapRegion region = (MapRegion)leveledRegion;
                           matrixStack.method_22903();
                           matrixStack.method_46416((float)(512 * region.getRegionX() + 32 - flooredCameraX), (float)(512 * region.getRegionZ() + 32 - flooredCameraZ), 0.0F);
                           matrixStack.method_22905(10.0F, 10.0F, 1.0F);
                           Misc.drawNormalText(matrixStack, "" + region.getLoadState(), 0.0F, 0.0F, -1, true, renderTypeBuffers);
                           matrixStack.method_22909();
                        }

                        if (debugConfig && textureLevel > 0) {
                           for(int leafX = 0; leafX < leveledSideInRegions; ++leafX) {
                              for(int leafZ = 0; leafZ < leveledSideInRegions; ++leafZ) {
                                 int regX = leafRegionMinX + leafX;
                                 int regZ = leafRegionMinZ + leafZ;
                                 MapRegion region = this.mapProcessor.getLeafMapRegion(renderedCaveLayer, regX, regZ, false);
                                 if (region != null) {
                                    boolean currentlyLoading = this.mapProcessor.getMapSaveLoad().getNextToLoadByViewing() == region;
                                    if (currentlyLoading || region.isLoaded() || region.isMetaLoaded()) {
                                       matrixStack.method_22903();
                                       matrixStack.method_46416((float)(512 * region.getRegionX() - flooredCameraX), (float)(512 * region.getRegionZ() - flooredCameraZ), 0.0F);
                                       float r = 0.0F;
                                       float g = 0.0F;
                                       float b = 0.0F;
                                       float a = 0.1569F;
                                       if (currentlyLoading) {
                                          b = 1.0F;
                                          r = 1.0F;
                                       } else if (region.isLoaded()) {
                                          g = 1.0F;
                                       } else {
                                          g = 1.0F;
                                          r = 1.0F;
                                       }

                                       MapRenderHelper.fillIntoExistingBuffer(matrixStack.method_23760().method_23761(), overlayBuffer, 0, 0, 512, 512, r, g, b, a);
                                       matrixStack.method_22909();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               ++this.currentUpdateSliceIndex;
               this.lastFrameRenderedRootTextures = frameRenderedRootTextures;
               WorldMapShaderHelper.setBrightness(brightness);
               WorldMapShaderHelper.setWithLight(true);
               rendererProvider.draw(withLightRenderer);
               WorldMapShaderHelper.setWithLight(false);
               rendererProvider.draw(noLightRenderer);
               LeveledRegion<?> nextToLoad = this.mapProcessor.getMapSaveLoad().getNextToLoadByViewing();
               boolean shouldRequest = false;
               if (nextToLoad != null) {
                  shouldRequest = nextToLoad.shouldAllowAnotherRegionToLoad();
               } else {
                  shouldRequest = true;
               }

               shouldRequest = shouldRequest && this.mapProcessor.getAffectingLoadingFrequencyCount() < 16;
               if (shouldRequest && !WorldMap.pauseRequests) {
                  int toRequest = 2;
                  int counter = 0;

                  for(int i = 0; i < this.branchRegionBuffer.size() && counter < toRequest; ++i) {
                     BranchLeveledRegion region = (BranchLeveledRegion)this.branchRegionBuffer.get(i);
                     if (!region.reloadHasBeenRequested() && !region.recacheHasBeenRequested() && !region.isLoaded()) {
                        region.setReloadHasBeenRequested(true, "Gui");
                        this.mapProcessor.getMapSaveLoad().requestBranchCache(region, "Gui");
                        if (counter == 0) {
                           this.mapProcessor.getMapSaveLoad().setNextToLoadByViewing(region);
                        }

                        ++counter;
                     }
                  }

                  toRequest = 1;
                  counter = 0;
                  if (!prevWaitingForBranchCache) {
                     for(int i = 0; i < this.regionBuffer.size() && counter < toRequest; ++i) {
                        MapRegion region = (MapRegion)this.regionBuffer.get(i);
                        if (region != nextToLoad || this.regionBuffer.size() <= 1) {
                           synchronized(region) {
                              if (region.canRequestReload_unsynced()) {
                                 if (region.getLoadState() == 2) {
                                    region.requestRefresh(this.mapProcessor);
                                 } else {
                                    this.mapProcessor.getMapSaveLoad().requestLoad(region, "Gui");
                                 }

                                 if (counter == 0) {
                                    this.mapProcessor.getMapSaveLoad().setNextToLoadByViewing(region);
                                 }

                                 ++counter;
                                 if (region.getLoadState() == 4) {
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               this.prevLoadingLeaves = loadingLeaves;
               int highlightChunkX = this.mouseBlockPosX >> 4;
               int highlightChunkZ = this.mouseBlockPosZ >> 4;
               int chunkHighlightLeftX = highlightChunkX << 4;
               int chunkHighlightRightX = highlightChunkX + 1 << 4;
               int chunkHighlightTopZ = highlightChunkZ << 4;
               int chunkHighlightBottomZ = highlightChunkZ + 1 << 4;
               int playerX = (int)Math.floor(this.player.method_23317());
               int playerZ = (int)Math.floor(this.player.method_23321());
               int playerChunkX = playerX >> 4;
               int playerChunkZ = playerZ >> 4;
               if (!hiddenUI) {
                  MapRenderHelper.renderDynamicHighlight(matrixStack, overlayBuffer, flooredCameraX, flooredCameraZ, chunkHighlightLeftX, chunkHighlightRightX, chunkHighlightTopZ, chunkHighlightBottomZ, 0.0F, 0.0F, 0.0F, 0.2F, 1.0F, 1.0F, 1.0F, 0.1569F);
                  this.renderServerChunkRadius(playerChunkX, playerChunkZ, flooredCameraX, flooredCameraZ, matrixStack, overlayBuffer, configManager);
               }

               MapTileSelection mapTileSelectionToRender = this.mapTileSelection;
               if (mapTileSelectionToRender == null && this.field_22787.field_1755 instanceof ExportScreen) {
                  mapTileSelectionToRender = ((ExportScreen)this.field_22787.field_1755).getSelection();
               }

               if (mapTileSelectionToRender != null) {
                  MapRenderHelper.renderDynamicHighlight(matrixStack, overlayBuffer, flooredCameraX, flooredCameraZ, mapTileSelectionToRender.getLeft() << 4, mapTileSelectionToRender.getRight() + 1 << 4, mapTileSelectionToRender.getTop() << 4, mapTileSelectionToRender.getBottom() + 1 << 4, 0.0F, 0.0F, 0.0F, 0.2F, 1.0F, 0.5F, 0.5F, 0.4F);
                  if (SupportMods.pac() && !this.mapProcessor.getMapWorld().isUsingCustomDimension()) {
                     int claimDistance = SupportMods.xaeroPac.getClaimDistance();
                     int claimableAreaLeft = playerChunkX - claimDistance;
                     int claimableAreaTop = playerChunkZ - claimDistance;
                     int claimableAreaRight = playerChunkX + claimDistance;
                     int claimableAreaBottom = playerChunkZ + claimDistance;
                     MapRenderHelper.renderDynamicChunkHighlight(claimableAreaLeft, claimableAreaRight, claimableAreaTop, claimableAreaBottom, flooredCameraX, flooredCameraZ, 0.0F, 0.0F, 1.0F, 0.3F, 0.0F, 0.0F, 1.0F, 0.15F, matrixStack, overlayBuffer);
                  }
               }

               renderTypeBuffers.endBatch();
               primaryScaleFBO.bindDefaultFramebuffer(mc);
               matrixStack.method_22909();
               if (!skipWorldRenderConfig) {
                  immediateRenderFBO.bindAsMainTarget(false);
               }

               matrixStack.method_22903();
               matrixStack.method_22905((float)secondaryScale, (float)secondaryScale, 1.0F);
               class_4588 colorBackgroundConsumer = renderTypeBuffers.getBuffer(CustomRenderTypes.MAP_COLOR_FILLER);
               int lineX = -mc.method_22683().method_4489() / 2;
               int lineY = mc.method_22683().method_4506() / 2 - 5;
               int lineW = mc.method_22683().method_4489();
               int lineH = 6;
               MapRenderHelper.fillIntoExistingBuffer(matrixStack.method_23760().method_23761(), colorBackgroundConsumer, lineX, lineY, lineX + lineW, lineY + lineH, 0.0F, 0.0F, 0.0F, 1.0F);
               lineX = mc.method_22683().method_4489() / 2 - 5;
               lineY = -mc.method_22683().method_4506() / 2;
               lineW = 6;
               lineH = mc.method_22683().method_4506();
               MapRenderHelper.fillIntoExistingBuffer(matrixStack.method_23760().method_23761(), colorBackgroundConsumer, lineX, lineY, lineX + lineW, lineY + lineH, 0.0F, 0.0F, 0.0F, 1.0F);
               renderTypeBuffers.endBatch();
               class_1921 mainFrameRenderType = CustomRenderTypes.MAP_FRAME;
               MultiTextureRenderTypeRenderer mainFrameRenderer = rendererProvider.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, mainFrameRenderType);
               class_4588 mainFrameVertexConsumer = mainFrameRenderer.begin(primaryScaleFBO.method_30277());
               renderTexturedModalRect(matrixStack.method_23760().method_23761(), mainFrameVertexConsumer, (float)(-mc.method_22683().method_4489() / 2) - (float)secondaryOffsetX, (float)(-mc.method_22683().method_4506() / 2) - (float)secondaryOffsetY, 0, 0, (float)primaryScaleFBO.field_1482, (float)primaryScaleFBO.field_1481, (float)primaryScaleFBO.field_1482, (float)primaryScaleFBO.field_1481, 1.0F, 1.0F, 1.0F, 1.0F);
               rendererProvider.draw(mainFrameRenderer);
               matrixStack.method_22909();
               matrixStack.method_22905((float)this.scale, (float)this.scale, 1.0F);
               double screenSizeBasedScale = scaleMultiplier;
               WorldMap.trackedPlayerRenderer.update(mc);
               class_4597.class_4598 vanillaRenderBuffers = this.field_22787.method_22940().method_23000();

               try {
                  this.viewed = WorldMap.mapElementRenderHandler.render(this, vanillaRenderBuffers, rendererProvider, this.cameraX, this.cameraZ, mc.method_22683().method_4489(), mc.method_22683().method_4506(), screenSizeBasedScale, this.scale, playerDimDiv, mousePosX, mousePosZ, brightness, renderedCaveLayer != Integer.MAX_VALUE, this.viewed, mc, partialTicks);
               } catch (Throwable t) {
                  WorldMap.LOGGER.error("error rendering map elements", t);
                  throw t;
               }

               vanillaRenderBuffers.method_22993();
               this.viewedInList = false;
               matrixStack.method_22903();
               matrixStack.method_46416(0.0F, 0.0F, 50.0F);
               class_4588 regularUIObjectConsumer = renderTypeBuffers.getBuffer(CustomRenderTypes.GUI_BILINEAR);
               if (!hiddenUI && this.mapProcessor.isFootstepsConfig()) {
                  ArrayList<Double[]> footprints = this.mapProcessor.getFootprints();
                  synchronized(footprints) {
                     for(int i = 0; i < footprints.size(); ++i) {
                        Double[] coords = (Double[])footprints.get(i);
                        this.setColourBuffer(1.0F, 0.1F, 0.1F, 1.0F);
                        this.drawDotOnMap(matrixStack, regularUIObjectConsumer, coords[0] / playerDimDiv - this.cameraX, coords[1] / playerDimDiv - this.cameraZ, 0.0F, (double)1.0F / this.scale);
                     }
                  }
               }

               if (!hiddenUI && (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.ARROW)) {
                  boolean toTheLeft = scaledPlayerX < leftBorder;
                  boolean toTheRight = scaledPlayerX > rightBorder;
                  boolean down = scaledPlayerZ > bottomBorder;
                  boolean up = scaledPlayerZ < topBorder;
                  float configuredR = 1.0F;
                  float configuredG = 1.0F;
                  float configuredB = 1.0F;
                  int effectiveArrowColorIndex = (Integer)configManager.getEffective(WorldMapProfiledConfigOptions.ARROW_COLOR);
                  if (effectiveArrowColorIndex == -2 && !SupportMods.minimap()) {
                     effectiveArrowColorIndex = 0;
                  }

                  if (effectiveArrowColorIndex == -2 && SupportMods.xaeroMinimap.getArrowColorIndex() == -1) {
                     effectiveArrowColorIndex = -1;
                  }

                  if (effectiveArrowColorIndex == -1) {
                     int rgb = Misc.getTeamColour((class_1297)(mc.field_1724 == null ? mc.method_1560() : mc.field_1724));
                     if (rgb == -1) {
                        effectiveArrowColorIndex = 0;
                     } else {
                        configuredR = (float)(rgb >> 16 & 255) / 255.0F;
                        configuredG = (float)(rgb >> 8 & 255) / 255.0F;
                        configuredB = (float)(rgb & 255) / 255.0F;
                     }
                  } else if (effectiveArrowColorIndex == -2) {
                     float[] c = SupportMods.xaeroMinimap.getArrowColor();
                     if (c == null) {
                        effectiveArrowColorIndex = 0;
                     } else {
                        configuredR = c[0];
                        configuredG = c[1];
                        configuredB = c[2];
                     }
                  }

                  if (effectiveArrowColorIndex >= 0) {
                     float[] c = WorldMapConfigConstants.ARROW_COLORS[effectiveArrowColorIndex];
                     configuredR = c[0];
                     configuredG = c[1];
                     configuredB = c[2];
                  }

                  if (!toTheLeft && !toTheRight && !up && !down) {
                     this.setColourBuffer(0.0F, 0.0F, 0.0F, 0.4F);
                     this.drawArrowOnMap(matrixStack, regularUIObjectConsumer, scaledPlayerX - this.cameraX, scaledPlayerZ - (double)2.0F * scaleMultiplier / this.scale - this.cameraZ, this.player.method_36454(), scaleMultiplier / this.scale);
                     this.setColourBuffer(0.0F, 0.0F, 0.0F, 0.9F);
                     this.drawArrowOnMap(matrixStack, regularUIObjectConsumer, scaledPlayerX - this.cameraX, scaledPlayerZ + (double)2.0F * scaleMultiplier / this.scale - this.cameraZ, this.player.method_36454(), scaleMultiplier / this.scale);
                     this.setColourBuffer(configuredR, configuredG, configuredB, 1.0F);
                     this.drawArrowOnMap(matrixStack, regularUIObjectConsumer, scaledPlayerX - this.cameraX, scaledPlayerZ - this.cameraZ, this.player.method_36454(), scaleMultiplier / this.scale);
                  } else {
                     double arrowX = scaledPlayerX;
                     double arrowZ = scaledPlayerZ;
                     float a = 0.0F;
                     if (toTheLeft) {
                        a = up ? 1.5F : (down ? 0.5F : 1.0F);
                        arrowX = leftBorder;
                     } else if (toTheRight) {
                        a = up ? 2.5F : (down ? 3.5F : 3.0F);
                        arrowX = rightBorder;
                     }

                     if (down) {
                        arrowZ = bottomBorder;
                     } else if (up) {
                        if (a == 0.0F) {
                           a = 2.0F;
                        }

                        arrowZ = topBorder;
                     }

                     this.setColourBuffer(0.0F, 0.0F, 0.0F, 0.4F);
                     this.drawFarArrowOnMap(matrixStack, regularUIObjectConsumer, arrowX - this.cameraX, arrowZ - (double)2.0F * scaleMultiplier / this.scale - this.cameraZ, a, scaleMultiplier / this.scale);
                     this.setColourBuffer(0.0F, 0.0F, 0.0F, 0.9F);
                     this.drawFarArrowOnMap(matrixStack, regularUIObjectConsumer, arrowX - this.cameraX, arrowZ + (double)2.0F * scaleMultiplier / this.scale - this.cameraZ, a, scaleMultiplier / this.scale);
                     this.setColourBuffer(configuredR, configuredG, configuredB, 1.0F);
                     this.drawFarArrowOnMap(matrixStack, regularUIObjectConsumer, arrowX - this.cameraX, arrowZ - this.cameraZ, a, scaleMultiplier / this.scale);
                  }
               }

               class_1044 guiTextures = this.field_22787.method_1531().method_4619(WorldMap.guiTextures);
               renderTypeBuffers.endBatch();
               matrixStack.method_22909();
               matrixStack.method_22909();
               if (!skipWorldRenderConfig) {
                  immediateRenderFBO.bindDefaultFramebuffer(mc);
               } else {
                  TextureUtils.clearRenderTargetDepth(this.field_22787.method_1522(), 1.0F);
               }

               int cursorDisplayOffset = 0;
               if (!hiddenUI) {
                  if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.COORDINATES)) {
                     String coordsString = "X: " + this.mouseBlockPosX;
                     if (mouseBlockBottomY != 32767) {
                        coordsString = coordsString + " Y: " + mouseBlockBottomY;
                     }

                     if (hasAmbiguousHeight && mouseBlockTopY != 32767) {
                        coordsString = coordsString + " (" + mouseBlockTopY + ")";
                     }

                     coordsString = coordsString + " Z: " + this.mouseBlockPosZ;
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, this.field_22793, (String)coordsString, this.field_22789 / 2, 2 + cursorDisplayOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                     cursorDisplayOffset += 10;
                  }

                  if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.DISPLAY_HOVERED_BIOME) && pointedAtBiome != null) {
                     class_2960 biomeRL = pointedAtBiome.method_29177();
                     String biomeText = biomeRL == null ? class_1074.method_4662("gui.xaero_wm_unknown_biome", new Object[0]) : class_1074.method_4662("biome." + biomeRL.method_12836() + "." + biomeRL.method_12832(), new Object[0]);
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, this.field_22793, (String)biomeText, this.field_22789 / 2, 2 + cursorDisplayOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  }

                  int subtleTooltipOffset = 12;
                  if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.DISPLAY_ZOOM)) {
                     double var10000 = (double)Math.round(destScale * (double)1000.0F);
                     String zoomString = var10000 / (double)1000.0F + "x";
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (String)zoomString, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  }

                  if (this.mapProcessor.getMapWorld().getCurrentDimension().getFullReloader() != null) {
                     subtleTooltipOffset += 12;
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (class_2561)FULL_RELOAD_IN_PROGRESS, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  }

                  if (this.mapProcessor.getMapWorld().isUsingUnknownDimensionType()) {
                     int var301 = subtleTooltipOffset + 24;
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (class_2561)UNKNOWN_DIMENSION_TYPE2, this.field_22789 / 2, this.field_22790 - var301, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                     subtleTooltipOffset = var301 + 12;
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (class_2561)UNKNOWN_DIMENSION_TYPE1, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  }

                  if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.DISPLAY_CAVE_MODE_START)) {
                     subtleTooltipOffset += 12;
                     if (globalCaveStart != Integer.MAX_VALUE && globalCaveStart != Integer.MIN_VALUE) {
                        String caveModeStartString = class_1074.method_4662("gui.xaero_wm_cave_mode_start_display", new Object[]{globalCaveStart});
                        MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (String)caveModeStartString, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                     }
                  }

                  if (SupportMods.minimap()) {
                     String subWorldNameToRender = SupportMods.xaeroMinimap.getSubWorldNameToRender();
                     if (subWorldNameToRender != null) {
                        subtleTooltipOffset += 24;
                        MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (String)subWorldNameToRender, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                     }
                  }

                  discoveredForHighlights = mouseBlockBottomY != 32767;
                  class_2561 subtleHighlightTooltip = this.mapProcessor.getMapWorld().getCurrentDimension().getHighlightHandler().getBlockHighlightSubtleTooltip(this.mouseBlockPosX, this.mouseBlockPosZ, discoveredForHighlights);
                  if (subtleHighlightTooltip != null) {
                     subtleTooltipOffset += 12;
                     MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, mc.field_1772, (class_2561)subtleHighlightTooltip, this.field_22789 / 2, this.field_22790 - subtleTooltipOffset, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  }
               }

               this.overWaypointsMenu = false;
               this.overPlayersMenu = false;
               if (this.waypointMenu) {
                  if (SupportMods.xaeroMinimap.getWaypointsSorted() != null) {
                     HoveredMapElementHolder<?, ?> hovered = SupportMods.xaeroMinimap.renderWaypointsMenu(guiGraphics, this, this.scale, this.field_22789, this.field_22790, scaledMouseX, scaledMouseY, this.leftMouseButton.isDown, this.leftMouseButton.clicked, this.viewed, mc);
                     if (hovered != null) {
                        this.overWaypointsMenu = true;
                        if (!(hovered.getElement() instanceof MapElementMenuHitbox)) {
                           this.viewed = hovered;
                           this.viewedInList = true;
                        }

                        if (hovered.getElement() instanceof Waypoint && this.leftMouseButton.clicked) {
                           this.cameraDestination = new int[]{(int)((Waypoint)this.viewed.getElement()).getRenderX(), (int)((Waypoint)this.viewed.getElement()).getRenderZ()};
                           this.leftMouseButton.isDown = false;
                           boolean closeWaypointsWhenHopping = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.CLOSE_WAYPOINTS_AFTER_HOP);
                           if (attachedCamera) {
                              attachedCamera = false;
                              if (!closeWaypointsWhenHopping) {
                                 this.shouldReinit = true;
                              }
                           }

                           if (closeWaypointsWhenHopping) {
                              this.onWaypointsButton(this.waypointsButton);
                           }
                        }
                     }
                  }
               } else if (this.playersMenu) {
                  HoveredMapElementHolder<?, ?> hovered = WorldMap.trackedPlayerMenuRenderer.renderMenu(guiGraphics, this, this.scale, this.field_22789, this.field_22790, scaledMouseX, scaledMouseY, this.leftMouseButton.isDown, this.leftMouseButton.clicked, this.viewed, mc);
                  if (hovered != null) {
                     this.overPlayersMenu = true;
                     if (!(hovered.getElement() instanceof MapElementMenuHitbox)) {
                        this.viewed = hovered;
                        this.viewedInList = true;
                     }

                     if (hovered.getElement() instanceof PlayerTrackerMapElement && WorldMap.trackedPlayerMenuRenderer.canJumpTo((PlayerTrackerMapElement)hovered.getElement()) && this.leftMouseButton.clicked) {
                        PlayerTrackerMapElement<?> clickedPlayer = (PlayerTrackerMapElement)this.viewed.getElement();
                        MapDimension clickedPlayerDim = this.mapProcessor.getMapWorld().getDimension(clickedPlayer.getDimension());
                        class_2874 clickedPlayerDimType = MapDimension.getDimensionType(clickedPlayerDim, clickedPlayer.getDimension(), this.mapProcessor.getWorldDimensionTypeRegistry());
                        double clickedPlayerDimDiv = this.mapProcessor.getMapWorld().getCurrentDimension().calculateDimDiv(this.mapProcessor.getWorldDimensionTypeRegistry(), clickedPlayerDimType);
                        double jumpX = clickedPlayer.getX() / clickedPlayerDimDiv;
                        double jumpZ = clickedPlayer.getZ() / clickedPlayerDimDiv;
                        this.cameraDestination = new int[]{(int)jumpX, (int)jumpZ};
                        this.leftMouseButton.isDown = false;
                        if (attachedCamera) {
                           attachedCamera = false;
                           this.shouldReinit = true;
                        }
                     }
                  }
               }

               if (SupportMods.minimap()) {
                  SupportMods.xaeroMinimap.drawSetChange(guiGraphics);
               }

               if (!hiddenUI && SupportMods.pac()) {
                  SupportMods.xaeroPac.onMapRender(this.field_22787, matrixStack, scaledMouseX, scaledMouseY, partialTicks, this.mapProcessor.getWorld().method_27983().method_29177(), highlightChunkX, highlightChunkZ);
               }

               RenderSystem.setProjectionMatrix(projectionBU, projectionTypeBU);
               RenderSystem.getModelViewStack().popMatrix();
            } else if (!mapLoaded) {
               this.renderLoadingScreen(guiGraphics);
            } else if (isLocked) {
               this.renderMessageScreen(guiGraphics, class_1074.method_4662("gui.xaero_current_map_locked1", new Object[0]), class_1074.method_4662("gui.xaero_current_map_locked2", new Object[0]));
            } else if (noWorldMapEffect) {
               this.renderMessageScreen(guiGraphics, class_1074.method_4662("gui.xaero_no_world_map_message", new Object[0]));
            } else if (!allowedBasedOnItem) {
               String configuredMapItemString = (String)configManager.getEffective(WorldMapProfiledConfigOptions.MAP_ITEM);
               String var10002 = class_1074.method_4662("gui.xaero_no_world_map_item_message", new Object[0]);
               String var10003 = mapItem.method_63680().getString();
               this.renderMessageScreen(guiGraphics, var10002, var10003 + " (" + configuredMapItemString + ")");
            }
         } else {
            this.renderLoadingScreen(guiGraphics);
         }

         if (!hiddenUI) {
            this.mapSwitchingGui.renderText(guiGraphics, this.field_22787, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790);
            guiGraphics.method_25290(class_10799.field_56883, WorldMap.guiTextures, this.field_22789 - 34, 2, 0.0F, 37.0F, 32, 32, 256, 256);
         }
      }

      if (!hiddenUI) {
         super.method_25394(guiGraphics, scaledMouseX, scaledMouseY, partialTicks);
      }

      if (this.rightClickMenu != null) {
         this.rightClickMenu.method_25394(guiGraphics, scaledMouseX, scaledMouseY, partialTicks);
      }

      if (!hiddenUI && mc.field_1755 == this) {
         boolean isOverDropdown = this.openDropdown != null && this.openDropdown.method_49606();
         if (!isOverDropdown && !this.renderTooltips(guiGraphics, scaledMouseX, scaledMouseY, partialTicks) && !this.leftMouseButton.isDown && !this.rightMouseButton.isDown) {
            if (this.viewed != null) {
               Tooltip hoveredTooltip = this.hoveredElementTooltipHelper(this.viewed, this.viewedInList);
               if (hoveredTooltip != null) {
                  hoveredTooltip.drawBox(guiGraphics, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790);
               }
            } else {
               synchronized(this.mapProcessor.renderThreadPauseSync) {
                  if (!this.mapProcessor.isRenderingPaused() && this.mapProcessor.getCurrentWorldId() != null && this.mapProcessor.getMapSaveLoad().isRegionDetectionComplete()) {
                     class_2561 bluntHighlightTooltip = this.mapProcessor.getMapWorld().getCurrentDimension().getHighlightHandler().getBlockHighlightBluntTooltip(this.mouseBlockPosX, this.mouseBlockPosZ, discoveredForHighlights);
                     if (bluntHighlightTooltip != null) {
                        (new Tooltip(bluntHighlightTooltip)).drawBox(guiGraphics, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790);
                     }
                  }
               }
            }
         }

         this.mapProcessor.getMessageBoxRenderer().render(guiGraphics, this.mapProcessor.getMessageBox(), this.field_22793, 1, this.field_22790 / 2, false);
      }

      this.leftMouseButton.clicked = this.rightMouseButton.clicked = false;
      this.noUploadingLimits = this.cameraX == cameraXBefore && this.cameraZ == cameraZBefore && scaleBefore == this.scale;
      MapRenderHelper.restoreDefaultShaderBlendState();
   }

   public void method_25420(class_332 guiGraphics, int i, int j, float f) {
   }

   protected void renderPreDropdown(class_332 guiGraphics, int scaledMouseX, int scaledMouseY, float partialTicks) {
      super.renderPreDropdown(guiGraphics, scaledMouseX, scaledMouseY, partialTicks);
      if (this.waypointMenu) {
         SupportMods.xaeroMinimap.getWaypointMenuRenderer().postMapRender(guiGraphics, this, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790, partialTicks);
      }

      if (this.playersMenu) {
         WorldMap.trackedPlayerMenuRenderer.postMapRender(guiGraphics, this, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790, partialTicks);
      }

      this.mapSwitchingGui.postMapRender(guiGraphics, this.field_22787, scaledMouseX, scaledMouseY, this.field_22789, this.field_22790);
   }

   private <E, C> Tooltip hoveredElementTooltipHelper(HoveredMapElementHolder<E, C> hovered, boolean viewedInList) {
      return hovered.getRenderer() == null ? null : hovered.getRenderer().getReader().getTooltip(hovered.getElement(), hovered.getRenderer().getContext(), viewedInList);
   }

   private void renderLoadingScreen(class_332 guiGraphics) {
      this.renderMessageScreen(guiGraphics, "Preparing World Map...");
   }

   private void renderMessageScreen(class_332 guiGraphics, String message) {
      this.renderMessageScreen(guiGraphics, message, (String)null);
   }

   private void renderMessageScreen(class_332 guiGraphics, String message, String message2) {
      guiGraphics.method_25294(0, 0, this.field_22787.method_22683().method_4489(), this.field_22787.method_22683().method_4506(), -16777216);
      guiGraphics.method_25300(this.field_22787.field_1772, message, this.field_22787.method_22683().method_4486() / 2, this.field_22787.method_22683().method_4502() / 2, -1);
      if (message2 != null) {
         guiGraphics.method_25300(this.field_22787.field_1772, message2, this.field_22787.method_22683().method_4486() / 2, this.field_22787.method_22683().method_4502() / 2 + 10, -1);
      }

   }

   public void drawDotOnMap(class_4587 matrixStack, class_4588 guiLinearBuffer, double x, double z, float angle, double sc) {
      this.drawObjectOnMap(matrixStack, guiLinearBuffer, x, z, angle, sc, 2.5F, 2.5F, 0, 69, 5, 5);
   }

   public void drawArrowOnMap(class_4587 matrixStack, class_4588 guiLinearBuffer, double x, double z, float angle, double sc) {
      this.drawObjectOnMap(matrixStack, guiLinearBuffer, x, z, angle, sc, 13.0F, 5.0F, 0, 0, 26, 28);
   }

   public void drawFarArrowOnMap(class_4587 matrixStack, class_4588 guiLinearBuffer, double x, double z, float angle, double sc) {
      this.drawObjectOnMap(matrixStack, guiLinearBuffer, x, z, angle * 90.0F, sc, 27.0F, 13.0F, 26, 0, 54, 13);
   }

   public void drawObjectOnMap(class_4587 matrixStack, class_4588 guiLinearBuffer, double x, double z, float angle, double sc, float offX, float offY, int textureX, int textureY, int w, int h) {
      matrixStack.method_22903();
      matrixStack.method_22904(x, z, (double)0.0F);
      matrixStack.method_22905((float)sc, (float)sc, 1.0F);
      if (angle != 0.0F) {
         OptimizedMath.rotatePose(matrixStack, angle, OptimizedMath.ZP);
      }

      Matrix4f matrix = matrixStack.method_23760().method_23761();
      renderTexturedModalRect(matrix, guiLinearBuffer, -offX, -offY, textureX, textureY, (float)w, (float)h, 256.0F, 256.0F, this.colourBuffer[0], this.colourBuffer[1], this.colourBuffer[2], this.colourBuffer[3]);
      matrixStack.method_22909();
   }

   public static void renderTexturedModalRectWithLighting3(Matrix4f matrix, float x, float y, float width, float height, GpuTexture texture, boolean hasLight, MultiTextureRenderTypeRenderer renderer) {
      buildTexturedModalRectWithLighting(matrix, renderer.begin(texture), x, y, width, height);
   }

   public static void renderTexturedModalSubRectWithLighting(Matrix4f matrix, float x, float y, float textureX1, float textureY1, float textureX2, float textureY2, float width, float height, GpuTexture texture, boolean hasLight, MultiTextureRenderTypeRenderer renderer) {
      buildTexturedModalSubRectWithLighting(matrix, renderer.begin(texture), x, y, textureX1, textureY1, textureX2, textureY2, width, height);
   }

   public static void buildTexturedModalRectWithLighting(Matrix4f matrix, class_287 vertexBuffer, float x, float y, float width, float height) {
      vertexBuffer.method_22918(matrix, x + 0.0F, y + height, 0.0F).method_22913(0.0F, 1.0F);
      vertexBuffer.method_22918(matrix, x + width, y + height, 0.0F).method_22913(1.0F, 1.0F);
      vertexBuffer.method_22918(matrix, x + width, y + 0.0F, 0.0F).method_22913(1.0F, 0.0F);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + 0.0F, 0.0F).method_22913(0.0F, 0.0F);
   }

   public static void buildTexturedModalSubRectWithLighting(Matrix4f matrix, class_287 vertexBuffer, float x, float y, float textureX1, float textureY1, float textureX2, float textureY2, float width, float height) {
      vertexBuffer.method_22918(matrix, x + 0.0F, y + height, 0.0F).method_22913(textureX1, textureY2);
      vertexBuffer.method_22918(matrix, x + width, y + height, 0.0F).method_22913(textureX2, textureY2);
      vertexBuffer.method_22918(matrix, x + width, y + 0.0F, 0.0F).method_22913(textureX2, textureY1);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + 0.0F, 0.0F).method_22913(textureX1, textureY1);
   }

   public static void renderTexturedModalRect(Matrix4f matrix, class_4588 vertexBuffer, float x, float y, int textureX, int textureY, float width, float height, float textureWidth, float textureHeight, float r, float g, float b, float a) {
      float normalizedTextureX = (float)textureX / textureWidth;
      float normalizedTextureY = (float)textureY / textureHeight;
      float normalizedTextureX2 = ((float)textureX + width) / textureWidth;
      float normalizedTextureY2 = ((float)textureY + height) / textureHeight;
      vertexBuffer.method_22918(matrix, x + 0.0F, y + height, 0.0F).method_22915(r, g, b, a).method_22913(normalizedTextureX, normalizedTextureY2);
      vertexBuffer.method_22918(matrix, x + width, y + height, 0.0F).method_22915(r, g, b, a).method_22913(normalizedTextureX2, normalizedTextureY2);
      vertexBuffer.method_22918(matrix, x + width, y + 0.0F, 0.0F).method_22915(r, g, b, a).method_22913(normalizedTextureX2, normalizedTextureY);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + 0.0F, 0.0F).method_22915(r, g, b, a).method_22913(normalizedTextureX, normalizedTextureY);
   }

   public void mapClicked(int button, int x, int y) {
      if (button == 1) {
         if (this.viewedOnMousePress == null || !this.viewedOnMousePress.isRightClickValid() || this.viewedOnMousePress.getElement() instanceof Waypoint && !SupportMods.xaeroMinimap.waypointExists((Waypoint)this.viewedOnMousePress.getElement())) {
            this.handleRightClick(this, (int)((double)x / this.screenScale), (int)((double)y / this.screenScale));
         } else {
            this.handleRightClick(this.viewedOnMousePress, (int)((double)x / this.screenScale), (int)((double)y / this.screenScale));
            this.mouseDownPosX = -1;
            this.mouseDownPosY = -1;
            this.mapTileSelection = null;
         }
      }

   }

   private void handleRightClick(IRightClickableElement target, int x, int y) {
      if (this.rightClickMenu != null) {
         this.rightClickMenu.setClosed(true);
      }

      if (hiddenUI) {
         this.mapTileSelection = null;
      } else {
         this.rightClickScreenWidth = this.field_22789;
         this.rightClickScreenHeight = this.field_22790;
         this.rightClickMenu = GuiRightClickMenu.getMenu(target, this, x, y, 150);
      }
   }

   public boolean method_25400(class_11905 event) {
      boolean result = super.method_25400(event);
      if (this.waypointMenu && SupportMods.xaeroMinimap.getWaypointMenuRenderer().charTyped()) {
         return true;
      } else {
         return this.playersMenu && WorldMap.trackedPlayerMenuRenderer.charTyped() ? true : result;
      }
   }

   public boolean method_25404(class_11908 event) {
      int par1 = event.comp_4795();
      int par2 = event.comp_4796();
      if (par1 == 258) {
         ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
         boolean minimapRadarConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.MINIMAP_RADAR);
         if (this.tabPressed && SupportMods.minimap() && minimapRadarConfig && class_310.method_1551().field_1690.field_1907.method_1417(event)) {
            return true;
         }

         this.tabPressed = true;
      }

      if (par1 == 290) {
         this.toggleHiddenUI();
         return true;
      } else {
         class_3675.class_307 inputType = par1 != -1 ? class_307.field_1668 : class_307.field_1671;
         int inputCode = par1 != -1 ? par1 : par2;
         if (this.isMapNavigationInput(inputType, inputCode)) {
            return true;
         } else {
            boolean result = super.method_25404(event);
            if (this.isUsingTextField()) {
               if (this.waypointMenu && SupportMods.xaeroMinimap.getWaypointMenuRenderer().keyPressed(this, par1)) {
                  result = true;
               } else if (this.playersMenu && WorldMap.trackedPlayerMenuRenderer.keyPressed(this, par1)) {
                  result = true;
               } else if (this.hopMenu && this.method_25399() == this.hopInputBox && par1 == 257) {
                  this.activateHopToCoords();
                  result = true;
               }

               return result;
            } else {
               return this.onInputPress(inputType, inputCode) || result;
            }
         }
      }
   }

   public boolean method_16803(class_11908 event) {
      int par1 = event.comp_4795();
      int par2 = event.comp_4796();
      if (par1 == 258) {
         this.tabPressed = false;
      }

      return this.onInputRelease(par1 != -1 ? class_307.field_1668 : class_307.field_1671, par1 != -1 ? par1 : par2) ? true : super.method_16803(event);
   }

   private static long bytesToMb(long bytes) {
      return bytes / 1024L / 1024L;
   }

   private void setColourBuffer(float r, float g, float b, float a) {
      this.colourBuffer[0] = r;
      this.colourBuffer[1] = g;
      this.colourBuffer[2] = b;
      this.colourBuffer[3] = a;
   }

   private boolean isUsingTextField() {
      class_339 currentFocused = (class_339)this.method_25399();
      return currentFocused != null && currentFocused.method_25370() && currentFocused instanceof class_342;
   }

   public void method_25393() {
      super.method_25393();
      if (this.waypointMenu) {
         SupportMods.xaeroMinimap.getWaypointMenuRenderer().tick();
      }

      if (this.playersMenu) {
         WorldMap.trackedPlayerMenuRenderer.tick();
      }

      this.caveModeOptions.tick(this);
   }

   public class_304 getTrackedPlayerKeyBinding() {
      return SupportMods.minimap() ? SupportMods.xaeroMinimap.getToggleAllyPlayersKey() : ControlsRegister.keyToggleTrackedPlayers;
   }

   private boolean onInputPress(class_3675.class_307 type, int code) {
      if (hiddenUI && !this.isExitInput(type, code)) {
         hiddenUI = false;
      }

      if (KeyMappingUtils.inputMatches(type, code, ControlsRegister.keyOpenSettings, 0)) {
         this.onSettingsButton(this.settingsButton);
         return true;
      } else {
         boolean result = false;
         if (KeyMappingUtils.inputMatches(type, code, this.field_22787.field_1690.field_1907, 0)) {
            this.field_22787.field_1690.field_1907.method_23481(true);
            result = true;
         }

         if (KeyMappingUtils.inputMatches(type, code, ControlsRegister.keyOpenMap, 0)) {
            this.goBack();
            result = true;
         }

         if (KeyMappingUtils.inputMatches(type, code, this.getTrackedPlayerKeyBinding(), 0)) {
            WorldMap.trackedPlayerMenuRenderer.onShowPlayersButton(this, this.field_22789, this.field_22790);
            return true;
         } else {
            if ((type == class_307.field_1668 && code == 257 || KeyMappingUtils.inputMatches(type, code, ControlsRegister.keyQuickConfirm, 0)) && this.mapSwitchingGui.active) {
               this.mapSwitchingGui.confirm(this, this.field_22787, this.field_22789, this.field_22790);
               result = true;
            }

            if (KeyMappingUtils.inputMatches(type, code, ControlsRegister.keyToggleDimension, 1)) {
               this.onDimensionToggleButton(this.dimensionToggleButton);
               result = true;
            }

            if (SupportMods.minimap()) {
               SupportMods.xaeroMinimap.onMapKeyPressed(type, code, this);
               result = true;
            }

            if (SupportMods.pac()) {
               result = SupportMods.xaeroPac.onMapKeyPressed(type, code, this) || result;
            }

            IRightClickableElement hoverTarget = this.getHoverTarget();
            if (hoverTarget != null && type == class_307.field_1668) {
               boolean isValid = hoverTarget.isRightClickValid();
               if (isValid) {
                  if (hoverTarget instanceof HoveredMapElementHolder && ((HoveredMapElementHolder)hoverTarget).getElement() instanceof Waypoint) {
                     switch (code) {
                        case 72:
                           SupportMods.xaeroMinimap.disableWaypoint((Waypoint)((HoveredMapElementHolder)hoverTarget).getElement());
                           this.closeRightClick();
                           result = true;
                           break;
                        case 261:
                           SupportMods.xaeroMinimap.deleteWaypoint((Waypoint)((HoveredMapElementHolder)hoverTarget).getElement());
                           this.closeRightClick();
                           result = true;
                     }
                  } else if (SupportMods.pac() && hoverTarget instanceof HoveredMapElementHolder && ((HoveredMapElementHolder)hoverTarget).getElement() instanceof PlayerTrackerMapElement) {
                     switch (code) {
                        case 67:
                           SupportMods.xaeroPac.openPlayerConfigScreen(this, this, (PlayerTrackerMapElement)((HoveredMapElementHolder)hoverTarget).getElement());
                           this.closeRightClick();
                           result = true;
                     }
                  }
               } else {
                  this.closeRightClick();
               }
            }

            return result;
         }
      }
   }

   private double getCurrentMapCoordinateScale() {
      return this.mapProcessor.getMapWorld().getCurrentDimension().calculateDimScale(this.mapProcessor.getWorldDimensionTypeRegistry());
   }

   private boolean onInputRelease(class_3675.class_307 type, int code) {
      boolean result = false;
      if (KeyMappingUtils.inputMatches(type, code, this.field_22787.field_1690.field_1907, 0)) {
         this.field_22787.field_1690.field_1907.method_23481(false);
         result = true;
      }

      if (SupportMods.minimap() && SupportMods.xaeroMinimap.onMapKeyReleased(type, code, this)) {
         result = true;
      }

      if (SupportMods.minimap() && this.lastViewedDimensionId != null && !this.isUsingTextField()) {
         ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
         boolean waypointsConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINTS);
         int waypointDestinationX = this.mouseBlockPosX;
         int waypointDestinationY = this.mouseBlockPosY;
         int waypointDestinationZ = this.mouseBlockPosZ;
         double waypointDestinationCoordinateScale = this.mouseBlockCoordinateScale;
         boolean waypointDestinationRightClick = false;
         if (this.rightClickMenu != null && this.rightClickMenu.getTarget() == this) {
            waypointDestinationX = this.rightClickX;
            waypointDestinationY = this.rightClickY;
            waypointDestinationZ = this.rightClickZ;
            waypointDestinationCoordinateScale = this.rightClickCoordinateScale;
            waypointDestinationRightClick = true;
         }

         if (KeyMappingUtils.inputMatches(type, code, SupportMods.xaeroMinimap.getWaypointKeyBinding(), 0) && waypointsConfig) {
            SupportMods.xaeroMinimap.createWaypoint(this, waypointDestinationX, waypointDestinationY == 32767 ? 32767 : waypointDestinationY + 1, waypointDestinationZ, waypointDestinationCoordinateScale, waypointDestinationRightClick);
            this.closeRightClick();
            result = true;
         }

         if (KeyMappingUtils.inputMatches(type, code, SupportMods.xaeroMinimap.getTempWaypointKeyBinding(), 0) && waypointsConfig) {
            this.closeRightClick();
            SupportMods.xaeroMinimap.createTempWaypoint(waypointDestinationX, waypointDestinationY == 32767 ? 32767 : waypointDestinationY + 1, waypointDestinationZ, waypointDestinationCoordinateScale, waypointDestinationRightClick);
            result = true;
         }

         IRightClickableElement hoverTarget = this.getHoverTarget();
         if (hoverTarget != null && !KeyMappingUtils.inputMatches(type, code, ControlsRegister.keyOpenMap, 0) && type == class_307.field_1668) {
            boolean isValid = hoverTarget.isRightClickValid();
            if (isValid) {
               if (hoverTarget instanceof HoveredMapElementHolder && ((HoveredMapElementHolder)hoverTarget).getElement() instanceof Waypoint) {
                  switch (code) {
                     case 69:
                        SupportMods.xaeroMinimap.openWaypoint(this, (Waypoint)((HoveredMapElementHolder)hoverTarget).getElement());
                        this.closeRightClick();
                        result = true;
                        break;
                     case 84:
                        SupportMods.xaeroMinimap.teleportToWaypoint(this, (Waypoint)((HoveredMapElementHolder)hoverTarget).getElement());
                        this.closeRightClick();
                        result = true;
                  }
               } else if (hoverTarget instanceof HoveredMapElementHolder && ((HoveredMapElementHolder)hoverTarget).getElement() instanceof PlayerTrackerMapElement) {
                  switch (code) {
                     case 84:
                        (new PlayerTeleporter()).teleportToPlayer(this, this.mapProcessor.getMapWorld(), (PlayerTrackerMapElement)((HoveredMapElementHolder)hoverTarget).getElement());
                        this.closeRightClick();
                        result = true;
                  }
               }
            } else {
               this.closeRightClick();
            }
         }
      }

      return result;
   }

   private IRightClickableElement getHoverTarget() {
      return (IRightClickableElement)(this.rightClickMenu != null ? this.rightClickMenu.getTarget() : this.viewed);
   }

   private void unfocusAll() {
      class_364 currentFocus = this.method_25399();
      if (currentFocus instanceof class_342 editBox) {
         editBox.method_25365(false);
      }

      if (SupportMods.minimap()) {
         SupportMods.xaeroMinimap.getWaypointMenuRenderer().unfocusAll();
      }

      WorldMap.trackedPlayerMenuRenderer.unfocusAll();
      this.caveModeOptions.unfocusAll();
      this.method_25395((class_364)null);
   }

   public void closeRightClick() {
      if (this.rightClickMenu != null) {
         this.rightClickMenu.setClosed(true);
      }

   }

   public void onRightClickClosed() {
      this.rightClickMenu = null;
      this.mapTileSelection = null;
   }

   private void closeDropdowns() {
      if (this.openDropdown != null) {
         this.openDropdown.setClosed(true);
      }

   }

   public ArrayList<RightClickOption> getRightClickOptions() {
      ArrayList<RightClickOption> options = new ArrayList();
      options.add(new RightClickOption("gui.xaero_right_click_map_title", options.size(), this) {
         public void onAction(class_437 screen) {
         }
      });
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      boolean coordinatesConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.COORDINATES);
      boolean waypointsConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINTS);
      if (coordinatesConfig && (!SupportMods.minimap() || !SupportMods.xaeroMinimap.hidingWaypointCoordinates())) {
         if (this.mapTileSelection != null) {
            String chunkOption = this.mapTileSelection.getStartX() == this.mapTileSelection.getEndX() && this.mapTileSelection.getStartZ() == this.mapTileSelection.getEndZ() ? String.format("C: (%d;%d)", this.mapTileSelection.getLeft(), this.mapTileSelection.getTop()) : String.format("C: (%d;%d):(%d;%d)", this.mapTileSelection.getLeft(), this.mapTileSelection.getTop(), this.mapTileSelection.getRight(), this.mapTileSelection.getBottom());
            options.add(new RightClickOption(chunkOption, class_2583.field_24360.method_10977(class_124.field_1080), options.size(), this) {
               public void onAction(class_437 screen) {
               }
            });
         }

         String coordsText = String.format(this.rightClickY != 32767 ? "X: %1$d, Y: %2$d, Z: %3$d" : "X: %1$d, Z: %3$d", this.rightClickX, this.rightClickY, this.rightClickZ);
         options.add(new RightClickOption(coordsText, class_2583.field_24360.method_10977(class_124.field_1080), options.size(), this) {
            public void onAction(class_437 screen) {
            }
         });
      }

      if (SupportMods.minimap() && waypointsConfig) {
         options.add((new RightClickOption("gui.xaero_right_click_map_create_waypoint", options.size(), this) {
            public void onAction(class_437 screen) {
               SupportMods.xaeroMinimap.createWaypoint(GuiMap.this, GuiMap.this.rightClickX, GuiMap.this.rightClickY == 32767 ? 32767 : GuiMap.this.rightClickY + 1, GuiMap.this.rightClickZ, GuiMap.this.rightClickCoordinateScale, true);
            }
         }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent(SupportMods.xaeroMinimap.getWaypointKeyBinding())}));
         options.add((new RightClickOption("gui.xaero_right_click_map_create_temporary_waypoint", options.size(), this) {
            public void onAction(class_437 screen) {
               SupportMods.xaeroMinimap.createTempWaypoint(GuiMap.this.rightClickX, GuiMap.this.rightClickY == 32767 ? 32767 : GuiMap.this.rightClickY + 1, GuiMap.this.rightClickZ, GuiMap.this.rightClickCoordinateScale, true);
            }
         }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent(SupportMods.xaeroMinimap.getTempWaypointKeyBinding())}));
      }

      MapDimension currentDimension = this.mapProcessor.getMapWorld().getCurrentDimension();
      if (this.field_22787.field_1761.method_2908() && currentDimension == null) {
         options.add(new RightClickOption("gui.xaero_right_click_map_cant_teleport_world", options.size(), this) {
            public void onAction(class_437 screen) {
            }
         });
      } else {
         boolean teleportAllowed = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.MAP_TELEPORT_ALLOWED);
         if (!teleportAllowed || this.rightClickY == 32767 && this.field_22787.field_1761.method_2908()) {
            if (!teleportAllowed) {
               options.add(new RightClickOption("gui.xaero_wm_right_click_map_teleport_not_allowed", options.size(), this) {
                  public void onAction(class_437 screen) {
                  }
               });
            } else {
               options.add(new RightClickOption("gui.xaero_right_click_map_cant_teleport", options.size(), this) {
                  public void onAction(class_437 screen) {
                  }
               });
            }
         } else {
            options.add(new RightClickOption("gui.xaero_right_click_map_teleport", options.size(), this) {
               public void onAction(class_437 screen) {
                  MapDimension currentDimension = GuiMap.this.mapProcessor.getMapWorld().getCurrentDimension();
                  if ((!GuiMap.this.field_22787.field_1761.method_2908() || currentDimension != null) && (GuiMap.this.rightClickY != 32767 || !GuiMap.this.field_22787.field_1761.method_2908())) {
                     class_5321<class_1937> tpDim = GuiMap.this.rightClickDim != GuiMap.this.field_22787.field_1687.method_27983() ? GuiMap.this.rightClickDim : null;
                     (new MapTeleporter()).teleport(GuiMap.this, GuiMap.this.mapProcessor.getMapWorld(), GuiMap.this.rightClickX, GuiMap.this.rightClickY == 32767 ? 32767 : GuiMap.this.rightClickY + 1, GuiMap.this.rightClickZ, tpDim);
                  }

               }
            });
         }
      }

      if (SupportMods.minimap()) {
         options.add(new RightClickOption("gui.xaero_right_click_map_share_location", options.size(), this) {
            public void onAction(class_437 screen) {
               SupportMods.xaeroMinimap.shareLocation(GuiMap.this, GuiMap.this.rightClickX, GuiMap.this.rightClickY == 32767 ? 32767 : GuiMap.this.rightClickY + 1, GuiMap.this.rightClickZ);
            }
         });
         if (waypointsConfig) {
            options.add((new RightClickOption("gui.xaero_right_click_map_waypoints_menu", options.size(), this) {
               public void onAction(class_437 screen) {
                  SupportMods.xaeroMinimap.openWaypointsMenu(GuiMap.this.field_22787, GuiMap.this);
               }
            }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent(SupportMods.xaeroMinimap.getTempWaypointsMenuKeyBinding())}));
         }
      }

      if (SupportMods.pac()) {
         SupportMods.xaeroPac.addRightClickOptions(this, options, this.mapTileSelection, this.mapProcessor);
      }

      options.add(new RightClickOption("gui.xaero_right_click_box_map_export", options.size(), this) {
         public void onAction(class_437 screen) {
            GuiMap.this.onExportButton(GuiMap.this.exportButton);
         }
      });
      options.add((new RightClickOption("gui.xaero_right_click_box_map_settings", options.size(), this) {
         public void onAction(class_437 screen) {
            GuiMap.this.onSettingsButton(GuiMap.this.settingsButton);
         }
      }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent(ControlsRegister.keyOpenSettings)}));
      DistanceUtils.addDistanceRightClickOption((double)this.rightClickX, (double)this.rightClickY, (double)this.rightClickZ, false, this, this.field_22787.method_1560(), this.field_22787.method_61966().method_60637(true), configManager, options);
      return options;
   }

   public boolean isRightClickValid() {
      return true;
   }

   public int getRightClickTitleBackgroundColor() {
      return -10461088;
   }

   public boolean shouldSkipWorldRender() {
      return true;
   }

   public double getUserScale() {
      return this.userScale;
   }

   public class_4185 getRadarButton() {
      return this.radarButton;
   }

   public void onDropdownOpen(DropDownWidget menu) {
      super.onDropdownOpen(menu);
      this.unfocusAll();
   }

   public void onDropdownClosed(DropDownWidget menu) {
      super.onDropdownClosed(menu);
      if (menu == this.rightClickMenu) {
         this.onRightClickClosed();
      }

   }

   public void onCaveModeStartSet() {
      this.caveModeOptions.onCaveModeStartSet(this);
   }

   public MapDimension getDimensionOnInit() {
      return this.dimensionOnInit;
   }

   public MapProcessor getMapProcessor() {
      return this.mapProcessor;
   }

   public void enableCaveModeOptions() {
      if (!this.caveModeOptions.isEnabled()) {
         this.caveModeOptions.toggle(this);
      }

   }

   public void method_37066(class_364 current) {
      super.method_37066(current);
   }

   private void toggleHiddenUI() {
      this.unfocusAll();
      hiddenUI = !hiddenUI;
      if (hiddenUI && this.rightClickMenu != null) {
         this.rightClickMenu.setClosed(true);
      }

   }

   private boolean isMapNavigationInput(class_3675.class_307 inputType, int inputCode) {
      if (inputType == class_307.field_1668 && inputCode == 341) {
         return true;
      } else if (inputType == class_307.field_1668 && inputCode == 345) {
         return true;
      } else {
         boolean isZoomIn = KeyMappingUtils.inputMatches(inputType, inputCode, ControlsRegister.keyZoomIn, 0);
         boolean isZoom = isZoomIn || KeyMappingUtils.inputMatches(inputType, inputCode, ControlsRegister.keyZoomOut, 0);
         return isZoom;
      }
   }

   private boolean isExitInput(class_3675.class_307 inputType, int inputCode) {
      if (KeyMappingUtils.inputMatches(inputType, inputCode, ControlsRegister.keyOpenMap, 0)) {
         return true;
      } else {
         return inputType == class_307.field_1668 && inputCode == 256;
      }
   }

   private void renderServerChunkRadius(int playerChunkX, int playerChunkZ, int flooredCameraX, int flooredCameraZ, class_4587 matrixStack, class_4588 overlayBuffer, ClientConfigManager configManager) {
      if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.SERVER_CHUNK_RADIUS)) {
         int serverChunkRadius = ((IWorldMapClientPlayNetHandler)this.field_22787.method_1562()).getXaero_serverChunkRadius();
         int serverChunkLeft = playerChunkX - serverChunkRadius;
         int serverChunkTop = playerChunkZ - serverChunkRadius;
         int serverChunkRight = playerChunkX + serverChunkRadius;
         int serverChunkBottom = playerChunkZ + serverChunkRadius;
         MapRenderHelper.renderDynamicChunkHighlight(serverChunkLeft, serverChunkRight, serverChunkTop, serverChunkBottom, flooredCameraX, flooredCameraZ, 1.0F, 0.0F, 1.0F, 0.6F, 1.0F, 0.0F, 1.0F, this.scale < (double)1.5F ? 0.2F * (1.5F - (float)this.scale) : 0.0F, matrixStack, overlayBuffer);
      }
   }

   static {
      identityMatrix.identity();
   }
}
