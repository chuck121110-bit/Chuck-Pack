package net.chuck.chuckpack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Port to Xaero's API for MC 26.1.2 — add "Auto Fly Here" right-click option to Xaero's waypoint hover.
// Xaero classes (HoveredMapElementHolder, RightClickOption, IRightClickableElement, Waypoint) are not available at compile time.
// Restore from git history when Xaero jars are updated for 26.1.2.
@Mixin(targets = "xaero.map.element.HoveredMapElementHolder", remap = false)
public abstract class HoveredMapElementHolderMixin {

    @Shadow protected Object element;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void chuckpack\$addWaypointOptions(CallbackInfoReturnable<?> cir) {
        // TODO: Port to Xaero's API for MC 26.1.2
    }
}
