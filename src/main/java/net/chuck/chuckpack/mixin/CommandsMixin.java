package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "dispatch", at = @At("HEAD"), cancellable = true)
    private static void chuckpack$onDispatch(String message, CallbackInfo ci) {
        if (message == null) return;
        String lower = message.toLowerCase().trim();
        // Handle swarm kill locally on host (so host can kill all/workers even when laggy via swarm)
        if (lower.equals("swarm kill") || lower.startsWith("swarm kill ")) {
            String target = lower.equals("swarm kill") ? "" : message.substring("swarm kill".length()).trim();
            // Send to workers if host
            try {
                Swarm swarm = Modules.get().get(Swarm.class);
                if (swarm != null && swarm.isHost() && swarm.host != null) {
                    String killCmd = target.isEmpty() ? "swarm ChuckPack-kill" : "swarm ChuckPack-kill " + target;
                    swarm.host.sendMessage(killCmd);
                    ChatUtils.infoPrefix("Swarm", target.isEmpty() ? "Sent kill to all workers" : "Sent kill to " + target);
                }
            } catch (Throwable ignored) {}
            // Kill self if no target or matches self
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        String myName = mc.player != null ? mc.player.getName().getString() : "";
                        if (target.isEmpty() || target.equalsIgnoreCase(myName)) {
                            ChatUtils.infoPrefix("Swarm", "Killed by host — closing Minecraft...");
                            try { mc.close(); } catch (Throwable ignored) {}
                            try { System.exit(0); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                });
            }
            ci.cancel();
        }
        // Also handle ChuckPack-kill for host (in case host receives own broadcast)
        if (lower.equals("swarm chuckpack-kill") || lower.startsWith("swarm chuckpack-kill ")) {
            String target = "";
            try {
                int idx = message.toLowerCase().indexOf("swarm chuckpack-kill");
                target = message.substring(idx + "swarm ChuckPack-kill".length()).trim();
            } catch (Throwable ignored) {}
            final String fTarget = target;
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        String myName = mc.player != null ? mc.player.getName().getString() : "";
                        if (fTarget.isEmpty() || fTarget.equalsIgnoreCase(myName)) {
                            ChatUtils.infoPrefix("Swarm", "Killed by host — closing Minecraft...");
                            try { mc.close(); } catch (Throwable ignored) {}
                            try { System.exit(0); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                });
            }
            ci.cancel();
        }
    }
}
