package net.aero.aeropack.mixin;

import java.util.ArrayList;
import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {

    @Shadow private int rightClickX;
    @Shadow private int rightClickY;
    @Shadow private int rightClickZ;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void aeropack$addAutoFlyHere(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            int wx = rightClickX;
            int wz = rightClickZ;

            final int wy = Math.max(-64, Math.min(319, rightClickY));
            boolean yKnown = wy != 319;

            int flyIdx = -1;
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).getDisplayName().getString().equals("Auto Fly Here")) {
                    flyIdx = i;
                    break;
                }
            }
            if (flyIdx == -1) {
                int addIdx = Math.max(0, options.size() - 1);
                options.add(addIdx, new RightClickOption("Auto Fly Here", addIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                    @Override
                    public void onAction(net.minecraft.client.gui.screen.Screen screen) {
                        AutoFly af = Modules.get().get(AutoFly.class);
                        if (af != null) {
                            if (!af.isActive()) af.toggle();
                            af.setTargetFromMap(wx, wy, wz, yKnown, false);
                        }
                    }
                });
            }
        } catch (Throwable ignored) {
        }
    }
}
