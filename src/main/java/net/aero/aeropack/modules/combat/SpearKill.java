package net.aero.aeropack.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.*;

public class SpearKill extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBlink = settings.createGroup("Blink Options");
    private final SettingGroup sgLunge = settings.createGroup("Lunge Options");
    private final SettingGroup sgBlinkLunge = settings.createGroup("Blink Lunge Options");

    // --- General ---

    private final Setting<Boolean> disableNoFall = sgGeneral.add(new BoolSetting.Builder()
        .name("Disable NoFall While Charging")
        .description("Prevents fall damage when lunging downward quickly.")
        .defaultValue(true)
        .build()
    );

    public enum Mode {
        Lunge,
        Blink
    }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Lunge = velocity boost, Blink = packet delay.")
        .defaultValue(Mode.Lunge)
        .build()
    );

    public final Setting<Double> maxRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Targeting Range")
        .description("How far in blocks entities can still be targeted.")
        .defaultValue(256.0)
        .min(0.0)
        .sliderRange(0.0, 512.0)
        .build()
    );

    public enum TargetListMode {
        Whitelist,
        Blacklist
    }

    private final Setting<TargetListMode> targetListMode = sgGeneral.add(new EnumSetting.Builder<TargetListMode>()
        .name("Target List Mode")
        .description("Whitelist = only target these entities, Blacklist = don't target these entities.")
        .defaultValue(TargetListMode.Blacklist)
        .build()
    );

    private final Setting<Set<EntityType<?>>> targetEntities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("Target Entities")
        .description("Entities to whitelist or blacklist.")
        .onlyAttackable()
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description("Do not spear kill friends.")
        .defaultValue(true)
        .build()
    );

    // --- Blink Lunge Options ---

    private final Setting<Boolean> blinkLunge = sgBlinkLunge.add(new BoolSetting.Builder()
        .name("Blink + Lunge")
        .description("Combine Blink mode with velocity based lunging.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Blink)
        .build()
    );

    private final Setting<Double> blinkLungeStrength = sgBlinkLunge.add(new DoubleSetting.Builder()
        .name("Lunge Strength")
        .description("Velocity applied towards target.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderRange(0.1, 2.0)
        .visible(() -> mode.get() == Mode.Blink && blinkLunge.get())
        .build()
    );

    private final Setting<Integer> blinkLungeTicks = sgBlinkLunge.add(new IntSetting.Builder()
        .name("Lunge Delay")
        .description("Ticks to charge before lunging.")
        .defaultValue(15)
        .min(1)
        .sliderRange(1, 30)
        .visible(() -> mode.get() == Mode.Blink && blinkLunge.get())
        .build()
    );

    // --- Blink Options ---

    private final Setting<Double> flushRange = sgBlink.add(new DoubleSetting.Builder()
        .name("Flush Range")
        .description("Distance to target when flush occurs (blocks).")
        .defaultValue(3.0)
        .min(1.0)
        .sliderRange(1.0, 10.0)
        .visible(() -> mode.get() == Mode.Blink)
        .build()
    );

    private final Setting<Double> maxFlushRange = sgBlink.add(new DoubleSetting.Builder()
        .name("Force Flush Distance")
        .description("Distance to force a flush.")
        .defaultValue(9.5)
        .min(1.0)
        .sliderRange(1.0, 20.0)
        .visible(() -> mode.get() == Mode.Blink && !blinkLunge.get())
        .build()
    );

    private final Setting<Boolean> blinkAimbot = sgBlink.add(new BoolSetting.Builder()
        .name("Aimbot")
        .description("Lock on to target.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Blink)
        .build()
    );

    private final Setting<Double> blinkDistanceBoost = sgBlink.add(new DoubleSetting.Builder()
        .name("Distance Boost")
        .description("Extra blocks to add to start position (extends the travel distance).")
        .defaultValue(0.0)
        .min(0.0)
        .sliderRange(0.0, 10.0)
        .visible(() -> mode.get() == Mode.Blink)
        .build()
    );

    // --- Lunge Options ---

    public enum LungeDirectionMode {
        DirectionBased,
        FromAbove,
        Auto_FromAboveFirst
    }

    private final Setting<LungeDirectionMode> lungeDirectionMode = sgLunge.add(new EnumSetting.Builder<LungeDirectionMode>()
        .name("Lunge Direction")
        .description("DirectionBased = toward look direction, FromAbove = stab from above, Auto_FromAboveFirst = stab from above unless path is invalid.")
        .defaultValue(LungeDirectionMode.DirectionBased)
        .visible(() -> mode.get() == Mode.Lunge)
        .build()
    );

    private final Setting<Double> aboveHeight = sgLunge.add(new DoubleSetting.Builder()
        .name("Above Height")
        .description("Blocks above target center.")
        .defaultValue(10.0)
        .min(5.0)
        .sliderRange(5.0, 50.0)
        .visible(() -> mode.get() == Mode.Lunge
            && (lungeDirectionMode.get() == LungeDirectionMode.FromAbove
                || lungeDirectionMode.get() == LungeDirectionMode.Auto_FromAboveFirst))
        .build()
    );

    private final Setting<Double> aboveHeightTriggerDistance = sgLunge.add(new DoubleSetting.Builder()
        .name("Above Height Trigger Distance")
        .description("If within this distance of the above target position, start the lunge toward the target.")
        .defaultValue(3.0)
        .min(1.0)
        .sliderRange(1.0, 10.0)
        .visible(() -> mode.get() == Mode.Lunge
            && (lungeDirectionMode.get() == LungeDirectionMode.FromAbove
                || lungeDirectionMode.get() == LungeDirectionMode.Auto_FromAboveFirst))
        .build()
    );

    private final Setting<Boolean> checkDistanceInvalid = sgLunge.add(new BoolSetting.Builder()
        .name("Trigger Distance Validation")
        .description("Checks if the area around the above position is valid using a radius of Above Height Trigger Distance.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Lunge && lungeDirectionMode.get() == LungeDirectionMode.Auto_FromAboveFirst)
        .build()
    );

    public final Setting<Double> lungeStrength = sgLunge.add(new DoubleSetting.Builder()
        .name("Spear Velocity")
        .description("The amount of velocity applied to the player, in the direction of the target.")
        .defaultValue(5.0)
        .min(0.0)
        .sliderRange(1.0, 10.0)
        .visible(() -> mode.get() == Mode.Lunge)
        .build()
    );

    private final Setting<Boolean> stopOnTarget = sgLunge.add(new BoolSetting.Builder()
        .name("Stop On Target")
        .description("Stops the lunge when you reach the target.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Lunge)
        .build()
    );

    public final Setting<Double> stopDistance = sgLunge.add(new DoubleSetting.Builder()
        .name("Stop Distance")
        .description("Distance between your hitbox and the entity's hitbox to attempt to stop at.")
        .defaultValue(2.0)
        .min(0.0)
        .sliderRange(0.0, 10.0)
        .visible(() -> stopOnTarget.get() && mode.get() == Mode.Lunge)
        .build()
    );

    private final Setting<Integer> lungeDelayModifier = sgLunge.add(new IntSetting.Builder()
        .name("Lunge Delay Modifier")
        .description("Wait percentage of time until spear is ready before lunge.")
        .defaultValue(100)
        .min(0)
        .sliderRange(0, 100)
        .visible(() -> mode.get() == Mode.Lunge)
        .build()
    );

    // --- State ---

    private final List<ServerboundMovePlayerPacket> packets = new ArrayList<>();
    private boolean isBlinking = false;
    private boolean isFlushing = false;
    private Vec3 startPos = null;
    private boolean wasCharging = false;
    private double lastTargetDistance = Double.MAX_VALUE;
    private boolean wasApproaching = false;
    private Entity killTarget;
    private int blinkChargeTicks = 0;
    private int flushCooldown = 0;
    private boolean firstPhase = false;
    private Vec3 aboveTargetPos = null;
    private boolean wasNoFallEnabled = false;
    private boolean noFallToggled = false;
    private boolean currentlyCharging = false;

    public SpearKill() {
        super(Categories.Combat, "Spear Kill", "Increases spear damage! Lunge mode uses velocity and Blink mode delays packets. Ported from Trouser Streak by etianl (Lunge) and Kimtaeho (Blink).");
    }

    @Override
    public void onActivate() {
        resetState();
    }

    @Override
    public void onDeactivate() {
        flushPackets();
        resetState();
    }

    private void resetState() {
        synchronized (packets) { packets.clear(); }
        isBlinking = false;
        isFlushing = false;
        startPos = null;
        wasCharging = false;
        lastTargetDistance = Double.MAX_VALUE;
        wasApproaching = false;
        killTarget = null;
        blinkChargeTicks = 0;
        flushCooldown = 0;
        firstPhase = false;
        aboveTargetPos = null;
        if (noFallToggled && wasNoFallEnabled) {
            Modules.get().get(NoFall.class).toggle();
        }
        noFallToggled = false;
        wasNoFallEnabled = false;
    }

    private boolean isUsingSpear() {
        if (mc.player == null) return false;
        String itemName = mc.player.getActiveItem().getItem().toString().toLowerCase();
        return itemName.contains("spear");
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        currentlyCharging = isUsingSpear();

        if (disableNoFall.get()) {
            if (currentlyCharging && !noFallToggled) {
                wasNoFallEnabled = Modules.get().get(NoFall.class).isActive();
                if (wasNoFallEnabled) {
                    Modules.get().get(NoFall.class).toggle();
                    noFallToggled = true;
                }
            } else if (!currentlyCharging && noFallToggled) {
                if (wasNoFallEnabled) Modules.get().get(NoFall.class).toggle();
                noFallToggled = false;
            }
        }

        if (mode.get() == Mode.Lunge) return;

        if (currentlyCharging) {
            blinkChargeTicks++;
            if (killTarget == null || !killTarget.isAlive() || !canSeeTarget(killTarget)) {
                killTarget = target();
                lastTargetDistance = killTarget != null ? mc.player.distanceTo(killTarget) : Double.MAX_VALUE;
                wasApproaching = false;
            }
        } else {
            blinkChargeTicks = 0;
            killTarget = null;
            lastTargetDistance = Double.MAX_VALUE;
            wasApproaching = false;
        }

        if (flushCooldown > 0) flushCooldown--;

        if (currentlyCharging && !wasCharging) startBlink();

        if (!currentlyCharging && wasCharging && isBlinking) {
            if (killTarget != null) rotateToTarget(killTarget);
            flushPackets();
            isBlinking = false;
            startPos = null;
        }

        wasCharging = currentlyCharging;

        if (isBlinking && killTarget != null && currentlyCharging) {
            double currentDistance = mc.player.distanceTo(killTarget);
            boolean isApproaching = currentDistance < lastTargetDistance;
            boolean shouldFlush = false;

            if (currentDistance <= flushRange.get()) {
                shouldFlush = true;
            } else if (wasApproaching && !isApproaching && currentDistance < 8.0) {
                shouldFlush = true;
            } else if (!blinkLunge.get() && startPos != null
                && mc.player.position().distanceTo(startPos) >= maxFlushRange.get()) {
                flushPackets();
                startBlink();
            }

            if (shouldFlush) {
                rotateToTarget(killTarget);
                flushPackets();
                if (blinkLunge.get()) flushCooldown = blinkLungeTicks.get();

                isBlinking = true;
                startPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                synchronized (packets) { packets.clear(); }
                lastTargetDistance = mc.player.distanceTo(killTarget);
                wasApproaching = false;
            } else {
                lastTargetDistance = currentDistance;
                wasApproaching = isApproaching;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mode.get() == Mode.Lunge) {
            if (currentlyCharging) {
                if (killTarget == null) killTarget = target();
                if (killTarget != null && !killTarget.isAlive()) {
                    if (stopOnTarget.get()) {
                        mc.player.setDeltaMovement(0, 0, 0);
                        mc.player.setSprinting(false);
                    }
                    killTarget = null;
                    firstPhase = false;
                    aboveTargetPos = null;
                }
                if (killTarget == null || !(killTarget instanceof LivingEntity)) return;
                if (!isValidTarget(killTarget)) return;
                doLunge();
            } else {
                killTarget = null;
                firstPhase = false;
                aboveTargetPos = null;
            }
            return;
        }

        if (currentlyCharging && blinkAimbot.get() && killTarget != null) {
            rotateToTarget(killTarget);
        }

        if (currentlyCharging && blinkLunge.get() && killTarget != null && flushCooldown == 0) {
            if (blinkChargeTicks >= blinkLungeTicks.get()) {
                rotateToTarget(killTarget);
                Vec3 viewDir = Vec3.directionFromRotation(mc.player.getXRot(), mc.player.getYRot());
                mc.player.setSprinting(true);
                mc.player.setDeltaMovement(viewDir.scale(blinkLungeStrength.get()));
            }
        }
    }

    private void doLunge() {
        int readyTicks = mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND
            ? getReadyTicks(mc.player.getMainHandItem().getItem())
            : getReadyTicks(mc.player.getOffhandItem().getItem());

        rotateToTarget(killTarget);

        if (mc.player.getUseItemRemainingTicks() > readyTicks) {
            AABB playerBox = mc.player.getBoundingBox().inflate(stopDistance.get());
            AABB targetBox = killTarget.getBoundingBox();
            boolean atTarget = playerBox.intersects(targetBox);

            if (atTarget) {
                if (stopOnTarget.get()) {
                    killTarget = null;
                    mc.player.setDeltaMovement(0, 0, 0);
                    mc.player.setSprinting(false);
                }
                firstPhase = false;
                aboveTargetPos = null;
                return;
            }

            double speed = lungeStrength.get();
            Vec3 viewDir;

            if (killTarget == null) return;

            switch (lungeDirectionMode.get()) {
                case DirectionBased -> {
                    viewDir = killTarget.getBoundingBox().getCenter().subtract(mc.player.position()).normalize();
                    mc.player.setSprinting(true);
                    mc.player.setDeltaMovement(viewDir.scale(speed));
                }
                case FromAbove -> {
                    if (!firstPhase || aboveTargetPos == null) {
                        Vec3 tc = killTarget.getBoundingBox().getCenter();
                        aboveTargetPos = new Vec3(tc.x, tc.y + aboveHeight.get(), tc.z);
                        firstPhase = true;
                    }
                    Vec3 pp = mc.player.position();
                    double distToAbove = pp.distanceTo(aboveTargetPos);
                    if (distToAbove < aboveHeightTriggerDistance.get()) {
                        viewDir = killTarget.getBoundingBox().getCenter().subtract(pp).normalize();
                        firstPhase = false;
                    } else {
                        viewDir = aboveTargetPos.subtract(pp).normalize();
                    }
                    mc.player.setSprinting(true);
                    mc.player.setDeltaMovement(viewDir.scale(speed));
                }
                case Auto_FromAboveFirst -> {
                    if (!firstPhase || aboveTargetPos == null) {
                        Vec3 tc = killTarget.getBoundingBox().getCenter();
                        aboveTargetPos = new Vec3(tc.x, tc.y + aboveHeight.get(), tc.z);
                        firstPhase = true;
                    }
                    Vec3 pp = mc.player.position();
                    double distToAbove = pp.distanceTo(aboveTargetPos);
                    boolean pathValid = isAbovePathValid(aboveTargetPos, killTarget);
                    if (!pathValid) {
                        viewDir = killTarget.getBoundingBox().getCenter().subtract(pp).normalize();
                        firstPhase = false;
                    } else if (distToAbove < aboveHeightTriggerDistance.get()) {
                        viewDir = killTarget.getBoundingBox().getCenter().subtract(pp).normalize();
                        firstPhase = false;
                    } else {
                        viewDir = aboveTargetPos.subtract(pp).normalize();
                    }
                    mc.player.setSprinting(true);
                    mc.player.setDeltaMovement(viewDir.scale(speed));
                }
            }
        }
    }

    private boolean isAbovePathValid(Vec3 abovePos, Entity target) {
        if (mc.level == null || abovePos == null) return false;
        Vec3 targetCenter = target.getBoundingBox().getCenter();

        if (isInvalid(abovePos)) return false;

        if (checkDistanceInvalid.get()) {
            double checkDist = aboveHeightTriggerDistance.get();
            int radius = (int) checkDist;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Vec3 testPos = abovePos.add(x, y, z);
                        if (testPos.distanceTo(abovePos) <= checkDist && isInvalid(testPos)) return false;
                    }
                }
            }
        }

        int pathSteps = Math.max(10, (int) (abovePos.distanceTo(targetCenter) * 2.5));
        for (int i = 1; i < pathSteps; i++) {
            double t = i / (double) pathSteps;
            if (isInvalid(abovePos.lerp(targetCenter, t))) return false;
        }
        return true;
    }

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private final Map<Vec3, Boolean> positionCache = new HashMap<>();

    private boolean isInvalid(Vec3 pos) {
        if (mc.level == null) return true;
        double clampedY = Mth.clamp(pos.y, mc.level.getMinY(), mc.level.getMinY() + mc.level.getHeight() - 1);
        if (clampedY != pos.y) return true;

        BlockPos floored = BlockPos.containing(pos);
        if (mc.level.getChunk(floored.getX() >> 4, floored.getZ() >> 4) == null) return true;
        if (positionCache.containsKey(pos)) return positionCache.get(pos);

        Entity entity = mc.player;
        Vec3 delta = pos.subtract(entity.position());
        AABB AABB = entity.getBoundingBox().move(delta);

        mutablePos.set(floored);
        for (int x = -1; x <= 1; x++) {
            mutablePos.setX(floored.getX() + x);
            for (int y = -1; y <= 1; y++) {
                mutablePos.setY(floored.getY() + y);
                for (int z = -1; z <= 1; z++) {
                    mutablePos.setZ(floored.getZ() + z);
                    BlockState state = mc.level.getBlockState(mutablePos);
                    if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                        || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CAMPFIRE)
                        || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW)) {
                        positionCache.put(pos, true);
                        return true;
                    }
                }
            }
        }

        for (Entity e : mc.level.getEntities(entity, AABB)) {
            if (e.canCollideWith(entity)) {
                positionCache.put(pos, true);
                return true;
            }
        }

        boolean collides = mc.level.getBlockCollisions(entity, AABB).iterator().hasNext();
        positionCache.put(pos, collides);
        return collides;
    }

    private void rotateToTarget(Entity target) {
        if (mc.player == null || target == null) return;
        Vec3 playerPos = mc.player.getEyePosition();
        AABB boundingBox = target.getBoundingBox();
        double targetCenterY = boundingBox.getCenter().y;
        double heightDiff = targetCenterY - playerPos.y;
        double boxHeight = boundingBox.maxY - boundingBox.minY;
        double targetY;

        if (Math.abs(heightDiff) < 1.0) {
            targetY = targetCenterY;
        } else if (heightDiff > 0) {
            double offset = Math.min(heightDiff / 5.0, 0.4);
            targetY = targetCenterY - (boxHeight * offset);
        } else {
            double offset = Math.min(-heightDiff / 5.0, 0.4);
            targetY = targetCenterY + (boxHeight * offset);
        }

        Vec3 targetPos = new Vec3(boundingBox.getCenter().x, targetY, boundingBox.getCenter().z);
        Vec3 toTarget = targetPos.subtract(playerPos).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toTarget.y));
        mc.player.setYRot(yaw);
        mc.player.setYHeadRot(yaw);
        mc.player.setXRot(pitch);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!Utils.canUpdate()) return;
        if (!(event.packet instanceof ServerboundMovePlayerPacket p)) return;
        if (mode.get() == Mode.Blink && isBlinking && !isFlushing) {
            event.cancel();
            synchronized (packets) {
                if (!packets.isEmpty()) {
                    ServerboundMovePlayerPacket last = packets.get(packets.size() - 1);
                    if (isSamePacket(p, last)) return;
                }
                packets.add(p);
            }
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (mc.level == null) return;
        if (!(event.packet instanceof ClientboundPlayerPositionPacket)) return;
        if (mode.get() == Mode.Blink && isBlinking) {
            synchronized (packets) { packets.clear(); }
            startPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            lastTargetDistance = killTarget != null ? mc.player.distanceTo(killTarget) : Double.MAX_VALUE;
            wasApproaching = false;
        }
    }

    private int getReadyTicks(Item item) {
        String name = item.toString().toLowerCase();
        int value = 14;
        if (name.contains("wooden")) value = 14;
        else if (name.contains("stone") || name.contains("golden")) value = 13;
        else if (name.contains("copper")) value = 12;
        else if (name.contains("iron")) value = 11;
        else if (name.contains("diamond")) value = 9;
        else if (name.contains("netherite")) value = 7;
        return Math.round(value * (lungeDelayModifier.get() / 100.0f));
    }

    private void startBlink() {
        isBlinking = true;
        startPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        synchronized (packets) { packets.clear(); }
        lastTargetDistance = killTarget != null ? mc.player.distanceTo(killTarget) : Double.MAX_VALUE;
        wasApproaching = false;
    }

    private boolean isSamePacket(ServerboundMovePlayerPacket a, ServerboundMovePlayerPacket b) {
        return a.isOnGround() == b.isOnGround()
            && a.getYRot(-1) == b.getYRot(-1)
            && a.getXRot(-1) == b.getXRot(-1)
            && a.getX(-1) == b.getX(-1)
            && a.getY(-1) == b.getY(-1)
            && a.getZ(-1) == b.getZ(-1);
    }

    private void flushPackets() {
        if (mc.player == null || mc.player.connection == null) return;
        synchronized (packets) {
            if (packets.isEmpty()) return;
            isFlushing = true;
            Vec3 currentPos = mc.player.position();
            double distance = startPos != null ? startPos.distanceTo(currentPos) : 0;
            if (distance < flushRange.get()) {
                packets.clear();
                isFlushing = false;
                return;
            }

            Vec3 sendStartPos = startPos;
            double boost = blinkDistanceBoost.get();
            if (boost > 0 && startPos != null) {
                Vec3 direction = currentPos.subtract(startPos);
                Vec3 horizontalDir = new Vec3(direction.x, 0, direction.z).normalize();
                if (horizontalDir.length() > 0.01) {
                    Vec3 targetPos = startPos.subtract(horizontalDir.scale(boost));
                    HitResult hit = mc.level.clipIncludingBorder(new ClipContext(
                        startPos, targetPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        mc.player));
                    if (hit.getType() == HitResult.Type.MISS) {
                        sendStartPos = targetPos;
                    } else {
                        sendStartPos = hit.getLocation().add(horizontalDir.scale(0.5));
                    }
                }
            }

            if (sendStartPos != null) {
                mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                    sendStartPos.x, sendStartPos.y, sendStartPos.z,
                    mc.player.getYRot(), mc.player.getXRot(), false, false));
            }

            mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                currentPos.x, currentPos.y, currentPos.z,
                mc.player.getYRot(), mc.player.getXRot(),
                mc.player.onGround(), mc.player.horizontalCollision));

            packets.clear();
            isFlushing = false;
        }
    }

    private Entity target() {
        if (mc.player == null || mc.level == null) return null;
        if (mc.hitResult instanceof EntityHitResult hit) {
            if (isValidTarget(hit.getEntity())) return hit.getEntity();
        }
        double range = maxRange.get();
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        HitResult blockHit = mc.level.clipIncludingBorder(new ClipContext(eyePos,
            eyePos.add(lookVec.scale(range)), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, mc.player));
        double rayLength = blockHit.getType() == HitResult.Type.MISS ? range :
            eyePos.distanceTo(blockHit.getLocation());
        List<Entity> candidates = mc.level.getEntities(mc.player,
            mc.player.getBoundingBox().expandTowards(lookVec.scale(rayLength)),
            e -> e instanceof LivingEntity && e.isAlive() && e != mc.player);
        candidates.sort(Comparator.comparingDouble(e -> eyePos.distanceToSqr(e.getBoundingBox().getCenter())));
        for (Entity e : candidates) {
            double dist = eyePos.distanceTo(e.getBoundingBox().getCenter());
            if (dist > range) break;
            if (!isValidTarget(e) || !canSeeTarget(e)) continue;
            Vec3 toEntity = e.getBoundingBox().getCenter().subtract(eyePos).normalize();
            if (lookVec.dot(toEntity) > 0.999) return e;
        }
        return null;
    }

    private boolean canSeeTarget(Entity target) {
        if (mc.player == null || mc.level == null) return false;
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        HitResult result = mc.level.clipIncludingBorder(new ClipContext(
            eyePos, targetCenter,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, mc.player));
        if (result.getType() == HitResult.Type.MISS) return true;
        return eyePos.distanceTo(result.getLocation()) >= eyePos.distanceTo(targetCenter) - 0.5;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null) return false;
        if (entity instanceof Player player && ignoreFriends.get() && Friends.get().isFriend(player)) return false;
        EntityType<?> type = entity.getType();
        boolean inList = targetEntities.get().contains(type);
        return targetListMode.get() == TargetListMode.Whitelist ? (inList || targetEntities.get().isEmpty()) : !inList;
    }
}
