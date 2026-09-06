package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.chuck.chuckpack.modules.world.OreSim;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = baritone.cache.WorldScanner.class, remap = false)
public class BaritoneWorldScannerMixin {
    @Inject(method = "scanChunkRadius", at = @At("HEAD"), cancellable = true, remap = false)
    private void chuckpack$oreSimScanChunkRadius(baritone.api.utils.IPlayerContext ctx, baritone.api.utils.BlockOptionalMetaLookup filter, int max, int y, int maxSearchRadius, CallbackInfoReturnable<List<BlockPos>> cir) {
        try {
            OreSim oreSim = Modules.get().get(OreSim.class);
            if (oreSim == null || !oreSim.baritone()) return;
            if (filter == null) return;
            // Check if filter contains any ore that OreSim is simulating
            List<BlockPos> goals = oreSim.getBaritoneGoalsForFilter(filter);
            // If OreSim has goals for this filter and we are mining an ore, use them
            // Also check if filter's blocks are actually ores we simulate (to avoid hijacking non-ore mining)
            // We do this by checking if goals is non-empty and filter matches at least one ore
            if (goals == null || goals.isEmpty()) return;
            // Only hijack if filter is for ores (check if any requested block is an ore)
            // Use helper to determine if filter matches OreSim's active ores
            boolean isOreFilter = false;
            try {
                java.lang.reflect.Method blocksMethod = filter.getClass().getMethod("blocks");
                java.util.Collection<?> blocks = (java.util.Collection<?>) blocksMethod.invoke(filter);
                for (Object bom : blocks) {
                    java.lang.reflect.Method getBlock = bom.getClass().getMethod("getBlock");
                    Object block = getBlock.invoke(bom);
                    if (block != null) {
                        String name = block.toString().toLowerCase();
                        if (name.contains("coal") || name.contains("iron") || name.contains("gold") || name.contains("redstone") || name.contains("diamond") || name.contains("lapis") || name.contains("copper") || name.contains("emerald") || name.contains("quartz") || name.contains("debris")) {
                            isOreFilter = true;
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {
                isOreFilter = true; // fallback: assume ore
            }
            if (!isOreFilter) return;
            // Limit to max requested
            if (goals.size() > max) {
                goals = goals.subList(0, max);
            }
            cir.setReturnValue(goals);
            cir.cancel();
        } catch (Throwable ignored) {}
    }
}
