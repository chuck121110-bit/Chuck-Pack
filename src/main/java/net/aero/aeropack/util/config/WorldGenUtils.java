/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.util.config;

import cubitect.Cubiomes;
import cubitect.Cubiomes.Pos;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.aero.aeropack.util.config.Seeds.Seed;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WorldGenUtils {

    private static final Logger LOG = LogManager.getLogger();

    private static final Map<Feature, List<Class<? extends Entity>>> FEATURE_ENTITIES = new HashMap<>() {{
        put(Feature.ocean_monument, Arrays.asList(ElderGuardian.class, Guardian.class));
        put(Feature.nether_fortress, Arrays.asList(Blaze.class, WitherSkeleton.class));
        put(Feature.mansion, Collections.singletonList(Evoker.class));
        put(Feature.slime_LevelChunk, Collections.singletonList(Slime.class));
        put(Feature.bastion_remnant, Collections.singletonList(PiglinBrute.class));
        put(Feature.end_city, Collections.singletonList(Shulker.class));
        put(Feature.village, Arrays.asList(Villager.class, IronGolem.class));
        put(Feature.mineshaft, Collections.singletonList(MinecartChest.class));
    }};

    public enum Feature {
        buried_treasure,
        mansion,
        stronghold,
        nether_fortress,
        ocean_monument,
        bastion_remnant,
        end_city,
        village,
        mineshaft,
        slime_LevelChunk,
        desert_pyramid
    }

    public static BlockPos locateFeature(Cubiomes.StructureType cFeature, BlockPos center) {
        Feature feature = switch (cFeature) {
            case Treasure -> Feature.buried_treasure;
            case Mansion -> Feature.mansion;
            case Stronghold -> Feature.stronghold;
            case Fortress -> Feature.nether_fortress;
            case Monument -> Feature.ocean_monument;
            case Bastion -> Feature.bastion_remnant;
            case End_City -> Feature.end_city;
            case Village -> Feature.village;
            case Mineshaft -> Feature.mineshaft;
            case Desert_Pyramid -> Feature.desert_pyramid;
            default -> null;
        };
        if (feature == null) return null;

        Seed seed = Seeds.get().getSeed();
        if (!isInDimension(getDimension(feature))) {
            return null;
        }
        if (seed != null) {
            try {
                BlockPos located = locateFeature(seed, feature, center);
                if (located != null) return located;
            } catch (Exception | Error ex) {
                LOG.error("Failed to locate feature via seed", ex);
            }
        }

        if (mc.player != null) {
            ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.isEmpty()) {
                stack = mc.player.getItemInHand(InteractionHand.OFF_HAND);
            }
            if (!stack.isEmpty()) {
                try {
                    BlockPos mapPos = locateFeatureMap(feature, stack);
                    if (mapPos != null) return mapPos;
                } catch (Exception | Error ex) {
                    LOG.error("Failed to locate feature via map", ex);
                }
            }
        }

        try {
            BlockPos entityPos = locateFeatureEntities(feature);
            if (entityPos != null) return entityPos;
        } catch (Exception | Error ex) {
            LOG.error("Failed to locate feature via entities", ex);
        }

        return null;
    }

    private static BlockPos locateFeatureMap(Feature feature, ItemStack stack) {
        if (!isValidMap(feature, stack)) return null;
        return getMapMarker(stack);
    }

    private static BlockPos locateFeatureEntities(Feature feature) {
        List<Class<? extends Entity>> entities = FEATURE_ENTITIES.get(feature);
        if (entities == null || mc.level == null) return null;

        for (Entity entity : mc.level.players()) {
            for (Class<? extends Entity> clazz : entities) {
                if (clazz.isInstance(entity)) {
                    return entity.blockPosition();
                }
            }
        }
        return null;
    }

    private static BlockPos locateFeature(Seed seed, Feature feature, BlockPos center) {
        Cubiomes.StructureType cType = switch (feature) {
            case buried_treasure -> Cubiomes.StructureType.Treasure;
            case mansion -> Cubiomes.StructureType.Mansion;
            case stronghold -> Cubiomes.StructureType.Stronghold;
            case nether_fortress -> Cubiomes.StructureType.Fortress;
            case ocean_monument -> Cubiomes.StructureType.Monument;
            case bastion_remnant -> Cubiomes.StructureType.Bastion;
            case end_city -> Cubiomes.StructureType.End_City;
            case village -> Cubiomes.StructureType.Village;
            case mineshaft -> Cubiomes.StructureType.Mineshaft;
            case slime_LevelChunk -> null;
            case desert_pyramid -> Cubiomes.StructureType.Desert_Pyramid;
        };
        if (cType == null) return null;
        Pos pos = Cubiomes.GetNearestStructure(cType, center.getX(), center.getZ(), seed.seed, seed.version);
        if (pos == null) return null;
        return new BlockPos(pos.x, 0, pos.z);
    }

    private static boolean isInDimension(meteordevelopment.meteorclient.utils.world.Dimension dimension) {
        return PlayerUtils.getDimension() == dimension;
    }

    private static meteordevelopment.meteorclient.utils.world.Dimension getDimension(Feature feature) {
        return switch (feature) {
            case nether_fortress, bastion_remnant -> meteordevelopment.meteorclient.utils.world.Dimension.Nether;
            case end_city -> meteordevelopment.meteorclient.utils.world.Dimension.End;
            default -> meteordevelopment.meteorclient.utils.world.Dimension.Overworld;
        };
    }

    private static boolean isValidMap(Feature feature, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!stack.getComponents().has(DataComponents.MAP_DECORATIONS)) return false;
        MapDecorations component = stack.get(DataComponents.MAP_DECORATIONS);
        if (component == null || component.decorations().isEmpty()) return false;
        String name = component.toString();
        if (!name.contains("translate")) return false;
        return switch (feature) {
            case buried_treasure -> name.contains("filled_map.buried_treasure");
            case ocean_monument -> name.contains("filled_map.monument");
            case mansion -> name.contains("filled_map.mansion");
            default -> false;
        };
    }

    private static BlockPos getMapMarker(ItemStack stack) {
        if (!stack.getComponents().has(DataComponents.MAP_DECORATIONS)) return null;
        MapDecorations component = stack.get(DataComponents.MAP_DECORATIONS);
        if (component == null || component.decorations().isEmpty()) return null;
        MapDecorations.Entry decoration = component.decorations().values().iterator().next();
        return new BlockPos((int) decoration.x(), 0, (int) decoration.z());
    }
}
