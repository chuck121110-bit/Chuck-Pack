package net.aero.aeropack.autoflypath.engine;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.noise.NoiseConfig;

public final class NetherBiomeRisk
{
	private static final Set<RegistryKey<Biome>> FEATURE_RISK_BIOMES = Set
		.of(BiomeKeys.CRIMSON_FOREST, BiomeKeys.WARPED_FOREST, BiomeKeys.BASALT_DELTAS);

	private final MultiNoiseUtil.Entries<RegistryKey<Biome>> entries;
	private final MultiNoiseUtil.MultiNoiseSampler sampler;
	private final ConcurrentHashMap<Long, Boolean> chunkRisk =
		new ConcurrentHashMap();

	private NetherBiomeRisk(
		MultiNoiseUtil.Entries<RegistryKey<Biome>> entries,
		MultiNoiseUtil.MultiNoiseSampler sampler)
	{
		this.entries = entries;
		this.sampler = sampler;
	}

	public static NetherBiomeRisk create(long seed)
	{
		Map<MultiNoiseBiomeSourceParameterList.Preset, MultiNoiseUtil.Entries<RegistryKey<Biome>>> presets =
			MultiNoiseBiomeSourceParameterList.getPresetToEntriesMap();

		MultiNoiseUtil.Entries<RegistryKey<Biome>> netherEntries =
			presets.get(MultiNoiseBiomeSourceParameterList.Preset.NETHER);

		NoiseConfig noiseConfig = NoiseConfig.create(
			BuiltinRegistries.createWrapperLookup(),
			ChunkGeneratorSettings.NETHER,
			seed);

		return new NetherBiomeRisk(netherEntries, noiseConfig.getMultiNoiseSampler());
	}

	public RegistryKey<Biome> biomeAt(int blockX, int blockZ)
	{
		MultiNoiseUtil.NoiseValuePoint point = this.sampler.sample(
			blockX / 4, 64 / 4, blockZ / 4);
		return this.entries.getValue(point);
	}

	public boolean isRiskyChunk(int chunkX, int chunkZ)
	{
		return this.chunkRisk
			.computeIfAbsent(new ChunkPos(chunkX, chunkZ).toLong(), key -> {
				int bx = (chunkX << 4) + 8;
				int bz = (chunkZ << 4) + 8;
				for(int[] o : new int[][]{{0, 0}, {-8, -8}, {-8, 7}, {7, -8}, {7, 7}})
				{
					if(!FEATURE_RISK_BIOMES
						.contains(this.biomeAt(bx + o[0], bz + o[1])))
						continue;
					return true;
				}
				return false;
			});
	}
}
