package net.chuck.chuckpack.util;

import java.util.Random;

// Creates Xaero minimap waypoints via reflection (no-ops when Xaero is absent).
// Same verified 26.2 call chain as AutoFly's waypoint code.
public final class XaeroWaypointHelper {
    private static final Random RANDOM = new Random();

    private XaeroWaypointHelper() {}

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
