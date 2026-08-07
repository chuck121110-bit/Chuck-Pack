package net.aero.aeropack.autoflypath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.aero.aeropack.autoflypath.flight.FlightController;

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
	private static volatile long landingProtectionUntilMs;

	private PathFlightRuntime()
	{
	}

	public static void initialize(PathFlightConfig newConfig)
	{
		if(controller != null)
			controller.stop();

		config = newConfig;
		controller = new FlightController(config);
	}

	public static void shutdown()
	{
		if(controller != null)
		{
			try { controller.stop(); } catch(Exception ignored) {}
		}
		controller = null;
		config = null;
	}

	public static void tick()
	{
		FlightController activeController = controller;
		if(activeController != null)
			activeController.clientTick();
	}

	public static PathFlightConfig config()
	{
		return config;
	}

	public static FlightController controller()
	{
		return controller;
	}

	public static boolean isPathFlightActive()
	{
		return controller != null && controller.isActive();
	}

	public static boolean isPathFlightDescending()
	{
		return isPathFlightActive() && controller.isDescending();
	}

	public static boolean isLandingProtectionActive()
	{
		return System.currentTimeMillis() < landingProtectionUntilMs;
	}

	public static void protectLanding()
	{
		landingProtectionUntilMs = System.currentTimeMillis() + 500L;
	}
}
