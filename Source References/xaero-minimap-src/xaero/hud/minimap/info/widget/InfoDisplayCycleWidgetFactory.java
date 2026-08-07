package xaero.hud.minimap.info.widget;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import xaero.common.gui.GuiInfoDisplayEdit;

public class InfoDisplayCycleWidgetFactory<T> implements InfoDisplayWidgetFactory<T> {
   private final List<T> values;
   private final List<class_2561> valueNames;

   public InfoDisplayCycleWidgetFactory(List<T> values, List<class_2561> valueNames) {
      this.values = values;
      this.valueNames = valueNames;
   }

   public class_339 create(int x, int y, int w, int h, GuiInfoDisplayEdit.MoveableEntry<T> entry, Runnable onChange, boolean includeNull) {
      return InfoDisplayCycleButton.Builder.begin().setBounds(x, y, w, h).setEntry(entry).setValues(this.values, this.valueNames).setOnChange(onChange).setIncludeNull(includeNull).build();
   }
}
