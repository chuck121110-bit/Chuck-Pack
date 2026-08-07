package xaeroplus.mixin.client.mc;

import net.minecraft.class_11239;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.minimap.render.MinimapPipRenderer;
import xaeroplus.XaeroPlus;
import xaeroplus.event.MinimapRenderEvent;

@Mixin({class_11239.class})
public class MixinPictureInPictureRenderer {
   @Inject(
      method = {"method_72113"},
      at = {@At("HEAD")},
      cancellable = true
   )
   protected void textureIsReadyToBlit(final CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof MinimapPipRenderer) {
         MinimapRenderEvent event = new MinimapRenderEvent();
         XaeroPlus.EVENT_BUS.call(event);
         if (event.cancelled) {
            cir.setReturnValue(true);
         }

      }
   }
}
