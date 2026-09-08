package net.chuck.chuckpack.util;

import meteordevelopment.meteorclient.utils.player.ChatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Registry of player names reported by swarm workers from whatever servers
// they are on. Unioned and deduplicated across all workers: the same name
// reported by any number of bots shows up exactly once.
public final class SwarmPlayerList {
    private static final int CAP = 400;
    private static final String PREFIX = "swarm ChuckPack-playertab ";
    private static final Set<String> NAMES = Collections.synchronizedSet(new LinkedHashSet<>());

    private SwarmPlayerList() {}

    public static String clean(String n) {
        if (n == null) return null;
        n = n.replace(';', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        if (n.isEmpty() || n.length() > 32) return null;
        return n;
    }

    // Payload format: "swarm ChuckPack-playertab <server>;<name1>;<name2>;..."
    public static void handlePayload(String payload) {
        try {
            if (payload == null || !payload.startsWith(PREFIX)) return;
            String[] parts = payload.substring(PREFIX.length()).trim().split(";", -1);
            if (parts.length < 1) return;
            String server = parts[0].isEmpty() ? "unknown server" : parts[0];
            List<String> fresh = new ArrayList<>();
            synchronized (NAMES) {
                for (int i = 1; i < parts.length; i++) {
                    String n = clean(parts[i]);
                    if (n == null || NAMES.contains(n)) continue;
                    NAMES.add(n);
                    fresh.add(n);
                    while (NAMES.size() > CAP) {
                        Iterator<String> it = NAMES.iterator();
                        it.next();
                        it.remove();
                    }
                }
            }
            if (!fresh.isEmpty()) {
                List<String> shown = fresh.size() > 25 ? fresh.subList(0, 25) : fresh;
                String msg = String.join(", ", shown);
                if (fresh.size() > 25) msg += " (+" + (fresh.size() - 25) + " more)";
                final String text = msg;
                // Render thread only: vanilla chat is not thread-safe.
                net.minecraft.client.Minecraft.getInstance().execute(() ->
                    ChatUtils.infoPrefix("Swarm", "Players seen on (highlight)%s(default): %s", server, text));
            }
        } catch (Throwable ignored) {}
    }

    public static List<String> names() {
        synchronized (NAMES) {
            return new ArrayList<>(NAMES);
        }
    }
}
