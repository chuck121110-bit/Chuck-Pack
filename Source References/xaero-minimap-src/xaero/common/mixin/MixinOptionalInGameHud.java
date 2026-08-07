package xaero.common.mixin;

import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_329.class})
public class MixinOptionalInGameHud {
   @Inject(
      at = {@At("RETURN")},
      method = {"method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}
   )
   public void onRenderEnd(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo info) {
      XaeroMinimapCore.afterIngameGuiRender(guiGraphics, deltaTracker);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_1736(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      cancellable = true
   )
   public void onRenderCrosshair(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo info) {
      if (XaeroMinimapCore.onRenderCrosshair(guiGraphics)) {
         info.cancel();
      }

   }

   @Inject(
      at = {@At("RETURN")},
      method = {"method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}
   )
   public void postRenderStatusEffectOverlay(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo info) {
      XaeroMinimapCore.onRenderStatusEffectOverlayPost(guiGraphics);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_1753(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}
   )
   public void onRenderStart(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo info) {
      XaeroMinimapCore.beforeIngameGuiRender(guiGraphics, deltaTracker);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      cancellable = true
   )
   public void onRenderStatusEffectOverlay(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo info) {
      if (XaeroMinimapCore.onRenderStatusEffectOverlay(guiGraphics)) {
         info.cancel();
      }

   }
}
