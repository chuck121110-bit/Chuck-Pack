package net.chuck.chuckpack.modules.combat;

import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SwarmGuard extends Module {

    public enum AttackMode {
        EntitiesOnly,
        HitDetectionOnly
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<AttackMode> attackMode = sgGeneral.add(new EnumSetting.Builder<AttackMode>()
        .name("Attack Mode")
        .description("What Swarm Guard targets.")
        .defaultValue(AttackMode.EntitiesOnly)
        .build()
    );

    public final Setting<Double> meleeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Melee Threat Range")
        .description("Horizontal range for melee mobs and players.")
        .defaultValue(15.0)
        .min(1.0)
        .sliderRange(1.0, 60.0)
        .build()
    );

    public final Setting<Double> meleeVerticalRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Melee Vertical Range")
        .defaultValue(4.0)
        .min(1.0)
        .sliderRange(1.0, 30.0)
        .build()
    );

    public final Setting<Double> rangedRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Ranged Threat Range")
        .defaultValue(30.0)
        .min(1.0)
        .sliderRange(1.0, 80.0)
        .build()
    );

    public final Setting<Double> rangedVerticalRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Ranged Vertical Range")
        .defaultValue(8.0)
        .min(1.0)
        .sliderRange(1.0, 40.0)
        .build()
    );

    public final Setting<Boolean> notifyWhenFull = sgGeneral.add(new BoolSetting.Builder()
        .name("Inventory Full Alerts")
        .description("Alert when any worker's inventory becomes full.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> inventoryCheckInterval = sgGeneral.add(new IntSetting.Builder()
        .name("Check Interval (ticks)")
        .description("Ticks between inventory checks (20 ticks = 1 second).")
        .defaultValue(100)
        .min(20)
        .sliderRange(20, 600)
        .build()
    );

    public SwarmGuard() {
        super(
            Categories.Combat,
            "Swarm Guard",
            "Per-worker bodyguard AI. Activated via .swarm guard."
        );
    }

    public static SwarmGuard get() {
        return Modules.get().get(SwarmGuard.class);
    }

    public String hostPlayerName = null;
    public boolean active = false;
    public int workerId = 0;

    private Player retaliationTarget = null;
    private int retaliationTicks = 0;
    private static final int RETALIATION_MAX_TICKS = 300;

    private int tickCounter = 0;
    private int inventoryTickCounter = 0;

    private enum InventoryAlertState { NONE, WARNING, FULL }
    private final Map<String, InventoryAlertState> workerAlertStates = new HashMap<>();

    public static final Path INVENTORY_DIR = Path.of(System.getProperty("java.io.tmpdir"), "swarm-inventory");

    public static void initInventoryDir() {
        try { Files.createDirectories(INVENTORY_DIR); } catch (IOException ignored) {}
    }

    public void activate(String hostName) {
        this.hostPlayerName = hostName;
        this.active = true;
        this.tickCounter = 0;
        this.retaliationTarget = null;
        this.retaliationTicks = 0;
        this.lastFollowedHostName = null;
        this.inventoryTickCounter = 0;
        this.workerAlertStates.clear();
        this.workerId = 0;
    }

    public void deactivate() {
        this.active = false;
        this.hostPlayerName = null;
        this.retaliationTarget = null;
        this.retaliationTicks = 0;
        this.lastFollowedHostName = null;
        this.inventoryTickCounter = 0;
        this.workerAlertStates.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (mc.getConnection() != null) {
                mc.player.connection.sendChat("#cancel");
            }
            if (PathManagers.get().isPathing()) {
                PathManagers.get().stop();
            }
            try { net.chuck.chuckpack.autoflypath.PathFlightRuntime.controller().stop(); } catch (Throwable ignored) {}
            try {
                var af = meteordevelopment.meteorclient.systems.modules.Modules.get().get(net.chuck.chuckpack.modules.movement.AutoFly.class);
                if (af != null && af.isActive()) af.toggle();
            } catch (Throwable ignored) {}
        }
    }

    // ── Host-side inventory management ────────────────────────────────────

    public static void sendInventoryCommandToAllWorkers(String cmd) {
        Minecraft mc = Minecraft.getInstance();
        Swarm swarm = Modules.get().get(Swarm.class);
        if (swarm == null || !swarm.isActive() || !swarm.isHost()) return;
        if (mc.level == null) return;

        SwarmConnection[] conns = swarm.host.getConnections();
        for (SwarmConnection conn : conns) {
            if (conn != null && conn.socket.isConnected() && !conn.socket.isClosed()) {
                conn.messageToSend = cmd;
            }
        }
    }

    public static void cleanInventoryFiles(String prefix) {
        initInventoryDir();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(INVENTORY_DIR, prefix + "*.txt")) {
            for (Path file : stream) {
                try { Files.deleteIfExists(file); } catch (IOException ignored) {}
            }
        } catch (IOException ignored) {}
    }

    private static final java.util.concurrent.ScheduledExecutorService chuckpack$executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ChuckPack-SwarmGuard");
        t.setDaemon(true);
        return t;
    });

    public void hostTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        Swarm swarm = Modules.get().get(Swarm.class);
        if (swarm == null || !swarm.isActive() || !swarm.isHost()) return;

        if (notifyWhenFull.get()) {
            inventoryTickCounter++;
            if (inventoryTickCounter >= inventoryCheckInterval.get()) {
                inventoryTickCounter = 0;
                cleanInventoryFiles("inv-");
                sendInventoryCommandToAllWorkers("swarm inventory-check");

                chuckpack$executor.schedule(() -> {
                    mc.execute(() -> {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(INVENTORY_DIR, "inv-*.txt")) {
                            for (Path file : stream) {
                                try {
                                    String content = Files.readString(file).trim();
                                    String name = "?";
                                    int empty = 0;
                                    for (String line : content.split("\n")) {
                                        if (line.startsWith("WORKER:")) name = line.substring(7);
                                        if (line.startsWith("EMPTY:")) {
                                            try { empty = Integer.parseInt(line.substring(6)); } catch (NumberFormatException ignored) {}
                                        }
                                    }

                                    InventoryAlertState newState;
                                    if (empty <= 0) {
                                        newState = InventoryAlertState.FULL;
                                    } else if (empty <= 3) {
                                        newState = InventoryAlertState.WARNING;
                                    } else {
                                        newState = InventoryAlertState.NONE;
                                    }

                                    InventoryAlertState lastState = workerAlertStates.getOrDefault(name, InventoryAlertState.NONE);

                                    if (newState != InventoryAlertState.NONE && newState != lastState) {
                                        workerAlertStates.put(name, newState);
                                        if (newState == InventoryAlertState.FULL) {
                                            ChatUtils.warning("(highlight)%s (default)inventory is (highlight)FULL(default)!", name);
                                        } else if (newState == InventoryAlertState.WARNING) {
                                            ChatUtils.warning("(highlight)%s (default)inventory low! (highlight)%d (default)slots left", name, empty);
                                        }
                                    } else if (newState == InventoryAlertState.NONE) {
                                        workerAlertStates.remove(name);
                                    }
                                } catch (IOException ignored) {}
                            }
                        } catch (IOException ignored) {}
                    });
                }, 1, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
    }

    // ── Worker tick (guard AI) ────────────────────────────────────────────

    public void tick(Minecraft mc) {
        if (!active || mc.player == null || mc.level == null) return;

        Swarm swarm = Modules.get().get(Swarm.class);
        if (swarm == null || !swarm.isActive() || !swarm.isWorker()) return;

        tickCounter++;

        Player host = findHost(mc);
        if (host == null) return;

        detectAndSetRetaliation(mc, host);

        if (retaliationTarget != null) {
            if (!retaliationTarget.isAlive() || mc.player.distanceTo(retaliationTarget) > 30.0 || retaliationTicks > RETALIATION_MAX_TICKS) {
                retaliationTarget = null;
                retaliationTicks = 0;
            } else {
                retaliationTicks++;
            }
        }

        Entity hostFightTarget = detectHostFightTarget(mc, host);

        Entity threat = null;
        if (attackMode.get() != AttackMode.HitDetectionOnly) {
            threat = findHighestThreat(mc, host);
        }

        if (retaliationTarget != null && retaliationTarget.isAlive() && retaliationTarget != host) {
            lastFollowedHostName = null;
            attackTarget(mc, retaliationTarget, host);
        } else if (hostFightTarget != null) {
            lastFollowedHostName = null;
            attackTarget(mc, hostFightTarget, host);
        } else if (threat != null && threat instanceof LivingEntity livingThreat && !livingThreat.isRemoved()) {
            lastFollowedHostName = null;
            attackTarget(mc, threat, host);
        } else {
            followHost(mc, host);
        }
    }

    private Entity detectHostFightTarget(Minecraft mc, Player host) {
        Entity best = null;
        double closestDist = 4.0;

        for (Entity entity : mc.level.players()) {
            if (entity == host || entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living) || living.isRemoved() || !living.isAlive()) continue;
            if (living.hurtTime <= 0) continue;

            if (entity instanceof Player p) {
                if (Friends.get().isFriend(p)) continue;
            }

            double dist = host.distanceTo(entity);
            if (dist < closestDist) {
                closestDist = dist;
                best = entity;
            }
        }

        return best;
    }

    private void attackTarget(Minecraft mc, Entity target, Player host) {
        if (target == host) return;
        if (PathManagers.get().isPathing() && tickCounter % 10 != 0) return;

        double distToTarget = mc.player.distanceTo(target);

        if (distToTarget <= 3.5) {
            if (mc.player.getAttackStrengthScale(0.5f) >= 1.0f) {
                Rotations.rotate(
                    Rotations.getYaw(target),
                    Rotations.getPitch(target, Target.Body)
                );
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        } else {
            if (tickCounter % 10 == 0) {
                PathManagers.get().moveTo(target.blockPosition());
            }
        }
    }

    private void detectAndSetRetaliation(Minecraft mc, Player host) {
        Player attacker = detectAttacker(mc, mc.player);
        if (attacker != null && !Friends.get().isFriend(attacker) && attacker != host) {
            setRetaliationTarget(attacker);
            return;
        }

        attacker = detectAttacker(mc, host);
        if (attacker != null && !Friends.get().isFriend(attacker) && attacker != mc.player) {
            setRetaliationTarget(attacker);
        }
    }

    private Player detectAttacker(Minecraft mc, Player target) {
        if (target.hurtTime > 0) {
            LivingEntity lastAttacker = target.getLastAttacker();
            if (lastAttacker instanceof Player player) return player;

            double closestDist = Double.MAX_VALUE;
            Player closest = null;
            for (Player p : mc.level.players()) {
                if (p == target || p == mc.player) continue;
                double dist = p.distanceTo(target);
                if (dist < 3.5 && dist < closestDist) {
                    closestDist = dist;
                    closest = p;
                }
            }
            if (closest != null && closest.getAttackStrengthScale(0.5f) < 0.3f) return closest;
        }
        return null;
    }

    private void setRetaliationTarget(Player player) {
        retaliationTarget = player;
        retaliationTicks = 0;
    }

    public void applyRetaliation(String attackerName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Player p : mc.level.players()) {
            if (p.getName().getString().equalsIgnoreCase(attackerName)) {
                setRetaliationTarget(p);
                return;
            }
        }
        retaliationTicks = 0;
    }

    private Player findHost(Minecraft mc) {
        if (hostPlayerName == null || hostPlayerName.isEmpty()) return null;
        String safe = hostPlayerName.replaceAll("\u00a7.", "");

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (player.getName().getString().replaceAll("\u00a7.", "").equalsIgnoreCase(safe)) return player;
        }
        return null;
    }

    private boolean isRangedThreat(Entity entity) {
        return entity instanceof RangedAttackMob;
    }

    private double getThreatScore(Entity entity, double distFromHost) {
        double base;
        if (entity instanceof Creeper) {
            base = 95;
        } else if (isRangedThreat(entity)) {
            base = distFromHost <= 16.0 ? 110 : 85;
        } else if (entity instanceof Monster) {
            base = 60;
        } else {
            return 0;
        }
        if (base <= 0) return 0;
        return base / Math.max(distFromHost, 0.5);
    }

    private Entity findHighestThreat(Minecraft mc, Player host) {
        Entity best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (entity == host) continue;
            if (!(entity instanceof LivingEntity living) || living.isRemoved() || !living.isAlive()) continue;
            if (!(entity instanceof Monster) && !isRangedThreat(entity)) continue;

            boolean ranged = isRangedThreat(entity);
            double vRange = ranged ? rangedVerticalRange.get() : meleeVerticalRange.get();
            double hRange = ranged ? rangedRange.get() : meleeRange.get();

            double yDiff = Math.abs(mc.player.getY() - entity.getY());
            if (yDiff > vRange) continue;

            double dist = host.distanceTo(entity);
            if (dist > hRange) continue;

            double score = getThreatScore(entity, dist);
            if (score > bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private String lastFollowedHostName = null;

    private void followHost(Minecraft mc, Player host) {
        String hostName = host.getName().getString();
        if (!hostName.equals(lastFollowedHostName)) {
            if (mc.player != null && mc.getConnection() != null) {
                mc.player.connection.sendChat("#follow player " + hostName);
            }
            lastFollowedHostName = hostName;
        }
    }
}
