package net.aero.aeropack.mixin;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import net.aero.aeropack.modules.movement.AutoFly;
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
    private static final Minecraft aeropack$mc = Minecraft.getInstance();

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
    }

    @Unique
    private static boolean aeropack$isAutoFlyActive() {
        AutoFly af = Modules.get().get(AutoFly.class);
        return af != null && af.isActive();
    }

    @Inject(method = "onPreTick", at = @At("HEAD"), cancellable = true)
    private void aeropack$cancelPreTick(TickEvent.Pre event, CallbackInfo ci) {
        if (aeropack$mc.player == null || aeropack$isAutoFlyActive()) ci.cancel();
    }

    @Inject(method = "onPostTick", at = @At("HEAD"), cancellable = true)
    private void aeropack$cancelPostTick(TickEvent.Post event, CallbackInfo ci) {
        if (aeropack$mc.player == null || aeropack$isAutoFlyActive()) ci.cancel();
    }

    @Inject(method = "onDeactivate", at = @At("HEAD"), cancellable = true)
    private void aeropack$nullCheckDeactivate(CallbackInfo ci) {
        if (aeropack$mc.player == null) ci.cancel();
    }
}
