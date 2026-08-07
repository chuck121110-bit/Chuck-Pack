package xaero.hud.gui.util;

import java.util.List;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.gui.ScreenBase;

public class GuiUtils {
   public static float getMinimapScale(ClientConfigManager configManager) {
      int uiScaleConfigValue = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.UI_SCALE);
      List<Integer> validValues = MinimapProfiledConfigOptions.UI_SCALE.getValidValues();
      int min = (Integer)validValues.get(0);
      int max = (Integer)validValues.get(validValues.size() - 1);
      return getUIScale(uiScaleConfigValue, min, max);
   }

   public static float getUIScale(int optionValue, int min, int max) {
      if (optionValue <= min) {
         return (float)getAutoUIScale();
      } else {
         return optionValue == max ? (float)class_310.method_1551().method_22683().method_4495() : (float)optionValue;
      }
   }

   public static int getAutoUIScale() {
      int height = class_310.method_1551().method_22683().method_4506();
      int width = class_310.method_1551().method_22683().method_4489();
      int size = height <= width ? height : width;
      if (size >= 1500) {
         int steps = size / 500;
         return steps;
      } else {
         return 2;
      }
   }

   public static void refreshScreenBase() {
      class_437 currentScreen = class_310.method_1551().field_1755;
      ScreenBase screenBase = currentScreen instanceof ScreenBase ? (ScreenBase)currentScreen : null;
      if (screenBase != null) {
         screenBase.refresh();
      }
   }
}
