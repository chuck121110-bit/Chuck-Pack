package net.aero.aeropack.modules.misc;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.aero.aeropack.uiutils.UiUtilsState;

public class UiUtilsMod extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSlotOverlay = settings.createGroup("Slot Overlay");

    public final Setting<Boolean> slotOverlayEnabled = sgSlotOverlay.add(new BoolSetting.Builder()
        .name("enabled")
        .description("Shows slot numbers over container slots.")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> slotOverlayColor = sgSlotOverlay.add(new ColorSetting.Builder()
        .name("color")
        .description("Color of the slot number overlay.")
        .defaultValue(new SettingColor(0xEF, 0xEF, 0xEF))
        .build()
    );

    public final Setting<Integer> slotOverlayAlpha = sgSlotOverlay.add(new IntSetting.Builder()
        .name("alpha")
        .description("Alpha of the slot number overlay.")
        .defaultValue(255)
        .range(0, 255)
        .sliderRange(0, 255)
        .build()
    );

    public final Setting<Integer> slotOverlayOffsetX = sgSlotOverlay.add(new IntSetting.Builder()
        .name("offset-x")
        .description("Horizontal offset for the slot number overlay.")
        .defaultValue(2)
        .range(-20, 20)
        .sliderRange(-20, 20)
        .build()
    );

    public final Setting<Integer> slotOverlayOffsetY = sgSlotOverlay.add(new IntSetting.Builder()
        .name("offset-y")
        .description("Vertical offset for the slot number overlay.")
        .defaultValue(2)
        .range(-20, 20)
        .sliderRange(-20, 20)
        .build()
    );

    public final Setting<Boolean> slotOverlayHoverOnly = sgSlotOverlay.add(new BoolSetting.Builder()
        .name("hover-only")
        .description("Only show the slot index when your mouse is over a slot.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> fabricateOverlayBgAlpha = sgGeneral.add(new IntSetting.Builder()
        .name("overlay-bg-alpha")
        .description("Background alpha for the fabricate packet overlay.")
        .defaultValue(120)
        .range(0, 255)
        .sliderRange(0, 255)
        .build()
    );

    public final Setting<Boolean> logToChat = sgGeneral.add(new BoolSetting.Builder()
        .name("log-to-chat")
        .description("Echo UI-Utils actions and diagnostics to chat.")
        .defaultValue(false)
        .build()
    );

    public UiUtilsMod() {
        super(Categories.Misc, "UI-Utils", "Slot overlay, packet fabrication, and UI quality-of-life.");
    }

    @Override
    public void onActivate() {
        UiUtilsState.enabled = true;
    }

    @Override
    public void onDeactivate() {
        UiUtilsState.enabled = false;
        UiUtilsState.fabricateOverlayOpen = false;
        UiUtilsState.sendUiPackets = true;
        UiUtilsState.delayUiPackets = false;
        UiUtilsState.delayedUiPackets.clear();
        UiUtilsState.shouldEditSign = false;
    }

    public boolean isLogToChat() {
        return logToChat.get();
    }

    public int getSlotOverlayColorI() {
        int alpha = slotOverlayAlpha.get();
        SettingColor c = slotOverlayColor.get();
        return (alpha << 24) | (c.r << 16) | (c.g << 8) | c.b;
    }

    public int getSlotOverlayOffsetX() {
        return slotOverlayOffsetX.get();
    }

    public int getSlotOverlayOffsetY() {
        return slotOverlayOffsetY.get();
    }

    public boolean isSlotOverlayEnabled() {
        return slotOverlayEnabled.get();
    }

    public boolean isSlotOverlayHoverOnly() {
        return slotOverlayHoverOnly.get();
    }

    public int getFabricateOverlayBgAlpha() {
        return fabricateOverlayBgAlpha.get();
    }
}
