package net.chuck.chuckpack.util;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.imageio.ImageIO;

/**
 * Shared Windows (OS-level) notifications for background events
 * (Baritone goals, full inventory, ...). In-game chat keyword alerts
 * intentionally do NOT use this — they use Minecraft toasts instead.
 *
 * Balloon popups are queued and paced: at most 5 pending, one shown
 * every 4 seconds, oldest dropped when full. Stops notification spam
 * from rapid-fire events.
 */
public final class WindowsNotify {
    private static final int MAX_QUEUE = 5;
    private static final long PACE_MS = 4000;

    private static volatile TrayIcon trayIcon;
    private static final Object LOCK = new Object();
    private static final Deque<String[]> QUEUE = new ArrayDeque<>();
    private static volatile boolean workerStarted;

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

    private static void ensureWorker() {
        if (workerStarted) return;
        synchronized (LOCK) {
            if (workerStarted) return;
            workerStarted = true;
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        String[] next;
                        synchronized (LOCK) {
                            while (QUEUE.isEmpty()) {
                                LOCK.wait();
                            }
                            next = QUEUE.pollFirst();
                        }
                        if (next == null) continue;
                        try {
                            TrayIcon icon = icon();
                            if (icon != null) {
                                String title = next[0];
                                String message = next[1];
                                java.awt.EventQueue.invokeAndWait(() ->
                                    icon.displayMessage(title, message, TrayIcon.MessageType.INFO));
                            }
                        } catch (Throwable ignored) {}
                        try {
                            Thread.sleep(PACE_MS);
                        } catch (InterruptedException e) {
                            return;
                        }
                        // No tray icon squatting: drop it when idle, it is
                        // re-added on the next notification.
                        synchronized (LOCK) {
                            if (QUEUE.isEmpty() && trayIcon != null) {
                                try {
                                    SystemTray.getSystemTray().remove(trayIcon);
                                } catch (Throwable ignored) {}
                                trayIcon = null;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }, "ChuckPack-Notify");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public static void send(String title, String message) {
        try {
            ensureWorker();
            synchronized (LOCK) {
                while (QUEUE.size() >= MAX_QUEUE) {
                    QUEUE.pollFirst();
                }
                QUEUE.addLast(new String[]{title, message});
                LOCK.notifyAll();
            }
        } catch (Throwable ignored) {}
    }
}
