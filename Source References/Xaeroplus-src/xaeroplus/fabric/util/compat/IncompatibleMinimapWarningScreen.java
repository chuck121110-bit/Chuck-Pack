package xaeroplus.fabric.util.compat;

import java.util.Optional;
import net.fabricmc.loader.api.Version;
import net.minecraft.class_11735;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5489;
import xaeroplus.util.ColorHelper;

public class IncompatibleMinimapWarningScreen extends class_437 {
   private final class_2561 titleComponent;
   private final class_2561 messageComponent;
   private class_5489 message;

   private static class_2561 getMessage(final Optional<Version> currentVersion, final Version compatibleMinimapVersion) {
      class_5250 msg = class_2561.method_43473();
      currentVersion.ifPresent((cv) -> msg.method_27692(class_124.field_1070).method_10852(class_2561.method_43471("xaeroplus.gui.minimap_incompatible.currently_installed_version")).method_10852(class_2561.method_43470(cv.getFriendlyString()).method_27692(class_124.field_1061)).method_10852(class_2561.method_43470("\n")));
      msg.method_10852(class_2561.method_43471("xaeroplus.gui.minimap_incompatible.required_version").method_27692(class_124.field_1070).method_10852(class_2561.method_43470(compatibleMinimapVersion.getFriendlyString()).method_27692(class_124.field_1075)));
      return msg;
   }

   public IncompatibleMinimapWarningScreen(Optional<Version> currentVersion, final Version compatibleMinimapVersion) {
      super(class_2561.method_43470("XaeroPlus"));
      this.message = class_5489.field_26528;
      this.titleComponent = class_2561.method_43471("xaeroplus.gui.minimap_incompatible.title").method_27695(new class_124[]{class_124.field_1079, class_124.field_1067});
      this.messageComponent = getMessage(currentVersion, compatibleMinimapVersion);
   }

   public void method_25426() {
      this.message = class_5489.method_30890(this.field_22793, this.messageComponent, this.field_22789 - 100);
      this.method_37063(class_4185.method_46430(class_2561.method_43471("xaeroplus.gui.minimap_incompatible.download_minimap"), (button) -> {
         class_156.method_668().method_670("https://modrinth.com/mod/xaeros-minimap/versions");
         class_310.method_1551().close();
      }).method_46434(this.field_22789 / 2 - 100 - 75, 150, 150, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43471("xaeroplus.gui.minimap_incompatible.exit"), (button) -> class_310.method_1551().close()).method_46434(this.field_22789 / 2 + 100 - 75, 150, 150, 20).method_46431());
   }

   public boolean method_25422() {
      return false;
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.method_27534(this.field_22793, this.titleComponent, this.field_22789 / 2, 50, ColorHelper.getColor(255, 255, 255, 255));
      int i = this.field_22789 / 2 - this.message.method_44048() / 2;
      this.message.method_75816(class_11735.field_62009, i, 75, 18, guiGraphics.method_75788());
   }
}
