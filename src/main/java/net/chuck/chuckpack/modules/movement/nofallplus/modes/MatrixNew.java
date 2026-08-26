package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;

public class MatrixNew extends NoFallMode {
    private Timer timer;

    public MatrixNew(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onDeactivate() {
        if (timer != null) timer.setOverride(1);
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null || mc.level == null) return;
        if (!(event.packet instanceof ServerboundMovePlayerPacket packet)) return;

        var accessor = (PlayerMoveC2SPacketAccessor) packet;
        timer = Modules.get().get(Timer.class);

        if (!mc.player.onGround() && mc.player.fallDistance > 2.69) {
            timer.setOverride(0.3);
            accessor.chuckpack$setOnGround(true);
            mc.player.fallDistance = 0;
        }

        if (mc.player.fallDistance > 3.5) {
            timer.setOverride(0.3);
        } else {
            timer.setOverride(1);
        }

        AABB box = mc.player.getBoundingBox().move(0, mc.player.getDeltaMovement().y, 0);
        boolean collides = false;
        for (var shape : mc.level.getCollisions(mc.player, box)) {
            if (!shape.isEmpty()) {
                collides = true;
                break;
            }
        }

        if (collides && !packet.isOnGround() && mc.player.getDeltaMovement().y < -0.6) {
            accessor.chuckpack$setOnGround(true);
        }
    }
}
