package xaero.hud.packet.basic;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.class_2540;
import net.minecraft.class_3222;
import xaero.common.XaeroMinimapSession;
import xaero.common.server.player.ServerPlayerData;

public class HandshakePacket {
   public static final int NETWORK_COMPATIBILITY = 3;
   private final int networkVersion;

   public HandshakePacket(int networkVersion) {
      this.networkVersion = networkVersion;
   }

   public HandshakePacket() {
      this(3);
   }

   public void write(class_2540 u) {
      u.method_53002(this.networkVersion);
   }

   public static HandshakePacket read(class_2540 buffer) {
      return new HandshakePacket(buffer.readInt());
   }

   public static class ClientHandler implements Consumer<HandshakePacket> {
      public void accept(HandshakePacket message) {
         XaeroMinimapSession session = XaeroMinimapSession.getCurrentSession();
         if (session != null) {
            session.getMinimapProcessor().setServerModNetworkVersion(message.networkVersion);
            session.getModMain().getMessageHandler().sendToServer(new HandshakePacket());
         }
      }
   }

   public static class ServerHandler implements BiConsumer<HandshakePacket, class_3222> {
      public void accept(HandshakePacket message, class_3222 player) {
         ServerPlayerData playerData = ServerPlayerData.get(player);
         playerData.setClientModNetworkVersion(message.networkVersion);
      }
   }
}
