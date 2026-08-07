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
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

    private final Map<Integer, Vec3d> previousPositions = new HashMap<>();
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
        if (packet instanceof BundleS2CPacket bundle) {
            for (Packet<?> subPacket : bundle.getPackets())
                inspectPacket(subPacket);
            return;
        }

        if (packet instanceof EntityAnimationS2CPacket animation
            && (animation.getAnimationId() == EntityAnimationS2CPacket.SWING_MAIN_HAND
            || animation.getAnimationId() == EntityAnimationS2CPacket.SWING_OFF_HAND)) {
            handleSwingPacket(animation.getEntityId());
            return;
        }

        if (packet instanceof EntityStatusS2CPacket entityEvent && entityEvent.getStatus() == 35) {
            handleEntityEvent(entityEvent);
            return;
        }

        if (packet instanceof EntityDamageS2CPacket damage) {
            handleDamageEvent(damage);
            return;
        }

        int id;
        Vec3d position;
        if (packet instanceof EntityPositionS2CPacket teleport) {
            id = teleport.entityId();
            position = teleport.change().position();
        } else if (packet instanceof EntityPositionSyncS2CPacket sync) {
            id = sync.id();
            position = sync.values().position();
        } else if (packet instanceof EntityS2CPacket move && move.isPositionChanged() && mc.level != null) {
            Entity movedEntity = move.getEntity(mc.level);
            if (movedEntity == null)
                return;
            id = movedEntity.getId();
            position = new Vec3d(movedEntity.getX() + move.getDeltaX() / 4096.0,
                movedEntity.getY() + move.getDeltaY() / 4096.0,
                movedEntity.getZ() + move.getDeltaZ() / 4096.0);
        } else
            return;

        if (mc.player == null || mc.level == null || position == null)
            return;

        handleIncomingPosition(id, position);

        Entity entity = mc.level.getEntityById(id);
        if (!(entity instanceof PlayerEntity player) || player == mc.player
            || isIgnoredPlayer(player) || !isHoldingMace(player))
            return;

        Vec3d oldPosition = entity.getEntityPos();
        boolean suddenRise = position.y - oldPosition.y >= 1.25;
        boolean aboveUs = position.y - mc.player.getY() >= 1.25;
        double horizontalSq = horizontalDistanceSqr(position, mc.player.getEntityPos());
        if (suddenRise && aboveUs && horizontalSq <= square(reachAllowance.get())) {
            macePacketCues.put(id, new MacePacketCue(position, System.currentTimeMillis()));
            tryImmediatePacketDodge();
        }
    }

    private void handleEntityEvent(EntityStatusS2CPacket entityEvent) {
        if (!isActive() || mc.player == null || mc.level == null)
            return;

        Entity entity = entityEvent.getEntity(mc.level);
        if (!(entity instanceof PlayerEntity player))
            return;

        if (player == mc.player && autoDistanceOnTotemPop.get())
            setKeepDistanceAlways();
    }

    private void handleDamageEvent(EntityDamageS2CPacket damage) {
        if (!isActive() || mc.player == null || mc.level == null
            || !autoDistanceOnDamage.get() || damage.entityId() != mc.player.getId())
            return;

        Entity source = mc.level.getEntityById(damage.sourceCauseId());
        if (!(source instanceof PlayerEntity attacker) || attacker == mc.player
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
        Entity entity = mc.level.getEntityById(entityId);
        if (!(entity instanceof PlayerEntity attacker) || attacker == mc.player
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

    private void handleIncomingPosition(int entityId, Vec3d incomingPosition) {
        if (!isActive() || mc.player == null || mc.level == null)
            return;
        Entity entity = mc.level.getEntityById(entityId);
        if (!(entity instanceof PlayerEntity attacker) || attacker == mc.player
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
        for (PlayerEntity attacker : mc.level.getPlayers()) {
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
                threat = getSpacingThreat(attacker, attacker.getEntityPos());
            }
            if (weaponThreat != null
                && (threat == null || weaponThreat.urgency > threat.urgency))
                threat = weaponThreat;

            if (threat != null && (best == null || threat.urgency > best.urgency))
                best = threat;
        }
        if (avoidHostileMobs.get()) {
            for (Entity entity : mc.level.getEntities()) {
                if (!(entity instanceof HostileEntity) || entity == mc.player
                    || !entity.isAlive())
                    continue;
                if (shouldSuppressDodging(entity.getEntityPos()))
                    continue;
                if (onlyChargingCreepers.get() && !isChargingCreeper(entity))
                    continue;

                Threat threat = getHostileMobThreat(entity);
                if (threat != null && (best == null || threat.urgency > best.urgency))
                    best = threat;
            }
        }
        if (avoidArrows.get()) {
            for (Entity entity : mc.level.getEntities()) {
                if (!(entity instanceof PersistentProjectileEntity arrow) || !arrow.isAlive()
                    || arrow.getOwner() == mc.player)
                    continue;
                Entity owner = arrow.getOwner();
                if (owner instanceof PlayerEntity player && isIgnoredPlayer(player))
                    continue;

                Threat threat = getArrowThreat(arrow);
                if (threat != null && (best == null || threat.urgency > best.urgency))
                    best = threat;
            }
        }
        if (avoidCrystals.get()) {
            for (Entity entity : mc.level.getEntities()) {
                if (!(entity instanceof EndCrystalEntity crystal) || !crystal.isAlive())
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
        for (PlayerEntity attacker : mc.level.getPlayers()) {
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
            Vec3d center = mc.player.getBoundingBox().getCenter();
            threat = new Threat(ThreatType.EMERGENCY, center, center.add(0, 0, 1), 1000);
        }
        activeThreat = ThreatType.EMERGENCY;
        teleportAway(threat, true);
    }

    private Threat getSpacingThreat(PlayerEntity attacker, Vec3d attackerPosition) {
        if (shouldSuppressDodging(attackerPosition))
            return null;
        Vec3d centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.getEntityPos());
        Vec3d attackerCenter = attackerPosition.add(centerOffset);
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        double distance = attackerCenter.distanceTo(playerCenter);
        if (distance >= playerDistance.get())
            return null;
        return new Threat(ThreatType.SPACING, attackerCenter, playerCenter,
            300 + (playerDistance.get() - distance) * 10);
    }

    private Threat getPacketSpearThreat(PlayerEntity attacker, Vec3d incomingPosition) {
        ItemStack spear = attacker.getActiveItem();
        if (!isSpear(spear))
            spear = getHeldSpear(attacker);
        if (!isSpear(spear))
            return null;
        if (onlyPrimedSpears.get()
            && !(attacker.isUsingItem() && attacker.getItemUseTime() >= getSpearReadyTicks(spear))
            && !wasRecentlyPrimed(attacker))
            return null;

        Vec3d centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.getEntityPos());
        Vec3d start = incomingPosition.add(centerOffset);
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d velocity = incomingPosition.subtract(attacker.getEntityPos());
        boolean primed = wasRecentlyPrimed(attacker) || attacker.isUsingItem()
            && attacker.getItemUseTime() >= getSpearReadyTicks(spear);
        if (!primed && velocity.length() < 1)
            return null;
        Vec3d toPlayer = playerCenter.subtract(start);
        if (velocity.lengthSquared() < 0.01 || velocity.dotProduct(toPlayer) <= 0) {
            Vec3d aim = attacker.getRotationVector();
            if (aim.dotProduct(toPlayer.normalize()) < 0.7)
                return null;
            velocity = aim.multiply(Math.max(0.75, velocity.length()));
        }

        double ticks = reactionTicks.get() + 2;
        double effectiveRange = Math.max(detectionRange.get(),
            velocity.length() * ticks + reachAllowance.get());
        if (start.squaredDistanceTo(playerCenter) > square(effectiveRange))
            return null;
        Vec3d end = start.add(velocity.multiply(ticks));
        if (distanceToSegment(playerCenter, start, end) > reachAllowance.get())
            return null;
        return new Threat(ThreatType.SPEAR, start, end, 500);
    }

    private Threat getSpearThreat(PlayerEntity attacker) {
        return getSpearThreat(attacker, false);
    }

    private Threat getSpearThreat(PlayerEntity attacker, boolean swingTriggered) {
        ItemStack spear = attacker.getActiveItem();
        boolean primedNow = attacker.isUsingItem() && isSpear(spear)
            && attacker.getItemUseTime() >= getSpearReadyTicks(spear);
        if (onlyPrimedSpears.get() && !primedNow && !wasRecentlyPrimed(attacker))
            return null;
        if (!primedNow && !swingTriggered)
            return null;
        if (!isSpear(spear))
            spear = getHeldSpear(attacker);
        if (!isSpear(spear))
            return null;

        Vec3d start = attacker.getBoundingBox().getCenter();
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d velocity = getObservedVelocity(attacker);
        double horizontalSpeed = horizontalLength(velocity);
        Vec3d toPlayer = playerCenter.subtract(start);
        double ticks = reactionTicks.get();
        double speedScaledRange = velocity.length() * (ticks + 2) + reachAllowance.get();
        double effectiveRange = Math.max(detectionRange.get(), speedScaledRange);
        if (start.squaredDistanceTo(playerCenter) > square(effectiveRange))
            return null;

        boolean movingAtUs = horizontalSpeed >= 0.12 && horizontalDot(toPlayer, velocity) > 0;
        Vec3d attackVelocity = velocity;
        if (!movingAtUs) {
            Vec3d aim = attacker.getRotationVector();
            double distance = toPlayer.length();
            double aimDot = distance < 1.0E-6 ? 1 : aim.dotProduct(toPlayer.multiply(1 / distance));
            double armedRange = reachAllowance.get() + ticks * 0.75;
            if (aimDot < 0.75 || distance > armedRange)
                return null;

            attackVelocity = aim.multiply(Math.max(0.75, velocity.length()));
            horizontalSpeed = Math.max(0.12, horizontalLength(attackVelocity));
        }

        Vec3d end = start.add(attackVelocity.multiply(ticks));
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

    private Threat getPacketMeleeThreat(PlayerEntity attacker, Vec3d incomingPosition,
        WeaponType weaponType) {
        ItemStack weapon = weaponType.getWeapon(attacker);
        if (weapon.isEmpty())
            return null;

        Vec3d centerOffset = attacker.getBoundingBox().getCenter().subtract(attacker.getEntityPos());
        Vec3d start = incomingPosition.add(centerOffset);
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d velocity = incomingPosition.subtract(attacker.getEntityPos());
        Vec3d toPlayer = playerCenter.subtract(start);
        double distance = toPlayer.length();
        double ticks = reactionTicks.get() + 2;
        double weaponReach = weaponType == WeaponType.AXE ? 4.0 : 3.5;
        double effectiveRange = Math.max(detectionRange.get(), weaponReach
            + velocity.length() * ticks + reachAllowance.get());
        if (distance > effectiveRange)
            return null;

        Vec3d attackVelocity = velocity;
        if (attackVelocity.lengthSquared() < 0.01
            || attackVelocity.dotProduct(toPlayer) <= 0) {
            Vec3d aim = attacker.getRotationVector();
            if (distance > 1.0E-6 && aim.dotProduct(toPlayer.normalize()) < 0.5)
                return null;
            attackVelocity = aim.multiply(Math.max(0.5, velocity.length()));
        }

        Vec3d end = start.add(attackVelocity.multiply(ticks));
        double missDistance = distanceToSegment(playerCenter, start, end);
        double closeRange = weaponReach + 0.75;
        if (missDistance > reachAllowance.get() && distance > closeRange)
            return null;

        double urgency = (weaponType == WeaponType.AXE ? 95 : 90)
            + Math.max(0, closeRange - distance) * 10
            + (attacker.isUsingItem() ? 20 : 0);
        return new Threat(weaponType.threatType, start, end, urgency);
    }

    private Threat getMeleeThreat(PlayerEntity attacker, WeaponType weaponType,
        boolean swingTriggered) {
        ItemStack weapon = weaponType.getWeapon(attacker);
        if (weapon.isEmpty())
            return null;

        Vec3d start = attacker.getBoundingBox().getCenter();
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d toPlayer = playerCenter.subtract(start);
        double distance = toPlayer.length();
        double horizontalDistance = horizontalLength(toPlayer);
        double weaponReach = weaponType == WeaponType.AXE ? 4.0 : 3.5;
        double ticks = reactionTicks.get();
        double speed = horizontalLength(getObservedVelocity(attacker));
        double effectiveRange = Math.max(detectionRange.get(),
            weaponReach + speed * (ticks + 2) + reachAllowance.get());
        if (distance > effectiveRange)
            return null;

        Vec3d velocity = getObservedVelocity(attacker);
        boolean movingAtUs = horizontalLength(velocity) >= 0.08
            && horizontalDot(toPlayer, velocity) > 0;
        Vec3d aim = attacker.getRotationVector();
        double aimDot = distance < 1.0E-6 ? 1 : aim.dotProduct(toPlayer.multiply(1 / distance));
        boolean closeEnough = horizontalDistance <= weaponReach + 0.9;
        boolean armed = swingTriggered || movingAtUs || aimDot > 0.55 || closeEnough;
        if (!armed)
            return null;

        Vec3d attackDirection = movingAtUs ? velocity : aim;
        if (attackDirection.lengthSquared() < 1.0E-6)
            attackDirection = toPlayer.normalize();
        Vec3d end = start.add(attackDirection.multiply(ticks + 1));
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

    private Threat getMaceThreat(PlayerEntity attacker) {
        if (!isHoldingMace(attacker))
            return null;

        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d attackerCenter = attacker.getBoundingBox().getCenter();
        double horizontalDistance = horizontalLength(attackerCenter.subtract(playerCenter));
        double armedRadius = reachAllowance.get() + 2 + armedRadiusBonus.get();
        if (horizontalDistance > armedRadius)
            return null;

        MacePacketCue cue = macePacketCues.get(attacker.getId());
        boolean packetSpoof = cue != null
            && System.currentTimeMillis() - cue.timeMs <= MACE_PACKET_CUE_MS;
        Vec3d velocity = getObservedVelocity(attacker);
        boolean fallingAbove = attackerCenter.y - playerCenter.y >= 1.25
            && (velocity.y < -0.08 || attacker.fallDistance >= 1.25F);
        Vec3d toPlayer = playerCenter.subtract(attackerCenter);
        double aimDot = toPlayer.lengthSquared() < 1.0E-6 ? 1
            : attacker.getRotationVector().dotProduct(toPlayer.normalize());
        boolean closing = horizontalDot(toPlayer, velocity) > 0.01;
        boolean inReach = horizontalDistance <= reachAllowance.get();
        boolean preparingAttack = inReach || aimDot > 0.55 || closing;
        if (!packetSpoof && !fallingAbove && !preparingAttack)
            return null;

        Vec3d pathStart = packetSpoof ? cue.position : attackerCenter;
        Vec3d pathEnd = new Vec3d(attackerCenter.x, playerCenter.y, attackerCenter.z);
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

        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d mobCenter = mob.getBoundingBox().getCenter();
        double distance = mobCenter.distanceTo(playerCenter);
        if (distance >= playerDistance.get())
            return null;

        return new Threat(ThreatType.MOB, mobCenter, playerCenter,
            250 + (playerDistance.get() - distance) * 10);
    }

    private Threat getArrowThreat(PersistentProjectileEntity arrow) {
        if (mc.player == null || mc.level == null)
            return null;

        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d arrowCenter = arrow.getBoundingBox().getCenter();
        Vec3d velocity = getObservedVelocity(arrow);
        if (velocity.lengthSquared() < 0.01)
            velocity = arrow.getDeltaMovement();
        if (velocity.lengthSquared() < 0.01)
            return null;

        Vec3d toPlayer = playerCenter.subtract(arrowCenter);
        if (velocity.dotProduct(toPlayer) <= 0)
            return null;

        double ticks = reactionTicks.get() + 2;
        Vec3d end = arrowCenter.add(velocity.multiply(ticks));
        double missDistance = distanceToSegment(playerCenter, arrowCenter, end);
        double effectiveRange = Math.max(detectionRange.get(),
            velocity.length() * ticks + reachAllowance.get());
        if (arrowCenter.squaredDistanceTo(playerCenter) > square(effectiveRange)
            || missDistance > reachAllowance.get() + 0.75)
            return null;

        double urgency = 110 + Math.max(0, 6 - arrowCenter.distanceTo(playerCenter)) * 12
            - missDistance * 5;
        return new Threat(ThreatType.ARROW, arrowCenter, end, urgency);
    }

    private Threat getCrystalThreat(EndCrystalEntity crystal) {
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d crystalCenter = crystal.getBoundingBox().getCenter();
        double distance = crystalCenter.distanceTo(playerCenter);
        double radius = Math.max(detectionRange.get(), 6);
        if (distance > radius)
            return null;

        double urgency = 260 + Math.max(0, radius - distance) * 20;
        return new Threat(ThreatType.CRYSTAL, crystalCenter, playerCenter, urgency);
    }

    private boolean isChargingCreeper(Entity entity) {
        return entity instanceof CreeperEntity creeper && isChargingCreeper(creeper);
    }

    private boolean isChargingCreeper(CreeperEntity creeper) {
        if (creeper == null || !creeper.isAlive())
            return false;

        if (creeper.getFuseSpeed() > 0 || creeper.isIgnited() || creeper.isCharged())
            return true;

        Long expiry = chargingCreeperCues.get(creeper.getId());
        return expiry != null && expiry >= System.currentTimeMillis();
    }

    private Vec3d chooseDodgeDestination(Threat threat) {
        Vec3d playerPos = mc.player.getEntityPos();
        Vec3d playerCenter = mc.player.getBoundingBox().getCenter();
        Vec3d attackAxis = threat.pathEnd.subtract(threat.pathStart);
        if (attackAxis.lengthSquared() > 1.0E-6)
            attackAxis = attackAxis.normalize();
        Vec3d currentVelocity = mc.player.getDeltaMovement();
        Vec3d currentHorizontal = new Vec3d(currentVelocity.x, 0, currentVelocity.z);
        if (currentHorizontal.lengthSquared() > 1.0E-6)
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
                    Vec3d offset = new Vec3d(Math.cos(angle) * distance, yOffset,
                        Math.sin(angle) * distance);
                    double score = scoreDestination(threat, playerCenter,
                        offset, attackAxis, currentHorizontal);
                    if (score == -Double.MAX_VALUE)
                        continue;
                    Vec3d destination = playerPos.add(offset);
                    if (!isSafeDestination(offset))
                        continue;
                    candidates.add(new DodgeCandidate(destination, score));
                }
            }

            if (yOffset != 0) {
                Vec3d offset = new Vec3d(0, yOffset, 0);
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

    private double scoreDestination(Threat threat, Vec3d playerCenter,
        Vec3d offset, Vec3d attackAxis, Vec3d currentHorizontal) {
        Vec3d dodgeAxis = offset.normalize();
        double alongAttack = attackAxis.lengthSquared() > 1.0E-6
            ? Math.abs(dodgeAxis.dotProduct(attackAxis)) : 0;
        if (threat.type == ThreatType.SPEAR && alongAttack > 0.3)
            return -Double.MAX_VALUE;

        Vec3d destinationCenter = playerCenter.add(offset);
        if ((threat.type == ThreatType.SPACING || threat.type == ThreatType.MOB)
            && destinationCenter.distanceTo(threat.pathStart) < playerDistance.get())
            return -Double.MAX_VALUE;
        double pathSeparation = distanceToLine(destinationCenter, threat.pathStart, threat.pathEnd);
        double attackerSeparation = destinationCenter.distanceTo(threat.pathStart);
        double momentumBonus = currentHorizontal.lengthSquared() > 1.0E-6
            ? dodgeAxis.dotProduct(currentHorizontal) * 0.25 : 0;
        double verticalCost = Math.abs(offset.y) * 0.15;
        double score = pathSeparation * 10 + offset.length() * 0.2
            + momentumBonus - verticalCost;
        if (threat.type == ThreatType.SPEAR)
            score += (1 - alongAttack) * 5;
        else
            score += attackerSeparation * 0.5;
        return score;
    }

    private boolean isSafeDestination(Vec3d offset) {
        Box moved = mc.player.getBoundingBox().offset(offset);
        if (!mc.level.isSpaceEmpty(mc.player, moved))
            return false;
        return !avoidDrops.get() || !mc.player.isOnGround()
            || !mc.level.isSpaceEmpty(mc.player, moved.offset(0, -0.65, 0));
    }

    private void teleportAway(Threat threat) {
        teleportAway(threat, false);
    }

    private void teleportAway(Threat threat, boolean emergency) {
        if (mc.player == null || mc.getConnection() == null)
            return;
        if (!emergency && shouldSuppressDodging(threat.pathStart))
            return;
        Vec3d destination = chooseDodgeDestination(threat);
        if (destination == null)
            return;

        mc.player.setPosition(destination.x, destination.y, destination.z);
        mc.player.setDeltaMovement(Vec3d.ZERO);
        mc.getConnection().sendPacket(new PlayerMoveC2SPacket.Full(
            destination.x, destination.y, destination.z,
            mc.player.getYRot(), mc.player.getXRot(), false, false));
        for (int i = 1; i < teleportPackets.get(); i++) {
            mc.getConnection().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                destination.x, destination.y, destination.z, false, false));
        }
        cooldownTicksLeft = teleportCooldown.get();
        statusTicksLeft = cooldownTicksLeft + 2;
    }

    private void rememberPositions() {
        previousPositions.clear();
        for (PlayerEntity player : mc.level.getPlayers())
            if (player != mc.player)
                previousPositions.put(player.getId(), player.getEntityPos());
    }

    private void rememberPrimedSpears() {
        long now = System.currentTimeMillis();
        for (PlayerEntity player : mc.level.getPlayers()) {
            if (player == mc.player || isIgnoredPlayer(player) || !player.isUsingItem())
                continue;
            ItemStack spear = player.getActiveItem();
            if (isSpear(spear) && player.getItemUseTime() >= getSpearReadyTicks(spear))
                primedSpearCues.put(player.getId(), now + PRIMED_SPEAR_MEMORY_MS);
        }
        primedSpearCues.values().removeIf(expiry -> expiry < now);
    }

    private void rememberChargingCreepers() {
        if (mc.level == null)
            return;

        long now = System.currentTimeMillis();
        for (Entity entity : mc.level.getEntities()) {
            if (!(entity instanceof CreeperEntity creeper) || !creeper.isAlive())
                continue;
            if (creeper.getFuseSpeed() > 0 || creeper.isIgnited() || creeper.isCharged())
                chargingCreeperCues.put(creeper.getId(), now + CHARGING_CREEPER_MEMORY_MS);
        }
        chargingCreeperCues.values().removeIf(expiry -> expiry < now);
    }

    private boolean wasRecentlyPrimed(PlayerEntity player) {
        Long expiry = primedSpearCues.get(player.getId());
        if (expiry != null && expiry >= System.currentTimeMillis())
            return true;
        ItemStack spear = player.getActiveItem();
        return player.isUsingItem() && isSpear(spear)
            && player.getItemUseTime() >= getSpearReadyTicks(spear);
    }

    private void prunePacketCues() {
        long cutoff = System.currentTimeMillis() - MACE_PACKET_CUE_MS;
        macePacketCues.values().removeIf(cue -> cue.timeMs < cutoff);
    }

    private Vec3d getObservedVelocity(Entity entity) {
        Vec3d networkVelocity = entity.getDeltaMovement();
        Vec3d previous = previousPositions.get(entity.getId());
        if (previous == null)
            return networkVelocity;
        Vec3d observed = entity.getEntityPos().subtract(previous);
        return observed.lengthSquared() > networkVelocity.lengthSquared() ? observed
            : networkVelocity;
    }

    private boolean isWalkingToward(Vec3d targetPosition) {
        if (mc.options == null || mc.player == null)
            return false;
        if (movePauseMode.get() == MovePauseMode.ANY_MOVEMENT_KEY)
            return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
        float forward = (mc.options.forwardKey.isPressed() ? 1 : 0)
            - (mc.options.backKey.isPressed() ? 1 : 0);
        float strafe = (mc.options.leftKey.isPressed() ? 1 : 0)
            - (mc.options.rightKey.isPressed() ? 1 : 0);
        if (forward == 0 && strafe == 0)
            return false;

        double yaw = Math.toRadians(mc.player.getYRot());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        Vec3d inputDirection = new Vec3d(-sin * forward + cos * strafe, 0,
            cos * forward + sin * strafe).normalize();
        Vec3d toPlayer = new Vec3d(targetPosition.x - mc.player.getX(), 0,
            targetPosition.z - mc.player.getZ());
        return toPlayer.lengthSquared() > 1.0E-6
            && inputDirection.dotProduct(toPlayer.normalize()) > 0.55;
    }

    private boolean shouldSuppressDodging(Vec3d targetPosition) {
        if (movePauseMode.get() == MovePauseMode.ANY_MOVEMENT_KEY)
            return mc.options != null && (mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed());
        return isWalkingToward(targetPosition);
    }

    private boolean isIgnoredPlayer(PlayerEntity player) {
        return player != null && Friends.get().isFriend(player);
    }

    private boolean isHoldingMace(PlayerEntity player) {
        return player.getMainItemStack().isOf(Items.MACE)
            || player.getOffhandItem().isOf(Items.MACE);
    }

    private ItemStack getHeldSpear(PlayerEntity player) {
        ItemStack mainHand = player.getMainItemStack();
        if (isSpear(mainHand))
            return mainHand;
        ItemStack offHand = player.getOffhandItem();
        return isSpear(offHand) ? offHand : ItemStack.EMPTY;
    }

    private boolean isSpear(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id != null && id.getPath().toLowerCase(Locale.ROOT).contains("spear");
    }

    private static boolean isSword(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return id != null && id.getPath().toLowerCase(Locale.ROOT).endsWith("_sword");
    }

    private static boolean isAxe(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
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

    private static double distanceToLine(Vec3d point, Vec3d start, Vec3d end) {
        Vec3d line = end.subtract(start);
        double lengthSq = line.lengthSquared();
        if (lengthSq < 1.0E-8)
            return point.distanceTo(start);
        double t = point.subtract(start).dotProduct(line) / lengthSq;
        return point.distanceTo(start.add(line.multiply(t)));
    }

    private static double distanceToSegment(Vec3d point, Vec3d start, Vec3d end) {
        Vec3d segment = end.subtract(start);
        double lengthSq = segment.lengthSquared();
        double t = lengthSq < 1.0E-8 ? 0 : Math.max(0,
            Math.min(1, point.subtract(start).dotProduct(segment) / lengthSq));
        return point.distanceTo(start.add(segment.multiply(t)));
    }

    private static double horizontalDistanceToSegment(Vec3d point, Vec3d start, Vec3d end) {
        Vec3d segment = new Vec3d(end.x - start.x, 0, end.z - start.z);
        Vec3d offset = new Vec3d(point.x - start.x, 0, point.z - start.z);
        double lengthSq = segment.lengthSquared();
        double t = lengthSq < 1.0E-8 ? 0
            : Math.max(0, Math.min(1, offset.dotProduct(segment) / lengthSq));
        Vec3d closest = start.add(segment.multiply(t));
        return horizontalLength(point.subtract(closest));
    }

    private static double verticalDistanceToSegment(Vec3d point, Vec3d start, Vec3d end) {
        Vec3d segment = end.subtract(start);
        double lengthSq = segment.lengthSquared();
        double t = lengthSq < 1.0E-8 ? 0 : Math.max(0,
            Math.min(1, point.subtract(start).dotProduct(segment) / lengthSq));
        return Math.abs(point.y - start.add(segment.multiply(t)).y);
    }

    private static double horizontalDistanceSqr(Vec3d first, Vec3d second) {
        double x = first.x - second.x;
        double z = first.z - second.z;
        return x * x + z * z;
    }

    private static double horizontalDot(Vec3d first, Vec3d second) {
        return first.x * second.x + first.z * second.z;
    }

    private static double horizontalLength(Vec3d vector) {
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

    private record Threat(ThreatType type, Vec3d pathStart, Vec3d pathEnd, double urgency) {
    }

    private record MacePacketCue(Vec3d position, long timeMs) {
    }

    private record DodgeCandidate(Vec3d destination, double score) {
    }

    private enum WeaponType {
        SWORD(ThreatType.SWORD),
        AXE(ThreatType.AXE);

        private final ThreatType threatType;

        WeaponType(ThreatType threatType) {
            this.threatType = threatType;
        }

        private ItemStack getWeapon(PlayerEntity player) {
            ItemStack mainHand = player.getMainItemStack();
            if (this == SWORD && isSword(mainHand) || this == AXE && isAxe(mainHand))
                return mainHand;
            ItemStack offHand = player.getOffhandItem();
            if (this == SWORD && isSword(offHand) || this == AXE && isAxe(offHand))
                return offHand;
            return ItemStack.EMPTY;
        }
    }
}
