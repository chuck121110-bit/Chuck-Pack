package net.aero.aeropack.pathfinding;

import java.util.ArrayList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class PathProcessor
{
	protected static final Minecraft MC = Minecraft.getInstance();
	
	private static final KeyMapping[] CONTROLS =
		{MC.options.keyUp, MC.options.keyDown, MC.options.keyRight,
			MC.options.keyLeft, MC.options.keyJump, MC.options.keyShift};
	
	protected final ArrayList<PathPos> path;
	protected int index;
	protected boolean done;
	protected int ticksOffPath;
	
	public PathProcessor(ArrayList<PathPos> path)
	{
		if(path.isEmpty())
			throw new IllegalStateException("There is no path!");
		
		this.path = path;
	}
	
	public abstract void process();
	
	public abstract boolean canBreakBlocks();
	
	public final int getIndex()
	{
		return index;
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final int getTicksOffPath()
	{
		return ticksOffPath;
	}
	
	protected final void facePosition(BlockPos pos)
	{
		Vec3 lookTarget = Vec3.atCenterOf(pos);
		Vec3 eyePos = MC.player.getEyePosition();
		double dx = lookTarget.x - eyePos.x;
		double dy = lookTarget.y - eyePos.y;
		double dz = lookTarget.z - eyePos.z;
		double horiz = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
		MC.player.setYRot(yaw);
		MC.player.setXRot(pitch);
	}
	
	public static final void lockControls()
	{
		for(KeyMapping key : CONTROLS)
			key.setDown(false);
		
		MC.player.setSprinting(false);
	}
	
	public static final void releaseControls()
	{
		for(KeyMapping key : CONTROLS)
			key.setDown(false);
	}
}
