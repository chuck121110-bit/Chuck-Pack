package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.movement.Tunnel;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ClientPacketListener.class)
public class TunnelChatMixin {

    private static final Pattern TUNNEL_PATTERN = Pattern.compile("^\\.tunnel\\s+(\\d+)x(\\d+)$", Pattern.CASE_INSENSITIVE);

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (message == null) return;

        Matcher matcher = TUNNEL_PATTERN.matcher(message.trim());
        if (matcher.matches()) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));

            if (width != height) {
                // Allow non-square tunnels, use the larger dimension
                int size = Math.max(width, height);
                startTunnel(size, ci);
            } else {
                startTunnel(width, ci);
            }
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String command, CallbackInfo ci) {
        if (command == null) return;

        // Handle /tunnel NxN as well
        Matcher matcher = TUNNEL_PATTERN.matcher("/" + command.trim());
        if (matcher.matches()) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            int size = Math.max(width, height);
            startTunnel(size, ci);
        }
    }

    private void startTunnel(int size, CallbackInfo ci) {
        Tunnel tunnel = Modules.get().get(Tunnel.class);
        if (tunnel != null) {
            if (!tunnel.isActive()) {
                tunnel.startTunnel(size);
            } else {
                tunnel.error("Tunnel is already active. Toggle it off first.");
            }
            ci.cancel();
        }
    }
}
