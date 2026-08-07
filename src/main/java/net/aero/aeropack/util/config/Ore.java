/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.util.config;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.aero.aeropack.mixin.CountPlacementModifierAccessor;
import net.aero.aeropack.mixin.HeightRangePlacementModifierAccessor;
import net.aero.aeropack.mixin.RarityFilterPlacementModifierAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.*;

public class Ore {
    private static final Setting<Boolean> coal = new BoolSetting.Builder().name("Coal").build();
    private static final Setting<Boolean> iron = new BoolSetting.Builder().name("Iron").build();
    private static final Setting<Boolean> gold = new BoolSetting.Builder().name("Gold").build();
    private static final Setting<Boolean> redstone = new BoolSetting.Builder().name("Redstone").build();
    private static final Setting<Boolean> diamond = new BoolSetting.Builder().name("Diamond").build();
    private static final Setting<Boolean> lapis = new BoolSetting.Builder().name("Lapis").build();
    private static final Setting<Boolean> copper = new BoolSetting.Builder().name("Copper").build();
    private static final Setting<Boolean> emerald = new BoolSetting.Builder().name("Emerald").build();
    private static final Setting<Boolean> quartz = new BoolSetting.Builder().name("Quartz").build();
    private static final Setting<Boolean> debris = new BoolSetting.Builder().name("Ancient Debris").build();

    public static final List<Setting<Boolean>> oreSettings = List.of(
        coal, iron, gold, redstone, diamond, lapis, copper, emerald, quartz, debris
    );

    public int step;
    public int index;
    public Setting<Boolean> active;
    public IntProvider count = ConstantInt.create(1);
    public HeightProvider heightProvider;
    public PlacementContext PlacementContext;
    public float rarity = 1.0F;
    public float discardOnAirChance;
    public int size;
    public Color color;
    public boolean scattered;

    private Ore(PlacedFeature feature, int step, int index, Setting<Boolean> active, Color color, PlacementContext PlacementContext) {
        this.step = step;
        this.index = index;
        this.active = active;
        this.color = color;
        this.PlacementContext = PlacementContext;

        for (PlacementModifier modifier : feature.placementModifiers()) {
            if (modifier instanceof CountPlacement countPlacement) {
                this.count = ((CountPlacementModifierAccessor) (Object) countPlacement).getCount();
            } else if (modifier instanceof HeightRangePlacement heightRange) {
                this.heightProvider = ((HeightRangePlacementModifierAccessor) (Object) heightRange).getHeight();
            } else if (modifier instanceof RarityFilter rarityFilter) {
                this.rarity = ((RarityFilterPlacementModifierAccessor) (Object) rarityFilter).getChance();
            }
        }

        FeatureConfiguration FeatureConfiguration = feature.feature().value().config();
        if (FeatureConfiguration instanceof OreConfiguration OreConfiguration) {
            this.discardOnAirChance = OreConfiguration.discardOnAirChance;
            this.size = OreConfiguration.size;
        } else {
            throw new IllegalStateException("Config for " + feature + " is not an OreConfiguration");
        }

        if (feature.feature().value().feature() instanceof net.minecraft.world.level.levelgen.feature.ScatteredOreFeature) {
            this.scattered = true;
        }
    }

    public static Map<ResourceKey<Biome>, List<Ore>> getRegistry(Dimension dimension) {
        // TODO: Port to 26.1.2 API — HolderGetter.WrapperLookup, HolderGetter.Impl,
        // PlacedFeature.IndexedFeatures, WorldPresets, DimensionType access all changed.
        // Previous implementation used BuiltinRegistries.createWrapperLookup(), etc.
        throw new UnsupportedOperationException("Ore.getRegistry() not yet ported to MC 26.1.2");
    }

    private static void registerOre(
        Map<PlacedFeature, Ore> map,
        List<?> indexer,
        Object oreRegistry,
        ResourceKey<PlacedFeature> oreKey,
        int genStep,
        Setting<Boolean> active,
        Color color,
        PlacementContext PlacementContext
    ) {
        // TODO: Port to 26.1.2 API
    }
}
