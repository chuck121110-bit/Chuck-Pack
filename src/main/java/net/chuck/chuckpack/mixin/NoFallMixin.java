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

    // User's maxFallHeightNoWater before anything clobbers it. Meteor sets
    // 159159 while NoFall is active and restores only its own default after,
    // which wipes custom values (and a mid-stream switch to NoGround cancels
    // the restore entirely, sticking at 159159).
    @Unique
    private static Integer chuckpack$fallSnapshot = null;

    @Unique
    private static int chuckpack$liveFallHeight() {
        return baritone.api.BaritoneAPI.getSettings().maxFallHeightNoWater.value;
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void chuckpack$setFallHeight(int target) {
        try {
            for (meteordevelopment.meteorclient.settings.SettingGroup group : meteordevelopment.meteorclient.pathing.PathManagers.get().getSettings().get().groups) {
                boolean done = false;
                for (meteordevelopment.meteorclient.settings.Setting<?> w : group) {
                    if (w.name.equals("maxFallHeightNoWater") && w.get() instanceof Integer) {
                        ((meteordevelopment.meteorclient.settings.Setting<Integer>) (Object) w).set(target);
                        done = true;
                        break;
                    }
                }
                if (done) break;
            }
        } catch (Throwable ignored) {}
        try {
            ((baritone.api.Settings.Setting<Integer>) (Object) baritone.api.BaritoneAPI.getSettings().maxFallHeightNoWater).value = target;
        } catch (Throwable ignored) {}
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
        if (chuckpack$isNoGround()) {
            // Pure NoGround cycle: meteor must not touch anything. Heal a
            // leaked 159159 first if we know the real value, then cancel.
            try {
                if (chuckpack$liveFallHeight() == 159159 && chuckpack$fallSnapshot != null) {
                    chuckpack$setFallHeight(chuckpack$fallSnapshot);
                }
            } catch (Throwable ignored) {}
            chuckpack$fallSnapshot = null;
            ci.cancel();
            return;
        }
        chuckpack$fallSnapshot = null;
        try {
            chuckpack$fallSnapshot = chuckpack$liveFallHeight();
        } catch (Throwable ignored) {}
    }

    @Inject(method = "onDeactivate", at = @At("HEAD"), cancellable = true)
    private void chuckpack$suppressDeactivate(CallbackInfo ci) {
        if (!chuckpack$isNoGround()) return;
        // NoGround: meteor's body would restore a stale pre value, and a
        // mid-stream switch here leaves 159159 stuck. Fix it ourselves.
        try {
            if (chuckpack$liveFallHeight() == 159159) {
                Integer snap = chuckpack$fallSnapshot;
                if (snap != null) {
                    chuckpack$setFallHeight(snap);
                } else {
                    try {
                        chuckpack$setFallHeight(baritone.api.BaritoneAPI.getSettings().maxFallHeightNoWater.defaultValue);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        chuckpack$fallSnapshot = null;
        ci.cancel();
    }

    @Inject(method = "onDeactivate", at = @At("TAIL"))
    private void chuckpack$restoreFallHeight(CallbackInfo ci) {
        if (chuckpack$isNoGround()) return;
        Integer snap = chuckpack$fallSnapshot;
        chuckpack$fallSnapshot = null;
        if (snap == null) return;
        try {
            baritone.api.Settings bs = baritone.api.BaritoneAPI.getSettings();
            int cur = bs.maxFallHeightNoWater.value;
            int def = bs.maxFallHeightNoWater.defaultValue;
            // Still meteor's mark (or its default restore) -> give the
            // pre-activate value back. User-edited mid-session -> keep it.
            if (cur == 159159 || cur == def) {
                chuckpack$setFallHeight(snap);
            }
        } catch (Throwable ignored) {}
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
