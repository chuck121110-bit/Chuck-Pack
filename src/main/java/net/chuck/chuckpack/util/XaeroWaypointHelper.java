package net.chuck.chuckpack.util;

import java.util.Random;

// Creates Xaero minimap waypoints via reflection (no-ops when Xaero is absent).
// Same verified 26.2 call chain as AutoFly's waypoint code.
public final class XaeroWaypointHelper {
    private static final Random RANDOM = new Random();

    private XaeroWaypointHelper() {}

    // Opens Xaero's add-waypoint screen prefilled (name "chat waypoint",
    // initial "C", AQUA, given coords). Mirrors GuiWaypoints' own edit-button
    // call: transient waypoint in the edited list + adding=true, so confirm
    // upserts it into the current set via WaypointSet.addAll. Cancel saves nothing.
    public static boolean openChatWaypointGui(int x, int y, int z) {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || mc.gui == null) return false;

            Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
                .getField("MINIMAP").get(null);
            Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
            if (session == null) return false;
            Object modMain = session.getClass().getMethod("getModMain").invoke(session);

            Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
            Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
            if (currentWorld == null) return false;

            Object container = currentWorld.getClass().getMethod("getContainer").invoke(currentWorld);
            Object root = container.getClass().getMethod("getRoot").invoke(container);
            Object path = root.getClass().getMethod("getPath").invoke(root);
            Object setId = currentWorld.getClass().getMethod("getCurrentWaypointSetId").invoke(currentWorld);

            Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
            Class<?> colorClass = Class.forName("xaero.hud.minimap.waypoint.WaypointColor");
            Class<?> purposeClass = Class.forName("xaero.hud.minimap.waypoint.WaypointPurpose");

            @SuppressWarnings({"unchecked", "rawtypes"})
            Object aqua = Enum.valueOf((Class) colorClass, "AQUA");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object purpose = Enum.valueOf((Class) purposeClass, "NORMAL");

            Object waypoint = wpClass.getConstructor(int.class, int.class, int.class, String.class, String.class,
                    colorClass, purposeClass, boolean.class, boolean.class)
                .newInstance(x, y, z, "chat waypoint", "C", aqua, purpose, false, true);

            java.util.ArrayList<Object> list = new java.util.ArrayList<>();
            list.add(waypoint);

            Class<?> guiClass = Class.forName("xaero.common.gui.GuiAddWaypoint");
            Object gui = guiClass.getConstructor(
                    Class.forName("xaero.common.HudMod"),
                    Class.forName("xaero.hud.minimap.module.MinimapSession"),
                    net.minecraft.client.gui.screens.Screen.class,
                    net.minecraft.client.gui.screens.Screen.class,
                    java.util.ArrayList.class,
                    Class.forName("xaero.hud.path.XaeroPath"),
                    Class.forName("xaero.hud.minimap.world.MinimapWorld"),
                    String.class, boolean.class)
                .newInstance(modMain, session, mc.gui.screen(), mc.gui.screen(), list, path, currentWorld, setId, true);

            mc.gui.setScreen((net.minecraft.client.gui.screens.Screen) gui);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean addWaypoint(String name, int x, int y, int z, boolean randomColor) {
        try {
            Object builtInMinimap = Class.forName("xaero.hud.minimap.BuiltInHudModules")
                .getField("MINIMAP").get(null);
            Object session = builtInMinimap.getClass().getMethod("getCurrentSession").invoke(builtInMinimap);
            if (session == null) return false;

            Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
            Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
            if (currentWorld == null) return false;

            Object currentSet = currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
            if (currentSet == null) return false;

            Class<?> wpClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
            Class<?> colorClass = Class.forName("xaero.hud.minimap.waypoint.WaypointColor");
            Class<?> purposeClass = Class.forName("xaero.hud.minimap.waypoint.WaypointPurpose");

            Object color = null;
            if (randomColor) {
                Object[] colors = (Object[]) colorClass.getMethod("values").invoke(null);
                if (colors != null && colors.length > 0) color = colors[RANDOM.nextInt(colors.length)];
            } else {
                for (Object c : (Object[]) colorClass.getMethod("values").invoke(null)) {
                    if (c.toString().equalsIgnoreCase("YELLOW")) { color = c; break; }
                }
            }
            if (color == null) return false;

            @SuppressWarnings({"unchecked", "rawtypes"})
            Object purpose = Enum.valueOf((Class) purposeClass, "NORMAL");

            String initials = name.isEmpty() ? "W" : name.substring(0, 1).toUpperCase();
            Object waypoint = wpClass.getConstructor(int.class, int.class, int.class, String.class, String.class,
                    colorClass, purposeClass, boolean.class, boolean.class)
                .newInstance(x, y, z, name, initials, color, purpose, false, true);
            currentSet.getClass().getMethod("add", wpClass).invoke(currentSet, waypoint);

            try {
                Object supportMods = Class.forName("xaero.map.mods.SupportMods").getField("xaeroMinimap").get(null);
                if (supportMods != null) {
                    java.lang.reflect.Field f = supportMods.getClass().getDeclaredField("refreshWaypoints");
                    f.setAccessible(true);
                    f.setBoolean(supportMods, true);
                }
            } catch (Throwable ignored) {}
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
