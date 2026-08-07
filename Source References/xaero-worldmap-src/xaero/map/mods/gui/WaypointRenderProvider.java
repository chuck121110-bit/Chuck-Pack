package xaero.map.mods.gui;

import java.util.Iterator;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.map.WorldMap;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.mods.SupportXaeroMinimap;

public class WaypointRenderProvider extends ElementRenderProvider<Waypoint, WaypointRenderContext> {
   private final SupportXaeroMinimap minimap;
   private Iterator<Waypoint> iterator;
   private int maxZSoFar;

   public WaypointRenderProvider(SupportXaeroMinimap minimap) {
      this.minimap = minimap;
   }

   public void begin(ElementRenderLocation location, WaypointRenderContext context) {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      if ((Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINTS) && this.minimap.getWaypoints() != null) {
         this.iterator = this.minimap.getWaypoints().iterator();
         context.worldmapWaypointsScale = (float)(Double)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINT_SCALE);
         context.waypointBackgrounds = (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINT_BACKGROUNDS);
         SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
         context.showDisabledWaypoints = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.DISPLAY_DISABLED_WAYPOINTS);
         this.maxZSoFar = Integer.MIN_VALUE;
      } else {
         this.iterator = null;
      }
   }

   public boolean hasNext(ElementRenderLocation location, WaypointRenderContext context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public Waypoint getNext(ElementRenderLocation location, WaypointRenderContext context) {
      Waypoint waypoint = (Waypoint)this.iterator.next();
      int z = waypoint.getZ();
      if (z < this.maxZSoFar) {
         this.minimap.requestWaypointsRefresh();
      } else {
         this.maxZSoFar = z;
      }

      return waypoint;
   }

   public void end(ElementRenderLocation location, WaypointRenderContext context) {
   }
}
