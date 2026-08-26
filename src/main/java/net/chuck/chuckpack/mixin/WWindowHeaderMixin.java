package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "meteordevelopment.meteorclient.gui.widgets.containers.WWindow$WHeader", remap = false)
public abstract class WWindowHeaderMixin {

    @Shadow
    private WTriangle triangle;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void chuckpack$preventTriangleNPE(meteordevelopment.meteorclient.gui.renderer.GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (triangle == null) cir.setReturnValue(false);
    }
}
