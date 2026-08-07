package net.aero.aeropack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Port to Xaero's API for MC 26.1.2 — add "Auto Fly Here" right-click option to Xaero's map.
// Xaero classes (xaero.map.gui.GuiMap, RightClickOption, IRightClickableElement) are not available at compile time.
// Restore from git history when Xaero jars are updated for 26.1.2.
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class GuiMapMixin {

    @Shadow private int rightClickX;
    @Shadow private int rightClickY;
    @Shadow private int rightClickZ;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void aeropack$addAutoFlyHere(CallbackInfoReturnable<?> cir) {
        // TODO: Port to Xaero's API for MC 26.1.2
    }
}
