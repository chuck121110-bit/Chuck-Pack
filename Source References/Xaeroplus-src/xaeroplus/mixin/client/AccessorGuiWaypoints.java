package xaeroplus.mixin.client;

import java.util.ArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.common.gui.GuiWaypoints;
import xaero.common.minimap.waypoints.Waypoint;

@Mixin(
   value = {GuiWaypoints.class},
   remap = false
)
public interface AccessorGuiWaypoints {
   @Accessor("waypointsSorted")
   ArrayList<Waypoint> getWaypointsSorted();
}
