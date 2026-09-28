package net.chuck.chuckpack.modules.combat;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.Random;

public class KnockbackDisplacer extends Module {
    private static final Random rnd = new Random();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> angle = sgGeneral.add(new DoubleSetting.Builder()
        .name("Displacement Angle")
        .description("The angle by which the knockback is displaced.")
        .defaultValue(180.0)
        .min(-180.0)
        .max(180.0)
        .sliderRange(-180.0, 180.0)
        .build()
    );

    private final Setting<Double> randomization = sgGeneral.add(new DoubleSetting.Builder()
        .name("Angle Randomization")
        .description("Randomizes the displacement angle by +/- this amount.")
        .defaultValue(0.0)
        .min(0.0)
        .max(180.0)
        .sliderRange(0.0, 180.0)
        .build()
    );

    public KnockbackDisplacer() {
        super(
            Categories.Combat,
            "Knockback Displacer",
            "Displaces the direction of knockback you deal by flicking your yaw on attack. Ported from Aoba."
        );
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isSprinting()) return;

        double displacement = angle.get();
        double rand = randomization.get();
        if (rand != 0) displacement += rnd.nextDouble(-rand, rand);

        float originalYaw = mc.player.getYRot();
        float displacedYaw = originalYaw + (float) displacement;

        // Flick yaw, send the displaced rotation to the server, flick back - all in one tick.
        mc.player.setYRot(displacedYaw);
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ(),
            displacedYaw,
            mc.player.getXRot(),
            mc.player.onGround(),
            mc.player.horizontalCollision
        ));
        mc.player.setYRot(originalYaw);
    }
}
