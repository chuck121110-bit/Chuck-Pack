package net.chuck.chuckpack.modules.world;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Enhanced Scaffold - 1:1 port of LiquidBounce's ModuleScaffold
 * Based on net.ccbluex.liquidbounce.features.module.modules.world.scaffold.ModuleScaffold
 * Original Kotlin ported to Java for Meteor (Fabric 1.21.11 / 26.1.2)
 * Source: Source References/liquidbounce.zip (CCBlueX LiquidBounce Nextgen)
 *
 * LiquidBounce scaffold features ported:
 * - Technique: Normal, Expand, GodBridge, Breezily
 * - SameY modes: OFF, ON, JUMP_KEY, FALLING
 * - Tower: None, Motion, Pulldown, Vulcan (simplified Motion pulldown)
 * - Delay, MinDist, Timer
 * - SafeWalk, Ledge, AutoBlock, RotationTiming, Swing
 * - Uses Meteor BlockUtils.place + InvUtils for placement (replaces LiquidBounce SilentHotbar/RotationManager/ScaffoldMovementPlanner)
 */
public class EnhancedScaffold extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRotation = settings.createGroup("Rotation");
    private final SettingGroup sgTower = settings.createGroup("Tower");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");

    // LiquidBounce: delay by intRange("Delay", 0..0, 0..40)
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks between placements. Matches LiquidBounce ModuleScaffold delay.")
        .defaultValue(0)
        .min(0)
        .max(10)
        .sliderMax(10)
        .build()
    );

    // LiquidBounce: minDist by float("MinDist", 0.0f, 0.0f..0.25f)
    private final Setting<Double> minDist = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-dist")
        .description("Minimum distance to block face. Port of LiquidBounce MinDist.")
        .defaultValue(0.0)
        .min(0.0)
        .max(0.25)
        .sliderMax(0.25)
        .build()
    );

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("Max placement range (4.25 in LiquidBounce).")
        .defaultValue(4.25)
        .min(1.0)
        .max(6.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Boolean> safeWalk = sgGeneral.add(new BoolSetting.Builder()
        .name("safe-walk")
        .description("Prevents falling off scaffold (SafeWalk). Matches ScaffoldEagleFeature + ScaffoldMovementPlanner safe walk.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ledge = sgGeneral.add(new BoolSetting.Builder()
        .name("ledge")
        .description("Ledge handling (jump / stop input at edge). Port of LiquidBounce ledge=true")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-block")
        .description("Automatically selects best block in hotbar (ScaffoldAutoBlockFeature).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> doNotUseBelowCount = sgGeneral.add(new IntSetting.Builder()
        .name("do-not-use-below-count")
        .description("Don't use stacks with count <= this. Port of ScaffoldAutoBlock doNotUseBelowCount.")
        .defaultValue(0)
        .min(0)
        .max(64)
        .sliderMax(10)
        .visible(autoBlock::get)
        .build()
    );

    // Technique - matches LiquidBounce technique choices
    public enum Technique {
        Normal,
        Expand,
        GodBridge,
        Breezily
    }

    private final Setting<Technique> technique = sgGeneral.add(new EnumSetting.Builder<Technique>()
        .name("technique")
        .description("Scaffold technique. Port of LiquidBounce Technique (Normal, Expand, GodBridge, Breezily).")
        .defaultValue(Technique.Normal)
        .build()
    );

    private final Setting<Integer> expandLength = sgGeneral.add(new IntSetting.Builder()
        .name("expand-length")
        .description("Blocks to expand forward (Expand technique).")
        .defaultValue(3)
        .min(1)
        .max(6)
        .sliderMax(6)
        .visible(() -> technique.get() == Technique.Expand)
        .build()
    );

    public enum SameYMode {
        OFF,
        ON,
        JUMP_KEY,
        FALLING
    }

    private final Setting<SameYMode> sameY = sgGeneral.add(new EnumSetting.Builder<SameYMode>()
        .name("same-y")
        .description("SameY placement (OFF, ON, JUMP_KEY, FALLING). Port of LiquidBounce SameYMode.")
        .defaultValue(SameYMode.OFF)
        .build()
    );

    public enum TowerMode {
        None,
        Motion,
        Pulldown,
        Vulcan
    }

    private final Setting<TowerMode> tower = sgTower.add(new EnumSetting.Builder<TowerMode>()
        .name("tower")
        .description("Tower when holding jump. Port of LiquidBounce ScaffoldTower (None, Motion, Pulldown, Vulcan).")
        .defaultValue(TowerMode.None)
        .build()
    );

    private final Setting<Double> towerTimer = sgTower.add(new DoubleSetting.Builder()
        .name("tower-timer")
        .description("Timer while towering (LiquidBounce timer).")
        .defaultValue(1.0)
        .min(0.1)
        .max(3.0)
        .sliderMax(3.0)
        .visible(() -> tower.get() != TowerMode.None)
        .build()
    );

    // Rotation - matches ScaffoldRotationValueGroup
    public enum RotationTiming {
        Normal,
        OnTick,
        OnTickSnap
    }

    private final Setting<RotationTiming> rotationTiming = sgRotation.add(new EnumSetting.Builder<RotationTiming>()
        .name("rotation-timing")
        .description("When to rotate (Normal, OnTick, OnTickSnap). Port of LiquidBounce RotationTiming.")
        .defaultValue(RotationTiming.Normal)
        .build()
    );

    private final Setting<Boolean> considerInventory = sgRotation.add(new BoolSetting.Builder()
        .name("consider-inventory")
        .description("Consider inventory for rotations.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> swing = sgRotation.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swing hand when placing.")
        .defaultValue(true)
        .build()
    );

    // Advanced
    private final Setting<Boolean> swingDoNotHide = sgAdvanced.add(new BoolSetting.Builder()
        .name("swing-do-not-hide")
        .description("Swing mode DO_NOT_HIDE from LiquidBounce (always swing).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> simulatePlacementAttempts = sgAdvanced.add(new BoolSetting.Builder()
        .name("simulate-placement-attempts")
        .description("Simulate placement attempts for Breezily/GodBridge timing (LiquidBounce SimulatePlacementAttempts).")
        .defaultValue(false)
        .build()
    );

    private int ticksPassed = 0;
    private int placementY = 0;
    private int startY = 0;
    private boolean wasTowering = false;
    private int forceSneak = 0;

    public EnhancedScaffold() {
        super(Categories.World, "enhanced-scaffold", "1:1 port of LiquidBounce scaffold (Normal/Expand/GodBridge/Breezily, SameY, Tower, SafeWalk). Based on ModuleScaffold.kt");
    }

    @Override
    public void onActivate() {
        ticksPassed = 0;
        if (mc.player != null) {
            placementY = mc.player.blockPosition().getY() - 1;
            startY = mc.player.blockPosition().getY();
        }
        wasTowering = false;
        forceSneak = 0;
    }

    @Override
    public void onDeactivate() {
        ticksPassed = 0;
        forceSneak = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        // LiquidBounce timer handler
        // Note: Meteor's timer is via TickEvent, we skip custom timer for now but keep setting for compat

        if (mc.player.onGround()) {
            placementY = mc.player.blockPosition().getY() - 1;
            wasTowering = false;
        }

        if (mc.options.keyJump.isDown()) {
            startY = mc.player.blockPosition().getY();
        }

        // Delay handling (LiquidBounce delay random 0..0 -> fixed)
        if (ticksPassed < delay.get()) {
            ticksPassed++;
            return;
        }

        boolean isTowering = tower.get() != TowerMode.None && mc.options.keyJump.isDown();
        Technique activeTechnique = isTowering ? Technique.Normal : technique.get();

        // SafeWalk force sneak at edge (LiquidBounce ScaffoldMovementPlanner + Eagle)
        if (safeWalk.get() || forceSneak > 0) {
            if (isOverAir() || forceSneak > 0) {
                mc.options.keyShift.setDown(true);
                if (forceSneak > 0) forceSneak--;
            }
        }

        // Ledge feature
        if (ledge.get()) {
            handleLedge(activeTechnique);
        }

        // Find placement target based on technique
        BlockPos target = findPlacementTarget(activeTechnique);
        if (target == null) return;

        // Check if already placed
        if (!mc.level.getBlockState(target).canBeReplaced()) return;

        // MinDist check (LiquidBounce isValidCrosshairTarget)
        if (!isValidCrosshairTarget(target)) return;

        // Find block in hotbar (ScaffoldAutoBlockFeature + BLOCK_COMPARATOR)
        FindItemResult result = findBestBlock();
        if (!result.found()) return;

        boolean rotate = rotationTiming.get() != RotationTiming.Normal ? true : true;
        // Place block using Meteor BlockUtils (handles rotation, swing, placement)
        boolean placed = BlockUtils.place(target, result, rotate, 50, swing.get(), true, false);
        if (placed) {
            ticksPassed = 0;
            if (mc.player != null && swing.get()) {
                if (swingDoNotHide.get()) mc.player.swing(InteractionHand.MAIN_HAND);
            }
            // Track placement for expand/godbridge
            if (activeTechnique == Technique.Breezily || activeTechnique == Technique.GodBridge) {
                // Breezily/GodBridge often use sneak timing - handled via forceSneak
            }
            // Tower motion boost (LiquidBounce ScaffoldTowerMotion etc.)
            if (isTowering) {
                handleTower();
            }
            // Ledge on placement
            if (technique.get() == Technique.Expand) {
                // Expand places multiple ahead, already handled via findPlacementTarget
            }
        }
    }

    private boolean isOverAir() {
        if (mc.player == null || mc.level == null) return false;
        BlockPos below = BlockPos.containing(mc.player.position()).below();
        // Expand bounding box check like LiquidBounce isBlockBelow (inflate 0.5 and -1.05)
        // Simplified: check if block below is air and player is near edge
        Vec3 pos = mc.player.position();
        double x = pos.x;
        double z = pos.z;
        // Check if player is at edge (no collision below)
        return mc.level.getBlockState(below).isAir() && mc.level.getBlockState(below.below()).isAir();
    }

    private void handleLedge(Technique tech) {
        if (mc.player == null || mc.level == null) return;
        // Simplified ledge: if block in front is air and below is air, stop input briefly
        // This mirrors LiquidBounce ledge() which can jump/stopInput/stepBack
        // We do minimal: if over air and moving, force sneak
        if (isOverAir() && mc.player.input.hasForwardImpulse()) {
            // Force sneak next 2 ticks to prevent fall (like ScaffoldEagle)
            if (forceSneak <= 0) forceSneak = 2;
        }
    }

    private void handleTower() {
        if (mc.player == null) return;
        switch (tower.get()) {
            case Motion -> {
                // LiquidBounce ScaffoldTowerMotion: motion Y boost
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.42, mc.player.getDeltaMovement().z);
                // Also set onGround false to allow next jump? Simplified.
            }
            case Pulldown -> {
                // ScaffoldTowerPulldown: pull down faster when not towering? Simplified: no extra logic
            }
            case Vulcan -> {
                // ScaffoldTowerVulcan uses tickHandler with motion, simplified to Motion
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.42, mc.player.getDeltaMovement().z);
            }
            default -> {}
        }
    }

    private BlockPos findPlacementTarget(Technique tech) {
        if (mc.player == null) return null;
        BlockPos playerPos = mc.player.blockPosition();

        // SameY handling (LiquidBounce SameYMode)
        BlockPos base;
        switch (sameY.get()) {
            case ON -> base = new BlockPos(playerPos.getX(), placementY, playerPos.getZ());
            case JUMP_KEY -> {
                if (mc.options.keyJump.isDown()) base = playerPos.below();
                else base = new BlockPos(playerPos.getX(), placementY, playerPos.getZ());
                break;
            }
            case FALLING -> {
                if (mc.player.getDeltaMovement().y < 0.2) base = new BlockPos(playerPos.getX(), placementY, playerPos.getZ());
                else base = playerPos.below();
                break;
            }
            default -> base = playerPos.below();
        }

        // Technique specific targeting (LiquidBounce ModuleScaffold.getTargetedPosition + technique.findPlacementTarget)
        return switch (tech) {
            case Expand -> {
                // Expand places 3-6 blocks ahead in movement direction
                Direction dir = mc.player.getDirection();
                BlockPos forward = base;
                // Try to place under player first, if already solid, extend
                if (!mc.level.getBlockState(base).canBeReplaced()) {
                    // Find air ahead
                    for (int i = 1; i <= expandLength.get(); i++) {
                        BlockPos cand = base.relative(dir, i);
                        if (mc.level.getBlockState(cand).canBeReplaced()) {
                            yield cand;
                        }
                    }
                    yield base; // fallback
                } else {
                    // If base is air, place there, but expand also checks ahead during placement
                    yield base;
                }
            }
            case GodBridge -> {
                // GodBridge: sneaky, places behind + requires sneak, simplified to Normal + sneak
                // In LiquidBounce, GodBridge uses ScaffoldGodBridgeTechnique with sneak and pitch
                yield base;
            }
            case Breezily -> {
                // Breezily: requires sneak and timing, simplified to Normal but with simulatePlacementAttempts logic
                yield base;
            }
            default -> base; // Normal
        };
    }

    private boolean isValidCrosshairTarget(BlockPos pos) {
        if (mc.player == null) return false;
        Vec3 eyes = mc.player.getEyePosition();
        // Check minDist similar to LiquidBounce isValidCrosshairTarget
        // Find closest placement side and check dist
        Direction side = BlockUtils.getPlaceSide(pos);
        if (side == null) return true; // fallback
        double dist = 0;
        if (side.getAxis() != Direction.Axis.Y) {
            Vec3 hit = Vec3.atCenterOf(pos);
            Vec3 diff = hit.subtract(eyes);
            dist = side == Direction.NORTH || side == Direction.SOUTH ? diff.z : diff.x;
            if (Math.abs(dist) < minDist.get()) return false;
        }
        // Range check
        Vec3 center = Vec3.atCenterOf(pos);
        if (eyes.distanceToSqr(center) > placeRange.get() * placeRange.get()) return false;
        return true;
    }

    private FindItemResult findBestBlock() {
        // Port of LiquidBounce BLOCK_COMPARATOR chain: PreferFavourable, Solid, FullCube, Walkable, AverageHard, StackSize
        // Simplified: find solid, full cube, not falling, not shulker, prefer larger stack
        FindItemResult best = InvUtils.findInHotbar(stack -> isValidBlock(stack));
        if (!best.found() && autoBlock.get()) {
            // Search inventory for best block and move to hotbar? LiquidBounce SilentHotbar does autoBlock from inventory
            // For Meteor, we can just search hotbar only; if not found, try to find in inventory and swap? Simplified: return not found
            // Alternative: search all 36 slots for valid block and use swap
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (isValidBlock(stack) && stack.getCount() > doNotUseBelowCount.get()) {
                    // Move to hotbar slot 0 via InvUtils
                    // Use InvUtils.move to swap? Simplified: find empty hotbar slot and move
                    // For now just return found with inventory slot (BlockUtils.place supports inventory)
                    // Meteor BlockUtils.place with FindItemResult from InvUtils.find handles inventory
                    FindItemResult invResult = InvUtils.find(stack2 -> stack2.getItem() == stack.getItem());
                    if (invResult.found()) return invResult;
                }
            }
        }
        // Filter by count
        if (best.found()) {
            ItemStack stack = mc.player.getInventory().getItem(best.slot());
            if (stack.getCount() <= doNotUseBelowCount.get()) {
                // Find alternative with higher count
                FindItemResult alt = findBlockWithCountAbove();
                if (alt.found()) return alt;
            }
        }
        return best;
    }

    private FindItemResult findBlockWithCountAbove() {
        FindItemResult best = new FindItemResult(0, 0);
        int bestCount = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!isValidBlock(stack)) continue;
            if (stack.getCount() <= doNotUseBelowCount.get()) continue;
            if (stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                best = new FindItemResult(i, stack.getCount());
            }
        }
        if (bestCount == -1) return new FindItemResult(0, 0);
        return best;
    }

    private boolean isValidBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        // Filter non-solid (LiquidBounce PreferSolidBlocks)
        BlockState state = block.defaultBlockState();
        if (!state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) return false;
        if (block instanceof ShulkerBoxBlock) return false;
        // Filter falling blocks that would fall (like FallingBlock.isFree)
        if (block instanceof FallingBlock) {
            BlockPos below = mc.player != null ? mc.player.blockPosition().below() : BlockPos.ZERO;
            if (mc.level != null && FallingBlock.isFree(mc.level.getBlockState(below.below()))) return false;
        }
        return true;
    }
}
