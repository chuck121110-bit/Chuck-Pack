package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.util.AutoUpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    @Inject(method = "render", at = @At("TAIL"))
    private void chuckpack$renderNotification(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            String status = AutoUpdateChecker.splashStatus;
            if (status == null || status.isEmpty()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.font == null) return;
            TitleScreen self = (TitleScreen) (Object) this;
            // Top-right corner notification, with background
            int textWidth = mc.font.width(status);
            int x = self.width - textWidth - 12;
            int y = 12;
            // Background quad for readability
            graphics.fill(x - 4, y - 4, x + textWidth + 4, y + mc.font.lineHeight + 4, 0x80000000);
            graphics.drawString(mc.font, status, x, y, 0xFFFFFF, true);
        } catch (Throwable ignored) {}
    }
}
