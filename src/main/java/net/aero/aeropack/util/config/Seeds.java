/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.util.config;

import cubitect.Cubiomes;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import java.util.HashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Seeds extends System<Seeds> {
    private static final Seeds INSTANCE = new Seeds();

    public final HashMap<String, Seed> seeds = new HashMap<>();

    private Seeds() {
        super("seeds");
        init();
        load(MeteorClient.FOLDER);
    }

    public static Seeds get() {
        return INSTANCE;
    }

    public Seed getSeed() {
        if (mc == null) return null;

        if (mc.hasSingleplayerServer()) {
            try {
                if (mc.getSingleplayerServer() != null) {
                    try {
                        var level = mc.getSingleplayerServer().overworld();
                        if (level != null) {
                            return new Seed(level.getSeed(), resolveCubiomesVersion());
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Throwable ignored) {}
            return null;
        }

        String worldName = Utils.getWorldName();
        if (worldName != null) {
            return seeds.get(worldName);
        }

        return null;
    }

    public void setSeed(String rawSeed) {
        if (mc == null || mc.hasSingleplayerServer()) return;

        ServerData server = mc.getCurrentServer();
        String verStr = server != null && server.version != null ? server.version.getString() : "unknown";
        setSeed(rawSeed, resolveCubiomesVersion(verStr));
    }

    public void setSeed(String rawSeed, Cubiomes.MCVersion version) {
        if (mc == null || mc.hasSingleplayerServer()) return;

        String worldName = Utils.getWorldName();
        if (worldName == null) return;

        long numericSeed = parseSeed(rawSeed);
        seeds.put(worldName, new Seed(numericSeed, version));
        save();
        MeteorClient.EVENT_BUS.post(SeedChangedEvent.get(numericSeed));
    }

    public void removeSeed(String worldName) {
        if (worldName == null) return;
        if (seeds.remove(worldName) != null) {
            save();
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        seeds.forEach((key, seed) -> {
            if (seed != null) {
                tag.put(key, seed.toTag());
            }
        });
        return tag;
    }

    @Override
    public Seeds fromTag(CompoundTag tag) {
        for (String key : tag.keySet()) {
            tag.getCompound(key).ifPresent(nbt -> seeds.put(key, Seed.fromTag(nbt)));
        }
        return this;
    }

    private static long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException ignored) {
            return seed.strip().hashCode();
        }
    }

    public static final class Seed {
        public final long seed;
        public final Cubiomes.MCVersion version;

        public Seed(long seed, Cubiomes.MCVersion version) {
            this.seed = seed;
            this.version = version == null ? resolveCubiomesVersion() : version;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("seed", seed);
            tag.putString("version", version.name());
            return tag;
        }

        public static Seed fromTag(CompoundTag tag) {
            long storedSeed = tag.getLong("seed").orElse(0L);
            String versionName = tag.getString("version").orElse("");
            Cubiomes.MCVersion storedVersion = parseCubiomesVersion(versionName);
            return new Seed(storedSeed, storedVersion);
        }

        public Component toText() {
            MutableComponent component = Component.literal(String.format("[%s%s%s] (%s)",
                ChatFormatting.GREEN,
                Long.toString(seed),
                ChatFormatting.WHITE,
                version.name()
            ));

            component.setStyle(component.getStyle()
                .withClickEvent(new ClickEvent.CopyToClipboard(Long.toString(seed)))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Copy to clipboard"))));

            return component;
        }
    }

    public static final class SeedChangedEvent {
        private static final SeedChangedEvent INSTANCE = new SeedChangedEvent();

        public long seed;

        public static SeedChangedEvent get(long seed) {
            INSTANCE.seed = seed;
            return INSTANCE;
        }
    }

    private static Cubiomes.MCVersion resolveCubiomesVersion() {
        return Cubiomes.MCVersion.MC_1_21_WD;
    }

    private static Cubiomes.MCVersion resolveCubiomesVersion(String gameVer) {
        return Cubiomes.MCVersion.MC_1_21_WD;
    }

    private static Cubiomes.MCVersion parseCubiomesVersion(String input) {
        if (input == null || input.isEmpty()) return resolveCubiomesVersion();
        String norm = input.trim().toUpperCase();
        try {
            return Cubiomes.MCVersion.valueOf(norm);
        } catch (IllegalArgumentException ignored) {}
        return resolveCubiomesVersion();
    }

    public static Cubiomes.MCVersion resolveForPublic(String input) {
        return parseCubiomesVersion(input);
    }
}
