package xaero.hud.minimap.waypoint.render.world;

import net.minecraft.class_310;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;

public class WaypointWorldRenderReader extends MinimapElementReader<Waypoint, WaypointWorldRenderContext> {
   private final WaypointWorldRenderContext context;

   public WaypointWorldRenderReader(WaypointWorldRenderContext context) {
      this.context = context;
   }

   public boolean isHidden(Waypoint element, WaypointWorldRenderContext context) {
      return false;
   }

   public double getRenderX(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return (double)element.getX() + (double)0.5F;
   }

   public double getRenderY(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      if (element.isYIncluded()) {
         return (double)(element.getY() + 1);
      } else {
         return context.renderEntityPos == null ? EntityUtils.getEntityY(class_310.method_1551().method_1560(), partialTicks) + (double)1.0F : context.renderEntityPos.field_1351 + (double)1.0F;
      }
   }

   public double getRenderZ(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return (double)element.getZ() + (double)0.5F;
   }

   public double getCoordinateScale(Waypoint element, WaypointWorldRenderContext context, MinimapElementRenderInfo renderInfo) {
      return context.dimCoordinateScale;
   }

   public boolean shouldScalePartialCoordinates(Waypoint element, WaypointWorldRenderContext context, MinimapElementRenderInfo renderInfo) {
      return false;
   }

   public int getInteractionBoxLeft(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return context.interactionBoxLeft;
   }

   public int getInteractionBoxRight(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return -context.interactionBoxLeft;
   }

   public int getInteractionBoxTop(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return context.interactionBoxTop;
   }

   public int getInteractionBoxBottom(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return -context.interactionBoxTop;
   }

   public int getRenderBoxLeft(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return -32;
   }

   public int getRenderBoxRight(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return 32;
   }

   public int getRenderBoxTop(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return -32;
   }

   public int getRenderBoxBottom(Waypoint element, WaypointWorldRenderContext context, float partialTicks) {
      return 32;
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

   public boolean isInteractable(MinimapElementRenderLocation location, Waypoint element) {
      return true;
   }

   public boolean isAlwaysHighlightedWhenHovered(Waypoint element, WaypointWorldRenderContext context) {
      return !context.onlyMainInfo;
   }
}
