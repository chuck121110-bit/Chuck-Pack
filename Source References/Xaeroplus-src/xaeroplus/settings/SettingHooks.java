package xaeroplus.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import xaeroplus.XaeroPlus;
import xaeroplus.util.FileUtil;

public class SettingHooks {
   public static void saveSettings() {
      try {
         saveXPSettings();
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed saving settings", e);
      }

   }

   public static synchronized void saveXPSettings() throws IOException {
      FileUtil.safeSave(XaeroPlus.configFile, (w) -> {
         PrintWriter writer = new PrintWriter(w);

         try {
            List<XaeroPlusSetting> allSettings = Settings.REGISTRY.getAllSettings().stream().sorted(Comparator.comparing(XaeroPlusSetting::getSettingName)).toList();

            for(int i = 0; i < allSettings.size(); ++i) {
               XaeroPlusSetting setting = (XaeroPlusSetting)allSettings.get(i);
               String var10001 = setting.getSettingName();
               writer.println(var10001 + ":" + setting.getSerializedValue());
            }
         } catch (Throwable var6) {
            try {
               writer.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         writer.close();
      });
   }

   public static synchronized void loadXPSettings() {
      try {
         if (!XaeroPlus.configFile.exists()) {
            return;
         }

         loadXPSettingsFromFile(XaeroPlus.configFile);
      } catch (Throwable e) {
         XaeroPlus.LOGGER.error("Error loading XaeroPlus settings", e);
      }

   }

   public static synchronized void loadXPSettingsFromFile(File file) throws IOException {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      String s;
      try {
         while((s = reader.readLine()) != null) {
            int colonIndex = s.indexOf(58);
            if (colonIndex != -1) {
               String settingName = s.substring(0, colonIndex);
               if (!settingName.isBlank()) {
                  String settingValue = s.substring(colonIndex + 1);
                  XaeroPlusSetting setting = Settings.REGISTRY.getSettingByName(settingName);
                  if (setting == null) {
                     XaeroPlus.LOGGER.warn("Setting not found: {}", settingName);
                  } else {
                     setting.deserializeValue(settingValue);
                  }
               }
            }
         }
      } catch (Throwable var8) {
         try {
            reader.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      reader.close();
   }
}
