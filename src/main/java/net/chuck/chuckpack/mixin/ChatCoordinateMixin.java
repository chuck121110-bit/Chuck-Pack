package net.chuck.chuckpack.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatCoordinateMixin {

    private static final Pattern COORD_XYZ_LABELED = Pattern.compile(
        "[Xx]\\s*[:=]?\\s*(-?\\d{1,10})\\s+[Yy]\\s*[:=]?\\s*(-?\\d{1,10})\\s+[Zz]\\s*[:=]?\\s*(-?\\d{1,10})"
    );

    private static final Pattern COORD_XYZ_BRACKET = Pattern.compile(
        "\\(\\s*(-?\\d{1,10})\\s*,\\s*(-?\\d{1,10})\\s*,\\s*(-?\\d{1,10})\\s*\\)"
    );

    private static final Pattern COORD_XYZ_PLAIN = Pattern.compile(
        "(?<![\\w.-])(-?\\d{1,10})\\s*,\\s*(-?\\d{1,10})\\s*,\\s*(-?\\d{1,10})(?![\\w.-])"
    );

    private static final Pattern COORD_XYZ_SPACE = Pattern.compile(
        "(?<![\\w.-])(-?\\d{1,10})\\s+(-?\\d{1,10})\\s+(-?\\d{1,10})(?![\\w.-])"
    );

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
                    .withColor(ChatFormatting.GREEN)
                    .withBold(true)
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click to copy coordinates")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("\nShift+click to teleport").withStyle(ChatFormatting.GRAY))
                    ))
                    .withClickEvent(new ClickEvent.CopyToClipboard(
                        match.x + " " + match.y + " " + match.z
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

        Matcher m1 = COORD_XYZ_LABELED.matcher(text);
        while (m1.find()) {
            matches.add(new CoordinateMatch(m1.start(), m1.end(),
                Integer.parseInt(m1.group(1)),
                Integer.parseInt(m1.group(2)),
                Integer.parseInt(m1.group(3))));
        }

        Matcher m2 = COORD_XYZ_BRACKET.matcher(text);
        while (m2.find()) {
            if (!overlaps(matches, m2.start(), m2.end())) {
                matches.add(new CoordinateMatch(m2.start(), m2.end(),
                    Integer.parseInt(m2.group(1)),
                    Integer.parseInt(m2.group(2)),
                    Integer.parseInt(m2.group(3))));
            }
        }

        Matcher m3 = COORD_XYZ_PLAIN.matcher(text);
        while (m3.find()) {
            if (!overlaps(matches, m3.start(), m3.end())) {
                matches.add(new CoordinateMatch(m3.start(), m3.end(),
                    Integer.parseInt(m3.group(1)),
                    Integer.parseInt(m3.group(2)),
                    Integer.parseInt(m3.group(3))));
            }
        }

        Matcher m4 = COORD_XYZ_SPACE.matcher(text);
        while (m4.find()) {
            if (!overlaps(matches, m4.start(), m4.end())) {
                int x = Integer.parseInt(m4.group(1));
                int y = Integer.parseInt(m4.group(2));
                int z = Integer.parseInt(m4.group(3));
                if (y >= -64 && y <= 320) {
                    matches.add(new CoordinateMatch(m4.start(), m4.end(), x, y, z));
                }
            }
        }

        matches.sort((a, b) -> Integer.compare(a.start, b.start));
        return matches;
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
