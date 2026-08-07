package net.aero.aeropack.pathfinding;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import net.minecraft.client.Minecraft;

public record PlayerAbilities(boolean invulnerable, boolean creativeFlying,
	boolean flying, boolean immuneToFallDamage, boolean noWaterSlowdown,
	boolean jesus, boolean spider)
{
	
	private static final Minecraft MC = Minecraft.getInstance();
	
	public static PlayerAbilities get()
	{
		net.minecraft.world.entity.player.Abilities mcAbilities =
			MC.player.getAbilities();
		
		boolean invulnerable =
			mcAbilities.invulnerable || mcAbilities.instabuild;
		boolean creativeFlying = mcAbilities.flying;
		boolean flying = creativeFlying || Modules.get().isActive(Flight.class);
		boolean immuneToFallDamage = invulnerable || Modules.get().isActive(NoFall.class);
		boolean noWaterSlowdown = false;
		boolean jesus = false;
		boolean spider = false;
		
		return new PlayerAbilities(invulnerable, creativeFlying, flying,
			immuneToFallDamage, noWaterSlowdown, jesus, spider);
	}
}
