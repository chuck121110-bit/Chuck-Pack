package xaeroplus.feature.extensions;

import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import xaero.common.gui.TooltipButton;
import xaero.lib.client.gui.widget.Tooltip;
import xaeroplus.util.ColorHelper;

public class MinimapGuiTexturedButton extends TooltipButton {
   public final int textureX;
   public final int textureY;
   public final int textureW;
   public final int textureH;
   protected final int factorW;
   protected final int factorH;
   public final class_2960 texture;

   public MinimapGuiTexturedButton(int x, int y, int w, int h, int textureX, int textureY, int textureW, int textureH, class_2960 texture, class_4185.class_4241 onPress, Supplier<Tooltip> tooltip, int factorW, int factorH) {
      super(x, y, w, h, class_2561.method_43473(), onPress, tooltip);
      this.textureX = textureX;
      this.textureY = textureY;
      this.textureW = textureW;
      this.textureH = textureH;
      this.texture = texture;
      this.factorW = factorW;
      this.factorH = factorH;
   }

   public class_2561 method_25369() {
      return (class_2561)(this.getXaero_tooltip() != null ? class_2561.method_43470(((Tooltip)this.getXaero_tooltip().get()).getPlainText()) : super.method_25369());
   }

   public void method_75752(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      int iconX = this.method_46426() + this.field_22758 / 2 - this.textureW / 2;
      int iconY = this.method_46427() + this.field_22759 / 2 - this.textureH / 2;
      int color = ColorHelper.getColor(64, 64, 64, 255);
      if (this.field_22763) {
         if (this.field_22762) {
            --iconY;
            color = ColorHelper.getColor(230, 230, 230, 255);
         } else {
            color = ColorHelper.getColor(252, 252, 252, 255);
         }
      }

      guiGraphics.method_25291(class_10799.field_56883, this.texture, iconX, iconY, (float)this.textureX, (float)this.textureY, this.textureW, this.textureH, this.factorW, this.factorH, color);
   }
}
