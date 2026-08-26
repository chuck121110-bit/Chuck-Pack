package net.chuck.chuckpack.modules.misc;

/*
    Ported from: https://github.com/etianl/Trouser-Streak
    Originally by etianl
    Uses the locator bar waypoint system introduced in Minecraft 1.21.6 to triangulate
    player coordinates without any mods on their end.
*/

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTriangulate extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum OutputMode { CHAT, HUD }

    private final Setting<OutputMode> outputMode = sgGeneral.add(new EnumSetting.Builder<OutputMode>()
            .name("output-mode")
            .description("How to display triangulated positions.")
            .defaultValue(OutputMode.CHAT)
            .build()
    );

    private final Setting<Boolean> createWaypoints = sgGeneral.add(new BoolSetting.Builder()
            .name("create-waypoints")
            .description("Create Meteor waypoints for triangulated player positions.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> minDelta = sgGeneral.add(new DoubleSetting.Builder()
            .name("minimum-sample-delta-degrees")
            .description("The minimum delta required between packet samples for an output.")
            .sliderRange(0, 360)
            .defaultValue(1)
            .build()
    );

    private final Setting<Integer> minMove = sgGeneral.add(new IntSetting.Builder()
            .name("minimum-move-distance")
            .description("Minimum distance you must move to record a new sample.")
            .sliderRange(0, 100)
            .defaultValue(20)
            .build()
    );

    private final Map<UUID, String> uuidToName = new ConcurrentHashMap<>();
    private final Map<UUID, Waypoint> playerWaypoints = new ConcurrentHashMap<>();

    private static class TriangulationSamples {
        public Vec3 position1;
        public Vec3 position2;
        public float azi1 = Float.NaN;
        public float azi2 = Float.NaN;
    }

    private final Map<UUID, TriangulationSamples> triangulations = new ConcurrentHashMap<>();

    public static class TriangulationResult {
        public double wx, wz;
        public String playerName;
        public long lastUpdated;

        public TriangulationResult(double wx, double wz, String playerName) {
            this.wx = wx;
            this.wz = wz;
            this.playerName = playerName;
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    public static final Map<UUID, TriangulationResult> lastResults = new ConcurrentHashMap<>();
    public static Map<UUID, TriangulationResult> getLastResults() { return lastResults; }

    public PlayerTriangulate() {
        super(Categories.Misc, "player-triangulate",
                "Triangulates player locations using the locator bar waypoint system introduced in Minecraft 1.21.6.");
    }

    @Override
    public void onActivate() {
        triangulations.clear();
        lastResults.clear();
        uuidToName.clear();
        playerWaypoints.clear();
    }

    @Override
    public void onDeactivate() {
        triangulations.clear();
        lastResults.clear();
        uuidToName.clear();
        playerWaypoints.clear();
    }

    private void addOrUpdateWaypoint(UUID uuid, String playerName, double wx, double wz) {
        if (!createWaypoints.get() || mc == null || mc.level == null || mc.getConnection() == null) return;

        String wpName = playerName + " (Triangulated)";

        Waypoints waypoints = Waypoints.get();
        Waypoint existing = playerWaypoints.get(uuid);

        if (existing != null) {
            for (Waypoint wp : waypoints) {
                if (wp == existing) {
                    BlockPos newPos = new BlockPos((int) Math.floor(wx), mc.player.getBlockY(), (int) Math.floor(wz));
                    existing.pos.set(newPos);
                    existing.name.set(wpName);
                    waypoints.save();
                    return;
                }
            }
            playerWaypoints.remove(uuid);
        }

        BlockPos newPos = new BlockPos((int) Math.floor(wx), mc.player.getBlockY(), (int) Math.floor(wz));
        Waypoint waypoint = new Waypoint.Builder()
                .name(wpName)
                .pos(newPos)
                .dimension(PlayerUtils.getDimension())
                .build();

        waypoints.add(waypoint);
        playerWaypoints.put(uuid, waypoint);
        waypoints.save();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.getConnection() != null && mc.getConnection().getOnlinePlayers() != null) {
            for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
                UUID uuid = entry.getProfile().id();
                if (uuid != null) {
                    Component displayName = entry.getTabListDisplayName();
                    String name = displayName != null ? displayName.getString() : entry.getProfile().name();

                    if (name != null && !name.isEmpty()) {
                        uuidToName.put(uuid, name);
                    }
                }
            }
        }

        // TODO: Port getWaypointHandler - Meteor 26.1.2 API changed
    }
}
