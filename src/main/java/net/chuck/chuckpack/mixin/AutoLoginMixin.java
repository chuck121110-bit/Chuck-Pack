package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.misc.AutoLogin;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class AutoLoginMixin {

    @Inject(method = "sendChat", at = @At("HEAD"))
    private void onSendChatMessage(String message, CallbackInfo ci) {
        AutoLogin.onChatSent(message);
        chuckpack$handleSwarmStop(message);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    private void onSendChatCommand(String command, CallbackInfo ci) {
        AutoLogin.onChatSent("/" + command);
        chuckpack$handleSwarmStop("/" + command);
        chuckpack$handleSwarmStop("#" + command);
        chuckpack$handleSwarmStop("." + command);
    }

    private static void chuckpack$handleSwarmStop(String msg) {
        if (msg == null) return;
        String lower = msg.toLowerCase().trim();
        String[] prefixes = {".swarm stop", "/swarm stop", "#swarm stop", ";swarm stop", "swarm stop", ".swarm halt", "/swarm halt", "#swarm halt", "swarm halt", ".swarm cancel", "/swarm cancel", "swarm cancel"};
        for (String p : prefixes) {
            if (lower.equals(p) || lower.startsWith(p + " ")) {
                try {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc != null) {
                        mc.execute(() -> {
                            try {
                                var af = meteordevelopment.meteorclient.systems.modules.Modules.get().get(net.chuck.chuckpack.modules.movement.AutoFly.class);
                                if (af != null && af.isActive()) af.toggle();
                                try { meteordevelopment.meteorclient.pathing.PathManagers.get().stop(); } catch (Throwable ignored) {}
                                try { baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null); } catch (Throwable ignored) {}
                                try { baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything(); } catch (Throwable ignored) {}
                            } catch (Throwable ignored) {}
                        });
                    }
                } catch (Throwable ignored) {}
                break;
            }
        }
    }
}

