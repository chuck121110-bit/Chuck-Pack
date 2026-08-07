package xaeroplus.feature.waypoint.eta;

import kaptainwutax.mathutils.util.Mth;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_310;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class WaypointEtaManager {
   public static final WaypointEtaManager INSTANCE = new WaypointEtaManager();
   private final Timer measurementTimer = Timers.tickTimer();
   private int measurementInterval = 20;
   private double lastX = (double)0.0F;
   private double lastZ = (double)0.0F;
   private double speedBlocksPerSecond = (double)0.0F;

   private WaypointEtaManager() {
      XaeroPlus.EVENT_BUS.register(this);
   }

   @EventHandler
   public void onClickTick(ClientTickEvent.Post event) {
      if (this.measurementTimer.tick((long)this.measurementInterval)) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null && mc.method_1560() != null) {
            double currentX = mc.method_1560().method_23317();
            double currentZ = mc.method_1560().method_23321();
            double dist = Math.hypot(currentX - this.lastX, currentZ - this.lastZ);
            this.lastX = currentX;
            this.lastZ = currentZ;
            double measurementMs = (double)(this.measurementInterval + 1) * (double)50.0F;
            double measurementSeconds = measurementMs / (double)1000.0F;
            if (!(measurementSeconds <= (double)0.0F)) {
               this.speedBlocksPerSecond = dist / measurementSeconds;
            }
         }
      }
   }

   public long getEtaSecondsToReachWaypoint(Waypoint waypoint) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.method_1560() != null) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession == null) {
            return 0L;
         } else {
            double dimDiv = minimapSession.getDimensionHelper().getDimensionDivision(minimapSession.getWorldManager().getCurrentWorld());
            int wpX = waypoint.getX(dimDiv);
            int wpZ = waypoint.getZ(dimDiv);
            double wpDist = Math.hypot((double)wpX - mc.method_1560().method_23317(), (double)wpZ - mc.method_1560().method_23321());
            double etaSeconds = wpDist / this.speedBlocksPerSecond;
            return etaSeconds != Double.POSITIVE_INFINITY && etaSeconds != Double.NEGATIVE_INFINITY && !Double.isNaN(etaSeconds) ? (long)etaSeconds : 0L;
         }
      } else {
         return 0L;
      }
   }

   public String getEtaTextSuffix(Waypoint waypoint) {
      long eta = this.getEtaSecondsToReachWaypoint(waypoint);
      if (eta <= 0L) {
         return "";
      } else {
         String etaText = " - ";
         if (eta > 86400L) {
            int days = (int)(eta / 86400L);
            int hours = (int)(eta % 86400L / 3600L);
            etaText = etaText + days + "d";
            if (hours > 0) {
               etaText = etaText + " " + hours + "h";
            }
         } else if (eta > 3600L) {
            int hours = (int)(eta / 3600L);
            int minutes = (int)(eta % 3600L / 60L);
            etaText = etaText + hours + "h";
            if (minutes > 0) {
               etaText = etaText + " " + minutes + "m";
            }
         } else if (eta > 60L) {
            int minutes = (int)(eta / 60L);
            int seconds = (int)(eta % 60L);
            etaText = etaText + minutes + "m";
            if (seconds > 0) {
               etaText = etaText + " " + seconds + "s";
            }
         } else {
            etaText = etaText + eta + "s";
         }

         return etaText;
      }
   }

   public void updateMeasurementInterval(final int interval) {
      this.measurementInterval = Mth.max(0, interval);
      this.measurementTimer.reset();
      this.speedBlocksPerSecond = (double)0.0F;
   }
}
