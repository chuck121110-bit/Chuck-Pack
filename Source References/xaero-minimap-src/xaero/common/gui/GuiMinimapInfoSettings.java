package xaero.common.gui;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.client.gui.widget.Tooltip;

public class GuiMinimapInfoSettings extends GuiMinimapSettings {
   public GuiMinimapInfoSettings(IXaeroMinimap modMain, class_437 backScreen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_minimap_info_settings"), backScreen, escScreen, context);
      new Tooltip("gui.xaero_box_minimap_info_display_manager");
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.INFO_DISPLAY_CONFIG), this.optionEntry(MinimapProfiledConfigOptions.INFO_DISPLAY_BG_OPACITY), this.optionEntry(MinimapProfiledConfigOptions.INFO_DISPLAY_ALIGNMENT), this.optionEntry(MinimapProfiledConfigOptions.OPAC_CURRENT_CLAIM)};
   }
}
