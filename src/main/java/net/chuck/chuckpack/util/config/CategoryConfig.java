package net.chuck.chuckpack.util.config;

import net.minecraft.nbt.CompoundTag;

public class CategoryConfig {
    private static final String FOLDER = "config";
    private static final String FILE = "chuckpack_category";

    private static boolean separateCategory = false;

    public static void load() {
        CompoundTag data = ChuckConfig.load(FOLDER, FILE);
        separateCategory = data.getBooleanOr("separateCategory", false);
    }

    public static void save() {
        CompoundTag data = new CompoundTag();
        data.putBoolean("separateCategory", separateCategory);
        ChuckConfig.save(FOLDER, FILE, data);
    }

    public static boolean isSeparateCategory() {
        return separateCategory;
    }

    public static void setSeparateCategory(boolean value) {
        separateCategory = value;
        save();
    }
}
