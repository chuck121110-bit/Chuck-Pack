package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public abstract class UiUtilsSleepingChatScreenMixin extends Screen {

    private UiUtilsSleepingChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        int baseX = 8;
        int startY = Math.max(5, (this.height - 20) / 2);
        addRenderableWidget(
            Button.builder(Component.literal("Client wake up"), b -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.stopSleeping();
                    mc.setScreen(null);
                }
            }).pos(baseX, startY).size(115, 20).build());
    }
}
