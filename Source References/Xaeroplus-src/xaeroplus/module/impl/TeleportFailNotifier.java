package xaeroplus.module.impl;

import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1074;
import xaeroplus.event.ClientTeleportEvent;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.XaeroTeleportAttemptEvent;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.NotificationUtil;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class TeleportFailNotifier extends Module {
   boolean awaitingTeleport = false;
   Timer timer = Timers.timer();

   @EventHandler
   public void onTeleportAttempt(XaeroTeleportAttemptEvent event) {
      this.awaitingTeleport = true;
      this.timer.reset();
   }

   @EventHandler
   public void onClientTeleport(ClientTeleportEvent event) {
      this.awaitingTeleport = false;
   }

   @EventHandler
   public void onTick(ClientTickEvent.Post event) {
      if (!this.mc.method_1496()) {
         if (this.awaitingTeleport) {
            if (this.timer.tick((long)Settings.REGISTRY.teleportFailNotifierDelay.getAsInt())) {
               this.awaitingTeleport = false;
               NotificationUtil.errorNotification(class_1074.method_4662("xaeroplus.gui.teleport_fail_notifier.message", new Object[0]));
            }

         }
      }
   }
}
