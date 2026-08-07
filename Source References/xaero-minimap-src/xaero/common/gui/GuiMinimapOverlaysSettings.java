package xaero.common.gui;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.client.gui.widget.Tooltip;

public class GuiMinimapOverlaysSettings extends GuiMinimapSettings {
   public GuiMinimapOverlaysSettings(IXaeroMinimap modMain, class_437 backScreen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_overlay_settings"), backScreen, escScreen, context);
      ScreenSwitchSettingEntry lightOverlayEntry = new ScreenSwitchSettingEntry("gui.xaero_light_overlay", (current, escape) -> new GuiLightOverlay(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry slimeChunksMultiplayerEntry = null;
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null && context.isClientSide() && class_310.method_1551().method_1576() == null) {
         MinimapWorld minimapWorld = minimapSession.getWorldManager().getAutoWorld();
         if (minimapWorld != null) {
            slimeChunksMultiplayerEntry = new ScreenSwitchSettingEntry("gui.xaero_slime_chunks", (current, escape) -> new GuiSlimeSeed(modMain, minimapSession, current, escape, context), (Tooltip)null, true);
         }
      }

      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.CHUNK_GRID), this.optionEntry(MinimapProfiledConfigOptions.CHUNK_GRID_LINE_WIDTH), lightOverlayEntry, (ISettingEntry)(slimeChunksMultiplayerEntry == null ? this.optionEntry(MinimapProfiledConfigOptions.SLIME_CHUNKS) : slimeChunksMultiplayerEntry), this.optionEntry(MinimapProfiledConfigOptions.OPAC_CLAIMS), this.optionEntry(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY), this.optionEntry(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY)};
   }
}
