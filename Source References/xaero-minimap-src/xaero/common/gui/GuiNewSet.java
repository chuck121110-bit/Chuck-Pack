package xaero.common.gui;

import java.io.IOException;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.MySmallButton;

public class GuiNewSet extends ScreenBase {
   private class_342 nameTextField;
   private MinimapSession session;
   private MinimapWorldManager manager;
   private MinimapWorld minimapWorld;
   private class_4185 confirmButton;

   public GuiNewSet(IXaeroMinimap modMain, MinimapSession session, class_437 par1GuiScreen, class_437 escapeScreen, MinimapWorld minimapWorld) {
      super(par1GuiScreen, escapeScreen, class_2561.method_43471("gui.xaero_create_set"));
      this.session = session;
      this.manager = this.session.getWorldManager();
      this.minimapWorld = minimapWorld;
      this.canSkipWorldRender = true;
   }

   public void method_25426() {
      super.method_25426();
      this.nameTextField = new class_342(this.field_22793, this.field_22789 / 2 - 100, 60, 200, 20, class_2561.method_43471("gui.xaero_set_name"));
      this.method_25395(this.nameTextField);
      this.nameTextField.method_25365(true);
      this.method_37063(this.nameTextField);
      this.method_37063(this.confirmButton = new MySmallButton(200, this.field_22789 / 2 - 155, this.field_22790 / 6 + 168, class_2561.method_43469("gui.xaero_confirm", new Object[0]), (b) -> {
         if (this.canConfirm()) {
            String setName = this.nameTextField.method_1882().replace(":", "§§");
            this.minimapWorld.setCurrentWaypointSetId(setName);
            this.minimapWorld.addWaypointSet(setName);

            try {
               this.session.getWorldManagerIO().saveWorld(this.minimapWorld);
            } catch (IOException e) {
               MinimapLogs.LOGGER.error("suppressed exception", e);
            }

            this.goBack();
         }

      }));
      this.method_37063(new MySmallButton(201, this.field_22789 / 2 + 5, this.field_22790 / 6 + 168, class_2561.method_43469("gui.xaero_cancel", new Object[0]), (b) -> this.goBack()));
      this.updateConfirmButton();
   }

   protected void method_56131() {
   }

   private boolean canConfirm() {
      return this.nameTextField.method_1882().length() > 0 && this.minimapWorld.getWaypointSet(this.nameTextField.method_1882()) == null;
   }

   private void updateConfirmButton() {
      this.confirmButton.field_22763 = this.canConfirm();
   }

   public boolean method_25404(class_11908 event) {
      boolean result = super.method_25404(event);
      if (event.comp_4795() == 257 && this.canConfirm()) {
         this.confirmButton.method_25348(new class_11909((double)0.0F, (double)0.0F, new class_11910(0, 0)), false);
         return true;
      } else {
         return result;
      }
   }

   public void method_25393() {
      this.updateConfirmButton();
   }

   public void method_25420(class_332 guiGraphics, int par1, int par2, float par3) {
      this.renderEscapeScreen(guiGraphics, 0, 0, par3);
      super.method_25420(guiGraphics, par1, par2, par3);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 20, -1);
      this.nameTextField.method_25394(guiGraphics, par1, par2, par3);
   }
}
