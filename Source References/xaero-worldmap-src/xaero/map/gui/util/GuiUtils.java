package xaero.map.gui.util;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import xaero.lib.client.controls.util.KeyMappingUtils;

public class GuiUtils {
   public static class_2561 getBoundKeyComponent(String keyName) {
      return class_2561.method_43470(keyName).method_27692(class_124.field_1077);
   }

   public static class_2561 getBoundKeyComponent(class_304 keyMapping) {
      return getBoundKeyComponent(KeyMappingUtils.getKeyName(keyMapping));
   }
}
