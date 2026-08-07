package xaeroplus.feature.drawing;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.TooltipButton;
import xaeroplus.util.Color;

public class DrawingColorPickerButton extends TooltipButton {
   private final Supplier<Color> colorSupplier;

   public DrawingColorPickerButton(final int x, final int y, final Supplier<Tooltip> tooltip, final Supplier<Color> colorSupplier, final class_4185.class_4241 onPress) {
      super(x, y, 20, 20, class_2561.method_43473(), onPress, tooltip);
      this.colorSupplier = (Supplier)Objects.requireNonNull(colorSupplier);
   }

   public void method_75752(final class_332 guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
      int minX = this.method_46426() + 6;
      int minY = this.method_46427() + 6;
      if (this.method_37303() && this.method_49606()) {
         --minY;
      }

      guiGraphics.method_25294(minX, minY, minX + 10, minY + 10, ((Color)this.colorSupplier.get()).getInt());
   }
}
