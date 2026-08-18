package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class NoRotateMixin {
    @Unique
    private float chuckpack\$savedYaw;

    @Unique
    private float chuckpack\$savedPitch;

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void chuckpack\$onPlayerPositionLookHead(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AutoFly af = Modules.get().get(AutoFly.class);
        if (af == null || !af.isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        this.ChuckPack$savedYaw = mc.player.getYRot();
        this.ChuckPack$savedPitch = mc.player.getXRot();
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void chuckpack\$onPlayerPositionLookReturn(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AutoFly af = Modules.get().get(AutoFly.class);
        if (af == null || !af.isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.setYRot(this.ChuckPack$savedYaw + 1.0E-6F);
        mc.player.setXRot(this.ChuckPack$savedPitch + 1.0E-6F);
    }
}
