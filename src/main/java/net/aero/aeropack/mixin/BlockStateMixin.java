package net.aero.aeropack.mixin;

import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class BlockStateMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void aeropack$makeLavaSolid(BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (pos == null) return;
        if (Modules.get() == null) return;
        AutoFly autoFly = Modules.get().get(AutoFly.class);
        if (autoFly == null || !autoFly.isActive()) return;

        BlockState self = (BlockState)(Object)this;
        boolean lava = self.getFluidState().isIn(FluidTags.LAVA);
        boolean fire = self.isOf(Blocks.FIRE) || self.isOf(Blocks.SOUL_FIRE);

        if (lava || fire) {
            cir.setReturnValue(VoxelShapes.fullCube());
            cir.cancel();
            return;
        }

        BlockState belowState = world.getBlockState(pos.down());
        boolean belowLava = self.getFluidState().isEmpty()
            && belowState.getFluidState().isIn(FluidTags.LAVA);
        boolean belowFire = self.getFluidState().isEmpty()
            && (belowState.isOf(Blocks.FIRE) || belowState.isOf(Blocks.SOUL_FIRE));

        if (belowLava || belowFire) {
            cir.setReturnValue(VoxelShapes.fullCube());
            cir.cancel();
        }
    }
}
