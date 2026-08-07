package xaeroplus.feature.extensions;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.ScreenSwitchSettingEntry;
import xaeroplus.settings.SettingLocation;
import xaeroplus.settings.Settings;

public class GuiXaeroPlusWorldMapSettings extends GuiSettings {
   public GuiXaeroPlusWorldMapSettings(class_437 parent, class_437 escapeScreen) {
      super(class_2561.method_43471("xaeroplus.gui.world_map_settings"), parent, escapeScreen);
      IXaeroPlusSettingEntry[] mainSettingsEntries = Settings.REGISTRY.getXaeroSettingEntries(SettingLocation.WORLD_MAP_MAIN);
      ScreenSwitchSettingEntry chunkHighlightSettingSwitchEntry = GuiXaeroPlusChunkHighlightSettings.getScreenSwitchSettingEntry(parent);
      ScreenSwitchSettingEntry overlaySettingSwitchEntry = GuiXaeroPlusOverlaySettings.getScreenSwitchSettingEntry(parent);
      this.entries = new ISettingEntry[mainSettingsEntries.length + 2];
      this.entries[0] = chunkHighlightSettingSwitchEntry;
      this.entries[1] = overlaySettingSwitchEntry;
      System.arraycopy(mainSettingsEntries, 0, this.entries, 2, mainSettingsEntries.length);
      this.canSkipWorldRender = true;
   }

   public void method_25420(class_332 guiGraphics, int i, int j, float f) {
      if (this.escape instanceof ScreenBase) {
         this.renderEscapeScreen(guiGraphics, 0, 0, f);
      }

      super.method_25420(guiGraphics, i, j, f);
   }

   public static ScreenSwitchSettingEntry getScreenSwitchSettingEntry(class_437 parent) {
      return new ScreenSwitchSettingEntry("xaeroplus.gui.world_map_settings", GuiXaeroPlusWorldMapSettings::new, (Tooltip)null, true);
   }
}
