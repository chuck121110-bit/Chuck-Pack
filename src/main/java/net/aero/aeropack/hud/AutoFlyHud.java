package net.aero.aeropack.hud;

import java.util.Locale;
import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoFlyHud extends HudElement
{
	public static final HudElementInfo<AutoFlyHud> INFO = new HudElementInfo<>(
		KeybindsHud.AERO_GROUP, "autofly-eta",
		"Shows distance, speed, and ETA while AutoFly is active.",
		AutoFlyHud::new
	);

	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
		.name("shadow")
		.description("Renders shadow behind text.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> autoCenter = sgGeneral.add(new BoolSetting.Builder()
		.name("auto-center")
		.description("Automatically positions the HUD 30 pixels below your crosshair, centered.")
		.defaultValue(false)
		.build()
	);

	private final Setting<Boolean> showSpeed = sgGeneral.add(new BoolSetting.Builder()
		.name("show-speed")
		.description("Show your current travel speed in blocks/sec.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
		.name("show-distance")
		.description("Show distance to destination in blocks.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Boolean> showETA = sgGeneral.add(new BoolSetting.Builder()
		.name("show-eta")
		.description("Show ETA based on your current speed and distance.")
		.defaultValue(true)
		.build()
	);

	private final Setting<Integer> updateInterval = sgGeneral.add(new IntSetting.Builder()
		.name("update-interval")
		.description("How often the display updates in ticks. 1 = every tick, 20 = once per second.")
		.defaultValue(1)
		.min(1)
		.max(10)
		.sliderRange(1, 10)
		.build()
	);

	private final Setting<Integer> maxWidth = sgGeneral.add(new IntSetting.Builder()
		.name("max-text-width")
		.description("Maximum horizontal width of the text in pixels. 0 = unlimited.")
		.defaultValue(0)
		.min(0)
		.max(800)
		.sliderRange(0, 800)
		.build()
	);

	private final SettingGroup sgColors = settings.createGroup("Colors");

	private final Setting<SettingColor> activeColor = sgColors.add(new ColorSetting.Builder()
		.name("active-color")
		.description("Color when flying.")
		.defaultValue(new SettingColor(97, 90, 255))
		.build()
	);

	private final Setting<SettingColor> idleColor = sgColors.add(new ColorSetting.Builder()
		.name("idle-color")
		.description("Color when not moving or arrived.")
		.defaultValue(new SettingColor(255, 85, 85))
		.build()
	);

	private int tickCounter;
	private String cachedText = "";
	private boolean cachedMoving;
	private double smoothedSpeed = 0.0;
	private int overrideColor = 0;
	private int speedHoldTicks = 0;

	public AutoFlyHud()
	{
		super(INFO);
	}

	@Override
	public void tick(HudRenderer renderer)
	{
		MinecraftClient mc = MinecraftClient.getInstance();

		AutoFly af = Modules.get().get(AutoFly.class);
		boolean active = af != null && af.isActive() && af.getDestination() != null && mc.player != null;

		if(!active)
		{
			if(isInEditor())
			{
				cachedText = "AutoFly: not active";
				double h = renderer.textHeight(shadow.get(), getScale());
				double w = renderer.textWidth(cachedText, shadow.get(), getScale());
				setSize(w, h);
			}
			else
			{
				setSize(0, 0);
				tickCounter = 0;
				cachedText = "";
				smoothedSpeed = 0.0;
				speedHoldTicks = 0;
			}
			return;
		}

		tickCounter++;
		if(tickCounter < updateInterval.get()) return;
		tickCounter = 0;

		if(af.setbackDisplayTicks > 0)
		{
			cachedText = "SETBACK DETECTED";
			overrideColor = 1;
			cachedMoving = false;
			if(isInEditor())
			{
				double h = renderer.textHeight(shadow.get(), getScale());
				double w = renderer.textWidth(cachedText, shadow.get(), getScale());
				setSize(w, h);
			}
			return;
		}

		BlockPos destCheck = af.getFinalTarget();
		if(destCheck == null) destCheck = af.getDestination();
		if(destCheck != null)
		{
			double distCheck = mc.player.getEntityPos().distanceTo(Vec3d.ofCenter(destCheck));
			if(distCheck < 5.0)
			{
				cachedText = "Arrived";
				overrideColor = 0;
				cachedMoving = false;
				if(isInEditor())
				{
					double h = renderer.textHeight(shadow.get(), getScale());
					double w = renderer.textWidth(cachedText, shadow.get(), getScale());
					setSize(w, h);
				}
				return;
			}
		}

		if(af.isSpeedIncreasing())
		{
			cachedText = "SPEED TUNING";
			overrideColor = 2;
			cachedMoving = false;
			if(isInEditor())
			{
				double h = renderer.textHeight(shadow.get(), getScale());
				double w = renderer.textWidth(cachedText, shadow.get(), getScale());
				setSize(w, h);
			}
			return;
		}

		overrideColor = 0;

		double dx = (mc.player.getX() - mc.player.lastX) * 20.0;
		double dy = (mc.player.getY() - mc.player.lastY) * 20.0;
		double dz = (mc.player.getZ() - mc.player.lastZ) * 20.0;
		double rawSpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if(rawSpeed > 1.0)
		{
			smoothedSpeed = smoothedSpeed * 0.3 + rawSpeed * 0.7;
			speedHoldTicks = 4;
		}
		else if(speedHoldTicks > 0)
		{
			speedHoldTicks--;
		}
		else
		{
			smoothedSpeed = smoothedSpeed * 0.7 + rawSpeed * 0.3;
		}

		double speed = smoothedSpeed;

		BlockPos dest = af.getFinalTarget();
		if(dest == null) dest = af.getDestination();
		double distance = mc.player.getEntityPos().distanceTo(Vec3d.ofCenter(dest));

		boolean arrived = distance < 5.0;
		cachedMoving = speed > 0.01 && !arrived;

		if(arrived)
		{
			cachedText = "Arrived";
		}
		else
		{
			StringBuilder sb = new StringBuilder();

			if(showDistance.get())
			{
				sb.append(String.format(Locale.ROOT, "DIST: %.0f", distance));
			}

			if(showSpeed.get())
			{
				if(!sb.isEmpty()) sb.append(" | ");
				sb.append(String.format(Locale.ROOT, "%.1f M/S", speed));
			}

			if(showETA.get())
			{
				if(!sb.isEmpty()) sb.append(" | ");
				if(speed > 0.1)
				{
					double etaSeconds = distance / speed;
					int totalMins = (int)(etaSeconds / 60);
					int totalSecs = (int)(etaSeconds % 60);
					int totalHours = totalMins / 60;
					totalMins %= 60;
					int totalDays = totalHours / 24;
					totalHours %= 24;

					sb.append("ETA: ");
					boolean started = false;
					if(totalDays > 0)
					{
						sb.append(totalDays).append("d");
						started = true;
					}
					if(totalHours > 0)
					{
						if(started) sb.append(" ");
						sb.append(totalHours).append("h");
						started = true;
					}
					if(totalMins > 0)
					{
						if(started) sb.append(" ");
						sb.append(totalMins).append("m");
						started = true;
					}
					if(!started)
					{
						sb.append(totalSecs).append("s");
					}
				}
				else
				{
					sb.append("ETA: --");
				}
			}

			cachedText = sb.toString();
		}

		if(isInEditor())
		{
			double h = renderer.textHeight(shadow.get(), getScale());
			double w = renderer.textWidth(cachedText, shadow.get(), getScale());
			if(w > maxWidth.get()) w = maxWidth.get();
			setSize(w, h);
		}
	}

	@Override
	public void render(HudRenderer renderer)
	{
		AutoFly af = Modules.get().get(AutoFly.class);
		if(af == null || !af.isActive())
		{
			if(isInEditor() && !cachedText.isEmpty())
			{
				renderer.text(cachedText, this.x, this.y, idleColor.get(), shadow.get(), getScale());
			}
			return;
		}

		if(cachedText.isEmpty()) return;

		String drawText = cachedText;

		double textW = renderer.textWidth(drawText, shadow.get(), getScale());
		if(maxWidth.get() > 0 && textW > maxWidth.get())
		{
			while(!drawText.isEmpty() && renderer.textWidth(drawText + "...", shadow.get(), getScale()) > maxWidth.get())
			{
				drawText = drawText.substring(0, drawText.length() - 1);
			}
			textW = renderer.textWidth(drawText, shadow.get(), getScale());
			drawText = drawText + "...";
		}

		double rx = this.x;
		double ry = this.y;

		if(autoCenter.get())
		{
			MinecraftClient mc = MinecraftClient.getInstance();
			if(mc.getWindow() != null)
			{
				double sw = mc.getWindow().getWidth();
				double sh = mc.getWindow().getHeight();
				rx = sw / 2.0 - textW / 2.0;
				ry = sh / 2.0 + 30.0;
			}
		}

		renderer.text(drawText, rx, ry, getDrawColor(), shadow.get(), getScale());
	}

	private Color getDrawColor()
	{
		if(overrideColor == 1) return idleColor.get();
		if(overrideColor == 2) return activeColor.get();
		return cachedMoving ? activeColor.get() : idleColor.get();
	}

	private double getScale()
	{
		return Hud.get().getTextScale();
	}
}
