package net.aero.aeropack.mixin.ui_utils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.aero.aeropack.modules.misc.UiUtilsMod;
import net.aero.aeropack.uiutils.UiUtils;
import net.aero.aeropack.uiutils.UiUtilsModAccess;
import net.aero.aeropack.uiutils.UiUtilsState;
import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class UiUtilsHandledScreenMixin extends Screen {

    @Unique
    private TextFieldWidget uiUtilsChatField;

    @Unique
    private ButtonWidget uiUtilsSpamButton;

    @Unique
    private ButtonWidget uiUtilsQueueButton;

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
    private ButtonWidget overlayClickSlotModeButton;

    @Unique
    private ButtonWidget overlayButtonClickModeButton;

    @Unique
    private TextFieldWidget overlayClickSyncIdField;

    @Unique
    private TextFieldWidget overlayClickRevisionField;

    @Unique
    private TextFieldWidget overlayClickSlotField;

    @Unique
    private TextFieldWidget overlayClickButtonField;

    @Unique
    private CyclingButtonWidget<SlotActionType> overlayClickActionButton;

    @Unique
    private CyclingButtonWidget<Boolean> overlayClickDelayToggle;

    @Unique
    private TextFieldWidget overlayClickTimesField;

    @Unique
    private ButtonWidget overlayClickSendButton;

    @Unique
    private TextFieldWidget overlayButtonSyncIdField;

    @Unique
    private TextFieldWidget overlayButtonIdField;

    @Unique
    private CyclingButtonWidget<Boolean> overlayButtonDelayToggle;

    @Unique
    private TextFieldWidget overlayButtonTimesField;

    @Unique
    private ButtonWidget overlayButtonSendButton;

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

    private UiUtilsHandledScreenMixin(Text title) {
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
            this::addDrawableChild);
        uiUtilsChatField =
            UiUtils.createChatField(mc, this.font, baseX, nextY + spacing);
        addDrawableChild(uiUtilsChatField);

        initFabricateOverlay();
    }

    @Inject(at = @At("TAIL"),
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
    private void onRender(DrawContext graphics, int mouseX, int mouseY,
        float partialTicks, CallbackInfo ci) {
        if (!UiUtilsState.isUiEnabled())
            return;

        UiUtils.refreshLabels();

        ScreenHandler screenHandler = ((HandledScreen<?>) (Object) this).getScreenHandler();
        UiUtils.renderSyncInfo(Minecraft.getInstance(), graphics, screenHandler);

        updateOverlayVisibility();

        if (UiUtilsState.fabricateOverlayOpen) {
            layoutFabricateOverlay();
            if (screenHandler != null)
                updateOverlaySyncInfo(screenHandler);
            drawFabricateLabels(graphics);
            if (overlayDragging) {
                int newX = MathHelper.clamp(mouseX - dragOffsetX, 0,
                    this.width - OVERLAY_WIDTH);
                int newY = MathHelper.clamp(mouseY - dragOffsetY, 0,
                    Math.max(0, this.height - 40));
                UiUtilsState.fabricateOverlayX = newX;
                UiUtilsState.fabricateOverlayY = newY;
            }
        }
    }

    @Inject(at = @At("HEAD"),
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
    private void aeropack$renderFabricateOverlayBackground(
        DrawContext graphics, int mouseX, int mouseY,
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
        method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z",
        cancellable = true)
    private void onKeyPressed(KeyInput keyInput,
        CallbackInfoReturnable<Boolean> cir) {
        if (!UiUtilsState.isUiEnabled())
            return;

        if (uiUtilsChatField != null && uiUtilsChatField.isFocused()) {
            if (keyInput.key() == 257) {
                String msg = uiUtilsChatField.getText().trim();
                Minecraft mc = Minecraft.getInstance();
                if (!msg.isEmpty() && mc.player != null) {
                    mc.player.networkHandler.sendChatMessage(msg);
                }
                uiUtilsChatField.setText("");
                uiUtilsChatField.setFocused(false);
                cir.setReturnValue(true);
                return;
            }

            if (uiUtilsChatField.keyPressed(keyInput)) {
                cir.setReturnValue(true);
                return;
            }

            if (keyInput.key() == net.minecraft.client.util.InputUtil.GLFW_KEY_ESCAPE) {
                uiUtilsChatField.setFocused(false);
                cir.setReturnValue(true);
                return;
            }
        }

        if (UiUtilsState.fabricateOverlayOpen && fabricateOverlayInitialized) {
            TextFieldWidget focused = getFocusedOverlayField();
            if (focused != null && focused != uiUtilsChatField) {
                if (focused.keyPressed(keyInput)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private TextFieldWidget getFocusedOverlayField() {
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

        overlayClickSlotModeButton = ButtonWidget.builder(
            Text.literal("Click Slot"),
            b -> switchFabricateMode(MODE_CLICK_SLOT))
            .dimensions(0, 0, MODE_BUTTON_WIDTH, 20).build();
        addDrawableChild(overlayClickSlotModeButton);

        overlayButtonClickModeButton = ButtonWidget.builder(
            Text.literal("Button Click"),
            b -> switchFabricateMode(MODE_BUTTON_CLICK))
            .dimensions(0, 0, MODE_BUTTON_WIDTH, 20).build();
        addDrawableChild(overlayButtonClickModeButton);

        overlayClickSyncIdField = new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20,
            Text.literal("Sync Id"));
        addDrawableChild(overlayClickSyncIdField);

        overlayClickRevisionField = new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20,
            Text.literal("Revision"));
        addDrawableChild(overlayClickRevisionField);

        overlayClickSlotField =
            new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20, Text.literal("Slot"));
        overlayClickSlotField.setText("0");
        addDrawableChild(overlayClickSlotField);

        overlayClickButtonField = new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20,
            Text.literal("Button"));
        overlayClickButtonField.setText("0");
        addDrawableChild(overlayClickButtonField);

        overlayClickActionButton = CyclingButtonWidget
            .<SlotActionType>builder(
                action -> Text.literal(action.name()),
                () -> SlotActionType.PICKUP)
            .values(SlotActionType.values()).build(0, 0, FIELD_WIDTH, 20,
                Text.literal("Action"), (button, value) -> {});
        addDrawableChild(overlayClickActionButton);

        overlayClickDelayToggle =
            CyclingButtonWidget.onOffBuilder(false).build(0, 0, DELAY_BUTTON_WIDTH, 20,
                Text.literal("Delay"), (button, value) -> {});
        addDrawableChild(overlayClickDelayToggle);

        overlayClickTimesField = new TextFieldWidget(textRenderer, 0, 0, TIMES_FIELD_WIDTH, 20,
            Text.literal("Times to send"));
        overlayClickTimesField.setText("1");
        addDrawableChild(overlayClickTimesField);

        overlayClickSendButton =
            ButtonWidget.builder(Text.literal("Send"), b -> sendClickSlot())
                .dimensions(0, 0, 90, 20).build();
        addDrawableChild(overlayClickSendButton);

        overlayButtonSyncIdField = new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20,
            Text.literal("Sync Id"));
        addDrawableChild(overlayButtonSyncIdField);

        overlayButtonIdField = new TextFieldWidget(textRenderer, 0, 0, FIELD_WIDTH, 20,
            Text.literal("Button Id"));
        overlayButtonIdField.setText("0");
        addDrawableChild(overlayButtonIdField);

        overlayButtonDelayToggle =
            CyclingButtonWidget.onOffBuilder(false).build(0, 0, DELAY_BUTTON_WIDTH, 20,
                Text.literal("Delay"), (button, value) -> {});
        addDrawableChild(overlayButtonDelayToggle);

        overlayButtonTimesField = new TextFieldWidget(textRenderer, 0, 0, TIMES_FIELD_WIDTH, 20,
            Text.literal("Times to send"));
        overlayButtonTimesField.setText("1");
        addDrawableChild(overlayButtonTimesField);

        overlayButtonSendButton =
            ButtonWidget.builder(Text.literal("Send"), b -> sendButtonClick())
                .dimensions(0, 0, 90, 20).build();
        addDrawableChild(overlayButtonSendButton);

        fabricateOverlayInitialized = true;
        switchFabricateMode(MODE_CLICK_SLOT);
        updateOverlayVisibility();
    }

    @Unique
    private void switchFabricateMode(int mode) {
        fabricateMode = mode;
        overlayClickSlotModeButton.setMessage(Text.literal(
            "Click Slot" + (fabricateMode == MODE_CLICK_SLOT ? " ✓" : "")));
        overlayButtonClickModeButton.setMessage(Text.literal(
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
    private void setWidgetVisibleAndActive(ClickableWidget widget,
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
    private void parkWidget(ClickableWidget widget, int offscreen) {
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
            overlayX = MathHelper.clamp(UiUtilsState.fabricateOverlayX, 0,
                this.width - OVERLAY_WIDTH);
        if (UiUtilsState.fabricateOverlayY >= 0)
            overlayY = MathHelper.clamp(UiUtilsState.fabricateOverlayY, 0,
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
    private void drawFabricateLabels(DrawContext graphics) {
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
    private void drawLabel(DrawContext graphics, String text,
        TextFieldWidget field) {
        graphics.drawTextWithShadow(this.font, text, field.getX(), field.getY() - LABEL_OFFSET,
            0xFFAAAAAA);
    }

    @Inject(at = @At("HEAD"),
        method = "mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z",
        cancellable = true)
    private void aeropack$fabricateOverlayMouseClicked(
        Click context,
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
        method = "mouseReleased(Lnet/minecraft/client/gui/Click;)Z",
        cancellable = true)
    private void aeropack$fabricateOverlayMouseReleased(
        Click context,
        CallbackInfoReturnable<Boolean> cir) {
        if (!overlayDragging)
            return;
        overlayDragging = false;
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Unique
    private void updateOverlaySyncInfo(ScreenHandler screenHandler) {
        if (screenHandler == null)
            return;

        if (!overlayClickSyncIdField.isFocused())
            overlayClickSyncIdField.setText(String.valueOf(screenHandler.syncId));
        if (!overlayClickRevisionField.isFocused())
            overlayClickRevisionField
                .setText(String.valueOf(screenHandler.getRevision()));
        if (!overlayButtonSyncIdField.isFocused())
            overlayButtonSyncIdField.setText(String.valueOf(screenHandler.syncId));
    }

    @Unique
    private void sendClickSlot() {
        if (!UiUtils.isInteger(overlayClickSyncIdField.getText())
            || !UiUtils.isInteger(overlayClickRevisionField.getText())
            || !UiUtils.isInteger(overlayClickSlotField.getText())
            || !UiUtils.isInteger(overlayClickButtonField.getText())
            || !UiUtils.isInteger(overlayClickTimesField.getText())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int syncId = Integer.parseInt(overlayClickSyncIdField.getText());
        short slot = Short.parseShort(overlayClickSlotField.getText());
        byte button = Byte.parseByte(overlayClickButtonField.getText());
        int timesToSend = Integer.parseInt(overlayClickTimesField.getText());
        if (timesToSend < 1)
            return;

        SlotActionType action = overlayClickActionButton.getValue();
        if (action == null)
            return;

        if (mc.getConnection() == null || mc.player == null)
            return;

        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        if (screenHandler == null)
            return;

        ClientPacketListener networkHandler = mc.getConnection();
        net.minecraft.screen.sync.ComponentChangesHash.ComponentHasher hashGenerator =
            networkHandler.getComponentHasher();

        List<net.minecraft.item.ItemStack> beforeStacks =
            new ArrayList<>(screenHandler.slots.size());
        for (int i = 0; i < screenHandler.slots.size(); i++)
            beforeStacks.add(screenHandler.slots.get(i).getStack().copy());
        net.minecraft.item.ItemStack carriedBeforeStack =
            screenHandler.getCursorStack().copy();

        screenHandler.onSlotClick(slot, button, action, mc.player);

        int revision = screenHandler.getRevision();

        Int2ObjectMap<ItemStackHash> diffSlots = new Int2ObjectArrayMap<>();
        for (int i = 0; i < screenHandler.slots.size(); i++) {
            net.minecraft.item.ItemStack beforeStack =
                beforeStacks.get(i);
            net.minecraft.item.ItemStack afterStack =
                screenHandler.slots.get(i).getStack();
            boolean changed;
            if (beforeStack.isEmpty() && afterStack.isEmpty())
                changed = false;
            else if (beforeStack.isEmpty() != afterStack.isEmpty())
                changed = true;
            else
                changed = beforeStack.getItem() != afterStack.getItem()
                    || beforeStack.getCount() != afterStack.getCount();
            if (changed) {
                diffSlots.put(i, ItemStackHash.fromItemStack(afterStack, hashGenerator));
            }
        }

        ItemStackHash cursor =
            ItemStackHash.fromItemStack(screenHandler.getCursorStack(), hashGenerator);
        ItemStackHash carriedBefore =
            ItemStackHash.fromItemStack(carriedBeforeStack, hashGenerator);

        ClickSlotC2SPacket packet =
            new ClickSlotC2SPacket(syncId, revision, slot, button,
                action, diffSlots, cursor);

        UiUtils.chatIfEnabled("ClickSlot: slot=" + slot + ", action=" + action
            + ", diff=" + diffSlots.size());

        Runnable toRun = UiUtils.getFabricatePacketRunnable(mc,
            overlayClickDelayToggle.getValue(), packet);
        for (int i = 0; i < timesToSend; i++)
            toRun.run();
    }

    @Unique
    private void sendButtonClick() {
        if (!UiUtils.isInteger(overlayButtonSyncIdField.getText())
            || !UiUtils.isInteger(overlayButtonIdField.getText())
            || !UiUtils.isInteger(overlayButtonTimesField.getText())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int syncId = Integer.parseInt(overlayButtonSyncIdField.getText());
        int buttonId = Integer.parseInt(overlayButtonIdField.getText());
        int timesToSend = Integer.parseInt(overlayButtonTimesField.getText());
        if (timesToSend < 1)
            return;

        ButtonClickC2SPacket packet =
            new ButtonClickC2SPacket(syncId, buttonId);

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
