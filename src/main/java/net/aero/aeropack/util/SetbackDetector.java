package net.aero.aeropack.util;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.aero.aeropack.modules.movement.AutoFly;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;

public class SetbackDetector
{
	private static final SetbackDetector INSTANCE = new SetbackDetector();
	private static final long COOLDOWN_MS = 500L;
	private static final long JOIN_GRACE_MS = 3000L;

	private long lastSetbackMs;
	private long suppressUntilMs;

	private SetbackDetector() {}

	public static SetbackDetector get()
	{
		return INSTANCE;
	}

	public long getLastSetbackMs()
	{
		return lastSetbackMs;
	}

	@EventHandler
	public void onPacketReceive(PacketEvent.Receive event)
	{
		if(event.packet instanceof ClientboundLoginPacket
			|| event.packet instanceof ClientboundRespawnPacket
			|| event.packet instanceof ClientboundLoginFinishedPacket)
		{
			suppressUntilMs = System.currentTimeMillis() + JOIN_GRACE_MS;
			return;
		}

		if(!(event.packet instanceof ClientboundPlayerPositionPacket)) return;

		long now = System.currentTimeMillis();
		if(now < suppressUntilMs) return;
		if(now - lastSetbackMs < COOLDOWN_MS) return;

		lastSetbackMs = now;

		AutoFly af = Modules.get().get(AutoFly.class);
		if(af != null && af.isActive())
		{
			af.onGlobalSetback(now);
		}

		ChatUtils.sendMsg(Component.literal("[WARNING] Setback detected.").withStyle(style -> style.withColor(TextColor.fromRgb(0x615AFF))));
	}
}
