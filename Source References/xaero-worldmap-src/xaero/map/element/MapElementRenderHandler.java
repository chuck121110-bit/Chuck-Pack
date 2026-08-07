package xaero.map.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.gui.GuiMap;
import xaero.map.mods.SupportMods;
import xaero.map.world.MapDimension;

public class MapElementRenderHandler {
   protected final MapElementGraphics guiGraphics;
   private final List<ElementRenderer<?, ?, ?>> renderers;
   protected final ElementRenderLocation location;
   private HoveredMapElementHolder<?, ?> previousHovered;
   private boolean previousHoveredPresent;
   private boolean renderingHovered;
   private Object workingHovered;
   private ElementRenderer<?, ?, ?> workingHoveredRenderer;

   private MapElementRenderHandler(List<ElementRenderer<?, ?, ?>> renderers, ElementRenderLocation location, MapElementGraphics guiGraphics) {
      this.renderers = renderers;
      this.location = location;
      this.guiGraphics = guiGraphics;
   }

   public void add(ElementRenderer<?, ?, ?> renderer) {
      this.renderers.add(renderer);
   }

   public static <E, C> HoveredMapElementHolder<E, C> createResult(E hovered, ElementRenderer<?, ?, ?> hoveredRenderer) {
      return new HoveredMapElementHolder<E, C>(hovered, hoveredRenderer);
   }

   private <E> ElementRenderer<E, ?, ?> getRenderer(HoveredMapElementHolder<E, ?> holder) {
      return holder.getRenderer();
   }

   public HoveredMapElementHolder<?, ?> render(GuiMap mapScreen, class_4597.class_4598 renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, double cameraX, double cameraZ, int width, int height, double screenSizeBasedScale, double scale, double playerDimDiv, double mouseX, double mouseZ, float brightness, boolean cave, HoveredMapElementHolder<?, ?> oldHovered, class_310 mc, float partialTicks) {
      MapProcessor mapProcessor = WorldMapSession.getCurrentSession().getMapProcessor();
      MapDimension mapDimension = mapProcessor.getMapWorld().getCurrentDimension();
      double mapDimScale = mapDimension.calculateDimScale(mapProcessor.getWorldDimensionTypeRegistry());
      class_4587 matrixStack = this.guiGraphics.pose();
      class_1060 textureManager = mc.method_1531();
      class_327 fontRenderer = mc.field_1772;
      class_1044 guiTextures = textureManager.method_4619(WorldMap.guiTextures);
      double baseScale = (double)1.0F / scale;
      Collections.sort(this.renderers);
      if (this.previousHovered == null) {
         this.previousHovered = oldHovered;
      }

      this.workingHovered = null;
      this.workingHoveredRenderer = null;
      this.previousHoveredPresent = false;
      ElementRenderInfo renderInfo = new ElementRenderInfo(this.location, mc.method_1560(), mc.field_1724, new class_243(cameraX, (double)-1.0F, cameraZ), mouseX, mouseZ, scale, cave, partialTicks, brightness, screenSizeBasedScale, (class_276)null, mapDimScale, mapDimension.getDimId());
      matrixStack.method_22903();
      matrixStack.method_22905((float)baseScale, (float)baseScale, 1.0F);

      for(ElementRenderer<?, ?, ?> renderer : this.renderers) {
         this.renderWithRenderer(renderer, renderInfo, renderTypeBuffers, rendererProvider, width, height, baseScale, playerDimDiv, true, 0, 0);
      }

      if (this.previousHoveredPresent) {
         this.renderHoveredWithRenderer(this.previousHovered, renderTypeBuffers, rendererProvider, renderInfo, baseScale, playerDimDiv, true, 0, 0);
      }

      this.previousHoveredPresent = false;
      int indexLimit = 19490;

      for(ElementRenderer<?, ?, ?> renderer : this.renderers) {
         int elementIndex = 0;
         elementIndex = this.renderWithRenderer(renderer, renderInfo, renderTypeBuffers, rendererProvider, width, height, baseScale, playerDimDiv, false, elementIndex, indexLimit);
         matrixStack.method_22904((double)0.0F, (double)0.0F, this.getElementIndexDepth(elementIndex, indexLimit));
         indexLimit -= elementIndex;
         if (indexLimit < 0) {
            indexLimit = 0;
         }
      }

      if (this.previousHoveredPresent) {
         this.renderHoveredWithRenderer(this.previousHovered, renderTypeBuffers, rendererProvider, renderInfo, baseScale, playerDimDiv, false, 0, indexLimit);
      }

      matrixStack.method_22909();
      this.previousHovered = this.previousHovered != null && this.previousHovered.is(this.workingHovered) ? this.previousHovered : (this.workingHovered == null ? null : createResult(this.workingHovered, this.workingHoveredRenderer));
      return this.previousHovered;
   }

   private <E, C> int renderHoveredWithRenderer(HoveredMapElementHolder<E, C> hoveredHolder, class_4597.class_4598 renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, ElementRenderInfo renderInfo, double baseScale, double playerDimDiv, boolean pre, int elementIndex, int indexLimit) {
      ElementRenderer<E, C, ?> renderer = hoveredHolder.getRenderer();
      if (!renderer.shouldRenderHovered(pre)) {
         return elementIndex;
      } else {
         class_4587 matrixStack = this.guiGraphics.pose();
         ElementReader<E, C, ?> reader = renderer.getReader();
         E hoveredCast = hoveredHolder.getElement();
         renderer.preRender(renderInfo, renderTypeBuffers, rendererProvider, pre);
         matrixStack.method_22903();
         if (!pre) {
            matrixStack.method_46416(0.0F, 0.0F, 1.0F);
         }

         double rendererDimDiv = renderer.shouldBeDimScaled() ? playerDimDiv : (double)1.0F;
         this.renderingHovered = true;
         if (!reader.isHidden(hoveredCast, renderer.getContext()) && this.transformAndRenderElement(renderer, hoveredCast, true, renderInfo, renderTypeBuffers, rendererProvider, baseScale, rendererDimDiv, pre, elementIndex, indexLimit) && !pre) {
            ++elementIndex;
         }

         this.renderingHovered = false;
         matrixStack.method_22909();
         renderer.postRender(renderInfo, renderTypeBuffers, rendererProvider, pre);
         return elementIndex;
      }
   }

   private <E, C, R extends ElementRenderer<E, C, R>> int renderWithRenderer(ElementRenderer<E, C, R> renderer, ElementRenderInfo renderInfo, class_4597.class_4598 renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, int width, int height, double baseScale, double playerDimDiv, boolean pre, int elementIndex, int indexLimit) {
      ElementRenderLocation location = this.location;
      if (!renderer.shouldRender(location, pre)) {
         return elementIndex;
      } else {
         ElementReader<E, C, R> reader = renderer.getReader();
         ElementRenderProvider<E, C> provider = renderer.getProvider();
         C context = renderer.getContext();
         double rendererDimDiv = renderer.shouldBeDimScaled() ? playerDimDiv : (double)1.0F;
         renderer.preRender(renderInfo, renderTypeBuffers, rendererProvider, pre);
         provider.begin(location, context);

         while(provider.hasNext(location, context)) {
            E e = provider.setupContextAndGetNext(location, context);
            if (e != null && !reader.isHidden(e, context) && reader.isOnScreen(e, renderInfo.renderPos.field_1352, renderInfo.renderPos.field_1350, width, height, renderInfo.scale, renderInfo.screenSizeBasedScale, rendererDimDiv, context, renderInfo.partialTicks) && this.transformAndRenderElement(renderer, e, false, renderInfo, renderTypeBuffers, rendererProvider, baseScale, rendererDimDiv, pre, elementIndex, indexLimit) && !pre) {
               ++elementIndex;
            }
         }

         provider.end(location, context);
         renderer.postRender(renderInfo, renderTypeBuffers, rendererProvider, pre);
         return elementIndex;
      }
   }

   private <E, C, R extends ElementRenderer<E, C, R>> boolean transformAndRenderElement(ElementRenderer<E, C, R> renderer, E e, boolean highlighted, ElementRenderInfo renderInfo, class_4597.class_4598 renderTypeBuffers, MultiTextureRenderTypeRendererProvider rendererProvider, double baseScale, double rendererDimDiv, boolean pre, int elementIndex, int indexLimit) {
      class_4587 matrixStack = this.guiGraphics.pose();
      ElementReader<E, C, R> reader = renderer.getReader();
      C context = renderer.getContext();
      if (!this.renderingHovered) {
         if (reader.isInteractable(renderInfo.location, e) && reader.isHoveredOnMap(this.location, e, renderInfo.mouseX, renderInfo.mouseZ, renderInfo.scale, renderInfo.screenSizeBasedScale, rendererDimDiv, context, renderInfo.partialTicks)) {
            this.workingHovered = e;
            this.workingHoveredRenderer = renderer;
         }

         if (!this.previousHoveredPresent && this.previousHovered != null && this.previousHovered.is(e)) {
            this.previousHoveredPresent = true;
            return false;
         }
      }

      matrixStack.method_22903();
      double offX = (reader.getRenderX(e, context, renderInfo.partialTicks) / rendererDimDiv - renderInfo.renderPos.field_1352) / baseScale;
      double offZ = (reader.getRenderZ(e, context, renderInfo.partialTicks) / rendererDimDiv - renderInfo.renderPos.field_1350) / baseScale;
      long roundedOffX = Math.round(offX);
      long roundedOffZ = Math.round(offZ);
      double partialX = offX - (double)roundedOffX;
      double partialY = offZ - (double)roundedOffZ;
      matrixStack.method_46416((float)roundedOffX, (float)roundedOffZ, 0.0F);
      boolean result = false;
      if (pre) {
         renderer.renderElementShadow(e, highlighted, (float)renderInfo.screenSizeBasedScale, partialX, partialY, renderInfo, this.guiGraphics, renderTypeBuffers, rendererProvider);
      } else {
         double optionalDepth = this.getElementIndexDepth(elementIndex, indexLimit);
         result = renderer.renderElement(e, highlighted, optionalDepth, (float)renderInfo.screenSizeBasedScale, partialX, partialY, renderInfo, this.guiGraphics, renderTypeBuffers, rendererProvider);
      }

      matrixStack.method_22909();
      return result;
   }

   private double getElementIndexDepth(int elementIndex, int indexLimit) {
      return (double)(elementIndex >= indexLimit ? indexLimit : elementIndex) * 0.1;
   }

   public static final class Builder {
      private class_4587 poseStack;

      private Builder() {
      }

      public Builder setDefault() {
         this.setPoseStack((class_4587)null);
         return this;
      }

      public Builder setPoseStack(class_4587 poseStack) {
         this.poseStack = poseStack;
         return this;
      }

      public MapElementRenderHandler build() {
         if (this.poseStack == null) {
            throw new IllegalStateException();
         } else {
            List<ElementRenderer<?, ?, ?>> renderers = new ArrayList();
            if (SupportMods.minimap()) {
               renderers.add(SupportMods.xaeroMinimap.getWaypointRenderer());
            }

            renderers.add(WorldMap.trackedPlayerRenderer);
            if (SupportMods.pac()) {
               renderers.add(SupportMods.xaeroPac.getCaimResultElementRenderer());
            }

            MapElementGraphics graphics = new MapElementGraphics(this.poseStack, () -> class_310.method_1551().method_22940().method_23000());
            return new MapElementRenderHandler(renderers, ElementRenderLocation.WORLD_MAP, graphics);
         }
      }

      public static Builder begin() {
         return new Builder();
      }
   }
}
