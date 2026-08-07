package net.aero.aeropack.modules.movement;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.*;

public class BedrockEscape extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTeleport = settings.createGroup("Teleport");
    private final SettingGroup sgShaftESP = settings.createGroup("Shaft ESP");
    private final SettingGroup sgShaftColors = settings.createGroup("Shaft Colors");

    // --- General ---

    private final Setting<Boolean> renderTarget = sgGeneral.add(new BoolSetting.Builder()
        .name("render-target")
        .description("Render a AABB around the landing zone.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderEscapeShafts = sgGeneral.add(new BoolSetting.Builder()
        .name("render-escape-shafts")
        .description("Highlights bedrock columns that can be escaped by breaking blocks below or above.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoSwitchTool = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch-tool")
        .description("Automatically switches to the best tool when breaking blocks below bedrock.")
        .defaultValue(true)
        .build()
    );

    // --- Teleport ---

    private final Setting<Integer> reach = sgTeleport.add(new IntSetting.Builder()
        .name("reach")
        .description("How far ahead to look for bedrock to tunnel through.")
        .defaultValue(48)
        .min(8)
        .max(96)
        .sliderMax(96)
        .build()
    );

    private final Setting<Boolean> allowLiquids = sgTeleport.add(new BoolSetting.Builder()
        .name("allow-liquids")
        .description("Allow teleport targets inside liquids.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> packetSpam = sgTeleport.add(new IntSetting.Builder()
        .name("packet-spam")
        .description("Movement packets to send before the teleport to stay under the radar.")
        .defaultValue(4)
        .min(1)
        .max(10)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> ignoreSafeTick = sgTeleport.add(new BoolSetting.Builder()
        .name("ignore-safe-tick")
        .description("Allow teleporting even when the safe-tick indicator isn't showing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> shiftClickActivation = sgTeleport.add(new BoolSetting.Builder()
        .name("shift-click-activation")
        .description("Require holding shift to teleport when going down through bedrock.")
        .defaultValue(false)
        .build()
    );

    // --- Shaft ESP ---

    private final Setting<Integer> shaftScanRadius = sgShaftESP.add(new IntSetting.Builder()
        .name("shaft-scan-radius")
        .description("LevelChunk radius to scan for bedrock escape shafts.")
        .defaultValue(3)
        .min(1)
        .max(8)
        .sliderMax(8)
        .build()
    );

    private final Setting<Integer> shaftChunksPerTick = sgShaftESP.add(new IntSetting.Builder()
        .name("shaft-chunks-per-tick")
        .description("How many chunks are scanned each tick for shaft ESP. Lower = better FPS.")
        .defaultValue(1)
        .min(1)
        .max(8)
        .sliderMax(8)
        .build()
    );

    private final Setting<Integer> shaftRenderLimit = sgShaftESP.add(new IntSetting.Builder()
        .name("shaft-render-limit")
        .description("Maximum number of shaft ESP boxes to render.")
        .defaultValue(300)
        .min(50)
        .max(5000)
        .sliderMax(5000)
        .build()
    );

    private final Setting<Integer> shaftDepth = sgShaftESP.add(new IntSetting.Builder()
        .name("shaft-depth")
        .description("How far below bedrock to scan when looking for escape paths.")
        .defaultValue(16)
        .min(4)
        .max(64)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> shaftFillBoxes = sgShaftESP.add(new BoolSetting.Builder()
        .name("shaft-fill-boxes")
        .description("Render solid fills for shaft ESP. Disable for wireframe-only mode.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> shaftSurfaceOnly = sgShaftESP.add(new BoolSetting.Builder()
        .name("shaft-surface-only")
        .description("Render a thin marker on the top surface of bedrock instead of full block markers.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> lowDamageLimit = sgShaftESP.add(new DoubleSetting.Builder()
        .name("low-damage-limit")
        .description("Maximum estimated damage (in hearts) for yellow escape shafts.")
        .defaultValue(3.0)
        .min(0.5)
        .max(10.0)
        .sliderMax(10.0)
        .build()
    );

    // --- Shaft Colors ---

    private final Setting<SettingColor> safeShaftColor = sgShaftColors.add(new ColorSetting.Builder()
        .name("safe-shaft-color")
        .description("Color for safe escape shafts.")
        .defaultValue(new SettingColor(60, 255, 100))
        .build()
    );

    private final Setting<SettingColor> superSafeShaftColor = sgShaftColors.add(new ColorSetting.Builder()
        .name("super-safe-shaft-color")
        .description("Color for super safe escape shafts.")
        .defaultValue(new SettingColor(60, 255, 100))
        .build()
    );

    private final Setting<SettingColor> lowDamageShaftColor = sgShaftColors.add(new ColorSetting.Builder()
        .name("low-damage-shaft-color")
        .description("Color for low damage escape shafts.")
        .defaultValue(new SettingColor(255, 235, 80))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgShaftColors.add(new ColorSetting.Builder()
        .name("target-AABB-color")
        .description("Color for the teleport target box.")
        .defaultValue(new SettingColor(128, 0, 255))
        .build()
    );

    // --- Constants ---

    private static final Color DAMAGE_COLOR_SAFE = new Color(255, 255, 255);
    private static final Color DAMAGE_COLOR_WARN = new Color(255, 255, 0);
    private static final Color DAMAGE_COLOR_DEATH = new Color(255, 64, 64);
    private static final int SAFE_TICK_COLOR = 0xFF00FF00;

    private static int colorToInt(Color c) {
        return (255 << 24) | (c.r << 16) | (c.g << 8) | c.b;
    }
    private static final double VERTICAL_LOOK_THRESHOLD = 0.99995;
    private static final int NETHER_FLOOR_RENDER_Y = -2;
    private static final int BOAT_PLACE_RETRY_TICKS = 12;
    private static final int BOAT_ENTER_RETRY_TICKS = 12;
    private static final int SHIFT_BREAK_COOLDOWN_TICKS = 6;
    private static final int BEDROCK_CONTEXT_RADIUS = 20;

    // --- State ---

    private Vec3 teleportTarget;
    private AABB targetBox;
    private double damageHearts;
    private int damageColor = colorToInt(DAMAGE_COLOR_SAFE);
    private boolean isValidTarget;
    private boolean teleportedThisPress;
    private boolean showSafeTick;
    private boolean targetBelow;
    private final List<net.minecraft.core.BlockPos> blocksBelowBedrock = new ArrayList<>();
    private int shiftBreakCooldown;

    // Boat state
    private int pendingBoatPlacementTicks;
    private InteractionHand pendingBoatHand;
    private int pendingBoatEnterTicks;

    // Shaft ESP state
    private final ArrayDeque<ChunkPos> shaftScanQueue = new ArrayDeque<>();
    private final HashSet<ChunkPos> queuedShaftChunks = new HashSet<>();
    private final HashMap<ChunkPos, ArrayList<ShaftCandidate>> shaftsByChunk = new HashMap<>();
    private final ArrayList<ColoredBox> safeShaftBoxes = new ArrayList<>();
    private final ArrayList<ColoredBox> superSafeShaftBoxes = new ArrayList<>();
    private final ArrayList<ColoredBox> lowDamageShaftBoxes = new ArrayList<>();
    private ChunkPos lastShaftPlayerChunk;
    private int sideBoundaryY = Integer.MIN_VALUE;
    private boolean playerAboveSideBoundary;
    private boolean hasSideBoundary;
    private int foundSafeShafts;
    private int foundSuperSafeShafts;
    private int foundLowDamageShafts;

    public BedrockEscape() {
        super(Categories.Movement, "Bedrock Escape", "Escapes through bedrock using teleportation, boats, and block breaking. Ported from Wurst CevAPI.");
    }

    @Override
    public void onActivate() {
        teleportedThisPress = false;
        clearPendingBoatPlacement();
        clearShaftScanState();
    }

    @Override
    public void onDeactivate() {
        clearPendingBoatPlacement();
        teleportedThisPress = false;
        clearShaftScanState();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) {
            clearPendingBoatPlacement();
            return;
        }

        handlePendingBoatPlacement();

        if (!isActiveBedrockEscapeContext()) {
            resetRuntimeState();
            clearShaftScanState();
            return;
        }

        updateTarget();
        updateEscapeShafts();

        if (!isValidTarget) return;
        if (!showSafeTick && !ignoreSafeTick.get()) return;

        boolean shiftOk = !targetBelow || !shiftClickActivation.get() || mc.options.keyShift.isDown();
        boolean wantsTeleport = mc.options.keyAttack.isDown() && shiftOk;
        if (wantsTeleport) {
            if (!teleportedThisPress) {
                performTeleport(teleportTarget);
                teleportedThisPress = true;
            }
            return;
        }

        teleportedThisPress = false;

        boolean breakingBelowBedrock = isValidTarget && mc.options.keyShift.isDown();
        if (breakingBelowBedrock) {
            if (shiftBreakCooldown <= 0) {
                breakBlocksBelowBedrock();
                shiftBreakCooldown = SHIFT_BREAK_COOLDOWN_TICKS;
            } else {
                shiftBreakCooldown--;
            }
            return;
        }

        shiftBreakCooldown = 0;
    }

    private boolean isActiveBedrockEscapeContext() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (mc.level.dimension() == Level.END) return false;

        net.minecraft.core.BlockPos playerPos = mc.player.blockPosition();
        net.minecraft.core.BlockPos.MutableBlockPos probe = new net.minecraft.core.BlockPos.MutableBlockPos(
            playerPos.getX(), playerPos.getY(), playerPos.getZ());
        int minY = mc.level.getMinY();
        int maxY = minY + mc.level.getHeight() - 1;
        int startY = playerPos.getY();
        int downY = Math.max(minY, startY - BEDROCK_CONTEXT_RADIUS);
        int upY = Math.min(maxY, startY + BEDROCK_CONTEXT_RADIUS);

        for (int y = startY; y >= downY; y--) {
            probe.set(playerPos.getX(), y, playerPos.getZ());
            if (mc.level.getBlockState(probe).is(Blocks.BEDROCK)) return true;
        }

        for (int y = startY + 1; y <= upY; y++) {
            probe.set(playerPos.getX(), y, playerPos.getZ());
            if (mc.level.getBlockState(probe).is(Blocks.BEDROCK)) return true;
        }

        return false;
    }

    private void resetRuntimeState() {
        teleportTarget = null;
        targetBox = null;
        damageHearts = 0;
        damageColor = colorToInt(DAMAGE_COLOR_SAFE);
        isValidTarget = false;
        showSafeTick = false;
        targetBelow = false;
        teleportedThisPress = false;
        shiftBreakCooldown = 0;
        blocksBelowBedrock.clear();
    }

    // --- Targeting ---

    private void updateTarget() {
        Minecraft mc = Minecraft.getInstance();
        teleportTarget = null;
        targetBox = null;
        damageHearts = 0;
        isValidTarget = false;
        showSafeTick = false;

        if (mc.player == null || mc.level == null) return;

        Vec3 start = mc.player.getEyePosition();
        Vec3 direction = mc.player.getLookAngle();
        double maxReach = reach.get();
        double step = 0.25;
        Vec3 sample = start.add(direction.scale(step));
        double traveled = step;
        boolean inBedrock = false;
        Vec3 fallback = null;
        net.minecraft.core.BlockPos lastBreakable = null;
        blocksBelowBedrock.clear();

        while (traveled <= maxReach) {
            net.minecraft.core.BlockPos candidate = net.minecraft.core.BlockPos.containing(sample);
            BlockState state = mc.level.getBlockState(candidate);

            if (state.is(Blocks.BEDROCK)) {
                inBedrock = true;
                blocksBelowBedrock.clear();
                lastBreakable = null;
            } else if (inBedrock) {
                if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                    if (lastBreakable == null || !lastBreakable.equals(candidate)) {
                        blocksBelowBedrock.add(candidate);
                        lastBreakable = candidate;
                    }
                }

                if (isSafeLanding(candidate)) {
                    teleportTarget = getAirTarget(candidate);
                    break;
                }
                if (fallback == null && isAirLike(state) && isFluidClear(candidate)) {
                    fallback = getAirTarget(candidate);
                }
            }

            sample = sample.add(direction.scale(step));
            traveled += step;
        }

        if (teleportTarget == null) {
            if (fallback == null || !inBedrock) return;
            teleportTarget = fallback;
        }

        isValidTarget = inBedrock;
        float playerHearts = getPlayerHearts();
        double dropDistance = mc.player.getY() - teleportTarget.y;
        boolean facingDown = direction.y <= -VERTICAL_LOOK_THRESHOLD;
        boolean facingUp = direction.y >= VERTICAL_LOOK_THRESHOLD;
        boolean isVertical = facingDown || facingUp;

        if (dropDistance > 0) {
            damageHearts = estimateFallDamageHearts(dropDistance);
            damageColor = getDamageTextColor(playerHearts, damageHearts);
        } else {
            damageHearts = 0;
            damageColor = colorToInt(DAMAGE_COLOR_SAFE);
        }

        boolean safeFromDamage = dropDistance <= 0 || playerHearts >= damageHearts;
        boolean correctOrientation = dropDistance > 0 ? facingDown : facingUp;
        showSafeTick = isValidTarget && safeFromDamage && isVertical && correctOrientation;
        targetBelow = dropDistance > 0;

        targetBox = new AABB(
            teleportTarget.x - 0.5, teleportTarget.y, teleportTarget.z - 0.5,
            teleportTarget.x + 0.5, teleportTarget.y + 1.9, teleportTarget.z + 0.5
        );
    }

    private boolean isSafeLanding(net.minecraft.core.BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        BlockState first = mc.level.getBlockState(pos);
        BlockState second = mc.level.getBlockState(pos.above());

        if (!isFluidClear(pos)) return false;

        return isAirLike(first) && isAirLike(second);
    }

    private boolean isAirLike(BlockState state) {
        return state.isAir() || state.getCollisionShape(Minecraft.getInstance().level,
            net.minecraft.core.BlockPos.ZERO).isEmpty();
    }

    private boolean isFluidClear(net.minecraft.core.BlockPos pos) {
        if (allowLiquids.get()) return true;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        net.minecraft.core.BlockPos below = pos.below();
        net.minecraft.core.BlockPos above = pos.above();
        if (!mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) || !mc.level.hasChunk(above.getX() >> 4, above.getZ() >> 4) || !mc.level.hasChunk(below.getX() >> 4, below.getZ() >> 4)) {
            return false;
        }

        BlockState first = mc.level.getBlockState(pos);
        BlockState second = mc.level.getBlockState(above);
        BlockState third = mc.level.getBlockState(below);
        return first.getFluidState().isEmpty() && second.getFluidState().isEmpty() && third.getFluidState().isEmpty();
    }

    private Vec3 getAirTarget(net.minecraft.core.BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
    }

    // --- Block Breaking ---

    private void breakBlocksBelowBedrock() {
        Minecraft mc = Minecraft.getInstance();
        if (blocksBelowBedrock.isEmpty() || mc.player == null || mc.getConnection() == null) return;

        for (net.minecraft.core.BlockPos pos : blocksBelowBedrock) {
            if (mc.level != null && !mc.level.getBlockState(pos).isAir()) {
                mc.getConnection().getConnection().send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND, new BlockHitResult(
                        Vec3.atCenterOf(pos), Direction.UP, pos, false
                    ), 0
                ));
            }
        }
    }

    // --- Teleport ---

    private void performTeleport(Vec3 destination) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || destination == null) return;

        boolean upwardRoofEscape = destination.y > mc.player.getY() && destination.y > 127;
        InteractionHand boatHand = upwardRoofEscape ? getHeldBoatHand() : null;

        for (int i = 0; i < packetSpam.get(); i++) {
            mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.Pos(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                mc.player.onGround(), mc.player.horizontalCollision
            ));
        }

        sendMove(destination);
        mc.player.setPos(destination.x, destination.y, destination.z);
        mc.player.setDeltaMovement(Vec3.ZERO);

        if (boatHand != null) {
            scheduleBoatPlacementRetry(boatHand);
            tryPlaceBoatAboveRoof(boatHand);
        } else {
            clearPendingBoatPlacement();
        }
    }

    private void sendMove(Vec3 destination) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.PosRot(
            destination.x, destination.y, destination.z,
            mc.player.getYRot(), mc.player.getXRot(),
            false, false
        ));
    }

    // --- Boat ---

    private void handlePendingBoatPlacement() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.player.getVehicle() != null) {
            clearPendingBoatPlacement();
            return;
        }

        if (pendingBoatEnterTicks > 0) {
            if (tryEnterNearbyBoat()) {
                clearPendingBoatPlacement();
                return;
            }
            pendingBoatEnterTicks--;
        }

        if (pendingBoatPlacementTicks <= 0 || pendingBoatHand == null) return;

        if (!isBoat(mc.player.getItemInHand(pendingBoatHand)) || mc.player.getY() <= 127) {
            pendingBoatHand = null;
            pendingBoatPlacementTicks = 0;
            return;
        }

        if (!tryEnterNearbyBoat() && tryPlaceBoatAboveRoof(pendingBoatHand))
            pendingBoatEnterTicks = BOAT_ENTER_RETRY_TICKS;

        pendingBoatPlacementTicks--;
        if (pendingBoatPlacementTicks <= 0) pendingBoatHand = null;
    }

    private void scheduleBoatPlacementRetry(InteractionHand InteractionHand) {
        pendingBoatHand = InteractionHand;
        pendingBoatPlacementTicks = BOAT_PLACE_RETRY_TICKS;
    }

    private void clearPendingBoatPlacement() {
        pendingBoatHand = null;
        pendingBoatPlacementTicks = 0;
        pendingBoatEnterTicks = 0;
    }

    private InteractionHand getHeldBoatHand() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;

        if (isBoat(mc.player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (isBoat(mc.player.getOffhandItem())) return InteractionHand.OFF_HAND;
        return null;
    }

    private boolean isBoat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_boat") || path.endsWith("_chest_boat")
            || path.endsWith("_raft") || path.endsWith("_chest_raft");
    }

    private boolean tryPlaceBoatAboveRoof(InteractionHand InteractionHand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null
            || InteractionHand == null || mc.player.isUsingItem()) {
            return false;
        }

        ItemStack stack = mc.player.getItemInHand(InteractionHand);
        if (!isBoat(stack)) return false;

        net.minecraft.core.BlockPos roofBase = findRoofBoatPlacementBase();
        if (roofBase == null) return false;

        Vec3 hitVec = Vec3.atCenterOf(roofBase).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, roofBase, false);

        mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, hitVec);
        mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.PosRot(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            mc.player.getYRot(), mc.player.getXRot(),
            false, false
        ));
        mc.hitResult = hitResult;

        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand, hitResult);
        if (!result.consumesAction()) {
            result = mc.gameMode.useItem(mc.player, InteractionHand);
        }

        mc.player.swing(InteractionHand);
        if (result.consumesAction()) pendingBoatEnterTicks = BOAT_ENTER_RETRY_TICKS;

        return mc.player.getVehicle() != null || result.consumesAction();
    }

    private boolean tryEnterNearbyBoat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) return false;

        Entity boat = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        Entity camera = mc.getCameraEntity();
        if (camera != null) {
            Vec3 start = camera.getEyePosition();
            Vec3 look = camera.getLookAngle();
            Vec3 end = start.add(look.scale(16.0));
            boat = getClosestBoatHit(start, end);
            if (boat != null)
                bestDistance = mc.player.distanceToSqr(boat);
        }

        if (boat == null) {
            AABB searchBox = mc.player.getBoundingBox().inflate(8.0, 8.0, 8.0);
            List<Entity> entities = mc.level.getEntities(mc.player, searchBox, this::isEnterableBoat);
            for (Entity entity : entities) {
                double distance = mc.player.distanceToSqr(entity);
                if (distance >= bestDistance) continue;
                bestDistance = distance;
                boat = entity;
            }
        }

        if (boat == null) return false;

        Vec3 targetVec = getBoatTopHitVec(boat);
        EntityHitResult hitResult = new EntityHitResult(boat, targetVec);
        mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, targetVec);
        mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.PosRot(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(),
            mc.player.getYRot(), mc.player.getXRot(),
            false, false
        ));

        for (InteractionHand InteractionHand : InteractionHand.values()) {
            InteractionResult result = mc.gameMode.interact(mc.player, boat, new net.minecraft.world.phys.EntityHitResult(boat), InteractionHand);
            if (result.consumesAction()) mc.player.swing(InteractionHand);
            if (mc.player.getVehicle() != null) return true;
        }

        return mc.player.getVehicle() != null;
    }

    private Entity getClosestBoatHit(Vec3 start, Vec3 end) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return null;

        Vec3 dir = end.subtract(start);
        double maxDist = dir.length();
        if (maxDist <= 0) return null;

        Vec3 dirNorm = dir.normalize();
        AABB searchBox = new AABB(start, end).inflate(1);
        Entity closest = null;
        double closestDist = Double.POSITIVE_INFINITY;

        List<Entity> entities = mc.level.getEntities(mc.player, searchBox, this::isEnterableBoat);
        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox();
            Vec3 hit = raycastBox(box, start, end);
            if (hit == null) continue;

            double dist = hit.subtract(start).dot(dirNorm);
            if (dist < 0 || dist > maxDist || dist >= closestDist) continue;

            closestDist = dist;
            closest = entity;
        }

        return closest;
    }

    private boolean isEnterableBoat(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (entity == null || entity.isRemoved() || entity == mc.player.getVehicle()) return false;

        if (entity instanceof Boat boat)
            return boat.getControllingPassenger() == null;
        if (entity instanceof ChestBoat chestBoat)
            return chestBoat.getControllingPassenger() == null;

        return false;
    }

    private Vec3 getBoatTopHitVec(Entity boat) {
        AABB box = boat.getBoundingBox();
        return new Vec3(
            (box.minX + box.maxX) * 0.5,
            box.maxY - 0.05,
            (box.minZ + box.maxZ) * 0.5
        );
    }

    private net.minecraft.core.BlockPos findRoofBoatPlacementBase() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;

        Vec3 look = mc.player.getLookAngle();
        double horizontalLength = Math.hypot(look.x, look.z);
        int forwardX = horizontalLength < 1.0E-4 ? 0 : (int) Math.round(look.x / horizontalLength);
        int forwardZ = horizontalLength < 1.0E-4 ? 1 : (int) Math.round(look.z / horizontalLength);

        if (forwardX == 0 && forwardZ == 0) forwardZ = 1;

        net.minecraft.core.BlockPos base = mc.player.blockPosition().below();
        int sideX = -forwardZ;
        int sideZ = forwardX;

        for (int forward = 2; forward <= 3; forward++) {
            for (int side = 0; side <= 1; side++) {
                net.minecraft.core.BlockPos center = base.offset(forwardX * forward, 0, forwardZ * forward);
                net.minecraft.core.BlockPos candidate = side == 0 ? center
                    : center.offset(sideX * side, 0, sideZ * side);
                if (isValidBoatPlacementBase(candidate)) return candidate;

                if (side > 0) {
                    net.minecraft.core.BlockPos mirrored = center.offset(-sideX * side, 0, -sideZ * side);
                    if (isValidBoatPlacementBase(mirrored)) return mirrored;
                }
            }
        }

        return null;
    }

    private boolean isValidBoatPlacementBase(net.minecraft.core.BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) return false;

        BlockState above = mc.level.getBlockState(pos.above());
        BlockState twoAbove = mc.level.getBlockState(pos.above(2));
        return above.isAir() && twoAbove.isAir();
    }

    // --- Damage Calculations ---

    private float getPlayerHearts() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0f;
        float totalHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        return Math.max(0f, totalHealth) / 2.0f;
    }

    private int getDamageTextColor(float playerHearts, double heartsToTake) {
        if (heartsToTake <= 0 || playerHearts <= 0) return colorToInt(DAMAGE_COLOR_SAFE);

        float ratio = (float) Math.min(heartsToTake / playerHearts, 1.0);
        if (ratio <= 0) return colorToInt(DAMAGE_COLOR_SAFE);

        if (ratio < 0.5f)
            return blendColor(DAMAGE_COLOR_SAFE, DAMAGE_COLOR_WARN, ratio / 0.5f);

        return blendColor(DAMAGE_COLOR_WARN, DAMAGE_COLOR_DEATH, (ratio - 0.5f) / 0.5f);
    }

    private double estimateFallDamageHearts(double dropDistance) {
        double raw = Math.max(0, dropDistance - 3.0);
        return Math.ceil(raw) / 2.0;
    }

    private int blendColor(Color from, Color to, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (from.r + (to.r - from.r) * t);
        int g = (int) (from.g + (to.g - from.g) * t);
        int b = (int) (from.b + (to.b - from.b) * t);
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    // --- Render ---

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        renderEscapeShaftHighlights(event);

        if (!renderTarget.get() || !isValidTarget || targetBox == null) return;

        Color color = boxColor.get();
        Color sideColor = new Color(color.r, color.g, color.b, 80);
        Color lineColor = new Color(color.r, color.g, color.b, 255);
        event.renderer.box(targetBox, sideColor, lineColor, ShapeMode.Both, 0);
    }

    // --- Shaft ESP ---

    private void updateEscapeShafts() {
        Minecraft mc = Minecraft.getInstance();
        if (!renderEscapeShafts.get() || mc.player == null || mc.level == null) {
            clearShaftScanState();
            return;
        }

        ChunkPos currentChunk = mc.player.chunkPosition();
        if (lastShaftPlayerChunk == null || !lastShaftPlayerChunk.equals(currentChunk)) {
            lastShaftPlayerChunk = currentChunk;
            rebuildShaftScanQueue();
        }

        if (shaftScanQueue.isEmpty()) rebuildShaftScanQueue();

        for (int i = 0; i < shaftChunksPerTick.get() && !shaftScanQueue.isEmpty(); i++)
            scanShaftChunk(shaftScanQueue.removeFirst());

        rebuildShaftRenderCache();
    }

    private void renderEscapeShaftHighlights(Render3DEvent event) {
        if (!renderEscapeShafts.get()) return;
        if (safeShaftBoxes.isEmpty() && superSafeShaftBoxes.isEmpty() && lowDamageShaftBoxes.isEmpty()) return;

        List<ColoredBox> limitedSafe = limitBoxes(safeShaftBoxes);
        List<ColoredBox> limitedSuperSafe = limitBoxes(superSafeShaftBoxes);
        List<ColoredBox> limitedLow = limitBoxes(lowDamageShaftBoxes);

        renderBoxes(event, limitedSafe);
        renderBoxes(event, limitedSuperSafe);
        renderBoxes(event, limitedLow);
    }

    private void renderBoxes(Render3DEvent event, List<ColoredBox> boxes) {
        for (ColoredBox box : boxes) {
            if (shaftFillBoxes.get()) {
                event.renderer.box(box.AABB(), box.color(), box.color(), ShapeMode.Both, 0);
            } else {
                event.renderer.box(box.AABB(), new Color(0, 0, 0, 0), box.color(), ShapeMode.Lines, 0);
            }
        }
    }

    private void rebuildShaftScanQueue() {
        shaftScanQueue.clear();
        queuedShaftChunks.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ChunkPos center = mc.player.chunkPosition();
        int radius = shaftScanRadius.get();

        HashSet<ChunkPos> currentArea = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ChunkPos pos = new ChunkPos(center.x() + x, center.z() + z);
                currentArea.add(pos);
                shaftScanQueue.addLast(pos);
                queuedShaftChunks.add(pos);
            }
        }

        shaftsByChunk.keySet().removeIf(pos -> !currentArea.contains(pos));
    }

    private void scanShaftChunk(ChunkPos chunkPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        ArrayList<ShaftCandidate> candidates = new ArrayList<>();
        int minY = mc.level.getMinY();
        int maxY = minY + mc.level.getHeight() - 1;
        int depthLimit = shaftDepth.get();
        float playerHearts = getPlayerHearts();

        net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int x = chunkPos.getMinBlockX(); x < chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z < chunkPos.getMaxBlockZ(); z++) {
                for (int y = maxY; y >= minY; y--) {
                    cursor.set(x, y, z);
                    if (!mc.level.getBlockState(cursor).is(Blocks.BEDROCK)) continue;
                    tryAddShaftCandidate(candidates, x, y, z, minY, maxY, depthLimit, playerHearts, true);
                    tryAddShaftCandidate(candidates, x, y, z, minY, maxY, depthLimit, playerHearts, false);
                }
            }
        }

        shaftsByChunk.put(chunkPos, candidates);
    }

    private void tryAddShaftCandidate(ArrayList<ShaftCandidate> candidates,
        int x, int y, int z, int minY, int maxY, int depthLimit,
        float playerHearts, boolean fromAbove) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!isSideAllowed(fromAbove)) return;

        net.minecraft.core.BlockPos above = new net.minecraft.core.BlockPos(x, y + 1, z);
        net.minecraft.core.BlockPos below = new net.minecraft.core.BlockPos(x, y - 1, z);
        boolean hasAbove = mc.level.hasChunk(above.getX() >> 4, above.getZ() >> 4);
        boolean hasBelow = mc.level.hasChunk(below.getX() >> 4, below.getZ() >> 4);
        if (!hasAbove) return;
        if (fromAbove && !hasBelow) return;

        int landingY;
        if (fromAbove) {
            if (!isAirLike(mc.level.getBlockState(above))) return;
            if (mc.level.getBlockState(below).is(Blocks.BEDROCK)) return;
            if (hasBedrockWithinDepth(x, z, y - 1, minY, depthLimit)) return;
            landingY = findBreakableTwoHighLandingYDown(x, y - 1, z, minY, depthLimit);
        } else {
            if (hasBelow && !isAirLike(mc.level.getBlockState(below))) return;
            if (mc.level.getBlockState(above).is(Blocks.BEDROCK)) return;
            if (hasBedrockWithinDepthUp(x, z, y + 1, maxY, depthLimit)) return;
            landingY = findBreakableTwoHighLandingYUp(x, y + 1, z, maxY, depthLimit);
        }

        if (landingY == Integer.MIN_VALUE) return;

        double targetY = landingY + 0.1;
        double dropDistance = mc.player.getY() - targetY;
        double damage = dropDistance > 0 ? estimateFallDamageHearts(dropDistance) : 0;

        if (playerHearts <= 0 || damage > playerHearts) return;

        boolean safe = damage <= 0;
        boolean superSafe = safe && !hasLavaNearExit(x, z, landingY, maxY);
        boolean low = !safe && damage <= lowDamageLimit.get();
        if (!safe && !low) return;

        candidates.add(new ShaftCandidate(new net.minecraft.core.BlockPos(x, y, z), landingY, safe, superSafe, fromAbove));
    }

    private boolean hasBedrockWithinDepth(int x, int z, int startY, int minY, int depthLimit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return true;

        int endY = Math.max(minY, startY - depthLimit);
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = startY; y >= endY; y--) {
            pos.set(x, y, z);
            if (!mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;
            if (mc.level.getBlockState(pos).is(Blocks.BEDROCK)) return true;
        }
        return false;
    }

    private boolean hasBedrockWithinDepthUp(int x, int z, int startY, int maxY, int depthLimit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return true;

        int endY = Math.min(maxY, startY + depthLimit);
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = startY; y <= endY; y++) {
            pos.set(x, y, z);
            if (!mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;
            if (mc.level.getBlockState(pos).is(Blocks.BEDROCK)) return true;
        }
        return false;
    }

    private int findBreakableTwoHighLandingYDown(int x, int startY, int z, int minY, int depthLimit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Integer.MIN_VALUE;

        int endY = Math.max(minY, startY - depthLimit);
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = startY - 1; y >= endY; y--) {
            pos.set(x, y, z);
            BlockState first = mc.level.getBlockState(pos);
            BlockState second = mc.level.getBlockState(pos.above());
            if (isBreakableEscapeBlock(first) && isBreakableEscapeBlock(second))
                return y;
        }
        return Integer.MIN_VALUE;
    }

    private int findBreakableTwoHighLandingYUp(int x, int startY, int z, int maxY, int depthLimit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Integer.MIN_VALUE;

        int endY = Math.min(maxY - 1, startY + depthLimit);
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = startY; y <= endY; y++) {
            pos.set(x, y, z);
            BlockState first = mc.level.getBlockState(pos);
            BlockState second = mc.level.getBlockState(pos.above());
            if (isBreakableEscapeBlock(first) && isBreakableEscapeBlock(second))
                return y;
        }
        return Integer.MIN_VALUE;
    }

    private boolean isBreakableEscapeBlock(BlockState state) {
        if (state.is(Blocks.BEDROCK)) return false;
        return state.getFluidState().isEmpty();
    }

    private boolean hasLavaNearExit(int x, int z, int landingY, int maxY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return true;

        int minY = mc.level.getMinY();
        int beginY = Math.max(landingY - 1, minY);
        if (beginY > maxY) return false;

        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = beginY; y <= maxY; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    pos.set(x + dx, y, z + dz);
                    if (!mc.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return true;
                    if (mc.level.getBlockState(pos).getFluidState().is(FluidTags.LAVA))
                        return true;
                }
            }
        }
        return false;
    }

    private void updateSideBoundary() {
        hasSideBoundary = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        int px = mc.player.getBlockX();
        int py = mc.player.getBlockY();
        int pz = mc.player.getBlockZ();
        int minY = mc.level.getMinY();
        int maxY = minY + mc.level.getHeight() - 1;
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();

        int aboveY = Integer.MIN_VALUE;
        for (int y = py; y <= maxY; y++) {
            pos.set(px, y, pz);
            if (mc.level.getBlockState(pos).is(Blocks.BEDROCK)) {
                aboveY = y;
                break;
            }
        }

        int belowY = Integer.MIN_VALUE;
        for (int y = py; y >= minY; y--) {
            pos.set(px, y, pz);
            if (mc.level.getBlockState(pos).is(Blocks.BEDROCK)) {
                belowY = y;
                break;
            }
        }

        boolean hasAbove = aboveY != Integer.MIN_VALUE;
        boolean hasBelow = belowY != Integer.MIN_VALUE;
        if (!hasAbove && !hasBelow) return;

        if (hasBelow && (!hasAbove || py - belowY <= aboveY - py)) {
            sideBoundaryY = belowY;
            playerAboveSideBoundary = true;
        } else {
            sideBoundaryY = aboveY;
            playerAboveSideBoundary = false;
        }

        hasSideBoundary = true;
    }

    private boolean isSideAllowed(boolean fromAbove) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null && mc.level.dimension() == Level.NETHER) {
            int py = mc.player.getBlockY();
            if (py >= 123) return fromAbove;
            if (py <= NETHER_FLOOR_RENDER_Y) return !fromAbove;
            return false;
        }

        if (!hasSideBoundary || mc.player == null) return true;

        if (fromAbove) return playerAboveSideBoundary;
        return !playerAboveSideBoundary;
    }

    private void rebuildShaftRenderCache() {
        safeShaftBoxes.clear();
        superSafeShaftBoxes.clear();
        lowDamageShaftBoxes.clear();
        foundSafeShafts = 0;
        foundSuperSafeShafts = 0;
        foundLowDamageShafts = 0;

        ArrayList<ShaftCandidate> safeCandidates = new ArrayList<>();
        ArrayList<ShaftCandidate> superSafeCandidates = new ArrayList<>();
        ArrayList<ShaftCandidate> lowCandidates = new ArrayList<>();

        for (ArrayList<ShaftCandidate> candidates : shaftsByChunk.values()) {
            for (ShaftCandidate candidate : candidates) {
                if (!isSideAllowed(candidate.fromAbove())) continue;

                if (candidate.safe()) {
                    if (candidate.fromAbove() || candidate.superSafe())
                        superSafeCandidates.add(candidate);
                    else
                        safeCandidates.add(candidate);
                } else {
                    lowCandidates.add(candidate);
                }
            }
        }

        Comparator<ShaftCandidate> byDistance = Comparator
            .comparingDouble(this::distanceSqToPlayer)
            .thenComparingInt(c -> c.surfacePos().getX())
            .thenComparingInt(c -> c.surfacePos().getY())
            .thenComparingInt(c -> c.surfacePos().getZ());
        safeCandidates.sort(byDistance);
        lowCandidates.sort(byDistance);

        for (ShaftCandidate candidate : safeCandidates) {
            AABB markerBox = getShaftMarkerBox(candidate.surfacePos(), candidate.fromAbove(), candidate.landingY());
            safeShaftBoxes.add(new ColoredBox(markerBox, new Color(safeShaftColor.get().r, safeShaftColor.get().g, safeShaftColor.get().b, 0xC0)));
            foundSafeShafts++;
        }

        for (ShaftCandidate candidate : superSafeCandidates) {
            AABB markerBox = getShaftMarkerBox(candidate.surfacePos(), candidate.fromAbove(), candidate.landingY());
            superSafeShaftBoxes.add(new ColoredBox(markerBox, new Color(superSafeShaftColor.get().r, superSafeShaftColor.get().g, superSafeShaftColor.get().b, 0xC0)));
            foundSuperSafeShafts++;
        }

        for (ShaftCandidate candidate : lowCandidates) {
            AABB markerBox = getShaftMarkerBox(candidate.surfacePos(), candidate.fromAbove(), candidate.landingY());
            lowDamageShaftBoxes.add(new ColoredBox(markerBox, new Color(lowDamageShaftColor.get().r, lowDamageShaftColor.get().g, lowDamageShaftColor.get().b, 0xB8)));
            foundLowDamageShafts++;
        }
    }

    private double distanceSqToPlayer(ShaftCandidate candidate) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return Double.MAX_VALUE;

        double cx = candidate.surfacePos().getX() + 0.5;
        double cy = candidate.surfacePos().getY() + 0.5;
        double cz = candidate.surfacePos().getZ() + 0.5;
        return mc.player.distanceToSqr(cx, cy, cz);
    }

    private AABB getShaftMarkerBox(net.minecraft.core.BlockPos surfacePos, boolean fromAbove, int landingY) {
        double x1 = surfacePos.getX();
        double y1 = surfacePos.getY();
        double z1 = surfacePos.getZ();

        if (!shaftSurfaceOnly.get()) {
            if (fromAbove)
                return new AABB(x1, y1, z1, x1 + 1, y1 + 1, z1 + 1);
            return new AABB(x1, landingY, z1, x1 + 1, landingY + 2, z1 + 1);
        }

        if (fromAbove)
            return new AABB(x1 + 0.05, y1 + 0.98, z1 + 0.05, x1 + 0.95, y1 + 1.02, z1 + 0.95);

        return new AABB(x1 + 0.05, landingY - 0.02, z1 + 0.05, x1 + 0.95, landingY + 0.02, z1 + 0.95);
    }

    private void clearShaftScanState() {
        shaftScanQueue.clear();
        queuedShaftChunks.clear();
        shaftsByChunk.clear();
        safeShaftBoxes.clear();
        superSafeShaftBoxes.clear();
        lowDamageShaftBoxes.clear();
        lastShaftPlayerChunk = null;
        sideBoundaryY = Integer.MIN_VALUE;
        playerAboveSideBoundary = false;
        hasSideBoundary = false;
        foundSafeShafts = 0;
        foundSuperSafeShafts = 0;
        foundLowDamageShafts = 0;
    }

    private List<ColoredBox> limitBoxes(ArrayList<ColoredBox> boxes) {
        int max = shaftRenderLimit.get();
        if (boxes.size() <= max) return boxes;
        return boxes.subList(0, max);
    }

    // --- Records and data classes ---

    private record ShaftCandidate(net.minecraft.core.BlockPos surfacePos, int landingY,
        boolean safe, boolean superSafe, boolean fromAbove) {}

    private record ColoredBox(AABB AABB, Color color) {}

    private static Vec3 raycastBox(AABB box, Vec3 start, Vec3 end) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;

        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        if (dx != 0) {
            double t1 = (box.minX - start.x) / dx;
            double t2 = (box.maxX - start.x) / dx;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
        } else if (start.x < box.minX || start.x > box.maxX) {
            return null;
        }

        if (dy != 0) {
            double t1 = (box.minY - start.y) / dy;
            double t2 = (box.maxY - start.y) / dy;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
        } else if (start.y < box.minY || start.y > box.maxY) {
            return null;
        }

        if (dz != 0) {
            double t1 = (box.minZ - start.z) / dz;
            double t2 = (box.maxZ - start.z) / dz;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            tmin = Math.max(tmin, t1);
            tmax = Math.min(tmax, t2);
        } else if (start.z < box.minZ || start.z > box.maxZ) {
            return null;
        }

        if (tmax < 0 || tmin > tmax) return null;

        double t = tmin >= 0 ? tmin : tmax;
        return new Vec3(start.x + dx * t, start.y + dy * t, start.z + dz * t);
    }
}
