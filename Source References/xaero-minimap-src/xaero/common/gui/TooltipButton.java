package xaero.common.gui;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import xaero.lib.client.gui.widget.ITooltipHaver;
import xaero.lib.client.gui.widget.Tooltip;

public class TooltipButton extends class_4185.class_12231 implements ITooltipHaver {
   private Supplier<Tooltip> tooltipSupplier;

   public TooltipButton(int x, int y, int w, int h, class_2561 text, class_4185.class_4241 onPress, Supplier<Tooltip> tooltipSupplier) {
      super(x, y, w, h, text, onPress, field_40754);
      this.tooltipSupplier = tooltipSupplier;
   }

   public Supplier<Tooltip> getXaero_tooltip() {
      return this.tooltipSupplier;
   }
}
