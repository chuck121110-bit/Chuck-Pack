package xaero.map.config.option.value.redirect;

import net.minecraft.class_437;
import xaero.lib.client.config.option.value.redirect.ClientOptionValueRedirectorManager;
import xaero.lib.common.config.util.ConfigConstants;
import xaero.map.common.config.WorldMapConfigConstants;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.effects.Effects;
import xaero.map.misc.Misc;
import xaero.map.mods.SupportMods;

public class WorldMapConfigOptionClientRedirectors {
   public static void registerAll(ClientOptionValueRedirectorManager manager) {
      manager.register(WorldMapProfiledConfigOptions.CAVE_MODE_ALLOWED, () -> false, (channel) -> Misc.hasEffect(Effects.NO_CAVE_MAPS) || Misc.hasEffect(Effects.NO_CAVE_MAPS_HARMFUL) || WorldMapClientConfigUtils.isFairPlay() || WorldMapClientConfigUtils.isCaveModeDisabledLegacy(), (current) -> null, ConfigConstants.OFF, () -> !Misc.hasEffect(Effects.NO_CAVE_MAPS) && !Misc.hasEffect(Effects.NO_CAVE_MAPS_HARMFUL) ? (WorldMapClientConfigUtils.isFairPlay() ? WorldMapConfigConstants.FAIRPLAY_TOOLTIP : WorldMapConfigConstants.LEGACY_PLUGIN_TOOLTIP) : WorldMapConfigConstants.EFFECT_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.WAYPOINTS, () -> false, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.WAYPOINT_BACKGROUNDS, () -> false, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.WAYPOINT_SCALE, () -> (double)1.0F, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.MIN_ZOOM_LOCAL_WAYPOINTS, () -> (double)0.0F, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.MINIMAP_RADAR, () -> false, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
      manager.register(WorldMapProfiledConfigOptions.RENDER_WAYPOINTS, () -> false, (channel) -> !SupportMods.minimap(), (current) -> null, ConfigConstants.OFF, () -> WorldMapConfigConstants.MINIMAP_TOOLTIP);
   }
}
