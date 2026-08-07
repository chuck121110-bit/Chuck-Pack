package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.aero.aeropack.modules.misc.UiUtilsMod;
import net.aero.aeropack.uiutils.UiUtilsModAccess;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ChestSlotOverlayMixin {

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int backgroundWidth;

    @Shadow
    protected int backgroundHeight;

    @Shadow
    public abstract ScreenHandler getScreenHandler();

    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
        at = @At("TAIL"))
    private void aeropack$renderSlotOverlay(DrawContext graphics,
        int mouseX, int mouseY, float delta, CallbackInfo ci) {
        UiUtilsMod hack = UiUtilsModAccess.get();
        boolean drawSlotNumbers = UiUtilsState.isUiEnabled() && hack != null
            && hack.isSlotOverlayEnabled();

        if (!drawSlotNumbers)
            return;

        ScreenHandler screenHandler = getScreenHandler();
        if (screenHandler == null)
            return;

        int totalSlots = screenHandler.slots.size();
        int color = hack.getSlotOverlayColorI();
        int offsetX = hack.getSlotOverlayOffsetX();
        int offsetY = hack.getSlotOverlayOffsetY();
        boolean hoverOnly = hack.isSlotOverlayHoverOnly();

        for (int index = 0; index < totalSlots; index++) {
            Slot slot = screenHandler.slots.get(index);
            if (slot == null)
                continue;

            if (hoverOnly) {
                int sx = x + slot.x;
                int sy = y + slot.y;
                if (!(mouseX >= sx && mouseX < sx + 16 && mouseY >= sy
                    && mouseY < sy + 16))
                    continue;
            }
            int textX = x + slot.x + offsetX;
            int textY = y + slot.y + offsetY;
            String label = String.valueOf(index);
            graphics.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, label, textX, textY, color);
        }
    }
}
