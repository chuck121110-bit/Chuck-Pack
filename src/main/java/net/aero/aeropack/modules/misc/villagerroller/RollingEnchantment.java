package net.aero.aeropack.modules.misc.villagerroller;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.enchantment.Enchantment;

public class RollingEnchantment implements ISerializable<RollingEnchantment> {
    private Identifier enchantment;
    private int minLevel;
    private int maxCost;
    private boolean enabled;

    public RollingEnchantment(Identifier enchantment, int minLevel, int maxCost, boolean enabled) {
        this.enchantment = enchantment;
        this.minLevel = minLevel;
        this.maxCost = maxCost;
        this.enabled = enabled;
    }

    public RollingEnchantment() {
        this.enchantment = Identifier.of("minecraft", "protection");
        this.minLevel = 0;
        this.maxCost = 0;
        this.enabled = false;
    }

    public Identifier getEnchantment() { return enchantment; }
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    public int getMaxCost() { return maxCost; }
    public void setMaxCost(int maxCost) { this.maxCost = maxCost; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("enchantment", enchantment.toString());
        tag.putInt("minLevel", minLevel);
        tag.putInt("maxCost", maxCost);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    @Override
    public RollingEnchantment fromTag(NbtCompound tag) {
        this.enchantment = Identifier.of(tag.getString("enchantment", ""));
        this.minLevel = tag.getInt("minLevel", 1);
        this.maxCost = tag.getInt("maxCost", 64);
        this.enabled = tag.getBoolean("enabled", true);
        return this;
    }

    public static int getMinimumPrice(RegistryEntry<Enchantment> e) {
        if (e == null) return 0;
        boolean isTreasure = e.isIn(net.minecraft.registry.tag.EnchantmentTags.TREASURE);
        int maxLevel = e.value().getMaxLevel();
        return isTreasure ? (2 + 3 * maxLevel) * 2 : 2 + 3 * maxLevel;
    }
}
