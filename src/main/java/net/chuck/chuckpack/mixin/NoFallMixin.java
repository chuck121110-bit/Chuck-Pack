package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
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
    public enum NoGroundMode {
        Disabled,
        NoGround
    }

    @Unique
    private Setting<NoGroundMode> chuckpack$noGroundMode;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chuckpack$addNoGroundSetting(CallbackInfo ci) {
        chuckpack$noGroundMode = sgGeneral.add(new EnumSetting.Builder<NoGroundMode>()
            .name("no-ground-mode")
            .description("No-Ground mode from Meteor Plus (spoof onGround=false on movement packets). When NoGround, never reports onGround to prevent fall damage. Disabled = use vanilla NoFall modes only.")
            .defaultValue(NoGroundMode.Disabled)
            .build()
        );
    }

    @Inject(method = "onSendPacket", at = @At("HEAD"))
    private void chuckpack$onSendPacketNoGround(PacketEvent.Send event, CallbackInfo ci) {
        if (chuckpack$noGroundMode == null || chuckpack$noGroundMode.get() != NoGroundMode.NoGround) return;
        if (event.packet instanceof ServerboundMovePlayerPacket packet) {
            // Exact logic from MeteorPlus No_Ground: only spoof if onGround true
            if (packet.isOnGround()) {
                ((PlayerMoveC2SPacketAccessor) packet).chuckpack$setOnGround(false);
            }
        }
    }
}
