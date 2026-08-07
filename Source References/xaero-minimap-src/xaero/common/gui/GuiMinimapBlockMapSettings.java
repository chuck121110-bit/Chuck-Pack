package xaero.common.gui;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.lib.client.gui.CustomSettingEntry;
import xaero.lib.client.gui.GuiConstants;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.common.config.util.ConfigConstants;
import xaero.lib.common.gui.widget.TooltipInfo;

public class GuiMinimapBlockMapSettings extends GuiMinimapSettings {
   public GuiMinimapBlockMapSettings(IXaeroMinimap modMain, class_437 backScreen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_minimap_block_map_settings"), backScreen, escScreen, context);
      ISettingEntry ignoreHeightmapsEntry = new CustomSettingEntry(() -> false, class_2561.method_43471("gui.xaero_ignore_heightmaps"), context.isClientSide() ? new TooltipInfo("gui.xaero_box_ignore_heightmaps") : new TooltipInfo(GuiConstants.SETTING_ENTRY_WRONG_CONTEXT_COMPONENT, false, true), false, () -> {
         MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (session == null) {
            return null;
         } else {
            MinimapWorldRootContainer currentRootContainer = session.getWorldManager().getAutoRootContainer();
            return currentRootContainer.getConfig().isIgnoreHeightmaps();
         }
      }, 0, 1, (i) -> i == 1, (v) -> {
         if (modMain.getSupportMods().shouldUseWorldMapChunks()) {
            return class_2561.method_43471("gui.xaero_world_map").method_27692(class_124.field_1054);
         } else {
            return v ? ConfigConstants.ON : ConfigConstants.OFF;
         }
      }, (oldValue, newValue) -> {
         if (modMain.getSupportMods().shouldUseWorldMapChunks()) {
            modMain.getSupportMods().worldmapSupport.openSettings();
         } else {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
               MinimapWorldRootContainer currentRootContainer = session.getWorldManager().getAutoRootContainer();
               currentRootContainer.getConfig().setIgnoreHeightmaps(newValue);
               currentRootContainer.getSession().getWorldManagerIO().getRootConfigIO().save(currentRootContainer);
            }
         }
      }, () -> context.isClientSide() && BuiltInHudModules.MINIMAP.getCurrentSession() != null);
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.BLOCK_COLORS), this.optionEntry(MinimapProfiledConfigOptions.BIOMES_IN_VANILLA_COLORS), this.optionEntry(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED), this.optionEntry(MinimapProfiledConfigOptions.BIOME_BLENDING), this.optionEntry(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS), this.optionEntry(MinimapProfiledConfigOptions.AUTO_CAVE_MODE), this.optionEntry(MinimapProfiledConfigOptions.CAVE_MODE_DEPTH), this.optionEntry(MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START), this.optionEntry(MinimapProfiledConfigOptions.LEGIBLE_CAVE_MAPS), this.optionEntry(MinimapProfiledConfigOptions.CAVE_MODE_TOGGLE_TIMER), this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_WORLD_MAP_CHUNKS), this.optionEntry(MinimapProfiledConfigOptions.TERRAIN_DEPTH), this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_REDSTONE), this.optionEntry(MinimapProfiledConfigOptions.TERRAIN_SLOPES), this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_FLOWERS), this.optionEntry(MinimapProfiledConfigOptions.BLOCK_TRANSPARENCY), this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_STAINED_GLASS), this.optionEntry(MinimapProfiledConfigOptions.ADJUST_HEIGHT_FOR_SHORT_BLOCKS), ignoreHeightmapsEntry, this.optionEntry(MinimapProfiledConfigOptions.ANTI_ALIASING)};
   }
}
