package net.aero.aeropack.swarm;

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
        Swarm swarm = Modules.get().get(Swarm.class);
        if (swarm == null || !swarm.isActive()) return;

        IAutoSwarmConnect autoConnect = (IAutoSwarmConnect) (Object) swarm;
        if (!autoConnect.aeropack$autoConnectSetting().get()) {
            lastScanMs = 0;
            return;
        }

        long now = System.currentTimeMillis();
        long currentSlot = now / SCAN_INTERVAL_MS;
        long lastSlot = lastScanMs / SCAN_INTERVAL_MS;
        if (currentSlot == lastSlot) return;
        lastScanMs = now;

        boolean hostAlive = swarm.isHost() && swarm.host != null && swarm.host.isAlive();
        boolean workerAlive = swarm.isWorker() && swarm.worker != null && swarm.worker.isAlive();

        if (hostAlive || workerAlive) return;

        aeropack$attemptConnect(swarm);
    }

    private void aeropack$attemptConnect(Swarm swarm) {
        try {
            Object mode = aeropack$getField(swarm, "mode");
            String modeName = mode != null ? aeropack$invokeGet(mode).toString() : null;

            if (modeName != null && modeName.equalsIgnoreCase("Host")) {
                aeropack$startHost(swarm);
            } else if (modeName != null && modeName.equalsIgnoreCase("Worker")) {
                aeropack$connectWorker(swarm);
            }
        } catch (Exception e) {
            ChatUtils.error("Auto Swarm Connect failed: " + e.getMessage());
        }
    }

    private void aeropack$startHost(Swarm swarm) throws Exception {
        swarm.close();
        int port = ((Number) aeropack$invokeGet(aeropack$getField(swarm, "serverPort"))).intValue();

        Class<?> hostClass = Class.forName("meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmHost");
        Object hostInstance = hostClass.getConstructor(int.class).newInstance(port);

        java.lang.reflect.Field hostField = Swarm.class.getField("host");
        hostField.set(swarm, hostInstance);

        ChatUtils.info("Auto Swarm Connect: started host on port " + port);
    }

    private void aeropack$connectWorker(Swarm swarm) throws Exception {
        swarm.close();
        String address = aeropack$invokeGet(aeropack$getField(swarm, "ipAddress")).toString();
        int port = ((Number) aeropack$invokeGet(aeropack$getField(swarm, "serverPort"))).intValue();

        Class<?> workerClass = Class.forName("meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker");
        Object workerInstance = workerClass.getConstructor(String.class, int.class).newInstance(address, port);

        java.lang.reflect.Field workerField = Swarm.class.getField("worker");
        workerField.set(swarm, workerInstance);

        ChatUtils.info("Auto Swarm Connect: connecting to " + address + ":" + port);
    }

    private Object aeropack$getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    private Object aeropack$invokeGet(Object setting) throws Exception {
        return setting.getClass().getMethod("get").invoke(setting);
    }
}
