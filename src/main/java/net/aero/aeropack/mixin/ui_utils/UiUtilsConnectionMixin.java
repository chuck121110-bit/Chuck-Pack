package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import io.netty.channel.ChannelFutureListener;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

@Mixin(ClientConnection.class)
public class UiUtilsConnectionMixin {

    @Inject(at = @At("HEAD"),
        method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
        cancellable = true)
    private void onSend(Packet<?> packet,
        @Nullable ChannelFutureListener callback, CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        boolean isUiPacket = packet instanceof ClickSlotC2SPacket
            || packet instanceof ButtonClickC2SPacket;
        if (isUiPacket) {
            UiUtils.chatIfEnabled(
                "Sending UI packet: " + packet.getClass().getSimpleName());
        }

        if (!UiUtilsState.sendUiPackets && isUiPacket) {
            UiUtils.chatIfEnabled("Canceled UI packet (sendUiPackets=false)");
            ci.cancel();
            return;
        }

        if (UiUtilsState.delayUiPackets && isUiPacket) {
            UiUtilsState.delayedUiPackets.add(packet);
            UiUtils.chatIfEnabled("Delayed UI packet (queued "
                + UiUtilsState.delayedUiPackets.size() + ")");
            ci.cancel();
            return;
        }

        if (!UiUtilsState.shouldEditSign
            && packet instanceof UpdateSignC2SPacket) {
            UiUtilsState.shouldEditSign = true;
            ci.cancel();
        }
    }
}
