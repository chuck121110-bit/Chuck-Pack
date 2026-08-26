package net.chuck.chuckpack.hud;

/*
    Ported from: https://github.com/etianl/Trouser-Streak
    Originally by etianl
    HUD element companion to PlayerTriangulate, displays triangulated player positions
    obtained from the locator bar waypoint system.
*/

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.chuck.chuckpack.modules.misc.PlayerTriangulate;

import java.util.*;

public class PlayerTriangulateHud extends HudElement {
    public static final HudElementInfo<PlayerTriangulateHud> INFO =
            new HudElementInfo<>(KeybindsHud.AERO_GROUP, "player-triangulations",
                    "Shows triangulated player positions from PlayerTriangulate.", PlayerTriangulateHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showBackground = sgGeneral.add(new BoolSetting.Builder()
            .name("background")
            .description("Show a background behind the HUD.")
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Background color.")
            .defaultValue(new SettingColor(25, 25, 25, 100))
            .visible(showBackground::get)
            .build()
    );

    private final Setting<Integer> maxHudEntries = sgGeneral.add(new IntSetting.Builder()
            .name("max-hud-entries")
            .description("Maximum number of triangulated players shown in the HUD.")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 1000)
            .build()
    );

    public PlayerTriangulateHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        Map<UUID, PlayerTriangulate.TriangulationResult> results = PlayerTriangulate.getLastResults();

        int maxEntries = maxHudEntries.get();

        List<PlayerTriangulate.TriangulationResult> sortedResults = new ArrayList<>(results.values());
        sortedResults.sort((a, b) -> Long.compare(b.lastUpdated, a.lastUpdated));

        List<PlayerTriangulate.TriangulationResult> toDisplay = sortedResults.size() > maxEntries
                ? sortedResults.subList(0, maxEntries)
                : sortedResults;

        double yOffset = 0;
        double maxWidth = 0;

        if (showBackground.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
        }

        if (toDisplay.isEmpty()) {
            String placeholder = "No triangulations yet";
            renderer.text(placeholder, x, y, Color.WHITE, true);
            maxWidth = renderer.textWidth(placeholder, true);
            yOffset = renderer.textHeight(true);
        } else {
            long now = System.currentTimeMillis();
            for (PlayerTriangulate.TriangulationResult result : toDisplay) {
                long ageSeconds = (now - result.lastUpdated) / 1000;
                String Component = String.format("%s: (%.1f, %.1f) [%ds ago]", result.playerName, result.wx, result.wz, ageSeconds);
                renderer.text(Component, x, y + yOffset, Color.WHITE, true);
                maxWidth = Math.max(maxWidth, renderer.textWidth(Component, true));
                yOffset += renderer.textHeight(true) + 2;
            }
        }

        setSize(maxWidth, yOffset);
    }
}
