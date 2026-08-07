package xaero.hud.minimap.waypoint.render;

import net.minecraft.class_310;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.waypoint.WaypointPurpose;

public class WaypointMapRenderReader extends MinimapElementReader<Waypoint, WaypointMapRenderContext> {
   public double getRenderX(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return (double)element.getX() + (double)0.5F;
   }

   public double getRenderY(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return (double)(element.getY() + 1);
   }

   public double getRenderZ(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return (double)element.getZ() + (double)0.5F;
   }

   public double getCoordinateScale(Waypoint element, WaypointMapRenderContext context, MinimapElementRenderInfo renderInfo) {
      return context.dimCoordinateScale;
   }

   public boolean shouldScalePartialCoordinates(Waypoint element, WaypointMapRenderContext context, MinimapElementRenderInfo renderInfo) {
      return false;
   }

   public boolean isHidden(Waypoint element, WaypointMapRenderContext context) {
      return false;
   }

   public int getInteractionBoxLeft(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return 0;
   }

   public int getInteractionBoxRight(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return 0;
   }

   public int getInteractionBoxTop(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return 0;
   }

   public int getInteractionBoxBottom(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return 0;
   }

   public int getRenderBoxLeft(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return -this.getRenderBoxRight(element, context, partialTicks);
   }

   public int getRenderBoxRight(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      int j = element.getPurpose() == WaypointPurpose.DEATH ? 4 : class_310.method_1551().field_1772.method_1727(element.getInitials()) / 2;
      int addedFrame = j > 4 ? j - 4 : 0;
      return 5 + addedFrame;
   }

   public int getRenderBoxTop(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return this.getRenderBoxLeft(element, context, partialTicks);
   }

   public int getRenderBoxBottom(Waypoint element, WaypointMapRenderContext context, float partialTicks) {
      return this.getRenderBoxRight(element, context, partialTicks);
   }

   public int getLeftSideLength(Waypoint element, class_310 mc) {
      return 0;
   }

   public String getMenuName(Waypoint element) {
      return "n/a";
   }

   public String getFilterName(Waypoint element) {
      return this.getMenuName(element);
   }

   public int getMenuTextFillLeftPadding(Waypoint element) {
      return 0;
   }

   public int getRightClickTitleBackgroundColor(Waypoint element) {
      return 0;
   }

   public boolean shouldScaleBoxWithOptionalScale() {
      return false;
   }
}
