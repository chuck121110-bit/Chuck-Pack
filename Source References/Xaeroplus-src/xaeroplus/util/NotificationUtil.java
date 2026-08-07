package xaeroplus.util;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class NotificationUtil {
   public static void inGameNotification(String message) {
      class_310 mc = class_310.method_1551();
      mc.execute(() -> {
         if (mc.field_1724 != null) {
            mc.field_1705.method_1743().method_1812(class_2561.method_43470("[").method_27692(class_124.field_1080).method_10852(class_2561.method_43470("XaeroPlus").method_27692(class_124.field_1075)).method_10852(class_2561.method_43470("] ").method_27692(class_124.field_1080)).method_10852(class_2561.method_43470(message).method_27692(class_124.field_1068)));
         }

      });
   }

   public static void errorNotification(String message) {
      class_310 mc = class_310.method_1551();
      mc.execute(() -> {
         if (mc.field_1724 != null) {
            mc.field_1705.method_1743().method_1812(class_2561.method_43470("[").method_27692(class_124.field_1080).method_10852(class_2561.method_43470("XaeroPlus").method_27692(class_124.field_1061)).method_10852(class_2561.method_43470("] ").method_27692(class_124.field_1080)).method_10852(class_2561.method_43470(message).method_27692(class_124.field_1061)));
         }

      });
   }
}
