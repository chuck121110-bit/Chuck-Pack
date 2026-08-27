package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.util.AutoUpdateChecker;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Shadow private String splash;

    @Inject(method = "init", at = @At("TAIL"))
    private void chuckpack$updateSplash(CallbackInfo ci) {
        try {
            if (AutoUpdateChecker.splashStatus != null && !AutoUpdateChecker.splashStatus.isEmpty()) {
                this.splash = AutoUpdateChecker.splashStatus;
            }
        } catch (Throwable ignored) {}
    }
}
