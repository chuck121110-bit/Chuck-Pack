package xaeroplus.feature.extensions;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import xaero.lib.client.graphics.util.TextureUtils;
import xaero.lib.client.render.util.GuiRenderUtil;
import xaeroplus.settings.SettingHooks;
import xaeroplus.settings.Settings;
import xaeroplus.settings.StringSetting;
import xaeroplus.util.ColorHelper;

public class GuiMinimapWaypointTeleportCommandSettings extends class_437 {
   private final class_437 parent;
   private final StringSetting setting;
   private class_342 commandInput;
   private class_4185 saveButton;

   public GuiMinimapWaypointTeleportCommandSettings(final class_437 parent, final class_437 escapeScreen, final StringSetting setting) {
      super(class_2561.method_43471(setting.getSettingNameTranslationKey()));
      this.parent = parent;
      this.setting = setting;
   }

   protected void method_25426() {
      this.commandInput = new class_342(this.field_22793, this.field_22789 / 2 - 150, this.field_22790 / 2 - 10, 300, 20, class_2561.method_43471("xaeroplus.gui.cross_dimension_waypoint_teleport_format.input"));
      this.commandInput.method_1880(512);
      this.commandInput.method_1868(ColorHelper.getColor(255, 255, 255, 255));
      this.commandInput.method_1852(this.setting.get());
      this.commandInput.method_1863((value) -> this.saveButton.field_22763 = !value.isBlank());
      this.method_37063(this.commandInput);
      this.saveButton = (class_4185)this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), (button) -> this.save()).method_46434(this.field_22789 / 2 - 104, this.field_22790 / 2 + 26, 100, 20).method_46431());
      this.saveButton.field_22763 = !this.commandInput.method_1882().isBlank();
      this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.cancel"), (button) -> this.method_25419()).method_46434(this.field_22789 / 2 + 4, this.field_22790 / 2 + 26, 100, 20).method_46431());
      this.method_48265(this.commandInput);
   }

   public void method_25420(final class_332 guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
      TextureUtils.clearRenderTargetDepth(this.field_22787.method_1522(), 1.0F);
      super.method_25420(guiGraphics, mouseX, mouseY, partialTick);
      GuiRenderUtil.flushGUI();
      super.method_25420(guiGraphics, mouseX, mouseY, partialTick);
   }

   public void method_25394(final class_332 guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
      super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, this.field_22790 / 2 - 42, ColorHelper.getColor(255, 255, 255, 255));
      String formatKey = this.setting == Settings.REGISTRY.crossDimensionWaypointTeleportRotationFormat ? "xaeroplus.gui.cross_dimension_waypoint_teleport_rotation_format.description" : "xaeroplus.gui.cross_dimension_waypoint_teleport_format.description";
      guiGraphics.method_27534(this.field_22793, class_2561.method_43471(formatKey), this.field_22789 / 2, this.field_22790 / 2 - 28, ColorHelper.getColor(160, 160, 160, 255));
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   private void save() {
      String command = this.commandInput.method_1882();
      if (!command.isBlank()) {
         this.setting.setValue(command);
         SettingHooks.saveSettings();
         this.method_25419();
      }
   }
}
