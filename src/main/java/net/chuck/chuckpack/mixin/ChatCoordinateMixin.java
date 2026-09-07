package net.chuck.chuckpack.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Highlights coordinates in chat (cyan) — clicking creates a "chat waypoint"
// (random color) via .chatwaypoint. Supports 3D and 2D (player Y) formats.
@Mixin(ChatComponent.class)
public class ChatCoordinateMixin {

    private static final String NUM = "(-?\\d{1,9}(?:\\.\\d+)?)";

    // 3D formats
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

    // 2D formats (Y falls back to the player's current Y)
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
        "(?<![\\w.\\-#:])" + NUM + "\\s+" + NUM + "(?![\\w.\\-:])"
    );
    private static final Pattern P_SLASH2 = Pattern.compile(
        "(?<![\\w.\\-])" + NUM + "\\s*/\\s*" + NUM + "(?![\\w.\\-])"
    );

    private static final int MAX_HORIZONTAL = 30000000;

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), argsOnly = true)
    private Component highlightCoords(Component message) {
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

                MutableComponent coordComponent = Component.literal(coordText);
                Style coordStyle = Style.EMPTY
                    .withColor(ChatFormatting.AQUA)
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click to create waypoint at " + match.x + ", " + match.y + ", " + match.z)
                            .withStyle(ChatFormatting.YELLOW)
                    ))
                    .withClickEvent(new ClickEvent.RunCommand(
                        ".chatwaypoint " + match.x + " " + match.y + " " + match.z
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

    private List<CoordinateMatch> findCoordinates(String text) {
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

    private int playerY() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.blockPosition().getY();
        } catch (Throwable ignored) {}
        return Integer.MIN_VALUE;
    }

    private void add3(List<CoordinateMatch> out, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            tryAdd(out, m.start(), m.end(), m.group(1), m.group(2), m.group(3));
        }
    }

    private void addTp(List<CoordinateMatch> out, Pattern p, String text) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (!overlaps(out, m.start(), m.end())) {
                tryAdd(out, m.start(), m.end(), m.group(1), m.group(2), m.group(3));
            }
        }
    }

    private void add3y(List<CoordinateMatch> out, Pattern p, String text) {
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

    private void add2(List<CoordinateMatch> out, Pattern p, String text, int y) {
        Matcher m = p.matcher(text);
        while (m.find()) {
            if (!overlaps(out, m.start(), m.end())) {
                tryAdd(out, m.start(), m.end(), m.group(1), String.valueOf(y), m.group(2));
            }
        }
    }

    private void tryAdd(List<CoordinateMatch> out, int s, int e, String xs, String ys, String zs) {
        try {
            int x = (int) Math.round(Double.parseDouble(xs));
            int y = (int) Math.round(Double.parseDouble(ys));
            int z = (int) Math.round(Double.parseDouble(zs));
            if (Math.abs((long) x) > MAX_HORIZONTAL || Math.abs((long) z) > MAX_HORIZONTAL) return;
            if (y < -64 || y > 320) return;
            out.add(new CoordinateMatch(s, e, x, y, z));
        } catch (NumberFormatException ignored) {}
    }

    private boolean overlaps(List<CoordinateMatch> existing, int start, int end) {
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

        CoordinateMatch(int start, int end, int x, int y, int z) {
            this.start = start;
            this.end = end;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
