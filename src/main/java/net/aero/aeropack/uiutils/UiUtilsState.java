package net.aero.aeropack.uiutils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.Packet;

public class UiUtilsState {
    public static boolean enabled = false;

    public static boolean fabricateOverlayOpen = false;
    public static int fabricateOverlayX = -1;
    public static int fabricateOverlayY = -1;

    public static boolean skipNextContainerRemoval = false;

    public static boolean sendUiPackets = true;
    public static boolean delayUiPackets = false;
    public static final List<Packet<?>> delayedUiPackets = new ArrayList<>();

    public static boolean shouldEditSign = false;

    public static boolean isUiEnabled() {
        return enabled;
    }
}
