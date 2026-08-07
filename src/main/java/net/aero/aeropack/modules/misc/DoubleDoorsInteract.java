package net.aero.aeropack.modules.misc;

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Ported from Meteorist by zgoly.
 * Original: zgoly.meteorist.modules.DoubleDoorsInteract
 */
public class DoubleDoorsInteract extends Module {
    private boolean isInteracting = false;

    public DoubleDoorsInteract() {
        super(Categories.Misc, "double-doors-interact", "Open both doors with one interaction. (Ported from Meteorist by zgoly)");
    }

    @EventHandler
    private void onInteract(InteractBlockEvent event) {
        if (isInteracting) return;

        isInteracting = true;
        BlockPos doorPos = event.result.getBlockPos();
        if (mc.world == null) { isInteracting = false; return; }

        var blockState = mc.world.getBlockState(doorPos);
        if (blockState.getBlock() instanceof DoorBlock) {
            Direction doorFacing = blockState.get(DoorBlock.FACING);
            DoorHinge doorHinge = blockState.get(DoorBlock.HINGE);

            BlockPos otherDoorPos;
            if (doorHinge == DoorHinge.LEFT) {
                otherDoorPos = doorPos.offset(doorFacing.rotateYClockwise());
            } else {
                otherDoorPos = doorPos.offset(doorFacing.rotateYCounterclockwise());
            }

            var otherBlockState = mc.world.getBlockState(otherDoorPos);
            if (otherBlockState.getBlock() instanceof DoorBlock) {
                if (blockState.get(DoorBlock.HALF) == otherBlockState.get(DoorBlock.HALF)
                        && blockState.get(DoorBlock.HINGE) != otherBlockState.get(DoorBlock.HINGE)
                        && blockState.get(DoorBlock.OPEN) == otherBlockState.get(DoorBlock.OPEN)) {
                    BlockUtils.interact(new BlockHitResult(
                        Vec3d.ofCenter(otherDoorPos), Direction.UP, otherDoorPos, false
                    ), Hand.MAIN_HAND, false);
                }
            }
        }
        isInteracting = false;
    }
}
