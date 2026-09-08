package net.chuck.chuckpack.modules.movement;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.chuck.chuckpack.autoflypath.PathFlightConfig;
import net.chuck.chuckpack.autoflypath.PathFlightRuntime;
import net.chuck.chuckpack.autoflypath.flight.BetterBlockPos;
import net.chuck.chuckpack.autoflypath.flight.FlightController;
import net.chuck.chuckpack.modules.misc.AntiSocial;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.chuck.chuckpack.mixin.LocalPlayerAccessor;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.chuck.chuckpack.pathfinding.PathFinder;
import net.chuck.chuckpack.pathfinding.PathProcessor;
import net.chuck.chuckpack.pathfinding.FlyPathProcessor;

public final class AutoFly extends Module
{
	private final SettingGroup sgGeneral = settings.getDefaultGroup();
	private final SettingGroup sgSpeed = settings.createGroup("Speed");
	private final SettingGroup sgDanger = settings.createGroup("Danger Avoidance");
	private final SettingGroup sgAutomation = settings.createGroup("Automation");
	private final SettingGroup sgRender = settings.createGroup("Render");
	private final SettingGroup sgDebug = settings.createGroup("Debug");

	// ═══════════════════════════════════════════════════════════════
	//  Legacy CEVAPI Pathfinding Settings
	// ═══════════════════════════════════════════════════════════════

	private final SettingGroup sgLegacy = settings.createGroup("Legacy Pathfinding");

	private final Setting<Boolean> fallingAllowed = sgLegacy.add(new BoolSetting.Builder()
		.name("falling-allowed")
		.description("Allow falling in legacy pathfinding")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> divingAllowed = sgLegacy.add(new BoolSetting.Builder()
		.name("diving-allowed")
		.description("Allow water/lava diving in legacy pathfinding")
		.defaultValue(true)
		.build()
	);

	private final Setting<Integer> thinkSpeed = sgLegacy.add(new IntSetting.Builder()
		.name("think-speed")
		.description("Nodes processed per tick in legacy pathfinding")
		.defaultValue(1024)
		.min(256)
		.max(8192)
		.build()
	);

	private final Setting<Integer> thinkTime = sgLegacy.add(new IntSetting.Builder()
		.name("think-time")
		.description("Maximum think iterations in legacy pathfinding")
		.defaultValue(200)
		.min(50)
		.max(1000)
		.build()
	);

	private final Setting<Double> startSpeed = sgSpeed.add(new DoubleSetting.Builder()
		.name("start-speed")
		.description("How fast the fly starts (blocks per tick).")
		.defaultValue(0.05)
		.min(0.01)
		.sliderMax(1.0)
		.build()
	);

	private final Setting<Double> rampUpIncrement = sgSpeed.add(new DoubleSetting.Builder()
		.name("ramp-up-increment")
		.description("How much speed increases per tick when not being set back.")
		.defaultValue(0.01)
		.min(0.005)
		.sliderMax(0.1)
		.build()
	);

	private final Setting<Double> rampUpLimit = sgSpeed.add(new DoubleSetting.Builder()
		.name("ramp-up-limit")
		.description("Maximum flight speed. It will ramp up to this speed.")
		.defaultValue(0.1)
		.min(0.05)
		.sliderMax(2.0)
		.build()
	);

	private final Setting<Double> arrivalRadius = sgGeneral.add(new DoubleSetting.Builder()
		.name("arrival-radius")
		.description("Distance at which the target is considered reached.")
		.defaultValue(5.0)
		.min(1.0)
		.sliderMax(32.0)
		.build()
	);

	private final Setting<Boolean> autoDisconnectOnArrival = sgGeneral.add(new BoolSetting.Builder()
		.name("auto-disconnect-on-arrival")
		.description("Automatically disconnect when AutoFly reaches the destination.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Double> disconnectRadius = sgGeneral.add(new DoubleSetting.Builder()
		.name("disconnect-radius")
		.description("Disconnect when within this distance of the target (0 = use arrival radius).")
		.defaultValue(0.0)
		.min(0.0)
		.sliderMax(64.0)
		.visible(autoDisconnectOnArrival::get)
		.build()
	);

	private final Setting<Boolean> jumpDisengage = sgGeneral.add(new BoolSetting.Builder()
		.name("jump-disengage")
		.description("Only jump key disables AutoFly. Requires double-jump. All other movement keys are ignored.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> waitChunksOverworld = sgGeneral.add(new BoolSetting.Builder()
		.name("wait-chunks-overworld")
		.description("Wait for chunks to load in the Overworld before continuing through them.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> waitChunksNether = sgGeneral.add(new BoolSetting.Builder()
		.name("wait-chunks-nether")
		.description("Wait for chunks to load in the Nether before continuing through them.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> waitChunksEnd = sgGeneral.add(new BoolSetting.Builder()
		.name("wait-chunks-end")
		.description("Wait for chunks to load in the End before continuing through them.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> dodgeGhastFireballs = sgDanger.add(new BoolSetting.Builder()
		.name("dodge-ghast-fireballs")
		.description("Automatically teleport away from ghast fireballs.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Double> ghastTeleportDistance = sgDanger.add(new DoubleSetting.Builder()
		.name("ghast-teleport-distance")
		.description("How many blocks to teleport away from ghast fireballs.")
		.defaultValue(20.0)
		.min(5.0)
		.sliderMax(100.0)
		.visible(dodgeGhastFireballs::get)
		.build()
	);

	private final Setting<Double> ghastScanRange = sgDanger.add(new DoubleSetting.Builder()
		.name("ghast-scan-range")
		.description("How far to scan for ghast fireballs.")
		.defaultValue(50.0)
		.min(10.0)
		.sliderMax(200.0)
		.visible(dodgeGhastFireballs::get)
		.build()
	);

	private final Setting<Boolean> dodgeArrows = sgDanger.add(new BoolSetting.Builder()
		.name("dodge-arrows")
		.description("Automatically teleport away from incoming arrows.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Double> arrowTeleportDistance = sgDanger.add(new DoubleSetting.Builder()
		.name("arrow-teleport-distance")
		.description("How many blocks to teleport away from arrows.")
		.defaultValue(15.0)
		.min(5.0)
		.sliderMax(100.0)
		.visible(dodgeArrows::get)
		.build()
	);

	private final Setting<Double> arrowScanRange = sgDanger.add(new DoubleSetting.Builder()
		.name("arrow-scan-range")
		.description("How far to scan for arrows.")
		.defaultValue(40.0)
		.min(10.0)
		.sliderMax(200.0)
		.visible(dodgeArrows::get)
		.build()
	);

	private final Setting<Boolean> dodgeLavaAndFire = sgDanger.add(new BoolSetting.Builder()
		.name("dodge-lava-and-fire")
		.description("Automatically teleport away from nearby lava and fire blocks.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Double> lavaTeleportDistance = sgDanger.add(new DoubleSetting.Builder()
		.name("lava-teleport-distance")
		.description("How many blocks to teleport away from lava and fire.")
		.defaultValue(8.0)
		.min(3.0)
		.sliderMax(32.0)
		.visible(dodgeLavaAndFire::get)
		.build()
	);

	private final Setting<Double> lavaScanRange = sgDanger.add(new DoubleSetting.Builder()
		.name("lava-scan-range")
		.description("How far to scan for lava and fire blocks.")
		.defaultValue(6.0)
		.min(2.0)
		.sliderMax(16.0)
		.visible(dodgeLavaAndFire::get)
		.build()
	);

	private final Setting<Boolean> autoEat = sgAutomation.add(new BoolSetting.Builder()
		.name("auto-eat")
		.description("Automatically enable the AutoEat module when AutoFly is active.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> autoAntiSocial = sgAutomation.add(new BoolSetting.Builder()
		.name("auto-anti-social")
		.description("Automatically enable the AntiSocial module when AutoFly is active.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> autoLogOnDamage = sgAutomation.add(new BoolSetting.Builder()
		.name("auto-log-on-damage")
		.description("Automatically disconnect when health drops below a threshold.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Integer> autoLogThreshold = sgAutomation.add(new IntSetting.Builder()
		.name("auto-log-threshold")
		.description("Health threshold in HP to trigger auto-log (20 HP = 10 hearts). Default 8 HP = 4 hearts.")
		.defaultValue(8)
		.min(1)
		.max(20)
		.visible(autoLogOnDamage::get)
		.build()
	);

	private final Setting<Boolean> autoLogOnUnreachable = sgAutomation.add(new BoolSetting.Builder()
		.name("auto-log-on-unreachable")
		.description("Automatically disconnect when destination appears unreachable.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> pathDebug = sgDebug.add(new BoolSetting.Builder()
		.name("path-debug")
		.description("Show path debug messages (stuck, rerouting, etc).")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> speedDebug = sgDebug.add(new BoolSetting.Builder()
		.name("speed-debug")
		.description("Show speed debug messages (setbacks, ramp changes).")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> verboseDebug = sgDebug.add(new BoolSetting.Builder()
		.name("verbose")
		.description("Log every tick's full state to chat AND game logs for real-time diagnostics.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> antiKick = sgAutomation.add(new BoolSetting.Builder()
		.name("anti-kick")
		.description("Meteor Flight packet anti-kick 1:1: periodically fly down a bit to reset floating ticks.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Integer> antiKickDelay = sgAutomation.add(new IntSetting.Builder()
		.name("delay")
		.description("The amount of delay, in ticks, between flying down a bit and return to original position.")
		.defaultValue(20)
		.min(1)
		.sliderMax(200)
		.visible(antiKick::get)
		.build()
	);

	private final Setting<Integer> antiKickOffTime = sgAutomation.add(new IntSetting.Builder()
		.name("off-time")
		.description("The amount of delay, in ticks, to fly down a bit to reset floating ticks.")
		.defaultValue(1)
		.min(1)
		.sliderRange(1, 20)
		.visible(antiKick::get)
		.build()
	);

	private final Setting<Boolean> showPath = sgRender.add(new BoolSetting.Builder()
		.name("show-path")
		.description("Render FlyTo's calculated route in the world.")
		.defaultValue(true)
		.build()
	);

	private final Setting<SettingColor> pathColor = sgRender.add(new ColorSetting.Builder()
		.name("path-color")
		.description("Color of the flight path trail.")
		.defaultValue(new SettingColor(90, 97, 255, 200))
		.visible(showPath::get)
		.build()
	);

	private final PathFlightConfig pathFlightConfig = new PathFlightConfig();
	private final FlightController pathFlightController;
	private boolean flightWasEnabled;

	private PathFinder legacyPathFinder;
	private PathProcessor legacyPathProcessor;

	private double currentSpeed;
	private long lastSetbackMs = 0;
	private long suppressSetbackUntilMs = 0;
	private boolean speedFrozen = false;
	private int ticksSinceSetback = -1;
	private boolean speedLocked = false;

	private BlockPos lastTarget;
	private boolean arrived;
	private boolean autoEnabledEat;
	private boolean autoEnabledAntiSocial;
	private BlockPos mapClickTarget;
	private boolean mapClickYKnown;
	private Object autoFlyWaypoint;
	private Object existingWaypointOriginal;
	private String originalWaypointSymbol;

	private long lastDangerAvoidMs = 0;
	private int unreachableTicks = 0;
	private boolean wasPathing = false;
	private int antiKickDelayLeft;
	private int antiKickOffLeft;
	private boolean antiKickFlip;
	private float antiKickLastYaw;
	private double antiKickLastPacketY;

	// Explore mode for base finding: spiral out, reroute when stuck
	private boolean exploreMode;
	private int exploreRadius;
	private int exploreHeight;
	private double exploreAngle;
	private int exploreLeg;
	private BlockPos exploreTarget;
	private Vec3 exploreLastPos;
	private int exploreStuckTicks;
	private int exploreStuckCooldown;

	// Setback state machine
	// Phase 0 = ramp up, counting setbacks
	// Phase 1 = hold speed, every setback decreases, after 5s quiet stays held
	private int setbackPhase = 0;
	private int setbackCount = 0;
	private long quietStartMs = 0;
	private long lastRampUpMs = 0;
	private static final long QUIET_WINDOW_MS = 5000;
	private static final int INITIAL_SETBACK_THRESHOLD = 10;

	public int setbackDisplayTicks = 0;
	private boolean jumpWasPressed = false;
	private long lastJumpPressMs = 0;
	private static final long DOUBLE_JUMP_WINDOW_MS = 400;

    public AutoFly()
    {
        super(Categories.Movement, "auto-fly", "Flies you to a destination using terrain-aware pathfinding. Right-click Xaero\u2019s map to set a target.");
        PathFlightRuntime.initialize(pathFlightConfig);
        pathFlightController = PathFlightRuntime.controller();
    }

	@Override
	public void onActivate()
	{
		if(mc.player == null || mc.level == null)
		{
			error("Join a Level before enabling AutoFly.");
			toggle();
			return;
		}

		cleanStaleAutoFlyWaypoints();

		pathFlightController.stop();
		currentSpeed = startSpeed.get();
		lastRampUpMs = 0;
		suppressSetbackUntilMs = System.currentTimeMillis() + 3000;
		speedFrozen = false;
		ticksSinceSetback = -1;
		speedLocked = false;
		setbackCount = 0;
		quietStartMs = 0;
		setbackPhase = 0;
		lastTarget = null;
		arrived = false;
		lastDangerAvoidMs = 0;
		unreachableTicks = 0;
		wasPathing = false;
		antiKickDelayLeft = antiKickDelay.get();
		antiKickOffLeft = antiKickOffTime.get();
		antiKickFlip = false;
		antiKickLastYaw = 0;
		antiKickLastPacketY = Double.MAX_VALUE;
		exploreMode = false;
		exploreTarget = null;
		exploreLastPos = null;
		exploreStuckTicks = 0;
		exploreStuckCooldown = 0;
		jumpWasPressed = false;
		lastJumpPressMs = 0;

		flightWasEnabled = Modules.get().isActive(Flight.class);
		suspendFlight();

		syncConfig();
	}

	@Override
	public void onDeactivate()
	{
		pathFlightController.stop();
		legacyPathFinder = null;
		legacyPathProcessor = null;
		lastTarget = null;
		mapClickTarget = null;
		removeAutoFlyWaypoint();
		restoreExistingWaypointSymbol();
		arrived = false;
		setbackDisplayTicks = 0;
		if(mc.player != null) restoreFlight();
		stopAutoEnabledModules();
	}

	@EventHandler
	private void onScreenOpen(OpenScreenEvent event)
	{
		if(event.screen instanceof DisconnectedScreen && isActive())
		{
			toggle();
		}
	}

	private void stopAutoEnabledModules()
	{
		if(mc.player == null) return;
		try
		{
			if(autoEnabledEat)
			{
				var eat = Modules.get().get("auto-eat");
				if(eat != null && eat.isActive()) eat.toggle();
				autoEnabledEat = false;
			}

			if(autoEnabledAntiSocial)
			{
				var social = Modules.get().get(AntiSocial.class);
				if(social != null && social.isActive()) social.toggle();
				autoEnabledAntiSocial = false;
			}
		}catch(Exception ignored) {}
	}

	@EventHandler
	private void onTick(TickEvent.Pre event)
	{
		if(mc.player == null || mc.level == null) return;

		if(setbackDisplayTicks > 0) setbackDisplayTicks--;

		if(Modules.get().isActive(Flight.class))
		{
			info("Flight enabled — disabling AutoFly.");
			toggle();
			return;
		}

		if(pathFlightController.isActive())
		{
			boolean input;
			if(jumpDisengage.get())
			{
				boolean jumpNow = mc.options.keyJump.isDown();
				long now = System.currentTimeMillis();
				if(jumpNow && !jumpWasPressed)
				{
					if(now - lastJumpPressMs <= DOUBLE_JUMP_WINDOW_MS)
					{
						input = true;
					}
					else
					{
						lastJumpPressMs = now;
						input = false;
					}
				}
				else
				{
					input = false;
				}
				jumpWasPressed = jumpNow;
			}
			else
			{
				input = mc.options.keyUp.isDown()
					|| mc.options.keyDown.isDown()
					|| mc.options.keyLeft.isDown()
					|| mc.options.keyRight.isDown()
					|| mc.options.keyJump.isDown()
					|| mc.options.keyShift.isDown();
			}
			if(input)
			{
				info("Input detected — disabling.");
				toggle();
				return;
			}
		}

		pathFlightController.clientTick();

		boolean reachedGoal = pathFlightController.hasReachedGoal();

		boolean isPathingNow = pathFlightController.isActive();
		if(wasPathing && !isPathingNow && !arrived)
		{
			arrived = true;
			pathFlightController.stop();
			stopAutoEnabledModules();
			if(reachedGoal)
			{
				removeAutoFlyWaypoint();
				restoreExistingWaypointSymbol();
				info("Destination reached! Disabling AutoFly.");
				if(autoDisconnectOnArrival.get())
				{
					info("Arrived! Disconnecting...");
					net.chuck.chuckpack.modules.render.CoordinateLogout.hardDisconnect("AutoFly: Target reached");
				}
				else
				{
					toggle();
					return;
				}
			}
			else if(autoDisconnectOnArrival.get())
			{
				info("Flight interrupted. Disconnecting...");
				net.chuck.chuckpack.modules.render.CoordinateLogout.hardDisconnect("AutoFly: Flight interrupted");
				return;
			}
			else
			{
				info("Flight interrupted.");
				toggle();
				return;
			}
		}
		wasPathing = isPathingNow;

		if(exploreMode && !arrived) exploreTick();

		if(arrived || !pathFlightController.isActive()) {
			// In explore mode arrival just picks next leg instead of stopping (handled in exploreTick)
			if(exploreMode && arrived) { arrived = false; exploreTick(); return; }
			if(exploreMode && !pathFlightController.isActive() && !arrived) { exploreTick(); return; }
			return;
		}

		if(autoLogOnDamage.get())
		{
			autoLogTick();
		}

		if(dodgeGhastFireballs.get() || dodgeArrows.get() || dodgeLavaAndFire.get())
		{
			dangerAvoidTick();
		}

		if(autoLogOnUnreachable.get())
		{
			if(pathFlightController.isDestinationUnreachable())
			{
				unreachableTicks++;
				if(pathDebug.get() && unreachableTicks % 20 == 0)
				{
					info("[PathDebug] Destination unreachable, logging out in " + (60 - unreachableTicks) + " ticks...");
				}
				if(unreachableTicks >= 60)
				{
					net.chuck.chuckpack.modules.render.CoordinateLogout.hardDisconnect("AutoFly Auto-Log: Destination unreachable");
					return;
				}
			}
			else
			{
				unreachableTicks = 0;
			}
		}

		if(autoDisconnectOnArrival.get())
		{
			double discRadius = disconnectRadius.get();
			if(discRadius > 0 && mc.player != null && lastTarget != null)
			{
				double dist = mc.player.position().distanceTo(Vec3.atCenterOf(lastTarget));
				if(dist <= discRadius)
				{
					info("Near target! Disconnecting...");
					pathFlightController.stop();
					arrived = true;
					stopAutoEnabledModules();
					mc.player.setDeltaMovement(Vec3.ZERO);
					net.chuck.chuckpack.modules.render.CoordinateLogout.hardDisconnect("AutoFly: Within disconnect radius");
					return;
				}
			}
		}

		adaptiveSpeedTick();

		// Meteor Flight packet anti-kick 1:1 (onPreTick yaw spin + onPostTick timing)
		{
			float currentYaw = mc.player.getYRot();
			if(mc.player.fallDistance >= 3f && currentYaw == antiKickLastYaw && mc.player.getDeltaMovement().length() < 0.003d)
			{
				mc.player.setYRot(currentYaw + (antiKickFlip ? 1 : -1));
				antiKickFlip = !antiKickFlip;
			}
			antiKickLastYaw = currentYaw;
		}

		if(antiKick.get() && mc.player != null)
		{
			if(antiKickDelayLeft > 0) antiKickDelayLeft--;

			if(antiKickOffLeft <= 0 && antiKickDelayLeft <= 0)
			{
				antiKickDelayLeft = antiKickDelay.get();
				antiKickOffLeft = antiKickOffTime.get();
				try {
					((meteordevelopment.meteorclient.mixin.LocalPlayerAccessor) mc.player).meteor$setPositionReminder(20);
				} catch(Throwable ignored) {}
			}
			else if(antiKickDelayLeft <= 0)
			{
				if(antiKickOffLeft == antiKickOffTime.get())
				{
					try {
						((meteordevelopment.meteorclient.mixin.LocalPlayerAccessor) mc.player).meteor$setPositionReminder(20);
					} catch(Throwable ignored) {}
				}
				antiKickOffLeft--;
			}
			if(pathFlightController.isActive() && mc.player.getYRot() != antiKickLastYaw) mc.player.setYRot(antiKickLastYaw);
		}

		syncConfig();
	}

	private void dangerAvoidTick()
	{
		if(mc.player == null || mc.level == null) return;

		long now = System.currentTimeMillis();
		if(now - lastDangerAvoidMs < 1000) return;

		Vec3 safe = findSafePosition();
		if(safe != null)
		{
			mc.player.setPos(safe);
			mc.player.setDeltaMovement(Vec3.ZERO);
			lastDangerAvoidMs = now;
			info("Teleported to safety!");
		}
	}

	private Vec3 findSafePosition()
	{
		if(mc.player == null || mc.level == null) return null;

		Vec3 playerPos = mc.player.position();
		BlockPos playerBlock = mc.player.blockPosition();

		if(dodgeGhastFireballs.get())
		{
			AABB scanBox = new AABB(playerPos.x - ghastScanRange.get(), playerPos.y - ghastScanRange.get(), playerPos.z - ghastScanRange.get(),
				playerPos.x + ghastScanRange.get(), playerPos.y + ghastScanRange.get(), playerPos.z + ghastScanRange.get());

			List<Entity> fireballs = new ArrayList<>();
			for(Entity e : mc.level.players())
			{
				if(e instanceof AbstractHurtingProjectile && scanBox.intersects(e.getBoundingBox()))
				{
					fireballs.add(e);
				}
			}
			for(Entity entity : fireballs)
			{
				if(entity.distanceToSqr(mc.player) > ghastScanRange.get() * ghastScanRange.get()) continue;

				if(entity instanceof AbstractHurtingProjectile fb && fb.getDeltaMovement().lengthSqr() > 0.01)
				{
					Vec3 toPlayer = playerPos.subtract(fb.position()).normalize();
					double dot = fb.getDeltaMovement().normalize().dot(toPlayer);

					if(dot > 0.1)
					{
						return sphereTeleport(playerPos, fb.position(), ghastTeleportDistance.get());
					}
				}
			}
		}

		if(dodgeArrows.get())
		{
			AABB scanBox = new AABB(playerPos.x - arrowScanRange.get(), playerPos.y - arrowScanRange.get(), playerPos.z - arrowScanRange.get(),
				playerPos.x + arrowScanRange.get(), playerPos.y + arrowScanRange.get(), playerPos.z + arrowScanRange.get());

			List<Entity> allProjectiles = new ArrayList<>();
			for(Entity e : mc.level.players())
			{
				if(e instanceof AbstractArrow && scanBox.intersects(e.getBoundingBox()))
				{
					allProjectiles.add(e);
				}
			}

			for(Entity entity : allProjectiles)
			{
				if(entity.distanceToSqr(mc.player) > arrowScanRange.get() * arrowScanRange.get()) continue;

				if(entity instanceof AbstractArrow proj && proj.getDeltaMovement().lengthSqr() > 0.01)
				{
					Vec3 toPlayer = playerPos.subtract(entity.position()).normalize();
					double dot = proj.getDeltaMovement().normalize().dot(toPlayer);

					if(dot > 0.1)
					{
						return sphereTeleport(playerPos, entity.position(), arrowTeleportDistance.get());
					}
				}
			}
		}

		if(dodgeLavaAndFire.get())
		{
			int scanRange = (int) Math.ceil(lavaScanRange.get());
			BlockPos nearestDanger = null;
			double nearestDangerDistSq = Double.MAX_VALUE;

			for(int x = -scanRange; x <= scanRange; x++)
			{
				for(int y = -scanRange; y <= scanRange; y++)
				{
					for(int z = -scanRange; z <= scanRange; z++)
					{
						BlockPos check = playerBlock.offset(x, y, z);
						var state = mc.level.getBlockState(check);
						if(state.getBlock() == net.minecraft.world.level.block.Blocks.LAVA
							|| state.getBlock() == net.minecraft.world.level.block.Blocks.FIRE
							|| state.getBlock() == net.minecraft.world.level.block.Blocks.SOUL_FIRE)
						{
							double distSq = playerPos.distanceToSqr(Vec3.atCenterOf(check));
							if(distSq < nearestDangerDistSq)
							{
								nearestDangerDistSq = distSq;
								nearestDanger = check;
							}
						}
					}
				}
			}

			if(nearestDanger != null && nearestDangerDistSq <= lavaScanRange.get() * lavaScanRange.get())
			{
				Vec3 dangerCenter = Vec3.atCenterOf(nearestDanger);
				double playerDistFromDanger = Math.sqrt(nearestDangerDistSq);
				Vec3 awayDir = playerPos.subtract(dangerCenter).normalize();

				if(awayDir.lengthSqr() < 0.01)
				{
					awayDir = new Vec3(0, 1, 0);
				}

				double radius = lavaTeleportDistance.get();
				double radiusSq = radius * radius;
				BlockPos bestCandidate = null;
				double bestDistFromDanger = playerDistFromDanger;

				for(int x = (int) Math.floor(-radius); x <= (int) Math.ceil(radius); x++)
				{
					for(int y = (int) Math.floor(-radius); y <= (int) Math.ceil(radius); y++)
					{
						for(int z = (int) Math.floor(-radius); z <= (int) Math.ceil(radius); z++)
						{
							double distSq = x * x + y * y + z * z;
							if(distSq > radiusSq || distSq < 1) continue;

							BlockPos candidate = playerBlock.offset(x, y, z);

							if(!isAirBlock(candidate)) continue;
							if(!isAirBlock(candidate.above())) continue;
							if(!isAirBlock(candidate.above(2))) continue;

							if(isDangerousAt(candidate)) continue;

							Vec3 candidatePos = Vec3.atCenterOf(candidate);
							double candidateDistFromDanger = candidatePos.distanceTo(dangerCenter);

							if(candidateDistFromDanger > bestDistFromDanger + 0.5)
							{
								bestDistFromDanger = candidateDistFromDanger;
								bestCandidate = candidate;
							}
						}
					}
				}

				if(bestCandidate != null)
				{
					return Vec3.atCenterOf(bestCandidate);
				}
			}
		}

		return null;
	}

	private boolean isDangerousAt(BlockPos pos)
	{
		if(mc.level == null) return true;

		for(int dx = -1; dx <= 1; dx++)
		{
			for(int dy = -3; dy <= 1; dy++)
			{
				for(int dz = -1; dz <= 1; dz++)
				{
					var state = mc.level.getBlockState(pos.offset(dx, dy, dz));
					if(state.getBlock() == net.minecraft.world.level.block.Blocks.LAVA
						|| state.getBlock() == net.minecraft.world.level.block.Blocks.FIRE
						|| state.getBlock() == net.minecraft.world.level.block.Blocks.SOUL_FIRE)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isAirBlock(BlockPos pos)
	{
		if(mc.level == null) return false;
		var state = mc.level.getBlockState(pos);
		return state.isAir();
	}

	private Vec3 teleportPosition(Vec3 from, Vec3 direction, double distance)
	{
		double targetX = from.x + direction.x * distance;
		double targetY = from.y + direction.y * distance;
		double targetZ = from.z + direction.z * distance;

		targetX = Mth.clamp(targetX, -29999999, 29999999);
		targetZ = Mth.clamp(targetZ, -29999999, 29999999);

		return new Vec3(targetX, targetY, targetZ);
	}

	private Vec3 sphereTeleport(Vec3 playerPos, Vec3 dangerPos, double radius)
	{
		BlockPos playerBlock = mc.player.blockPosition();
		double radiusSq = radius * radius;
		double playerDistFromDanger = playerPos.distanceTo(dangerPos);
		BlockPos bestCandidate = null;
		double bestDistFromDanger = playerDistFromDanger;

		for(int x = (int) Math.floor(-radius); x <= (int) Math.ceil(radius); x++)
		{
			for(int y = (int) Math.floor(-radius); y <= (int) Math.ceil(radius); y++)
			{
				for(int z = (int) Math.floor(-radius); z <= (int) Math.ceil(radius); z++)
				{
					double distSq = x * x + y * y + z * z;
					if(distSq > radiusSq || distSq < 1) continue;

					BlockPos candidate = playerBlock.offset(x, y, z);

					if(!isAirBlock(candidate)) continue;
					if(!isAirBlock(candidate.above())) continue;
					if(!isAirBlock(candidate.above(2))) continue;

					if(isDangerousAt(candidate)) continue;

					Vec3 candidatePos = Vec3.atCenterOf(candidate);
					double candidateDistFromDanger = candidatePos.distanceTo(dangerPos);

					if(candidateDistFromDanger > bestDistFromDanger + 0.5)
					{
						bestDistFromDanger = candidateDistFromDanger;
						bestCandidate = candidate;
					}
				}
			}
		}

		if(bestCandidate != null)
		{
			return Vec3.atCenterOf(bestCandidate);
		}

		Vec3 awayDir = playerPos.subtract(dangerPos).normalize();
		if(awayDir.lengthSqr() < 0.01) awayDir = new Vec3(0, 1, 0);
		return teleportPosition(playerPos, awayDir, radius);
	}

	private void autoLogTick()
	{
		if(mc.player == null) return;

		float currentHealth = mc.player.getHealth();

		if((int) currentHealth <= autoLogThreshold.get())
		{
			info("Auto-log: Health too low (" + (int) currentHealth + " HP)! Disconnecting...");
			net.chuck.chuckpack.modules.render.CoordinateLogout.hardDisconnect("AutoFly Auto-Log: Low health (" + (int) currentHealth + " HP)");
		}
	}

	private void adaptiveSpeedTick()
	{
		if(mc.player == null || !pathFlightController.isActive()) return;

		long now = System.currentTimeMillis();

		if(setbackPhase == 0)
		{
			if(now - lastRampUpMs >= 250)
			{
				currentSpeed = Math.min(rampUpLimit.get(), currentSpeed + rampUpIncrement.get());
				lastRampUpMs = now;
				pathFlightConfig.flightHorizontalSpeed = currentSpeed * 10.0;
				pathFlightConfig.flightVerticalSpeed = currentSpeed * 10.0 * 0.667;
				if(speedDebug.get())
				{
					info("[SpeedDebug] Ramping up: " + String.format(Locale.ROOT, "%.3f", currentSpeed));
				}
			}
		}
		else if(setbackPhase == 1)
		{
			if(now - quietStartMs >= QUIET_WINDOW_MS)
			{
				if(speedDebug.get())
				{
					info("[SpeedDebug] 5s quiet - holding speed at " + String.format(Locale.ROOT, "%.3f", currentSpeed));
				}
			}
		}
	}

	@EventHandler
	private void onSendPacket(PacketEvent.Send event)
	{
		if(!isActive() || mc.player == null) return;
		if(!antiKick.get()) return;
		if(!(event.packet instanceof ServerboundMovePlayerPacket packet)) return;

		// 1:1 Flight onSendPacket
		double currentY = packet.getY(Double.MAX_VALUE);
		if(currentY != Double.MAX_VALUE)
		{
			antiKickPacket(packet, currentY);
		}
		else
		{
			ServerboundMovePlayerPacket fullPacket;
			if(packet.hasRotation())
			{
				fullPacket = new ServerboundMovePlayerPacket.PosRot(
					mc.player.getX(), mc.player.getY(), mc.player.getZ(),
					packet.getYRot(0), packet.getXRot(0),
					packet.isOnGround(), mc.player.horizontalCollision
				);
			}
			else
			{
				fullPacket = new ServerboundMovePlayerPacket.Pos(
					mc.player.getX(), mc.player.getY(), mc.player.getZ(),
					packet.isOnGround(), mc.player.horizontalCollision
				);
			}
			event.cancel();
			antiKickPacket(fullPacket, mc.player.getY());
			mc.getConnection().send(fullPacket);
		}
	}

	private void antiKickPacket(ServerboundMovePlayerPacket packet, double currentY)
	{
		if(antiKickDelayLeft <= 0 && antiKickLastPacketY != Double.MAX_VALUE
			&& shouldFlyDown(currentY, antiKickLastPacketY) && meteordevelopment.meteorclient.utils.entity.EntityUtils.isOnAir(mc.player))
		{
			((meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor) packet).meteor$setY(antiKickLastPacketY - 0.0313);
		}
		else
		{
			antiKickLastPacketY = currentY;
		}
	}

	private boolean shouldFlyDown(double currentY, double lastY)
	{
		if(currentY >= lastY) return true;
		return lastY - currentY < 0.0313;
	}

	@EventHandler
	private void onReceivePacket(PacketEvent.Receive event)
	{
		if(event.packet instanceof ClientboundRespawnPacket)
		{
			suppressSetbackUntilMs = System.currentTimeMillis() + 3000;
		}
	}

	public boolean isSpeedIncreasing()
	{
		return isActive() && setbackPhase == 0 && currentSpeed < rampUpLimit.get();
	}

	public void onGlobalSetback(long now)
	{
		if(!isActive() || mc.player == null) return;
		if(now < suppressSetbackUntilMs) return;

		lastSetbackMs = now;
		setbackDisplayTicks = 40;

		if(setbackPhase == 0)
		{
			setbackCount++;

			if(setbackCount < INITIAL_SETBACK_THRESHOLD)
			{
				if(speedDebug.get())
				{
					info("[SpeedDebug] Setback " + setbackCount + "/" + INITIAL_SETBACK_THRESHOLD);
				}
				return;
			}

			currentSpeed = Math.max(startSpeed.get(), currentSpeed - rampUpIncrement.get());
			syncConfig();
			setbackPhase = 1;
			quietStartMs = now;
			if(speedDebug.get())
			{
				info("[SpeedDebug] 10 setbacks! Speed: " + String.format(Locale.ROOT, "%.3f", currentSpeed) + " → holding");
			}
			return;
		}

		if(setbackPhase == 1)
		{
			currentSpeed = Math.max(startSpeed.get(), currentSpeed - rampUpIncrement.get());
			quietStartMs = now;
			syncConfig();
			if(speedDebug.get())
			{
				info("[SpeedDebug] Setback during hold, speed: " + String.format(Locale.ROOT, "%.3f", currentSpeed));
			}
		}
	}

	public void startExplore(int radius, int height)
	{
		if(mc.player == null || mc.level == null) {
			error("Join a Level before exploring.");
			return;
		}
		if(!isActive()) toggle();
		exploreMode = true;
		exploreRadius = Math.max(256, radius);
		exploreHeight = height;
		exploreAngle = 0;
		exploreLeg = 0;
		exploreStuckTicks = 0;
		exploreStuckCooldown = 0;
		exploreLastPos = mc.player.position();
		pickExploreTarget();
		info("Explore started: radius " + exploreRadius + " height " + exploreHeight + ". Reroutes when stuck.");
	}

	public void stopExplore()
	{
		exploreMode = false;
		exploreTarget = null;
		if(isActive()) {
			pathFlightController.stop();
			toggle();
		}
		info("Explore stopped.");
	}

	public boolean isExploring() { return exploreMode && isActive(); }

	private void pickExploreTarget()
	{
		if(mc.player == null) return;
		BlockPos origin = mc.player.blockPosition();
		// Spiral out: increase distance every 2 legs, rotate 90deg each leg for grid coverage (base finding loads chunks)
		int step = 256;
		int dist = step * (exploreLeg / 2 + 1);
		if(dist > exploreRadius) {
			// Reset spiral when radius covered
			exploreLeg = 0;
			dist = step;
		}
		double rad = Math.toRadians(exploreAngle);
		int tx = origin.getX() + (int)(Math.cos(rad) * dist);
		int tz = origin.getZ() + (int)(Math.sin(rad) * dist);
		int ty = exploreHeight;
		exploreAngle += 90;
		if(exploreAngle >= 360) exploreAngle -= 360;
		exploreLeg++;
		exploreTarget = new BlockPos(tx, ty, tz);
		exploreStuckTicks = 0;
		setTargetFromMapInternal(tx, ty, tz);
		if(pathDebug.get()) info("[Explore] New leg to " + tx + " " + ty + " " + tz);
	}

	private void exploreTick()
	{
		if(!exploreMode || mc.player == null) return;
		if(exploreStuckCooldown > 0) { exploreStuckCooldown--; return; }
		Vec3 cur = mc.player.position();
		if(exploreLastPos != null) {
			double moved = cur.distanceTo(exploreLastPos);
			if(moved < 0.5) {
				exploreStuckTicks++;
			} else {
				exploreStuckTicks = 0;
			}
			// Stuck for ~5s (100 ticks) or destination unreachable -> pick different way
			boolean unreachable = false;
			try { unreachable = pathFlightController.isDestinationUnreachable(); } catch(Throwable ignored) {}
			if(exploreStuckTicks >= 100 || unreachable) {
				if(pathDebug.get() || true) info("[Explore] Stuck, rerouting different way.");
				// Turn 135deg different way instead of 90 to avoid same obstacle
				exploreAngle += 135;
				if(exploreAngle >= 360) exploreAngle -= 360;
				exploreLeg++;
				pickExploreTarget();
				exploreStuckCooldown = 40;
				unreachableTicks = 0;
				return;
			}
		}
		exploreLastPos = cur;
		// If arrived at leg target (path inactive but explore on), pick next leg
		if(!pathFlightController.isActive() && exploreMode) {
			pickExploreTarget();
		}
	}

	public void setTargetFromMap(int x, int y, int z)
	{
		exploreMode = false;
		setTargetFromMap(x, y, z, false);
	}

	public void setTargetFromMap(int x, int y, int z, boolean yKnown, boolean skipWaypoint)
	{
		exploreMode = false;
		mapClickYKnown = yKnown;
		setTargetFromMapInternal(x, y, z);
		if(!skipWaypoint) createAutoFlyWaypoint();
		else { removeAutoFlyWaypoint(); mapClickTarget = null; }
	}

	private void setTargetFromMap(int x, int y, int z, boolean yKnown)
	{
		mapClickYKnown = yKnown;
		setTargetFromMapInternal(x, y, z);
		createAutoFlyWaypoint();
	}

	public BlockPos getDestination()
	{
		if(pathFlightController != null && pathFlightController.isActive())
		{
			return pathFlightController.currentDestination();
		}
		return lastTarget;
	}

	public BlockPos getFinalTarget()
	{
		return lastTarget;
	}

	private void setTargetFromMapInternal(int x, int y, int z)
	{
		if(mc.player == null || mc.level == null) return;

		int minY = mc.level.getMinY();
		y = Math.max(minY, Math.min(319, y));

		removeAutoFlyWaypoint();
		restoreExistingWaypointSymbol();

		arrived = false;
		currentSpeed = startSpeed.get();
		lastRampUpMs = 0;
		suppressSetbackUntilMs = System.currentTimeMillis() + 3000;
		speedFrozen = false;
		ticksSinceSetback = -1;
		speedLocked = false;
		setbackCount = 0;
		quietStartMs = 0;
		setbackPhase = 0;
		setbackDisplayTicks = 0;

		if(autoEat.get())
		{
			var eat = Modules.get().get("auto-eat");
			if(eat != null && !eat.isActive())
			{
				eat.toggle();
				autoEnabledEat = true;
			}
		}

		if(autoAntiSocial.get())
		{
			var social = Modules.get().get(AntiSocial.class);
			if(social != null && !social.isActive())
			{
				social.toggle();
				autoEnabledAntiSocial = true;
			}
		}

		syncConfig();

		mc.player.setPos(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());

		mapClickTarget = new BlockPos(x, y, z);
		if(mapClickYKnown)
		{
			pathFlightController.flyTo(x, y, z);
		}
		else
		{
			pathFlightController.flyTo(x, z);
		}
		lastTarget = new BlockPos(x, y, z);
	}

	public void setTargetFromExistingWaypoint(int x, int y, int z, String originalSymbol)
	{
		setTargetFromExistingWaypoint(x, y, z, originalSymbol, null);
	}

	public void setTargetFromExistingWaypoint(int x, int y, int z, String originalSymbol, Object minimapWaypoint)
	{
		removeAutoFlyWaypoint();
		restoreExistingWaypointSymbol();

		existingWaypointOriginal = null;
		originalWaypointSymbol = null;

		arrived = false;
		currentSpeed = startSpeed.get();
		lastRampUpMs = 0;
		suppressSetbackUntilMs = System.currentTimeMillis() + 3000;
		speedFrozen = false;
		ticksSinceSetback = -1;
		speedLocked = false;
		setbackCount = 0;
		quietStartMs = 0;
		setbackPhase = 0;
		setbackDisplayTicks = 0;

		if(autoEat.get())
		{
			var eat = Modules.get().get("auto-eat");
			if(eat != null && !eat.isActive())
			{
				eat.toggle();
				autoEnabledEat = true;
			}
		}

		if(autoAntiSocial.get())
		{
			var social = Modules.get().get(AntiSocial.class);
			if(social != null && !social.isActive())
			{
				social.toggle();
				autoEnabledAntiSocial = true;
			}
		}

		syncConfig();

		mapClickTarget = new BlockPos(x, y, z);
		mapClickYKnown = true;
		pathFlightController.flyTo(x, y, z);
		lastTarget = new BlockPos(x, y, z);

		try
		{
			Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
				.getField("MINIMAP").get(null);
			Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
			if(session == null) return;

			Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
			Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
			if(currentWorld == null) return;

			Object currentSet = currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
			if(currentSet == null) return;

		Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
		if(minimapWaypoint != null && wpClass.isInstance(minimapWaypoint))
		{
			existingWaypointOriginal = minimapWaypoint;
			originalWaypointSymbol = originalSymbol;
			wpClass.getMethod("setSymbol", String.class).invoke(minimapWaypoint, "AD");
			try {
				Object worldManagerIO = session.getClass().getMethod("getWorldManagerIO").invoke(session);
				for(java.lang.reflect.Method m : worldManagerIO.getClass().getMethods()) {
					if(m.getName().equals("saveWorld")) {
						m.invoke(worldManagerIO, currentWorld);
						break;
					}
				}
			} catch(Throwable ignored) {}
			try {
				Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
				if(supportMods != null) {
					java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
					f.setAccessible(true);
					f.setBoolean(supportMods, true);
				}
			} catch(Throwable ignored) {}
			return;
		}
		Iterable<?> existingWaypoints = (Iterable<?>) currentSet.getClass().getMethod("getWaypoints").invoke(currentSet);
		for(Object wp : existingWaypoints)
		{
			int exX = (int) wpClass.getMethod("getX").invoke(wp);
			int exZ = (int) wpClass.getMethod("getZ").invoke(wp);
			int exY = (int) wpClass.getMethod("getY").invoke(wp);
			if(exX == x && exZ == z && exY == y)
			{
				existingWaypointOriginal = wp;
				originalWaypointSymbol = originalSymbol;
				wpClass.getMethod("setSymbol", String.class).invoke(wp, "AD");
					try {
						Object worldManagerIO = session.getClass().getMethod("getWorldManagerIO").invoke(session);
						for(java.lang.reflect.Method m : worldManagerIO.getClass().getMethods()) {
							if(m.getName().equals("saveWorld")) {
								m.invoke(worldManagerIO, currentWorld);
								break;
							}
						}
					} catch(Throwable ignored) {}
					try {
						Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
						if(supportMods != null) {
							java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
							f.setAccessible(true);
							f.setBoolean(supportMods, true);
						}
					} catch(Throwable ignored) {}
					break;
				}
			}
		}catch(Throwable ignored) {}
	}

	private void restoreExistingWaypointSymbol()
	{
		if(existingWaypointOriginal == null || originalWaypointSymbol == null) return;
		try
		{
			Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
			wpClass.getMethod("setSymbol", String.class).invoke(existingWaypointOriginal, originalWaypointSymbol);

			Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
				.getField("MINIMAP").get(null);
			Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
			if(session != null)
			{
				Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
				Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
				if(currentWorld != null)
				{
					try {
						Object worldManagerIO = session.getClass().getMethod("getWorldManagerIO").invoke(session);
						for(java.lang.reflect.Method m : worldManagerIO.getClass().getMethods()) {
							if(m.getName().equals("saveWorld")) {
								m.invoke(worldManagerIO, currentWorld);
								break;
							}
						}
					} catch(Throwable ignored) {}
					try {
						Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
						if(supportMods != null) {
							java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
							f.setAccessible(true);
							f.setBoolean(supportMods, true);
						}
					} catch(Throwable ignored) {}
				}
			}
		}catch(Throwable ignored) {}
		existingWaypointOriginal = null;
		originalWaypointSymbol = null;
	}

	private void syncConfig()
	{
		pathFlightConfig.flightProcess = isActive();
		pathFlightConfig.assumeFlightHack = true;
		pathFlightConfig.flightHorizontalSpeed = currentSpeed * 10.0;
		pathFlightConfig.flightVerticalSpeed = currentSpeed * 10.0 * 0.667;
		pathFlightConfig.flightArrivalRadius = arrivalRadius.get();
		pathFlightConfig.flightAntiHunger = false;
		pathFlightConfig.flightFaceTravel = false;
		pathFlightConfig.flightRenderPath = false;
		pathFlightConfig.flightDebug = pathDebug.get();
		pathFlightConfig.flightVerbose = verboseDebug.get();
		pathFlightConfig.flightCruiseHeight = 120;

		if(mc.level != null && mc.level.dimension() != null)
		{
			String dim = mc.level.dimension().identifier().toString();
			if(dim.equals("minecraft:the_nether"))
			{
				pathFlightConfig.waitChunks = waitChunksNether.get();
			}
			else if(dim.equals("minecraft:the_end"))
			{
				pathFlightConfig.waitChunks = waitChunksEnd.get();
			}
			else
			{
				pathFlightConfig.waitChunks = waitChunksOverworld.get();
			}
		}
		pathFlightConfig.flightPredictTerrain = !pathFlightConfig.waitChunks;
		net.chuck.chuckpack.util.config.Seeds.Seed storedSeed = net.chuck.chuckpack.util.config.Seeds.get().getSeed();
		pathFlightConfig.flightSeed = storedSeed != null ? storedSeed.seed : 0L;
	}

	private void syncLegacyPathFinder()
	{
		if(legacyPathFinder != null)
		{
			legacyPathFinder.setFallingAllowed(fallingAllowed.get());
			legacyPathFinder.setDivingAllowed(divingAllowed.get());
			legacyPathFinder.setThinkSpeed(thinkSpeed.get());
			legacyPathFinder.setThinkTime(thinkTime.get());
		}
	}

	private void suspendFlight()
	{
		var flight = Modules.get().get(Flight.class);
		if(flight != null && flight.isActive()) flight.toggle();
	}

	private void restoreFlight()
	{
		var flight = Modules.get().get(Flight.class);
		if(flight != null && flightWasEnabled && !flight.isActive()) flight.toggle();
		flightWasEnabled = false;
	}

	private void createAutoFlyWaypoint()
	{
		if(mapClickTarget == null || mc.player == null) return;
		try
		{
			int wx = mapClickTarget.getX();
			int wz = mapClickTarget.getZ();
			int wy = mapClickTarget.getY();
			boolean yKnown = mapClickYKnown;

			Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
				.getField("MINIMAP").get(null);
			Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
			if(session == null) { mapClickTarget = null; return; }

			Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
			Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
			if(currentWorld == null) { mapClickTarget = null; return; }

			Object currentSet = currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
			if(currentSet == null) { mapClickTarget = null; return; }

			Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
			Class<?> colorClass = Class.forName("xaero.hud.minimap.waypoint.WaypointColor");
			Class<?> purposeClass = Class.forName("xaero.hud.minimap.waypoint.WaypointPurpose");
			Object color = colorClass.getField("AQUA").get(null);
			Object purpose = purposeClass.getField("NORMAL").get(null);

			Iterable<?> existingWaypoints = (Iterable<?>) currentSet.getClass().getMethod("getWaypoints").invoke(currentSet);
			for(Object wp : existingWaypoints)
			{
				int exX = (int) wpClass.getMethod("getX").invoke(wp);
				int exZ = (int) wpClass.getMethod("getZ").invoke(wp);
				if(exX == wx && exZ == wz)
				{
					mapClickTarget = null;
					return;
				}
			}

			Object waypoint = wpClass.getConstructor(int.class, int.class, int.class, String.class, String.class, colorClass, purposeClass, boolean.class, boolean.class)
				.newInstance(wx, wy, wz, "Auto Fly Destination", "Ad", color, purpose, false, yKnown);

			wpClass.getMethod("setTemporary", boolean.class).invoke(waypoint, true);

			currentSet.getClass().getMethod("add", wpClass).invoke(currentSet, waypoint);

			try {
				Object worldManagerIO = session.getClass().getMethod("getWorldManagerIO").invoke(session);
				for(java.lang.reflect.Method m : worldManagerIO.getClass().getMethods()) {
					if(m.getName().equals("saveWorld")) {
						m.invoke(worldManagerIO, currentWorld);
						break;
					}
				}
			} catch(Throwable ignored) {}

			try {
				Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
				if(supportMods != null) {
					java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
					f.setAccessible(true);
					f.setBoolean(supportMods, true);
				}
			} catch(Throwable ignored) {}

			autoFlyWaypoint = waypoint;
			mapClickTarget = null;
		}catch(Throwable ignored)
		{
			mapClickTarget = null;
		}
	}

	private void removeAutoFlyWaypoint()
	{
		if(autoFlyWaypoint == null) return;
		try
		{
			Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
				.getField("MINIMAP").get(null);
			Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
			if(session != null)
			{
				Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
				Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
				if(currentWorld != null)
				{
					Object currentSet = currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
					if(currentSet != null)
					{
						Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
						currentSet.getClass().getMethod("remove", wpClass).invoke(currentSet, autoFlyWaypoint);
					}
				}
			}
		}catch(Throwable ignored) {}
		autoFlyWaypoint = null;
	}

	private void cleanStaleAutoFlyWaypoints()
	{
		try
		{
			Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
				.getField("MINIMAP").get(null);
			Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
			if(session == null) return;

			Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
			Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
			if(currentWorld == null) return;

			Object currentSet = currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
			if(currentSet == null) return;

			Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
			Iterable<?> existingWaypoints = (Iterable<?>) currentSet.getClass().getMethod("getWaypoints").invoke(currentSet);

			List<Object> toRemove = new ArrayList<>();
			for(Object wp : existingWaypoints)
			{
				String name = (String) wpClass.getMethod("getName").invoke(wp);
				if("Auto Fly Destination".equals(name))
				{
					toRemove.add(wp);
				}
			}

			for(Object wp : toRemove)
			{
				currentSet.getClass().getMethod("remove", wpClass).invoke(currentSet, wp);
			}

			if(!toRemove.isEmpty())
			{
				try {
					Object worldManagerIO = session.getClass().getMethod("getWorldManagerIO").invoke(session);
					for(java.lang.reflect.Method m : worldManagerIO.getClass().getMethods()) {
						if(m.getName().equals("saveWorld")) {
							m.invoke(worldManagerIO, currentWorld);
							break;
						}
					}
				} catch(Throwable ignored) {}

				try {
					Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
					if(supportMods != null) {
						java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
						f.setAccessible(true);
						f.setBoolean(supportMods, true);
					}
				} catch(Throwable ignored) {}
			}
		}catch(Throwable ignored) {}
	}

	@EventHandler
	private void onRender3D(Render3DEvent event)
	{
		if(!showPath.get() || mc.player == null) return;
		if(!pathFlightController.isActive()) return;

		List<BetterBlockPos> path = pathFlightController.getVisiblePath();
		if(path.isEmpty()) return;

		Color color = pathColor.get();

		Vec3 prev = mc.player.position();
		for(BetterBlockPos pos : path)
		{
			Vec3 center = Vec3.atCenterOf(pos);
			event.renderer.line(prev.x, prev.y, prev.z, center.x, center.y, center.z, color);
			prev = center;
		}
	}

}
