package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

/**
 * Swarm Fixes
 *
 * Toggles Chuck Pack's fixes/additions to the Swarm module's command set:
 *  - ".swarm mine" accepting multiple block types (upstream bug #1763)
 *  - ".swarm drop" — drops everything that isn't a tool, weapon, armor, or food
 *
 * When disabled, ".swarm mine" falls back to vanilla single-block behavior
 * (only the first listed block is used) and ".swarm drop" is unavailable.
 *
 * This module has no tick/render logic of its own — its only purpose is to
 * act as an on/off flag that SwarmMineMixin checks before applying its
 * replacement command logic.
 *
 * Author: Aero (Chuck Pack)
 */
public class SwarmFixes extends Module {

    public SwarmFixes() {
        super(
            Categories.Misc,
            "Swarm Fixes",
            "Enables Chuck Pack's multi-block .swarm mine fix and the .swarm drop command."
        );
    }

    /** Static accessor so the mixin can check this without going through Modules.get() every call. */
    public static boolean isEnabled() {
        return meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(SwarmFixes.class);
    }
}
