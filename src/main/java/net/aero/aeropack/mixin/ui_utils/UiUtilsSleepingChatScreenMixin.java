package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SleepingChatScreen.class)
public abstract class UiUtilsSleepingChatScreenMixin extends Screen {

    private UiUtilsSleepingChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        int baseX = 8;
        int startY = Math.max(5, (this.height - 20) / 2);
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Client wake up"), b -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) {
                    mc.player.wakeUp();
                    mc.setScreen(null);
                }
            }).dimensions(baseX, startY, 115, 20).build());
    }
}
