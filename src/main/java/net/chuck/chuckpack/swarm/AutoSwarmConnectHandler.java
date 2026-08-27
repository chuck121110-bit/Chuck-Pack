package net.chuck.chuckpack.swarm;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;

public class AutoSwarmConnectHandler {

    private static final long SCAN_INTERVAL_MS = 5000;

    private long lastScanMs = 0;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        try {
            if (Modules.get() == null) return;
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm == null) return;
            IAutoSwarmConnect autoConnect = (IAutoSwarmConnect) (Object) swarm;
            boolean autoOn = false;
            try { autoOn = autoConnect.chuckpack$autoConnectSetting().get(); } catch (Throwable ignored) {}
            if (!autoOn) {
                lastScanMs = 0;
                return;
            }
            // Auto-toggle Swarm active so it works from TitleScreen without clicking Play (never requires .swarm sync)
            if (!swarm.isActive()) {
                try { swarm.toggle(); ChatUtils.info("Auto Swarm Connect: auto-enabled Swarm"); } catch (Throwable ignored) {}
                return;
            }

            long now = System.currentTimeMillis();
            long currentSlot = now / SCAN_INTERVAL_MS;
            long lastSlot = lastScanMs / SCAN_INTERVAL_MS;
            if (currentSlot == lastSlot) return;
            lastScanMs = now;

            // Works outside world (TitleScreen) — no player/level needed
            boolean hostAlive = false, workerAlive = false;
            try { hostAlive = swarm.isHost() && swarm.host != null && swarm.host.isAlive(); } catch (Throwable ignored) {}
            try { workerAlive = swarm.isWorker() && swarm.worker != null && swarm.worker.isAlive(); } catch (Throwable ignored) {}

            if (hostAlive || workerAlive) return;

            chuckpack$attemptConnect(swarm);
        } catch (Throwable t) {
            // Never crash game from auto-connect tick (server joining crash fix)
            try { ChatUtils.error("Auto Swarm Connect tick: " + t.getMessage()); } catch (Throwable ignored) {}
        }
    }

    private static long lastFailLogMs = 0;
    private static String lastFailAddr = "";

    private void chuckpack$attemptConnect(Swarm swarm) {
        try {
            Object mode = chuckpack$getField(swarm, "mode");
            String modeName = mode != null ? chuckpack$invokeGet(mode).toString() : null;

            if (modeName != null && modeName.equalsIgnoreCase("Host")) {
                chuckpack$startHost(swarm);
            } else if (modeName != null && modeName.equalsIgnoreCase("Worker")) {
                chuckpack$connectWorker(swarm);
            }
        } catch (Throwable e) {
            // Rate-limit Connection refused spam (worker tries every 5s when host not yet up — qnd8U8h log)
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            boolean isRefused = msg.toLowerCase().contains("refused") || (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().toLowerCase().contains("refused"));
            long now = System.currentTimeMillis();
            if (isRefused) {
                if (now - lastFailLogMs < 30000) return; // only log every 30s for refused
                lastFailLogMs = now;
                ChatUtils.info("Auto Swarm Connect: host not yet up, retrying in 5s... (" + msg + ")");
            } else {
                if (now - lastFailLogMs < 5000) return;
                lastFailLogMs = now;
                ChatUtils.error("Auto Swarm Connect failed: " + msg);
            }
        }
    }

    private void chuckpack$startHost(Swarm swarm) throws Exception {
        swarm.close();
        int port = ((Number) chuckpack$invokeGet(chuckpack$getField(swarm, "serverPort"))).intValue();

        Class<?> hostClass = Class.forName("meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmHost");
        Object hostInstance = hostClass.getConstructor(int.class).newInstance(port);

        java.lang.reflect.Field hostField = Swarm.class.getField("host");
        hostField.set(swarm, hostInstance);

        ChatUtils.info("Auto Swarm Connect: started host on port " + port);
    }

    private void chuckpack$connectWorker(Swarm swarm) throws Exception {
        try { swarm.close(); } catch (Throwable ignored) {}
        String address = "";
        int port = 6969;
        try { address = chuckpack$invokeGet(chuckpack$getField(swarm, "ipAddress")).toString(); } catch (Throwable ignored) {}
        try { port = ((Number) chuckpack$invokeGet(chuckpack$getField(swarm, "serverPort"))).intValue(); } catch (Throwable ignored) {}
        if (address == null || address.trim().isEmpty()) {
            // Fallback to SwarmAutoConnect's address if Swarm ipAddress empty (lets worker join host with no server)
            try {
                net.chuck.chuckpack.modules.misc.SwarmAutoConnect sac = Modules.get().get(net.chuck.chuckpack.modules.misc.SwarmAutoConnect.class);
                if (sac != null && !sac.address.get().trim().isEmpty()) address = sac.address.get().trim();
                else address = "localhost";
            } catch (Throwable ignored) { address = "localhost"; }
        }
        if (address.trim().isEmpty()) address = "localhost";

        Class<?> workerClass = Class.forName("meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker");
        Object workerInstance = workerClass.getConstructor(String.class, int.class).newInstance(address, port);

        java.lang.reflect.Field workerField = Swarm.class.getField("worker");
        workerField.set(swarm, workerInstance);

        ChatUtils.info("Auto Swarm Connect: connecting to " + address + ":" + port);
    }

    private Object chuckpack$getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    private Object chuckpack$invokeGet(Object setting) throws Exception {
        return setting.getClass().getMethod("get").invoke(setting);
    }
}
