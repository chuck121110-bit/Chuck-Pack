package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class Verus extends NoFallMode {
    public Verus(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (event.packet instanceof ServerboundMovePlayerPacket packet) {
            var accessor = (PlayerMoveC2SPacketAccessor) packet;
            if (mc.player.fallDistance > 3.35) {
                accessor.chuckpack$setOnGround(true);
                mc.player.fallDistance = 0;
                Vec3 velocity = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(velocity.x, 0, velocity.z);
            }
        }
    }
}
