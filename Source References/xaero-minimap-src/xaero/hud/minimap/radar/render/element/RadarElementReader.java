package xaero.hud.minimap.radar.render.element;

import net.minecraft.class_1297;
import net.minecraft.class_310;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;

public final class RadarElementReader extends MinimapElementReader<class_1297, RadarRenderContext> {
   public double getRenderX(class_1297 element, RadarRenderContext context, float partialTicks) {
      return EntityUtils.getEntityX(element, partialTicks);
   }

   public double getRenderY(class_1297 element, RadarRenderContext context, float partialTicks) {
      return EntityUtils.getEntityY(element, partialTicks);
   }

   public double getRenderZ(class_1297 element, RadarRenderContext context, float partialTicks) {
      return EntityUtils.getEntityZ(element, partialTicks);
   }

   public boolean isHidden(class_1297 element, RadarRenderContext context) {
      return false;
   }

   public int getInteractionBoxLeft(class_1297 element, RadarRenderContext context, float partialTicks) {
      return context.icon ? -16 : -6;
   }

   public int getInteractionBoxRight(class_1297 element, RadarRenderContext context, float partialTicks) {
      return context.icon ? 16 : 6;
   }

   public int getInteractionBoxTop(class_1297 element, RadarRenderContext context, float partialTicks) {
      return context.icon ? -16 : -6;
   }

   public int getInteractionBoxBottom(class_1297 element, RadarRenderContext context, float partialTicks) {
      return context.icon ? 16 : 6;
   }

   public int getRenderBoxLeft(class_1297 element, RadarRenderContext context, float partialTicks) {
      return -64;
   }

   public int getRenderBoxRight(class_1297 element, RadarRenderContext context, float partialTicks) {
      return 64;
   }

   public int getRenderBoxTop(class_1297 element, RadarRenderContext context, float partialTicks) {
      return -32;
   }

   public int getRenderBoxBottom(class_1297 element, RadarRenderContext context, float partialTicks) {
      return 32;
   }

   public int getLeftSideLength(class_1297 element, class_310 mc) {
      return 0;
   }

   public String getMenuName(class_1297 element) {
      return "n/a";
   }

   public String getFilterName(class_1297 element) {
      return this.getMenuName(element);
   }

   public int getMenuTextFillLeftPadding(class_1297 element) {
      return 0;
   }

   public int getRightClickTitleBackgroundColor(class_1297 element) {
      return 0;
   }

   public float getBoxScale(MinimapElementRenderLocation location, class_1297 element, RadarRenderContext context) {
      return (location == MinimapElementRenderLocation.OVER_MINIMAP ? 0.5F : 1.0F) * (float)(context.icon ? context.iconScale : context.dotScale);
   }

   public boolean isInteractable(MinimapElementRenderLocation location, class_1297 element) {
      return true;
   }

   public boolean shouldScaleBoxWithOptionalScale() {
      return true;
   }
}
