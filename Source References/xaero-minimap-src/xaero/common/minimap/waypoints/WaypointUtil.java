package xaero.common.minimap.waypoints;

public class WaypointUtil {
   public static int getAddedMinimapIconFrame(int initialsWidth) {
      return getAddedMinimapIconFrame(0, initialsWidth);
   }

   public static int getAddedMinimapIconFrame(int addedFrame, int initialsWidth) {
      if (initialsWidth > 8) {
         int totalToAdd = initialsWidth - 8;
         int frameToAdd = totalToAdd - totalToAdd / 2;
         if (frameToAdd > addedFrame) {
            addedFrame = frameToAdd;
         }
      }

      return addedFrame;
   }
}
