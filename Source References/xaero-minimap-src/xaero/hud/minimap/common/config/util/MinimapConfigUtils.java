package xaero.hud.minimap.common.config.util;

import java.util.function.BiFunction;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import xaero.common.platform.Services;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.lib.common.config.option.ConfigOption;

public class MinimapConfigUtils {
   public static BiFunction<ConfigOption<Integer>, Integer, class_2561> getUIScaleDisplayGetter(int auto, int max, boolean includeAutoValue) {
      return getUIScaleDisplayGetter(auto, max, (double)1.0F, includeAutoValue);
   }

   public static BiFunction<ConfigOption<Integer>, Integer, class_2561> getUIScaleDisplayGetter(int auto, int max, double autoScale, boolean includeAutoValue) {
      return (o, v) -> {
         if (v == auto) {
            class_5250 result = MinimapConfigConstants.AUTO_SCALE_COMPONENT.method_27661();
            if (includeAutoValue && !Services.PLATFORM.isDedicatedServer()) {
               MinimapConfigClientUtils.addAutoUIScaleValueToComponent(result, autoScale);
            }

            return result;
         } else if (v == max) {
            class_5250 result = MinimapConfigConstants.MINECRAFT_SCALE_COMPONENT.method_27661();
            if (!Services.PLATFORM.isDedicatedServer()) {
               MinimapConfigClientUtils.addAutoMCScaleValueToComponent(result);
            }

            return result;
         } else {
            return class_2561.method_43470(v.toString());
         }
      };
   }

   public static BiFunction<ConfigOption<Integer>, Integer, class_2561> getUIScaleDisplayGetter(int auto, int max) {
      return getUIScaleDisplayGetter(auto, max, (double)1.0F);
   }

   public static BiFunction<ConfigOption<Integer>, Integer, class_2561> getUIScaleDisplayGetter(int auto, int max, double autoScale) {
      return getUIScaleDisplayGetter(auto, max, autoScale, true);
   }

   public static class_2561 getAutoMinimapSizeName() {
      class_5250 component = class_2561.method_43471("gui.xaero_auto_map_size");
      if (!Services.PLATFORM.isDedicatedServer()) {
         component.method_10855().add(class_2561.method_43470(" (" + MinimapConfigClientUtils.getAutoMinimapSize() + ")"));
      }

      return component;
   }
}
