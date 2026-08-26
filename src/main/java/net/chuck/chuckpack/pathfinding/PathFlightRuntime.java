package net.chuck.chuckpack.pathfinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.chuck.chuckpack.pathfinding.flight.FlightController;

public final class PathFlightRuntime
{
	public static final ExecutorService EXECUTOR =
		Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "AutoFly-Path-Worker");
			thread.setDaemon(true);
			return thread;
		});
	
	private static PathFlightConfig config;
	private static FlightController controller;
	
	private PathFlightRuntime()
	{}
	
	public static void initialize(PathFlightConfig newConfig)
	{
		if(controller != null)
			controller.stop();
		config = newConfig;
		controller = new FlightController(config);
	}
	
	public static PathFlightConfig config()
	{
		return config;
	}
	
	public static FlightController controller()
	{
		return controller;
	}
}
