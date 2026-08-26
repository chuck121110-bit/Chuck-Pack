package net.chuck.chuckpack.mixin;

import java.util.ArrayList;
import net.chuck.chuckpack.modules.movement.AutoFly;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.Minecraft;
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

    @Inject(method = "getRightClickOptions", at = @At("RETURN"), remap = false)
    private void chuckpack$addAutoFlyHere(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            int wx = rightClickX;
            int wz = rightClickZ;

            final int wy = Math.max(-64, Math.min(319, rightClickY));
            boolean yKnown = wy != 319;

            for (int i = options.size() - 1; i >= 0; i--) {
                String name = options.get(i).getDisplayName().getString();
                if (name.equals("Auto Fly Here") || name.equals("Swarm Fly Here")) {
                    options.remove(i);
                }
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

            insertIdx = Math.max(0, options.size() - 1);
            options.add(insertIdx, new RightClickOption("Swarm Fly Here", insertIdx, (xaero.map.gui.IRightClickableElement) (Object) this) {
                    @Override
                    public void onAction(net.minecraft.client.gui.screens.Screen screen) {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (swarm == null || !swarm.isActive()) {
                            ChatUtils.error("Swarm module must be active to use Swarm Fly Here.");
                            return;
                        }
                        String cmd = "swarm fly " + wx + " " + (yKnown ? wy + " " : "") + wz;
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) {
                            meteordevelopment.meteorclient.utils.player.ChatUtils.sendPlayerMsg(cmd);
                        }
                    }
                });
        } catch (Throwable ignored) {
        }
    }
}
