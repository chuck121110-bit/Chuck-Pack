package net.chuck.chuckpack.modules.movement.nofallplus.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.chuck.chuckpack.mixin.PlayerMoveC2SPacketAccessor;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallMode;
import net.chuck.chuckpack.modules.movement.nofallplus.NoFallPlus;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

public class Eclip extends NoFallMode {
    private int ticks = 0;
    private int teleports = 0;
    private final int blocks = 50;
    private int slot = 0;

    public Eclip(NoFallPlus settings) {
        super(settings);
    }

    @Override
    public void onActivate() {
        ticks = 0;
        teleports = 0;
    }

    @Override
    public void onTickEventPre(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.player.fallDistance > 2.0) {
            var result = InvUtils.find(Items.ELYTRA);
            if (result.found() && mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                InvUtils.move().from(result.slot()).toArmor(2);
            }
        }
    }

    @Override
    public void onSendPacket(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mc.player.fallDistance <= 3.35) return;

        mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
        ticks++;

        mc.player.setPos(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ());
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(
            mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(),
            true, mc.player.horizontalCollision));
        teleports++;
        ticks++;
        mc.player.fallDistance = 0;
        ticks++;

        InvUtils.move().fromArmor(2).to(slot);
    }
}
