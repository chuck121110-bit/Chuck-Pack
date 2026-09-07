package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Unique
    private boolean chuckpack$isNoGround() {
        return chuckpack$noGroundMode != null && chuckpack$noGroundMode.get() == NoGroundMode.NoGround;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chuckpack$addNoGroundSetting(CallbackInfo ci) {
        chuckpack$noGroundMode = sgGeneral.add(new EnumSetting.Builder<NoGroundMode>()
            .name("no-ground-mode")
            .description("Standalone NoGround mode, 1:1 port of Meteor Plus No_Ground. When selected it REPLACES the Mode above entirely: move packets reporting onGround get flipped to false and nothing else runs.")
            .defaultValue(NoGroundMode.Disabled)
            .build()
        );
    }

    // Standalone mode: meteor's own NoFall logic (Packet/AirPlace/Place) is fully suppressed
    // while NoGround is selected, so NoGround runs alone — exactly like Meteor Plus, where
    // No_Ground is its own mode with no other logic attached.
    @Inject(method = "onActivate", at = @At("HEAD"), cancellable = true)
    private void chuckpack$suppressActivate(CallbackInfo ci) {
        if (chuckpack$isNoGround()) ci.cancel();
    }

    @Inject(method = "onDeactivate", at = @At("HEAD"), cancellable = true)
    private void chuckpack$suppressDeactivate(CallbackInfo ci) {
        if (chuckpack$isNoGround()) ci.cancel();
    }

    @Inject(method = "onSendPacket", at = @At("HEAD"), cancellable = true)
    private void chuckpack$noGroundSend(PacketEvent.Send event, CallbackInfo ci) {
        if (!chuckpack$isNoGround()) return;
        // 1:1 Meteor Plus No_Ground: only flip packets that report onGround, nothing else.
        if (event.packet instanceof IServerboundMovePlayerPacket
            && ((PlayerMoveC2SPacketAccessor) event.packet).chuckpack$getOnGround()) {
            ((PlayerMoveC2SPacketAccessor) event.packet).chuckpack$setOnGround(false);
        }
        ci.cancel();
    }

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void chuckpack$suppressTick(TickEvent.Pre event, CallbackInfo ci) {
        if (chuckpack$isNoGround()) ci.cancel();
    }

    @Inject(method = "getInfoString", at = @At("HEAD"), cancellable = true)
    private void chuckpack$infoString(CallbackInfoReturnable<String> cir) {
        if (chuckpack$isNoGround()) cir.setReturnValue("No Ground");
    }
}
