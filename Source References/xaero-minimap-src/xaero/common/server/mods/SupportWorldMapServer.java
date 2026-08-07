package xaero.common.server.mods;

import xaero.map.WorldMap;

public class SupportWorldMapServer {
   private final int compatibilityVersion;

   public SupportWorldMapServer() {
      this.compatibilityVersion = WorldMap.MINIMAP_COMPATIBILITY_VERSION;
   }

   public boolean supportsTrackedPlayers() {
      return this.compatibilityVersion >= 22;
   }
}
