package net.aero.mapintegration.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("mapintegration.json");

    private static ModConfig INSTANCE = new ModConfig();

    public boolean skipDeleteConfirmation = true;
    public boolean removeTeleport = true;
    public boolean removeExport = false;
    public boolean removeSettings = false;
    public boolean removeShare = true;
    public boolean improveCoordinateDisplay = true;
    public boolean highlightChatCoordinates = true;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                net.aero.mapintegration.MapIntegrationMod.LOG.error("Failed to load config", e);
            }
        }
        save();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            net.aero.mapintegration.MapIntegrationMod.LOG.error("Failed to save config", e);
        }
    }
}
