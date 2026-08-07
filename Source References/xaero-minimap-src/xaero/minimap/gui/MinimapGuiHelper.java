package xaero.minimap.gui;

import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.common.gui.GuiHelper;
import xaero.common.gui.GuiMinimapMain;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;

public class MinimapGuiHelper extends GuiHelper {
   public MinimapGuiHelper(IXaeroMinimap modMain) {
      super(modMain);
   }

   public GuiSettings getMainSettingsScreen(class_437 parent) {
      return new GuiMinimapMain(this.modMain, parent, (class_437)null, true, BuiltInEditConfigScreenContexts.CLIENT);
   }

   public void openMainSettingsFromScreen(class_437 screen) {
      class_310.method_1551().method_1507(this.getMinimapSettingsFromScreen(screen));
   }
}
