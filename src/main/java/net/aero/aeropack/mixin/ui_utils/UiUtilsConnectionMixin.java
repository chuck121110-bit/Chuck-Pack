package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import io.netty.channel.ChannelFutureListener;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;

@Mixin(Connection.class)
public class UiUtilsConnectionMixin {

    @Inject(at = @At("HEAD"),
        method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V",
        cancellable = true)
    private void onSend(Packet<?> packet,
        @Nullable ChannelFutureListener callback, CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        boolean isUiPacket = packet instanceof ServerboundContainerClickPacket
            || packet instanceof ServerboundContainerButtonClickPacket;
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
            && packet instanceof ServerboundSignUpdatePacket) {
            UiUtilsState.shouldEditSign = true;
            ci.cancel();
        }
    }
}
