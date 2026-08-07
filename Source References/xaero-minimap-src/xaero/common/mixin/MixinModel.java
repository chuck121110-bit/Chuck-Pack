package xaero.common.mixin;

import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_3879.class})
public class MixinModel {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_62100(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V"}
   )
   public void onRender(class_4587 matrices, class_4588 vertices, int light, int overlay, int color, CallbackInfo info) {
      XaeroMinimapCore.onEntityIconsModelRenderDetection((class_3879)this, vertices, color);
   }
}
