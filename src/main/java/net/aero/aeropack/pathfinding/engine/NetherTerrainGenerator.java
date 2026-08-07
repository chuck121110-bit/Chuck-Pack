package net.aero.aeropack.pathfinding.engine;

public final class NetherTerrainGenerator
{
	private static final int TERRAIN_MIN_Y = 0;
	private static final int TERRAIN_HEIGHT = 128;
	private static final int SEA_LEVEL = 32;
	private static final int FEATURE_MARGIN = 8;

	public NetherTerrainGenerator(long seed)
	{
		// TODO: Port to 26.1.2 API — BlendedNoise.createUnseeded() and withNewRandom() changed
		throw new UnsupportedOperationException("NetherTerrainGenerator() not yet ported to MC 26.1.2");
	}

	private static double slide(double v, int y)
	{
		double top =
			NetherTerrainGenerator.clampedGradient(y, 104, 128, 1.0, 0.0);
		v = 0.9375 + top * (v - 0.9375);
		double bottom =
			NetherTerrainGenerator.clampedGradient(y, -8, 24, 0.0, 1.0);
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
		// TODO: Port generate() — depends on noise field which requires ported constructor
		throw new UnsupportedOperationException("NetherTerrainGenerator.generate() not yet ported to MC 26.1.2");
	}

	private static double trilerp(double[][][] c, int xi, int yi, int zi,
		double fx, double fy, double fz)
	{
		double x00 = c[xi][yi][zi] + fx * (c[xi + 1][yi][zi] - c[xi][yi][zi]);
		double x01 = c[xi][yi][zi + 1]
			+ fx * (c[xi + 1][yi][zi + 1] - c[xi][yi][zi + 1]);
		double x10 = c[xi][yi + 1][zi]
			+ fx * (c[xi + 1][yi + 1][zi] - c[xi][yi + 1][zi]);
		double x11 = c[xi][yi + 1][zi + 1]
			+ fx * (c[xi + 1][yi + 1][zi + 1] - c[xi][yi + 1][zi + 1]);
		double z0 = x00 + fz * (x01 - x00);
		double z1 = x10 + fz * (x11 - x10);
		return z0 + fy * (z1 - z0);
	}
}
