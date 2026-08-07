package xaero.hud.minimap.info.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import xaero.common.gui.GuiInfoDisplayEdit;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.common.config.util.ConfigConstants;

public final class InfoDisplayCycleButton extends class_4185.class_12231 {
   private int currentIndex;

   private InfoDisplayCycleButton(int currentIndex, int x, int y, int w, int h, class_2561 component, class_4185.class_4241 onPress) {
      super(x, y, w, h, component, onPress, field_40754);
      this.currentIndex = currentIndex;
   }

   public static final class Builder<T> {
      private int x;
      private int y;
      private int w;
      private int h;
      private List<T> values;
      private List<class_2561> valueNames;
      private GuiInfoDisplayEdit.MoveableEntry<T> entry;
      private Runnable onChange;
      private boolean includeNull;

      private Builder() {
      }

      public Builder<T> setDefault() {
         this.setBounds(0, 0, 0, 0);
         this.setValues((List)null, (List)null);
         this.setEntry((GuiInfoDisplayEdit.MoveableEntry)null);
         this.setIncludeNull(false);
         return this;
      }

      public Builder<T> setBounds(int x, int y, int w, int h) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
         return this;
      }

      public Builder<T> setValues(List<T> values, List<class_2561> valueNames) {
         if (values == null != (valueNames == null)) {
            throw new IllegalArgumentException();
         } else if (values != null && values.size() != valueNames.size()) {
            throw new IllegalArgumentException();
         } else {
            this.values = values;
            this.valueNames = valueNames;
            return this;
         }
      }

      public Builder<T> setEntry(GuiInfoDisplayEdit.MoveableEntry<T> entry) {
         this.entry = entry;
         return this;
      }

      public Builder<T> setOnChange(Runnable onChange) {
         this.onChange = onChange;
         return this;
      }

      public Builder<T> setIncludeNull(boolean includeNull) {
         this.includeNull = includeNull;
         return this;
      }

      public InfoDisplayCycleButton build() {
         if (this.w != 0 && this.h != 0 && this.values != null && this.entry != null) {
            List<T> allValues = this.values;
            List<class_2561> allValueNames = this.valueNames;
            if (this.includeNull) {
               allValues = new ArrayList(this.values);
               allValueNames = new ArrayList(this.valueNames);
               allValues.add(0, (Object)null);
               allValueNames.add(0, ConfigConstants.UNSPECIFIED);
            }

            int currentStateIndex = allValues.indexOf(this.entry.getState());
            if (currentStateIndex < 0) {
               this.entry.setState(allValues.get(0));
               currentStateIndex = 0;
            }

            GuiInfoDisplayEdit.MoveableEntry<T> finalEntry = this.entry;
            Runnable finalOnChange = this.onChange;
            class_4185.class_4241 action = (b) -> {
               InfoDisplayCycleButton cycleButton = (InfoDisplayCycleButton)b;
               if (ScreenBase.hasShiftDown()) {
                  --cycleButton.currentIndex;
                  if (cycleButton.currentIndex < 0) {
                     cycleButton.currentIndex = allValues.size() - 1;
                  }
               } else {
                  cycleButton.currentIndex = (cycleButton.currentIndex + 1) % allValues.size();
               }

               finalEntry.setState(allValues.get(cycleButton.currentIndex));
               cycleButton.method_25355((class_2561)allValueNames.get(cycleButton.currentIndex));
               if (finalOnChange != null) {
                  finalOnChange.run();
               }

            };
            return new InfoDisplayCycleButton(currentStateIndex, this.x, this.y, this.w, this.h, (class_2561)allValueNames.get(currentStateIndex), action);
         } else {
            throw new IllegalStateException();
         }
      }

      public static <T> Builder<T> begin() {
         return (new Builder<T>()).setDefault();
      }
   }
}
