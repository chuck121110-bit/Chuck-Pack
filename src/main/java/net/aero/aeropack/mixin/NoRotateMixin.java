package net.aero.aeropack.mixin;

import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class NoRotateMixin {
    @Unique
    private float aeropack$savedYaw;

    @Unique
    private float aeropack$savedPitch;

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void aeropack$onPlayerPositionLookHead(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        AutoFly af = Modules.get().get(AutoFly.class);
        if (af == null || !af.isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        this.aeropack$savedYaw = mc.player.getYaw();
        this.aeropack$savedPitch = mc.player.getPitch();
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void aeropack$onPlayerPositionLookReturn(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        AutoFly af = Modules.get().get(AutoFly.class);
        if (af == null || !af.isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.setYaw(this.aeropack$savedYaw + 1.0E-6F);
        mc.player.setPitch(this.aeropack$savedPitch + 1.0E-6F);
    }
}
