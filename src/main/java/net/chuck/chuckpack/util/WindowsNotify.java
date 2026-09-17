package net.chuck.chuckpack.util;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Shared Windows (OS-level) notifications for background events
 * (Baritone goals, full inventory, ...). In-game chat keyword alerts
 * intentionally do NOT use this — they use Minecraft toasts instead.
 */
public final class WindowsNotify {
    private static volatile TrayIcon trayIcon;
    private static final Object LOCK = new Object();

    private WindowsNotify() {}

    private static TrayIcon icon() {
        TrayIcon icon = trayIcon;
        if (icon != null) return icon;
        synchronized (LOCK) {
            if (trayIcon != null) return trayIcon;
            if (!SystemTray.isSupported()) return null;
            try (InputStream is = WindowsNotify.class.getResourceAsStream("/assets/chuckpack/icon.png")) {
                if (is == null) return null;
                BufferedImage image = ImageIO.read(is);
                if (image == null) return null;
                Image scaled = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                TrayIcon created = new TrayIcon(scaled, "Chuck Pack");
                created.setImageAutoSize(true);
                try {
                    SystemTray.getSystemTray().add(created);
                } catch (Exception e) {
                    return null;
                }
                trayIcon = created;
                return created;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static void send(String title, String message) {
        try {
            TrayIcon icon = icon();
            if (icon != null) {
                icon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            }
        } catch (Throwable ignored) {}
    }
}
