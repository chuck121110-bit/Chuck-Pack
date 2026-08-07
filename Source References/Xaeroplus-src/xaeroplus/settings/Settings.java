package xaeroplus.settings;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import xaero.map.WorldMapSession;
import xaeroplus.Globals;
import xaeroplus.feature.extensions.DrawOrderScreen;
import xaeroplus.feature.extensions.GuiMinimapWaypointTeleportCommandSettings;
import xaeroplus.feature.waypoint.WaypointAPI;
import xaeroplus.feature.waypoint.eta.WaypointEtaManager;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.BaritoneGoalSync;
import xaeroplus.module.impl.BaritonePathSync;
import xaeroplus.module.impl.Beacons;
import xaeroplus.module.impl.Breadcrumbs;
import xaeroplus.module.impl.FpsLimiter;
import xaeroplus.module.impl.Highways;
import xaeroplus.module.impl.LavaColumns;
import xaeroplus.module.impl.LiquidNewChunks;
import xaeroplus.module.impl.MapArtGrid;
import xaeroplus.module.impl.OldBiomes;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;
import xaeroplus.module.impl.Pearls;
import xaeroplus.module.impl.PortalSkipDetection;
import xaeroplus.module.impl.Portals;
import xaeroplus.module.impl.RegionGrid;
import xaeroplus.module.impl.RenderDistance;
import xaeroplus.module.impl.SpawnChunks;
import xaeroplus.module.impl.SpawnChunksPlayer;
import xaeroplus.module.impl.SpawnPoint;
import xaeroplus.module.impl.TeleportFailNotifier;
import xaeroplus.module.impl.WorldBorder;
import xaeroplus.module.impl.WorldTools;
import xaeroplus.util.BaritoneHelper;
import xaeroplus.util.ColorHelper;
import xaeroplus.util.WorldToolsHelper;

public final class Settings extends SettingRegistry {
   public static final Settings REGISTRY = new Settings();
   public final StringSetting drawOrderSetting;
   public final BooleanSetting transparentWorldmapBackgroundSetting;
   public final BooleanSetting fastZipWrite;
   public final BooleanSetting writesWhileDimSwitched;
   public final BooleanSetting baritoneWaypointSyncSetting;
   public final BooleanSetting spawnPointSetting;
   public final BooleanSetting pearlWaypointsSetting;
   public final BooleanSetting persistMapDimensionSwitchSetting;
   public final BooleanSetting radarWhileDimensionSwitchedSetting;
   public final BooleanSetting transparentObsidianRoofSetting;
   public final DoubleSetting transparentObsidianRoofYSetting;
   public final DoubleSetting transparentObsidianRoofDarkeningSetting;
   public final DoubleSetting transparentObsidianRoofSnowOpacitySetting;
   public final BooleanSetting crossDimensionCursorCoordinates;
   public final BooleanSetting owAutoWaypointDimension;
   public final BooleanSetting nullOverworldDimensionFolder;
   public final EnumSetting<DataFolderResolutionMode> dataFolderResolutionMode;
   public final BooleanSetting netherCaveFix;
   public final BooleanSetting disableXaeroInternetAccess;
   public final BooleanSetting expandSettingEntries;
   public final BooleanSetting teleportFailNotifier;
   public final DoubleSetting teleportFailNotifierDelay;
   public final BooleanSetting disableTeleportation;
   public final BooleanSetting sodiumSettingIntegration;
   public final BooleanSetting worldMapUIAdditions;
   public final BooleanSetting waypointsListUIAdditions;
   public final BooleanSetting atomicMoveAndReplace;
   public final BooleanSetting optimizeRegionDetectionLookups;
   public final BooleanSetting paletteNewChunksEnabledSetting;
   public final BooleanSetting paletteNewChunksVersionUpgradedChunks;
   public final BooleanSetting paletteNewChunksSaveLoadToDisk;
   public final DoubleSetting paletteNewChunksAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> paletteNewChunksColorSetting;
   public final BooleanSetting paletteNewChunksRenderInverse;
   public final BooleanSetting paletteNewChunksRescan;
   public final EnumSetting<PaletteNewChunksRescanAge> paletteNewChunksMinRescanAge;
   public final BooleanSetting oldChunksEnabledSetting;
   public final BooleanSetting oldChunksInverse;
   public final BooleanSetting oldChunksSaveLoadToDisk;
   public final DoubleSetting oldChunksAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> oldChunksColorSetting;
   public final BooleanSetting portalsEnabledSetting;
   public final BooleanSetting portalsSaveLoadToDisk;
   public final DoubleSetting portalsAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> portalsColorSetting;
   public final BooleanSetting lavaColumnsEnabledSetting;
   public final DoubleSetting lavaColumnsMinHeight;
   public final DoubleSetting lavaColumnsAlphaShift;
   public final DoubleSetting lavaColumnsAlphaStep;
   public final EnumSetting<ColorHelper.HighlightColor> lavaColumnsColor;
   public final BooleanSetting lavaColumnsSaveLoadToDisk;
   public final BooleanSetting oldBiomesSetting;
   public final BooleanSetting oldBiomesSaveToDiskSetting;
   public final DoubleSetting oldBiomesAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> oldBiomesColorSetting;
   public final BooleanSetting liquidNewChunksEnabledSetting;
   public final BooleanSetting liquidNewChunksSaveLoadToDisk;
   public final DoubleSetting liquidNewChunksAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> liquidNewChunksColorSetting;
   public final BooleanSetting liquidNewChunksInverseHighlightsSetting;
   public final EnumSetting<ColorHelper.HighlightColor> liquidNewChunksInverseColorSetting;
   public final BooleanSetting liquidNewChunksOnlyAboveY0Setting;
   public final BooleanSetting worldToolsEnabledSetting;
   public DoubleSetting worldToolsAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> worldToolsColorSetting;
   public final BooleanSetting portalSkipDetectionEnabledSetting;
   public final DoubleSetting portalSkipDetectionAlphaSetting;
   public final EnumSetting<ColorHelper.HighlightColor> portalSkipDetectionColorSetting;
   public final DoubleSetting portalSkipPortalRadius;
   public final DoubleSetting portalSkipDetectionSearchDelayTicksSetting;
   public final BooleanSetting portalSkipNewChunksSetting;
   public final BooleanSetting portalSkipOldChunkInverseSetting;
   public final BooleanSetting breadcrumbsEnabledSetting;
   public final BooleanSetting breadcrumbsSaveLoadToDiskSetting;
   public final EnumSetting<BreadcrumbsMode> breadcrumbsModeSetting;
   public final DoubleSetting breadcrumbsChunkRadiusSetting;
   public final EnumSetting<ColorHelper.HighlightColor> breadcrumbsColorSetting;
   public final DoubleSetting breadcrumbsOpacitySetting;
   public final BooleanSetting baritonePathSyncSetting;
   public final EnumSetting<ColorHelper.HighlightColor> baritonePathSyncColorSetting;
   public final DoubleSetting baritonePathSyncOpacity;
   public final BooleanSetting highwayHighlightsSetting;
   public final EnumSetting<HighwayWidth> highwayWidthSetting;
   public final EnumSetting<ColorHelper.HighlightColor> highwaysColorSetting;
   public final DoubleSetting highwaysColorAlphaSetting;
   public final BooleanSetting showRenderDistanceSetting;
   public final BooleanSetting showWorldBorderSetting;
   public final BooleanSetting spawnChunksEnabledSetting;
   public final BooleanSetting playerSpawnChunksEnabledSetting;
   public final BooleanSetting spawnChunksRedstoneProcessingEnabled;
   public final BooleanSetting spawnChunksOuterChunksEnabled;
   public final EnumSetting<ColorHelper.HighlightColor> spawnChunksEntityProcessingColor;
   public final EnumSetting<ColorHelper.HighlightColor> spawnChunksRedstoneProcessingColor;
   public final EnumSetting<ColorHelper.HighlightColor> spawnChunksLazyChunksColor;
   public final EnumSetting<ColorHelper.HighlightColor> spawnChunksOuterChunksColor;
   public final BooleanSetting mapArtGridEnabledSetting;
   public final EnumSetting<ColorHelper.HighlightColor> mapArtGridColorSetting;
   public final BooleanSetting regionGridEnabledSetting;
   public final EnumSetting<ColorHelper.HighlightColor> regionGridColorSetting;
   public final BooleanSetting regionGridTextSetting;
   public final BooleanSetting beaconsOverlaySetting;
   public final BooleanSetting minimapFpsLimiter;
   public final DoubleSetting minimapFpsLimit;
   public final DoubleSetting minimapScaleMultiplierSetting;
   public final DoubleSetting minimapSizeMultiplierSetting;
   public final DoubleSetting minimapRenderZOffsetSetting;
   public final BooleanSetting alwaysRenderPlayerWithNameOnRadar;
   public final BooleanSetting alwaysRenderPlayerIconOnRadar;
   public final BooleanSetting waypointBeacons;
   public final DoubleSetting waypointBeaconScaleMin;
   public final DoubleSetting waypointBeaconDistanceMin;
   public final BooleanSetting waypointEta;
   public final DoubleSetting waypointEtaMeasurementInterval;
   public final BooleanSetting longWaypointInitials;
   public final BooleanSetting disableWaypointSharing;
   public final BooleanSetting plainWaypointSharing;
   public final BooleanSetting disableReceivingWaypoints;
   public final BooleanSetting limitDeathpointsRenderDistance;
   public final BooleanSetting disableWaypointSetChangeTooltip;
   public final BooleanSetting useCustomCrossDimensionWaypointTeleportFormat;
   public final StringSetting crossDimensionWaypointTeleportFormat;
   public final StringSetting crossDimensionWaypointTeleportRotationFormat;
   public final BooleanSetting switchToNetherSetting;
   public final BooleanSetting switchToOverworldSetting;
   public final BooleanSetting switchToEndSetting;
   public final BooleanSetting switchWaypointsToNetherSetting;
   public final BooleanSetting switchWaypointsToOverworldSetting;
   public final BooleanSetting switchWaypointsToEndSetting;
   public final BooleanSetting worldMapBaritoneGoalHereKeybindSetting;
   public final BooleanSetting worldMapBaritonePathHereKeybindSetting;
   public final BooleanSetting worldMapBaritoneElytraHereKeybindSetting;
   public final BooleanSetting worldMapToggleDrawingKeybindSetting;

   private Settings() {
      this.drawOrderSetting = this.register(StringSetting.create("Draw Order", "xaeroplus.setting.draw_order", "", (Consumer)((s) -> Globals.drawManager.registry().loadOrder(s)), (StringSetting.ScreenSupplier)((parent, escape, setting) -> new DrawOrderScreen(parent, escape))), SettingLocation.WORLD_MAP_MAIN);
      this.transparentWorldmapBackgroundSetting = this.register(BooleanSetting.create("Transparent WorldMap Background", "xaeroplus.setting.transparent_worldmap_background", false), SettingLocation.WORLD_MAP_MAIN);
      this.fastZipWrite = this.register(BooleanSetting.create("Fast Zip Writes", "xaeroplus.setting.fast_zip_writes", true, (BooleanConsumer)((b) -> {
         if (!b) {
            Globals.zipFastByteBuffer = new ByteArrayOutputStream();
         }

      })), SettingLocation.WORLD_MAP_MAIN);
      this.writesWhileDimSwitched = this.register(BooleanSetting.create("Region Writes While Dim Switched", "xaeroplus.setting.region_write_while_dimension_switched", false), SettingLocation.WORLD_MAP_MAIN);
      this.baritoneWaypointSyncSetting = this.register(BooleanSetting.create("Baritone Goal Waypoint", "xaeroplus.setting.baritone_waypoint", true, (b) -> {
         if (BaritoneHelper.isBaritonePresent()) {
            ((BaritoneGoalSync)ModuleManager.getModule(BaritoneGoalSync.class)).setEnabled(b);
         }

      }, BaritoneHelper::isBaritonePresent), SettingLocation.WORLD_MAP_MAIN);
      this.spawnPointSetting = this.register(BooleanSetting.create("Spawn Point Waypoint", "xaeroplus.setting.spawn_point_waypoint", false, (BooleanConsumer)((b) -> ((SpawnPoint)ModuleManager.getModule(SpawnPoint.class)).setEnabled(b))), SettingLocation.WORLD_MAP_MAIN);
      this.pearlWaypointsSetting = this.register(BooleanSetting.create("Pearl Waypoints", "xaeroplus.setting.pearl_waypoints", false, (BooleanConsumer)((b) -> ((Pearls)ModuleManager.getModule(Pearls.class)).setEnabled(b))), SettingLocation.WORLD_MAP_MAIN);
      this.persistMapDimensionSwitchSetting = this.register(BooleanSetting.create("Persist Dim Switch", "xaeroplus.setting.persist_dimension_switch", true), SettingLocation.WORLD_MAP_MAIN);
      this.radarWhileDimensionSwitchedSetting = this.register(BooleanSetting.create("Radar While Dim Switched", "xaeroplus.setting.radar_while_dimension_switched", true), SettingLocation.WORLD_MAP_MAIN);
      this.transparentObsidianRoofSetting = this.register(BooleanSetting.create("Transparent Obsidian Roof", "xaeroplus.setting.transparent_obsidian_roof", false, (BooleanConsumer)((v) -> markChunksDirtyInWriteDistance())), SettingLocation.WORLD_MAP_MAIN);
      DoubleConsumer var10008 = (v) -> markChunksDirtyInWriteDistance();
      BooleanSetting var10009 = this.transparentObsidianRoofSetting;
      Objects.requireNonNull(var10009);
      this.transparentObsidianRoofYSetting = this.register(DoubleSetting.create("Roof Y Level", "xaeroplus.setting.transparent_obsidian_roof_y", (double)0.0F, (double)320.0F, (double)1.0F, (double)250.0F, var10008, var10009::get), SettingLocation.WORLD_MAP_MAIN);
      var10008 = (v) -> markChunksDirtyInWriteDistance();
      var10009 = this.transparentObsidianRoofSetting;
      Objects.requireNonNull(var10009);
      this.transparentObsidianRoofDarkeningSetting = this.register(DoubleSetting.create("Roof Obsidian Opacity", "xaeroplus.setting.transparent_obsidian_roof_darkening", (double)0.0F, (double)255.0F, (double)5.0F, (double)150.0F, var10008, var10009::get), SettingLocation.WORLD_MAP_MAIN);
      var10008 = (v) -> markChunksDirtyInWriteDistance();
      var10009 = this.transparentObsidianRoofSetting;
      Objects.requireNonNull(var10009);
      this.transparentObsidianRoofSnowOpacitySetting = this.register(DoubleSetting.create("Roof Snow Opacity", "xaeroplus.setting.transparent_obsidian_roof_snow_opacity", (double)0.0F, (double)255.0F, (double)5.0F, (double)10.0F, var10008, var10009::get), SettingLocation.WORLD_MAP_MAIN);
      this.crossDimensionCursorCoordinates = this.register(BooleanSetting.create("Cross Dim Cursor Coords", "xaeroplus.setting.cross_dimension_cursor_coordinates", false), SettingLocation.WORLD_MAP_MAIN);
      this.owAutoWaypointDimension = this.register(BooleanSetting.create("Prefer Overworld Waypoints", "xaeroplus.setting.ow_auto_waypoint_dimension", false), SettingLocation.WORLD_MAP_MAIN);
      this.nullOverworldDimensionFolder = this.register(BooleanSetting.create("null OW Dim Dir", "xaeroplus.setting.null_overworld_dimension_folder", true, Globals::setNullOverworldDimFolderIfAble, () -> false), SettingLocation.WORLD_MAP_MAIN);
      this.dataFolderResolutionMode = this.register(EnumSetting.create("Data Dir Mode", "xaeroplus.setting.data_folder_resolution_mode", Settings.DataFolderResolutionMode.values(), Settings.DataFolderResolutionMode.IP, Globals::setDataFolderResolutionModeIfAble), SettingLocation.WORLD_MAP_MAIN);
      this.netherCaveFix = this.register(BooleanSetting.create("Nether Cave Fix", "xaeroplus.setting.nether_cave_fix", true), SettingLocation.WORLD_MAP_MAIN);
      this.disableXaeroInternetAccess = this.register(BooleanSetting.create("Disable Xaero Internet Access", "xaeroplus.setting.disable_internet", false), SettingLocation.WORLD_MAP_MAIN);
      this.expandSettingEntries = this.register(BooleanSetting.create("Expanded Setting Entries", "xaeroplus.setting.expanded_settings", false), SettingLocation.WORLD_MAP_MAIN);
      this.teleportFailNotifier = this.register(BooleanSetting.create("Teleport Fail Notifier", "xaeroplus.setting.teleport_fail_notifier", true, (BooleanConsumer)((b) -> ((TeleportFailNotifier)ModuleManager.getModule(TeleportFailNotifier.class)).setEnabled(b))), SettingLocation.WORLD_MAP_MAIN);
      this.teleportFailNotifierDelay = this.register(DoubleSetting.create("Teleport Fail Delay", "xaeroplus.setting.teleport_fail_notifier_delay", (double)1.0F, (double)120.0F, (double)1.0F, (double)30.0F, (BooleanSupplier)(() -> ((TeleportFailNotifier)ModuleManager.getModule(TeleportFailNotifier.class)).isEnabled())), SettingLocation.WORLD_MAP_MAIN);
      this.disableTeleportation = this.register(BooleanSetting.create("Disable Teleportation", "xaeroplus.setting.disable_teleportation", false), SettingLocation.WORLD_MAP_MAIN);
      this.sodiumSettingIntegration = this.register(BooleanSetting.create("Sodium/Embeddium Setting Integration", "xaeroplus.setting.sodium_embeddium_integration", true), SettingLocation.WORLD_MAP_MAIN);
      this.worldMapUIAdditions = this.register(BooleanSetting.create("WorldMap UI Additions", "xaeroplus.setting.world_map_ui_additions", true, false), SettingLocation.WORLD_MAP_MAIN);
      this.waypointsListUIAdditions = this.register(BooleanSetting.create("Waypoints List UI Additions", "xaeroplus.setting.waypoints_list_ui_additions", true, false), SettingLocation.WORLD_MAP_MAIN);
      this.atomicMoveAndReplace = this.register(BooleanSetting.create("Atomic File Move And Replace", "Atomic File Move And Replace", true, false), SettingLocation.WORLD_MAP_MAIN);
      this.optimizeRegionDetectionLookups = this.register(BooleanSetting.create("Optimize Region Detection Lookups", "Optimize Region Detection Lookups", true, false), SettingLocation.WORLD_MAP_MAIN);
      this.paletteNewChunksEnabledSetting = this.register(BooleanSetting.create("Palette NewChunks", "xaeroplus.setting.palette_new_chunks_highlighting", false, true, (BooleanConsumer)((b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksVersionUpgradedChunks = this.register(BooleanSetting.create("Palette NewChunks Version Upgraded", "xaeroplus.setting.palette_new_chunks_version_upgraded", true, (BooleanSupplier)(() -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled())), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksSaveLoadToDisk = this.register(BooleanSetting.create("Save/Load Palette NewChunks to Disk", "xaeroplus.setting.palette_new_chunks_save_load_to_disk", true, (b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setDiskCache(b), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksAlphaSetting = this.register(DoubleSetting.create("Palette NewChunks Opacity", "xaeroplus.setting.palette_new_chunks_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setAlpha(b), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksColorSetting = this.register(EnumSetting.create("Palette NewChunks Color", "xaeroplus.setting.palette_new_chunks_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setRgbColor(b.getColor()), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksRenderInverse = this.register(BooleanSetting.create("Palette NewChunks Inverse", "xaeroplus.setting.palette_new_chunks_inverse", false, (b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setInverse(b), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksRescan = this.register(BooleanSetting.create("Palette NewChunks Rescan", "xaeroplus.setting.palette_new_chunks_rescan", false, (b) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setRescan(b), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.paletteNewChunksMinRescanAge = this.register(EnumSetting.create("Palette NewChunks Min Rescan Age", "xaeroplus.setting.palette_new_chunks_min_rescan_age", Settings.PaletteNewChunksRescanAge.values(), Settings.PaletteNewChunksRescanAge.ONE_WEEK, (v) -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).setMinRescanAge(v.getDuration()), () -> ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class)).isEnabled() && this.paletteNewChunksRescan.get()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldChunksEnabledSetting = this.register(BooleanSetting.create("OldChunks Highlighting", "xaeroplus.setting.old_chunks_highlighting", false, true, (BooleanConsumer)((b) -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldChunksInverse = this.register(BooleanSetting.create("OldChunks Inverse", "xaeroplus.setting.old_chunks_inverse", false, (b) -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).setInverse(b), () -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldChunksSaveLoadToDisk = this.register(BooleanSetting.create("Save/Load OldChunks to Disk", "xaeroplus.setting.old_chunks_save_load_to_disk", true, (b) -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).setDiskCache(b), () -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldChunksAlphaSetting = this.register(DoubleSetting.create("Old Chunks Opacity", "xaeroplus.setting.old_chunks_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).setAlpha(b), () -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldChunksColorSetting = this.register(EnumSetting.create("Old Chunks Color", "xaeroplus.setting.old_chunks_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.YELLOW, (b) -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).setRgbColor(b.getColor()), () -> ((OldChunks)ModuleManager.getModule(OldChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalsEnabledSetting = this.register(BooleanSetting.create("Portal Highlights", "xaeroplus.setting.portals", false, true, (BooleanConsumer)((b) -> ((Portals)ModuleManager.getModule(Portals.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalsSaveLoadToDisk = this.register(BooleanSetting.create("Save/Load Portals to Disk", "xaeroplus.setting.portals_save_load_to_disk", true, (b) -> ((Portals)ModuleManager.getModule(Portals.class)).setDiskCache(b), () -> ((Portals)ModuleManager.getModule(Portals.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalsAlphaSetting = this.register(DoubleSetting.create("Portal Highlights Opacity", "xaeroplus.setting.portals_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((Portals)ModuleManager.getModule(Portals.class)).setAlpha(b), () -> ((Portals)ModuleManager.getModule(Portals.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalsColorSetting = this.register(EnumSetting.create("Portal Highlights Color", "xaeroplus.setting.portals_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.MAGENTA, (b) -> ((Portals)ModuleManager.getModule(Portals.class)).setRgbColor(b.getColor()), () -> ((Portals)ModuleManager.getModule(Portals.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsEnabledSetting = this.register(BooleanSetting.create("Lava Columns", "xaeroplus.setting.lava_columns", false, true, (BooleanConsumer)((b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsMinHeight = this.register(DoubleSetting.create("Min Lava Column Height", "xaeroplus.setting.lava_columns_min_height", (double)0.0F, (double)20.0F, (double)1.0F, (double)5.0F, (b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setMinColumnHeight((int)b), () -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsAlphaShift = this.register(DoubleSetting.create("Lava Columns Base Alpha Shift", "xaeroplus.setting.lava_columns_alpha_shift", (double)-200.0F, (double)200.0F, (double)1.0F, (double)0.0F, (b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setAlphaShift((int)b), () -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsAlphaStep = this.register(DoubleSetting.create("Lava Columns Alpha Step", "xaeroplus.setting.lava_columns_alpha_step", (double)1.0F, (double)30.0F, (double)1.0F, (double)8.0F, (b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setAlphaStep((int)b), () -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsColor = this.register(EnumSetting.create("Lava Columns Color", "xaeroplus.setting.lava_columns_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.GREEN, (b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setRgbColor(b.getColor()), () -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.lavaColumnsSaveLoadToDisk = this.register(BooleanSetting.create("Save/Load Lava Columns to Disk", "xaeroplus.setting.lava_columns_save_load_to_disk", true, (b) -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).setDiskCache(b), () -> ((LavaColumns)ModuleManager.getModule(LavaColumns.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldBiomesSetting = this.register(BooleanSetting.create("Old Biomes", "xaeroplus.setting.old_biomes_enabled", false, true, (BooleanConsumer)((b) -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldBiomesSaveToDiskSetting = this.register(BooleanSetting.create("Save/Load OldBiomes To Disk", "xaeroplus.setting.old_biomes_save_load_to_disk", true, (b) -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).setDiskCache(b), () -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldBiomesAlphaSetting = this.register(DoubleSetting.create("OldBiomes Opacity", "xaeroplus.setting.old_biomes_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).setAlpha(b), () -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.oldBiomesColorSetting = this.register(EnumSetting.create("OldBiomes Color", "xaeroplus.setting.old_biomes_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.GREEN, (b) -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).setRgbColor(b.getColor()), () -> ((OldBiomes)ModuleManager.getModule(OldBiomes.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksEnabledSetting = this.register(BooleanSetting.create("NewChunks Highlighting", "xaeroplus.setting.new_chunks_highlighting", false, true, (BooleanConsumer)((b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksSaveLoadToDisk = this.register(BooleanSetting.create("Save/Load NewChunks to Disk", "xaeroplus.setting.new_chunks_save_load_to_disk", true, (b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setDiskCache(b), () -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksAlphaSetting = this.register(DoubleSetting.create("New Chunks Opacity", "xaeroplus.setting.new_chunks_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setAlpha(b), () -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksColorSetting = this.register(EnumSetting.create("New Chunks Color", "xaeroplus.setting.new_chunks_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setRgbColor(b.getColor()), () -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksInverseHighlightsSetting = this.register(BooleanSetting.create("New Chunks Render Inverse", "xaeroplus.setting.new_chunks_inverse_enabled", false, (b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setInverseRenderEnabled(b), () -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksInverseColorSetting = this.register(EnumSetting.create("New Chunks Inverse Color", "xaeroplus.setting.new_chunks_inverse_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.GREEN, (b) -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).setInverseRgbColor(b.getColor()), () -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.liquidNewChunksOnlyAboveY0Setting = this.register(BooleanSetting.create("Liquid NewChunks Only Y > 0", "xaeroplus.setting.new_chunks_only_above_y0", false, (BooleanSupplier)(() -> ((LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class)).isEnabled())), SettingLocation.CHUNK_HIGHLIGHTS);
      this.worldToolsEnabledSetting = this.register(BooleanSetting.create("WorldTools Highlights", "xaeroplus.setting.world_tools", true, true, (b) -> ((WorldTools)ModuleManager.getModule(WorldTools.class)).setEnabled(b), WorldToolsHelper::isWorldToolsPresent), SettingLocation.CHUNK_HIGHLIGHTS);
      this.worldToolsAlphaSetting = this.register(DoubleSetting.create("WorldTools Highlights Opacity", "xaeroplus.setting.world_tools_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((WorldTools)ModuleManager.getModule(WorldTools.class)).setAlpha(b), () -> WorldToolsHelper.isWorldToolsPresent() && ((WorldTools)ModuleManager.getModule(WorldTools.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.worldToolsColorSetting = this.register(EnumSetting.create("WorldTools Highlights Color", "xaeroplus.setting.world_tools_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.GREEN, (b) -> ((WorldTools)ModuleManager.getModule(WorldTools.class)).setRgbColor(b.getColor()), () -> WorldToolsHelper.isWorldToolsPresent() && ((WorldTools)ModuleManager.getModule(WorldTools.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipDetectionEnabledSetting = this.register(BooleanSetting.create("PortalSkip Detection", "xaeroplus.setting.portal_skip_detection", false, true, (BooleanConsumer)((b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipDetectionAlphaSetting = this.register(DoubleSetting.create("PortalSkip Opacity", "xaeroplus.setting.portal_skip_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setAlpha(b), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipDetectionColorSetting = this.register(EnumSetting.create("PortalSkip Color", "xaeroplus.setting.portal_skip_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.WHITE, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setRgbColor(b.getColor()), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipPortalRadius = this.register(DoubleSetting.create("PortalSkip Portal Radius", "xaeroplus.setting.portal_skip_portal_radius", (double)0.0F, (double)32.0F, (double)1.0F, (double)15.0F, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setPortalRadius(b), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipDetectionSearchDelayTicksSetting = this.register(DoubleSetting.create("PortalSkip Search Delay", "xaeroplus.setting.portal_skip_search_delay", (double)0.0F, (double)100.0F, (double)1.0F, (double)10.0F, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setSearchDelayTicks(b), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipNewChunksSetting = this.register(BooleanSetting.create("PortalSkip NewChunks", "xaeroplus.setting.portal_skip_new_chunks", true, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setNewChunks(b), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.portalSkipOldChunkInverseSetting = this.register(BooleanSetting.create("PortalSkip OldChunks Inverse", "xaeroplus.setting.portal_skip_old_chunks_inverse", true, (b) -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).setOldChunksInverse(b), () -> ((PortalSkipDetection)ModuleManager.getModule(PortalSkipDetection.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsEnabledSetting = this.register(BooleanSetting.create("Breadcrumbs", "xaeroplus.setting.breadcrumbs", false, (BooleanConsumer)((b) -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).setEnabled(b))), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsSaveLoadToDiskSetting = this.register(BooleanSetting.create("Save/Load Breadcrumbs to Disk", "xaeroplus.setting.breadcrumbs_save_load_to_disk", true, (b) -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).setDiskCache(b), () -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsModeSetting = this.register(EnumSetting.create("Breadcrumbs Mode", "xaeroplus.setting.breadcrumbs_mode", Settings.BreadcrumbsMode.values(), Settings.BreadcrumbsMode.CHUNK_RADIUS), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsChunkRadiusSetting = this.register(DoubleSetting.create("Breadcrumbs Chunk Radius", "xaeroplus.setting.breadcrumbs_chunk_radius", (double)0.0F, (double)16.0F, (double)1.0F, (double)0.0F, (d) -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).setChunkRadius(d), () -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).isEnabled() && this.breadcrumbsModeSetting.get() == Settings.BreadcrumbsMode.CHUNK_RADIUS), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsColorSetting = this.register(EnumSetting.create("Breadcrumbs Color", "xaeroplus.setting.breadcrumbs_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.CYAN, (b) -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).setRgbColor(b.getColor()), () -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.breadcrumbsOpacitySetting = this.register(DoubleSetting.create("Breadcrumbs Opacity", "xaeroplus.setting.breadcrumbs_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).setAlpha(b), () -> ((Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class)).isEnabled()), SettingLocation.CHUNK_HIGHLIGHTS);
      this.baritonePathSyncSetting = this.register(BooleanSetting.create("Baritone Path", "xaeroplus.setting.baritone_path", true, (b) -> {
         if (BaritoneHelper.isBaritonePresent()) {
            ((BaritonePathSync)ModuleManager.getModule(BaritonePathSync.class)).setEnabled(b);
         }

      }, BaritoneHelper::isBaritonePresent), SettingLocation.OVERLAYS);
      this.baritonePathSyncColorSetting = this.register(EnumSetting.create("Baritone Path Color", "xaeroplus.setting.baritone_path_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> ((BaritonePathSync)ModuleManager.getModule(BaritonePathSync.class)).setColor(b.getColor()), () -> ((BaritonePathSync)ModuleManager.getModule(BaritonePathSync.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.baritonePathSyncOpacity = this.register(DoubleSetting.create("Baritone Path Opacity", "xaeroplus.setting.baritone_path_opacity", (double)0.0F, (double)255.0F, (double)5.0F, (double)150.0F, (v) -> ((BaritonePathSync)ModuleManager.getModule(BaritonePathSync.class)).setOpacity((int)v), () -> ((BaritonePathSync)ModuleManager.getModule(BaritonePathSync.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.highwayHighlightsSetting = this.register(BooleanSetting.create("2b2t Highways", "xaeroplus.setting.2b2t_highways_enabled", false, true, (BooleanConsumer)((b) -> ((Highways)ModuleManager.getModule(Highways.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.highwayWidthSetting = this.register(EnumSetting.create("2b2t Highways Width", "xaeroplus.setting.2b2t_highways_width", Settings.HighwayWidth.values(), Settings.HighwayWidth.ONE, (v) -> ((Highways)ModuleManager.getModule(Highways.class)).setWidth(v), () -> ((Highways)ModuleManager.getModule(Highways.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.highwaysColorSetting = this.register(EnumSetting.create("2b2t Highways Color", "xaeroplus.setting.2b2t_highways_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.BLUE, (b) -> ((Highways)ModuleManager.getModule(Highways.class)).setRgbColor(b.getColor()), () -> ((Highways)ModuleManager.getModule(Highways.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.highwaysColorAlphaSetting = this.register(DoubleSetting.create("2b2t Highways Opacity", "xaeroplus.setting.2b2t_highways_opacity", (double)0.0F, (double)255.0F, (double)10.0F, (double)100.0F, (b) -> ((Highways)ModuleManager.getModule(Highways.class)).setAlpha(b), () -> ((Highways)ModuleManager.getModule(Highways.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.showRenderDistanceSetting = this.register(BooleanSetting.create("Show Render Distance", "xaeroplus.setting.show_render_distance", false, true, (BooleanConsumer)((b) -> ((RenderDistance)ModuleManager.getModule(RenderDistance.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.showWorldBorderSetting = this.register(BooleanSetting.create("Show World Border", "xaeroplus.setting.show_world_border", false, (BooleanConsumer)((b) -> ((WorldBorder)ModuleManager.getModule(WorldBorder.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.spawnChunksEnabledSetting = this.register(BooleanSetting.create("Spawn Chunks", "xaeroplus.setting.spawn_chunks", false, true, (BooleanConsumer)((b) -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.playerSpawnChunksEnabledSetting = this.register(BooleanSetting.create("Player Spawn Chunks", "xaeroplus.setting.player_spawn_chunks", false, true, (BooleanConsumer)((b) -> ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.spawnChunksRedstoneProcessingEnabled = this.register(BooleanSetting.create("Spawn Chunks Redstone Processing", "xaeroplus.setting.spawn_chunks_redstone_processing", false, (BooleanSupplier)(() -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled())), SettingLocation.OVERLAYS);
      this.spawnChunksOuterChunksEnabled = this.register(BooleanSetting.create("Spawn Chunks Outer Chunks", "xaeroplus.setting.spawn_chunks_outer_chunks", false, (BooleanSupplier)(() -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled())), SettingLocation.OVERLAYS);
      this.spawnChunksEntityProcessingColor = this.register(EnumSetting.create("Spawn Chunks Entity Processing Color", "xaeroplus.setting.spawn_chunks_entity_processing_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.GREEN, (b) -> {
         ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).setEntityProcessingColor(b.getColor());
         ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).setEntityProcessingColor(b.getColor());
      }, () -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.spawnChunksRedstoneProcessingColor = this.register(EnumSetting.create("Spawn Chunks Redstone Processing Color", "xaeroplus.setting.spawn_chunks_redstone_processing_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> {
         ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).setRedstoneProcessingColor(b.getColor());
         ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).setRedstoneProcessingColor(b.getColor());
      }, () -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.spawnChunksLazyChunksColor = this.register(EnumSetting.create("Spawn Chunks Lazy Chunks Color", "xaeroplus.setting.spawn_chunks_lazy_chunks_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.BLUE, (b) -> {
         ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).setLazyChunksColor(b.getColor());
         ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).setLazyChunksColor(b.getColor());
      }, () -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.spawnChunksOuterChunksColor = this.register(EnumSetting.create("Spawn Chunks Outer Chunks Color", "xaeroplus.setting.spawn_chunks_outer_chunks_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.YELLOW, (b) -> {
         ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).setOuterChunksColor(b.getColor());
         ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).setOuterChunksColor(b.getColor());
      }, () -> ((SpawnChunks)ModuleManager.getModule(SpawnChunks.class)).isEnabled() || ((SpawnChunksPlayer)ModuleManager.getModule(SpawnChunksPlayer.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.mapArtGridEnabledSetting = this.register(BooleanSetting.create("Map Art Grid", "xaeroplus.setting.map_art_grid", false, true, (BooleanConsumer)((b) -> ((MapArtGrid)ModuleManager.getModule(MapArtGrid.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.mapArtGridColorSetting = this.register(EnumSetting.create("Map Art Grid Color", "xaeroplus.setting.map_art_grid_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> ((MapArtGrid)ModuleManager.getModule(MapArtGrid.class)).setRgbColor(b.getColor()), () -> ((MapArtGrid)ModuleManager.getModule(MapArtGrid.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.regionGridEnabledSetting = this.register(BooleanSetting.create("Region Grid", "xaeroplus.setting.region_grid", false, true, (BooleanConsumer)((b) -> ((RegionGrid)ModuleManager.getModule(RegionGrid.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.regionGridColorSetting = this.register(EnumSetting.create("Region Grid Color", "xaeroplus.setting.region_grid_color", ColorHelper.HighlightColor.values(), ColorHelper.HighlightColor.RED, (b) -> ((RegionGrid)ModuleManager.getModule(RegionGrid.class)).setRgbColor(b.getColor()), () -> ((RegionGrid)ModuleManager.getModule(RegionGrid.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.regionGridTextSetting = this.register(BooleanSetting.create("Region Grid Text", "xaeroplus.setting.region_grid_text", false, false, (b) -> ((RegionGrid)ModuleManager.getModule(RegionGrid.class)).setTextEnabled(b), () -> ((RegionGrid)ModuleManager.getModule(RegionGrid.class)).isEnabled()), SettingLocation.OVERLAYS);
      this.beaconsOverlaySetting = this.register(BooleanSetting.create("Beacons Overlay", "xaeroplus.setting.beacons_overlay", false, true, (BooleanConsumer)((b) -> ((Beacons)ModuleManager.getModule(Beacons.class)).setEnabled(b))), SettingLocation.OVERLAYS);
      this.minimapFpsLimiter = this.register(BooleanSetting.create("Minimap FPS Limiter", "xaeroplus.setting.fps_limiter", false, (BooleanConsumer)((b) -> ((FpsLimiter)ModuleManager.getModule(FpsLimiter.class)).setEnabled(b))), SettingLocation.MINIMAP_MAIN);
      this.minimapFpsLimit = this.register(DoubleSetting.create("Minimap FPS Limit", "xaeroplus.setting.fps_limiter_limit", (double)5.0F, (double)120.0F, (double)5.0F, (double)60.0F), SettingLocation.MINIMAP_MAIN);
      this.minimapScaleMultiplierSetting = this.register(DoubleSetting.create("Minimap Scaling Factor", "xaeroplus.setting.minimap_scaling", (double)1.0F, (double)5.0F, (double)1.0F, (double)1.0F, (DoubleConsumer)((b) -> Globals.shouldResetFBO = true)), SettingLocation.MINIMAP_VIEW);
      this.minimapSizeMultiplierSetting = this.register(DoubleSetting.create("Minimap Size Multiplier", "xaeroplus.setting.minimap_size_multiplier", (double)1.0F, (double)4.0F, (double)1.0F, (double)1.0F, (DoubleConsumer)((b) -> Globals.shouldResetFBO = true)), SettingLocation.MINIMAP_VIEW);
      this.minimapRenderZOffsetSetting = this.register(DoubleSetting.create("Minimap Render Z", "xaeroplus.setting.minimap_render_z_offset", (double)-1000.0F, (double)1000.0F, (double)50.0F, (double)0.0F, (BooleanSupplier)(() -> false)), SettingLocation.MINIMAP_VIEW);
      this.alwaysRenderPlayerWithNameOnRadar = this.register(BooleanSetting.create("Always Render Player Name", "xaeroplus.setting.always_render_player_name", true), SettingLocation.MINIMAP_ENTITY_RADAR);
      this.alwaysRenderPlayerIconOnRadar = this.register(BooleanSetting.create("Always Render Player Icon", "xaeroplus.setting.always_render_player_icon", true), SettingLocation.MINIMAP_ENTITY_RADAR);
      this.waypointBeacons = this.register(BooleanSetting.create("Waypoint Beacons", "xaeroplus.setting.waypoint_beacons", false), SettingLocation.MINIMAP_WAYPOINTS);
      BooleanSetting var5 = this.waypointBeacons;
      Objects.requireNonNull(var5);
      this.waypointBeaconScaleMin = this.register(DoubleSetting.create("Waypoint Beacon Scale Min", "xaeroplus.setting.waypoint_beacon_scale_min", (double)0.0F, (double)30.0F, (double)1.0F, (double)0.0F, var5::get), SettingLocation.MINIMAP_WAYPOINTS);
      var5 = this.waypointBeacons;
      Objects.requireNonNull(var5);
      this.waypointBeaconDistanceMin = this.register(DoubleSetting.create("Waypoint Beacon Distance Min", "xaeroplus.setting.waypoint_beacon_distance_min", (double)0.0F, (double)512.0F, (double)8.0F, (double)0.0F, var5::get), SettingLocation.MINIMAP_WAYPOINTS);
      this.waypointEta = this.register(BooleanSetting.create("Waypoint ETA", "xaeroplus.setting.waypoint_eta", false), SettingLocation.MINIMAP_WAYPOINTS);
      DoubleConsumer var7 = (v) -> WaypointEtaManager.INSTANCE.updateMeasurementInterval((int)v);
      var10009 = this.waypointEta;
      Objects.requireNonNull(var10009);
      this.waypointEtaMeasurementInterval = this.register(DoubleSetting.create("Waypoint ETA Measurement Interval", "xaeroplus.setting.waypoint_eta_measurement_interval", (double)0.0F, (double)120.0F, (double)2.0F, (double)10.0F, var7, var10009::get), SettingLocation.MINIMAP_WAYPOINTS);
      this.longWaypointInitials = this.register(BooleanSetting.create("Long Waypoint Initials", "xaeroplus.setting.allow_longer_waypoint_initials", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.disableWaypointSharing = this.register(BooleanSetting.create("Disable Waypoint Sharing", "xaeroplus.setting.disable_waypoint_sharing", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.plainWaypointSharing = this.register(BooleanSetting.create("Plain Waypoint Sharing", "xaeroplus.setting.plain_waypoint_sharing", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.disableReceivingWaypoints = this.register(BooleanSetting.create("Disable Receiving Waypoints", "xaeroplus.setting.disable_receiving_waypoints", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.limitDeathpointsRenderDistance = this.register(BooleanSetting.create("Deathpoints Render Distance", "xaeroplus.setting.deathpoints_render_distance", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.disableWaypointSetChangeTooltip = this.register(BooleanSetting.create("Disable Waypoint Set Change Tooltip", "xaeroplus.setting.waypoint_set_change_tooltip", false), SettingLocation.MINIMAP_WAYPOINTS);
      this.useCustomCrossDimensionWaypointTeleportFormat = this.register(BooleanSetting.create("Use Custom Cross-Dim Waypoint Teleport Format", "xaeroplus.setting.use_custom_cross_dimension_waypoint_teleport_format", false), SettingLocation.MINIMAP_WAYPOINTS);
      StringSetting.ScreenSupplier var10005 = GuiMinimapWaypointTeleportCommandSettings::new;
      BooleanSetting var10006 = this.useCustomCrossDimensionWaypointTeleportFormat;
      Objects.requireNonNull(var10006);
      this.crossDimensionWaypointTeleportFormat = this.register(StringSetting.create("Cross-Dim Waypoint Teleport Format", "xaeroplus.setting.cross_dimension_waypoint_teleport_format", "/execute as @s in {d} run tp {x} {y} {z}", var10005, var10006::get), SettingLocation.MINIMAP_WAYPOINTS);
      var10005 = GuiMinimapWaypointTeleportCommandSettings::new;
      var10006 = this.useCustomCrossDimensionWaypointTeleportFormat;
      Objects.requireNonNull(var10006);
      this.crossDimensionWaypointTeleportRotationFormat = this.register(StringSetting.create("Cross-Dim Waypoint Teleport Rotation Format", "xaeroplus.setting.cross_dimension_waypoint_teleport_rotation_format", "/execute as @s in {d} run tp {x} {y} {z} {yaw} ~", var10005, var10006::get), SettingLocation.MINIMAP_WAYPOINTS);
      this.switchToNetherSetting = this.register(BooleanSetting.create("Switch to Nether", "xaeroplus.keybind.switch_to_nether", false, true, (BooleanConsumer)((b) -> Globals.switchToDimension(class_1937.field_25180))), SettingLocation.KEYBINDS);
      this.switchToOverworldSetting = this.register(BooleanSetting.create("Switch to Overworld", "xaeroplus.keybind.switch_to_overworld", false, true, (BooleanConsumer)((b) -> Globals.switchToDimension(class_1937.field_25179))), SettingLocation.KEYBINDS);
      this.switchToEndSetting = this.register(BooleanSetting.create("Switch to End", "xaeroplus.keybind.switch_to_end", false, true, (BooleanConsumer)((b) -> Globals.switchToDimension(class_1937.field_25181))), SettingLocation.KEYBINDS);
      this.switchWaypointsToNetherSetting = this.register(BooleanSetting.create("Switch Waypoints to Nether", "xaeroplus.keybind.switch_waypoints_to_nether", false, true, (BooleanConsumer)((b) -> WaypointAPI.switchWaypointDimension(class_1937.field_25180))), SettingLocation.KEYBINDS);
      this.switchWaypointsToOverworldSetting = this.register(BooleanSetting.create("Switch Waypoints to Overworld", "xaeroplus.keybind.switch_waypoints_to_overworld", false, true, (BooleanConsumer)((b) -> WaypointAPI.switchWaypointDimension(class_1937.field_25179))), SettingLocation.KEYBINDS);
      this.switchWaypointsToEndSetting = this.register(BooleanSetting.create("Switch Waypoints to End", "xaeroplus.keybind.switch_waypoints_to_end", false, true, (BooleanConsumer)((b) -> WaypointAPI.switchWaypointDimension(class_1937.field_25181))), SettingLocation.KEYBINDS);
      this.worldMapBaritoneGoalHereKeybindSetting = this.register(BooleanSetting.create("WorldMap Baritone Goal Here", "xaeroplus.keybind.world_map_baritone_goal_here", false, true), SettingLocation.KEYBINDS);
      this.worldMapBaritonePathHereKeybindSetting = this.register(BooleanSetting.create("WorldMap Baritone Path Here", "xaeroplus.keybind.world_map_baritone_path_here", false, true), SettingLocation.KEYBINDS);
      this.worldMapBaritoneElytraHereKeybindSetting = this.register(BooleanSetting.create("WorldMap Baritone Elytra Here", "xaeroplus.keybind.world_map_baritone_elytra_here", false, true), SettingLocation.KEYBINDS);
      this.worldMapToggleDrawingKeybindSetting = this.register(BooleanSetting.create("WorldMap Toggle Drawing", "xaeroplus.gui.world_map.start_drawing", false, true), SettingLocation.KEYBINDS);
   }

   static void markChunksDirtyInWriteDistance() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.method_1560() != null) {
         class_1297 var2 = mc.method_1560();
         if (var2 instanceof class_1657) {
            class_1657 player = (class_1657)var2;
            WorldMapSession session = WorldMapSession.getCurrentSession();
            if (session != null) {
               session.getMapProcessor().getMapWriter().setDirtyInWriteDistance(player, mc.field_1687);
               session.getMapProcessor().getMapWriter().requestCachedColoursClear();
            }
         }
      }

   }

   public static enum DataFolderResolutionMode implements TranslatableSettingEnum {
      IP("xaeroplus.setting.data_folder_resolution_mode.ip"),
      SERVER_NAME("xaeroplus.setting.data_folder_resolution_mode.server_name"),
      BASE_DOMAIN("xaeroplus.setting.data_folder_resolution_mode.base_domain");

      private final String translationKey;

      private DataFolderResolutionMode(final String translationKey) {
         this.translationKey = translationKey;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }

      // $FF: synthetic method
      private static DataFolderResolutionMode[] $values() {
         return new DataFolderResolutionMode[]{IP, SERVER_NAME, BASE_DOMAIN};
      }
   }

   public static enum PaletteNewChunksRescanAge implements TranslatableSettingEnum {
      ZERO(Duration.ZERO, "xaeroplus.setting.palette_new_chunks_rescan_age.zero"),
      ONE_HOUR(Duration.ofHours(1L), "xaeroplus.setting.palette_new_chunks_rescan_age.one_hour"),
      ONE_DAY(Duration.ofDays(1L), "xaeroplus.setting.palette_new_chunks_rescan_age.one_day"),
      ONE_WEEK(Duration.ofDays(7L), "xaeroplus.setting.palette_new_chunks_rescan_age.one_week");

      private final Duration duration;
      private final String translationKey;

      private PaletteNewChunksRescanAge(Duration duration, String translationKey) {
         this.duration = duration;
         this.translationKey = translationKey;
      }

      public Duration getDuration() {
         return this.duration;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }

      // $FF: synthetic method
      private static PaletteNewChunksRescanAge[] $values() {
         return new PaletteNewChunksRescanAge[]{ZERO, ONE_HOUR, ONE_DAY, ONE_WEEK};
      }
   }

   public static enum BreadcrumbsMode implements TranslatableSettingEnum {
      CHUNK_RADIUS("xaeroplus.setting.breadcrumbs_mode.chunk_radius"),
      SEEN_CHUNKS("xaeroplus.setting.breadcrumbs_mode.seen_chunks");

      private final String translationKey;

      private BreadcrumbsMode(final String translationKey) {
         this.translationKey = translationKey;
      }

      public String getTranslationKey() {
         return this.translationKey;
      }

      // $FF: synthetic method
      private static BreadcrumbsMode[] $values() {
         return new BreadcrumbsMode[]{CHUNK_RADIUS, SEEN_CHUNKS};
      }
   }

   public static enum HighwayWidth implements TranslatableSettingEnum {
      ONE(1),
      THREE(3),
      FIVE(5);

      private final int width;

      private HighwayWidth(final int width) {
         this.width = width;
      }

      public String getTranslationKey() {
         return String.valueOf(this.width);
      }

      public int getWidth() {
         return this.width;
      }

      // $FF: synthetic method
      private static HighwayWidth[] $values() {
         return new HighwayWidth[]{ONE, THREE, FIVE};
      }
   }
}
