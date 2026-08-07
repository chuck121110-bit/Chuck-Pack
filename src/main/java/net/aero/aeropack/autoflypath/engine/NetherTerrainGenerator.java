package net.aero.aeropack.autoflypath.engine;

import net.minecraft.world.level.levelgen.DensityFunction;

public final class NetherTerrainGenerator
{
	private static final int TERRAIN_MIN_Y = 0;
	private static final int TERRAIN_HEIGHT = 128;
	private static final int FEATURE_MARGIN = 8;

	private final DensityFunction densityFunction;

	public NetherTerrainGenerator(long seed)
	{
		// TODO: Port to 26.1.2 API — RandomState.create() and BuiltinRegistries.createWrapperLookup() changed
		// Previous implementation: RandomState.create(BuiltinRegistries.createWrapperLookup(), NoiseGeneratorSettings.NETHER, seed)
		throw new UnsupportedOperationException("NetherTerrainGenerator() not yet ported to MC 26.1.2");
	}

	private static double slide(double v, int y)
	{
		double top = clampedGradient(y, 104, 128, 1.0, 0.0);
		v = 0.9375 + top * (v - 0.9375);
		double bottom = clampedGradient(y, -8, 24, 0.0, 1.0);
		return 2.5 + bottom * (v - 2.5);
	}

	private static double clampedGradient(int y, int fromY, int toY,
		double fromValue, double toValue)
	{
		double t = Math.max(0.0,
			Math.min(1.0, (double)(y - fromY) / (double)(toY - fromY)));
		return fromValue + t * (toValue - fromValue);
	}

	public void generate(FlightGrid grid, int cx, int cz)
	{
		this.generate(grid, cx, cz, false);
	}

	public void generate(FlightGrid grid, int cx, int cz, boolean featureRisk)
	{
		// TODO: Port generate() — depends on densityFunction which requires ported constructor
		throw new UnsupportedOperationException("NetherTerrainGenerator.generate() not yet ported to MC 26.1.2");
	}
}
