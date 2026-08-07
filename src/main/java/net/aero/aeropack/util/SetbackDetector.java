package net.aero.aeropack.util;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.aero.aeropack.modules.movement.AutoFly;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;

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
		if(event.packet instanceof GameJoinS2CPacket
			|| event.packet instanceof PlayerRespawnS2CPacket
			|| event.packet instanceof LoginSuccessS2CPacket)
		{
			suppressUntilMs = System.currentTimeMillis() + JOIN_GRACE_MS;
			return;
		}

		if(!(event.packet instanceof PlayerPositionLookS2CPacket)) return;

		long now = System.currentTimeMillis();
		if(now < suppressUntilMs) return;
		if(now - lastSetbackMs < COOLDOWN_MS) return;

		lastSetbackMs = now;

		AutoFly af = Modules.get().get(AutoFly.class);
		if(af != null && af.isActive())
		{
			af.onGlobalSetback(now);
		}

		ChatUtils.sendMsg(Text.literal("[WARNING] Setback detected.").styled(style -> style.withColor(net.minecraft.text.TextColor.fromRgb(0x615AFF))));
	}
}
