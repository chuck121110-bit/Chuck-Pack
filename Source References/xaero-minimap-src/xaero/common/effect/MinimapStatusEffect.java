package xaero.common.effect;

import net.minecraft.class_1291;
import net.minecraft.class_2960;
import net.minecraft.class_4081;

public class MinimapStatusEffect extends class_1291 {
   private class_2960 id;

   protected MinimapStatusEffect(class_4081 type, int color, String idPrefix) {
      super(type, color);
      String suffix = type == class_4081.field_18272 ? "_harmful" : (type == class_4081.field_18271 ? "_beneficial" : "");
      this.setRegistryName(class_2960.method_60655("xaerominimap", idPrefix + suffix));
   }

   protected void setRegistryName(class_2960 id) {
      this.id = id;
   }

   public class_2960 getRegistryName() {
      return this.id;
   }
}
