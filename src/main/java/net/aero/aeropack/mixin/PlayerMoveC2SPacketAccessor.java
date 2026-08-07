package net.aero.aeropack.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Mutable
    @Accessor("y")
    void aeropack$setY(double value);

    @Mutable
    @Accessor("onGround")
    void aeropack$setOnGround(boolean value);
}
