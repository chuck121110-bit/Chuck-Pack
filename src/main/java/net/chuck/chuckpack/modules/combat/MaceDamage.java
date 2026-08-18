package net.chuck.chuckpack.modules.combat;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.HitResult;

public class MaceDamage extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> fallHeight = sgGeneral.add(new IntSetting.Builder()
        .name("Fall Height")
        .description("Spoofs vertical movement packets before mace hits for higher smash damage.")
        .defaultValue(22)
        .min(1)
        .sliderRange(1, 500)
        .build()
    );

    public MaceDamage() {
        super(
            Categories.Combat,
            "Mace Damage",
            "Spoofs vertical movement packets before mace hits for higher smash damage."
        );
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null || mc.getConnection() == null) return;
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) return;
        if (mc.player.getMainHandItem().getItem() != Items.MACE) return;

        for (int i = 0; i < 4; i++) sendFakeY(0.0);
        sendFakeY(Math.sqrt(fallHeight.get() * fallHeight.get()));
        sendFakeY(0.0);
    }

    private void sendFakeY(double offset) {
        mc.getConnection().getConnection().send(new ServerboundMovePlayerPacket.Pos(
            mc.player.getX(),
            mc.player.getY() + offset,
            mc.player.getZ(),
            false,
            mc.player.horizontalCollision
        ));
    }
}
