package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
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

/**
 * Standalone module: highlights X Y Z coordinates in chat, clicking opens
 * the Xaero waypoint screen. No hover text on purpose, just works.
 */
public class ChatWaypoints extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatCoordsClick = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-coords-click")
        .description("Highlight X Y Z coordinates in chat, clicking opens the waypoint screen. No hover text, just works.")
        .defaultValue(true)
        .build()
    );

    public ChatWaypoints() {
        super(Categories.Misc, "chat-waypoints", "Clickable chat coordinates that open the waypoint screen.");
    }

    public static boolean chatCoordsEnabled() {
        try {
            ChatWaypoints module = Modules.get().get(ChatWaypoints.class);
            return module != null && module.isActive() && module.chatCoordsClick.get();
        } catch (Throwable ignored) {
            return false;
        }
    }

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
}
