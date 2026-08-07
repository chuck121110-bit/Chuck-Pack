package xaero.common.mixin.immediatelyfast;

import net.minecraft.class_1921;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.common.core.IBufferSource;
import xaero.common.core.XaeroMinimapCore;

@Mixin({BatchableBufferSource.class})
public class MixinOptionalBatchableBufferSource implements IBufferSource {
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
