package xaero.hud.gui.widget;

import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_357;
import xaero.lib.client.gui.widget.IClickableWidget;
import xaero.lib.client.gui.widget.Tooltip;

public class XaeroSliderWidget extends class_357 implements IClickableWidget {
   private Supplier<Tooltip> tooltip;
   private final DoubleConsumer valueListener;
   private final Supplier<class_2561> labelGetter;

   public XaeroSliderWidget(int x, int y, int w, int h, class_2561 label, double value, DoubleConsumer valueListener, Supplier<class_2561> labelGetter) {
      super(x, y, w, h, label, value);
      this.valueListener = valueListener;
      this.labelGetter = labelGetter;
   }

   protected void method_25344() {
      this.valueListener.accept(this.field_22753);
   }

   protected void method_25346() {
      this.method_25355((class_2561)this.labelGetter.get());
   }

   public void setXaero_tooltip(Supplier<Tooltip> tooltip) {
      this.tooltip = tooltip;
   }

   public Supplier<Tooltip> getXaero_tooltip() {
      return this.tooltip;
   }
}
