package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class SwarmAutoConnect extends Module {

    private static final long SCAN_INTERVAL_MS = 5000;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Your role in the swarm. Workers auto-connect, hosts do nothing.")
        .defaultValue(Mode.Worker)
        .build()
    );

    public final Setting<String> address = sgGeneral.add(new StringSetting.Builder()
        .name("address")
        .description("IP address of the swarm host.")
        .defaultValue("localhost")
        .visible(() -> mode.get() == Mode.Worker)
        .build()
    );

    public final Setting<Integer> port = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("TCP port of the swarm host.")
        .defaultValue(6969)
        .range(1, 65535)
        .noSlider()
        .build()
    );

    private long lastScanMs = 0;

    public SwarmAutoConnect() {
        super(Categories.Misc, "Swarm Auto Connect", "Workers auto-connect to the swarm host every 5 seconds using system time sync.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mode.get() != Mode.Worker) return;

        Swarm swarm = Modules.get().get(Swarm.class);
        if (swarm == null || !swarm.isActive()) return;

        long now = System.currentTimeMillis();
        long currentSlot = now / SCAN_INTERVAL_MS;
        long lastSlot = lastScanMs / SCAN_INTERVAL_MS;
        if (currentSlot == lastSlot) return;
        lastScanMs = now;

        if (!swarm.isWorker()) {
            setSwarmSetting(swarm, "ipAddress", address.get());
            setSwarmSetting(swarm, "serverPort", port.get());
            swarm.close();
            swarm.worker = new SwarmWorker(address.get(), port.get());
            ChatUtils.infoPrefix("Swarm Auto Connect", "Starting worker -> (highlight)%s:%d", address.get(), port.get());
        }
    }

    private void setSwarmSetting(Swarm swarm, String fieldName, Object value) {
        try {
            var f = swarm.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object settingObj = f.get(swarm);
            var valueField = settingObj.getClass().getSuperclass().getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(settingObj, value);
        } catch (Exception ignored) {
        }
    }

    public enum Mode {
        Worker,
        Host
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
