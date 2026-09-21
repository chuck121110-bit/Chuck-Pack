package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapIntegration extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> freecamWaypointKeybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("freecam-waypoint-keybind")
        .description("Press to create a waypoint where FreeCam is looking. Only works when Freecam is active.")
        .defaultValue(Keybind.none())
        .action(this::freecamWaypoint)
        .build()
    );

    private final Setting<Boolean> chatCoordsClick = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-coords-click")
        .description("Highlight X Y Z coordinates in chat, clicking opens the waypoint screen. No hover text, just works.")
        .defaultValue(true)
        .build()
    );

    public MapIntegration() {
        super(Categories.Misc, "map-integration", "Waypoints from FreeCam position and clickable chat coordinates.");
    }

    public static boolean chatCoordsEnabled() {
        try {
            MapIntegration module = Modules.get().get(MapIntegration.class);
            return module != null && module.isActive() && module.chatCoordsClick.get();
        } catch (Throwable ignored) {
            return false;
        }
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

    // ------------------------------------------------------------------
    // Clickable chat coordinates (moved here from the chat mixin so the
    // whole feature is owned by this module; no hover text on purpose).
    // ------------------------------------------------------------------

    private static final String NUM = "(-?\\d{1,9}(?:\\.\\d+)?)";

    private static final Pattern P_LABELED = Pattern.compile(
        "(?i)\\bx\\s*[:=]?\\s*" + NUM + "[\\s,;]+y\\s*[:=]?\\s*" + NUM + "[\\s,;]+z\\s*[:=]?\\s*" + NUM + "(?![\\w.])"
    );
    private static final Pattern P_PAREN3 = Pattern.compile(
        "\\(\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*\\)"
    );
    private static final Pattern P_BRACKET3 = Pattern.compile(
        "\\[\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*\\]"
    );
    private static final Pattern P_TP = Pattern.compile(
        "(?i)(?:/tp|/teleport|\\btp|\\bteleport)(?:\\s+@[\\w]+)?\\s+" + NUM + "\\s+" + NUM + "\\s+" + NUM
    );
    private static final Pattern P_BARITONE = Pattern.compile(
        "(?i)#(?:goto|mine|thisway|path|goal)\\s+" + NUM + "\\s+" + NUM + "\\s+" + NUM
    );
    private static final Pattern P_COMMA3 = Pattern.compile(
        "(?<![\\w.\\-])" + NUM + "\\s*,\\s*" + NUM + "\\s*,\\s*" + NUM + "(?![\\w.\\-])"
    );
    private static final Pattern P_SLASH3 = Pattern.compile(
        "(?<![\\w.\\-])" + NUM + "\\s*/\\s*" + NUM + "\\s*/\\s*" + NUM + "(?![\\w.\\-])"
    );
    private static final Pattern P_SPACE3 = Pattern.compile(
        "(?<![\\w.\\-#:])" + NUM + "\\s+" + NUM + "\\s+" + NUM + "(?![\\w.\\-:])"
    );

    private static final Pattern P_XZ_LABELED = Pattern.compile(
        "(?i)\\bx\\s*[:=]?\\s*" + NUM + "\\s*[,;\\s]\\s*z\\s*[:=]?\\s*" + NUM + "(?![\\w.])"
    );
    private static final Pattern P_PAREN2 = Pattern.compile(
        "\\(\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*\\)"
    );
    private static final Pattern P_BRACKET2 = Pattern.compile(
        "\\[\\s*" + NUM + "\\s*[,;\\s]\\s*" + NUM + "\\s*\\]"
    );
    private static final Pattern P_COMMA2 = Pattern.compile(
        "(?<![\\w.\\-])" + NUM + "\\s*,\\s*" + NUM + "(?![\\w.\\-])"
    );
    private static final Pattern P_SPACE2 = Pattern.compile(
        "(?<![\\w.\\-#:])" + NUM + "\\s+" + NUM + "\\s+" + NUM + "(?![\\w.\\-:])"
    );
    private static final Pattern P_SLASH2 = Pattern.compile(
        "(?<![\\w.\\-])" + NUM + "\\s*/\\s*" + NUM + "(?![\\w.\\-])"
    );

    private static final int MAX_HORIZONTAL = 30000000;

    public static Component highlightCoords(Component message) {
        try {
            String text = message.getString();
            if (text == null || text.isEmpty()) return message;

            List<CoordinateMatch> matches = findCoordinates(text);
            if (matches.isEmpty()) return message;

            MutableComponent result = Component.literal("");
            int lastEnd = 0;

            for (CoordinateMatch match : matches) {
                if (match.start > lastEnd) {
                    result.append(Component.literal(text.substring(lastEnd, match.start)));
                }

                String coordText = text.substring(match.start, match.end);
                String yToken = match.twoD ? "~" : String.valueOf(match.y);

                MutableComponent coordComponent = Component.literal(coordText);
                Style coordStyle = Style.EMPTY
                    .withColor(ChatFormatting.AQUA)
                    .withClickEvent(new ClickEvent.RunCommand(
                        ".chatwaypoint-gui " + match.x + " " + yToken + " " + match.z
                    ));

                coordComponent.setStyle(coordStyle);
                result.append(coordComponent);
                lastEnd = match.end;
            }

            if (lastEnd < text.length()) {
                result.append(Component.literal(text.substring(lastEnd)));
            }

            result.setStyle(message.getStyle());
            return result;
        } catch (Exception ignored) {
            return message;
        }
    }

    private static List<CoordinateMatch> findCoordinates(String text) {
        List<CoordinateMatch> matches = new ArrayList<>();

        add3(matches, P_LABELED, text);
        add3(matches, P_PAREN3, text);
        add3(matches, P_BRACKET3, text);
        addTp(matches, P_TP, text);
        addTp(matches, P_BARITONE, text);
        add3(matches, P_COMMA3, text);
        add3(matches, P_SLASH3, text);
        add3y(matches, P_SPACE3, text);

        int playerY = playerY();
        if (playerY != Integer.MIN_VALUE) {
            add2(matches, P_XZ_LABELED, text, playerY);
            add2(matches, P_PAREN2, text, playerY);
            add2(matches, P_BRACKET2, text, playerY);
            add2(matches, P_COMMA2, text, playerY);
            add2(matches, P_SPACE2, text, playerY);
            add2(matches, P_SLASH2, text, playerY);
        }

        matches.sort((a, b) -> Integer.compare(a.start, b.start));
        List<CoordinateMatch> deduped = new ArrayList<>();
        int lastEnd = -1;
        for (CoordinateMatch m : matches) {
            if (m.start < lastEnd) continue;
            deduped.add(m);
            lastEnd = m.end;
        }
        return deduped;
    }

    private static int playerY() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.blockPosition().getY();
        } catch (Throwable ignored) {}
        return Integer.MIN_VALUE;
    }

    private static void add3(List<CoordinateMatch> out, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            tryAdd(out, m.start(), m.end(), m.group(1), m.group(2), m.group(3));
        }
    }

    private static void addTp(List<CoordinateMatch> out, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (!overlaps(out, m.start(), m.end())) {
                tryAdd(out, m.start(), m.end(), m.group(1), m.group(2), m.group(3));
            }
        }
    }

    private static void add3y(List<CoordinateMatch> out, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (!overlaps(out, m.start(), m.end())) {
                try {
                    int y = (int) Math.round(Double.parseDouble(m.group(2)));
                    if (y < -64 || y > 320) continue;
                    tryAdd(out, m.start(), m.end(), m.group(1), m.group(2), m.group(3));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private static void add2(List<CoordinateMatch> out, Pattern p, String text, int y) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (!overlaps(out, m.start(), m.end())) {
                tryAdd(out, m.start(), m.end(), m.group(1), String.valueOf(y), m.group(2), true);
            }
        }
    }

    private static void tryAdd(List<CoordinateMatch> out, int s, int e, String xs, String ys, String zs) {
        tryAdd(out, s, e, xs, ys, zs, false);
    }

    private static void tryAdd(List<CoordinateMatch> out, int s, int e, String xs, String ys, String zs, boolean twoD) {
        try {
            int x = (int) Math.round(Double.parseDouble(xs));
            int y = (int) Math.round(Double.parseDouble(ys));
            int z = (int) Math.round(Double.parseDouble(zs));
            if (Math.abs((long) x) > MAX_HORIZONTAL || Math.abs((long) z) > MAX_HORIZONTAL) return;
            if (y < -64 || y > 320) return;
            out.add(new CoordinateMatch(s, e, x, y, z, twoD));
        } catch (NumberFormatException ignored) {}
    }

    private static boolean overlaps(List<CoordinateMatch> existing, int start, int end) {
        for (CoordinateMatch m : existing) {
            if (start < m.end && end > m.start) return true;
        }
        return false;
    }

    private static class CoordinateMatch {
        final int start;
        final int end;
        final int x;
        final int y;
        final int z;
        final boolean twoD;

        CoordinateMatch(int start, int end, int x, int y, int z, boolean twoD) {
            this.start = start;
            this.end = end;
            this.x = x;
            this.y = y;
            this.z = z;
            this.twoD = twoD;
        }
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
                    net.minecraft.client.Minecraft.getInstance().setScreenAndShow((net.minecraft.client.gui.screens.Screen) gui);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
