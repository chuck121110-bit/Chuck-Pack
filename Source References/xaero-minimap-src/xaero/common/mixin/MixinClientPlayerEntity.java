package xaero.common.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_746.class})
public class MixinClientPlayerEntity {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_7331()V"}
   )
   public void onTickStart(CallbackInfo info) {
      XaeroMinimapCore.beforeRespawn((class_1657)this);
   }
}
