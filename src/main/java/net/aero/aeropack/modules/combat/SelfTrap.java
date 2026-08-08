package net.aero.aeropack.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SelfTrap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");
    private final SettingGroup sgBehavior = settings.createGroup("Behavior");

    private final Setting<Integer> width = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("Width of the trap (blocks from center, each direction).")
        .defaultValue(1)
        .min(0)
        .sliderMax(4)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("Height of the trap above feet level.")
        .defaultValue(2)
        .min(1)
        .sliderMax(4)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Tick delay between block placements.")
        .defaultValue(2)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> includeRoof = sgGeneral.add(new BoolSetting.Builder()
        .name("roof")
        .description("Place a roof layer on top.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> includeBase = sgGeneral.add(new BoolSetting.Builder()
        .name("base")
        .description("Place a floor layer at feet level.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Item>> blackList = sgBlocks.add(new ItemListSetting.Builder()
        .name("blacklist")
        .description("Blocks to never use.")
        .defaultValue(List.of())
        .build()
    );

    private final Setting<Boolean> replaceNonAir = sgBehavior.add(new BoolSetting.Builder()
        .name("replace-non-air")
        .description("Also replace non-air replaceable blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgBehavior.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disable after all blocks are placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgBehavior.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate towards placement position.")
        .defaultValue(true)
        .build()
    );

    private int tickCounter = 0;
    private final List<BlockPos> placementQueue = new ArrayList<>();
    private boolean done = false;

    public SelfTrap() {
        super(
            Categories.Combat,
            "Self Trap",
            "Automatically places blocks around you to form a trap/cage."
        );
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        placementQueue.clear();
        done = false;
        buildPlacementQueue();
    }

    private void buildPlacementQueue() {
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int w = width.get();
        int h = height.get();

        for (int y = 0; y <= h; y++) {
            for (int x = -w; x <= w; x++) {
                for (int z = -w; z <= w; z++) {
                    if (x == 0 && z == 0) continue;
                    if (y == 0 && !includeBase.get()) continue;
                    if (y == h && !includeRoof.get()) continue;

                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);

                    if (!state.isAir()) continue;

                    placementQueue.add(pos);
                }
            }
        }

        placementQueue.sort(Comparator.comparingDouble(p ->
            mc.player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)
        ));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) {
            toggle();
            return;
        }

        tickCounter++;

        if (done && placementQueue.isEmpty()) {
            if (autoDisable.get()) toggle();
            return;
        }

        if (tickCounter < delay.get()) return;
        tickCounter = 0;

        int slot = findBlockSlot();
        if (slot == -1) {
            warning("No valid blocks found.");
            toggle();
            return;
        }

        int placed = 0;

        for (int i = placementQueue.size() - 1; i >= 0 && placed < 1; i--) {
            BlockPos pos = placementQueue.get(i);

            BlockState state = mc.level.getBlockState(pos);
            if (!state.isAir()) {
                placementQueue.remove(i);
                continue;
            }

            if (rotate.get()) {
                mc.player.setYRot((float) Math.toDegrees(
                    Math.atan2(pos.getZ() + 0.5 - mc.player.getZ(), pos.getX() + 0.5 - mc.player.getX())
                ) + 90f);
            }

            int prevSlot = mc.player.getInventory().getSelectedSlot();
            InvUtils.move().from(slot).toHotbar(0);

            mc.gameMode.useItemOn(
                mc.player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                    new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5),
                    Direction.UP,
                    pos.below(),
                    false
                )
            );

            InvUtils.move().from(0).toHotbar(prevSlot);
            placementQueue.remove(i);
            placed++;
        }

        if (placementQueue.isEmpty()) {
            done = true;
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getItem(i).getItem();
            if (item instanceof BlockItem blockItem) {
                if (blackList.get().contains(item)) continue;
                if (item == Blocks.BEDROCK.asItem()) continue;
                return i;
            }
        }

        for (int i = 9; i < 36; i++) {
            Item item = mc.player.getInventory().getItem(i).getItem();
            if (item instanceof BlockItem) {
                if (blackList.get().contains(item)) continue;
                if (item == Blocks.BEDROCK.asItem()) continue;
                return i;
            }
        }

        return -1;
    }
}
