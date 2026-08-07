package xaero.map.mods.pac.gui.claim.element;

import net.minecraft.class_4587;
import net.minecraft.class_4597;
import xaero.lib.XaeroLib;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.map.element.MapElementGraphics;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.CustomRenderTypes;
import xaero.map.graphics.MapRenderHelper;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.mods.pac.gui.claim.ClaimResultElement;
import xaero.map.mods.pac.gui.claim.ClaimResultElementManager;

public class ClaimResultElementRenderer extends ElementRenderer<ClaimResultElement, ClaimResultElementRenderContext, ClaimResultElementRenderer> {
   private final ClaimResultElementManager manager;

   private ClaimResultElementRenderer(ClaimResultElementManager manager, ClaimResultElementRenderContext context, ClaimResultElementRenderProvider provider, ClaimResultElementRenderReader reader) {
      super(context, provider, reader);
      this.manager = manager;
   }

   public void preRender(ElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
      XaeroBufferProvider renderTypeBuffers = XaeroLib.INSTANCE.getClient().getBufferProvider();
      (this.context).guiIconBuffer = renderTypeBuffers.getBuffer(CustomRenderTypes.GUI_BILINEAR);
      (this.context).toDelete.clear();
   }

   public void postRender(ElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
      XaeroBufferProvider renderTypeBuffers = XaeroLib.INSTANCE.getClient().getBufferProvider();
      renderTypeBuffers.endBatch();

      for(ClaimResultElement element : (this.context).toDelete) {
         this.manager.remove(element);
      }

   }

   public void renderElementShadow(ClaimResultElement element, boolean hovered, float optionalScale, double partialX, double partialY, ElementRenderInfo renderInfo, MapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
   }

   public boolean renderElement(ClaimResultElement element, boolean hovered, double optionalDepth, float optionalScale, double partialX, double partialY, ElementRenderInfo renderInfo, MapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
      class_4587 matrixStack = guiGraphics.pose();
      long time = System.currentTimeMillis();
      int iconScale = (int)Math.ceil((double)optionalScale);
      matrixStack.method_22904(partialX, partialY, (double)0.0F);
      matrixStack.method_22905((float)iconScale, (float)iconScale, 1.0F);
      int iconU = element.hasPositive() ? 0 : 32;
      int iconV = 78;
      float r;
      float g;
      float b;
      if (element.hasPositive() == element.hasNegative()) {
         r = 1.0F;
         g = 0.6666667F;
         b = 0.0F;
      } else if (element.hasPositive()) {
         r = 0.0F;
         g = 0.6666667F;
         b = 0.0F;
      } else {
         r = 0.8F;
         g = 0.1F;
         b = 0.1F;
      }

      MapRenderHelper.blitIntoExistingBuffer(matrixStack.method_23760().method_23761(), (this.context).guiIconBuffer, -16.0F, -16.0F, iconU, iconV, 32, 32, 32, 32, r, g, b, 1.0F, 256, 256);
      if (hovered) {
         element.setFadeOutStartTime(time);
      }

      if (time - element.getFadeOutStartTime() > 3000L) {
         (this.context).toDelete.add(element);
      }

      return false;
   }

   public boolean shouldRender(ElementRenderLocation location, boolean pre) {
      return true;
   }

   public int getOrder() {
      return 150;
   }

   public static final class Builder {
      private ClaimResultElementManager manager;

      private Builder() {
      }

      private Builder setDefault() {
         this.setManager((ClaimResultElementManager)null);
         return this;
      }

      public Builder setManager(ClaimResultElementManager manager) {
         this.manager = manager;
         return this;
      }

      public ClaimResultElementRenderer build() {
         if (this.manager == null) {
            throw new IllegalStateException();
         } else {
            return new ClaimResultElementRenderer(this.manager, new ClaimResultElementRenderContext(), new ClaimResultElementRenderProvider(this.manager), new ClaimResultElementRenderReader());
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
