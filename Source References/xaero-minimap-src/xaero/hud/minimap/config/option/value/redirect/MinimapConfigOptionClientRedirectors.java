package xaero.hud.minimap.config.option.value.redirect;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.minimap.MinimapProcessor;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.lib.client.config.option.value.redirect.ClientOptionValueRedirectorManager;
import xaero.lib.common.config.channel.ConfigChannel;

public class MinimapConfigOptionClientRedirectors {
   public static final class_2561 LEGACY_PLUGIN_TOOLTIP = class_2561.method_43471("gui.xaero_minimap_redirect_legacy");
   public static final class_2561 FAIRPLAY_TOOLTIP = class_2561.method_43471("gui.xaero_config_redirect_fairplay");
   public static final class_2561 SAFE_MODE_TOOLTIP = class_2561.method_43471("gui.xaero_config_redirect_safe_mode");
   public static final class_2561 EFFECT_TOOLTIP = class_2561.method_43471("gui.xaero_config_redirect_effect");
   public static final class_2561 FROM_WORLDMAP_TOOLTIP;
   public static final class_2561 HARD_WORLDMAP_TOOLTIP;
   public static final class_2561 FROM_WORLDMAP_SCREEN_TOOLTIP;
   public static final class_2561 WORLD_MAP_COMPONENT;

   public static void registerAll(ClientOptionValueRedirectorManager manager) {
      manager.register(MinimapProfiledConfigOptions.DISPLAY_MINIMAP, () -> false, (channel) -> HudMod.INSTANCE.getSettings().minimapDisabled() || class_310.method_1551().field_1724 != null && !MinimapProcessor.hasMinimapItem(class_310.method_1551().field_1724), (Function)null, (class_2561)null, () -> {
         if (HudMod.INSTANCE.getSettings().minimapDisabled()) {
            return LEGACY_PLUGIN_TOOLTIP;
         } else if (class_310.method_1551().field_1724 != null && !MinimapProcessor.hasMinimapItem(class_310.method_1551().field_1724)) {
            String minimapItemId = (String)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.MINIMAP_ITEM);
            return class_2561.method_43469("gui.xaero_minimap_redirect_item", new Object[]{minimapItemId});
         } else {
            return null;
         }
      });
      manager.register(MinimapProfiledConfigOptions.DISPLAY_RADAR, () -> false, (channel) -> HudMod.INSTANCE.isFairPlay(), (Function)null, (class_2561)null, () -> FAIRPLAY_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.RADAR_DOTS_STYLE, () -> 1, (channel) -> !HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().assumeUsingFBO(), (Function)null, (class_2561)null, () -> SAFE_MODE_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.RADAR_SMOOTH_DOTS, () -> false, (channel) -> !HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().assumeUsingFBO(), (Function)null, (class_2561)null, () -> SAFE_MODE_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.RADAR_MAIN_DOT_SIZE, () -> 2, (channel) -> !HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().assumeUsingFBO(), (Function)null, (class_2561)null, () -> SAFE_MODE_TOOLTIP);
      Predicate<ConfigChannel> waypointDisableCondition = (channel) -> HudMod.INSTANCE.getSettings().showWaypointsDisabled() || class_310.method_1551().field_1724 != null && !MinimapProcessor.hasMinimapItem(class_310.method_1551().field_1724);
      Supplier<class_2561> waypointDisableTooltipSupplier = () -> {
         if (HudMod.INSTANCE.getSettings().showWaypointsDisabled()) {
            return LEGACY_PLUGIN_TOOLTIP;
         } else if (class_310.method_1551().field_1724 != null && !MinimapProcessor.hasMinimapItem(class_310.method_1551().field_1724)) {
            String minimapItemId = (String)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.MINIMAP_ITEM);
            return class_2561.method_43469("gui.xaero_minimap_redirect_item", new Object[]{minimapItemId});
         } else {
            return null;
         }
      };
      manager.register(MinimapProfiledConfigOptions.WAYPOINTS_ON_MINIMAP, () -> false, waypointDisableCondition, (Function)null, (class_2561)null, waypointDisableTooltipSupplier);
      manager.register(MinimapProfiledConfigOptions.WAYPOINTS_IN_WORLD, () -> false, waypointDisableCondition, (Function)null, (class_2561)null, waypointDisableTooltipSupplier);
      manager.register(MinimapProfiledConfigOptions.DEATHPOINTS, () -> false, (channel) -> HudMod.INSTANCE.getSettings().deathpointsDisabled(), (Function)null, (class_2561)null, () -> LEGACY_PLUGIN_TOOLTIP);
      Function<class_437, class_437> worldMapSettingsScreenFactory = (current) -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getSettingsScreen(current);
      manager.register(MinimapProfiledConfigOptions.WAYPOINT_PARTIAL_Y_TELEPORT, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getPartialYTeleport(), (channel) -> HudMod.INSTANCE.getSupportMods().worldmap(), worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      Predicate<ConfigChannel> shouldUseWorldMapChunksPredicate = (channel) -> HudMod.INSTANCE.getSupportMods().shouldUseWorldMapChunks();
      manager.register(MinimapProfiledConfigOptions.BLOCK_COLORS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapColours(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.BIOMES_IN_VANILLA_COLORS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapBiomeColorsVanillaMode(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED, () -> false, (channel) -> MinimapConfigClientUtils.isFairPlayForCaveMode() || HudMod.INSTANCE.getSettings().caveMapsDisabled() || MinimapConfigClientUtils.hasNoCaveModeEffect(), (Function)null, (class_2561)null, () -> MinimapConfigClientUtils.isFairPlayForCaveMode() ? FAIRPLAY_TOOLTIP : (HudMod.INSTANCE.getSettings().caveMapsDisabled() ? LEGACY_PLUGIN_TOOLTIP : EFFECT_TOOLTIP));
      manager.register(MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getManualCaveStart(), (channel) -> HudMod.INSTANCE.getSupportMods().shouldUseWorldMapCaveChunks(), (currentScreen) -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapScreenForOption(MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START, currentScreen), WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_SCREEN_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.BIOME_BLENDING, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getBiomeBlending(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.CAVE_MODE_DEPTH, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getCaveModeDepth(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.LEGIBLE_CAVE_MAPS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.isLegibleCaveMaps(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.DISPLAY_WORLD_MAP_CHUNKS, () -> false, (channel) -> !HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().assumeUsingFBO(), (Function)null, (class_2561)null, () -> SAFE_MODE_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.TERRAIN_DEPTH, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapTerrainDepth(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.TERRAIN_SLOPES, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapTerrainSlopes(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.DISPLAY_REDSTONE, () -> false, shouldUseWorldMapChunksPredicate, (Function)null, WORLD_MAP_COMPONENT, () -> HARD_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.DISPLAY_FLOWERS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getWorldMapFlowers(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.DISPLAY_STAINED_GLASS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.isStainedGlassDisplayed(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.ADJUST_HEIGHT_FOR_SHORT_BLOCKS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getAdjustHeightForCarpetLikeBlocks(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.ANTI_ALIASING, () -> false, (channel) -> !HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().assumeUsingFBO(), (Function)null, (class_2561)null, () -> SAFE_MODE_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.OPAC_CLAIMS, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getDisplayClaims(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getClaimsFillOpacity(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
      manager.register(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY, () -> HudMod.INSTANCE.getSupportMods().worldmapSupport.getClaimsBorderOpacity(), shouldUseWorldMapChunksPredicate, worldMapSettingsScreenFactory, WORLD_MAP_COMPONENT, () -> FROM_WORLDMAP_TOOLTIP);
   }

   static {
      FROM_WORLDMAP_TOOLTIP = class_2561.method_43471("gui.xaero_uses_worldmap_value").method_27692(class_124.field_1054);
      HARD_WORLDMAP_TOOLTIP = class_2561.method_43471("gui.xaero_uses_worldmap_hard_value").method_27692(class_124.field_1054);
      FROM_WORLDMAP_SCREEN_TOOLTIP = class_2561.method_43471("gui.xaero_uses_worldmap_screen_value").method_27692(class_124.field_1054);
      WORLD_MAP_COMPONENT = class_2561.method_43471("gui.xaero_world_map").method_27692(class_124.field_1054);
   }
}
