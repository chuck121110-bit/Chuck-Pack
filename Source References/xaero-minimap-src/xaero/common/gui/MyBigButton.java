package xaero.common.gui;

import net.minecraft.class_2561;
import net.minecraft.class_4185;

public class MyBigButton extends class_4185.class_12231 {
   protected int id;

   public MyBigButton(int id, int par1, int par2, int par3, int par4, int par5, class_2561 par6Str, class_4185.class_4241 onPress) {
      super(par1, par2, par3, par4, par6Str, onPress, field_40754);
      this.id = id;
   }

   public MyBigButton(int id, int par1, int par2, class_2561 par5Str, class_4185.class_4241 onPress) {
      super(par1, par2, 200, 20, par5Str, onPress, field_40754);
      this.id = id;
   }

   public int getId() {
      return this.id;
   }
}
