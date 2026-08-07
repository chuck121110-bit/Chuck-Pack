package net.aero.aeropack.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
            .filter(block -> block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapdoorBlock || block instanceof ButtonBlock || block instanceof LeverBlock)
            .defaultValue(Registries.BLOCK.stream().filter(block -> block instanceof DoorBlock || block instanceof FenceGateBlock).toList())
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
            .name("swing-hand")
            .description("Swing hand client-side.")
            .defaultValue(true)
            .build()
    );

    public AutoInteract() {
        super(Categories.Misc, "auto-interact", "Automatically interacts with interactable blocks like doors, trapdoors, etc. (Ported from Meteorist by zgoly)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        for (BlockPos blockPos : BlockPos.iterate(
                mc.player.getBlockPos().add(-outerRange.get(), -outerRange.get(), -outerRange.get()),
                mc.player.getBlockPos().add(outerRange.get(), outerRange.get(), outerRange.get())
        )) {
            BlockState blockState = mc.world.getBlockState(blockPos);

            if (blockState.getBlock() instanceof DoorBlock && blockState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
                continue;
            if (blockState.getBlock() instanceof DoorBlock && !DoorBlock.canOpenByHand(mc.world, blockPos))
                continue;

            if (blocks.get().contains(blockState.getBlock())) {
                boolean shouldOpen = PlayerUtils.distanceTo(Vec3d.ofCenter(blockPos)) <= innerRange.get();
                boolean isOpen = switch (blockState.getBlock()) {
                    case DoorBlock ignored -> blockState.get(DoorBlock.OPEN);
                    case FenceGateBlock ignored -> blockState.get(FenceGateBlock.OPEN);
                    case TrapdoorBlock ignored -> blockState.get(TrapdoorBlock.OPEN);
                    case ButtonBlock ignored -> blockState.get(ButtonBlock.POWERED);
                    case LeverBlock ignored -> blockState.get(LeverBlock.POWERED);
                    default -> false;
                };

                if (shouldOpen != isOpen) {
                    BlockUtils.interact(new BlockHitResult(
                        Vec3d.ofCenter(blockPos), Direction.UP, blockPos, false
                    ), Hand.MAIN_HAND, swingHand.get());
                    break;
                }
            }
        }
    }
}
