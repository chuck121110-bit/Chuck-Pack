package net.chuck.chuckpack.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    @Accessor("connection")
    net.minecraft.client.multiplayer.ClientPacketListener chuckpack$getConnection();
}
