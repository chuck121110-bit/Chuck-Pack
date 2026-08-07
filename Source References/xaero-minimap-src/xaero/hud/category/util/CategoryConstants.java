package xaero.hud.category.util;

import net.minecraft.class_2561;

public class CategoryConstants {
   public static class_2561 ON = class_2561.method_43471("gui.xaero_on");
   public static class_2561 OFF = class_2561.method_43471("gui.xaero_off");
   public static class_2561 INHERIT = class_2561.method_43471("gui.xaero_category_setting_inherit");

   public static class_2561 getBooleanComponent(boolean value) {
      return value ? ON : OFF;
   }
}
