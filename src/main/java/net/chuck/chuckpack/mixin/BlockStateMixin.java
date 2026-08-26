package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void chuckpack$makeLavaSolid(BlockGetter blockGetter, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (pos == null) return;
        if (Modules.get() == null) return;
        AutoFly autoFly = Modules.get().get(AutoFly.class);
        if (autoFly == null || !autoFly.isActive()) return;

        BlockState self = (BlockState)(Object)this;
        boolean lava = self.getFluidState().is(FluidTags.LAVA);
        boolean fire = self.is(Blocks.FIRE) || self.is(Blocks.SOUL_FIRE);

        if (lava || fire) {
            cir.setReturnValue(Shapes.block());
            cir.cancel();
            return;
        }

        BlockState belowState = blockGetter.getBlockState(pos.below());
        boolean belowLava = self.getFluidState().isEmpty()
            && belowState.getFluidState().is(FluidTags.LAVA);
        boolean belowFire = self.getFluidState().isEmpty()
            && (belowState.is(Blocks.FIRE) || belowState.is(Blocks.SOUL_FIRE));

        if (belowLava || belowFire) {
            cir.setReturnValue(Shapes.block());
            cir.cancel();
        }
    }
}
