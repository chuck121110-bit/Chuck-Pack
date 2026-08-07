package xaero.common.mixin;

import net.minecraft.class_32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_32.class_5143.class})
public class MixinLevelStorageAccess {
   @Inject(
      at = {@At("RETURN")},
      method = {"method_27015()V"},
      cancellable = false
   )
   public void onDeleteLevel(CallbackInfo info) {
      XaeroMinimapCore.onDeleteWorld((class_32.class_5143)this);
   }
}
