package net.aero.mapintegration.mixin;

import java.util.ArrayList;
import java.util.Locale;
import net.aero.mapintegration.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void mapintegration$modifyMapMenu(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        try {
            ArrayList<RightClickOption> options = cir.getReturnValue();
            if (options == null) return;

            ModConfig c = ModConfig.get();

            for (int i = options.size() - 1; i >= 0; i--) {
                String name = options.get(i).getDisplayName().getString().toLowerCase(Locale.ROOT);

                if (c.removeTeleport && name.contains("teleport")) {
                    options.remove(i);
                } else if (c.removeExport && name.contains("export")) {
                    options.remove(i);
                } else if (c.removeSettings && name.contains("settings")) {
                    options.remove(i);
                } else if (c.removeShare && name.contains("share")) {
                    options.remove(i);
                } else if (c.improveCoordinateDisplay && (name.startsWith("c:") || name.startsWith("c:"))) {
                    options.remove(i);
                }
            }

            net.minecraft.client.gui.screen.Screen self = (net.minecraft.client.gui.screen.Screen) (Object) this;

            for (int i = options.size() - 1; i >= 0; i--) {
                String name = options.get(i).getDisplayName().getString();
                if (name.matches("(?i)\\s*x:\\s*-?\\d+.*y:\\s*-?\\d+.*z:\\s*-?\\d+.*")
                    || name.matches("(?i)\\s*x:\\s*-?\\d+.*z:\\s*-?\\d+.*")) {
                    int coordIdx = i;
                    String coordName = name;
                    RightClickOption coordOption = new RightClickOption("", Style.EMPTY.withFormatting(Formatting.GRAY), coordIdx, (IRightClickableElement) (Object) this) {
                        @Override
                        protected String getName() {
                            return coordName;
                        }
                        @Override
                        public void onAction(net.minecraft.client.gui.screen.Screen screen) {
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
