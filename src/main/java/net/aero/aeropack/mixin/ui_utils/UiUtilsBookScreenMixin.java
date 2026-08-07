package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookScreen.class)
public abstract class UiUtilsBookScreenMixin extends Screen {

    @Unique
    private TextFieldWidget uiUtilsChatField;

    private UiUtilsBookScreenMixin(Text title) {
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
            this::addDrawableChild);
        uiUtilsChatField =
            UiUtils.createChatField(mc, this.font, baseX, nextY + spacing);
        addDrawableChild(uiUtilsChatField);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        UiUtils.refreshLabels();
    }
}
