package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.pathing.PathManagers;
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

import static meteordevelopment.meteorclient.MeteorClient.mc;

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
            .description("NoGround mode from Meteor Editions (PR #6516). Never reports onGround to prevent fall damage - superior to Packet as it doesn't rob mace smash. Same logic as MeteorPlus No_Ground.")
            .defaultValue(NoGroundMode.Disabled)
            .build()
        );
    }

    @Inject(method = "onActivate", at = @At("TAIL"))
    private void chuckpack$onActivateNoGround(CallbackInfo ci) {
        if (chuckpack$noGroundMode != null && chuckpack$noGroundMode.get() == NoGroundMode.NoGround) {
            try {
                PathManagers.get().getSettings().getNoFall().set(true);
            } catch (Throwable ignored) {}
        }
    }

    @Inject(method = "onDeactivate", at = @At("HEAD"))
    private void chuckpack$onDeactivateNoGround(CallbackInfo ci) {
        if (chuckpack$noGroundMode != null && chuckpack$noGroundMode.get() == NoGroundMode.NoGround) {
            try {
                // Stop damage when disabling module - same as Meteor Editions PR
                if (mc.player != null && mc.player.connection != null) {
                    sendNoGroundPacket(0.0000008);
                    sendNoGroundPacket(0);
                }
            } catch (Throwable ignored) {}
            try {
                // PathManagers restore is handled by original NoFall onDeactivate, but ensure Baritone state
            } catch (Throwable ignored) {}
        }
    }

    @Unique
    private void sendNoGroundPacket(double height) {
        if (mc.player == null || mc.player.connection == null) return;
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        ServerboundMovePlayerPacket packet = new ServerboundMovePlayerPacket.Pos(x, y + height, z, false, false);
        ((IServerboundMovePlayerPacket) packet).meteor$setTag(1337);
        mc.player.connection.send(packet);
    }

    @Inject(method = "onSendPacket", at = @At("HEAD"))
    private void chuckpack$onSendPacketNoGround(PacketEvent.Send event, CallbackInfo ci) {
        if (chuckpack$noGroundMode == null || chuckpack$noGroundMode.get() != NoGroundMode.NoGround) return;
        if (!(event.packet instanceof ServerboundMovePlayerPacket)) return;
        // Avoid handling our own deactivate packets (tag 1337) - same as Meteor Editions
        if (event.packet instanceof IServerboundMovePlayerPacket tagged && tagged.meteor$getTag() == 1337) return;
        // Never report a landing - same as Meteor Editions PR: unconditional false
        ((PlayerMoveC2SPacketAccessor) event.packet).chuckpack$setOnGround(false);
    }
}
