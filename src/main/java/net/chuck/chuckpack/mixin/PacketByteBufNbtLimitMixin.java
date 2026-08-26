package net.chuck.chuckpack.mixin;

import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtAccounter.class)
public class PacketByteBufNbtLimitMixin {
    @Inject(method = "defaultQuota", at = @At("HEAD"), cancellable = true)
    private static void aero$raiseNbtLimit(CallbackInfoReturnable<NbtAccounter> cir) {
        cir.setReturnValue(NbtAccounter.unlimitedHeap());
    }
}
