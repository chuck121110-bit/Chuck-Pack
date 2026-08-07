package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_2246;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.region.MapPixel;
import xaeroplus.settings.Settings;

@Mixin(
   value = {MapPixel.class},
   remap = false
)
public abstract class MixinMapPixel {
   @Shadow
   protected class_2680 state;

   @Inject(
      method = {"getPixelColours"},
      at = {@At("RETURN")},
      remap = false
   )
   public void getPixelColours(final CallbackInfo ci, @Local(argsOnly = true) final int[] result_dest) {
      if (Settings.REGISTRY.transparentObsidianRoofSetting.get()) {
         if (this.state.method_26204() != class_2246.field_10540 && this.state.method_26204() != class_2246.field_22423) {
            if (this.state.method_26204() == class_2246.field_10477) {
               result_dest[3] = Settings.REGISTRY.transparentObsidianRoofSnowOpacitySetting.getAsInt();
            }
         } else {
            result_dest[3] = Settings.REGISTRY.transparentObsidianRoofDarkeningSetting.getAsInt();
         }
      }

   }
}
