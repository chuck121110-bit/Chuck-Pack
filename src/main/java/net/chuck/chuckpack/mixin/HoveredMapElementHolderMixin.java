package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

@Mixin(targets = "xaero.map.element.HoveredMapElementHolder", remap = false)
public abstract class HoveredMapElementHolderMixin {

    @Shadow @Final protected Object element;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"), remap = false)
    private void chuckpack$addWaypointOptions(CallbackInfoReturnable<java.util.ArrayList<RightClickOption>> cir) {
        try {
            java.util.ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;
            Object el = this.element;
            // Worldmap GUI waypoint (xaero.map.mods.gui.Waypoint)
            try {
                Class<?> wmWp = Class.forName("xaero.map.mods.gui.Waypoint");
                if (wmWp.isInstance(el)) {
                    final int wx = (int) wmWp.getMethod("getX").invoke(el);
                    final int wy = (int) wmWp.getMethod("getY").invoke(el);
                    final int wz = (int) wmWp.getMethod("getZ").invoke(el);
                    boolean yKnownTmp = true;
                    try { yKnownTmp = (boolean) wmWp.getMethod("isYIncluded").invoke(el); } catch (Throwable ignored) { yKnownTmp = wy != -64; }
                    final boolean yKnown = yKnownTmp;
                    // resolve underlying common waypoint for recolor/rename
                    Object real = el;
                    try { Object o = wmWp.getMethod("getOriginal").invoke(el); if (o != null) real = o; } catch (Throwable ignored) {}
                    final Object realFinal = real;
                    String symTmp = "";
                    try { Object s = realFinal.getClass().getMethod("getSymbol").invoke(realFinal); if (s != null) symTmp = s.toString(); } catch (Throwable ignored) {}
                    final String origSym = symTmp;
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
                                af.setTargetFromExistingWaypoint(wx, wy, wz, origSym, realFinal);
                            }
                        }
                    });
                    Swarm swarm = Modules.get().get(Swarm.class);
                    if (swarm != null && swarm.isActive()) {
                        insertIdx = Math.max(0, options.size() - 1);
                        String prefix = meteordevelopment.meteorclient.systems.config.Config.get().prefix.get();
                        options.add(insertIdx, new RightClickOption("Swarm Fly Here", insertIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                            @Override
                            public void onAction(net.minecraft.client.gui.screens.Screen screen) {
                                String cmd = prefix + "swarm fly " + wx + " " + (yKnown ? wy + " " : "") + wz;
                                net.minecraft.client.Minecraft.getInstance().execute(() -> { try { meteordevelopment.meteorclient.commands.Commands.dispatch(cmd.substring(1)); } catch (Exception ignored) {} });
                            }
                        });
                    }
                    return;
                }
            } catch (Throwable ignored) {}
            // Minimap common waypoint via reflection
            try {
                Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
                if (wpClass.isInstance(el)) {
                    final int wx = (int) wpClass.getMethod("getX").invoke(el);
                    final int wy = (int) wpClass.getMethod("getY").invoke(el);
                    final int wz = (int) wpClass.getMethod("getZ").invoke(el);
                    boolean yKnownTmp = true;
                    try { yKnownTmp = (boolean) wpClass.getMethod("isYIncluded").invoke(el); } catch (Throwable ignored) { yKnownTmp = wy != -64; }
                    final boolean yKnown = yKnownTmp;
                    String symTmp = "";
                    try { Object s = wpClass.getMethod("getSymbol").invoke(el); if (s != null) symTmp = s.toString(); } catch (Throwable ignored) {}
                    final String origSym = symTmp;
                    final Object realFinal = el;
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
                                af.setTargetFromExistingWaypoint(wx, wy, wz, origSym, realFinal);
                            }
                        }
                    });
                    Swarm swarm = Modules.get().get(Swarm.class);
                    if (swarm != null && swarm.isActive()) {
                        insertIdx = Math.max(0, options.size() - 1);
                        String prefix = meteordevelopment.meteorclient.systems.config.Config.get().prefix.get();
                        options.add(insertIdx, new RightClickOption("Swarm Fly Here", insertIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                            @Override
                            public void onAction(net.minecraft.client.gui.screens.Screen screen) {
                                String cmd = prefix + "swarm fly " + wx + " " + (yKnown ? wy + " " : "") + wz;
                                net.minecraft.client.Minecraft.getInstance().execute(() -> { try { meteordevelopment.meteorclient.commands.Commands.dispatch(cmd.substring(1)); } catch (Exception ignored) {} });
                            }
                        });
                    }
                    return;
                }
            } catch (Throwable ignored2) { return; }
        } catch (Throwable ignored) {}
    }
}
