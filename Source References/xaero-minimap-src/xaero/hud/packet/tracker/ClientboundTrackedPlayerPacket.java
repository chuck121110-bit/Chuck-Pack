package xaero.hud.packet.tracker;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.class_2487;
import net.minecraft.class_2505;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import xaero.common.XaeroMinimapSession;
import xaero.hud.nbt.util.XaeroNbtUtils;

public class ClientboundTrackedPlayerPacket {
   private final boolean remove;
   private final UUID id;
   private final double x;
   private final double y;
   private final double z;
   private final class_2960 dimension;
   private final int clientNetworkVersion;

   public ClientboundTrackedPlayerPacket(boolean remove, UUID id, double x, double y, double z, class_2960 dimension, int clientNetworkVersion) {
      this.remove = remove;
      this.id = id;
      this.x = x;
      this.y = y;
      this.z = z;
      this.dimension = dimension;
      this.clientNetworkVersion = clientNetworkVersion;
   }

   public void write(class_2540 buffer) {
      class_2487 nbt = new class_2487();
      nbt.method_10556("r", this.remove);
      if (this.clientNetworkVersion < 3) {
         XaeroNbtUtils.putUUIDAsLongArray(nbt, "i", this.id);
      } else {
         XaeroNbtUtils.putUUID(nbt, "i", this.id);
      }

      if (!this.remove) {
         nbt.method_10549("x", this.x);
         nbt.method_10549("y", this.y);
         nbt.method_10549("z", this.z);
         nbt.method_10582("d", this.dimension.toString());
      }

      buffer.method_10794(nbt);
   }

   public static ClientboundTrackedPlayerPacket read(class_2540 buffer) {
      class_2487 nbt = (class_2487)buffer.method_30616(class_2505.method_53898());
      boolean remove = (Boolean)nbt.method_10577("r").orElse(false);
      UUID id = (UUID)XaeroNbtUtils.getUUID(nbt, "i").orElse((Object)null);
      double x = remove ? (double)0.0F : (Double)nbt.method_10574("x").orElse((double)0.0F);
      double y = remove ? (double)0.0F : (Double)nbt.method_10574("y").orElse((double)0.0F);
      double z = remove ? (double)0.0F : (Double)nbt.method_10574("z").orElse((double)0.0F);
      String dimensionString = remove ? null : (String)nbt.method_10558("d").orElse((Object)null);
      class_2960 dimension = dimensionString == null ? null : class_2960.method_60654(dimensionString);
      return new ClientboundTrackedPlayerPacket(remove, id, x, y, z, dimension, 3);
   }

   public static class Handler implements Consumer<ClientboundTrackedPlayerPacket> {
      public void accept(ClientboundTrackedPlayerPacket t) {
         XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
         if (minimapSession != null) {
            if (t.remove) {
               minimapSession.getMinimapProcessor().getSyncedTrackedPlayerManager().remove(t.id);
            } else {
               minimapSession.getMinimapProcessor().getSyncedTrackedPlayerManager().update(t.id, t.x, t.y, t.z, class_5321.method_29179(class_7924.field_41223, t.dimension));
            }
         }
      }
   }
}
