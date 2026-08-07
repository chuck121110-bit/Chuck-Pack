package xaero.common.mixin;

import net.minecraft.class_1921;
import net.minecraft.class_4597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.common.core.IBufferSource;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_4597.class_4598.class})
public class MixinBufferSource implements IBufferSource {
   private class_1921 xaero_lastRenderType;

   public class_1921 getXaero_lastRenderType() {
      return this.xaero_lastRenderType;
   }

   public void setXaero_lastRenderType(class_1921 lastRenderType) {
      this.xaero_lastRenderType = lastRenderType;
   }

   @ModifyVariable(
      method = {"method_73477(Lnet/minecraft/class_1921;)Lnet/minecraft/class_4588;"},
      index = 1,
      at = @At("HEAD")
   )
   public class_1921 onGetBuffer(class_1921 argument) {
      XaeroMinimapCore.onBufferSourceGetBuffer(this, argument);
      return argument;
   }
}
