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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.*;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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
    public IntProvider count = ConstantInt.of(1);
    public HeightProvider heightProvider;
    public PlacementContext placementCtx;
    public float rarity = 1.0F;
    public float discardOnAirChance;
    public int size;
    public Color color;
    public boolean scattered;

    private Ore(PlacedFeature feature, int step, int index, Setting<Boolean> active, Color color, PlacementContext placementCtx) {
        this.step = step;
        this.index = index;
        this.active = active;
        this.color = color;
        this.placementCtx = placementCtx;

        for (PlacementModifier modifier : feature.placement()) {
            if (modifier instanceof CountPlacement countPlacement) {
                this.count = ((CountPlacementModifierAccessor) (Object) countPlacement).getCount();
            } else if (modifier instanceof HeightRangePlacement heightRange) {
                this.heightProvider = ((HeightRangePlacementModifierAccessor) (Object) heightRange).getHeight();
            } else if (modifier instanceof RarityFilter rarityFilter) {
                this.rarity = ((RarityFilterPlacementModifierAccessor) (Object) rarityFilter).getChance();
            }
        }

        FeatureConfiguration featureConfiguration = feature.feature().value().config();
        if (featureConfiguration instanceof OreConfiguration oc) {
            this.discardOnAirChance = oc.discardChanceOnAirExposure;
            this.size = oc.size;
        } else {
            throw new IllegalStateException("Config for " + feature + " is not an OreConfiguration");
        }

        if (feature.feature().value().feature() instanceof net.minecraft.world.level.levelgen.feature.ScatteredOreFeature) {
            this.scattered = true;
        }
    }

    private static ResourceKey<PlacedFeature> oreKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("minecraft", name));
    }

    private static final List<String> OVERWORLD_ORES = List.of(
        "ore_coal_lower", "ore_coal_upper",
        "ore_iron_middle", "ore_iron_small", "ore_iron_upper",
        "ore_gold", "ore_gold_lower", "ore_gold_extra",
        "ore_redstone", "ore_redstone_lower",
        "ore_diamond", "ore_diamond_buried", "ore_diamond_large", "ore_diamond_medium",
        "ore_lapis", "ore_lapis_buried",
        "ore_copper", "ore_copper_large",
        "ore_emerald"
    );

    private static final List<String> NETHER_ORES = List.of(
        "ore_gold_nether", "ore_gold_deltas",
        "ore_quartz_nether", "ore_quartz_deltas",
        "ore_debris_small", "ore_ancient_debris_large"
    );

    public static Map<ResourceKey<Biome>, List<Ore>> getRegistry(Dimension dimension) {
        if (mc.level == null || mc.level.getServer() == null) return Collections.emptyMap();

        MinecraftServer server = mc.level.getServer();
        var registryAccess = mc.level.registryAccess();

        ResourceKey<LevelStem> stemKey = switch (dimension) {
            case Nether -> LevelStem.NETHER;
            case End -> LevelStem.END;
            default -> LevelStem.OVERWORLD;
        };

        ServerLevel serverLevel = server.getLevel(switch (dimension) {
            case Nether -> net.minecraft.world.level.Level.NETHER;
            case End -> net.minecraft.world.level.Level.END;
            default -> net.minecraft.world.level.Level.OVERWORLD;
        });

        if (serverLevel == null) return Collections.emptyMap();

        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        BiomeSource biomeSource = chunkGenerator.getBiomeSource();

        Set<Holder<Biome>> biomes = biomeSource.possibleBiomes();

        WorldGenerationContext heightContext = new WorldGenerationContext(chunkGenerator, serverLevel);
        PlacementContext placementCtx = new PlacementContext(serverLevel, chunkGenerator, Optional.empty());

        Map<PlacedFeature, Ore> featureToOre = new HashMap<>();

        List<String> oreNames = switch (dimension) {
            case Nether -> NETHER_ORES;
            default -> OVERWORLD_ORES;
        };

        int stepIndex = switch (dimension) {
            case Nether -> 7;
            default -> 6;
        };

        var featureRegistry = registryAccess.lookupOrThrow(Registries.PLACED_FEATURE);

        for (int i = 0; i < oreNames.size(); i++) {
            String oreName = oreNames.get(i);
            ResourceKey<PlacedFeature> key = oreKey(oreName);
            Optional<Holder.Reference<PlacedFeature>> holder = featureRegistry.get(key);
            if (holder.isEmpty()) continue;

            PlacedFeature feature = holder.get().value();
            Setting<Boolean> active = getActiveSetting(oreName);
            Color oreColor = getColor(oreName);

            if (active != null) {
                featureToOre.put(feature, new Ore(feature, stepIndex, i, active, oreColor, placementCtx));
            }
        }

        Map<ResourceKey<Biome>, List<Ore>> biomeOreMap = new HashMap<>();
        for (Holder<Biome> biome : biomes) {
            ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);
            if (biomeKey == null) continue;

            List<Ore> ores = new ArrayList<>();
            biomeOreMap.put(biomeKey, ores);

            var genSettings = biome.value().getGenerationSettings();
            List<HolderSet<PlacedFeature>> featureSets = genSettings.features();

            for (HolderSet<PlacedFeature> featureSet : featureSets) {
                for (Holder<PlacedFeature> featureHolder : featureSet) {
                    PlacedFeature feature = featureHolder.value();
                    Ore ore = featureToOre.get(feature);
                    if (ore != null) {
                        ores.add(ore);
                    }
                }
            }
        }

        return biomeOreMap;
    }

    private static Setting<Boolean> getActiveSetting(String oreName) {
        return switch (oreName) {
            case "ore_coal_lower", "ore_coal_upper" -> coal;
            case "ore_iron_middle", "ore_iron_small", "ore_iron_upper" -> iron;
            case "ore_gold", "ore_gold_lower", "ore_gold_extra", "ore_gold_nether", "ore_gold_deltas" -> gold;
            case "ore_redstone", "ore_redstone_lower" -> redstone;
            case "ore_diamond", "ore_diamond_buried", "ore_diamond_large", "ore_diamond_medium" -> diamond;
            case "ore_lapis", "ore_lapis_buried" -> lapis;
            case "ore_copper", "ore_copper_large" -> copper;
            case "ore_emerald" -> emerald;
            case "ore_quartz_nether", "ore_quartz_deltas" -> quartz;
            case "ore_debris_small", "ore_ancient_debris_large" -> debris;
            default -> null;
        };
    }

    private static Color getColor(String oreName) {
        return switch (oreName) {
            case "ore_coal_lower", "ore_coal_upper" -> new Color(47, 44, 54);
            case "ore_iron_middle", "ore_iron_small", "ore_iron_upper" -> new Color(236, 173, 119);
            case "ore_gold", "ore_gold_lower", "ore_gold_extra", "ore_gold_nether", "ore_gold_deltas" -> new Color(247, 229, 30);
            case "ore_redstone", "ore_redstone_lower" -> new Color(245, 7, 23);
            case "ore_diamond", "ore_diamond_buried", "ore_diamond_large", "ore_diamond_medium" -> new Color(33, 244, 255);
            case "ore_lapis", "ore_lapis_buried" -> new Color(8, 26, 189);
            case "ore_copper", "ore_copper_large" -> new Color(239, 151, 0);
            case "ore_emerald" -> new Color(27, 209, 45);
            case "ore_quartz_nether", "ore_quartz_deltas" -> new Color(205, 205, 205);
            case "ore_debris_small", "ore_ancient_debris_large" -> new Color(209, 27, 245);
            default -> new Color(255, 255, 255);
        };
    }
}
