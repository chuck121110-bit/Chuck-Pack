package xaero.map.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.core.XaeroWorldMapCore;

@Mixin({class_310.class})
public class MixinForgeMinecraftClient {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_1523(Z)V"}
   )
   public void onRunTickStart(CallbackInfo info) {
      XaeroWorldMapCore.onMinecraftRunTick();
   }

   @ModifyArg(
      method = {"method_1523(Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_757;method_3192(Lnet/minecraft/class_9779;Z)V"
),
      index = 1
   )
   public boolean onRenderCall(boolean renderingInGame) {
      return XaeroWorldMapCore.onRenderCall(renderingInGame);
   }
}
