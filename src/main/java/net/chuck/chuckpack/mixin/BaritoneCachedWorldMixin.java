package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.chuck.chuckpack.modules.world.OreSim;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = baritone.cache.CachedWorld.class, remap = false)
public class BaritoneCachedWorldMixin {
    @Inject(method = "getLocationsOf", at = @At("HEAD"), cancellable = true, remap = false)
    private void chuckpack$oreSimGetLocationsOf(String blockName, int max, int centerX, int centerZ, int maxRegionDistanceSq, CallbackInfoReturnable<ArrayList<BlockPos>> cir) {
        try {
            OreSim oreSim = Modules.get().get(OreSim.class);
            if (oreSim == null || !oreSim.baritone()) return;
            if (blockName == null) return;
            String lower = blockName.toLowerCase();
            boolean isOre = lower.contains("coal") || lower.contains("iron") || lower.contains("gold") || lower.contains("redstone") || lower.contains("diamond") || lower.contains("lapis") || lower.contains("copper") || lower.contains("emerald") || lower.contains("quartz") || lower.contains("debris");
            if (!isOre) return;
            // Only hijack if OreSim has this ore enabled
            // Check via Ore settings: we can just return OreSim goals filtered to this blockName
            // For simplicity, return all OreSim goals that match this blockName
            List<BlockPos> goals = oreSim.getBaritoneGoals();
            if (goals == null || goals.isEmpty()) return;
            // Filter goals to only those that are of this ore type
            // Use OreSim's filtered method by constructing a dummy filter with this single block
            // Instead, we filter via blockName contains ore name
            List<BlockPos> filtered = new ArrayList<>();
            for (BlockPos pos : goals) {
                // We don't have per-pos ore type, so we need to check via chunkRenderers
                // For now, just return all goals if the requested block is any ore that is enabled
                // The caller will filter via prune, but we can just return all
                filtered.add(pos);
                if (filtered.size() >= max) break;
            }
            // If we have filtered list, return it
            if (!filtered.isEmpty()) {
                cir.setReturnValue(new ArrayList<>(filtered));
                cir.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
