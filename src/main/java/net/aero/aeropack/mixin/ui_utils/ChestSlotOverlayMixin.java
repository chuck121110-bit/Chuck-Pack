package net.aero.aeropack.mixin.ui_utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.aero.aeropack.modules.misc.UiUtilsMod;
import net.aero.aeropack.uiutils.UiUtilsModAccess;
import net.aero.aeropack.uiutils.UiUtilsState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class ChestSlotOverlayMixin {

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Shadow
    public abstract AbstractContainerMenu getMenu();

    @Inject(
        method = "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
        at = @At("TAIL"))
    private void aeropack$renderSlotOverlay(GuiGraphicsExtractor graphics,
        int mouseX, int mouseY, float delta, CallbackInfo ci) {
        UiUtilsMod hack = UiUtilsModAccess.get();
        boolean drawSlotNumbers = UiUtilsState.isUiEnabled() && hack != null
            && hack.isSlotOverlayEnabled();

        if (!drawSlotNumbers)
            return;

        AbstractContainerMenu AbstractContainerMenu = getMenu();
        if (AbstractContainerMenu == null)
            return;

        int totalSlots = AbstractContainerMenu.slots.size();
        int color = hack.getSlotOverlayColorI();
        int offsetX = hack.getSlotOverlayOffsetX();
        int offsetY = hack.getSlotOverlayOffsetY();
        boolean hoverOnly = hack.isSlotOverlayHoverOnly();

        for (int index = 0; index < totalSlots; index++) {
            Slot slot = AbstractContainerMenu.slots.get(index);
            if (slot == null)
                continue;

            if (hoverOnly) {
                int sx = leftPos + slot.x;
                int sy = topPos + slot.y;
                if (!(mouseX >= sx && mouseX < sx + 16 && mouseY >= sy
                    && mouseY < sy + 16))
                    continue;
            }
            int textX = leftPos + slot.x + offsetX;
            int textY = topPos + slot.y + offsetY;
            String label = String.valueOf(index);
            graphics.text(net.minecraft.client.Minecraft.getInstance().font, label, textX, textY, color);
        }
    }
}
