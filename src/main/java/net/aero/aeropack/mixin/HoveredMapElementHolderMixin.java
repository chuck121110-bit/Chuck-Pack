package net.aero.aeropack.mixin;

import java.util.ArrayList;
import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.HoveredMapElementHolder;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

@Mixin(value = HoveredMapElementHolder.class, remap = false)
public abstract class HoveredMapElementHolderMixin {

    @Shadow protected Object element;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void aeropack$addWaypointOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            if (!(element instanceof Waypoint)) return;

            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            Waypoint waypoint = (Waypoint) element;
            int wx = waypoint.getX();
            int wz = waypoint.getZ();
            int wy = waypoint.getY();
            String originalSymbol = waypoint.getSymbol();
            IRightClickableElement self = (IRightClickableElement) (Object) this;

            // Convert waypoint Y to current world equivalent for AutoFly
            int minY = -64;
            int maxY = 319;
            String dim = "";
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.world != null && mc.world.getRegistryKey() != null) {
                dim = mc.world.getRegistryKey().getValue().toString();
            }

            if (dim.equals("minecraft:the_nether") && (wy < minY || wy > 127)) {
                int overworldY = wy * 8 + 8;
                if (overworldY >= minY && overworldY <= maxY) {
                    wy = overworldY;
                }
            }

            final int finalWy = wy;

            int idx = Math.max(0, options.size() - 1);

            options.add(idx, new RightClickOption("Auto Fly Here", idx, self) {
                @Override
                public void onAction(Screen screen) {
                    AutoFly af = Modules.get().get(AutoFly.class);
                    if (af != null) {
                        if (!af.isActive()) af.toggle();
                        af.setTargetFromExistingWaypoint(wx, finalWy, wz, originalSymbol);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
