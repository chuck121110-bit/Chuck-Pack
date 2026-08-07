package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookViewScreen.class)
public abstract class UiUtilsBookScreenMixin extends Screen {

    @Unique
    private EditBox uiUtilsChatField;

    private UiUtilsBookScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        Minecraft mc = Minecraft.getInstance();
        int spacing = 4;
        int buttonHeight = 20;
        int buttonCount = UiUtils.getUiWidgetRows();
        int chatHeight = 20;
        int blockHeight = buttonCount * buttonHeight
            + (buttonCount - 1) * spacing + spacing + chatHeight;
        int startY = Math.max(5, (this.height - blockHeight) / 2);
        int baseX = 8;
        int nextY = UiUtils.addUiWidgets(mc, baseX, startY, spacing,
            this::addRenderableWidget);
        uiUtilsChatField =
            UiUtils.createChatField(mc, this.font, baseX, nextY + spacing);
        addRenderableWidget(uiUtilsChatField);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        UiUtils.refreshLabels();
    }
}
