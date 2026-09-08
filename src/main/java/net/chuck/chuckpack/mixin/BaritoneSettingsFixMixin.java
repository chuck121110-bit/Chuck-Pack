package net.chuck.chuckpack.mixin;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fixes two meteor<->baritone sync bugs that made Colors and Block/Item Lists
// in the PathManager tab never persist:
//
// 1. Colors: meteor captures the color ONCE when wrappers are created and its
//    onModuleActivated writes that stale value back on every tab open, wiping
//    live edits (which then get saved over). Snapshot live colors before the
//    tab's onActivated sync and restore them after.
//
// 2. Block/Item lists: meteor's list screen mutates the shared list object in
//    place, so baritone's value stays == its defaultValue and
//    SettingsUtil.save() skips it forever. Re-assign fresh copies so the
//    values differ from defaults and actually get written.
@Mixin(targets = "meteordevelopment.meteorclient.gui.tabs.builtin.PathManagerTab$PathManagerScreen", remap = false)
public class BaritoneSettingsFixMixin {

    @Unique
    private static final Map<String, int[]> chuckpack$colorSnapshot = new HashMap<>();

    @Inject(method = "<init>(Lmeteordevelopment/meteorclient/gui/GuiTheme;Lmeteordevelopment/meteorclient/gui/tabs/Tab;)V", at = @At("HEAD"))
    private void chuckpack$snapshotLiveColors(CallbackInfo ci) {
        chuckpack$colorSnapshot.clear();
        for (Settings.Setting<?> s : chuckpack$baritoneSettings()) {
            try {
                if (s.value instanceof Color c) {
                    chuckpack$colorSnapshot.put(s.getName(), new int[]{c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()});
                }
            } catch (Throwable ignored) {}
        }
    }

    @Inject(method = "<init>(Lmeteordevelopment/meteorclient/gui/GuiTheme;Lmeteordevelopment/meteorclient/gui/tabs/Tab;)V", at = @At("TAIL"))
    private void chuckpack$restoreAndResync(CallbackInfo ci) {
        // Restore live colors clobbered by meteor's stale onModuleActivated sync.
        for (Settings.Setting<?> s : chuckpack$baritoneSettings()) {
            try {
                int[] rgba = chuckpack$colorSnapshot.get(s.getName());
                if (rgba != null) {
                    ((Settings.Setting<Color>) (Object) s).value = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
                }
            } catch (Throwable ignored) {}
        }
        // Re-sync meteor wrappers FROM live with fresh objects (also un-aliases shared lists).
        try {
            chuckpack$resyncWrappersFromLive();
        } catch (Throwable ignored) {}
    }

    @Inject(method = "onClosed()V", at = @At("HEAD"))
    private void chuckpack$syncBeforeSave(CallbackInfo ci) {
        try {
            chuckpack$pushWrappersToLive();
        } catch (Throwable ignored) {}
    }

    @Inject(method = "onClosed()V", at = @At("TAIL"))
    private void chuckpack$logAfterSave(CallbackInfo ci) {
        try {
            chuckpack$forcePersistColorsAndLists();
            int colors = 0, lists = 0;
            for (Settings.Setting<?> s : chuckpack$baritoneSettings()) {
                if (s.value instanceof Color) colors++;
                else if (s.value instanceof List) lists++;
            }
            java.nio.file.Path f = net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath()
                .resolve("baritone").resolve("settings.txt");
            long lines = 0;
            try {
                lines = java.nio.file.Files.readAllLines(f).size();
            } catch (Throwable ignored) {}
            System.out.println("[ChuckPack] Baritone tab closed: live has " + colors + " colors, " + lists
                + " lists; settings.txt=" + f + " (" + lines + " lines)");
        } catch (Throwable ignored) {}
    }

    // Baritone only writes settings whose value != default, and shared-list
    // aliasing can defeat that check. Force every color/list setting line into
    // settings.txt (replace or append) using baritone's own serialization.
    @Unique
    private static void chuckpack$forcePersistColorsAndLists() {
        try {
            java.nio.file.Path f = net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath()
                .resolve("baritone").resolve("settings.txt");
            List<String> lines = new ArrayList<>();
            try {
                lines.addAll(java.nio.file.Files.readAllLines(f));
            } catch (Throwable ignored) {}
            Map<String, List<Integer>> indicesByName = new HashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;
                int sp = line.indexOf(' ');
                if (sp > 0) indicesByName.computeIfAbsent(line.substring(0, sp).toLowerCase(), k -> new ArrayList<>()).add(i);
            }
            // Collect first, then apply: replacements use original indices, removals last.
            List<Settings.Setting<?>> targets = new ArrayList<>();
            for (Settings.Setting<?> s : chuckpack$baritoneSettings()) {
                if (s.value instanceof Color || s.value instanceof List) targets.add(s);
            }
            java.util.Set<Integer> removeLines = new java.util.HashSet<>();
            Map<Integer, String> replaceLines = new HashMap<>();
            for (Settings.Setting<?> s : targets) {
                List<Integer> idxs = indicesByName.getOrDefault(s.getName().toLowerCase(), List.of());
                if (s.value instanceof List<?> l && l.isEmpty()) {
                    // An emptied list must vanish, not persist as an unparsable empty value.
                    removeLines.addAll(idxs);
                    continue;
                }
                String serialized;
                try {
                    serialized = baritone.api.utils.SettingsUtil.settingToString(s);
                } catch (Throwable ignored) {
                    continue;
                }
                if (!idxs.isEmpty()) {
                    replaceLines.put(idxs.get(0), serialized);
                    removeLines.addAll(idxs.subList(1, idxs.size()));
                } else {
                    lines.add(serialized);
                }
            }
            for (Map.Entry<Integer, String> e : replaceLines.entrySet()) lines.set(e.getKey(), e.getValue());
            List<String> kept = new ArrayList<>(lines.size());
            for (int i = 0; i < lines.size(); i++) {
                if (!removeLines.contains(i)) kept.add(lines.get(i));
            }
            lines = kept;
            java.nio.file.Files.createDirectories(f.getParent());
            java.nio.file.Files.write(f, lines);
        } catch (Throwable ignored) {}
    }

    @Unique
    private static List<Settings.Setting<?>> chuckpack$baritoneSettings() {
        List<Settings.Setting<?>> out = new ArrayList<>();
        Field[] fields;
        try {
            fields = BaritoneAPI.getSettings().getClass().getDeclaredFields();
        } catch (Throwable ignored) {
            return out;
        }
        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers())) continue;
                Object obj = field.get(BaritoneAPI.getSettings());
                if (obj instanceof Settings.Setting<?> s) out.add(s);
            } catch (Throwable ignored) {}
        }
        return out;
    }

    @Unique
    private static Map<String, Settings.Setting<?>> chuckpack$baritoneByName() {
        Map<String, Settings.Setting<?>> map = new HashMap<>();
        for (Settings.Setting<?> s : chuckpack$baritoneSettings()) map.put(s.getName(), s);
        return map;
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void chuckpack$resyncWrappersFromLive() {
        Map<String, Settings.Setting<?>> live = chuckpack$baritoneByName();
        meteordevelopment.meteorclient.settings.Settings wrappers;
        try {
            wrappers = PathManagers.get().getSettings().get();
        } catch (Throwable ignored) {
            return;
        }
        for (SettingGroup group : wrappers.groups) {
            for (Setting<?> w : group) {
                Settings.Setting<?> b = live.get(w.name);
                if (b == null) continue;
                try {
                    if (w.get() instanceof SettingColor && b.value instanceof Color c) {
                        ((Setting<SettingColor>) (Object) w).set(new SettingColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
                    } else if (w.get() instanceof List && b.value instanceof List<?> bl) {
                        ((Setting<List>) (Object) w).set(new ArrayList<>(bl));
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void chuckpack$pushWrappersToLive() {
        Map<String, Settings.Setting<?>> live = chuckpack$baritoneByName();
        meteordevelopment.meteorclient.settings.Settings wrappers;
        try {
            wrappers = PathManagers.get().getSettings().get();
        } catch (Throwable ignored) {
            return;
        }
        for (SettingGroup group : wrappers.groups) {
            for (Setting<?> w : group) {
                Settings.Setting<?> b = live.get(w.name);
                if (b == null) continue;
                try {
                    if (w.get() instanceof SettingColor sc) {
                        ((Settings.Setting<Color>) (Object) b).value = new Color(sc.r, sc.g, sc.b, sc.a);
                    } else if (w.get() instanceof List<?> wl) {
                        ((Settings.Setting<List>) (Object) b).value = new ArrayList<>(wl);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }
}
