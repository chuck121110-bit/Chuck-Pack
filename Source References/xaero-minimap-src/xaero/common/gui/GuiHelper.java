package xaero.common.gui;

import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.EditConfigScreen;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

public abstract class GuiHelper {
   protected IXaeroMinimap modMain;

   public GuiHelper(IXaeroMinimap modMain) {
      this.modMain = modMain;
   }

   public ScreenBase getMinimapSettingsFromScreen(class_437 currentScreen) {
      class_437 escScreen = ScreenBase.tryToGetEscape(currentScreen);
      IEditConfigScreenContext var10000;
      if (currentScreen instanceof EditConfigScreen ecs) {
         var10000 = ecs.getContext();
      } else {
         var10000 = BuiltInEditConfigScreenContexts.CLIENT;
      }

      IEditConfigScreenContext context = var10000;
      return new GuiMinimapMain(this.modMain, currentScreen, escScreen, true, context);
   }

   /** @deprecated */
   @Deprecated
   public void openMinimapSettingsFromScreen(class_437 parent, class_437 escScreen) {
      class_310.method_1551().method_1507(new GuiMinimapMain(this.modMain, parent, escScreen, true, BuiltInEditConfigScreenContexts.CLIENT));
   }

   public abstract GuiSettings getMainSettingsScreen(class_437 var1);

   public abstract void openMainSettingsFromScreen(class_437 var1);
}
