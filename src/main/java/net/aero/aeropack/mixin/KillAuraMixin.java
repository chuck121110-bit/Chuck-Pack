package net.aero.aeropack.mixin;

import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KillAura.class, remap = false)
public abstract class KillAuraMixin {
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void aeropack$nullCheck(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            ci.cancel();
        }
    }
}
