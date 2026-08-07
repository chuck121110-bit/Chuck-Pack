package xaeroplus.feature.extensions;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.ScreenSwitchSettingEntry;
import xaeroplus.settings.SettingLocation;
import xaeroplus.settings.Settings;

public class GuiXaeroPlusChunkHighlightSettings extends GuiSettings {
   public GuiXaeroPlusChunkHighlightSettings(class_437 parent, class_437 escapeScreen) {
      super(class_2561.method_43471("xaeroplus.gui.chunk_highlight_settings"), parent, escapeScreen);
      this.entries = Settings.REGISTRY.getXaeroSettingEntries(SettingLocation.CHUNK_HIGHLIGHTS);
      this.canSkipWorldRender = true;
   }

   public void method_25420(class_332 guiGraphics, int i, int j, float f) {
      if (this.escape instanceof ScreenBase) {
         this.renderEscapeScreen(guiGraphics, 0, 0, f);
      }

      super.method_25420(guiGraphics, i, j, f);
   }

   public static ScreenSwitchSettingEntry getScreenSwitchSettingEntry(class_437 parent) {
      return new ScreenSwitchSettingEntry("xaeroplus.gui.chunk_highlight_settings", GuiXaeroPlusChunkHighlightSettings::new, (Tooltip)null, true);
   }
}
