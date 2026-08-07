package xaero.common.gui;

import java.util.Optional;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import xaero.hud.preset.HudPreset;
import xaero.hud.preset.HudPresetManager;
import xaero.lib.client.gui.ISettingEntry;

public class HudPresetSettingEntry implements ISettingEntry {
   private final HudPresetManager manager;
   private final HudPreset preset;
   private final String searchString;

   public HudPresetSettingEntry(HudPresetManager manager, HudPreset preset) {
      this.manager = manager;
      this.preset = preset;
      StringBuilder searchStringBuilder = new StringBuilder();
      searchStringBuilder.append(preset.getId()).append(" ");
      preset.getName().method_27657((s) -> {
         searchStringBuilder.append(s);
         return Optional.empty();
      });
      this.searchString = searchStringBuilder.toString();
   }

   public String getStringForSearch() {
      return this.searchString;
   }

   public class_339 createWidget(int x, int y, int w) {
      return class_4185.method_46430(this.preset.getName(), (b) -> {
         for(HudPreset preset : this.manager.getPresets()) {
            preset.cancel();
         }

         this.preset.apply();
         class_437 patt0$temp = class_310.method_1551().field_1755;
         if (patt0$temp instanceof GuiChoosePreset gui) {
            gui.goBack();
         }

      }).method_46434(x, y, w, 20).method_46431();
   }
}
