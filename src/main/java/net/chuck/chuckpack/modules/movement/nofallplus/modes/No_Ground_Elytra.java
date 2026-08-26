package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class No_Ground_Elytra extends NoFallMode {
    public No_Ground_Elytra(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onActivate() {
        if (!InvUtils.find(Items.ELYTRA).found()) {
            ChatUtils.error("Elytra not found, this bypass needs an elytra to work.");
        }
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
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (event.packet instanceof ServerboundPlayerCommandPacket) return;
        if (mc.player.onGround() && event.packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket packet) {
            mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
    }
}
