package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.radar.render.element.RadarRenderContext;
import xaero.hud.minimap.radar.render.element.RadarRenderer;
import xaeroplus.Globals;
import xaeroplus.settings.Settings;

@Mixin(
   value = {RadarRenderer.class},
   remap = false
)
public abstract class MixinRadarRenderer extends MinimapElementRenderer<class_1297, RadarRenderContext> {
   @Shadow
   private boolean name;

   public MixinRadarRenderer(final MinimapElementReader<class_1297, RadarRenderContext> elementReader, final MinimapElementRenderProvider<class_1297, RadarRenderContext> provider, final RadarRenderContext context) {
      super(elementReader, provider, context);
   }

   @Inject(
      method = {"setupRenderForEntity"},
      at = {@At("RETURN")},
      remap = true
   )
   public void forceEntityRadarRenderSettings(final class_1297 e, final CallbackInfo ci) {
      if (e instanceof class_1657) {
         if (e != class_310.method_1551().field_1724) {
            if (Settings.REGISTRY.alwaysRenderPlayerIconOnRadar.get()) {
               ((RadarRenderContext)this.context).icon = true;
            }

            if (Settings.REGISTRY.alwaysRenderPlayerWithNameOnRadar.get()) {
               this.name = true;
            }

         }
      }
   }

   @Inject(
      method = {"renderElement(Lnet/minecraft/class_1297;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lxaero/hud/minimap/element/render/MinimapElementGraphics;Lnet/minecraft/class_4597$class_4598;)Z"},
      at = {@At("HEAD")},
      remap = true
   )
   public void adjustElementScaleForMinimapScaling(final CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<MinimapElementRenderInfo> renderInfoRef, @Local(argsOnly = true) LocalFloatRef optionalScaleRef) {
      if (((MinimapElementRenderInfo)renderInfoRef.get()).location == MinimapElementRenderLocation.IN_MINIMAP) {
         optionalScaleRef.set(optionalScaleRef.get() * (float)Globals.minimapScaleMultiplier / (float)Globals.minimapSizeMultiplier);
      }

   }
}
