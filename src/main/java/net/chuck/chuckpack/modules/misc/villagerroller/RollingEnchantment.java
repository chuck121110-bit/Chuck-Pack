package net.chuck.chuckpack.modules.misc.villagerroller;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

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
        this.enchantment = Identifier.fromNamespaceAndPath("minecraft", "protection");
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
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("enchantment", enchantment.toString());
        tag.putInt("minLevel", minLevel);
        tag.putInt("maxCost", maxCost);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    @Override
    public RollingEnchantment fromTag(CompoundTag tag) {
        this.enchantment = Identifier.tryParse(tag.getStringOr("enchantment", ""));
        this.minLevel = tag.getIntOr("minLevel", 1);
        this.maxCost = tag.getIntOr("maxCost", 64);
        this.enabled = tag.getBooleanOr("enabled", true);
        return this;
    }

    public static int getMinimumPrice(Holder<Enchantment> e) {
        if (e == null) return 0;
        boolean isDoublePrice = e.is(EnchantmentTags.DOUBLE_TRADE_PRICE);
        int maxLevel = e.value().getMaxLevel();
        return isDoublePrice ? (2 + 3 * maxLevel) * 2 : 2 + 3 * maxLevel;
    }
}
