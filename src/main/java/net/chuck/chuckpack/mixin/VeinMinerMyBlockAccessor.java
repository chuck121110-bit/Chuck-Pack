package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * VeinMiner.MyBlock is a package-private inner class (not accessible to us
 * directly by type), so we reach its blockPos field and its AABB-rendering
 * method through a Mixin accessor/invoker interface instead.
 */
@Mixin(targets = "meteordevelopment.meteorclient.systems.modules.world.VeinMiner$MyBlock")
public interface VeinMinerMyBlockAccessor {
    @Accessor("blockPos")
    BlockPos chuckpack\$getBlockPos();

    @Invoker("render")
    void chuckpack\$invokeRender(Render3DEvent event);
}
