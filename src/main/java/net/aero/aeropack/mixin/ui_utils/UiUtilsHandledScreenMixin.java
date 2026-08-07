package net.aero.aeropack.mixin.ui_utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.aero.aeropack.modules.misc.UiUtilsMod;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsModAccess;
import net.aero.aeropack.uiutils.UiUtilsState;
import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class UiUtilsHandledScreenMixin extends Screen {

    @Unique
    private EditBox uiUtilsChatField;

    @Unique
    private Button uiUtilsSpamButton;

    @Unique
    private Button uiUtilsQueueButton;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int backgroundWidth;

    @Unique
    private boolean fabricateOverlayInitialized;

    @Unique
    private int fabricateMode = MODE_CLICK_SLOT;

    @Unique
    private Button overlayClickSlotModeButton;

    @Unique
    private Button overlayButtonClickModeButton;

    @Unique
    private EditBox overlayClickSyncIdField;

    @Unique
    private EditBox overlayClickRevisionField;

    @Unique
    private EditBox overlayClickSlotField;

    @Unique
    private EditBox overlayClickButtonField;

    @Unique
    private CycleButton<ClickAction> overlayClickActionButton;

    @Unique
    private CycleButton<Boolean> overlayClickDelayToggle;

    @Unique
    private EditBox overlayClickTimesField;

    @Unique
    private Button overlayClickSendButton;

    @Unique
    private EditBox overlayButtonSyncIdField;

    @Unique
    private EditBox overlayButtonIdField;

    @Unique
    private CycleButton<Boolean> overlayButtonDelayToggle;

    @Unique
    private EditBox overlayButtonTimesField;

    @Unique
    private Button overlayButtonSendButton;

    @Unique
    private static final int OVERLAY_WIDTH = 260;

    @Unique
    private static final int MODE_BUTTON_WIDTH = 110;

    @Unique
    private static final int MODE_BUTTON_GAP = 10;

    @Unique
    private static final int FIELD_WIDTH = 110;

    @Unique
    private static final int ROW_SPACING = 36;

    @Unique
    private static final int LABEL_OFFSET = 10;

    @Unique
    private static final int CONTENT_TOP_OFFSET = 12;

    @Unique
    private static final int OVERLAY_DRAG_BAR_HEIGHT = 10;

    @Unique
    private static final int DELAY_BUTTON_WIDTH = 90;

    @Unique
    private static final int TIMES_FIELD_WIDTH = 50;

    @Unique
    private static final int MODE_CLICK_SLOT = 0;

    @Unique
    private static final int MODE_BUTTON_CLICK = 1;

    @Unique
    private int overlayXPos;

    @Unique
    private int overlayYPos;

    @Unique
    private int overlayBottomY;

    @Unique
    private boolean overlayDragging;

    @Unique
    private int dragOffsetX;

    @Unique
    private int dragOffsetY;

    private UiUtilsHandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        fabricateOverlayInitialized = false;

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

        initFabricateOverlay();
    }

    @Inject(at = @At("TAIL"),
        method = "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partialTicks, CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        UiUtils.refreshLabels();

        AbstractContainerMenu AbstractContainerMenu = ((AbstractContainerScreen<?>) (Object) this).getMenu();
        UiUtils.renderSyncInfo(Minecraft.getInstance(), graphics, AbstractContainerMenu);

        updateOverlayVisibility();

        if (UiUtilsState.fabricateOverlayOpen) {
            layoutFabricateOverlay();
            if (AbstractContainerMenu != null)
                updateOverlaySyncInfo(AbstractContainerMenu);
            drawFabricateLabels(graphics);
            if (overlayDragging) {
                int newX = Mth.clamp(mouseX - dragOffsetX, 0,
                    this.width - OVERLAY_WIDTH);
                int newY = Mth.clamp(mouseY - dragOffsetY, 0,
                    Math.max(0, this.height - 40));
                UiUtilsState.fabricateOverlayX = newX;
                UiUtilsState.fabricateOverlayY = newY;
            }
        }
    }

    @Inject(at = @At("HEAD"),
        method = "render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
    private void aeropack$renderFabricateOverlayBackground(
        GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partialTicks, CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;
        UiUtils.refreshLabels();
        if (!UiUtilsState.fabricateOverlayOpen)
            return;
        layoutFabricateOverlay();
        UiUtilsMod hack = UiUtilsModAccess.get();
        int alpha = hack != null ? hack.getFabricateOverlayBgAlpha() : 120;
        if (alpha <= 0)
            return;
        int x1 = Math.max(0, overlayXPos - 6);
        int y1 = Math.max(0, overlayYPos - 6);
        int x2 = Math.min(this.width, overlayXPos + OVERLAY_WIDTH + 6);
        int y2 = Math.min(this.height, overlayBottomY + 6);
        int color = (alpha << 24);
        graphics.fill(x1, y1, x2, y2, color);
    }

    @Inject(at = @At("HEAD"),
        method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z",
        cancellable = true)
    private void onKeyPressed(KeyEvent keyEvent,
        CallbackInfoReturnable<Boolean> cir) {
        if (!UiUtilsState.isUiEnabled())
            return;

        if (uiUtilsChatField != null && uiUtilsChatField.isFocused()) {
            if (keyEvent.input() == 257) {
                String msg = uiUtilsChatField.getValue().trim();
                Minecraft mc = Minecraft.getInstance();
                if (!msg.isEmpty() && mc.player != null) {
                    mc.player.connection.sendChat(msg);
                }
                uiUtilsChatField.setValue("");
                uiUtilsChatField.setFocused(false);
                cir.setReturnValue(true);
                return;
            }

            if (uiUtilsChatField.keyPressed(keyEvent)) {
                cir.setReturnValue(true);
                return;
            }

            if (keyEvent.input() == GLFW.GLFW_KEY_ESCAPE) {
                uiUtilsChatField.setFocused(false);
                cir.setReturnValue(true);
                return;
            }
        }

        if (UiUtilsState.fabricateOverlayOpen && fabricateOverlayInitialized) {
            EditBox focused = getFocusedOverlayField();
            if (focused != null && focused != uiUtilsChatField) {
                if (focused.keyPressed(keyEvent)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private EditBox getFocusedOverlayField() {
        if (overlayClickSyncIdField != null && overlayClickSyncIdField.isFocused())
            return overlayClickSyncIdField;
        if (overlayClickRevisionField != null && overlayClickRevisionField.isFocused())
            return overlayClickRevisionField;
        if (overlayClickSlotField != null && overlayClickSlotField.isFocused())
            return overlayClickSlotField;
        if (overlayClickButtonField != null && overlayClickButtonField.isFocused())
            return overlayClickButtonField;
        if (overlayClickTimesField != null && overlayClickTimesField.isFocused())
            return overlayClickTimesField;
        if (overlayButtonSyncIdField != null && overlayButtonSyncIdField.isFocused())
            return overlayButtonSyncIdField;
        if (overlayButtonIdField != null && overlayButtonIdField.isFocused())
            return overlayButtonIdField;
        if (overlayButtonTimesField != null && overlayButtonTimesField.isFocused())
            return overlayButtonTimesField;
        return null;
    }

    @Unique
    private void initFabricateOverlay() {
        if (fabricateOverlayInitialized)
            return;

        overlayClickSlotModeButton = Button.builder(
            Component.literal("Click Slot"),
            b -> switchFabricateMode(MODE_CLICK_SLOT))
            .pos(0, 0).size(MODE_BUTTON_WIDTH, 20).build();
        addRenderableWidget(overlayClickSlotModeButton);

        overlayButtonClickModeButton = Button.builder(
            Component.literal("Button Click"),
            b -> switchFabricateMode(MODE_BUTTON_CLICK))
            .pos(0, 0).size(MODE_BUTTON_WIDTH, 20).build();
        addRenderableWidget(overlayButtonClickModeButton);

        overlayClickSyncIdField = new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20,
            Component.literal("Sync Id"));
        addRenderableWidget(overlayClickSyncIdField);

        overlayClickRevisionField = new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20,
            Component.literal("Revision"));
        addRenderableWidget(overlayClickRevisionField);

        overlayClickSlotField =
            new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20, Component.literal("Slot"));
        overlayClickSlotField.setValue("0");
        addRenderableWidget(overlayClickSlotField);

        overlayClickButtonField = new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20,
            Component.literal("Button"));
        overlayClickButtonField.setValue("0");
        addRenderableWidget(overlayClickButtonField);

        overlayClickActionButton = CycleButton
            .<ClickAction>builder(
                action -> Component.literal(action.name()),
                () -> ClickAction.PRIMARY)
            .withValues(ClickAction.values()).create(0, 0, FIELD_WIDTH, 20,
                Component.literal("Action"), (button, value) -> {});
        addRenderableWidget(overlayClickActionButton);

        overlayClickDelayToggle =
            CycleButton.onOffBuilder(false).create(0, 0, DELAY_BUTTON_WIDTH, 20,
                Component.literal("Delay"), (button, value) -> {});
        addRenderableWidget(overlayClickDelayToggle);

        overlayClickTimesField = new EditBox(Minecraft.getInstance().font, 0, 0, TIMES_FIELD_WIDTH, 20,
            Component.literal("Times to send"));
        overlayClickTimesField.setValue("1");
        addRenderableWidget(overlayClickTimesField);

        overlayClickSendButton =
            Button.builder(Component.literal("Send"), b -> sendClickSlot())
                .pos(0, 0).size(90, 20).build();
        addRenderableWidget(overlayClickSendButton);

        overlayButtonSyncIdField = new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20,
            Component.literal("Sync Id"));
        addRenderableWidget(overlayButtonSyncIdField);

        overlayButtonIdField = new EditBox(Minecraft.getInstance().font, 0, 0, FIELD_WIDTH, 20,
            Component.literal("Button Id"));
        overlayButtonIdField.setValue("0");
        addRenderableWidget(overlayButtonIdField);

        overlayButtonDelayToggle =
            CycleButton.onOffBuilder(false).create(0, 0, DELAY_BUTTON_WIDTH, 20,
                Component.literal("Delay"), (button, value) -> {});
        addRenderableWidget(overlayButtonDelayToggle);

        overlayButtonTimesField = new EditBox(Minecraft.getInstance().font, 0, 0, TIMES_FIELD_WIDTH, 20,
            Component.literal("Times to send"));
        overlayButtonTimesField.setValue("1");
        addRenderableWidget(overlayButtonTimesField);

        overlayButtonSendButton =
            Button.builder(Component.literal("Send"), b -> sendButtonClick())
                .pos(0, 0).size(90, 20).build();
        addRenderableWidget(overlayButtonSendButton);

        fabricateOverlayInitialized = true;
        switchFabricateMode(MODE_CLICK_SLOT);
        updateOverlayVisibility();
    }

    @Unique
    private void switchFabricateMode(int mode) {
        fabricateMode = mode;
        overlayClickSlotModeButton.setMessage(Component.literal(
            "Click Slot" + (fabricateMode == MODE_CLICK_SLOT ? " ✓" : "")));
        overlayButtonClickModeButton.setMessage(Component.literal(
            "Button Click" + (fabricateMode == MODE_BUTTON_CLICK ? " ✓" : "")));

        boolean showClick = fabricateMode == MODE_CLICK_SLOT;
        setWidgetVisibleAndActive(overlayClickSyncIdField, showClick);
        setWidgetVisibleAndActive(overlayClickRevisionField, showClick);
        setWidgetVisibleAndActive(overlayClickSlotField, showClick);
        setWidgetVisibleAndActive(overlayClickButtonField, showClick);
        setWidgetVisibleAndActive(overlayClickActionButton, showClick);
        setWidgetVisibleAndActive(overlayClickDelayToggle, showClick);
        setWidgetVisibleAndActive(overlayClickTimesField, showClick);
        setWidgetVisibleAndActive(overlayClickSendButton, showClick);

        boolean showButton = fabricateMode == MODE_BUTTON_CLICK;
        setWidgetVisibleAndActive(overlayButtonSyncIdField, showButton);
        setWidgetVisibleAndActive(overlayButtonIdField, showButton);
        setWidgetVisibleAndActive(overlayButtonDelayToggle, showButton);
        setWidgetVisibleAndActive(overlayButtonTimesField, showButton);
        setWidgetVisibleAndActive(overlayButtonSendButton, showButton);
    }

    @Unique
    private void updateOverlayVisibility() {
        if (!fabricateOverlayInitialized)
            return;

        boolean visible = UiUtilsState.fabricateOverlayOpen;
        setWidgetVisibleAndActive(overlayClickSlotModeButton, visible);
        setWidgetVisibleAndActive(overlayButtonClickModeButton, visible);
        if (!visible) {
            hideAndParkOverlayWidgets();
            return;
        }

        switchFabricateMode(fabricateMode);
    }

    @Unique
    private void setWidgetVisibleAndActive(AbstractWidget widget,
        boolean visible) {
        if (widget == null)
            return;
        widget.visible = visible;
        widget.active = visible;
    }

    @Unique
    private void hideAndParkOverlayWidgets() {
        setWidgetVisibleAndActive(overlayClickSyncIdField, false);
        setWidgetVisibleAndActive(overlayClickRevisionField, false);
        setWidgetVisibleAndActive(overlayClickSlotField, false);
        setWidgetVisibleAndActive(overlayClickButtonField, false);
        setWidgetVisibleAndActive(overlayClickActionButton, false);
        setWidgetVisibleAndActive(overlayClickDelayToggle, false);
        setWidgetVisibleAndActive(overlayClickTimesField, false);
        setWidgetVisibleAndActive(overlayClickSendButton, false);
        setWidgetVisibleAndActive(overlayButtonSyncIdField, false);
        setWidgetVisibleAndActive(overlayButtonIdField, false);
        setWidgetVisibleAndActive(overlayButtonDelayToggle, false);
        setWidgetVisibleAndActive(overlayButtonTimesField, false);
        setWidgetVisibleAndActive(overlayButtonSendButton, false);

        final int offscreen = -2000;
        parkWidget(overlayClickSlotModeButton, offscreen);
        parkWidget(overlayButtonClickModeButton, offscreen);
        parkWidget(overlayClickSyncIdField, offscreen);
        parkWidget(overlayClickRevisionField, offscreen);
        parkWidget(overlayClickSlotField, offscreen);
        parkWidget(overlayClickButtonField, offscreen);
        parkWidget(overlayClickActionButton, offscreen);
        parkWidget(overlayClickDelayToggle, offscreen);
        parkWidget(overlayClickTimesField, offscreen);
        parkWidget(overlayClickSendButton, offscreen);
        parkWidget(overlayButtonSyncIdField, offscreen);
        parkWidget(overlayButtonIdField, offscreen);
        parkWidget(overlayButtonDelayToggle, offscreen);
        parkWidget(overlayButtonTimesField, offscreen);
        parkWidget(overlayButtonSendButton, offscreen);
    }

    @Unique
    private void parkWidget(AbstractWidget widget, int offscreen) {
        if (widget == null)
            return;
        widget.setX(offscreen);
        widget.setY(offscreen);
    }

    @Unique
    private void layoutFabricateOverlay() {
        int overlayX = this.x + this.backgroundWidth + 8;
        int overlayY = this.y;
        if (UiUtilsState.fabricateOverlayX >= 0)
            overlayX = Mth.clamp(UiUtilsState.fabricateOverlayX, 0,
                this.width - OVERLAY_WIDTH);
        if (UiUtilsState.fabricateOverlayY >= 0)
            overlayY = Mth.clamp(UiUtilsState.fabricateOverlayY, 0,
                Math.max(0, this.height - 40));
        if (overlayX + OVERLAY_WIDTH > this.width - 8)
            overlayX = this.width - OVERLAY_WIDTH - 8;
        if (overlayX < 8)
            overlayX = 8;
        int modeGroupWidth = MODE_BUTTON_WIDTH * 2 + MODE_BUTTON_GAP;
        int modeStartX = overlayX + (OVERLAY_WIDTH - modeGroupWidth) / 2;
        int modeY = overlayY + 12;

        overlayClickSlotModeButton.setX(modeStartX);
        overlayClickSlotModeButton.setY(modeY);
        overlayButtonClickModeButton
            .setX(modeStartX + MODE_BUTTON_WIDTH + MODE_BUTTON_GAP);
        overlayButtonClickModeButton.setY(modeY);

        int inputX = overlayX + (OVERLAY_WIDTH - FIELD_WIDTH) / 2;
        int clickY = modeY + 32 + CONTENT_TOP_OFFSET;
        overlayClickSyncIdField.setX(inputX);
        overlayClickSyncIdField.setY(clickY);
        clickY += ROW_SPACING;
        overlayClickRevisionField.setX(inputX);
        overlayClickRevisionField.setY(clickY);
        clickY += ROW_SPACING;
        overlayClickSlotField.setX(inputX);
        overlayClickSlotField.setY(clickY);
        clickY += ROW_SPACING;
        overlayClickButtonField.setX(inputX);
        overlayClickButtonField.setY(clickY);
        clickY += ROW_SPACING;
        overlayClickActionButton.setX(inputX);
        overlayClickActionButton.setY(clickY);
        clickY += ROW_SPACING;
        int delayTotal = DELAY_BUTTON_WIDTH + 8 + TIMES_FIELD_WIDTH;
        int delayX = overlayX + (OVERLAY_WIDTH - delayTotal) / 2;
        overlayClickDelayToggle.setX(delayX);
        overlayClickDelayToggle.setY(clickY);
        overlayClickTimesField.setX(delayX + DELAY_BUTTON_WIDTH + 8);
        overlayClickTimesField.setY(clickY);
        clickY += ROW_SPACING;
        overlayClickSendButton.setX(overlayX + (OVERLAY_WIDTH - 90) / 2);
        overlayClickSendButton.setY(clickY);

        int buttonY = modeY + 32 + CONTENT_TOP_OFFSET;
        overlayButtonSyncIdField.setX(inputX);
        overlayButtonSyncIdField.setY(buttonY);
        buttonY += ROW_SPACING;
        overlayButtonIdField.setX(inputX);
        overlayButtonIdField.setY(buttonY);
        buttonY += ROW_SPACING;
        overlayButtonDelayToggle.setX(delayX);
        overlayButtonDelayToggle.setY(buttonY);
        overlayButtonTimesField.setX(delayX + DELAY_BUTTON_WIDTH + 8);
        overlayButtonTimesField.setY(buttonY);
        buttonY += ROW_SPACING;
        overlayButtonSendButton.setX(overlayX + (OVERLAY_WIDTH - 90) / 2);
        overlayButtonSendButton.setY(buttonY);

        this.overlayXPos = overlayX;
        this.overlayYPos = overlayY;
        this.overlayBottomY = (fabricateMode == MODE_CLICK_SLOT) ? (clickY + 20 + 12)
            : (buttonY + 20 + 12);
    }

    @Unique
    private void drawFabricateLabels(GuiGraphicsExtractor graphics) {
        if (fabricateMode == MODE_CLICK_SLOT) {
            drawLabel(graphics, "Sync Id", overlayClickSyncIdField);
            drawLabel(graphics, "Revision", overlayClickRevisionField);
            drawLabel(graphics, "Slot", overlayClickSlotField);
            drawLabel(graphics, "Button", overlayClickButtonField);
        } else {
            drawLabel(graphics, "Sync Id", overlayButtonSyncIdField);
            drawLabel(graphics, "Button Id", overlayButtonIdField);
        }
    }

    @Unique
    private void drawLabel(GuiGraphicsExtractor graphics, String Component,
        EditBox field) {
        graphics.text(this.font, Component, field.getX(), field.getY() - LABEL_OFFSET,
            0xFFAAAAAA);
    }

    @Inject(at = @At("HEAD"),
        method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
        cancellable = true)
    private void aeropack$fabricateOverlayMouseClicked(
        net.minecraft.client.input.MouseButtonEvent context,
        boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!UiUtilsState.isUiEnabled() || !UiUtilsState.fabricateOverlayOpen)
            return;
        double mx = context.x();
        double my = context.y();
        int btn = context.button();
        if (btn != 0)
            return;
        int dragBarTop = overlayYPos;
        int dragBarBottom = overlayYPos + OVERLAY_DRAG_BAR_HEIGHT;
        if (mx >= overlayXPos && mx <= overlayXPos + OVERLAY_WIDTH
            && my >= dragBarTop && my <= dragBarBottom) {
            overlayDragging = true;
            dragOffsetX = (int) Math.round(mx - overlayXPos);
            dragOffsetY = (int) Math.round(my - overlayYPos);
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"),
        method = "mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z",
        cancellable = true)
    private void aeropack$fabricateOverlayMouseReleased(
        net.minecraft.client.input.MouseButtonEvent context,
        CallbackInfoReturnable<Boolean> cir) {
        if (!overlayDragging)
            return;
        overlayDragging = false;
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Unique
    private void updateOverlaySyncInfo(AbstractContainerMenu menu) {
        if (menu == null)
            return;

        if (!overlayClickSyncIdField.isFocused())
            overlayClickSyncIdField.setValue(String.valueOf(menu.containerId));
        if (!overlayClickRevisionField.isFocused())
            overlayClickRevisionField
                .setValue(String.valueOf(menu.getStateId()));
        if (!overlayButtonSyncIdField.isFocused())
            overlayButtonSyncIdField.setValue(String.valueOf(menu.containerId));
    }

    @Unique
    private void sendClickSlot() {
        // TODO: Port sendClickSlot to 26.1.2 API — ServerboundContainerClickPacket constructor changed
        // (now takes ContainerInput, HashedStack instead of ClickAction, Component).
        // Component.fromItemStack and ComponentChangesHash.ComponentHasher no longer exist.
        UiUtils.chatIfEnabled("ClickSlot: not yet ported to MC 26.1.2");
    }

    @Unique
    private void sendButtonClick() {
        if (!UiUtils.isInteger(overlayButtonSyncIdField.getValue())
            || !UiUtils.isInteger(overlayButtonIdField.getValue())
            || !UiUtils.isInteger(overlayButtonTimesField.getValue())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int syncId = Integer.parseInt(overlayButtonSyncIdField.getValue());
        int buttonId = Integer.parseInt(overlayButtonIdField.getValue());
        int timesToSend = Integer.parseInt(overlayButtonTimesField.getValue());
        if (timesToSend < 1)
            return;

        ServerboundContainerButtonClickPacket packet =
            new ServerboundContainerButtonClickPacket(syncId, buttonId);

        UiUtils.chatIfEnabled(
            "ButtonClick: buttonId=" + buttonId + ", times=" + timesToSend);

        Runnable toRun = UiUtils.getFabricatePacketRunnable(mc,
            overlayButtonDelayToggle.getValue(), packet);
        for (int i = 0; i < timesToSend; i++)
            toRun.run();
    }

    @Inject(at = @At("HEAD"), method = "removed()V", cancellable = true)
    private void onRemoved(CallbackInfo ci) {
        if (UiUtilsState.skipNextContainerRemoval) {
            UiUtilsState.skipNextContainerRemoval = false;
            ci.cancel();
            return;
        }

        fabricateOverlayInitialized = false;
    }
}
