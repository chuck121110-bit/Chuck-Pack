package net.chuck.chuckpack.pathfinding.engine;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class NetherBiomeRisk
{
	private static final Set<ResourceKey<Biome>> FEATURE_RISK_BIOMES = Set
		.of(Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS);
	private final ConcurrentHashMap<Long, Boolean> chunkRisk =
		new ConcurrentHashMap();

	public static NetherBiomeRisk create(long seed)
	{
		// TODO: Port to 26.1.2 API — Climate.ParameterList/Sampler API changed significantly
		// Previous implementation used VanillaRegistries.createLookup(), Climate.ParameterList, etc.
		throw new UnsupportedOperationException("NetherBiomeRisk.create() not yet ported to MC 26.1.2");
	}

	public ResourceKey<Biome> biomeAt(int blockX, int blockZ)
	{
		// TODO: Port to 26.1.2 API
		throw new UnsupportedOperationException("NetherBiomeRisk.biomeAt() not yet ported to MC 26.1.2");
	}

	public boolean isRiskyChunk(int chunkX, int chunkZ)
	{
		return this.chunkRisk
			.computeIfAbsent(ChunkPos.pack(chunkX, chunkZ), key -> {
				// TODO: Port biomeAt() first, then re-enable this
				return false;
			});
	}
}
