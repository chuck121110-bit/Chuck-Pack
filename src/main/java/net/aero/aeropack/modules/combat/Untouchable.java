package net.aero.aeropack.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Untouchable extends Module {
    private static final int DIRECTION_SAMPLES = 16;
    private static final long MACE_PACKET_CUE_MS = 500;
    private static final long PRIMED_SPEAR_MEMORY_MS = 1200;
    private static final long CHARGING_CREEPER_MEMORY_MS = 1200;

    private enum KeepDistanceMode {
        ALWAYS("Always"),
        WHILE_DODGING("While dodging");

        private final String name;

        KeepDistanceMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum MovePauseMode {
        MOVE_TOWARD_PLAYER("Moving toward player"),
        ANY_MOVEMENT_KEY("Any movement key");

        private final String name;

        MovePauseMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> dodgeMaces = sgGeneral.add(new BoolSetting.Builder()
        .name("Dodge Maces")
        .description("Dodges mace hits, including packet-spoofed mace combos.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> dodgeSwords = sgGeneral.add(new BoolSetting.Builder()
        .name("Dodge Swords")
        .description("Dodges sword attacks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> dodgeAxes = sgGeneral.add(new BoolSetting.Builder()
        .name("Dodge Axes")
        .description("Dodges axe attacks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> dodgeSpears = sgGeneral.add(new BoolSetting.Builder()
        .name("Dodge Spears")
        .description("Dodges spear attacks.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyPrimedSpears = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Primed Spears")
        .description("Only dodges spears that are primed for attack.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> keepDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("Keep Distance")
        .description("Keeps a safe distance from armed players.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> autoDistanceOnDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Distance on Damage")
        .description("Automatically switches to always keeping distance after taking damage.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> autoDistanceOnTotemPop = sgGeneral.add(new BoolSetting.Builder()
        .name("Auto Distance on Totem Pop")
        .description("Automatically switches to always keeping distance after a totem pops.")
        .defaultValue(false)
        .build()
    );
    private final Setting<KeepDistanceMode> keepDistanceMode = sgGeneral.add(new EnumSetting.Builder<KeepDistanceMode>()
        .name("Keep Distance Mode")
        .description("When to keep distance from players.")
        .defaultValue(KeepDistanceMode.ALWAYS)
        .build()
    );
    private final Setting<MovePauseMode> movePauseMode = sgGeneral.add(new EnumSetting.Builder<MovePauseMode>()
        .name("Move Pause Mode")
        .description("When moving manually pauses automatic dodging.")
        .defaultValue(MovePauseMode.MOVE_TOWARD_PLAYER)
        .build()
    );
    private final Setting<Double> playerDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Player Distance")
        .description("The minimum distance to keep from other players.")
        .defaultValue(7)
        .range(3, 16)
        .sliderRange(3, 16)
        .build()
    );
    private final Setting<Double> detectionRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Detection Range")
        .description("The maximum range at which threats are detected.")
        .defaultValue(12)
        .range(6, 32)
        .sliderRange(6, 32)
        .build()
    );
    private final Setting<Double> reachAllowance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Reach Allowance")
        .description("How close an attack line may pass to you while still being dodged.")
        .defaultValue(6)
        .range(3, 10)
        .sliderRange(3, 10)
        .build()
    );
    private final Setting<Integer> reactionTicks = sgGeneral.add(new IntSetting.Builder()
        .name("Prediction")
        .description("How many ticks ahead to predict incoming attacks.")
        .defaultValue(4)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Double> armedRadiusBonus = sgGeneral.add(new DoubleSetting.Builder()
        .name("Armed Radius Bonus")
        .description("Extra defensive radius while an enemy is holding a spear or mace.")
        .defaultValue(4)
        .range(0, 12)
        .sliderRange(0, 12)
        .build()
    );
    private final Setting<Double> scanDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Teleport Distance")
        .description("The maximum horizontal distance to search for a safe teleport destination.")
        .defaultValue(6)
        .range(1, 12)
        .sliderRange(1, 12)
        .build()
    );
    private final Setting<Integer> verticalScan = sgGeneral.add(new IntSetting.Builder()
        .name("Vertical Scan")
        .description("The maximum vertical offset to search for a safe teleport destination.")
        .defaultValue(3)
        .range(0, 8)
        .sliderRange(0, 8)
        .build()
    );
    private final Setting<Integer> teleportPackets = sgGeneral.add(new IntSetting.Builder()
        .name("Teleport Packets")
        .description("How many movement packets to send per teleport.")
        .defaultValue(4)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Integer> teleportCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("Teleport Cooldown")
        .description("How many ticks to wait between teleports.")
        .defaultValue(3)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );
    private final Setting<Boolean> avoidDrops = sgGeneral.add(new BoolSetting.Builder()
        .name("Avoid Drops")
        .description("Avoids teleporting into air above open drops.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> avoidHostileMobs = sgGeneral.add(new BoolSetting.Builder()
        .name("Avoid Hostile Mobs")
        .description("Dodges away from nearby hostile mobs.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> onlyChargingCreepers = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Charging Creepers")
        .description("Only avoids creepers that are charging their explosion.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> avoidArrows = sgGeneral.add(new BoolSetting.Builder()
        .name("Avoid Arrows")
        .description("Dodges incoming arrows.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> avoidCrystals = sgGeneral.add(new BoolSetting.Builder()
        .name("Avoid Crystals")
        .description("Dodges dangerous end crystals near you when they are placed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> emergencyEscape = sgGeneral.add(new BoolSetting.Builder()
        .name("Emergency Escape")
        .description("Teleports perpendicular to the current threat after taking at least the configured damage.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> emergencyDamage = sgGeneral.add(new IntSetting.Builder()
        .name("Emergency Damage")
        .description("Minimum health damage that triggers an emergency escape.")
        .defaultValue(10)
        .range(1, 40)
        .sliderRange(1, 40)
        .build()
    );

    private final Map<Integer, Vec3> previousPositions = new HashMap<>();
    private final Map<Integer, MacePacketCue> macePacketCues = new ConcurrentHashMap<>();
    private final Map<Integer, Long> primedSpearCues = new ConcurrentHashMap<>();
    private final Map<Integer, Long> chargingCreeperCues = new ConcurrentHashMap<>();
    private int cooldownTicksLeft;
    private int statusTicksLeft;
    private ThreatType activeThreat;
    private float lastHealth = -1;

    public Untouchable() {
        super(Categories.Combat, "Untouchable",
            "Predicts incoming damage and teleports you to the nearest safe spot before the hit lands.");
    }

    @Override
    public String getInfoString() {
        return activeThreat == null ? null : activeThreat.label;
    }

    @Override
    public void onActivate() {
        reset();
    }

    @Override
    public void onDeactivate() {
        reset();
    }

    private void reset() {
        previousPositions.clear();
        macePacketCues.clear();
        primedSpearCues.clear();
        chargingCreeperCues.clear();
        cooldownTicksLeft = 0;
        statusTicksLeft = 0;
        activeThreat = null;
        lastHealth = -1;
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        inspectPacket(event.packet);
    }

    private void inspectPacket(Packet<?> packet) {
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (Packet<?> subPacket : bundle.subPackets())
                inspectPacket(subPacket);
            return;
        }

        if (packet instanceof ClientboundAnimatePacket animation
            && (animation.getAction() == ClientboundAnimatePacket.SWING_MAIN_HAND
            || animation.getAction() == ClientboundAnimatePacket.SWING_OFF_HAND)) {
            handleSwingPacket(animation.getId());
            return;
        }

        if (packet instanceof ClientboundEntityEventPacket entityEvent && entityEvent.getEventId() == 35) {
            handleEntityEvent(entityEvent);
            return;
        }

        if (packet instanceof ClientboundDamageEventPacket damage) {
            handleDamageEvent(damage);
            return;
        }

        int id;
        Vec3 position;
        if (packet instanceof ClientboundSetEntityLinkPacket sync) {
            id = sync.getSourceId();
            Entity linkedEntity = mc.level != null ? mc.level.getEntity(sync.getDestId()) : null;
            position = linkedEntity != null ? linkedEntity.position() : null;
        } else if (packet instanceof ClientboundMoveEntityPacket move && move.hasPosition() && mc.level != null) {
            Entity movedEntity = move.getEntity(mc.level);
            if (movedEntity == null)
                return;
            id = movedEntity.getId();
            position = new Vec3(movedEntity.getX() + move.getXa() / 4096.0,
                movedEntity.getY() + move.getYa() / 4096.0,
                movedEntity.getZ() + move.getZa() / 4096.0);
        } else
            return;

        if (mc.player == null || mc.level == null || position == null)
            return;

        handleIncomingPosition(id, position);

        Entity entity = mc.level.getEntity(id);
        if (!(entity instanceof Player player) || player == mc.player
            || isIgnoredPlayer(player) || !isHoldingMace(player))
            return;

        Vec3 oldPosition = entity.position();
        boolean suddenRise = position.y - oldPosition.y >= 1.25;
        boolean aboveUs = position.y - mc.player.getY() >= 1.25;
        double horizontalSq = horizontalDistanceSqr(position, mc.player.position());
        if (suddenRise && aboveUs && horizontalSq <= square(reachAllowance.get())) {
            macePacketCues.put(id, new MacePacketCue(position, System.currentTimeMillis()));
            tryImmediatePacketDodge();
        }
    }

    private void handleEntityEvent(ClientboundEntityEventPacket entityEvent) {
        if (!isActive() || mc.player == null || mc.level == null)
            return;

        Entity entity = entityEvent.getEntity(mc.level);
        if (!(entity instanceof Player player))
            return;

        if (player == mc.player && autoDistanceOnTotemPop.get())
            setKeepDistanceAlways();
    }

    private void handleDamageEvent(ClientboundDamageEventPacket damage) {
        if (!isActive() || mc.player == null || mc.level == null
            || !autoDistanceOnDamage.get() || damage.entityId() != mc.player.getId())
            return;

        Entity source = mc.level.getEntity(damage.sourceCauseId());
        if (!(source instanceof Player attacker) || attacker == mc.player
            || isIgnoredPlayer(attacker))
            return;

        setKeepDistanceAlways();
    }

    private void setKeepDistanceAlways() {
        if (keepDistanceMode.get() != KeepDistanceMode.ALWAYS)
            keepDistanceMode.set(KeepDistanceMode.ALWAYS);
    }

    private void tryImmediatePacketDodge() {
        if (!isActive() || mc.player == null || mc.level == null
            || cooldownTicksLeft > 0)
            return;
        Threat threat = findMostUrgentThreat();
        if (threat != null && threat.type == ThreatType.MACE)
            teleportAway(threat);
    }

    private void handleSwingPacket(int entityId) {
        if (!isActive() || mc.player == null || mc.level == null
            || cooldownTicksLeft > 0)
            return;
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof Player attacker) || attacker == mc.player
            || isIgnoredPlayer(attacker))
            return;

        Threat threat = null;
        if (dodgeSpears.get()
            && (!onlyPrimedSpears.get() && isSpear(getHeldSpear(attacker))
            || wasRecentlyPrimed(attacker)))
            threat = getSpearThreat(attacker, true);
        if (dodgeMaces.get() && isHoldingMace(attacker)) {
            Threat maceThreat = getMaceThreat(attacker);
            if (maceThreat != null)
                threat = maceThreat;
        }
        if (dodgeSwords.get()) {
            Threat swordThreat = getMeleeThreat(attacker, WeaponType.SWORD, true);
            if (swordThreat != null && (threat == null || swordThreat.urgency > threat.urgency))
                threat = swordThreat;
        }
        if (dodgeAxes.get()) {
            Threat axeThreat = getMeleeThreat(attacker, WeaponType.AXE, true);
            if (axeThreat != null && (threat == null || axeThreat.urgency > threat.urgency))
                threat = axeThreat;
        }
        if (threat == null)
            return;

        activeThreat = threat.type;
        teleportAway(threat);
    }

    private void handleIncomingPosition(int entityId, Vec3 incomingPosition) {
        if (!isActive() || mc.player == null || mc.level == null)
            return;
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof Player attacker) || attacker == mc.player
            || isIgnoredPlayer(attacker))
            return;

        Threat threat = dodgeSpears.get() ? getPacketSpearThreat(attacker, incomingPosition) : null;
        if (threat == null && dodgeSwords.get())
            threat = getPacketMeleeThreat(attacker, incomingPosition, WeaponType.SWORD);
        if (threat == null && dodgeAxes.get())
            threat = getPacketMeleeThreat(attacker, incomingPosition, WeaponType.AXE);
        if (threat == null && keepDistance.get()
            && (keepDistanceMode.get() == KeepDistanceMode.ALWAYS
            || dodgeSpears.get() && getSpearThreat(attacker) != null
            || dodgeMaces.get() && getMaceThreat(attacker) != null
            || dodgeSwords.get() && getMeleeThreat(attacker, WeaponType.SWORD, false) != null
            || dodgeAxes.get() && getMeleeThreat(attacker, WeaponType.AXE, false) != null))
            threat = getSpacingThreat(attacker, incomingPosition);
        if (threat == null)
            return;
        if (cooldownTicksLeft > 0 && threat.type != ThreatType.SPACING
            && threat.type != ThreatType.SPEAR)
            return;

        activeThreat = threat.type;
        teleportAway(threat);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null || mc.player.isSpectator()) {
            reset();
            return;
        }

        rememberPrimedSpears();
        rememberChargingCreepers();
        checkEmergencyDamage();
        if (cooldownTicksLeft > 0)
            cooldownTicksLeft--;

        Threat threat = findMostUrgentThreat();
        if (threat != null) {
            activeThreat = threat.type;
            statusTicksLeft = teleportCooldown.get() + 2;
            if (cooldownTicksLeft <= 0 || threat.type == ThreatType.SPACING
                || threat.type == ThreatType.MACE)
                teleportAway(threat);
        } else if (statusTicksLeft > 0)
            statusTicksLeft--;
        else
            activeThreat = null;

        rememberPositions();
        prunePacketCues();
    }

    private Threat findMostUrgentThreat() {
        Threat best = null;
        for (Player attacker : mc.level.players()) {
            if (attacker == mc.player || !attacker.isAlive()
                || isIgnoredPlayer(attacker) || attacker.isSpectator())
                continue;

            Threat threat = null;
            Threat weaponThreat = null;
            if (dodgeSpears.get()) {
                Threat spearThreat = getSpearThreat(attacker);
                if (spearThreat != null)
                    weaponThreat = spearThreat;
            }
            if (dodgeMaces.get()) {
                Threat maceThreat = getMaceThreat(attacker);
                if (maceThreat != null && (weaponThreat == null
                    || maceThreat.urgency > weaponThreat.urgency))
                    weaponThreat = maceThreat;
            }
            if (dodgeSwords.get()) {
                Threat swordThreat = getMeleeThreat(attacker, WeaponType.SWORD, false);
                if (swordThreat != null && (weaponThreat == null
                    || swordThreat.urgency > weaponThreat.urgency))
                    weaponThreat = swordThreat;
            }
            if (dodgeAxes.get()) {
                Threat axeThreat = getMeleeThreat(attacker, WeaponType.AXE, false);
                if (axeThreat != null && (weaponThreat == null
                    || axeThreat.urgency > weaponThreat.urgency))
                    weaponThreat = axeThreat;
            }

            if (keepDistance.get()
                && (keepDistanceMode.get() == KeepDistanceMode.ALWAYS
                || weaponThreat != null)) {
                threat = getSpacingThreat(attacker, attacker.position());
            }
            if (weaponThreat != null
                && (threat == null || weaponThreat.urgency > threat.urgency))
                threat = weaponThreat;

            if (threat != null && (best == null || threat.urgency > best.urgency))
                best = threat;
        }
        if (avoidHostileMobs.get()) {
            for (Entity entity : mc.level.players()) {
                if (!(entity instanceof Enemy) || entity == mc.player
                    || !entity.isAlive())
                    continue;
                if (shouldSuppressDodging(entity.position()))
                    continue;
                if (onlyChargingCreepers.get() && !isChargingCreeper(entity))
                    continue;

                Threat threat = getHostileMobThreat(entity);
                if (threat != null && (best == null || threat.urgency > best.urgency))
                    best = threat;
            }
        }
        if (avoidArrows.get()) {
            for (Entity entity : mc.level.players()) {
                if (!(entity instanceof AbstractArrow arrow) || !arrow.isAlive()
                    || arrow.getOwner() == mc.player)
                    continue;
                Entity owner = arrow.getOwner();
                if (owner instanceof Player player && isIgnoredPlayer(player))
                    continue;

                Threat threat = getArrowThreat(arrow);
                if (threat != null && (best == null || threat.urgency > best.urgency))
                    best = threat;
            }
        }
        if (avoidCrystals.get()) {
            for (Entity entity : mc.level.players()) {
                if (!(entity instanceof EndCrystal crystal) || !crystal.isAlive())
                    continue;
                Threat threat = getCrystalThreat(crystal);
                if (threat != null && (best == null || threat.urgency > best.urgency))
                    best = threat;
            }
        }
        return best;
    }

    private void checkEmergencyDamage() {
        float health = mc.player.getHealth();
        if (lastHealth < 0) {
            lastHealth = health;
            return;
        }

        float damage = lastHealth - health;
        lastHealth = health;
        if (!emergencyEscape.get() || damage < emergencyDamage.get()
            || mc.player.hurtTime <= 0)
            return;

        Threat threat = null;
        for (Player attacker : mc.level.players()) {
            if (attacker == mc.player || !attacker.isAlive()
                || isIgnoredPlayer(attacker))
                continue;
            Threat candidate = getMeleeThreat(attacker, WeaponType.SWORD, false);
            if (candidate == null)
                candidate = getMeleeThreat(attacker, WeaponType.AXE, false);
            if (candidate != null && (threat == null || candidate.urgency > threat.urgency))
                threat = candidate;
        }

        if (threat == null) {
            Vec3 center = mc.player.getBoundingBox().getCenter();
            threat = new Threat(ThreatType.EMERGENCY, center, center.add(0, 0, 1), 1000);
        }
        activeThreat = ThreatType.EMERGENCY;
        teleportAway(threat, true);
    }

    private Threat getSpacingThreat(Player attacker, Vec3 attackerPosition) {
        if (shouldSuppressDodging(attackerPosition))
            return null;
        Vec3 centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.position());
        Vec3 attackerCenter = attackerPosition.add(centerOffset);
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        double distance = attackerCenter.distanceTo(playerCenter);
        if (distance >= playerDistance.get())
            return null;
        return new Threat(ThreatType.SPACING, attackerCenter, playerCenter,
            300 + (playerDistance.get() - distance) * 10);
    }

    private Threat getPacketSpearThreat(Player attacker, Vec3 incomingPosition) {
        ItemStack spear = attacker.getActiveItem();
        if (!isSpear(spear))
            spear = getHeldSpear(attacker);
        if (!isSpear(spear))
            return null;
        if (onlyPrimedSpears.get()
            && !(attacker.isUsingItem() && attacker.getUseItemRemainingTicks() >= getSpearReadyTicks(spear))
            && !wasRecentlyPrimed(attacker))
            return null;

        Vec3 centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.position());
        Vec3 start = incomingPosition.add(centerOffset);
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 velocity = incomingPosition.subtract(attacker.position());
        boolean primed = wasRecentlyPrimed(attacker) || attacker.isUsingItem()
            && attacker.getUseItemRemainingTicks() >= getSpearReadyTicks(spear);
        if (!primed && velocity.length() < 1)
            return null;
        Vec3 toPlayer = playerCenter.subtract(start);
        if (velocity.lengthSqr() < 0.01 || velocity.dot(toPlayer) <= 0) {
            Vec3 aim = attacker.getLookAngle();
            if (aim.dot(toPlayer.normalize()) < 0.7)
                return null;
            velocity = aim.scale(Math.max(0.75, velocity.length()));
        }

        double ticks = reactionTicks.get() + 2;
        double effectiveRange = Math.max(detectionRange.get(),
            velocity.length() * ticks + reachAllowance.get());
        if (start.distanceToSqr(playerCenter) > square(effectiveRange))
            return null;
        Vec3 end = start.add(velocity.scale(ticks));
        if (distanceToSegment(playerCenter, start, end) > reachAllowance.get())
            return null;
        return new Threat(ThreatType.SPEAR, start, end, 500);
    }

    private Threat getSpearThreat(Player attacker) {
        return getSpearThreat(attacker, false);
    }

    private Threat getSpearThreat(Player attacker, boolean swingTriggered) {
        ItemStack spear = attacker.getActiveItem();
        boolean primedNow = attacker.isUsingItem() && isSpear(spear)
            && attacker.getUseItemRemainingTicks() >= getSpearReadyTicks(spear);
        if (onlyPrimedSpears.get() && !primedNow && !wasRecentlyPrimed(attacker))
            return null;
        if (!primedNow && !swingTriggered)
            return null;
        if (!isSpear(spear))
            spear = getHeldSpear(attacker);
        if (!isSpear(spear))
            return null;

        Vec3 start = attacker.getBoundingBox().getCenter();
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 velocity = getObservedVelocity(attacker);
        double horizontalSpeed = horizontalLength(velocity);
        Vec3 toPlayer = playerCenter.subtract(start);
        double ticks = reactionTicks.get();
        double speedScaledRange = velocity.length() * (ticks + 2) + reachAllowance.get();
        double effectiveRange = Math.max(detectionRange.get(), speedScaledRange);
        if (start.distanceToSqr(playerCenter) > square(effectiveRange))
            return null;

        boolean movingAtUs = horizontalSpeed >= 0.12 && horizontalDot(toPlayer, velocity) > 0;
        Vec3 attackVelocity = velocity;
        if (!movingAtUs) {
            Vec3 aim = attacker.getLookAngle();
            double distance = toPlayer.length();
            double aimDot = distance < 1.0E-6 ? 1 : aim.dot(toPlayer.scale(1 / distance));
            double armedRange = reachAllowance.get() + ticks * 0.75;
            if (aimDot < 0.75 || distance > armedRange)
                return null;

            attackVelocity = aim.scale(Math.max(0.75, velocity.length()));
            horizontalSpeed = Math.max(0.12, horizontalLength(attackVelocity));
        }

        Vec3 end = start.add(attackVelocity.scale(ticks));
        double missDistance = horizontalDistanceToSegment(playerCenter, start, end);
        double verticalMiss = verticalDistanceToSegment(playerCenter, start, end);
        if (missDistance > reachAllowance.get() || verticalMiss > 3)
            return null;

        double currentDistance = horizontalLength(toPlayer);
        double ticksToReach = Math.max(0,
            (currentDistance - reachAllowance.get()) / horizontalSpeed);
        double urgency = (swingTriggered ? 180 : movingAtUs ? 100 : 85)
            - ticksToReach * 10 - missDistance;
        return new Threat(ThreatType.SPEAR, start, end, urgency);
    }

    private Threat getPacketMeleeThreat(Player attacker, Vec3 incomingPosition,
        WeaponType weaponType) {
        ItemStack weapon = weaponType.getWeapon(attacker);
        if (weapon.isEmpty())
            return null;

        Vec3 centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.position());
        Vec3 start = incomingPosition.add(centerOffset);
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 velocity = incomingPosition.subtract(attacker.position());
        Vec3 toPlayer = playerCenter.subtract(start);
        double distance = toPlayer.length();
        double ticks = reactionTicks.get() + 2;
        double weaponReach = weaponType == WeaponType.AXE ? 4.0 : 3.5;
        double effectiveRange = Math.max(detectionRange.get(), weaponReach
            + velocity.length() * ticks + reachAllowance.get());
        if (distance > effectiveRange)
            return null;

        Vec3 attackVelocity = velocity;
        if (attackVelocity.lengthSqr() < 0.01
            || attackVelocity.dot(toPlayer) <= 0) {
            Vec3 aim = attacker.getLookAngle();
            if (distance > 1.0E-6 && aim.dot(toPlayer.normalize()) < 0.5)
                return null;
            attackVelocity = aim.scale(Math.max(0.5, velocity.length()));
        }

        Vec3 end = start.add(attackVelocity.scale(ticks));
        double missDistance = distanceToSegment(playerCenter, start, end);
        double closeRange = weaponReach + 0.75;
        if (missDistance > reachAllowance.get() && distance > closeRange)
            return null;

        double urgency = (weaponType == WeaponType.AXE ? 95 : 90)
            + Math.max(0, closeRange - distance) * 10
            + (attacker.isUsingItem() ? 20 : 0);
        return new Threat(weaponType.threatType, start, end, urgency);
    }

    private Threat getMeleeThreat(Player attacker, WeaponType weaponType,
        boolean swingTriggered) {
        ItemStack weapon = weaponType.getWeapon(attacker);
        if (weapon.isEmpty())
            return null;

        Vec3 start = attacker.getBoundingBox().getCenter();
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 toPlayer = playerCenter.subtract(start);
        double distance = toPlayer.length();
        double horizontalDistance = horizontalLength(toPlayer);
        double weaponReach = weaponType == WeaponType.AXE ? 4.0 : 3.5;
        double ticks = reactionTicks.get();
        double speed = horizontalLength(getObservedVelocity(attacker));
        double effectiveRange = Math.max(detectionRange.get(),
            weaponReach + speed * (ticks + 2) + reachAllowance.get());
        if (distance > effectiveRange)
            return null;

        Vec3 velocity = getObservedVelocity(attacker);
        boolean movingAtUs = horizontalLength(velocity) >= 0.08
            && horizontalDot(toPlayer, velocity) > 0;
        Vec3 aim = attacker.getLookAngle();
        double aimDot = distance < 1.0E-6 ? 1 : aim.dot(toPlayer.scale(1 / distance));
        boolean closeEnough = horizontalDistance <= weaponReach + 0.9;
        boolean armed = swingTriggered || movingAtUs || aimDot > 0.55 || closeEnough;
        if (!armed)
            return null;

        Vec3 attackDirection = movingAtUs ? velocity : aim;
        if (attackDirection.lengthSqr() < 1.0E-6)
            attackDirection = toPlayer.normalize();
        Vec3 end = start.add(attackDirection.scale(ticks + 1));
        double missDistance = distanceToSegment(playerCenter, start, end);
        if (missDistance > reachAllowance.get() && !closeEnough)
            return null;

        double urgency = swingTriggered ? 180 : movingAtUs ? 120 : 100;
        urgency += Math.max(0, weaponReach - horizontalDistance) * 8;
        urgency -= Math.min(distance, 6) * 6;
        if (weaponType == WeaponType.AXE)
            urgency += 10;
        return new Threat(weaponType.threatType, start, end, urgency);
    }

    private Threat getMaceThreat(Player attacker) {
        if (!isHoldingMace(attacker))
            return null;

        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 attackerCenter = attacker.getBoundingBox().getCenter();
        double horizontalDistance = horizontalLength(attackerCenter.subtract(playerCenter));
        double armedRadius = reachAllowance.get() + 2 + armedRadiusBonus.get();
        if (horizontalDistance > armedRadius)
            return null;

        MacePacketCue cue = macePacketCues.get(attacker.getId());
        boolean packetSpoof = cue != null
            && System.currentTimeMillis() - cue.timeMs <= MACE_PACKET_CUE_MS;
        Vec3 velocity = getObservedVelocity(attacker);
        boolean fallingAbove = attackerCenter.y - playerCenter.y >= 1.25
            && (velocity.y < -0.08 || attacker.fallDistance >= 1.25F);
        Vec3 toPlayer = playerCenter.subtract(attackerCenter);
        double aimDot = toPlayer.lengthSqr() < 1.0E-6 ? 1
            : attacker.getLookAngle().dot(toPlayer.normalize());
        boolean closing = horizontalDot(toPlayer, velocity) > 0.01;
        boolean inReach = horizontalDistance <= reachAllowance.get();
        boolean preparingAttack = inReach || aimDot > 0.55 || closing;
        if (!packetSpoof && !fallingAbove && !preparingAttack)
            return null;

        Vec3 pathStart = packetSpoof ? cue.position : attackerCenter;
        Vec3 pathEnd = new Vec3(attackerCenter.x, playerCenter.y, attackerCenter.z);
        double urgency = packetSpoof ? 200
            : fallingAbove
                ? 150 + Math.max(0, -velocity.y * 20)
                    - Math.max(0, attackerCenter.y - playerCenter.y)
                : 125 - horizontalDistance;
        return new Threat(ThreatType.MACE, pathStart, pathEnd, urgency);
    }

    private Threat getHostileMobThreat(Entity mob) {
        if (mc.player == null || mob == null)
            return null;

        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 mobCenter = mob.getBoundingBox().getCenter();
        double distance = mobCenter.distanceTo(playerCenter);
        if (distance >= playerDistance.get())
            return null;

        return new Threat(ThreatType.MOB, mobCenter, playerCenter,
            250 + (playerDistance.get() - distance) * 10);
    }

    private Threat getArrowThreat(AbstractArrow arrow) {
        if (mc.player == null || mc.level == null)
            return null;

        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 arrowCenter = arrow.getBoundingBox().getCenter();
        Vec3 velocity = getObservedVelocity(arrow);
        if (velocity.lengthSqr() < 0.01)
            velocity = arrow.getDeltaMovement();
        if (velocity.lengthSqr() < 0.01)
            return null;

        Vec3 toPlayer = playerCenter.subtract(arrowCenter);
        if (velocity.dot(toPlayer) <= 0)
            return null;

        double ticks = reactionTicks.get() + 2;
        Vec3 end = arrowCenter.add(velocity.scale(ticks));
        double missDistance = distanceToSegment(playerCenter, arrowCenter, end);
        double effectiveRange = Math.max(detectionRange.get(),
            velocity.length() * ticks + reachAllowance.get());
        if (arrowCenter.distanceToSqr(playerCenter) > square(effectiveRange)
            || missDistance > reachAllowance.get() + 0.75)
            return null;

        double urgency = 110 + Math.max(0, 6 - arrowCenter.distanceTo(playerCenter)) * 12
            - missDistance * 5;
        return new Threat(ThreatType.ARROW, arrowCenter, end, urgency);
    }

    private Threat getCrystalThreat(EndCrystal crystal) {
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 crystalCenter = crystal.getBoundingBox().getCenter();
        double distance = crystalCenter.distanceTo(playerCenter);
        double radius = Math.max(detectionRange.get(), 6);
        if (distance > radius)
            return null;

        double urgency = 260 + Math.max(0, radius - distance) * 20;
        return new Threat(ThreatType.CRYSTAL, crystalCenter, playerCenter, urgency);
    }

    private boolean isChargingCreeper(Entity entity) {
        return entity instanceof Creeper creeper && isChargingCreeper(creeper);
    }

    private boolean isChargingCreeper(Creeper creeper) {
        if (creeper == null || !creeper.isAlive())
            return false;

        if (creeper.getSwellDir() > 0 || creeper.isIgnited() || creeper.isPowered())
            return true;

        Long expiry = chargingCreeperCues.get(creeper.getId());
        return expiry != null && expiry >= System.currentTimeMillis();
    }

    private Vec3 chooseDodgeDestination(Threat threat) {
        Vec3 playerPos = mc.player.position();
        Vec3 playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3 attackAxis = threat.pathEnd.subtract(threat.pathStart);
        if (attackAxis.lengthSqr() > 1.0E-6)
            attackAxis = attackAxis.normalize();
        Vec3 currentVelocity = mc.player.getDeltaMovement();
        Vec3 currentHorizontal = new Vec3(currentVelocity.x, 0, currentVelocity.z);
        if (currentHorizontal.lengthSqr() > 1.0E-6)
            currentHorizontal = currentHorizontal.normalize();

        ArrayList<DodgeCandidate> candidates = new ArrayList<>();
        int verticalRange = verticalScan.get();
        double maxHorizontal = threat.type == ThreatType.ARROW ? 1.0 : scanDistance.get();
        double minHorizontal = threat.type == ThreatType.ARROW ? 1.0 : 0.5;
        double horizontalStep = threat.type == ThreatType.ARROW ? 1.0 : 0.5;

        for (int yOffset = -verticalRange; yOffset <= verticalRange; yOffset++) {
            for (double distance = maxHorizontal; distance >= minHorizontal; distance -= horizontalStep) {
                for (int i = 0; i < DIRECTION_SAMPLES; i++) {
                    double angle = Math.PI * 2 * i / DIRECTION_SAMPLES;
                    Vec3 offset = new Vec3(Math.cos(angle) * distance, yOffset,
                        Math.sin(angle) * distance);
                    double score = scoreDestination(threat, playerCenter,
                        offset, attackAxis, currentHorizontal);
                    if (score == -Double.MAX_VALUE)
                        continue;
                    Vec3 destination = playerPos.add(offset);
                    if (!isSafeDestination(offset))
                        continue;
                    candidates.add(new DodgeCandidate(destination, score));
                }
            }

            if (yOffset != 0) {
                Vec3 offset = new Vec3(0, yOffset, 0);
                double score = scoreDestination(threat, playerCenter, offset,
                    attackAxis, currentHorizontal);
                if (score != -Double.MAX_VALUE && isSafeDestination(offset))
                    candidates.add(new DodgeCandidate(playerPos.add(offset), score));
            }
        }

        if (candidates.isEmpty())
            return null;
        candidates.sort(Comparator.comparingDouble(DodgeCandidate::score).reversed());
        int randomPool = Math.min(8, candidates.size());
        return candidates.get(ThreadLocalRandom.current().nextInt(randomPool)).destination;
    }

    private double scoreDestination(Threat threat, Vec3 playerCenter,
        Vec3 offset, Vec3 attackAxis, Vec3 currentHorizontal) {
        Vec3 dodgeAxis = offset.normalize();
        double alongAttack = attackAxis.lengthSqr() > 1.0E-6
            ? Math.abs(dodgeAxis.dot(attackAxis)) : 0;
        if (threat.type == ThreatType.SPEAR && alongAttack > 0.3)
            return -Double.MAX_VALUE;

        Vec3 destinationCenter = playerCenter.add(offset);
        if ((threat.type == ThreatType.SPACING || threat.type == ThreatType.MOB)
            && destinationCenter.distanceTo(threat.pathStart) < playerDistance.get())
            return -Double.MAX_VALUE;
        double pathSeparation = distanceToLine(destinationCenter, threat.pathStart, threat.pathEnd);
        double attackerSeparation = destinationCenter.distanceTo(threat.pathStart);
        double momentumBonus = currentHorizontal.lengthSqr() > 1.0E-6
            ? dodgeAxis.dot(currentHorizontal) * 0.25 : 0;
        double verticalCost = Math.abs(offset.y) * 0.15;
        double score = pathSeparation * 10 + offset.length() * 0.2
            + momentumBonus - verticalCost;
        if (threat.type == ThreatType.SPEAR)
            score += (1 - alongAttack) * 5;
        else
            score += attackerSeparation * 0.5;
        return score;
    }

    private boolean isSafeDestination(Vec3 offset) {
        AABB moved = mc.player.getBoundingBox().move(offset);
        if (!mc.level.noCollision(mc.player, moved))
            return false;
        return !avoidDrops.get() || !mc.player.onGround()
            || !mc.level.noCollision(mc.player, moved.move(0, -0.65, 0));
    }

    private void teleportAway(Threat threat) {
        teleportAway(threat, false);
    }

    private void teleportAway(Threat threat, boolean emergency) {
        if (mc.player == null || mc.getConnection() == null)
            return;
        if (!emergency && shouldSuppressDodging(threat.pathStart))
            return;
        Vec3 destination = chooseDodgeDestination(threat);
        if (destination == null)
            return;

        mc.player.setPos(destination.x, destination.y, destination.z);
        mc.player.setDeltaMovement(Vec3.ZERO);
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
            destination.x, destination.y, destination.z,
            mc.player.getYRot(), mc.player.getXRot(), false, false));
        for (int i = 1; i < teleportPackets.get(); i++) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                destination.x, destination.y, destination.z, false, false));
        }
        cooldownTicksLeft = teleportCooldown.get();
        statusTicksLeft = cooldownTicksLeft + 2;
    }

    private void rememberPositions() {
        previousPositions.clear();
        for (Player player : mc.level.players())
            if (player != mc.player)
                previousPositions.put(player.getId(), player.position());
    }

    private void rememberPrimedSpears() {
        long now = System.currentTimeMillis();
        for (Player player : mc.level.players()) {
            if (player == mc.player || isIgnoredPlayer(player) || !player.isUsingItem())
                continue;
            ItemStack spear = player.getActiveItem();
            if (isSpear(spear) && player.getUseItemRemainingTicks() >= getSpearReadyTicks(spear))
                primedSpearCues.put(player.getId(), now + PRIMED_SPEAR_MEMORY_MS);
        }
        primedSpearCues.values().removeIf(expiry -> expiry < now);
    }

    private void rememberChargingCreepers() {
        if (mc.level == null)
            return;

        long now = System.currentTimeMillis();
        for (Entity entity : mc.level.players()) {
            if (!(entity instanceof Creeper creeper) || !creeper.isAlive())
                continue;
            if (creeper.getSwellDir() > 0 || creeper.isIgnited() || creeper.isPowered())
                chargingCreeperCues.put(creeper.getId(), now + CHARGING_CREEPER_MEMORY_MS);
        }
        chargingCreeperCues.values().removeIf(expiry -> expiry < now);
    }

    private boolean wasRecentlyPrimed(Player player) {
        Long expiry = primedSpearCues.get(player.getId());
        if (expiry != null && expiry >= System.currentTimeMillis())
            return true;
        ItemStack spear = player.getActiveItem();
        return player.isUsingItem() && isSpear(spear)
            && player.getUseItemRemainingTicks() >= getSpearReadyTicks(spear);
    }

    private void prunePacketCues() {
        long cutoff = System.currentTimeMillis() - MACE_PACKET_CUE_MS;
        macePacketCues.values().removeIf(cue -> cue.timeMs < cutoff);
    }

    private Vec3 getObservedVelocity(Entity entity) {
        Vec3 networkVelocity = entity.getDeltaMovement();
        Vec3 previous = previousPositions.get(entity.getId());
        if (previous == null)
            return networkVelocity;
        Vec3 observed = entity.position().subtract(previous);
        return observed.lengthSqr() > networkVelocity.lengthSqr() ? observed
            : networkVelocity;
    }

    private boolean isWalkingToward(Vec3 targetPosition) {
        if (mc.options == null || mc.player == null)
            return false;
        if (movePauseMode.get() == MovePauseMode.ANY_MOVEMENT_KEY)
            return mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
        float forward = (mc.options.keyUp.isDown() ? 1 : 0)
            - (mc.options.keyDown.isDown() ? 1 : 0);
        float strafe = (mc.options.keyLeft.isDown() ? 1 : 0)
            - (mc.options.keyRight.isDown() ? 1 : 0);
        if (forward == 0 && strafe == 0)
            return false;

        double yaw = Math.toRadians(mc.player.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        Vec3 inputDirection = new Vec3(-sin * forward + cos * strafe, 0,
            cos * forward + sin * strafe).normalize();
        Vec3 toPlayer = new Vec3(targetPosition.x - mc.player.getX(), 0,
            targetPosition.z - mc.player.getZ());
        return toPlayer.lengthSqr() > 1.0E-6
            && inputDirection.dot(toPlayer.normalize()) > 0.55;
    }

    private boolean shouldSuppressDodging(Vec3 targetPosition) {
        if (movePauseMode.get() == MovePauseMode.ANY_MOVEMENT_KEY)
            return mc.options != null && (mc.options.keyUp.isDown()
                || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown()
                || mc.options.keyRight.isDown());
        return isWalkingToward(targetPosition);
    }

    private boolean isIgnoredPlayer(Player player) {
        return player != null && Friends.get().isFriend(player);
    }

    private boolean isHoldingMace(Player player) {
        return player.getMainHandItem().getItem() == Items.MACE
            || player.getOffhandItem().getItem() == Items.MACE;
    }

    private ItemStack getHeldSpear(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isSpear(mainHand))
            return mainHand;
        ItemStack offHand = player.getOffhandItem();
        return isSpear(offHand) ? offHand : ItemStack.EMPTY;
    }

    private boolean isSpear(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().toLowerCase(Locale.ROOT).contains("spear");
    }

    private static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().toLowerCase(Locale.ROOT).endsWith("_sword");
    }

    private static boolean isAxe(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && id.getPath().toLowerCase(Locale.ROOT).endsWith("_axe");
    }

    private int getSpearReadyTicks(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase(Locale.ROOT);
        if (name.contains("netherite"))
            return 7;
        if (name.contains("diamond"))
            return 9;
        if (name.contains("iron"))
            return 11;
        if (name.contains("copper"))
            return 12;
        if (name.contains("stone") || name.contains("golden"))
            return 13;
        return 14;
    }

    private static double distanceToLine(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 line = end.subtract(start);
        double lengthSq = line.lengthSqr();
        if (lengthSq < 1.0E-8)
            return point.distanceTo(start);
        double t = point.subtract(start).dot(line) / lengthSq;
        return point.distanceTo(start.add(line.scale(t)));
    }

    private static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSq = segment.lengthSqr();
        double t = lengthSq < 1.0E-8 ? 0 : Math.max(0,
            Math.min(1, point.subtract(start).dot(segment) / lengthSq));
        return point.distanceTo(start.add(segment.scale(t)));
    }

    private static double horizontalDistanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = new Vec3(end.x - start.x, 0, end.z - start.z);
        Vec3 offset = new Vec3(point.x - start.x, 0, point.z - start.z);
        double lengthSq = segment.lengthSqr();
        double t = lengthSq < 1.0E-8 ? 0
            : Math.max(0, Math.min(1, offset.dot(segment) / lengthSq));
        Vec3 closest = start.add(segment.scale(t));
        return horizontalLength(point.subtract(closest));
    }

    private static double verticalDistanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSq = segment.lengthSqr();
        double t = lengthSq < 1.0E-8 ? 0 : Math.max(0,
            Math.min(1, point.subtract(start).dot(segment) / lengthSq));
        return Math.abs(point.y - start.add(segment.scale(t)).y);
    }

    private static double horizontalDistanceSqr(Vec3 first, Vec3 second) {
        double x = first.x - second.x;
        double z = first.z - second.z;
        return x * x + z * z;
    }

    private static double horizontalDot(Vec3 first, Vec3 second) {
        return first.x * second.x + first.z * second.z;
    }

    private static double horizontalLength(Vec3 vector) {
        return Math.sqrt(vector.x * vector.x + vector.z * vector.z);
    }

    private static double square(double value) {
        return value * value;
    }

    private enum ThreatType {
        MACE("Mace!"),
        SPEAR("Spear!"),
        SWORD("Sword!"),
        AXE("Axe!"),
        ARROW("Arrow"),
        CRYSTAL("Crystal!"),
        MOB("Mob"),
        EMERGENCY("Emergency!"),
        SPACING("Spacing");

        private final String label;

        ThreatType(String label) {
            this.label = label;
        }
    }

    private record Threat(ThreatType type, Vec3 pathStart, Vec3 pathEnd, double urgency) {
    }

    private record MacePacketCue(Vec3 position, long timeMs) {
    }

    private record DodgeCandidate(Vec3 destination, double score) {
    }

    private enum WeaponType {
        SWORD(ThreatType.SWORD),
        AXE(ThreatType.AXE);

        private final ThreatType threatType;

        WeaponType(ThreatType threatType) {
            this.threatType = threatType;
        }

        private ItemStack getWeapon(Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (this == SWORD && isSword(mainHand) || this == AXE && isAxe(mainHand))
                return mainHand;
            ItemStack offHand = player.getOffhandItem();
            if (this == SWORD && isSword(offHand) || this == AXE && isAxe(offHand))
                return offHand;
            return ItemStack.EMPTY;
        }
    }
}
