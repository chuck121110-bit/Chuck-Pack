package net.aero.mapintegration.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Pattern;

@Mixin(ChatScreen.class)
public abstract class ChatHudClickMixin {

    private static final Pattern COORD_PATTERN_3 = Pattern.compile("^(-?\\d+),\\s*(-?\\d+),\\s*(-?\\d+)$");
    private static final Pattern COORD_PATTERN_2 = Pattern.compile("^(-?\\d+),\\s*~,\\s*(-?\\d+)$");

    @Inject(method = "handleClickEvent", at = @At("HEAD"), cancellable = true)
    private void mapintegration$handleCoordClick(Style style, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (bl) return;
            if (style == null) return;

            ClickEvent clickEvent = style.getClickEvent();
            if (!(clickEvent instanceof ClickEvent.CopyToClipboard copy)) return;

            String value = copy.value();

            java.util.regex.Matcher m3 = COORD_PATTERN_3.matcher(value);
            if (m3.matches()) {
                int x = Integer.parseInt(m3.group(1));
                int y = Integer.parseInt(m3.group(2));
                int z = Integer.parseInt(m3.group(3));
                openWaypointAddViaReflection(x, y, z, true);
                cir.setReturnValue(true);
                return;
            }

            java.util.regex.Matcher m2 = COORD_PATTERN_2.matcher(value);
            if (m2.matches()) {
                int x = Integer.parseInt(m2.group(1));
                int z = Integer.parseInt(m2.group(2));
                openWaypointAddViaReflection(x, 0, z, false);
                cir.setReturnValue(true);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void openWaypointAddViaReflection(int x, int y, int z, boolean yIncluded) {
        try {
            Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
                .getField("MINIMAP").get(null);
            Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
            if (session == null) return;

            Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
            Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
            if (currentWorld == null) return;

            Object container = currentWorld.getClass().getMethod("getContainer").invoke(currentWorld);
            Object root = container.getClass().getMethod("getRoot").invoke(container);
            Object path = root.getClass().getMethod("getPath").invoke(root);
            String setId = (String) currentWorld.getClass().getMethod("getCurrentWaypointSetId").invoke(currentWorld);

            Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
            Class<?> colorClass = Class.forName("xaero.hud.minimap.waypoint.WaypointColor");
            Class<?> purposeClass = Class.forName("xaero.hud.minimap.waypoint.WaypointPurpose");
            Object[] colors = colorClass.getEnumConstants();
            java.util.List<Object> valid = new java.util.ArrayList<>();
            for (Object c : colors) {
                String colorName = ((Enum<?>) c).name();
                if (!colorName.equals("BLACK") && !colorName.equals("WHITE")) valid.add(c);
            }
            Object color = valid.get(new java.util.Random().nextInt(valid.size()));
            Object purpose = purposeClass.getField("DESTINATION").get(null);

            Object waypoint = wpClass.getConstructor(int.class, int.class, int.class, String.class, String.class, colorClass, purposeClass, boolean.class, boolean.class)
                .newInstance(x, y, z, "Chat waypoint", "Cw", color, purpose, false, yIncluded);

            Object hudMod = Class.forName("xaero.common.HudMod").getField("INSTANCE").get(null);
            Object wpList = com.google.common.collect.Lists.newArrayList(waypoint);

            Class<?> guiClass = Class.forName("xaero.common.gui.GuiAddWaypoint");
            for (java.lang.reflect.Constructor<?> ctor : guiClass.getConstructors()) {
                if (ctor.getParameterTypes().length == 9) {
                    Object gui = ctor.newInstance(hudMod, session, null, null, wpList, path, currentWorld, setId, true);
                    MinecraftClient.getInstance().setScreen((net.minecraft.client.gui.screen.Screen) gui);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
