package xaero.hud.packet.basic;

import java.util.function.Consumer;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;

public class ClientboundRulesPacket {
   public final boolean allowCaveModeOnServer;
   public final boolean allowNetherCaveModeOnServer;
   public final boolean allowRadarOnServer;

   public ClientboundRulesPacket(boolean allowCaveModeOnServer, boolean allowNetherCaveModeOnServer, boolean allowRadarOnServer) {
      this.allowCaveModeOnServer = allowCaveModeOnServer;
      this.allowNetherCaveModeOnServer = allowNetherCaveModeOnServer;
      this.allowRadarOnServer = allowRadarOnServer;
   }

   public void write(class_2540 u) {
      class_2487 nbt = new class_2487();
      nbt.method_10556("cm", this.allowCaveModeOnServer);
      nbt.method_10556("ncm", this.allowNetherCaveModeOnServer);
      nbt.method_10556("r", this.allowRadarOnServer);
      u.method_10794(nbt);
   }

   public static ClientboundRulesPacket read(class_2540 buffer) {
      class_2487 nbt = buffer.method_10798();
      return new ClientboundRulesPacket((Boolean)nbt.method_10577("cm").orElse(false), (Boolean)nbt.method_10577("ncm").orElse(false), (Boolean)nbt.method_10577("r").orElse(false));
   }

   public static class ClientHandler implements Consumer<ClientboundRulesPacket> {
      public void accept(ClientboundRulesPacket message) {
         MinimapClientWorldDataHelper.getCurrentWorldData().setSyncedRules(message);
      }
   }
}
