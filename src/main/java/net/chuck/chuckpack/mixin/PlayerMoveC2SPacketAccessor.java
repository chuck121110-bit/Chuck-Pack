package net.chuck.chuckpack.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Mutable
    @Accessor("y")
    void chuckpack\$setY(double value);

    @Mutable
    @Accessor("onGround")
    void chuckpack\$setOnGround(boolean value);
}
