package net.chuck.chuckpack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class AutoResourcePackMixin {

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void chuckpack$autoAcceptResourcePack(Screen screen, CallbackInfo ci) {
        if (screen == null) return;
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> chuckpack$tryAccept(mc));
        mc.execute(() -> mc.execute(() -> chuckpack$tryAccept(mc)));
    }

    @Unique
    private static void chuckpack$tryAccept(Minecraft mc) {
        if (mc.screen == null) return;

        if (mc.screen instanceof ConfirmScreen confirmScreen) {
            ConfirmScreenAccessor accessor = (ConfirmScreenAccessor) confirmScreen;
            var yesButton = accessor.chuckpack$getYesButton();
            if (yesButton != null && yesButton.active) {
                double cx = yesButton.getX() + yesButton.getWidth() / 2.0;
                double cy = yesButton.getY() + yesButton.getHeight() / 2.0;
                yesButton.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(cx, cy, new net.minecraft.client.input.MouseButtonInfo(0, 0)), false);
            }
        }
    }
}
