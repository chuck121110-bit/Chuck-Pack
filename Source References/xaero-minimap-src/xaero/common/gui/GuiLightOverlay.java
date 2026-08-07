package xaero.common.gui;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

public class GuiLightOverlay extends GuiMinimapSettings {
   public GuiLightOverlay(IXaeroMinimap modMain, class_437 par1Screen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_light_overlay"), par1Screen, escScreen, context);
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE), this.optionEntry(MinimapProfiledConfigOptions.LIGHT_OVERLAY_MAX_LIGHT), this.optionEntry(MinimapProfiledConfigOptions.LIGHT_OVERLAY_COLOR), this.optionEntry(MinimapProfiledConfigOptions.LIGHT_OVERLAY_MIN_LIGHT)};
   }
}
