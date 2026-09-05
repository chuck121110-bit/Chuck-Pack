package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

// TODO: Port to Xaero's API for MC 26.1.2 — add "Auto Fly Here" right-click option to Xaero's waypoint hover.
// Xaero classes (HoveredMapElementHolder, RightClickOption, IRightClickableElement, Waypoint) are not available at compile time.
// Restore from git history when Xaero jars are updated for 26.1.2.
@Mixin(targets = "xaero.map.element.HoveredMapElementHolder", remap = false)
public abstract class HoveredMapElementHolderMixin {

    @Shadow protected Object element;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void chuckpack$addWaypointOptions(CallbackInfoReturnable<java.util.ArrayList<RightClickOption>> cir) {
        try {
            java.util.ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;
            Object el = this.element;
            if (!(el instanceof Waypoint)) return;
            Waypoint wp = (Waypoint) el;
            int wx = wp.getX();
            int wy = wp.getY();
            int wz = wp.getZ();
            boolean yKnown = wp.isYIncluded();
            for (int i = options.size() - 1; i >= 0; i--) {
                String name = options.get(i).getDisplayName().getString();
                if (name.equals("Auto Fly Here") || name.equals("Swarm Fly Here")) options.remove(i);
            }
            int insertIdx = Math.max(0, options.size() - 1);
            options.add(insertIdx, new RightClickOption("Auto Fly Here", insertIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                @Override
                public void onAction(net.minecraft.client.gui.screens.Screen screen) {
                    AutoFly af = Modules.get().get(AutoFly.class);
                    if (af != null) {
                        if (!af.isActive()) af.toggle();
                        af.setTargetFromMap(wx, wy, wz, yKnown, false);
                    }
                }
            });
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm != null && swarm.isActive()) {
                insertIdx = Math.max(0, options.size() - 1);
                options.add(insertIdx, new RightClickOption("Swarm Fly Here", insertIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                    @Override
                    public void onAction(net.minecraft.client.gui.screens.Screen screen) {
                        String cmd = "swarm fly " + wx + " " + (yKnown ? wy + " " : "") + wz;
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.player != null) meteordevelopment.meteorclient.utils.player.ChatUtils.sendPlayerMsg(cmd);
                    }
                });
            }
        } catch (Throwable ignored) {}
    }
}
