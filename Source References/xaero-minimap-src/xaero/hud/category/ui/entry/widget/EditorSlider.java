package xaero.hud.category.ui.entry.widget;

import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_315;
import net.minecraft.class_3532;
import net.minecraft.class_4892;
import net.minecraft.class_5250;
import xaero.common.gui.IXaeroNarratableWidget;
import xaero.hud.category.ui.GuiCategoryEditor;

public class EditorSlider extends class_4892 implements IXaeroNarratableWidget {
   protected int currentIndex;
   protected int prevNarrationIndex;
   protected int optionCount;
   protected IntConsumer updatedIndexConsumer;
   protected Supplier<class_2561> messageSupplier;
   protected final GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList;

   public EditorSlider(IntConsumer updatedIndexConsumer, Supplier<class_2561> messageSupplier, int currentIndex, int optionCount, int widthIn, int heightIn, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
      super((class_315)null, 2, 2, widthIn, heightIn, (double)0.0F);
      this.updatedIndexConsumer = updatedIndexConsumer;
      this.messageSupplier = messageSupplier;
      this.optionCount = optionCount;
      this.currentIndex = this.prevNarrationIndex = currentIndex;
      this.field_22753 = this.toSliderValue(currentIndex);
      this.rowList = rowList;
      this.method_25346();
   }

   public boolean method_25404(class_11908 event) {
      if (event.comp_4795() == 263) {
         this.manualOptionChange(this.currentIndex - 1);
         return false;
      } else if (event.comp_4795() == 262) {
         this.manualOptionChange(this.currentIndex + 1);
         return false;
      } else {
         return super.method_25404(event);
      }
   }

   private void manualOptionChange(int index) {
      if (index < 0) {
         index = 0;
      } else if (index >= this.optionCount) {
         index = this.optionCount - 1;
      }

      this.field_22753 = this.toSliderValue(index);
      this.method_25344();
      this.method_25346();
   }

   public class_5250 method_25360() {
      return class_2561.method_43470("");
   }

   protected void method_25344() {
      this.currentIndex = this.toValue(this.field_22753);
      this.updatedIndexConsumer.accept(this.currentIndex);
   }

   protected void method_25346() {
      this.method_25355((class_2561)this.messageSupplier.get());
      if (this.currentIndex != this.prevNarrationIndex) {
         this.rowList.narrateSelection();
      }

      this.prevNarrationIndex = this.currentIndex;
   }

   public double toSliderValue(int i) {
      return (double)i / (double)(this.optionCount - 1);
   }

   public int toValue(double d) {
      return (int)this.clamp(class_3532.method_16436(class_3532.method_15350(d, (double)0.0F, (double)1.0F), (double)0.0F, (double)(this.optionCount - 1)));
   }

   private double clamp(double d) {
      d = (double)Math.round(d);
      return class_3532.method_15350(d, (double)0.0F, (double)(this.optionCount - 1));
   }
}
