package net.aero.aeropack.util.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AeroConfig {
    public static void save(String folderName, String fileName, NbtCompound data) {
        try {
            Path basePath = FabricLoader.getInstance().getGameDir().resolve("aeropack");
            Path filePath = basePath.resolve(sanitize(folderName)).resolve(fileName + ".nbt");
            Files.createDirectories(filePath.getParent());
            NbtIo.write(data, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NbtCompound load(String folderName, String fileName) {
        try {
            Path basePath = FabricLoader.getInstance().getGameDir().resolve("aeropack");
            Path filePath = basePath.resolve(sanitize(folderName)).resolve(fileName + ".nbt");
            if (!Files.exists(filePath)) return new NbtCompound();
            return NbtIo.read(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return new NbtCompound();
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
