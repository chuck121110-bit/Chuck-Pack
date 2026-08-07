package xaero.hud.minimap.waypoint.util;

import net.minecraft.class_310;
import xaero.common.HudMod;
import xaero.common.gui.GuiMisc;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.lib.client.config.ClientConfigManager;

public class WaypointUtils {
   public static String getDistanceText(double distance, int precision, double convertToKmThreshold) {
      return convertToKmThreshold != (double)-1.0F && distance >= convertToKmThreshold ? GuiMisc.getFormat(precision).format(distance / (double)1000.0F) + "km" : GuiMisc.getFormat(precision).format(distance) + "m";
   }

   public static String getDistanceText(double distance) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      double convertToKmThreshold = (double)(Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_CONVERT_DISTANCE_TO_KM_AT);
      int precision = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_PRECISION);
      return getDistanceText(distance, precision, convertToKmThreshold);
   }

   public static String getDistanceText(Waypoint w, double fromX, double fromY, double fromZ, double contextCoordinateScale, double waypointCoordinateScale) {
      double waypointPosDivider = contextCoordinateScale / waypointCoordinateScale;
      double wX = (double)w.getX(waypointPosDivider) + (double)0.5F;
      double wZ = (double)w.getZ(waypointPosDivider) + (double)0.5F;
      double xFromEntity = wX - fromX;
      double yFromEntity = (double)w.getY() - fromY;
      if (!w.isYIncluded()) {
         yFromEntity = (double)0.0F;
      }

      double zFromEntity = wZ - fromZ;
      double distance = Math.sqrt(xFromEntity * xFromEntity + yFromEntity * yFromEntity + zFromEntity * zFromEntity);
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      double convertToKmThreshold = (double)(Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_CONVERT_DISTANCE_TO_KM_AT);
      int precision = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_PRECISION);
      return getDistanceText(distance, precision, convertToKmThreshold);
   }

   public static String getDistanceTextForCurrentWorld(Waypoint w, double waypointCoordinateScale, double fromX, double fromY, double fromZ) {
      double contextCoordinateScale = class_310.method_1551().field_1687.method_8597().comp_646();
      return getDistanceText(w, fromX, fromY, fromZ, contextCoordinateScale, waypointCoordinateScale);
   }
}
