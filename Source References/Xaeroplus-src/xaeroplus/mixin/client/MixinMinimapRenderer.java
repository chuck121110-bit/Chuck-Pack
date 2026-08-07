package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.hud.minimap.Minimap;
import xaeroplus.Globals;
import xaeroplus.feature.extensions.CustomMinimapFBORenderer;
import xaeroplus.settings.Settings;
import xaeroplus.util.GuiMapHelper;

@Mixin(
   value = {MinimapRenderer.class},
   remap = false
)
public class MixinMinimapRenderer {
   @Shadow
   protected Minimap minimap;
   @Final
   @Shadow
   protected class_4587 matrixStack;

   @Inject(
      method = {"renderMinimap"},
      at = {@At("HEAD")}
   )
   public void resetFBOSize(CallbackInfo ci, @Local(argsOnly = true) MinimapProcessor minimap) {
      if (this.minimap.usingFBO() && Globals.shouldResetFBO) {
         Globals.minimapScaleMultiplier = Settings.REGISTRY.minimapScaleMultiplierSetting.getAsInt();
         Globals.minimapSizeMultiplier = Settings.REGISTRY.minimapSizeMultiplierSetting.getAsInt();
         ((CustomMinimapFBORenderer)this.minimap.getMinimapFBORenderer()).reloadMapFrameBuffers();
         Globals.shouldResetFBO = false;
         minimap.setToResetImage(true);
      }

   }

   @Inject(
      method = {"renderMinimap"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void cancelMinimapRenderWhileInTransparentWorldmap(final CallbackInfo ci) {
      if (GuiMapHelper.isGuiMapLoaded()) {
         ci.cancel();
      }

   }

   @ModifyExpressionValue(
      method = {"renderMinimap"},
      at = {@At(
   value = "CONSTANT",
   args = {"intValue=256"}
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lxaero/common/minimap/render/MinimapRenderer;renderChunks(Lxaero/hud/minimap/module/MinimapSession;Lxaero/common/minimap/MinimapProcessor;Lnet/minecraft/class_243;Lnet/minecraft/class_5321;DIIFFIZZIDDZZLxaero/common/settings/ModSettings;Lxaero/lib/client/graphics/XaeroBufferProvider;)V"
)
)}
   )
   public int modifyMinimapSizeConstantI(final int constant) {
      return this.minimap.usingFBO() ? constant * Globals.minimapSizeMultiplier : constant;
   }

   @ModifyExpressionValue(
      method = {"renderMinimap"},
      at = {@At(
   value = "CONSTANT",
   args = {"floatValue=256.0"},
   ordinal = 0
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lxaero/common/minimap/render/MinimapRenderer;renderChunks(Lxaero/hud/minimap/module/MinimapSession;Lxaero/common/minimap/MinimapProcessor;Lnet/minecraft/class_243;Lnet/minecraft/class_5321;DIIFFIZZIDDZZLxaero/common/settings/ModSettings;Lxaero/lib/client/graphics/XaeroBufferProvider;)V"
)
)}
   )
   public float modifyMinimapSizeConstantF(final float constant) {
      return this.minimap.usingFBO() ? constant * (float)Globals.minimapSizeMultiplier : constant;
   }

   @ModifyExpressionValue(
      method = {"renderMinimap"},
      at = {@At(
   value = "CONSTANT",
   args = {"floatValue=256.0"},
   ordinal = 1
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lxaero/common/minimap/render/MinimapRenderer;renderChunks(Lxaero/hud/minimap/module/MinimapSession;Lxaero/common/minimap/MinimapProcessor;Lnet/minecraft/class_243;Lnet/minecraft/class_5321;DIIFFIZZIDDZZLxaero/common/settings/ModSettings;Lxaero/lib/client/graphics/XaeroBufferProvider;)V"
)
)}
   )
   public float modifyMinimapSizeConstantFCircle(final float constant) {
      return this.minimap.usingFBO() ? constant * (float)Globals.minimapSizeMultiplier : constant;
   }

   @ModifyArg(
      method = {"renderMinimap"},
      at = @At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/element/render/over/MinimapElementOverMapRendererHandler;prepareRender(DDDIIIIZF)V"
),
      index = 2
   )
   public double setOvermapRendererZoom(double zoom) {
      return this.minimap.usingFBO() ? zoom / (double)Globals.minimapScaleMultiplier * (double)Globals.minimapSizeMultiplier : zoom;
   }
}
