package xaero.map.mods.minimap.element;

import java.util.function.Supplier;
import net.minecraft.class_4597;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaero.map.element.MapElementGraphics;
import xaero.map.element.render.ElementRenderInfo;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.mods.SupportMods;

public final class MinimapElementRendererWrapper<E, C> extends ElementRenderer<E, C, MinimapElementRendererWrapper<E, C>> {
   private final int order;
   private final IXaeroMinimap modMain;
   private final MinimapElementRenderer<E, C> renderer;
   private final Supplier<Boolean> shouldRenderSupplier;
   private MinimapElementRenderInfo convertedRenderInfo;

   private MinimapElementRendererWrapper(IXaeroMinimap modMain, C context, MinimapElementRenderProviderWrapper<E, C> provider, MinimapElementReaderWrapper<E, C> reader, MinimapElementRenderer<E, C> renderer, Supplier<Boolean> shouldRenderSupplier, int order) {
      super(context, provider, reader);
      this.order = order;
      this.renderer = renderer;
      this.modMain = modMain;
      this.shouldRenderSupplier = shouldRenderSupplier;
   }

   public static MinimapElementRenderLocation getRenderLocation(ElementRenderLocation worldMapLocation) {
      if (worldMapLocation == ElementRenderLocation.IN_MINIMAP) {
         return MinimapElementRenderLocation.IN_MINIMAP;
      } else if (worldMapLocation == ElementRenderLocation.OVER_MINIMAP) {
         return MinimapElementRenderLocation.OVER_MINIMAP;
      } else if (worldMapLocation == ElementRenderLocation.IN_WORLD) {
         return MinimapElementRenderLocation.IN_WORLD;
      } else if (worldMapLocation == ElementRenderLocation.WORLD_MAP) {
         return MinimapElementRenderLocation.WORLD_MAP;
      } else {
         return worldMapLocation == ElementRenderLocation.WORLD_MAP_MENU ? MinimapElementRenderLocation.WORLD_MAP_MENU : MinimapElementRenderLocation.UNKNOWN;
      }
   }

   private MinimapElementRenderInfo convertRenderInfo(ElementRenderInfo renderInfo) {
      return new MinimapElementRenderInfo(getRenderLocation(renderInfo.location), renderInfo.renderEntity, renderInfo.player, renderInfo.renderPos, renderInfo.cave, renderInfo.partialTicks, renderInfo.framebuffer, renderInfo.backgroundCoordinateScale, renderInfo.mapDimension);
   }

   public void preRender(ElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider minimapMultiTextureRender = minimapSession.getMultiTextureRenderTypeRenderers();
      this.convertedRenderInfo = this.convertRenderInfo(renderInfo);
      this.renderer.preRender(this.convertedRenderInfo, vanillaBufferSource, minimapMultiTextureRender);
   }

   public void postRender(ElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider, boolean shadow) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider minimapMultiTextureRender = minimapSession.getMultiTextureRenderTypeRenderers();
      this.renderer.postRender(this.convertedRenderInfo, vanillaBufferSource, minimapMultiTextureRender);
      this.convertedRenderInfo = null;
   }

   public boolean renderElement(E element, boolean hovered, double optionalDepth, float optionalScale, double partialX, double partialY, ElementRenderInfo renderInfo, MapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
      return this.renderer.renderElement(element, hovered, false, optionalDepth, optionalScale, partialX, partialY, this.convertedRenderInfo, SupportMods.xaeroMinimap.wrapElementGraphics(guiGraphics), vanillaBufferSource);
   }

   public void renderElementShadow(E element, boolean hovered, float optionalScale, double partialX, double partialY, ElementRenderInfo renderInfo, MapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
   }

   public boolean shouldRender(ElementRenderLocation location, boolean shadow) {
      return !shadow && (Boolean)this.shouldRenderSupplier.get() && this.renderer.shouldRender(getRenderLocation(location));
   }

   public int getOrder() {
      return this.order;
   }

   public static final class Builder<E, C> {
      private final MinimapElementRenderer<E, C> renderer;
      private Supplier<Boolean> shouldRenderSupplier;
      private IXaeroMinimap modMain;
      private int order;

      private Builder(MinimapElementRenderer<E, C> renderer) {
         this.renderer = renderer;
      }

      private Builder<E, C> setDefault() {
         this.setModMain((IXaeroMinimap)null);
         this.setShouldRenderSupplier(() -> true);
         this.setOrder(0);
         return this;
      }

      public Builder<E, C> setModMain(IXaeroMinimap modMain) {
         this.modMain = modMain;
         return this;
      }

      public Builder<E, C> setShouldRenderSupplier(Supplier<Boolean> shouldRenderSupplier) {
         this.shouldRenderSupplier = shouldRenderSupplier;
         return this;
      }

      public Builder<E, C> setOrder(int order) {
         this.order = order;
         return this;
      }

      public MinimapElementRendererWrapper<E, C> build() {
         if (this.modMain != null && this.shouldRenderSupplier != null) {
            MinimapElementRenderProviderWrapper<E, C> providerWrapper = new MinimapElementRenderProviderWrapper<E, C>(this.renderer.getProvider());
            MinimapElementReaderWrapper<E, C> readerWrapper = new MinimapElementReaderWrapper<E, C>(this.renderer.getElementReader());
            C context = (C)this.renderer.getContext();
            return new MinimapElementRendererWrapper<E, C>(this.modMain, context, providerWrapper, readerWrapper, this.renderer, this.shouldRenderSupplier, this.order);
         } else {
            throw new IllegalStateException();
         }
      }

      public static <E, C> Builder<E, C> begin(MinimapElementRenderer<E, C> renderer) {
         return (new Builder<E, C>(renderer)).setDefault();
      }
   }
}
