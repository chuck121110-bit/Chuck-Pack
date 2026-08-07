package net.aero.mapintegration.mixin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.aero.mapintegration.config.ModConfig;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.hud.ChatHud", remap = true)
public abstract class ChatHudMixin {

    private static final Pattern COORD_PATTERN_LABELED = Pattern.compile(
        "[Xx]\\s*:\\s*(-?\\d{1,8})\\s*[,;]?\\s+[Yy]\\s*:\\s*(-?\\d{1,8})\\s*[,;]?\\s+[Zz]\\s*:\\s*(-?\\d{1,8})"
    );
    private static final Pattern COORD_PATTERN_LABELED_XZ = Pattern.compile(
        "[Xx]\\s*:\\s*(-?\\d{1,8})\\s*[,;]?\\s+[Zz]\\s*:\\s*(-?\\d{1,8})"
    );
    private static final Pattern COORD_PATTERN_PLAIN = Pattern.compile(
        "(-?\\d{1,8})\\s*[,\\s]\\s*(-?\\d{1,8})(?:\\s*[,\\s]\\s*(-?\\d{1,8}))?"
    );
    private static final Pattern COORD_PATTERN_TILDE = Pattern.compile(
        "(-?\\d{1,8})\\s+~\\s+(-?\\d{1,8})"
    );

    @Unique
    private static final Map<Text, int[]> mapintegration$pendingCoords = new HashMap<>();

    private static class CoordMatch {
        final int start, end, x, y, z;
        final String text;
        final boolean hasY;

        CoordMatch(int start, int end, String text, int x, int y, int z, boolean hasY) {
            this.start = start;
            this.end = end;
            this.text = text;
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasY = hasY;
        }
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text mapintegration$processChatCoordinates(Text message) {
        try {
            if (!ModConfig.get().highlightChatCoordinates) return message;

            String plainText = message.getString();
            List<CoordMatch> matches = new ArrayList<>();

            Matcher labeledMatcher = COORD_PATTERN_LABELED.matcher(plainText);
            while (labeledMatcher.find()) {
                matches.add(new CoordMatch(
                    labeledMatcher.start(), labeledMatcher.end(), labeledMatcher.group(),
                    Integer.parseInt(labeledMatcher.group(1)),
                    Integer.parseInt(labeledMatcher.group(2)),
                    Integer.parseInt(labeledMatcher.group(3)),
                    true
                ));
            }

            Matcher labeledXZMatcher = COORD_PATTERN_LABELED_XZ.matcher(plainText);
            while (labeledXZMatcher.find()) {
                boolean overlaps = false;
                for (CoordMatch m : matches) {
                    if (labeledXZMatcher.start() < m.end && labeledXZMatcher.end() > m.start) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    matches.add(new CoordMatch(
                        labeledXZMatcher.start(), labeledXZMatcher.end(), labeledXZMatcher.group(),
                        Integer.parseInt(labeledXZMatcher.group(1)),
                        0,
                        Integer.parseInt(labeledXZMatcher.group(2)),
                        false
                    ));
                }
            }

            Matcher plainMatcher = COORD_PATTERN_PLAIN.matcher(plainText);
            while (plainMatcher.find()) {
                boolean overlaps = false;
                for (CoordMatch m : matches) {
                    if (plainMatcher.start() < m.end && plainMatcher.end() > m.start) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    boolean hasThree = plainMatcher.group(3) != null;
                    matches.add(new CoordMatch(
                        plainMatcher.start(), plainMatcher.end(), plainMatcher.group(),
                        Integer.parseInt(plainMatcher.group(1)),
                        hasThree ? Integer.parseInt(plainMatcher.group(2)) : 0,
                        hasThree ? Integer.parseInt(plainMatcher.group(3)) : Integer.parseInt(plainMatcher.group(2)),
                        hasThree
                    ));
                }
            }

            Matcher tildeMatcher = COORD_PATTERN_TILDE.matcher(plainText);
            while (tildeMatcher.find()) {
                boolean overlaps = false;
                for (CoordMatch m : matches) {
                    if (tildeMatcher.start() < m.end && tildeMatcher.end() > m.start) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    matches.add(new CoordMatch(
                        tildeMatcher.start(), tildeMatcher.end(), tildeMatcher.group(),
                        Integer.parseInt(tildeMatcher.group(1)),
                        0,
                        Integer.parseInt(tildeMatcher.group(2)),
                        false
                    ));
                }
            }

            if (matches.isEmpty()) return message;

            matches.sort(Comparator.comparingInt(m -> m.start));

            MutableText newMessage = Text.empty();
            int lastEnd = 0;

            for (CoordMatch match : matches) {
                if (match.start > lastEnd) {
                    newMessage.append(Text.literal(plainText.substring(lastEnd, match.start)));
                }

                final int fx = match.x;
                final int fz = match.z;
                final boolean fHasY = match.hasY;
                final int fy = match.y;

                String hoverText;
                String copyText;
                if (fHasY) {
                    hoverText = String.format("Click to create waypoint at %d, %d, %d", fx, fy, fz);
                    copyText = String.format("%d, %d, %d", fx, fy, fz);
                } else {
                    hoverText = String.format("Click to create waypoint at %d, ~, %d", fx, fz);
                    copyText = String.format("%d, ~, %d", fx, fz);
                }

                String finalHoverText = hoverText;
                String finalCopyText = copyText;
                MutableText coordText = Text.literal(match.text).setStyle(Style.EMPTY
                    .withColor(Formatting.AQUA)
                    .withItalic(true)
                    .withUnderline(true)
                    .withClickEvent(new ClickEvent.CopyToClipboard(finalCopyText))
                    .withHoverEvent(new HoverEvent.ShowText(Text.literal(finalHoverText)))
                );

                mapintegration$pendingCoords.put(coordText, new int[]{fx, fy, fz, fHasY ? 1 : 0});
                newMessage.append(coordText);

                lastEnd = match.end;
            }

            if (lastEnd < plainText.length()) {
                newMessage.append(Text.literal(plainText.substring(lastEnd)));
            }

            return newMessage;
        } catch (Throwable e) {
            return message;
        }
    }

    @Unique
    private static int[] mapintegration$getCoords(Text text) {
        return mapintegration$pendingCoords.get(text);
    }
}
