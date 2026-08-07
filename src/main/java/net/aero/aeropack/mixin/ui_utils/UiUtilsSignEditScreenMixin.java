package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class UiUtilsSignEditScreenMixin extends Screen {

    private UiUtilsSignEditScreenMixin(Text title) {
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
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Close without packet"), b -> {
                UiUtilsState.shouldEditSign = false;
                mc.setScreen(null);
            }).dimensions(baseX, startY, 115, 20).build());

        addDrawableChild(
            ButtonWidget.builder(Text.literal("Disconnect"), b -> {
                if (mc.getConnection() != null)
                    mc.getConnection().getConnection().disconnect(
                        Text.literal("Disconnecting (UI-UTILS)"));
            }).dimensions(baseX, startY + buttonHeight + spacing, 115, 20).build());
    }
}
