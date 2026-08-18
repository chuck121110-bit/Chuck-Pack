package net.chuck.chuckpack.autoflypath;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PathFlightConfig
{
	public boolean flightProcess = true;
	public boolean assumeFlightHack = true;
	public double flightHorizontalSpeed = 1.0;
	public double flightVerticalSpeed = 10.0;
	public double flightArrivalRadius = 5.0;
	public boolean flightPredictTerrain;
	public long flightSeed;
	public final Map<String, Long> flightServerSeeds = new LinkedHashMap<>();
	public boolean flightAntiHunger = true;
	public boolean flightFaceTravel;
	public boolean flightRenderPath = true;
	public boolean flightDebug;
	public boolean flightVerbose;
	public int flightCruiseHeight;
	public boolean waitChunks = true;
}
