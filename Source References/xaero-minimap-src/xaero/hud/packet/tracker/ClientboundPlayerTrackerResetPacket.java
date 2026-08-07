package xaero.hud.packet.tracker;

import java.util.function.Consumer;
import net.minecraft.class_2540;
import xaero.common.XaeroMinimapSession;

public class ClientboundPlayerTrackerResetPacket {
   public void write(class_2540 buffer) {
   }

   public static ClientboundPlayerTrackerResetPacket read(class_2540 buffer) {
      return new ClientboundPlayerTrackerResetPacket();
   }

   public static class Handler implements Consumer<ClientboundPlayerTrackerResetPacket> {
      public void accept(ClientboundPlayerTrackerResetPacket t) {
         XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
         if (minimapSession != null) {
            minimapSession.getMinimapProcessor().getSyncedTrackedPlayerManager().reset();
         }
      }
   }
}
