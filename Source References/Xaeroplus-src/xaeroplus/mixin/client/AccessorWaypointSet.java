package xaeroplus.mixin.client;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;

@Mixin(
   value = {WaypointSet.class},
   remap = false
)
public interface AccessorWaypointSet {
   @Accessor("list")
   List<Waypoint> getList();
}
