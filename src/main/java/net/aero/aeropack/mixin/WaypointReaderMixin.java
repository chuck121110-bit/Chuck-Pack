package net.aero.aeropack.mixin;

import java.util.ArrayList;
import net.aero.aeropack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = WaypointReader.class, remap = false)
public abstract class WaypointReaderMixin {

    @Inject(method = "getRightClickOptions(Lxaero/map/mods/gui/Waypoint;Lxaero/map/gui/IRightClickableElement;)Ljava/util/ArrayList;", at = @At("RETURN"))
    private void aeropack$addAutoFlyHere(Waypoint waypoint, IRightClickableElement target, CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            int wx = waypoint.getX();
            int wy = waypoint.getY();
            int wz = waypoint.getZ();
            boolean yKnown = wy != -64;

            for (int i = options.size() - 1; i >= 0; i--) {
                if (options.get(i).getDisplayName().getString().equals("Auto Fly Here")) {
                    options.remove(i);
                }
            }

            int insertIdx = options.size();
            options.add(insertIdx, new RightClickOption("Auto Fly Here", insertIdx, target) {
                @Override
                public void onAction(Screen screen) {
                    AutoFly af = Modules.get().get(AutoFly.class);
                    if (af != null) {
                        String originalSymbol = "";
                        try {
                            xaero.common.minimap.waypoints.Waypoint realWp =
                                (xaero.common.minimap.waypoints.Waypoint) waypoint.getOriginal();
                            Object sym = realWp.getClass().getMethod("getSymbol").invoke(realWp);
                            if (sym != null) originalSymbol = sym.toString();
                        } catch (Throwable ignored) {
                        }
                        if (!af.isActive()) af.toggle();
                        af.setTargetFromExistingWaypoint(wx, wy, wz, originalSymbol);
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
