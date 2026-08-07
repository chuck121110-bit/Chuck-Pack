package xaero.common.mixin;

import net.minecraft.class_1059;
import net.minecraft.class_7766;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_1059.class})
public class MixinFabricSpriteAtlasTexture {
   @Inject(
      at = {@At("RETURN")},
      method = {"method_45848(Lnet/minecraft/class_7766$class_7767;)V"}
   )
   public void onUpload(class_7766.class_7767 spriteAtlasTexture$Data_1, CallbackInfo info) {
      if (XaeroMinimapCore.isModLoaded()) {
         HudMod.INSTANCE.getEvents().handleTextureStitchEventPost((class_1059)this);
      }
   }
}
