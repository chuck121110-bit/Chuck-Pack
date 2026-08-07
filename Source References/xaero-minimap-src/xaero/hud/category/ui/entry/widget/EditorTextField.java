package xaero.hud.category.ui.entry.widget;

import java.util.function.Predicate;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_5250;
import xaero.common.gui.IXaeroNarratableWidget;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.util.GuiUtils;

public class EditorTextField extends class_342 implements IXaeroNarratableWidget {
   private final UpdatedValueConsumer updatedValueConsumer;
   private int highlightPos;
   private final GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList;
   private boolean pauseCallback;
   private final Predicate<String> validator;

   public EditorTextField(UpdatedValueConsumer updatedValueConsumer, String startValue, int startCursorPos, int startHighlighPos, int maxLength, class_327 font, int w, int h, class_2561 message, Predicate<String> validator, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
      super(font, 3, 3, w, h, message);
      this.method_1880(maxLength);
      this.method_1852(startValue);
      this.method_1875(startCursorPos);
      this.method_1884(startHighlighPos);
      this.updatedValueConsumer = updatedValueConsumer;
      this.rowList = rowList;
      this.validator = validator;
      this.updateColorForValue(startValue);
      this.method_1863((s) -> {
         if (!this.pauseCallback) {
            this.updateColorForValue(s);
            this.updatedValueConsumer.accept(s, this.method_1881(), this.highlightPos, this.rowList);
         }
      });
   }

   private void updateColorForValue(String value) {
      this.method_1868(this.validator.test(value) ? -2039584 : -43691);
   }

   public void method_1875(int i) {
      super.method_1875(i);
   }

   public void method_1884(int i) {
      super.method_1884(i);
      this.highlightPos = i;
   }

   public class_5250 method_25360() {
      return super.method_25360();
   }

   public void method_48579(class_332 guiGraphics, int i, int j, float f) {
      super.method_48579(guiGraphics, i, j, f);
      int o = this.method_46426() + 4;
      int p = this.method_46427() + (this.field_22759 - 8) / 2;
      if (this.method_1882().isEmpty() && !this.method_25370()) {
         guiGraphics.method_51439(class_310.method_1551().field_1772, this.method_25369(), o, p, -11184811, true);
         GuiUtils.setFieldText(this, "");
         this.pauseCallback = false;
      }

   }

   @FunctionalInterface
   public interface UpdatedValueConsumer {
      void accept(String var1, int var2, int var3, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList var4);
   }
}
