package xaero.common.gui;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

public class GuiMinimapMiscSettings extends GuiMinimapSettings {
   public GuiMinimapMiscSettings(class_437 backScreen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_minimap_misc_settings"), backScreen, escScreen, context);
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.SAFE_MODE), this.primaryOptionEntry(MinimapPrimaryClientConfigOptions.UPDATE_NOTIFICATIONS), this.optionEntry(MinimapProfiledConfigOptions.UI_SCALE), this.optionEntry(MinimapProfiledConfigOptions.MINIMAP_ITEM)};
   }
}
