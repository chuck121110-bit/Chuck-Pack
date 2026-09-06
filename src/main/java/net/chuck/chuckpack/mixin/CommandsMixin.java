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
                            ChatUtils.infoPrefix("Swarm", "Killed by host â€” closing Minecraft...");
                            try { mc.close(); } catch (Throwable ignored) {}
                            try { System.exit(0); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                });
            }
            ci.cancel();
        }
        // Swarm stop should also stop AutoFly
        if (lower.equals("swarm stop") || lower.equals("swarm halt") || lower.equals("swarm cancel") || lower.startsWith("swarm stop ") || lower.startsWith("swarm halt ") || lower.startsWith("swarm cancel ")) {
            try {
                Swarm swarm = Modules.get().get(Swarm.class);
                if (swarm != null && swarm.isHost() && swarm.host != null) {
                    swarm.host.sendMessage("swarm ChuckPack-stopfly");
                }
            } catch (Throwable ignored) {}
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        var af = Modules.get().get(net.chuck.chuckpack.modules.movement.AutoFly.class);
                        if (af != null && af.isActive()) af.toggle();
                        // Also stop Baritone
                        try { meteordevelopment.meteorclient.pathing.PathManagers.get().stop(); } catch (Throwable ignored) {}
                        try { baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null); } catch (Throwable ignored) {}
                        try { baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything(); } catch (Throwable ignored) {}
                        ChatUtils.infoPrefix("Swarm", "Stopped flying.");
                    } catch (Throwable ignored) {}
                });
            }
            // Don't cancel, let original swarm stop also run
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
                            ChatUtils.infoPrefix("Swarm", "Killed by host â€” closing Minecraft...");
                            try { mc.close(); } catch (Throwable ignored) {}
                            try { System.exit(0); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                });
            }
            ci.cancel();
        }
        // Swarm OreSim simulate - host side: enable locally and broadcast to workers
        if (lower.equals("swarm simulate") || lower.startsWith("swarm simulate ") || lower.equals("swarm oresim") || lower.startsWith("swarm oresim ") || lower.equals("swarm orsim") || lower.startsWith("swarm orsim ")) {
            String args = "";
            try {
                if (lower.startsWith("swarm simulate")) args = message.substring("swarm simulate".length()).trim();
                else if (lower.startsWith("swarm oresim")) args = message.substring("swarm oresim".length()).trim();
                else if (lower.startsWith("swarm orsim")) args = message.substring("swarm orsim".length()).trim();
            } catch (Throwable ignored) {}
            final String fArgs = args;
            // Broadcast to workers
            try {
                Swarm swarm = Modules.get().get(Swarm.class);
                if (swarm != null && swarm.isHost() && swarm.host != null) {
                    String msg = fArgs.isEmpty() ? "swarm ChuckPack-oresim" : "swarm ChuckPack-oresim " + fArgs;
                    swarm.host.sendMessage(msg);
                    ChatUtils.infoPrefix("Swarm", fArgs.isEmpty() ? "Sent OreSim enable to workers" : "Sent OreSim simulate to workers: " + fArgs);
                }
            } catch (Throwable ignored) {}
            // Execute locally
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        String[] split = fArgs.isEmpty() ? new String[0] : fArgs.split("\\s+");
                        net.chuck.chuckpack.modules.world.OreSim.handleSwarmSimulate(split, false);
                    } catch (Throwable t) {
                        ChatUtils.error("OreSim swarm failed: " + t.getMessage());
                    }
                });
            }
            ci.cancel();
        }
        // Swarm seed - host side: set seed locally and broadcast to workers
        if (lower.equals("swarm seed") || lower.startsWith("swarm seed ")) {
            String args = "";
            try {
                args = message.substring("swarm seed".length()).trim();
            } catch (Throwable ignored) {}
            final String fArgs = args;
            // Broadcast to workers
            try {
                Swarm swarm = Modules.get().get(Swarm.class);
                if (swarm != null && swarm.isHost() && swarm.host != null) {
                    String msg = fArgs.isEmpty() ? "swarm ChuckPack-seed" : "swarm ChuckPack-seed " + fArgs;
                    swarm.host.sendMessage(msg);
                    ChatUtils.infoPrefix("Swarm", fArgs.isEmpty() ? "Sent seed request to workers" : "Sent seed to workers: " + fArgs);
                }
            } catch (Throwable ignored) {}
            // Execute locally - use same logic as SeedCommand
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        if (fArgs.isEmpty()) {
                            // Show current seed
                            var seed = net.chuck.chuckpack.util.config.Seeds.get().getSeed();
                            if (seed != null) ChatUtils.info("Seed: " + seed.seed + " (" + seed.version + ")");
                            else ChatUtils.error("No seed for current world");
                        } else {
                            String[] parts = fArgs.split("\\s+");
                            String seedStr = parts[0];
                            String ver = parts.length > 1 ? parts[1] : net.chuck.chuckpack.util.config.Seeds.DEFAULT_VERSION;
                            net.chuck.chuckpack.util.config.Seeds.get().setSeed(seedStr, ver);
                            ChatUtils.info("Seed set to " + seedStr + " (" + ver + ") via swarm");
                        }
                    } catch (Throwable t) {
                        ChatUtils.error("Swarm seed failed: " + t.getMessage());
                    }
                });
            }
            ci.cancel();
        }
        // Direct ChuckPack-oresim/seed from workers (should be handled in SwarmWorkerMixin, but also handle here for host self)
        if (lower.equals("swarm chuckpack-oresim") || lower.startsWith("swarm chuckpack-oresim ")) {
            String args = "";
            try {
                args = message.substring("swarm ChuckPack-oresim".length()).trim();
            } catch (Throwable ignored) {}
            final String fArgs = args;
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        String[] split = fArgs.isEmpty() ? new String[0] : fArgs.split("\\s+");
                        net.chuck.chuckpack.modules.world.OreSim.handleSwarmSimulate(split, true);
                    } catch (Throwable t) {}
                });
            }
            ci.cancel();
        }
        if (lower.equals("swarm chuckpack-seed") || lower.startsWith("swarm chuckpack-seed ")) {
            String args = "";
            try {
                args = message.substring("swarm ChuckPack-seed".length()).trim();
            } catch (Throwable ignored) {}
            final String fArgs = args;
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                mc.execute(() -> {
                    try {
                        if (!fArgs.isEmpty()) {
                            String[] parts = fArgs.split("\\s+");
                            String seedStr = parts[0];
                            String ver = parts.length > 1 ? parts[1] : net.chuck.chuckpack.util.config.Seeds.DEFAULT_VERSION;
                            net.chuck.chuckpack.util.config.Seeds.get().setSeed(seedStr, ver);
                            ChatUtils.info("Seed set via swarm: " + seedStr);
                        }
                    } catch (Throwable ignored) {}
                });
            }
            ci.cancel();
        }
    }
}

