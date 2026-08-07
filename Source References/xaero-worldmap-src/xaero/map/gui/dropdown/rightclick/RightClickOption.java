package xaero.map.gui.dropdown.rightclick;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import xaero.map.gui.IRightClickableElement;

public abstract class RightClickOption {
   protected final String name;
   protected final class_2583 style;
   protected int index;
   protected boolean active;
   protected IRightClickableElement target;
   protected Object[] nameFormatArgs;

   public RightClickOption(String name, class_2583 style, int index, IRightClickableElement target) {
      this.name = name;
      this.style = style;
      this.index = index;
      this.active = true;
      this.target = target;
      this.nameFormatArgs = new Object[0];
   }

   public RightClickOption(String name, int index, IRightClickableElement target) {
      this(name, class_2583.field_24360, index, target);
   }

   public abstract void onAction(class_437 var1);

   public boolean onSelected(class_437 screen) {
      boolean active = this.isActive();
      if (active && this.target.isRightClickValid()) {
         this.onAction(screen);
      }

      return active;
   }

   protected String getName() {
      return this.name;
   }

   public class_2561 getDisplayName() {
      class_5250 displayName = class_2561.method_43469(this.getName(), this.nameFormatArgs);
      if (!this.isActive()) {
         displayName.method_27692(class_124.field_1063);
      } else {
         displayName.method_27696(this.style);
      }

      return displayName;
   }

   public boolean isActive() {
      return this.active;
   }

   public RightClickOption setActive(boolean isActive) {
      this.active = isActive;
      return this;
   }

   public RightClickOption setNameFormatArgs(Object... nameFormatArgs) {
      this.nameFormatArgs = nameFormatArgs;
      return this;
   }
}
