package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import net.chuck.chuckpack.modules.movement.AutoFly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hard-blocks sprinting while flying (Flight or AutoFly active, no-sprint on).
 * Cancelling setSprinting(true) at the source beats every sprint mode
 * (Sprint module Always/Rage, sprint key, double-tap) no matter what
 * re-enables it later in the tick.
 */
@Mixin(value = LocalPlayer.class, remap = false)
public abstract class NoSprintWhileFlyingMixin {
    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true)
    private void chuckpack$blockSprintWhileFlying(boolean sprinting, CallbackInfo ci) {
        if (!sprinting) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player != (Object) this) return;

        AutoFly af = Modules.get().get(AutoFly.class);
        if (af == null || !af.isActive()) {
            Flight flight = Modules.get().get(Flight.class);
            if (flight == null || !flight.isActive()) return;
        }

        try {
            Flight flight = Modules.get().get(Flight.class);
            if (flight == null) return;
            Setting<?> s = flight.settings.get("no-sprint");
            if (s instanceof BoolSetting bs && bs.get()) {
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
