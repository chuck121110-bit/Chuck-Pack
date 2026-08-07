package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.lib.client.graphics.shader.LibShaders;

@Mixin(
   value = {LibShaders.class},
   remap = false
)
public class MixinLibShaders {
   @ModifyExpressionValue(
      method = {"<clinit>"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_2960;method_60655(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/class_2960;"
)}
   )
   private static class_2960 editShader(final class_2960 original) {
      return original.method_12832().equals("core/map") ? class_2960.method_60655("xaeroplus", "custom_map") : original;
   }
}
