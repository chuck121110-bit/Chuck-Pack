package xaero.common.gui;

import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.lib.client.gui.ScreenBase;

public class GuiInstructions extends ScreenBase {
   public GuiInstructions(IXaeroMinimap modMain, class_437 par1GuiScreen, class_437 escape) {
      super(par1GuiScreen, escape, class_2561.method_43471("gui.xaero_instructions"));
   }

   public void method_25426() {
      super.method_25426();
      this.method_37063(class_4185.method_46430(class_2561.method_43469("gui.xaero_OK", new Object[0]), (b) -> this.goBack()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 6 + 168, 200, 20).method_46431());
   }

   public void method_25420(class_332 guiGraphics, int par1, int par2, float par3) {
      super.method_25420(guiGraphics, par1, par2, par3);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 20, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_select", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 10, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_drag", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 21, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_deselect", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 32, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_center", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 43, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_different_centered", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 54, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_flip", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 65, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_settings", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 76, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_preset", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 87, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_save", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 98, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_howto_cancel", new Object[0]), this.field_22789 / 2, this.field_22790 / 7 + 109, -1);
   }
}
