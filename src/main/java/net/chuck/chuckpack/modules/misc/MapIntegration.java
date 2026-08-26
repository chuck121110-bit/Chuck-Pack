package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.Keybind;

public class MapIntegration extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> freecamWaypointKeybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("freecam-waypoint-keybind")
        .description("Press to create a waypoint where FreeCam is looking. Only works when Freecam is active.")
        .defaultValue(Keybind.none())
        .action(this::freecamWaypoint)
        .build()
    );

    public MapIntegration() {
        super(Categories.Misc, "map-integration", "Creates waypoints from FreeCam position.");
    }

    private void freecamWaypoint() {
        if (mc.player == null || mc.level == null) return;

        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam == null || !freecam.isActive()) return;

        int x = (int) Math.floor(freecam.pos.x);
        int y = (int) Math.floor(freecam.pos.y);
        int z = (int) Math.floor(freecam.pos.z);
        openWaypointAddViaReflection(x, y, z, true, "", "");
    }

    private static void openWaypointAddViaReflection(int x, int y, int z, boolean yIncluded, String name, String initial) {
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
                .newInstance(x, y, z, name, initial, color, purpose, false, yIncluded);

            Object hudMod = Class.forName("xaero.common.HudMod").getField("INSTANCE").get(null);
            Object wpList = com.google.common.collect.Lists.newArrayList(waypoint);

            Class<?> guiClass = Class.forName("xaero.common.gui.GuiAddWaypoint");
            for (java.lang.reflect.Constructor<?> ctor : guiClass.getConstructors()) {
                if (ctor.getParameterTypes().length == 9) {
                    Object gui = ctor.newInstance(hudMod, session, null, null, wpList, path, currentWorld, setId, true);
                    net.minecraft.client.Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) gui);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
