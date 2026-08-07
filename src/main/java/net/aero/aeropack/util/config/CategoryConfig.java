package net.aero.aeropack.util.config;

import net.minecraft.nbt.NbtCompound;

public class CategoryConfig {
    private static final String FOLDER = "config";
    private static final String FILE = "aeropack_category";

    private static boolean separateCategory = false;

    public static void load() {
        NbtCompound data = AeroConfig.load(FOLDER, FILE);
        separateCategory = data.getBoolean("separateCategory", false);
    }

    public static void save() {
        NbtCompound data = new NbtCompound();
        data.putBoolean("separateCategory", separateCategory);
        AeroConfig.save(FOLDER, FILE, data);
    }

    public static boolean isSeparateCategory() {
        return separateCategory;
    }

    public static void setSeparateCategory(boolean value) {
        separateCategory = value;
        save();
    }
}
