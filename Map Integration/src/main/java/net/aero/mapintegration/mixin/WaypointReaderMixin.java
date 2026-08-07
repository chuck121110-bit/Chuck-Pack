package net.aero.mapintegration.mixin;

import java.util.ArrayList;
import net.aero.mapintegration.config.ModConfig;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;
import xaero.map.mods.SupportMods;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;

@Mixin(value = WaypointReader.class, remap = false)
public abstract class WaypointReaderMixin {

    @Inject(method = "getRightClickOptions(Lxaero/map/mods/gui/Waypoint;Lxaero/map/gui/IRightClickableElement;)Ljava/util/ArrayList;", at = @At("RETURN"))
    private void mapintegration$modifyWaypointMenu(Waypoint waypoint, IRightClickableElement target, CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            ModConfig c = ModConfig.get();

            for (int i = options.size() - 1; i >= 0; i--) {
                RightClickOption option = options.get(i);
                String name = option.getDisplayName().getString();

                if (c.skipDeleteConfirmation && (name.contains("Delete") || name.contains("Deletion"))) {
                    int deleteIdx = i;
                    xaero.map.mods.gui.Waypoint wp = waypoint;
                    RightClickOption newDelete = new RightClickOption("Delete Waypoint", deleteIdx, target) {
                        @Override
                        public void onAction(Screen screen) {
                            SupportMods.xaeroMinimap.deleteWaypoint(wp);
                        }
                    };
                    options.set(deleteIdx, newDelete);
                } else if (name.contains("Restore")) {
                    int restoreIdx = i;
                    xaero.map.mods.gui.Waypoint wp = waypoint;
                    RightClickOption newRestore = new RightClickOption("Save Waypoint", restoreIdx, target) {
                        @Override
                        public void onAction(Screen screen) {
                            SupportMods.xaeroMinimap.toggleTemporaryWaypoint(wp);
                        }
                    };
                    options.set(restoreIdx, newRestore);
                } else if (c.removeTeleport && name.contains("Teleport")) {
                    options.remove(i);
                } else if (c.removeShare && name.contains("Share")) {
                    options.remove(i);
                } else if (c.improveCoordinateDisplay && (name.startsWith("C:") || name.startsWith("c:"))) {
                    options.remove(i);
                } else if (name.startsWith("X:") || name.startsWith("x:")) {
                    int coordIdx = i;
                    String coordName = name;
                    RightClickOption coordOption = new RightClickOption("", Style.EMPTY.withFormatting(Formatting.GRAY), coordIdx, target) {
                        @Override
                        protected String getName() {
                            return coordName;
                        }
                        @Override
                        public void onAction(Screen screen) {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            if (mc != null && mc.keyboard != null) {
                                mc.keyboard.setClipboard(coordName);
                            }
                        }
                    };
                    options.set(coordIdx, coordOption);
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
