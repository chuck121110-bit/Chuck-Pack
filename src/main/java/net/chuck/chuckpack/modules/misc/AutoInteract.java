package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

/**
 * Ported from Meteorist by zgoly.
 * Original: zgoly.meteorist.modules.AutoInteract
 */
public class AutoInteract extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("The block to interact with.")
            .filter(block -> block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock || block instanceof ButtonBlock || block instanceof LeverBlock)
            .defaultValue(BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof DoorBlock || block instanceof FenceGateBlock).toList())
            .build()
    );
    private final Setting<Double> innerRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("inner-range")
            .description("The range to interact with blocks.")
            .defaultValue(3)
            .min(0)
            .build()
    );
    private final Setting<Integer> outerRange = sgGeneral.add(new IntSetting.Builder()
            .name("outer-range")
            .description("The range to stop interacting with blocks.")
            .defaultValue(4)
            .min(1)
            .build()
    );
    private final Setting<BlockPos> rangePosOffset = sgGeneral.add(new BlockPosSetting.Builder()
            .name("range-pos-offset")
            .description("The offset of the range position.")
            .defaultValue(new BlockPos(0, 1, 0))
            .build()
    );
    private final Setting<Boolean> swingHand = sgGeneral.add(new BoolSetting.Builder()
            .name("swing-InteractionHand")
            .description("Swing InteractionHand client-side.")
            .defaultValue(true)
            .build()
    );

    public AutoInteract() {
        super(Categories.Misc, "auto-interact", "Automatically interacts with interactable blocks like doors, trapdoors, etc. (Ported from Meteorist by zgoly)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        BlockPos center = mc.player.blockPosition();
        int r = outerRange.get();
        for (int x = center.getX() - r; x <= center.getX() + r; x++) {
            for (int y = center.getY() - r; y <= center.getY() + r; y++) {
                for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState blockState = mc.level.getBlockState(blockPos);

                    if (blockState.getBlock() instanceof DoorBlock && blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
                        continue;
                    if (blockState.getBlock() instanceof DoorBlock && blockState.getValue(DoorBlock.POWERED))
                        continue;

                    if (blocks.get().contains(blockState.getBlock())) {
                        boolean shouldOpen = PlayerUtils.distanceTo(Vec3.atCenterOf(blockPos)) <= innerRange.get();
                        boolean isOpen;
                        if (blockState.getBlock() instanceof DoorBlock) {
                            isOpen = blockState.getValue(DoorBlock.OPEN);
                        } else if (blockState.getBlock() instanceof FenceGateBlock) {
                            isOpen = blockState.getValue(FenceGateBlock.OPEN);
                        } else if (blockState.getBlock() instanceof TrapDoorBlock) {
                            isOpen = blockState.getValue(TrapDoorBlock.OPEN);
                        } else if (blockState.getBlock() instanceof ButtonBlock) {
                            isOpen = blockState.getValue(ButtonBlock.POWERED);
                        } else if (blockState.getBlock() instanceof LeverBlock) {
                            isOpen = blockState.getValue(LeverBlock.POWERED);
                        } else {
                            isOpen = false;
                        }

                        if (shouldOpen != isOpen) {
                            BlockUtils.interact(new BlockHitResult(
                                Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false
                            ), InteractionHand.MAIN_HAND, swingHand.get());
                            return;
                        }
                    }
                }
            }
        }
    }
}
