package net.aero.aeropack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class AutoResourcePackMixin {

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void aeropack$autoAcceptResourcePack(Screen screen, CallbackInfo ci) {
        if (screen == null) return;
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> aeropack$tryAccept(mc));
        mc.execute(() -> mc.execute(() -> aeropack$tryAccept(mc)));
    }

    @Unique
    private static void aeropack$tryAccept(Minecraft mc) {
        if (mc.currentScreen == null) return;

        if (mc.currentScreen instanceof ConfirmScreen confirmScreen) {
            ConfirmScreenAccessor accessor = (ConfirmScreenAccessor) confirmScreen;
            var yesButton = accessor.aeropack$getYesButton();
            if (yesButton != null && yesButton.active) {
                double cx = yesButton.getX() + yesButton.getWidth() / 2.0;
                double cy = yesButton.getY() + yesButton.getHeight() / 2.0;
                yesButton.mouseClicked(new Click(cx, cy, new MouseInput(0, 0)), false);
            }
        }
    }
}
