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
    @Shadow private Setting<NoFall.Mode> mode;

    // Single merged Mode list: meteor's Packet/AirPlace/Place plus NoGround as its own
    // standalone mode (1:1 port of Meteor Plus No_Ground). Names match meteor's so configs keep working.
    @Unique
    public enum Mode {
        Packet,
        AirPlace,
        Place,
        NoGround
    }

    @Unique
    private Setting<Mode> chuckpack$mode;

    @Unique
    private boolean chuckpack$isNoGround() {
        return chuckpack$mode != null && chuckpack$mode.get() == Mode.NoGround;
    }

    // Push our selection into meteor's hidden mode so its sub-settings visibility
    // (placed-item, air-place-mode, anchor) keeps working. NoGround maps to Packet
    // but meteor's logic is fully suppressed while selected, so it never runs.
    @Unique
    private void chuckpack$pushMode() {
        if (chuckpack$mode == null || mode == null) return;
        switch (chuckpack$mode.get()) {
            case Packet -> mode.set(NoFall.Mode.Packet);
            case AirPlace -> mode.set(NoFall.Mode.AirPlace);
            case Place -> mode.set(NoFall.Mode.Place);
            case NoGround -> mode.set(NoFall.Mode.Packet);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chuckpack$mergeModeSetting(CallbackInfo ci) {
        // Hide meteor's Mode dropdown and move it off the "mode" config key so ours owns it.
        ((SettingMutatorAccessor) (Object) mode).chuckpack$setName("mode-base");
        ((SettingMutatorAccessor) (Object) mode).chuckpack$setVisible(() -> false);

        chuckpack$mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("The way you are saved from fall damage. NoGround is a 1:1 port of Meteor Plus No_Ground: always spoofs onGround false, nothing else runs.")
            .defaultValue(Mode.Packet)
            .onChanged(v -> chuckpack$pushMode())
            .onModuleActivated(v -> chuckpack$pushMode())
            .build()
        );

        // Adopt meteor's loaded value so existing configs keep their mode.
        try {
            switch (mode.get()) {
                case AirPlace -> chuckpack$mode.set(Mode.AirPlace);
                case Place -> chuckpack$mode.set(Mode.Place);
                default -> chuckpack$mode.set(Mode.Packet);
            }
        } catch (Throwable ignored) {}
        chuckpack$pushMode();
    }

    // Standalone mode: meteor's own NoFall logic is fully suppressed while NoGround
    // is selected, so NoGround runs alone — like Meteor Plus, where No_Ground is its
    // own mode with no other logic attached.
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
        // ALWAYS spoof no-ground, no matter what: every move packet goes out onGround=false.
        if (event.packet instanceof IServerboundMovePlayerPacket) {
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
        if (chuckpack$isNoGround()) cir.setReturnValue("NoGround");
    }
}
