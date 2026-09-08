package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Quitting with swarm host/worker active leaves meteor's socket threads
// blocked forever, hanging JVM shutdown until the watchdog crashes.
// Close swarm connections first so the client can exit cleanly.
@Mixin(Minecraft.class)
public class ClientShutdownMixin {
    @Inject(method = "close", at = @At("HEAD"))
    private void chuckpack$closeSwarmOnShutdown(CallbackInfo ci) {
        try {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm != null) swarm.close();
        } catch (Throwable ignored) {}
    }
}
