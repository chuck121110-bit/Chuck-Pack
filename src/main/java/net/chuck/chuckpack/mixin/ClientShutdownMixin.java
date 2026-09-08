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
        // Backstop: lingering non-daemon pools (baritone cache etc.) can stall
        // JVM exit past Mojang's 15s shutdown watchdog. All saves are done by
        // now, so force out — unless a crash screen is showing, so real crash
        // reports stay readable.
        try {
            Thread backstop = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                    return;
                }
                try {
                    net.minecraft.client.gui.screens.Screen screen = Minecraft.getInstance().gui.screen();
                    if (screen != null && screen.getClass().getName().toLowerCase(java.util.Locale.ROOT).contains("crash")) return;
                } catch (Throwable ignored) {}
                System.exit(0);
            }, "ChuckPack-ShutdownBackstop");
            backstop.setDaemon(true);
            backstop.start();
        } catch (Throwable ignored) {}
    }
}
