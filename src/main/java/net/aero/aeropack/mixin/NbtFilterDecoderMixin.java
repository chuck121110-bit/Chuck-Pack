package net.aero.aeropack.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.aero.aeropack.modules.misc.NbtFilter;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.ConnectionProtocol;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CompressionDecoder.class)
public abstract class NbtFilterDecoderMixin {
    @Shadow
    @Final
    private ConnectionProtocol state;

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private void aero$dropOversizedChunkPacket(ChannelHandlerContext context,
        ByteBuf buf, List<Object> out, CallbackInfo ci) {
        if (!NbtFilter.shouldDropRawClientboundPacket(state, buf))
            return;

        buf.skipBytes(buf.readableBytes());
        ci.cancel();
    }
}
