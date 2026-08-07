package xaero.hud.minimap.element.render.map;

import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import xaero.common.HudMod;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.element.render.MinimapElementRendererHandler;

public final class MinimapElementMapRendererHandler extends MinimapElementRendererHandler {
   private double ps;
   private double pc;
   private double zoom;
   private float halfWView;

   private MinimapElementMapRendererHandler(HudMod modMain, List<MinimapElementRenderer<?, ?>> renderers, MinimapElementGraphics guiGraphics) {
      super(modMain, renderers, MinimapElementRenderLocation.IN_MINIMAP, 19490, guiGraphics);
   }

   public void prepareRender(double ps, double pc, double zoom, float halfWView) {
      this.ps = ps;
      this.pc = pc;
      this.zoom = zoom;
      this.halfWView = halfWView;
   }

   protected <E, RRC, RR extends MinimapElementRenderer<E, RRC>> boolean transformAndRenderForRenderer(E element, double elementX, double elementY, double elementZ, RR renderer, RRC context, int elementIndex, double optionalDepth, MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource) {
      class_4587 matrixStack = this.guiGraphics.pose();
      class_243 renderPos = renderInfo.renderPos;
      double offx = elementX - renderPos.field_1352;
      double offz = elementZ - renderPos.field_1350;
      matrixStack.method_22903();
      double zoomedOffX = offx * this.zoom;
      double zoomedOffZ = offz * this.zoom;
      double translateX = this.ps * zoomedOffX - this.pc * zoomedOffZ;
      double translateY = this.pc * zoomedOffX + this.ps * zoomedOffZ;
      int roundedX = (int)Math.round(translateX);
      int roundedY = (int)Math.round(translateY);
      boolean outOfBounds = (float)Math.abs(roundedX) > this.halfWView || (float)Math.abs(roundedY) > this.halfWView;
      double partialX = translateX - (double)roundedX;
      double partialY = translateY - (double)roundedY;
      matrixStack.method_46416((float)roundedX, (float)roundedY, 0.0F);
      boolean result = ((MinimapElementRenderer)renderer).renderElement(element, false, outOfBounds, optionalDepth, 1.0F, partialX, partialY, renderInfo, this.guiGraphics, vanillaBufferSource);
      matrixStack.method_22909();
      return result;
   }

   protected void beforeRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource) {
   }

   protected void afterRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource) {
   }

   public static final class Builder extends MinimapElementRendererHandler.Builder<Builder> {
      public MinimapElementMapRendererHandler build() {
         return (MinimapElementMapRendererHandler)super.build();
      }

      protected MinimapElementMapRendererHandler buildInternally(List<MinimapElementRenderer<?, ?>> renderers, MinimapElementGraphics graphics) {
         return new MinimapElementMapRendererHandler(HudMod.INSTANCE, renderers, graphics);
      }

      protected Builder setDefault() {
         super.setDefault();
         return this;
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
