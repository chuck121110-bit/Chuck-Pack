package xaero.map.util;

import java.util.ArrayList;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2583;
import net.minecraft.class_437;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.util.NumberFormatUtils;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.entity.util.EntityUtil;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

public class DistanceUtils {
   public static String getDistanceText(double distance, int precision, double convertToKmThreshold) {
      return convertToKmThreshold != (double)-1.0F && distance >= convertToKmThreshold ? NumberFormatUtils.getPrecisionFormat(precision).format(distance / (double)1000.0F) + "km" : NumberFormatUtils.getPrecisionFormat(precision).format(distance) + "m";
   }

   public static void addDistanceRightClickOption(double posX, double posY, double posZ, boolean is3D, IRightClickableElement target, class_1297 fromEntity, float partialTicks, ClientConfigManager configManager, ArrayList<RightClickOption> options) {
      if (fromEntity != null) {
         boolean distancesConfig = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.DISTANCES);
         if (distancesConfig) {
            int precisionConfig = (Integer)configManager.getEffective(WorldMapProfiledConfigOptions.DISTANCE_PRECISION);
            int kmAtConfig = (Integer)configManager.getEffective(WorldMapProfiledConfigOptions.DISTANCE_TO_KM_AT);
            double renderEntityX = EntityUtil.getEntityX(fromEntity, partialTicks);
            double renderEntityY = is3D ? EntityUtil.getEntityY(fromEntity, partialTicks) : (double)0.0F;
            double renderEntityZ = EntityUtil.getEntityZ(fromEntity, partialTicks);
            double fromRenderEntityX = posX - renderEntityX;
            double fromRenderEntityY = is3D ? posY - renderEntityY : (double)0.0F;
            double fromRenderEntityZ = posZ - renderEntityZ;
            double distance = Math.sqrt(fromRenderEntityX * fromRenderEntityX + fromRenderEntityY * fromRenderEntityY + fromRenderEntityZ * fromRenderEntityZ);
            String distanceText = getDistanceText(distance, precisionConfig, (double)kmAtConfig);
            options.add(new RightClickOption(distanceText, class_2583.field_24360.method_10977(class_124.field_1080), options.size(), target) {
               public void onAction(class_437 screen) {
               }
            });
         }
      }
   }
}
