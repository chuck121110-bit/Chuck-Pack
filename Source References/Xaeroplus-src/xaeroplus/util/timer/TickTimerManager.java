package xaeroplus.util.timer;

import net.lenni0451.lambdaevents.EventHandler;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;

public final class TickTimerManager {
   public static final int TICK_PRIORITY = 2147483646;
   public static final TickTimerManager INSTANCE = new TickTimerManager();
   private volatile long tickTime = 0L;

   private TickTimerManager() {
      XaeroPlus.EVENT_BUS.register(this);
   }

   public long getTickTime() {
      return this.tickTime;
   }

   @EventHandler(
      priority = 2147483646
   )
   public void onClientTick(ClientTickEvent.Pre event) {
      ++this.tickTime;
   }
}
