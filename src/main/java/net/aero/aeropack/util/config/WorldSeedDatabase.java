package net.aero.aeropack.util.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class WorldSeedDatabase {
    private static final String FOLDER = "world_seeds";
    private static final String FILE = "seeds";

    public static class SeedEntry {
        public String address;
        public String worldName;
        public String seed;

        public SeedEntry(String address, String worldName, String seed) {
            this.address = address != null ? address : "";
            this.worldName = worldName != null ? worldName : "";
            this.seed = seed != null ? seed : "0";
        }

        public NbtCompound toTag() {
            NbtCompound tag = new NbtCompound();
            tag.putString("address", address);
            tag.putString("worldName", worldName);
            tag.putString("seed", seed);
            return tag;
        }

        public static SeedEntry fromTag(NbtCompound tag) {
            return new SeedEntry(
                tag.getString("address", ""),
                tag.getString("worldName", ""),
                tag.getString("seed", "0")
            );
        }

        public String getLabel() {
            String label = !address.isEmpty() ? address : (!worldName.isEmpty() ? worldName : "Unknown");
            return label + " \u2192 " + seed;
        }
    }

    public static List<SeedEntry> load() {
        NbtCompound data = AeroConfig.load(FOLDER, FILE);
        List<SeedEntry> entries = new ArrayList<>();
        NbtList list = data.getListOrEmpty("entries");
        for (int i = 0; i < list.size(); i++) {
            NbtElement element = list.get(i);
            if (element instanceof NbtCompound compound) {
                entries.add(SeedEntry.fromTag(compound));
            }
        }
        return entries;
    }

    public static void save(List<SeedEntry> entries) {
        NbtCompound data = new NbtCompound();
        NbtList list = new NbtList();
        for (SeedEntry entry : entries) {
            list.add(entry.toTag());
        }
        data.put("entries", list);
        AeroConfig.save(FOLDER, FILE, data);
    }

    public static String getSeedForAddress(String address) {
        if (address == null || address.isEmpty()) return null;
        for (SeedEntry entry : load()) {
            if (entry.address.equalsIgnoreCase(address)) {
                return entry.seed;
            }
        }
        return null;
    }
}
