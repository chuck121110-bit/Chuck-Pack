package net.aero.aeropack.uiutils;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UiUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger("AeroPack/UI-Utils");

    public static int spamCount = 1;
    public static Packet<?> lastFabricatedPacket = null;

    private static Button spamButtonRef;
    private static Button queueButtonRef;

    private static int spamButtonX;
    private static int spamButtonY;
    private static int spamButtonWidth;
    private static int queueButtonX;
    private static int queueButtonY;
    private static int queueButtonWidth;

    private UiUtils() {
    }

    private static final int BTN_WIDTH = 160;
    private static final int HALF_BTN_WIDTH = 78;
    private static final int BTN_HEIGHT = 20;
    private static final int ROWS = 14;

    public static int getUiWidgetRows() {
        return ROWS;
    }

    public static int addUiWidgets(Minecraft mc, int x, int startY, int spacing,
                                   Consumer<AbstractWidget> addWidget) {
        int y = startY;
        int halfGap = 4;

        addWidget.accept(Button.builder(
            Component.literal("Close without packet"),
            b -> {
                mc.setScreen(null);
                UiUtils.chatIfEnabled("Closed screen without packet");
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Send packets: " + UiUtilsState.sendUiPackets),
            b -> {
                UiUtilsState.sendUiPackets = !UiUtilsState.sendUiPackets;
                b.setMessage(Component.literal("Send packets: " + UiUtilsState.sendUiPackets));
                UiUtils.chatIfEnabled("sendUiPackets=" + UiUtilsState.sendUiPackets);
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Delay packets: " + UiUtilsState.delayUiPackets),
            b -> {
                UiUtilsState.delayUiPackets = !UiUtilsState.delayUiPackets;
                b.setMessage(Component.literal("Delay packets: " + UiUtilsState.delayUiPackets));
                UiUtils.chatIfEnabled("delayUiPackets=" + UiUtilsState.delayUiPackets);
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Leave & send packets"),
            b -> {
                sendDelayedPackets();
                mc.setScreen(null);
                UiUtils.chatIfEnabled("Left screen and sent delayed packets");
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Disconnect & send packets"),
            b -> {
                sendDelayedPackets();
                if (mc.getConnection() != null)
                    mc.getConnection().getConnection().disconnect(
                        Component.literal("Disconnecting (UI-UTILS)"));
                UiUtils.chatIfEnabled("Disconnected and sent delayed packets");
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Fabricate packet"),
            b -> {
                UiUtilsState.fabricateOverlayOpen = !UiUtilsState.fabricateOverlayOpen;
                UiUtils.chatIfEnabled("Fabricate overlay: " + UiUtilsState.fabricateOverlayOpen);
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Copy GUI Title JSON"),
            b -> {
                if (mc.currentScreen != null) {
                    String title = mc.currentScreen.getTitle().getString();
                    mc.keyboardHandler().setClipboard(title);
                    UiUtils.chatIfEnabled("Copied GUI title: " + title);
                }
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        Button queueBtn = Button.builder(
            Component.literal("Queue: " + UiUtilsState.delayedUiPackets.size()),
            b -> {}).dimensions(x + HALF_BTN_WIDTH + halfGap, y, HALF_BTN_WIDTH, BTN_HEIGHT).build();
        queueButtonRef = queueBtn;
        queueButtonX = x + HALF_BTN_WIDTH + halfGap;
        queueButtonY = y;
        queueButtonWidth = HALF_BTN_WIDTH;

        addWidget.accept(Button.builder(
            Component.literal("Clear Queue"),
            b -> {
                int count = UiUtilsState.delayedUiPackets.size();
                UiUtilsState.delayedUiPackets.clear();
                queueBtn.setMessage(Component.literal("Queue: 0"));
                UiUtils.chatIfEnabled("Cleared " + count + " queued packets");
            }).dimensions(x, y, HALF_BTN_WIDTH, BTN_HEIGHT).build());

        addWidget.accept(queueBtn);
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Resync Inv"),
            b -> {
                if (mc.player != null && mc.getConnection() != null) {
                    mc.player.currentScreenHandler = mc.player.playerScreenHandler;
                    mc.setScreen(null);
                    UiUtils.chatIfEnabled("Resynced inventory");
                }
            }).dimensions(x, y, HALF_BTN_WIDTH, BTN_HEIGHT).build());

        addWidget.accept(Button.builder(
            Component.literal("Disconnect"),
            b -> {
                if (mc.getConnection() != null) {
                    mc.getConnection().getConnection().disconnect(
                        Component.literal("Disconnecting (UI-UTILS)"));
                    UiUtils.chatIfEnabled("Disconnected");
                }
            }).dimensions(x + HALF_BTN_WIDTH + halfGap, y, HALF_BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        int spamTotalWidth = BTN_WIDTH;
        int minusWidth = 20;
        int plusWidth = 20;
        int spamBtnWidth = spamTotalWidth - minusWidth - halfGap - plusWidth - halfGap;

        Button localSpamButton = Button.builder(
            Component.literal("Spam (x" + spamCount + ")"),
            b -> {
                for (int i = 0; i < spamCount; i++) {
                    if (lastFabricatedPacket != null) {
                        if (!UiUtilsState.sendUiPackets) {
                            UiUtils.chatIfEnabled("Send packets is off, packet not sent");
                            continue;
                        }
                        if (UiUtilsState.delayUiPackets) {
                            UiUtilsState.delayedUiPackets.add(lastFabricatedPacket);
                        } else {
                            sendPacket(lastFabricatedPacket);
                        }
                    }
                }
                if (UiUtilsState.delayUiPackets) {
                    queueBtn.setMessage(Component.literal("Queue: " + UiUtilsState.delayedUiPackets.size()));
                }
                UiUtils.chatIfEnabled("Spammed " + spamCount + " packets");
            }).dimensions(x + minusWidth + halfGap, y, spamBtnWidth, BTN_HEIGHT).build();
        final Button spamBtn = localSpamButton;
        spamButtonRef = spamBtn;
        spamButtonX = x + minusWidth + halfGap;
        spamButtonY = y;
        spamButtonWidth = spamBtnWidth;

        addWidget.accept(Button.builder(
            Component.literal("-"),
            b -> {
                spamCount = Math.max(1, spamCount - 1);
                spamBtn.setMessage(Component.literal("Spam (x" + spamCount + ")"));
                UiUtils.chatIfEnabled("Spam count: " + spamCount);
            }).dimensions(x, y, minusWidth, BTN_HEIGHT).build());

        addWidget.accept(spamBtn);

        addWidget.accept(Button.builder(
            Component.literal("+"),
            b -> {
                spamCount = Math.min(64, spamCount + 1);
                spamBtn.setMessage(Component.literal("Spam (x" + spamCount + ")"));
                UiUtils.chatIfEnabled("Spam count: " + spamCount);
            }).dimensions(x + minusWidth + halfGap + spamBtnWidth + halfGap, y, plusWidth, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("Send One"),
            b -> {
                if (!UiUtilsState.delayedUiPackets.isEmpty()) {
                    Packet<?> pkt = UiUtilsState.delayedUiPackets.remove(0);
                    sendPacket(pkt);
                    queueBtn.setMessage(Component.literal("Queue: " + UiUtilsState.delayedUiPackets.size()));
                    UiUtils.chatIfEnabled("Sent 1 packet, " + UiUtilsState.delayedUiPackets.size() + " remaining");
                } else {
                    UiUtils.chatIfEnabled("Queue is empty");
                }
            }).dimensions(x, y, HALF_BTN_WIDTH, BTN_HEIGHT).build());

        addWidget.accept(Button.builder(
            Component.literal("Pop Last"),
            b -> {
                if (!UiUtilsState.delayedUiPackets.isEmpty()) {
                    UiUtilsState.delayedUiPackets.remove(
                        UiUtilsState.delayedUiPackets.size() - 1);
                    queueBtn.setMessage(Component.literal("Queue: " + UiUtilsState.delayedUiPackets.size()));
                    UiUtils.chatIfEnabled("Popped last, " + UiUtilsState.delayedUiPackets.size() + " remaining");
                } else {
                    UiUtils.chatIfEnabled("Queue is empty");
                }
            }).dimensions(x + HALF_BTN_WIDTH + halfGap, y, HALF_BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        addWidget.accept(Button.builder(
            Component.literal("How to Use"),
            b -> {
                Minecraft.getInstance().setScreen(
                    new net.aero.aeropack.gui.screens.UiUtilsDocumentationScreen(
                        meteordevelopment.meteorclient.gui.GuiThemes.get()));
            }).dimensions(x, y, BTN_WIDTH, BTN_HEIGHT).build());
        y += BTN_HEIGHT + spacing;

        return y;
    }

    public static EditBox createChatField(Minecraft mc, Font Font,
                                                   int x, int y) {
        EditBox field = new EditBox(Font, x, y, 200, 20, Component.literal("")) {
            @Override
            public void setFocused(boolean focused) {
                super.setFocused(focused);
                if (focused) setSuggestion(null);
                else if (getText().isEmpty()) setSuggestion("Chat ...");
            }

            @Override
            public void setText(String Component) {
                super.setText(Component);
                if (isFocused() || !Component.isEmpty()) setSuggestion(null);
                else setSuggestion("Chat ...");
            }
        };
        field.setSuggestion("Chat ...");
        return field;
    }

    public static void renderSyncInfo(Minecraft mc, GuiGraphicsExtractor graphics,
                                      AbstractContainerMenu AbstractContainerMenu) {
        if (AbstractContainerMenu == null) return;
        Font Font = mc.font;
        String info = "SyncID: " + AbstractContainerMenu.syncId + " Rev: " + AbstractContainerMenu.getRevision()
                + " Slots: " + AbstractContainerMenu.slots.size();
        graphics.drawTextWithShadow(Font, info, 4, 4, 0xFFFFFF);
    }

    public static void chatIfEnabled(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            try {
                var mod = meteordevelopment.meteorclient.systems.modules.Modules.get().get(
                    net.aero.aeropack.modules.misc.UiUtilsMod.class);
                if (mod != null && mod.isLogToChat()) {
                    mc.player.sendSystemMessage(Component.literal("[UI-Utils] " + message));
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public static void refreshLabels() {
        if (spamButtonRef != null) {
            spamButtonRef.setMessage(Component.literal("Spam (x" + spamCount + ")"));
        }
        if (queueButtonRef != null) {
            queueButtonRef.setMessage(Component.literal("Queue: " + UiUtilsState.delayedUiPackets.size()));
        }
    }

    public static void renderLabels(GuiGraphicsExtractor graphics, Font Font) {
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Runnable getFabricatePacketRunnable(Minecraft mc, boolean delay,
                                                     Packet<?> packet) {
        lastFabricatedPacket = packet;
        if (delay) {
            return () -> {
                UiUtilsState.delayedUiPackets.add(packet);
            };
        } else {
            return () -> {
                ClientPacketListener handler = mc.getConnection();
                if (handler != null) handler.sendPacket(packet);
            };
        }
    }

    private static void sendDelayedPackets() {
        Minecraft mc = Minecraft.getInstance();
        for (Packet<?> pkt : UiUtilsState.delayedUiPackets) {
            sendPacket(pkt);
        }
        UiUtilsState.delayedUiPackets.clear();
    }

    private static void sendPacket(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) handler.sendPacket(packet);
    }
}
