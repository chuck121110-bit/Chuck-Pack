package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class UiUtilsSignEditScreenMixin extends Screen {

    private UiUtilsSignEditScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        Minecraft mc = Minecraft.getInstance();
        int spacing = 4;
        int buttonHeight = 20;
        int totalHeight = buttonHeight * 2 + spacing;
        int startY = Math.max(5, (this.height - totalHeight) / 2);
        int baseX = 8;
        addRenderableWidget(
            Button.builder(Component.literal("Close without packet"), b -> {
                UiUtilsState.shouldEditSign = false;
                mc.setScreen(null);
            }).pos(baseX, startY).size(115, 20).build());

        addRenderableWidget(
            Button.builder(Component.literal("Disconnect"), b -> {
                if (mc.getConnection() != null)
                    mc.getConnection().getConnection().disconnect(
                        Component.literal("Disconnecting (UI-UTILS)"));
            }).pos(baseX, startY + buttonHeight + spacing).size(115, 20).build());
    }
}
