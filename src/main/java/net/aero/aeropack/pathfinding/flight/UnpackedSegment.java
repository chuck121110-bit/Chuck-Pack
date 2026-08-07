package net.aero.aeropack.pathfinding.flight;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class UnpackedSegment
{
	private final Stream<BetterBlockPos> path;
	private final boolean finished;
	
	public UnpackedSegment(Stream<BetterBlockPos> path, boolean finished)
	{
		this.path = path;
		this.finished = finished;
	}
	
	public UnpackedSegment append(Stream<BetterBlockPos> other,
		boolean otherFinished)
	{
		return new UnpackedSegment(Stream.concat(this.path, other),
			otherFinished);
	}
	
	public UnpackedSegment prepend(Stream<BetterBlockPos> other)
	{
		return new UnpackedSegment(Stream.concat(other, this.path),
			this.finished);
	}
	
	public List<BetterBlockPos> collect()
	{
		List<BetterBlockPos> path = this.path.collect(Collectors.toList());
		HashMap<BetterBlockPos, Integer> positionFirstSeen =
			new HashMap<BetterBlockPos, Integer>();
		for(int i = 0; i < path.size(); ++i)
		{
			BetterBlockPos pos = path.get(i);
			if(positionFirstSeen.containsKey((Object)pos))
			{
				int j = (Integer)positionFirstSeen.get((Object)pos);
				while(i > j)
				{
					path.remove(i);
					--i;
				}
				continue;
			}
			positionFirstSeen.put(pos, i);
		}
		return path;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}
}
