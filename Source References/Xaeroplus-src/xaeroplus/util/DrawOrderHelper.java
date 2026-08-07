package xaeroplus.util;

import java.util.ArrayList;
import java.util.List;
import xaeroplus.settings.Settings;

public class DrawOrderHelper {
   public static List<String> load(String serializedValue) {
      ArrayList<String> result = new ArrayList();
      if (serializedValue.isBlank()) {
         return result;
      } else {
         String[] split = serializedValue.split("\\|");

         for(String s : split) {
            if (!s.isBlank()) {
               result.add(s.trim());
            }
         }

         return result;
      }
   }

   public static List<String> load() {
      return load(Settings.REGISTRY.drawOrderSetting.getSerializedValue());
   }

   public static String serialize(List<String> ids) {
      return ids.isEmpty() ? "" : String.join("|", ids);
   }
}
