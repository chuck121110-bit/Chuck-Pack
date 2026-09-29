package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import net.chuck.chuckpack.modules.movement.AutoFly;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Flight.class)
public class FlightMixin {
    @Shadow @Final private SettingGroup sgGeneral;

    @Unique
    private static final Minecraft chuckpack$mc = Minecraft.getInstance();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        sgGeneral.add(new BoolSetting.Builder()
            .name("scroll-speed")
            .description("Hold Shift and scroll to change flight speed.")
            .defaultValue(true)
            .build()
        );

        sgGeneral.add(new DoubleSetting.Builder()
            .name("scroll-sensitivity")
            .description("How much the flight speed changes per scroll tick.")
            .defaultValue(0.01)
            .min(0.001)
            .sliderMax(0.1)
            .build()
        );

        sgGeneral.add(new BoolSetting.Builder()
            .name("no-sprint")
            .description("Disable sprint while flying to save hunger.")
            .defaultValue(true)
            .build()
        );
    }

    @Unique
    private static boolean chuckpack$isAutoFlyActive() {
        AutoFly af = Modules.get().get(AutoFly.class);
        return af != null && af.isActive();
    }

    @Inject(method = "onPreTick", at = @At("HEAD"), cancellable = true)
    private void chuckpack$cancelPreTick(TickEvent.Pre event, CallbackInfo ci) {
        if (chuckpack$mc.player == null || chuckpack$isAutoFlyActive()) ci.cancel();
    }

    @Inject(method = "onPostTick", at = @At("HEAD"), cancellable = true)
    private void chuckpack$cancelPostTick(TickEvent.Post event, CallbackInfo ci) {
        if (chuckpack$mc.player == null || chuckpack$isAutoFlyActive()) ci.cancel();
    }

    @Inject(method = "onDeactivate", at = @At("HEAD"), cancellable = true)
    private void chuckpack$nullCheckDeactivate(CallbackInfo ci) {
        if (chuckpack$mc.player == null) ci.cancel();
    }

    @Inject(method = "onPreTick", at = @At("HEAD"))
    private void chuckpack$noSprint(TickEvent.Pre event, CallbackInfo ci) {
        Flight self = (Flight)(Object)this;
        if ((!self.isActive() && !chuckpack$isAutoFlyActive()) || chuckpack$mc.player == null) return;
        try {
            var s = self.settings.get("no-sprint");
            if (s instanceof BoolSetting bs && bs.get()) {
                chuckpack$mc.player.setSprinting(false);
                chuckpack$mc.options.keySprint.setDown(false);
            }
        } catch (Throwable ignored) {}
    }
}
