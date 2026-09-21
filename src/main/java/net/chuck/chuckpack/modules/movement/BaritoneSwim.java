package net.chuck.chuckpack.modules.movement;

import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/**
 * Holds jump (spacebar) while Baritone is pathing and you are in water,
 * so it swims up instead of getting stuck. Only releases keys it pressed
 * itself, never fights your own input.
 */
public class BaritoneSwim extends Module {
    private boolean jumpedByUs;

    public BaritoneSwim() {
        super(Categories.Movement, "baritone-swim", "Holds space to swim up while Baritone is pathing in water.");
    }

    @Override
    public void onDeactivate() {
        if (jumpedByUs && mc.options != null) {
            mc.options.keyJump.setDown(false);
        }
        jumpedByUs = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.options == null) return;

        boolean shouldJump = false;
        try {
            shouldJump = mc.player.isInWater()
                && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
        } catch (Throwable ignored) {
            shouldJump = false;
        }

        if (shouldJump) {
            mc.options.keyJump.setDown(true);
            jumpedByUs = true;
        } else if (jumpedByUs) {
            mc.options.keyJump.setDown(false);
            jumpedByUs = false;
        }
    }
}
