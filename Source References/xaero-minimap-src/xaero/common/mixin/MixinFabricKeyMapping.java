package xaero.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_304;
import net.minecraft.class_4666;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_304.class})
public class MixinFabricKeyMapping {
   @ModifyReturnValue(
      method = {"method_1434()Z"},
      at = {@At("RETURN")}
   )
   public boolean onIsDown(boolean original) throws Exception {
      if (!(this instanceof class_4666)) {
         return original;
      } else {
         return XaeroMinimapCore.onToggleKeyIsDown((class_4666)this) ? true : original;
      }
   }
}
