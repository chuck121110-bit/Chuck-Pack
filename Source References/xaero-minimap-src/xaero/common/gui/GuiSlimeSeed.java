package xaero.common.gui;

import java.io.IOException;
import net.minecraft.class_1074;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;
import org.apache.commons.lang3.StringUtils;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

public class GuiSlimeSeed extends GuiMinimapSettings {
   public class_342 seedTextField;
   private final MinimapWorld minimapWorld;
   private final IXaeroMinimap modMain;
   private final MinimapSession session;

   public GuiSlimeSeed(IXaeroMinimap modMain, MinimapSession session, class_437 parent, class_437 escape, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_slime_chunks"), parent, escape, context);
      this.modMain = modMain;
      this.session = session;
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.SLIME_CHUNKS), this.optionEntry(MinimapProfiledConfigOptions.OPEN_SLIME_CHUNKS_SCREEN)};
      this.minimapWorld = session.getWorldManager().getAutoWorld();
   }

   public void method_25426() {
      super.method_25426();
      this.seedTextField = new class_342(this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 7 + 68, 200, 20, class_2561.method_43471("gui.xaero_used_seed"));
      class_342 var10000 = this.seedTextField;
      Object var10001 = this.minimapWorld.getSlimeChunkSeed() == null ? "" : this.minimapWorld.getSlimeChunkSeed();
      var10000.method_1852("" + String.valueOf(var10001));
      this.method_37063(this.seedTextField);
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partial) {
      super.method_25394(guiGraphics, mouseX, mouseY, partial);
      this.seedTextField.method_25394(guiGraphics, mouseX, mouseY, partial);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_used_seed", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 55, -1);
   }

   public void method_25393() {
   }

   public boolean method_25404(class_11908 event) {
      boolean result = super.method_25404(event);
      if (event.comp_4795() == 257) {
         this.goBack();
      }

      this.updateSlimeSeed();
      return result;
   }

   public boolean method_25400(class_11905 event) {
      boolean result = super.method_25400(event);
      this.updateSlimeSeed();
      return result;
   }

   private void updateSlimeSeed() {
      String s = this.seedTextField.method_1882();
      if (!StringUtils.isEmpty(s)) {
         try {
            long j = Long.parseLong(s);
            this.minimapWorld.setSlimeChunkSeed(j);
         } catch (NumberFormatException var5) {
            this.minimapWorld.setSlimeChunkSeed((long)s.hashCode());
         }
      }

      try {
         this.session.getWorldManagerIO().saveWorld(this.minimapWorld);
      } catch (IOException e) {
         MinimapLogs.LOGGER.error("suppressed exception", e);
      }

   }
}
