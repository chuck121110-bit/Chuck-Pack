package xaeroplus.mixin.client.mc;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_287;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaeroplus.Globals;

@Mixin({class_287.class})
public class MixinBufferBuilder {
   @ModifyExpressionValue(
      method = {"method_60805"},
      at = {@At(
   value = "CONSTANT",
   args = {"intValue=16777215"}
)}
   )
   public int bypassVertexCountLimit(final int original) {
      return Globals.bypassVertexCountLimit ? Integer.MAX_VALUE : original;
   }
}
