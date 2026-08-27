package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.util.AutoUpdateChecker;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void chuckpack$updateSplash(CallbackInfo ci) {
        try {
            if (AutoUpdateChecker.splashStatus != null && !AutoUpdateChecker.splashStatus.isEmpty()) {
                // Use reflection to avoid @Shadow field not found on 26.1.2 (no refMap)
                TitleScreen self = (TitleScreen) (Object) this;
                try {
                    java.lang.reflect.Field f = TitleScreen.class.getDeclaredField("splash");
                    f.setAccessible(true);
                    f.set(self, AutoUpdateChecker.splashStatus);
                } catch (Throwable ignored) {
                    // Fallback: try obfuscated field name or just log
                    try {
                        for (java.lang.reflect.Field ff : TitleScreen.class.getDeclaredFields()) {
                            if (ff.getType() == String.class) {
                                ff.setAccessible(true);
                                Object val = ff.get(self);
                                if (val instanceof String s && s.length() < 100) {
                                    ff.set(self, AutoUpdateChecker.splashStatus);
                                    break;
                                }
                            }
                        }
                    } catch (Throwable ignored2) {}
                }
            }
        } catch (Throwable ignored) {}
    }
    // Top-right notification will be added via separate overlay (splash covers center) — kept simple to avoid GuiGraphics signature mismatch on 26.1.2
}
