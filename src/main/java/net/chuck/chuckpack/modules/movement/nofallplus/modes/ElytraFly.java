package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraFly extends NoFallMode {
    public ElytraFly(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onTickEventPre(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.player.fallDistance > 2.0) {
            var result = InvUtils.find(Items.ELYTRA);
            if (result.found()) {
                if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                    InvUtils.move().from(result.slot()).toArmor(2);
                }
            }
        }

        if (mc.player.fallDistance > 2.7) {
            mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
            Vec3 velocity = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(velocity.x, 0, velocity.z);
            mc.player.fallDistance = 0;
            mc.player.setOnGround(true);
        }
    }
}
