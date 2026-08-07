package xaero.hud.minimap.waypoint;

import net.minecraft.class_310;
import xaero.common.HudMod;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.render.WaypointDeleter;

public class WaypointSession {
   private final MinimapSession session;
   private final class_310 mc;
   protected final WaypointSharingHandler sharing;
   private final DeathpointHandler deathpointHandler;
   private final TemporaryWaypointHandler temporaryHandler;
   private final WaypointTeleport teleport;
   private final WaypointCollector collector;
   private final WaypointDeleter deleter;
   private final DestinationHandler destinationHandler;
   private long setChangedTime;

   public WaypointSession(HudMod modMain, MinimapSession session) {
      this.session = session;
      this.mc = class_310.method_1551();
      this.sharing = WaypointSharingHandler.Builder.begin().setModMain(modMain).setSession(session).build();
      this.deathpointHandler = new DeathpointHandler(modMain, session);
      this.temporaryHandler = new TemporaryWaypointHandler(modMain);
      this.teleport = new WaypointTeleport(modMain, this, session);
      this.collector = new WaypointCollector(session);
      this.deleter = new WaypointDeleter(modMain);
      this.destinationHandler = new DestinationHandler(session, this.deleter);
   }

   public WaypointSharingHandler getSharing() {
      return this.sharing;
   }

   public DeathpointHandler getDeathpointHandler() {
      return this.deathpointHandler;
   }

   public TemporaryWaypointHandler getTemporaryHandler() {
      return this.temporaryHandler;
   }

   public WaypointTeleport getTeleport() {
      return this.teleport;
   }

   public WaypointCollector getCollector() {
      return this.collector;
   }

   public WaypointDeleter getDeleter() {
      return this.deleter;
   }

   public DestinationHandler getDestinationHandler() {
      return this.destinationHandler;
   }

   public class_310 getMc() {
      return this.mc;
   }

   public long getSetChangedTime() {
      return this.setChangedTime;
   }

   public void setSetChangedTime(long setChangedTime) {
      this.setChangedTime = setChangedTime;
   }

   public MinimapSession getSession() {
      return this.session;
   }
}
