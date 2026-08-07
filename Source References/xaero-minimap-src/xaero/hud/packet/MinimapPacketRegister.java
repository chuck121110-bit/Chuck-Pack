package xaero.hud.packet;

import java.util.function.BiConsumer;
import xaero.common.server.level.LevelMapProperties;
import xaero.hud.packet.basic.ClientboundRulesPacket;
import xaero.hud.packet.basic.HandshakePacket;
import xaero.hud.packet.tracker.ClientboundPlayerTrackerResetPacket;
import xaero.hud.packet.tracker.ClientboundTrackedPlayerPacket;
import xaero.lib.common.packet.IPacketHandler;

public class MinimapPacketRegister {
   public void register(IPacketHandler messageHandler) {
      messageHandler.register(0, LevelMapProperties.class, LevelMapProperties::write, LevelMapProperties::read, (BiConsumer)null, new LevelMapPropertiesConsumer());
      messageHandler.register(1, HandshakePacket.class, HandshakePacket::write, HandshakePacket::read, new HandshakePacket.ServerHandler(), new HandshakePacket.ClientHandler());
      messageHandler.register(2, ClientboundTrackedPlayerPacket.class, ClientboundTrackedPlayerPacket::write, ClientboundTrackedPlayerPacket::read, (BiConsumer)null, new ClientboundTrackedPlayerPacket.Handler());
      messageHandler.register(3, ClientboundPlayerTrackerResetPacket.class, ClientboundPlayerTrackerResetPacket::write, ClientboundPlayerTrackerResetPacket::read, (BiConsumer)null, new ClientboundPlayerTrackerResetPacket.Handler());
      messageHandler.register(4, ClientboundRulesPacket.class, ClientboundRulesPacket::write, ClientboundRulesPacket::read, (BiConsumer)null, new ClientboundRulesPacket.ClientHandler());
   }
}
