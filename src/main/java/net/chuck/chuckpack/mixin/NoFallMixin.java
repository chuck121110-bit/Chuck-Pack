package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoFall.class)
public class NoFallMixin {
    @Shadow @Final private SettingGroup sgGeneral;

    @Unique
    private Setting<Boolean> chuckpack$noGroundSpoof;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chuckpack$addNoGroundSetting(CallbackInfo ci) {
        chuckpack$noGroundSpoof = sgGeneral.add(new BoolSetting.Builder()
            .name("no-ground-spoof")
            .description("Spoofs onGround=false on movement packets to prevent fall damage (NoGround mode from meteor-plus). Compatible with all other NoFall modes.")
            .defaultValue(false)
            .build()
        );
    }

    @Inject(method = "onSendPacket", at = @At("HEAD"))
    private void chuckpack$onSendPacketNoGround(PacketEvent.Send event, CallbackInfo ci) {
        if (chuckpack$noGroundSpoof != null && chuckpack$noGroundSpoof.get()) {
            if (event.packet instanceof ServerboundMovePlayerPacket packet) {
                ((PlayerMoveC2SPacketAccessor) packet).chuckpack$setOnGround(false);
            }
        }
    }
}
