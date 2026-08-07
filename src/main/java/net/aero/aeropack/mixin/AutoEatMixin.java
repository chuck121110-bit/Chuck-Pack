package net.aero.aeropack.mixin;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AutoEat.class)
public class AutoEatMixin {
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void aero$guardNullPlayer(TickEvent.Pre event, CallbackInfo info) {
        if (Minecraft.getInstance().player == null) info.cancel();
    }
}
