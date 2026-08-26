package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Vulcan extends NoFallMode {
    private boolean vulCanNoFall = false;
    private boolean vulCantNoFall = false;
    private boolean nextSpoof = false;
    private boolean doSpoof = false;

    public Vulcan(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onActivate() {
        vulCanNoFall = false;
        vulCantNoFall = false;
        nextSpoof = false;
        doSpoof = false;
    }

    @Override
    public void onTickEventPre(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (!vulCanNoFall && mc.player.fallDistance > 3.25) {
            vulCanNoFall = true;
        }

        if (vulCanNoFall && mc.player.onGround() && vulCantNoFall) {
            vulCantNoFall = false;
        }

        if (vulCantNoFall) return;

        if (nextSpoof) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, -0.1, 0));
            mc.player.fallDistance = -0.1f;
            strafe(0.3f);
            nextSpoof = false;
        }

        if (mc.player.fallDistance > 3.5625) {
            mc.player.fallDistance = 0;
            doSpoof = true;
            nextSpoof = true;
        }
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (event.packet instanceof ServerboundMovePlayerPacket packet) {
            var accessor = (PlayerMoveC2SPacketAccessor) packet;
            accessor.chuckpack$setOnGround(true);
            doSpoof = false;
            double roundedY = Math.round(mc.player.position().y * 2.0) / 2.0;
            accessor.chuckpack$setY(roundedY);
            mc.player.setPos(mc.player.getX(), roundedY, mc.player.getZ());
        }
    }

    private void strafe(float speed) {
        if (mc.player == null) return;
        float yaw = mc.player.getYRot();
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));
        mc.player.setDeltaMovement(sin * speed, mc.player.getDeltaMovement().y, cos * speed);
    }
}
