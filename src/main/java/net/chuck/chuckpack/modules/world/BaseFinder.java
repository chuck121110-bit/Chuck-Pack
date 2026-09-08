package net.chuck.chuckpack.modules.world;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/*
    This BaseFinder was made from the newchunks code,
    Newchunks was Ported from: https://github.com/BleachDrinker420/BleachHack/blob/master/BleachHack-Fabric-1.16/src/main/java/bleach/hack/module/mods/NewChunks.java
    Ported for meteor-rejects
    updated and modified by etianll :D
*/
public class BaseFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDetectors = settings.createGroup("Block Detectors");
    private final SettingGroup sgEDetectors = settings.createGroup("Entity Detectors");
    private final SettingGroup sglists = settings.createGroup("Blocks To Check For");
    private final SettingGroup sgCdata = settings.createGroup("Saved Base Data");
    private final SettingGroup sgcacheCdata = settings.createGroup("Cached Base Data");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup locationLogs = settings.createGroup("Location Logs");

    // general
    private final Setting<Boolean> chatFeedback = sgGeneral.add(new BoolSetting.Builder()
            .name("Chat feedback")
            .description("Displays info for you.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> baseNotifier = sgGeneral.add(new BoolSetting.Builder()
            .name("Base Notifier")
            .description("When a base is found, sends a clickable chat message with Pulse and View Triggers buttons.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> autoChunkDelete = sgGeneral.add(new BoolSetting.Builder()
            .name("Auto delete flagged chunks")
            .description("Automatically stops flagging a chunk as a base once you have spent enough time near it.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Integer> chunkDeleteDelay = sgGeneral.add(new IntSetting.Builder()
            .name("Delete time")
            .description("How many seconds you must continuously stay within the delete radius of a flagged chunk before it is removed.")
            .min(1)
            .sliderRange(1, 60)
            .defaultValue(2)
            .visible(autoChunkDelete::get)
            .build()
    );
    private final Setting<Integer> chunkDeleteRadius = sgGeneral.add(new IntSetting.Builder()
            .name("Delete radius")
            .description("How close in chunks you need to be to a flagged chunk for its delete timer to count.")
            .min(0)
            .sliderRange(0, 20)
            .defaultValue(1)
            .visible(autoChunkDelete::get)
            .build()
    );
    private final Setting<Boolean> displaycoords = sgGeneral.add(new BoolSetting.Builder()
            .name("DisplayCoords")
            .description("Displays coords of bases in chat.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
            .name("Detection Y Minimum OffSet")
            .description("Scans blocks above or at this this many blocks from minimum build limit.")
            .min(0)
            .sliderRange(0,319)
            .defaultValue(0)
            .build());
    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
            .name("Detection Y Maximum OffSet")
            .description("Scans blocks below or at this this many blocks from maximum build limit.")
            .min(0)
            .sliderRange(0,319)
            .defaultValue(0)
            .build());
    private final Setting<Integer> minSpawnDistanceOverworld = sgGeneral.add(new IntSetting.Builder()
            .name("Min Spawn Distance Overworld (Chunks)")
            .description("Chunks closer than this to Level spawn in the Overworld will be completely ignored by all detectors (blocks, entities, sky builds, etc). Helps avoid false positives near spawn.")
            .min(0)
            .sliderRange(0, 5000)
            .defaultValue(0)
            .build());
    private final Setting<Integer> minSpawnDistanceNether = sgGeneral.add(new IntSetting.Builder()
            .name("Min Spawn Distance Nether (Chunks)")
            .description("Chunks closer than this to Level spawn in the Nether will be completely ignored by all detectors. Nether coordinates are compressed 8:1 versus the Overworld, so this should usually be a much smaller value - otherwise nether roof scanning can get blocked entirely.")
            .min(0)
            .sliderRange(0, 5000)
            .defaultValue(0)
            .build());
    private final Setting<Boolean> signFinder = sgDetectors.add(new BoolSetting.Builder()
            .name("Written Sign Finder")
            .description("Finds signs that have Component on them because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> portalFinder = sgDetectors.add(new BoolSetting.Builder()
            .name("Open Portal Finder")
            .description("Finds End/Nether portals that are open because they are usually not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> bubblesFinder = sgDetectors.add(new BoolSetting.Builder()
            .name("Bubble Column Finder")
            .description("Finds bubble column blocks made by soul sand because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> skybuildfind = sgDetectors.add(new BoolSetting.Builder()
            .name("Sky Build Finder")
            .description("If Blocks higher than terrain can naturally generate, flag LevelChunk as possible build.")
            .defaultValue(true)
            .build());
    private final Setting<Integer> skybuildint = sgDetectors.add(new IntSetting.Builder()
            .name("Sky Build Y Threshold")
            .description("If Blocks higher than this Y value, flag chunk as possible build.")
            .min(-64)
            .sliderRange(-64, 319)
            .defaultValue(260)
            .visible(skybuildfind::get)
            .build());
    private final Setting<Boolean> bedrockfind = sgDetectors.add(new BoolSetting.Builder()
            .name("Bedrock Finder")
            .description("If Bedrock Blocks higher than they can naturally generate in the Overworld or Nether, flag LevelChunk as possible build.")
            .defaultValue(true)
            .build());
    private final Setting<Integer> bedrockint = sgDetectors.add(new IntSetting.Builder()
            .name("Bedrock Y Threshold")
            .description("If bedrock higher than this many blocks above minimum build limit, flag LevelChunk as possible build.")
            .min(-64)
            .sliderRange(-64, 384)
            .defaultValue(4)
            .visible(bedrockfind::get)
            .build());
    private final Setting<Boolean> spawner = sgDetectors.add(new BoolSetting.Builder()
            .name("Unnatural Spawner Finder")
            .description("If a spawner doesn't have the proper natural companion blocks with it in the LevelChunk, flag as possible build.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> roofDetector = sgDetectors.add(new BoolSetting.Builder()
            .name("Nether Roof Build Finder")
            .description("If anything but mushrooms on the nether roof, flag as possible build.")
            .defaultValue(true)
            .build());
    private final Setting<Integer> entityScanDelay = sgEDetectors.add(new IntSetting.Builder()
            .name("Entity Scan Tick Delay")
            .description("Delay between scanning all the entities within render distance.")
            .min(0)
            .sliderRange(0,300)
            .defaultValue(20)
            .build());
    private final Setting<Boolean> entityChatFeedback = sgEDetectors.add(new BoolSetting.Builder()
            .name("Entity Chat Feedback")
            .description("Sends a chat message whenever an entity detector finds something. Turn off to stop chat messages from Item Frame, Ender Pearl, Villager, NameTag, Boat, and Entity Cluster detections.")
            .defaultValue(false)
            .build());
    private final Setting<Boolean> frameFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("Item Frame Finder")
            .description("Finds item frames that do not contain an elytra because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> pearlFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("Ender Pearl Finder")
            .description("Finds ender pearls entities because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> nameFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("NameTag Finder")
            .description("Finds mobs with a nametag because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> villagerFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("Villager Finder")
            .description("Finds villagers with a level greater than 1 because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> boatFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("Boat Finder")
            .description("Finds villagers with a level greater than 1 because they are not natural.")
            .defaultValue(true)
            .build());
    private final Setting<Boolean> entityClusterFinder = sgEDetectors.add(new BoolSetting.Builder()
            .name("Entity Cluster Finder")
            .description("Finds clusters of entities per LevelChunk.")
            .defaultValue(true)
            .build());
    private final Setting<Set<EntityType<?>>> entitieslist = sgEDetectors.add(new EntityTypeListSetting.Builder()
            .name("Entities")
            .description("Select specific entities.")
            .defaultValue(getDefaultCreatures())
            .build()
    );
    private Set<EntityType<?>> getDefaultCreatures() {
        Set<EntityType<?>> creatures = new HashSet<>();
        BuiltInRegistries.ENTITY_TYPE.stream().forEach(entityType -> {
            if (entityType.getCategory() == MobCategory.CREATURE) {
                creatures.add(entityType);
            }
        });
        return creatures;
    }
    private final Setting<Integer> animalsFoundThreshold = sgEDetectors.add(new IntSetting.Builder()
            .name("Entity Cluster Threshold")
            .description("Once this many entities are found in a LevelChunk trigger it as being a base.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(14)
            .build());
    private final Setting<Integer> bsefndtickdelay = sgGeneral.add(new IntSetting.Builder()
            .name("Base Found Message Tick Delay")
            .description("Delays the allowance of Base Found messages to reduce spam.")
            .min(0)
            .sliderRange(0,300)
            .defaultValue(5)
            .build());
    private final Setting<Boolean> list1Activar = sglists.add(new BoolSetting.Builder()
            .name("List #1 Activate")
            .description("Activates checks for List #1")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks1 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #1 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(
                    
                    Blocks.CRAFTER, Blocks.SPRUCE_SAPLING, Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.CHERRY_SAPLING,
                    Blocks.BAMBOO_SAPLING, Blocks.CHERRY_BUTTON, Blocks.CHERRY_DOOR, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE, Blocks.CHERRY_PLANKS,
                    Blocks.CHERRY_PRESSURE_PLATE, Blocks.CHERRY_STAIRS, Blocks.CHERRY_WOOD, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_SLAB, Blocks.MANGROVE_PLANKS,
                    Blocks.MANGROVE_BUTTON, Blocks.MANGROVE_DOOR, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB,
                    Blocks.MANGROVE_TRAPDOOR, Blocks.BIRCH_DOOR, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON,
                    Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_BUTTON, Blocks.BAMBOO_DOOR, Blocks.BAMBOO_FENCE,
                    Blocks.BAMBOO_FENCE_GATE, Blocks.BAMBOO_MOSAIC, Blocks.BAMBOO_MOSAIC_SLAB, Blocks.BAMBOO_MOSAIC_STAIRS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_PRESSURE_PLATE,
                    Blocks.BAMBOO_SLAB, Blocks.BAMBOO_STAIRS, Blocks.BAMBOO_TRAPDOOR, Blocks.CHISELED_BOOKSHELF, Blocks.CONCRETE.black(), Blocks.CONCRETE.blue(),
                    Blocks.CONCRETE.cyan(), Blocks.CONCRETE.brown(), Blocks.CONCRETE.orange(), Blocks.CONCRETE.magenta(), Blocks.CONCRETE.lightBlue(), Blocks.CONCRETE.yellow(),
                    Blocks.CONCRETE.lime(), Blocks.CONCRETE.pink(), Blocks.CONCRETE.gray(), Blocks.CONCRETE.lightGray(), Blocks.CONCRETE.purple(), Blocks.CONCRETE.green(),
                    Blocks.CONCRETE_POWDER.black(), Blocks.CONCRETE_POWDER.blue(), Blocks.CONCRETE_POWDER.cyan(), Blocks.CONCRETE_POWDER.brown(), Blocks.CONCRETE_POWDER.white(), Blocks.CONCRETE_POWDER.orange(),
                    Blocks.CONCRETE_POWDER.magenta(), Blocks.CONCRETE_POWDER.lightBlue(), Blocks.CONCRETE_POWDER.yellow(), Blocks.CONCRETE_POWDER.lime(), Blocks.CONCRETE_POWDER.pink(), Blocks.CONCRETE_POWDER.gray(),
                    Blocks.CONCRETE_POWDER.lightGray(), Blocks.CONCRETE_POWDER.purple(), Blocks.CONCRETE_POWDER.green(), Blocks.CONCRETE_POWDER.red(), Blocks.DYED_TERRACOTTA.purple(), Blocks.DYED_TERRACOTTA.magenta(),
                    Blocks.DYED_TERRACOTTA.pink(), Blocks.GLAZED_TERRACOTTA.magenta(), Blocks.GLAZED_TERRACOTTA.pink(), Blocks.GLAZED_TERRACOTTA.gray(), Blocks.GLAZED_TERRACOTTA.blue(), Blocks.GLAZED_TERRACOTTA.brown(),
                    Blocks.GLAZED_TERRACOTTA.green(), Blocks.COPPER_BLOCK.weathering().oxidized(), Blocks.CUT_COPPER.weathering().unaffected(), Blocks.CUT_COPPER.weathering().exposed(), Blocks.CUT_COPPER.weathering().weathered(), Blocks.CUT_COPPER_SLAB.weathering().unaffected(),
                    Blocks.CUT_COPPER_STAIRS.weathering().unaffected(), Blocks.CUT_COPPER_SLAB.weathering().exposed(), Blocks.CUT_COPPER_STAIRS.weathering().exposed(), Blocks.CUT_COPPER_SLAB.weathering().weathered(), Blocks.CUT_COPPER_STAIRS.weathering().weathered(), Blocks.CUT_COPPER_SLAB.weathering().oxidized(),
                    Blocks.CUT_COPPER_STAIRS.weathering().oxidized(), Blocks.COPPER_BULB.weathering().unaffected(), Blocks.COPPER_BULB.weathering().exposed(), Blocks.COPPER_BULB.weathering().weathered(), Blocks.COPPER_BULB.weathering().oxidized(), Blocks.CHISELED_COPPER.weathering().unaffected(),
                    Blocks.CHISELED_COPPER.weathering().exposed(), Blocks.CHISELED_COPPER.weathering().weathered(), Blocks.CHISELED_COPPER.weathering().oxidized(), Blocks.COPPER_DOOR.weathering().unaffected(), Blocks.COPPER_DOOR.weathering().exposed(), Blocks.COPPER_DOOR.weathering().weathered(),
                    Blocks.COPPER_DOOR.weathering().oxidized(), Blocks.COPPER_GRATE.weathering().unaffected(), Blocks.COPPER_GRATE.weathering().exposed(), Blocks.COPPER_GRATE.weathering().weathered(), Blocks.COPPER_GRATE.weathering().oxidized(), Blocks.COPPER_TRAPDOOR.weathering().unaffected(),
                    Blocks.COPPER_TRAPDOOR.weathering().exposed(), Blocks.COPPER_TRAPDOOR.weathering().weathered(), Blocks.COPPER_BLOCK.waxed().exposed(), Blocks.COPPER_BLOCK.waxed().weathered(), Blocks.CUT_COPPER.waxed().exposed(), Blocks.CUT_COPPER.waxed().weathered(),
                    Blocks.CUT_COPPER_SLAB.waxed().exposed(), Blocks.CUT_COPPER_STAIRS.waxed().exposed(), Blocks.CUT_COPPER_SLAB.waxed().weathered(), Blocks.CUT_COPPER_STAIRS.waxed().weathered(), Blocks.CHISELED_COPPER.waxed().exposed(), Blocks.CHISELED_COPPER.waxed().weathered(),
                    Blocks.COPPER_DOOR.waxed().exposed(), Blocks.COPPER_DOOR.waxed().weathered(), Blocks.COPPER_GRATE.waxed().exposed(), Blocks.COPPER_GRATE.waxed().weathered(), Blocks.COPPER_TRAPDOOR.waxed().unaffected(), Blocks.COPPER_TRAPDOOR.waxed().exposed(),
                    Blocks.COPPER_TRAPDOOR.waxed().weathered(), Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.POTTED_MANGROVE_PROPAGULE, Blocks.POTTED_AZALEA, Blocks.POTTED_CHERRY_SAPLING,
                    Blocks.POTTED_FERN, Blocks.POTTED_ACACIA_SAPLING, Blocks.POTTED_WARPED_FUNGUS, Blocks.POTTED_WARPED_ROOTS, Blocks.POTTED_CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_ROOTS,
                    Blocks.POTTED_OAK_SAPLING, Blocks.POTTED_WITHER_ROSE, Blocks.WITHER_ROSE, Blocks.CAKE, Blocks.CANDLE_CAKE, Blocks.DYED_CANDLE_CAKE.blue(),
                    Blocks.DYED_CANDLE_CAKE.black(), Blocks.DYED_CANDLE_CAKE.brown(), Blocks.DYED_CANDLE_CAKE.cyan(), Blocks.DYED_CANDLE_CAKE.gray(), Blocks.DYED_CANDLE_CAKE.green(), Blocks.DYED_CANDLE_CAKE.lightBlue(),
                    Blocks.DYED_CANDLE_CAKE.lightGray(), Blocks.DYED_CANDLE_CAKE.lime(), Blocks.DYED_CANDLE_CAKE.magenta(), Blocks.DYED_CANDLE_CAKE.orange(), Blocks.DYED_CANDLE_CAKE.pink(), Blocks.DYED_CANDLE_CAKE.purple(),
                    Blocks.DYED_CANDLE_CAKE.red(), Blocks.DYED_CANDLE_CAKE.white(), Blocks.DYED_CANDLE_CAKE.yellow(), Blocks.DYED_CANDLE.blue(), Blocks.DYED_CANDLE.black(), Blocks.DYED_CANDLE.brown(),
                    Blocks.DYED_CANDLE.cyan(), Blocks.DYED_CANDLE.gray(), Blocks.DYED_CANDLE.green(), Blocks.DYED_CANDLE.lightBlue(), Blocks.DYED_CANDLE.lightGray(), Blocks.DYED_CANDLE.lime(),
                    Blocks.DYED_CANDLE.magenta(), Blocks.DYED_CANDLE.orange(), Blocks.DYED_CANDLE.pink(), Blocks.DYED_CANDLE.purple(), Blocks.DYED_CANDLE.yellow(), Blocks.SMOOTH_RED_SANDSTONE,
                    Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE_SLAB,
                    Blocks.RED_SANDSTONE_STAIRS, Blocks.RED_SANDSTONE_WALL, Blocks.ANDESITE_STAIRS, Blocks.ANDESITE_SLAB, Blocks.ANDESITE_WALL, Blocks.POLISHED_ANDESITE_SLAB,
                    Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE_STAIRS, Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE_STAIRS, Blocks.TUFF_SLAB,
                    Blocks.TUFF_STAIRS, Blocks.TUFF_WALL, Blocks.TUFF_BRICK_SLAB, Blocks.TUFF_BRICK_STAIRS, Blocks.TUFF_BRICK_WALL, Blocks.CRACKED_NETHER_BRICKS,
                    Blocks.CHISELED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_SLAB,
                    Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_WALL, Blocks.STAINED_GLASS.orange(), Blocks.STAINED_GLASS.lightBlue(), Blocks.STAINED_GLASS.yellow(), Blocks.STAINED_GLASS.lime(),
                    Blocks.STAINED_GLASS.pink(), Blocks.STAINED_GLASS.cyan(), Blocks.STAINED_GLASS.purple(), Blocks.STAINED_GLASS.blue(), Blocks.STAINED_GLASS.green(), Blocks.STAINED_GLASS.red(),
                    Blocks.CRIMSON_PRESSURE_PLATE, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_PLANKS,
                    Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_PRESSURE_PLATE,
                    Blocks.WARPED_BUTTON, Blocks.WARPED_DOOR, Blocks.WARPED_FENCE, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_PLANKS, Blocks.WARPED_SIGN,
                    Blocks.WARPED_WALL_SIGN, Blocks.WARPED_SLAB, Blocks.WARPED_STAIRS, Blocks.WARPED_TRAPDOOR, Blocks.SCAFFOLDING, Blocks.CHERRY_SIGN,
                    Blocks.CHERRY_WALL_SIGN, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.BIRCH_SIGN,
                    Blocks.BIRCH_WALL_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.MANGROVE_SIGN,
                    Blocks.MANGROVE_WALL_SIGN, Blocks.SLIME_BLOCK, Blocks.SPONGE, Blocks.TINTED_GLASS, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN,
                    Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN,
                    Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN,
                    Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN,
                    Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_STAIRS,
                    Blocks.OCHRE_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PETRIFIED_OAK_SLAB, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.STRIPPED_CHERRY_LOG,
                    Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD, Blocks.CRIMSON_HYPHAE,
                    Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_JUNGLE_LOG,
                    Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM,
                    Blocks.SHULKER_BOX, Blocks.DYED_SHULKER_BOX.black(), Blocks.DYED_SHULKER_BOX.blue(), Blocks.DYED_SHULKER_BOX.brown(), Blocks.DYED_SHULKER_BOX.cyan(), Blocks.DYED_SHULKER_BOX.gray(),
                    Blocks.DYED_SHULKER_BOX.green(), Blocks.DYED_SHULKER_BOX.lightBlue(), Blocks.DYED_SHULKER_BOX.lightGray(), Blocks.DYED_SHULKER_BOX.lime(), Blocks.DYED_SHULKER_BOX.magenta(), Blocks.DYED_SHULKER_BOX.orange(),
                    Blocks.DYED_SHULKER_BOX.pink(), Blocks.DYED_SHULKER_BOX.purple(), Blocks.DYED_SHULKER_BOX.red(), Blocks.DYED_SHULKER_BOX.white(), Blocks.DYED_SHULKER_BOX.yellow(), Blocks.LAVA_CAULDRON,
                    Blocks.POWDER_SNOW_CAULDRON, Blocks.ACTIVATOR_RAIL, Blocks.BEACON, Blocks.BEEHIVE, Blocks.REPEATING_COMMAND_BLOCK, Blocks.COMMAND_BLOCK,
                    Blocks.CHAIN_COMMAND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.IRON_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.RAW_GOLD_BLOCK, Blocks.CONDUIT,
                    Blocks.DAYLIGHT_DETECTOR, Blocks.DETECTOR_RAIL, Blocks.DRIED_KELP_BLOCK, Blocks.DROPPER, Blocks.ENCHANTING_TABLE, Blocks.PIGLIN_HEAD,
                    Blocks.PIGLIN_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.PLAYER_HEAD,
                    Blocks.PLAYER_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL,
                    Blocks.HEAVY_CORE, Blocks.HONEY_BLOCK, Blocks.HONEYCOMB_BLOCK, Blocks.JUKEBOX, Blocks.LODESTONE, Blocks.OBSERVER,
                    Blocks.POWERED_RAIL, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE,
                    Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE, Blocks.RESPAWN_ANCHOR, Blocks.CALIBRATED_SCULK_SENSOR,
                    Blocks.SNIFFER_EGG, Blocks.RESIN_BLOCK, Blocks.RESIN_BRICKS, Blocks.RESIN_BRICK_SLAB, Blocks.RESIN_BRICK_WALL, Blocks.RESIN_BRICK_STAIRS,
                    Blocks.CHISELED_RESIN_BRICKS, Blocks.POTTED_CLOSED_EYEBLOSSOM, Blocks.POTTED_OPEN_EYEBLOSSOM, Blocks.POTTED_PALE_OAK_SAPLING, Blocks.PALE_OAK_SAPLING, Blocks.PALE_OAK_BUTTON,
                    Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_FENCE_GATE, Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_PRESSURE_PLATE, Blocks.PALE_OAK_HANGING_SIGN,
                    Blocks.PALE_OAK_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_TRAPDOOR,
                    Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.COPPER_BARS.weathering().unaffected(), Blocks.COPPER_BARS.waxed().unaffected(), Blocks.COPPER_BARS.weathering().exposed(), Blocks.COPPER_BARS.waxed().exposed(),
                    Blocks.COPPER_BARS.weathering().weathered(), Blocks.COPPER_BARS.waxed().weathered(), Blocks.COPPER_BARS.weathering().oxidized(), Blocks.COPPER_BARS.waxed().oxidized(), Blocks.COPPER_CHAIN.weathering().unaffected(), Blocks.COPPER_CHAIN.waxed().unaffected(),
                    Blocks.COPPER_CHAIN.weathering().exposed(), Blocks.COPPER_CHAIN.waxed().exposed(), Blocks.COPPER_CHAIN.weathering().weathered(), Blocks.COPPER_CHAIN.waxed().weathered(), Blocks.COPPER_CHAIN.weathering().oxidized(), Blocks.COPPER_CHAIN.waxed().oxidized(),
                    Blocks.COPPER_LANTERN.weathering().unaffected(), Blocks.COPPER_LANTERN.waxed().unaffected(), Blocks.COPPER_LANTERN.weathering().exposed(), Blocks.COPPER_LANTERN.waxed().exposed(), Blocks.COPPER_LANTERN.weathering().weathered(), Blocks.COPPER_LANTERN.waxed().weathered(),
                    Blocks.COPPER_LANTERN.weathering().oxidized(), Blocks.COPPER_LANTERN.waxed().oxidized(), Blocks.COPPER_CHEST.weathering().unaffected(), Blocks.COPPER_CHEST.weathering().exposed(), Blocks.COPPER_CHEST.weathering().oxidized(), Blocks.COPPER_CHEST.weathering().weathered(),
                    Blocks.COPPER_CHEST.waxed().unaffected(), Blocks.COPPER_CHEST.waxed().exposed(), Blocks.COPPER_CHEST.waxed().oxidized(), Blocks.COPPER_CHEST.waxed().weathered(), Blocks.COPPER_GOLEM_STATUE.weathering().unaffected(), Blocks.COPPER_GOLEM_STATUE.weathering().exposed(),
                    Blocks.COPPER_GOLEM_STATUE.weathering().weathered(), Blocks.COPPER_GOLEM_STATUE.weathering().oxidized(), Blocks.COPPER_GOLEM_STATUE.waxed().unaffected(), Blocks.COPPER_GOLEM_STATUE.waxed().exposed(), Blocks.COPPER_GOLEM_STATUE.waxed().weathered(), Blocks.COPPER_GOLEM_STATUE.waxed().oxidized(),
                    Blocks.COPPER_TORCH, Blocks.COPPER_WALL_TORCH, Blocks.LIGHTNING_ROD.weathering().unaffected(), Blocks.LIGHTNING_ROD.weathering().exposed(), Blocks.LIGHTNING_ROD.weathering().weathered(), Blocks.LIGHTNING_ROD.weathering().oxidized(),
                    Blocks.LIGHTNING_ROD.waxed().unaffected(), Blocks.LIGHTNING_ROD.waxed().exposed(), Blocks.LIGHTNING_ROD.waxed().weathered(), Blocks.LIGHTNING_ROD.waxed().oxidized(), Blocks.OAK_SHELF, Blocks.DARK_OAK_SHELF,
                    Blocks.PALE_OAK_SHELF, Blocks.ACACIA_SHELF, Blocks.BAMBOO_SHELF, Blocks.BIRCH_SHELF, Blocks.CHERRY_SHELF, Blocks.CRIMSON_SHELF,
                    Blocks.JUNGLE_SHELF, Blocks.MANGROVE_SHELF, Blocks.SPRUCE_SHELF, Blocks.WARPED_SHELF, Blocks.GOLDEN_DANDELION, Blocks.POTTED_GOLDEN_DANDELION,
                    Blocks.CINNABAR_SLAB, Blocks.CINNABAR_STAIRS, Blocks.CINNABAR_WALL, Blocks.CINNABAR_BRICKS, Blocks.CINNABAR_BRICK_SLAB, Blocks.CINNABAR_BRICK_STAIRS,
                    Blocks.CINNABAR_BRICK_WALL, Blocks.CHISELED_CINNABAR, Blocks.POLISHED_CINNABAR, Blocks.POLISHED_CINNABAR_SLAB, Blocks.POLISHED_CINNABAR_STAIRS, Blocks.POLISHED_CINNABAR_WALL,
                    Blocks.SULFUR_SLAB, Blocks.SULFUR_STAIRS, Blocks.SULFUR_WALL, Blocks.SULFUR_BRICKS, Blocks.SULFUR_BRICK_SLAB, Blocks.SULFUR_BRICK_STAIRS,
                    Blocks.SULFUR_BRICK_WALL, Blocks.CHISELED_SULFUR, Blocks.POLISHED_SULFUR, Blocks.POLISHED_SULFUR_SLAB, Blocks.POLISHED_SULFUR_STAIRS, Blocks.POLISHED_SULFUR_WALL
            )
            .visible(list1Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list2Activar = sglists.add(new BoolSetting.Builder()
            .name("List #2 Activate")
            .description("Activates checks for List #2")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks2 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #2 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(Blocks.POLISHED_DIORITE, Blocks.NOTE_BLOCK, Blocks.MANGROVE_WOOD, Blocks.COPPER_BLOCK.weathering().weathered())
            .visible(list2Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list3Activar = sglists.add(new BoolSetting.Builder()
            .name("List #3 Activate")
            .description("Activates checks for List #3")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks3 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #3 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(Blocks.CRAFTING_TABLE, Blocks.BREWING_STAND, Blocks.ENDER_CHEST, Blocks.SMOOTH_QUARTZ, Blocks.REDSTONE_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.STAINED_GLASS.brown())
            .visible(list3Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list4Activar = sglists.add(new BoolSetting.Builder()
            .name("List #4 Activate")
            .description("Activates checks for List #4")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks4 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #4 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(Blocks.TRAPPED_CHEST, Blocks.IRON_TRAPDOOR, Blocks.LAPIS_BLOCK)
            .visible(list4Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list5Activar = sglists.add(new BoolSetting.Builder()
            .name("List #5 Activate")
            .description("Activates checks for List #5")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks5 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #5 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(Blocks.QUARTZ_BLOCK, Blocks.FURNACE, Blocks.BED.black(), Blocks.BED.gray(), Blocks.BED.lightBlue(), Blocks.BED.lightGray(), Blocks.BED.pink(), Blocks.BED.red(), Blocks.BED.white(), Blocks.BED.yellow(), Blocks.BED.orange(), Blocks.BED.blue(), Blocks.BED.cyan(), Blocks.BED.green(), Blocks.BED.lime(), Blocks.BED.purple(), Blocks.BED.magenta(), Blocks.BED.brown(), Blocks.CONCRETE.white())
            .visible(list5Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list6Activar = sglists.add(new BoolSetting.Builder()
            .name("List #6 Activate")
            .description("Activates checks for List #6")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks6 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #6 (Default)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue(Blocks.REDSTONE_TORCH, Blocks.HOPPER)
            .visible(list6Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Boolean> list7Activar = sglists.add(new BoolSetting.Builder()
            .name("List #7 Activate")
            .description("Activates checks for List #7")
            .defaultValue(true)
            .build());
    private final Setting<List<Block>> Blawcks7 = sglists.add(new BlockListSetting.Builder()
            .name("Block List #7 (Extra Custom)")
            .description("If the total amount of any of these found is greater than the Number specified, throw a base location.")
            .defaultValue()
            .visible(list7Activar::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Integer> blowkfind1 = sglists.add(new IntSetting.Builder()
            .name("(List #1) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(1)
            .visible(list1Activar::get)
            .build());
    private final Setting<Integer> blowkfind2 = sglists.add(new IntSetting.Builder()
            .name("(List #2) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(6)
            .visible(list2Activar::get)
            .build());
    private final Setting<Integer> blowkfind3 = sglists.add(new IntSetting.Builder()
            .name("(List #3) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(4)
            .visible(list3Activar::get)
            .build());
    private final Setting<Integer> blowkfind4 = sglists.add(new IntSetting.Builder()
            .name("(List #4) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(2)
            .visible(list4Activar::get)
            .build());
    private final Setting<Integer> blowkfind5 = sglists.add(new IntSetting.Builder()
            .name("(List #5) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(12)
            .visible(list5Activar::get)
            .build());
    private final Setting<Integer> blowkfind6 = sglists.add(new IntSetting.Builder()
            .name("(List #6) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(12)
            .visible(list6Activar::get)
            .build());
    private final Setting<Integer> blowkfind7 = sglists.add(new IntSetting.Builder()
            .name("(List #7) Number Of Blocks to Find")
            .description("How many blocks it takes, from of any of the listed blocks to throw a base location.")
            .min(1)
            .sliderRange(1,100)
            .defaultValue(1)
            .visible(list7Activar::get)
            .build());
    private final Setting<Boolean> exclusionEnabled = sglists.add(new BoolSetting.Builder()
            .name("Chunk Exclusion")
            .description("If a chunk contains enough of these blocks, it will be excluded from detection. Useful for filtering out structures like woodland mansions or villages.")
            .defaultValue(false)
            .build()
    );
    private final Setting<List<Block>> exclusionBlocks = sglists.add(new BlockListSetting.Builder()
            .name("Exclusion Blocks")
            .description("Blocks that cause a chunk to be excluded when the minimum count is reached. Add blocks like dark oak fences to filter woodland mansions.")
            .defaultValue()
            .visible(exclusionEnabled::get)
            .filter(this::filterBlocks)
            .build()
    );
    private final Setting<Integer> exclusionMinCount = sglists.add(new IntSetting.Builder()
            .name("Exclusion Min Count")
            .description("Minimum number of exclusion blocks in a chunk before it is excluded from detection.")
            .min(1)
            .sliderRange(1, 100)
            .defaultValue(5)
            .visible(exclusionEnabled::get)
            .build()
    );
    private final Setting<Boolean> remove = sgcacheCdata.add(new BoolSetting.Builder()
            .name("RemoveOnModuleDisabled")
            .description("Removes the cached chunks containing bases when disabling the module.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> worldleaveremove = sgcacheCdata.add(new BoolSetting.Builder()
            .name("RemoveOnLeaveWorldOrChangeDimensions")
            .description("Removes the cached chunks containing bases when leaving the Level or changing dimensions.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> removerenderdist = sgcacheCdata.add(new BoolSetting.Builder()
            .name("RemoveOutsideRenderDistance")
            .description("Removes the cached chunks when they leave the defined render distance.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> save = sgCdata.add(new BoolSetting.Builder()
            .name("SaveBaseData")
            .description("Saves the cached bases to a file.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> load = sgCdata.add(new BoolSetting.Builder()
            .name("LoadBaseData")
            .description("Loads the saved bases from the file.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> autoreload = sgCdata.add(new BoolSetting.Builder()
            .name("AutoReloadBases")
            .description("Reloads the bases automatically from your savefiles on a delay.")
            .defaultValue(false)
            .visible(load::get)
            .build()
    );
    private final Setting<Integer> removedelay = sgCdata.add(new IntSetting.Builder()
            .name("AutoReloadDelayInSeconds")
            .description("Reloads the bases automatically from your savefiles on a delay.")
            .sliderRange(1,300)
            .defaultValue(60)
            .visible(() -> autoreload.get() && load.get())
            .build());

    @Override
    public WWidget getWidget(GuiTheme theme) {
        this.cachedTheme = theme;
        WTable table1 = theme.table();
        WTable table = theme.table();
        WButton nearestB = table1.add(theme.button("NearestBase")).expandX().minWidth(100).widget();
        nearestB.action = () -> {
            if(isBaseFinderModuleOn==0){
                error("Please turn on BaseFinder module and push the button again.");
            } else {
                findnearestbaseticks=1;
            }
        };
        table1.row();
        WButton adddata = table1.add(theme.button("AddBase")).expandX().minWidth(100).widget();
        adddata.action = () -> {
            if(isBaseFinderModuleOn==0){
                error("Please turn on BaseFinder module and push the button again.");
            } else {
                if (!baseChunks.contains(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()))){
                    baseChunks.add(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()));
                    suppressedChunks.remove(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()));
                    addTrigger(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()), "Manual Add");
                    try {
                        Path baseDir = FabricLoader.getInstance().getGameDir()
                                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                        Files.createDirectories(baseDir);
                        Path filePath = baseDir.resolve("BaseChunkData.txt");
                        ChunkPos chunkPos = new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z());
                        String data = chunkPos + System.lineSeparator();
                        Files.write(filePath, data.getBytes(StandardCharsets.UTF_8),
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ChatUtils.sendMsg(Component.literal("Base near X"+mc.player.chunkPosition().getMiddleBlockX()+", Z"+mc.player.chunkPosition().getMiddleBlockZ()+" added to the BaseFinder."));
            }
        };
        table1.row();
        WButton deldata = table1.add(theme.button("RemoveBase")).expandX().minWidth(100).widget();
        deldata.action = () -> {
            if(isBaseFinderModuleOn==0){
                error("Please turn on BaseFinder module and push the button again.");
            } else {
                if (baseChunks.contains(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()))){
                    baseChunks.remove(new ChunkPos(mc.player.chunkPosition().x(), mc.player.chunkPosition().z()));
                    try {
                        Path baseDir = FabricLoader.getInstance().getGameDir()
                                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                        Files.createDirectories(baseDir);
                        Path filePath = baseDir.resolve("BaseChunkData.txt");
                        Files.deleteIfExists(filePath);
                        List<String> chunkDataLines = baseChunks.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        Files.write(filePath, chunkDataLines, StandardCharsets.UTF_8,
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ChatUtils.sendMsg(Component.literal("Base near X"+mc.player.chunkPosition().getMiddleBlockX()+", Z"+mc.player.chunkPosition().getMiddleBlockZ()+" removed from the BaseFinder."));
            }
        };
        table1.row();
        WButton dellastdata = table1.add(theme.button("RemoveLastBase")).expandX().minWidth(100).widget();
        dellastdata.action = () -> {
            if(isBaseFinderModuleOn==0){
                error("Please turn on BaseFinder module and push the button again.");
            } else if(isBaseFinderModuleOn!=0 && (LastBaseFound.x()==2000000000 || LastBaseFound.z()==2000000000)){
                error("Please find a base and run the command again.");
            } else {
                if (baseChunks.contains(new ChunkPos(LastBaseFound.x(), LastBaseFound.z()))){
                    baseChunks.remove(new ChunkPos(LastBaseFound.x(), LastBaseFound.z()));
                    try {
                        Path baseDir = FabricLoader.getInstance().getGameDir()
                                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                        Files.createDirectories(baseDir);
                        Path filePath = baseDir.resolve("BaseChunkData.txt");
                        Files.deleteIfExists(filePath);
                        List<String> chunkDataLines = baseChunks.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        Files.write(filePath, chunkDataLines, StandardCharsets.UTF_8,
                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ChatUtils.sendMsg(Component.literal("Base near X"+LastBaseFound.getMiddleBlockX()+", Z"+LastBaseFound.getMiddleBlockZ()+" removed from the BaseFinder."));
                LastBaseFound= new ChunkPos(2000000000, 2000000000);
            }
        };
        table1.row();
        WButton deletedata = table1.add(theme.button("**DELETE ALL BASE DATA**")).expandX().minWidth(100).widget();
        deletedata.action = () -> {
            if (!(mc.level==null) && mc.level.hasChunk(mc.player.chunkPosition().x(),mc.player.chunkPosition().z())){
                if (deletewarning==0) error("PRESS AGAIN WITHIN 5s TO DELETE ALL BASE DATA FOR THIS DIMENSION.");
                deletewarningTicks=0;
                deletewarning++;
            }
        };
        table1.row();
        List<LoggedBase> sortedBases = new ArrayList<>(loggedBases);
        sortedBases.sort(Comparator.comparingInt(a -> a.y));
        var list = theme.verticalList();
        list.add(table1);
        var clear = list.add(theme.button("Clear Logged Positions")).widget();
        clear.action = () -> {
            loggedBases.clear();
            loggedBasePositions.clear();
            table.clear();
            saveJsonLog();
            saveCsvLog();
        };

        if (save.get()) {
            WButton viewChunks = list.add(theme.button("View Flagged Chunks (" + baseChunks.size() + ")")).widget();
            viewChunks.action = () -> {
                mc.setScreenAndShow(new net.chuck.chuckpack.gui.screens.FlaggedChunksScreen(theme, this, baseChunks, chunkTriggerReasons, chunkBlockCounts, chunkEntityCounts));
            };
        }

        if(!sortedBases.isEmpty()) list.add(table);
        for(LoggedBase lb : sortedBases) {
            table.add(theme.label("Pos: " + lb.x + ", " + lb.y + ", " + lb.z));
            WButton gotoBtn = table.add(theme.button("Goto")).widget();
            gotoBtn.action = () -> { meteordevelopment.meteorclient.pathing.PathManagers.get().moveTo(new BlockPos(lb.x, lb.y, lb.z), true); };
            var delete = table.add(theme.button("-")).widget();
            delete.action = () -> {
                loggedBases.remove(lb);
                loggedBasePositions.remove(new ChunkPos((lb.x - 8) / 16, (lb.z - 8) / 16));
                table.clear();
                for(LoggedBase l : loggedBases) {
                    table.add(theme.label("Pos: " + l.x + ", " + l.y + ", " + l.z));
                    WButton gotoBtn2 = table.add(theme.button("Goto")).widget();
                    gotoBtn2.action = () -> { meteordevelopment.meteorclient.pathing.PathManagers.get().moveTo(new BlockPos(l.x, l.y, l.z), true); };
                    var delete2 = table.add(theme.button("-")).widget();
                    delete2.action = () -> {
                        loggedBases.remove(l);
                        loggedBasePositions.remove(new ChunkPos((l.x - 8) / 16, (l.z - 8) / 16));
                        table.clear();
                        for(LoggedBase l2 : loggedBases) {
                            table.add(theme.label("Pos: " + l2.x + ", " + l2.y + ", " + l2.z));
                            WButton gotoBtn3 = table.add(theme.button("Goto")).widget();
                            gotoBtn3.action = () -> { meteordevelopment.meteorclient.pathing.PathManagers.get().moveTo(new BlockPos(l2.x, l2.y, l2.z), true); };
                            var delete3 = table.add(theme.button("-")).widget();
                            delete3.action = () -> {
                                loggedBases.remove(l2);
                                loggedBasePositions.remove(new ChunkPos((l2.x - 8) / 16, (l2.z - 8) / 16));
                            };
                            table.row();
                        }
                        saveJsonLog();
                        saveCsvLog();
                    };
                    table.row();
                }
                saveJsonLog();
                saveCsvLog();
            };
            table.row();
        }
        return list;
    }

    public void openFlaggedChunksScreen() {
        // Use the live GUI theme when settings were never opened (cachedTheme unset).
        GuiTheme theme = cachedTheme != null ? cachedTheme : GuiThemes.get();
        mc.setScreenAndShow(new net.chuck.chuckpack.gui.screens.FlaggedChunksScreen(theme, this, baseChunks, chunkTriggerReasons, chunkBlockCounts, chunkEntityCounts));
    }

    // render
    public final Setting<Integer> renderDistance = sgRender.add(new IntSetting.Builder()
            .name("Render-Distance(Chunks)")
            .description("How many chunks from the character to render the detected chunks with bases.")
            .defaultValue(128)
            .min(6)
            .sliderRange(6,128)
            .build()
    );
    public final Setting<Integer> renderHeightY = sgRender.add(new IntSetting.Builder()
            .name("render-TopY")
            .description("The render height.")
            .defaultValue(256)
            .sliderRange(-128,512)
            .build()
    );
    public final Setting<Integer> renderHeightYbottom = sgRender.add(new IntSetting.Builder()
            .name("render-BottomY")
            .description("The render height.")
            .defaultValue(150)
            .sliderRange(-128,512)
            .build()
    );
    private final Setting<Boolean> trcr = sgRender.add(new BoolSetting.Builder()
            .name("Tracers")
            .description("Show tracers to the base chunks.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> nearesttrcr = sgRender.add(new BoolSetting.Builder()
            .name("Tracer to NearestBase Only")
            .description("Show only one tracer to the nearest base LevelChunk.")
            .defaultValue(true)
            .build()
    );
    public final Setting<Integer> trcrdist = sgRender.add(new IntSetting.Builder()
            .name("Tracer Distance (in chunks)")
            .description("How far from the base LevelChunk to still render a tracer.")
            .defaultValue(32)
            .sliderRange(1,1024)
            .visible(trcr::get)
            .build()
    );
    private final Setting<SettingColor> baseChunksSideColor = sgRender.add(new ColorSetting.Builder()
            .name("Base-chunks-waypoint-color")
            .description("Color of the waypoints indicating chunks that may contain bases or builds.")
            .defaultValue(new SettingColor(255, 127, 0, 40, true))
            .build()
    );
    private final Setting<SettingColor> baseChunksLineColor = sgRender.add(new ColorSetting.Builder()
            .name("Base-chunks-tracer-color")
            .description("Color of tracers to the chunks that may contain bases or builds.")
            .defaultValue(new SettingColor(255, 127, 0, 255, true))
            .visible(trcr::get)
            .build()
    );
    private final Setting<Boolean> locLogging = locationLogs.add(new BoolSetting.Builder()
            .name("Enable Location Logging")
            .description("Logs the locations of detected spawners to a table in this options menu.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> locLoggingCSV = locationLogs.add(new BoolSetting.Builder()
            .name("Log to CSV file")
            .description("Logs the locations of detected spawners to a csv file.")
            .defaultValue(false)
            .build()
    );
    private ExecutorService taskExecutor;
    private final Set<Block> blockSet1 = new LinkedHashSet<>();
    private final Set<Block> blockSet2 = new LinkedHashSet<>();
    private final Set<Block> blockSet3 = new LinkedHashSet<>();
    private final Set<Block> blockSet4 = new LinkedHashSet<>();
    private final Set<Block> blockSet5 = new LinkedHashSet<>();
    private final Set<Block> blockSet6 = new LinkedHashSet<>();
    private final Set<Block> blockSet7 = new LinkedHashSet<>();
    private int basefoundspamTicks=0;
    private boolean basefound=false;
    private int deletewarningTicks=666;
    private int deletewarning=0;
    private boolean checkingchunk1=false;
    private int found1 = 0;
    private boolean checkingchunk2=false;
    private int found2 = 0;
    private boolean checkingchunk3=false;
    private int found3 = 0;
    private boolean checkingchunk4=false;
    private int found4 = 0;
    private boolean checkingchunk5=false;
    private int found5 = 0;
    private boolean checkingchunk6=false;
    private int found6 = 0;
    private boolean checkingchunk7=false;
    private int found7 = 0;
    private GuiTheme cachedTheme;
    private ChunkPos LastBaseFound = new ChunkPos(2000000000, 2000000000);
    private ChunkPos closestBase = new ChunkPos(2000000000, 2000000000);
    private double basedistance=2000000000;
    private String serverip;
    private String worldName;
    private ChunkPos basepos;
    private BlockPos blockposi;
    private final Set<ChunkPos> baseChunks = Collections.synchronizedSet(new HashSet<>());
    // Chunks that Auto delete flagged chunks has removed. Kept separate from baseChunks
    // (which represents "currently flagged") so detection logic can permanently
    // skip these instead of re-flagging them on the very next scan pass.
    // Chunks that Auto delete flagged chunks has removed, suppressed only while the
    // LevelChunk stays loaded on the client. The moment a suppressed LevelChunk unloads
    // (player moves far enough away), it's dropped from this set with no
    // memory kept - if it loads again later, it gets scanned completely
    // fresh, exactly as if Auto delete flagged chunks never touched it.
    private final Set<ChunkPos> suppressedChunks = Collections.synchronizedSet(new HashSet<>());
    private final java.util.Map<ChunkPos, Map<String, Integer>> chunkTriggerReasons = Collections.synchronizedMap(new java.util.LinkedHashMap<>());
    // Tracks how many consecutive ticks the player has continuously stayed
    // within Delete radius of each flagged LevelChunk. Reset to 0 the moment
    // the player leaves range, per Delete time setting.
    private final Map<ChunkPos, Integer> chunkDeleteProgress = new HashMap<>();
    private static int isBaseFinderModuleOn=0;
    private int autoreloadticks=0;
    private int loadingticks=0;
    private boolean worldchange=false;
    private int justenabledsavedata=0;
    private boolean saveDataWasOn = false;
    private int findnearestbaseticks=0;
    private boolean spawnernaturalblocks=false;
    private boolean spawnerfound=false;
    private int spawnerY;
    private String lastblockfound1;
    private String lastblockfound2;
    private String lastblockfound3;
    private String lastblockfound4;
    private String lastblockfound5;
    private String lastblockfound6;
    private String lastblockfound7;
    private final Map<ChunkPos, Map<Block, Integer>> chunkBlockCounts = new HashMap<>();
    private final Map<ChunkPos, Map<String, Integer>> chunkEntityCounts = new HashMap<>();
    private final Map<ChunkPos, Long> pulsingChunks = new HashMap<>();
    private static final long PULSE_DURATION_MS = 1000;
    private int entityScanTicks;
    private volatile boolean deactivating = false;

    public BaseFinder() {
        super(Categories.World, "Base Finder", "Estimates if a build or base may be in the LevelChunk based on the blocks it contains. Ported from Trouser Streak by etianl.");
    }
    private void clearChunkData() {
        baseChunks.clear();
        chunkTriggerReasons.clear();
        chunkBlockCounts.clear();
        chunkEntityCounts.clear();
        suppressedChunks.clear();
        basedistance=2000000000;
        closestBase = new ChunkPos(2000000000, 2000000000);
        LastBaseFound = new ChunkPos(2000000000, 2000000000);
    }

    private void addTrigger(ChunkPos pos, String reason) {
        chunkTriggerReasons.computeIfAbsent(pos, k -> Collections.synchronizedMap(new HashMap<>())).merge(reason, 1, Integer::sum);
    }

    private void notifyBaseFound(ChunkPos pos) {
        if (!baseNotifier.get()) return;
        MutableComponent openGuiBtn = Component.literal("[Open GUI]")
            .setStyle(Style.EMPTY
                .withColor(ChatFormatting.DARK_GRAY)
                .withUnderlined(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Open flagged chunks GUI")))
                .withClickEvent(new ClickEvent.RunCommand(".openflaggedchunks")));
        MutableComponent message = Component.literal("Base was found ")
            .withStyle(ChatFormatting.DARK_GRAY)
            .append(openGuiBtn);
        ChatUtils.sendMsg(message);
    }

    private boolean isInSpawnChunks(ChunkPos pos) {
        if (mc.level == null) return false;

        int minDistance = mc.level.dimension() == Level.NETHER
            ? minSpawnDistanceNether.get()
            : minSpawnDistanceOverworld.get();

        if (minDistance <= 0) return false;

        BlockPos spawnPos = mc.level.getRespawnData().pos();
        ChunkPos spawnChunk = ChunkPos.containing(spawnPos);
        double dx = pos.x() - spawnChunk.x();
        double dz = pos.z() - spawnChunk.z();
        return Math.sqrt(dx * dx + dz * dz) < minDistance;
    }

    public void pulseChunk(ChunkPos pos) {
        pulsingChunks.put(pos, System.currentTimeMillis());
    }
    @Override
    public void onActivate() {
        deactivating = false;
        deduplicateBlockLists();
        taskExecutor = Executors.newCachedThreadPool();
        isBaseFinderModuleOn=1;
        if (save.get())saveDataWasOn = true;
        else if (!save.get())saveDataWasOn = false;
        if (autoreload.get()) {
            clearChunkData();
        }
        if (save.get() || load.get()) {
            if (mc.hasSingleplayerServer()){
                Path worldPath = mc.getSingleplayerServer().getServerDirectory();
                Path savesDir = worldPath.getParent();
                if (savesDir != null) {
                    Path worldDir = savesDir.getFileName();
                    serverip = (worldDir != null ? worldDir.toString() : "singleplayer")
                            .replaceAll("[^a-zA-Z0-9._-]", "_");
                } else {
                    serverip = "singleplayer";
                }
            } else {
                serverip = mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9._\\-]", "_");
            }
            worldName= mc.level.dimension().identifier().toString().replaceAll("[^a-zA-Z0-9._\\-]", "_");
            if (save.get()) {
                try {
                    Path baseDir = FabricLoader.getInstance().getGameDir()
                            .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                    Files.createDirectories(baseDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (load.get()){
                loadData();
            }
        }
        autoreloadticks=0;
        loadingticks=0;
        worldchange=false;
        justenabledsavedata = 0;
    }

    @Override
    public void onDeactivate() {
        deactivating = true;
        taskExecutor.shutdownNow();
        isBaseFinderModuleOn=0;
        autoreloadticks=0;
        loadingticks=0;
        worldchange=false;
        justenabledsavedata = 0;
        if (remove.get() || autoreload.get()) {
            clearChunkData();
        }
        super.onDeactivate();
    }

    private void deduplicateBlockLists() {
        rebuildBlockSet(Blawcks1.get(), blockSet1);
        rebuildBlockSet(Blawcks2.get(), blockSet2);
        rebuildBlockSet(Blawcks3.get(), blockSet3);
        rebuildBlockSet(Blawcks4.get(), blockSet4);
        rebuildBlockSet(Blawcks5.get(), blockSet5);
        rebuildBlockSet(Blawcks6.get(), blockSet6);
        rebuildBlockSet(Blawcks7.get(), blockSet7);
    }

    private void rebuildBlockSet(List<Block> blocks, Set<Block> target) {
        target.clear();
        target.addAll(blocks);
    }

    private Set<Block> deduplicateAndFilter(List<Block> blocks) {
        return new LinkedHashSet<>(blocks);
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof DisconnectedScreen) {
            if (worldleaveremove.get()) {
                clearChunkData();
            }
        }
        if (event.screen instanceof LevelLoadingScreen) {
            worldchange=true;
        }
    }
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (worldleaveremove.get()) {
            clearChunkData();
        }
    }
    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        worldName = mc.level.dimension().identifier().toString().replaceAll("[^a-zA-Z0-9._\\-]", "_");

        if (basefound && basefoundspamTicks < bsefndtickdelay.get()) basefoundspamTicks++;
        else if (basefoundspamTicks >= bsefndtickdelay.get()) {
            basefound = false;
            basefoundspamTicks = 0;
        }
        if (deletewarningTicks <= 100) deletewarningTicks++;
        if (deletewarning>=2){
            if (mc.hasSingleplayerServer()){
                Path worldPath = mc.getSingleplayerServer().getServerDirectory();
                Path savesDir = worldPath.getParent();
                if (savesDir != null) {
                    Path worldDir = savesDir.getFileName();
                    serverip = (worldDir != null ? worldDir.toString() : "singleplayer")
                            .replaceAll("[^a-zA-Z0-9._-]", "_");
                } else {
                    serverip = "singleplayer";
                }
            } else {
                serverip = mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9._\\-]", "_");
            }
            clearChunkData();
            try {
                Path baseDir = FabricLoader.getInstance().getGameDir()
                        .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                Path filePath = baseDir.resolve("BaseChunkData.txt");
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            error("LevelChunk Data deleted for this Dimension.");
            deletewarning=0;
        }
        if (load.get()) {
            if (loadingticks < 1) {
                loadData();
                loadingticks++;
            }
        } else if (!load.get()) {
            loadingticks = 0;
        }

        try {
            if (baseChunks.stream().toList().size() > 0) {
                for (int b = 0; b < baseChunks.stream().toList().size(); b++) {
                    if (basedistance > Math.sqrt(Math.pow(baseChunks.stream().toList().get(b).x() - mc.player.chunkPosition().x(), 2) + Math.pow(baseChunks.stream().toList().get(b).z() - mc.player.chunkPosition().z(), 2))) {
                        closestBase = new ChunkPos(baseChunks.stream().toList().get(b).x(), baseChunks.stream().toList().get(b).z());
                        basedistance = Math.sqrt(Math.pow(baseChunks.stream().toList().get(b).x() - mc.player.chunkPosition().x(), 2) + Math.pow(baseChunks.stream().toList().get(b).z() - mc.player.chunkPosition().z(), 2));
                    }
                }
                basedistance = 2000000000;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (findnearestbaseticks == 1) {
            if (closestBase.x() < 1000000000 && closestBase.z() < 1000000000)
                ChatUtils.sendMsg(Component.literal("#Nearest possible base at X" + closestBase.x() * 16 + " x Z" + closestBase.z() * 16));
            if (!(closestBase.x() < 1000000000 && closestBase.z() < 1000000000))
                error("No Bases Logged Yet.");
            findnearestbaseticks = 0;
        }

        if (save.get() || load.get()) {
            if (mc.hasSingleplayerServer()) {
                Path worldPath = mc.getSingleplayerServer().getServerDirectory();
                Path savesDir = worldPath.getParent();
                if (savesDir != null) {
                    Path worldDir = savesDir.getFileName();
                    serverip = (worldDir != null ? worldDir.toString() : "singleplayer")
                            .replaceAll("[^a-zA-Z0-9._-]", "_");
                } else {
                    serverip = "singleplayer";
                }
            } else {
                serverip = mc.getCurrentServer().ip.replaceAll("[^a-zA-Z0-9._\\-]", "_");
            }
            worldName = mc.level.dimension().identifier().toString().replaceAll("[^a-zA-Z0-9._\\-]", "_");
        }

        if (autoreload.get()) {
            autoreloadticks++;
            if (autoreloadticks == removedelay.get() * 20) {
                clearChunkData();
                if (load.get()) {
                    loadData();
                }
            } else if (autoreloadticks >= removedelay.get() * 20) {
                autoreloadticks = 0;
            }
        }
        //autoreload when entering different dimensions
        if (load.get() && worldchange) {
            if (worldleaveremove.get()) {
                clearChunkData();
            }
            loadData();
            worldchange = false;
        }
        if (!save.get()) saveDataWasOn = false;
        if (save.get() && justenabledsavedata <= 2 && !saveDataWasOn) {
            justenabledsavedata++;
            if (justenabledsavedata == 1) {
                synchronized (baseChunks) {
                    for (ChunkPos chunkPos : baseChunks) {
                        saveBaseChunkData(chunkPos);
                    }
                }
            }
        }

        if (entityScanTicks < entityScanDelay.get()) entityScanTicks++;
        if (entityScanTicks >= entityScanDelay.get() && (pearlFinder.get() || frameFinder.get() || villagerFinder.get() || nameFinder.get() || boatFinder.get() || entityClusterFinder.get())) {
            if (mc.level == null) return;

            chunkEntityCounts.clear();

            int renderDistance = mc.options.renderDistance().get();
            ChunkPos playerChunkPos = ChunkPos.containing(mc.player.blockPosition());
            for (int chunkX = playerChunkPos.x() - renderDistance; chunkX <= playerChunkPos.x() + renderDistance; chunkX++) {
                for (int chunkZ = playerChunkPos.z() - renderDistance; chunkZ <= playerChunkPos.z() + renderDistance; chunkZ++) {
                    LevelChunk chunk = mc.level.getChunk(chunkX, chunkZ);
                    if (chunk != null && chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
                        if (isInSpawnChunks(chunk.getPos())) continue;
                        AABB chunkBox = new AABB(
                                chunk.getPos().getMinBlockX(), mc.level.getMinY(), chunk.getPos().getMinBlockZ(),
                                chunk.getPos().getMaxBlockX() + 1, mc.level.getMinY() + mc.level.getHeight(), chunk.getPos().getMaxBlockZ() + 1
                        );
                        if (!suppressedChunks.contains(chunk.getPos())) {
                            boolean alreadyFlagged = baseChunks.contains(chunk.getPos());
                            AtomicInteger animalsFound = new AtomicInteger();
                            mc.level.getEntities((Entity) null, chunkBox, entity -> true).forEach(entity -> {
                                if ((entity instanceof ItemFrame || entity instanceof GlowItemFrame) && frameFinder.get()) {
                                    ItemFrame itemFrame = (ItemFrame) entity;
                                    Item heldItem = itemFrame.getItem().getItem();
                                    if (heldItem != Items.ELYTRA) {
                                        chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("Item Frame", 1, Integer::sum);
                                        if (!alreadyFlagged) {
                                            baseChunks.add(chunk.getPos());
                                            addTrigger(chunk.getPos(), "Item Frame");
                                            if (save.get()) {
                                                saveBaseChunkData(chunk.getPos());
                                            }
                                            if (basefoundspamTicks == 0) {
                                                if (entityChatFeedback.get()){
                                                    if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Item Frame located near X" + entity.position().x + ", Y" + entity.position().y + ", Z" + entity.position().z));
                                                    else ChatUtils.sendMsg(Component.literal("Item Frame located!"));
                                                }
                                            }
                                            LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                            basefound = true;
                                            notifyBaseFound(chunk.getPos());
                                        }
                                    }
                                } else if (entity instanceof ThrownEnderpearl && pearlFinder.get()) {
                                    chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("Ender Pearl", 1, Integer::sum);
                                    if (!alreadyFlagged) {
                                        baseChunks.add(chunk.getPos());
                                        addTrigger(chunk.getPos(), "Ender Pearl");
                                        if (save.get()) {
                                            saveBaseChunkData(chunk.getPos());
                                        }
                                        if (basefoundspamTicks == 0) {
                                            if (entityChatFeedback.get()){
                                                if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Ender Pearl located near X" + entity.position().x + ", Y" + entity.position().y + ", Z" + entity.position().z));
                                                else ChatUtils.sendMsg(Component.literal("Ender Pearl located!"));
                                            }
                                        }
                                        LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                        basefound = true;
                                        notifyBaseFound(chunk.getPos());
                                    }
                                } else if (entity instanceof Villager && villagerFinder.get()) {
                                    if (((Villager) entity).getVillagerData().level() > 1) {
                                        chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("Villager", 1, Integer::sum);
                                        if (!alreadyFlagged) {
                                            baseChunks.add(chunk.getPos());
                                            addTrigger(chunk.getPos(), "Villager");
                                            if (save.get()) {
                                                saveBaseChunkData(chunk.getPos());
                                            }
                                            if (basefoundspamTicks == 0) {
                                                if (entityChatFeedback.get()){
                                                    if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Illegal Villager located near X" + entity.position().x + ", Y" + entity.position().y + ", Z" + entity.position().z));
                                                    else ChatUtils.sendMsg(Component.literal("Illegal Villager located!"));
                                                }
                                            }
                                            LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                            basefound = true;
                                            notifyBaseFound(chunk.getPos());
                                        }
                                    }
                                } else if (entity.hasCustomName() && nameFinder.get()) {
                                    chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("NameTag", 1, Integer::sum);
                                    if (!alreadyFlagged) {
                                        baseChunks.add(chunk.getPos());
                                        addTrigger(chunk.getPos(), "NameTag");
                                        if (save.get()) {
                                            saveBaseChunkData(chunk.getPos());
                                        }
                                        if (basefoundspamTicks == 0) {
                                            if (entityChatFeedback.get()){
                                                if (displaycoords.get())ChatUtils.sendMsg(Component.literal("NameTagged Entity located near X" + entity.position().x + ", Y" + entity.position().y + ", Z" + entity.position().z));
                                                else ChatUtils.sendMsg(Component.literal("NameTagged Entity located!"));
                                            }
                                        }
                                        LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                        basefound = true;
                                        notifyBaseFound(chunk.getPos());
                                    }
                                } else if ((entity instanceof ChestBoat || entity instanceof Boat) && boatFinder.get()) {
                                    chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("Boat", 1, Integer::sum);
                                    if (!alreadyFlagged) {
                                        baseChunks.add(chunk.getPos());
                                        addTrigger(chunk.getPos(), "Boat");
                                        if (save.get()) {
                                            saveBaseChunkData(chunk.getPos());
                                        }
                                        if (basefoundspamTicks == 0) {
                                            if (entityChatFeedback.get()){
                                                if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Illegal Boat located near X" + entity.position().x + ", Y" + entity.position().y + ", Z" + entity.position().z));
                                                else ChatUtils.sendMsg(Component.literal("Illegal Boat located!"));
                                            }
                                        }
                                        LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                        basefound = true;
                                        notifyBaseFound(chunk.getPos());
                                    }
                                } else if (entitieslist.get().contains(entity.getType()) && entityClusterFinder.get()) {
                                    animalsFound.getAndIncrement();
                                }
                            });
                            if (animalsFound.get() >= animalsFoundThreshold.get() && entityClusterFinder.get()){
                                chunkEntityCounts.computeIfAbsent(chunk.getPos(), k -> new HashMap<>()).merge("Entity Cluster", animalsFound.get(), Integer::sum);
                                if (!alreadyFlagged) {
                                    baseChunks.add(chunk.getPos());
                                    addTrigger(chunk.getPos(), "Entity Cluster");
                                    if (save.get()) {
                                        saveBaseChunkData(chunk.getPos());
                                    }
                                    if (basefoundspamTicks == 0) {
                                        if (entityChatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Illegal amount of entities located near X" + chunk.getPos().getMiddleBlockX() + ", Z" + chunk.getPos().getMiddleBlockZ()));
                                            else ChatUtils.sendMsg(Component.literal("Illegal amount of entities located!"));
                                        }
                                    }
                                    LastBaseFound = new ChunkPos(chunk.getPos().x(), chunk.getPos().z());
                                    basefound = true;
                                    notifyBaseFound(chunk.getPos());
                                }
                            }
                        }
                    }
                }
            }
            entityScanTicks = 0;
        }
        if (removerenderdist.get()) removeChunksOutsideRenderDistance();
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        int topY = renderHeightY.get();
        int bottomY = renderHeightYbottom.get();
        int midpoint = (topY + bottomY) / 2;
        BlockPos playerPos = new BlockPos(mc.player.getBlockX(), midpoint, mc.player.getBlockZ());
        if (baseChunksLineColor.get().a > 5 || baseChunksSideColor.get().a > 5){
            if (!nearesttrcr.get()){
                synchronized (baseChunks) {
                    long now = System.currentTimeMillis();
                    pulsingChunks.entrySet().removeIf(e -> now - e.getValue() > PULSE_DURATION_MS);
                    for (ChunkPos c : baseChunks) {
                        if (playerPos.distSqr(new BlockPos(c.getMiddleBlockX(), midpoint, c.getMiddleBlockZ())) <= (double)(renderDistance.get()*16) * (double)(renderDistance.get()*16)) {
                            Long pulseStart = pulsingChunks.get(c);
                            Color sideColor = baseChunksSideColor.get();
                            Color lineColor = baseChunksLineColor.get();
                            ShapeMode mode = ShapeMode.Sides;
                            if (pulseStart != null && now - pulseStart < PULSE_DURATION_MS) {
                                boolean white = ((now - pulseStart) / 100) % 2 == 0;
                                sideColor = white ? new Color(255, 255, 255, 200) : new Color(0, 0, 0, 200);
                                lineColor = white ? Color.WHITE : Color.BLACK;
                                mode = ShapeMode.Both;
                            }
                            render(new AABB(new Vec3(c.getWorldPosition().getX()+7, c.getWorldPosition().getY()+renderHeightYbottom.get(), c.getWorldPosition().getZ()+7), new Vec3(c.getWorldPosition().getX()+8, c.getWorldPosition().getY()+renderHeightY.get(), c.getWorldPosition().getZ()+8)), sideColor, lineColor, mode, event);
                        }
                    }
                }
            } else if (nearesttrcr.get()){
                synchronized (baseChunks) {
                    long now = System.currentTimeMillis();
                    for (ChunkPos c : baseChunks) {
                        if (playerPos.distSqr(new BlockPos(c.getMiddleBlockX(), midpoint, c.getMiddleBlockZ())) <= (double)(renderDistance.get()*16) * (double)(renderDistance.get()*16)) {
                            Long pulseStart = pulsingChunks.get(c);
                            Color sideColor = baseChunksSideColor.get();
                            Color lineColor = baseChunksLineColor.get();
                            ShapeMode mode = ShapeMode.Sides;
                            if (pulseStart != null && now - pulseStart < PULSE_DURATION_MS) {
                                boolean white = ((now - pulseStart) / 100) % 2 == 0;
                                sideColor = white ? new Color(255, 255, 255, 200) : new Color(0, 0, 0, 200);
                                lineColor = white ? Color.WHITE : Color.BLACK;
                                mode = ShapeMode.Both;
                            }
                            render(new AABB(new Vec3(c.getWorldPosition().getX()+7, c.getWorldPosition().getY()+renderHeightYbottom.get(), c.getWorldPosition().getZ()+7), new Vec3(c.getWorldPosition().getX()+8, c.getWorldPosition().getY()+renderHeightY.get(), c.getWorldPosition().getZ()+8)), sideColor, lineColor, mode, event);
                        }
                    }
                }
                render2(new AABB(new Vec3(closestBase.getWorldPosition().getX()+7, closestBase.getWorldPosition().getY()+renderHeightYbottom.get(), closestBase.getWorldPosition().getZ()+7), new Vec3 (closestBase.getWorldPosition().getX()+8, closestBase.getWorldPosition().getY()+renderHeightY.get(), closestBase.getWorldPosition().getZ()+8)), baseChunksSideColor.get(), baseChunksLineColor.get(),ShapeMode.Sides, event);
            }
        }
    }

    private void render(AABB box, Color sides, Color lines, ShapeMode shapeMode, Render3DEvent event) {
        if (trcr.get() && Math.abs(box.minX-RenderUtils.center.x)<=trcrdist.get()*16 && Math.abs(box.minZ-RenderUtils.center.z)<=trcrdist.get()*16)
            if (!nearesttrcr.get())
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, box.minX+0.5, box.minY+((box.maxY-box.minY)/2), box.minZ+0.5, lines);
        event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, new Color(0,0,0,0), shapeMode, 0);
    }
    private void render2(AABB box, Color sides, Color lines, ShapeMode shapeMode, Render3DEvent event) {
        if (trcr.get() && Math.abs(box.minX-RenderUtils.center.x)<=trcrdist.get()*16 && Math.abs(box.minZ-RenderUtils.center.z)<=trcrdist.get()*16)
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, box.minX+0.5, box.minY+((box.maxY-box.minY)/2), box.minZ+0.5, lines);
        event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, new Color(0,0,0,0), shapeMode, 0);
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        if (deactivating) return;
        if (event.packet instanceof ServerboundMovePlayerPacket) return; //this keeps getting cast to the chunkdata for no reason
        if (!(event.packet instanceof ServerboundMovePlayerPacket) && event.packet instanceof ClientboundLevelChunkWithLightPacket packet && mc.level != null) {

            basepos = new ChunkPos(packet.getX(), packet.getZ());

            if (mc.level.getChunkSource().getChunk(packet.getX(), packet.getZ(), false) == null) {
                LevelChunk chunk = new LevelChunk(mc.level, basepos);
                try {
                    Map<Heightmap.Types, long[]> heightmaps = new EnumMap<>(Heightmap.Types.class);

                    Heightmap.Types type = Heightmap.Types.MOTION_BLOCKING;
                    long[] emptyHeightmapData = new long[37];
                    heightmaps.put(type, emptyHeightmapData);

                    if (mc.isSameThread()) {
                        chunk.replaceWithPacketData(packet.getChunkData().getReadBuffer(), heightmaps,
                                packet.getChunkData().getBlockEntitiesTagsConsumer(packet.getX(), packet.getZ()));
                    } else {
                        mc.execute(() -> chunk.replaceWithPacketData(packet.getChunkData().getReadBuffer(), heightmaps,
                                packet.getChunkData().getBlockEntitiesTagsConsumer(packet.getX(), packet.getZ())));
                    }
                } catch (Exception e) {e.printStackTrace();}

                deduplicateBlockLists();
                boolean newlyFound = false;
                if (bubblesFinder.get() || spawner.get() || signFinder.get() || portalFinder.get() || roofDetector.get() || bedrockfind.get() || skybuildfind.get() || !blockSet1.isEmpty() || !blockSet2.isEmpty() || !blockSet3.isEmpty() || !blockSet4.isEmpty() || !blockSet5.isEmpty() || !blockSet6.isEmpty() || !blockSet7.isEmpty()){
                    int Ymin = mc.level.getMinY()+minY.get();
                    int Ymax = mc.level.getMaxY()-maxY.get();
                    try {
                        Set<BlockPos> blockpositions1 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions2 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions3 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions4 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions5 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions6 = Collections.synchronizedSet(new HashSet<>());
                        Set<BlockPos> blockpositions7 = Collections.synchronizedSet(new HashSet<>());
                        int exclusionBlockCount = 0;
                        boolean chunkExcluded = false;
                        if (isInSpawnChunks(basepos)) {
                            chunkExcluded = true;
                        }
                        LevelChunkSection[] sections = chunk.getSections();
                        int Y = mc.level.getMinY();
                        if (!chunkExcluded)
                        for (LevelChunkSection section: sections){
                            if (section == null) {
                                Y+=16;
                                continue;
                            }
                            boolean isNetherRoofSection = mc.level.dimension() == Level.NETHER && Y >= 128;
                            if (section.hasOnlyAir() && !isNetherRoofSection) {
                                Y+=16;
                                continue;
                            }
                            for (int x = 0; x < 16; x++) {
                                for (int y = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        int currentY = Y + y;
                                        if (currentY <= Ymin || currentY >= Ymax) continue;
                                        blockposi=new BlockPos(x, currentY, z);
                                        BlockState blerks = section.getBlockState(x,y,z);
                                        if (exclusionEnabled.get() && !exclusionBlocks.get().isEmpty() && exclusionBlocks.get().contains(blerks.getBlock())) {
                                            exclusionBlockCount++;
                                        }
                                        if (blerks.getBlock()!=Blocks.AIR && blerks.getBlock()!=Blocks.STONE){
                                            if (!(blerks.getBlock()==Blocks.DEEPSLATE) && !(blerks.getBlock()==Blocks.DIRT) && !(blerks.getBlock()==Blocks.GRASS_BLOCK) && !(blerks.getBlock()==Blocks.WATER) && !(blerks.getBlock()==Blocks.SAND) && !(blerks.getBlock()==Blocks.GRAVEL) && !(blerks.getBlock()==Blocks.BEDROCK) && !(blerks.getBlock()==Blocks.NETHERRACK) && !(blerks.getBlock()==Blocks.LAVA)){
                                                if (signFinder.get() && blerks.getBlock() instanceof SignBlock || blerks.getBlock() instanceof HangingSignBlock) {
                                                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                                                        Boolean signtextfound = false;
                                                        if (blockEntity instanceof SignBlockEntity){
                                                            SignText signText = ((SignBlockEntity) blockEntity).getFrontText();
                                                            SignText signText2 = ((SignBlockEntity) blockEntity).getBackText();
                                                            Component[] lines = signText.getMessages(false);
                                                            Component[] lines2 = signText2.getMessages(false);
                                                            int i = 0;
                                                            for (Component line : lines) {
                                                                if (line.getString().length() != 0 && (line.getString() != "<----" && i == 1) && (line.getString() != "---->" && i == 2)){ //handling for arrows is for igloos
                                                                    signtextfound = true;
                                                                    if (signtextfound) break;
                                                                }
                                                                i++;
                                                            }
                                                            for (Component line2 : lines2) {
                                                                if (signtextfound) break;
                                                                if (line2.getString().length() != 0){
                                                                    signtextfound = true;
                                                                    if (signtextfound) break;
                                                                }
                                                            }
                                                        } else if (blockEntity instanceof HangingSignBlockEntity) {
                                                            SignText signText = ((HangingSignBlockEntity) blockEntity).getFrontText();
                                                            SignText signText2 = ((HangingSignBlockEntity) blockEntity).getBackText();
                                                            Component[] lines = signText.getMessages(false);
                                                            Component[] lines2 = signText2.getMessages(false);
                                                            for (Component line : lines) {
                                                                if (line.getString().length() != 0){ //handling for arrows is for igloos
                                                                    signtextfound = true;
                                                                    if (signtextfound) break;
                                                                }
                                                            }
                                                            for (Component line2 : lines2) {
                                                                if (signtextfound) break;
                                                                if (line2.getString().length() != 0){
                                                                    signtextfound = true;
                                                                    if (signtextfound) break;
                                                                }
                                                            }
                                                        }
                                                        if (signtextfound && !baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                            baseChunks.add(basepos);
                                                            addTrigger(basepos, "Sign");
                                                            if (save.get()) {
                                                                saveBaseChunkData(basepos);
                                                            }
                                                            if (basefoundspamTicks==0){
                                                                if (chatFeedback.get()){
                                                                    if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Written Sign located near X"+blockEntity.getBlockPos().getX()+", Y"+blockEntity.getBlockPos().getY()+", Z"+blockEntity.getBlockPos().getZ()));
                                                                    else ChatUtils.sendMsg(Component.literal("Written Sign located!"));
                                                                }
                                                                LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                            basefound=true;
                            newlyFound=true;
                                        newlyFound=true;
                                                            }
                                                        }
                                                    }
                                                }
                                                if (skybuildfind.get() && currentY>skybuildint.get()) {
                                                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                        baseChunks.add(basepos);
                                                        addTrigger(basepos, "Sky Build");
                                                        if (save.get()) {
                                                            saveBaseChunkData(basepos);
                                                        }
                                                        if (basefoundspamTicks==0){
                                                            if (chatFeedback.get()) {
                                                                if (displaycoords.get()) ChatUtils.sendMsg(Component.literal("Sky build located near X" + basepos.getMiddleBlockX() + ", Z" + basepos.getMiddleBlockZ()));
                                                                else ChatUtils.sendMsg(Component.literal("Sky build located!"));
                                                            }
                                                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                                            basefound=true;
                                                            newlyFound=true;
                                                        }
                                                    }
                                                }
                                                if (bubblesFinder.get() && blerks.getBlock() instanceof BubbleColumnBlock && !blerks.getValue(BubbleColumnBlock.DRAG_DOWN)) {
                                                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                        baseChunks.add(basepos);
                                                        addTrigger(basepos, "Bubble Column");
                                                        if (save.get()) {
                                                            saveBaseChunkData(basepos);
                                                        }
                                                        if (basefoundspamTicks==0){
                                                            if (chatFeedback.get()) {
                                                                if (displaycoords.get()) ChatUtils.sendMsg(Component.literal("Bubble column located near X" + basepos.getMiddleBlockX() + ", Z" + basepos.getMiddleBlockZ()));
                                                                else ChatUtils.sendMsg(Component.literal("Bubble column located!"));
                                                            }
                                                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                                            basefound=true;
                                                            newlyFound=true;
                                                        }
                                                    }
                                                }
                                                if (portalFinder.get() && (blerks.getBlock()==Blocks.NETHER_PORTAL || blerks.getBlock()==Blocks.END_PORTAL)) {
                                                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                        baseChunks.add(basepos);
                                                        addTrigger(basepos, "Portal");
                                                        if (save.get()) {
                                                            saveBaseChunkData(basepos);
                                                        }
                                                        if (basefoundspamTicks==0){
                                                            if (chatFeedback.get()) {
                                                                if (displaycoords.get()) ChatUtils.sendMsg(Component.literal("Open portal located near X" + basepos.getMiddleBlockX() + ", Z" + basepos.getMiddleBlockZ()));
                                                                else ChatUtils.sendMsg(Component.literal("Open portal located!"));
                                                            }
                                                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                                            basefound=true;
                                                            newlyFound=true;
                                                        }
                                                    }
                                                }
                                                if (bedrockfind.get() && blerks.getBlock()==Blocks.BEDROCK && ((currentY>mc.level.getMinY()+bedrockint.get() && mc.level.dimension() == Level.OVERWORLD) || (currentY>mc.level.getMinY()+bedrockint.get() && (currentY < 123 || currentY > 127) && mc.level.dimension() == Level.NETHER))) {
                                                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                        baseChunks.add(basepos);
                                                        addTrigger(basepos, "Bedrock");
                                                        if (save.get()) {
                                                            saveBaseChunkData(basepos);
                                                        }
                                                        if (basefoundspamTicks==0){
                                                            if (chatFeedback.get()) {
                                                                if (displaycoords.get()) ChatUtils.sendMsg(Component.literal("Unnatural bedrock located near X" + basepos.getMiddleBlockX() + ", Z" + basepos.getMiddleBlockZ()));
                                                                else ChatUtils.sendMsg(Component.literal("Unnatural bedrock located!"));
                                                            }
                                                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                                            basefound=true;
                                                            newlyFound=true;
                                                        }
                                                    }
                                                }
                                                if (roofDetector.get() && blerks.getBlock()!=Blocks.RED_MUSHROOM && blerks.getBlock()!=Blocks.BROWN_MUSHROOM && currentY>=128 && mc.level.dimension() == Level.NETHER){
                                                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                                        baseChunks.add(basepos);
                                                        addTrigger(basepos, "Nether Roof");
                                                        if (save.get()) {
                                                            saveBaseChunkData(basepos);
                                                        }
                                                        if (basefoundspamTicks==0){
                                                            if (chatFeedback.get()) {
                                                                if (displaycoords.get()) ChatUtils.sendMsg(Component.literal("Nether roof build located near X" + basepos.getMiddleBlockX() + ", Z" + basepos.getMiddleBlockZ()));
                                                                else ChatUtils.sendMsg(Component.literal("Nether roof build located!"));
                                                            }
                                                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                                            basefound=true;
                                                            newlyFound=true;
                                                        }
                                                    }
                                                }
                                                if (spawner.get()){
                                                    if (blerks.getBlock()==Blocks.SPAWNER){
                                                        spawnerY=currentY;
                                                        spawnerfound=true;
                                                    }
                                                    //dungeon MOSSY_COBBLESTONE, mineshaft COBWEB, fortress NETHER_BRICK_FENCE, stronghold STONE_BRICK_STAIRS, bastion CHAIN
                                                    if (mc.level.dimension() == Level.OVERWORLD && (blerks.getBlock()==Blocks.MOSSY_COBBLESTONE || blerks.getBlock()==Blocks.COBWEB || blerks.getBlock()==Blocks.STONE_BRICK_STAIRS || blerks.getBlock()==Blocks.BUDDING_AMETHYST))spawnernaturalblocks=true;
                                                    else if (mc.level.dimension() == Level.NETHER && (blerks.getBlock()==Blocks.NETHER_BRICK_FENCE || blerks.getBlock()==Blocks.IRON_CHAIN))spawnernaturalblocks=true;
                                                }
                                                boolean isMultiBlockSecondary = (blerks.getBlock() instanceof BedBlock && blerks.getValue(BedBlock.PART) != BedPart.FOOT)
                                                    || (blerks.getBlock() instanceof DoorBlock && blerks.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER);
                                                if (!isMultiBlockSecondary) {
                                                    if (list1Activar.get() && !blockSet1.isEmpty()){
                                                    if (blockSet1.contains(blerks.getBlock())) {
                                                        blockpositions1.add(blockposi);
                                                        found1= blockpositions1.size();
                                                        lastblockfound1=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list2Activar.get() && !blockSet2.isEmpty()){
                                                    if (blockSet2.contains(blerks.getBlock())) {
                                                        blockpositions2.add(blockposi);
                                                        found2= blockpositions2.size();
                                                        lastblockfound2=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list3Activar.get() && !blockSet3.isEmpty()){
                                                    if (blockSet3.contains(blerks.getBlock())) {
                                                        blockpositions3.add(blockposi);
                                                        found3= blockpositions3.size();
                                                        lastblockfound3=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list4Activar.get() && !blockSet4.isEmpty()){
                                                    if (blockSet4.contains(blerks.getBlock())) {
                                                        blockpositions4.add(blockposi);
                                                        found4= blockpositions4.size();
                                                        lastblockfound4=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list5Activar.get() && !blockSet5.isEmpty()){
                                                    if (blockSet5.contains(blerks.getBlock())) {
                                                        blockpositions5.add(blockposi);
                                                        found5= blockpositions5.size();
                                                        lastblockfound5=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list6Activar.get() && !blockSet6.isEmpty()){
                                                    if (blockSet6.contains(blerks.getBlock())) {
                                                        blockpositions6.add(blockposi);
                                                        found6= blockpositions6.size();
                                                        lastblockfound6=blerks.getBlock().toString();
                                                    }
                                                }
                                                if (list7Activar.get() && !blockSet7.isEmpty()){
                                                    if (blockSet7.contains(blerks.getBlock())) {
                                                        blockpositions7.add(blockposi);
                                                        found7= blockpositions7.size();
                                                        lastblockfound7=blerks.getBlock().toString();
                                                    }
                                                }
                                                if ((list1Activar.get() && !blockSet1.isEmpty() && blockSet1.contains(blerks.getBlock()))
                                                    || (list2Activar.get() && !blockSet2.isEmpty() && blockSet2.contains(blerks.getBlock()))
                                                    || (list3Activar.get() && !blockSet3.isEmpty() && blockSet3.contains(blerks.getBlock()))
                                                    || (list4Activar.get() && !blockSet4.isEmpty() && blockSet4.contains(blerks.getBlock()))
                                                    || (list5Activar.get() && !blockSet5.isEmpty() && blockSet5.contains(blerks.getBlock()))
                                                    || (list6Activar.get() && !blockSet6.isEmpty() && blockSet6.contains(blerks.getBlock()))
                                                    || (list7Activar.get() && !blockSet7.isEmpty() && blockSet7.contains(blerks.getBlock()))) {
                                                    chunkBlockCounts.computeIfAbsent(basepos, k -> new HashMap<>()).merge(blerks.getBlock(), 1, Integer::sum);
                                                }
                                                }
                                            }
                                        }
                                        if (!blockSet1.isEmpty())checkingchunk1=true;
                                        if (!blockSet2.isEmpty())checkingchunk2=true;
                                        if (!blockSet3.isEmpty())checkingchunk3=true;
                                        if (!blockSet4.isEmpty())checkingchunk4=true;
                                        if (!blockSet5.isEmpty())checkingchunk5=true;
                                        if (!blockSet6.isEmpty())checkingchunk6=true;
                                        if (!blockSet7.isEmpty())checkingchunk7=true;
                                    }
                                }
                            }
                            Y+=16;
                        }
                        if (exclusionEnabled.get() && exclusionBlockCount >= exclusionMinCount.get()) {
                            chunkExcluded = true;
                        }
                        if (chunkExcluded) {
                            blockpositions1.clear(); blockpositions2.clear(); blockpositions3.clear();
                            blockpositions4.clear(); blockpositions5.clear(); blockpositions6.clear();
                            blockpositions7.clear();
                            found1=0; found2=0; found3=0; found4=0; found5=0; found6=0; found7=0;
                            checkingchunk1=false; checkingchunk2=false; checkingchunk3=false;
                            checkingchunk4=false; checkingchunk5=false; checkingchunk6=false; checkingchunk7=false;
                            spawnerfound=false; spawnernaturalblocks=false;
                        }
                        if (!chunkExcluded) {
                        //CheckList 1
                        if (!blockSet1.isEmpty()){
                            if (checkingchunk1 && found1>=blowkfind1.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List1)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions1.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound1 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List1)Possible build located! (" + lastblockfound1 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 1");
                                blockpositions1.clear();
                                found1 = 0;
                                checkingchunk1=false;
                            } else if (checkingchunk1 && found1<blowkfind1.get()){
                                blockpositions1.clear();
                                found1 = 0;
                                checkingchunk1=false;
                            }
                        }

                        //CheckList 2
                        if (!blockSet2.isEmpty()){
                            if (checkingchunk2 && found2>=blowkfind2.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List2)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions2.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound2 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List2)Possible build located! (" + lastblockfound2 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 2");
                                blockpositions2.clear();
                                found2 = 0;
                                checkingchunk2=false;
                            } else if (checkingchunk2 && found2<blowkfind2.get()){
                                blockpositions2.clear();
                                found2 = 0;
                                checkingchunk2=false;
                            }
                        }

                        //CheckList 3
                        if (!blockSet3.isEmpty()){
                            if (checkingchunk3 && found3>=blowkfind3.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List3)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions3.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound3 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List3)Possible build located! (" + lastblockfound3 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 3");
                                blockpositions3.clear();
                                found3 = 0;
                                checkingchunk3=false;
                            } else if (checkingchunk3 && found3<blowkfind3.get()){
                                blockpositions3.clear();
                                found3 = 0;
                                checkingchunk3=false;
                            }
                        }

                        //CheckList 4
                        if (!blockSet4.isEmpty()){
                            if (checkingchunk4 && found4>=blowkfind4.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List4)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions4.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound4 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List4)Possible build located! (" + lastblockfound4 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 4");
                                blockpositions4.clear();
                                found4 = 0;
                                checkingchunk4=false;
                            } else if (checkingchunk4 && found4<blowkfind4.get()){
                                blockpositions4.clear();
                                found4 = 0;
                                checkingchunk4=false;
                            }
                        }

                        //CheckList 5
                        if (!blockSet5.isEmpty()){
                            if (checkingchunk5 && found5>=blowkfind5.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List5)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions5.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound5 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List5)Possible build located! (" + lastblockfound5 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 5");
                                blockpositions5.clear();
                                found5 = 0;
                                checkingchunk5=false;
                            } else if (checkingchunk5 && found5<blowkfind5.get()){
                                blockpositions5.clear();
                                found5 = 0;
                                checkingchunk5=false;
                            }
                        }

                        //CheckList 6
                        if (!blockSet6.isEmpty()){
                            if (checkingchunk6 && found6>=blowkfind6.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List6)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions6.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound6 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List6)Possible build located! (" + lastblockfound6 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 6");
                                blockpositions6.clear();
                                found6 = 0;
                                checkingchunk6=false;
                            } else if (checkingchunk6 && found6<blowkfind6.get()){
                                blockpositions6.clear();
                                found6 = 0;
                                checkingchunk6=false;
                            }
                        }

                        //CheckList 7
                        if (!blockSet7.isEmpty()){
                            if (checkingchunk7 && found7>=blowkfind7.get()) {
                                if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                                    baseChunks.add(basepos);
                                    if (save.get()) {
                                        saveBaseChunkData(basepos);
                                    }
                                    if (basefoundspamTicks== 0) {
                                        if (chatFeedback.get()){
                                            if (displaycoords.get())ChatUtils.sendMsg(Component.literal("(List7)Possible build located near X" + basepos.getMiddleBlockX() + ", Y" + blockpositions7.stream().toList().get(0).getY() + ", Z" + basepos.getMiddleBlockZ() + " (" + lastblockfound7 + ")"));
                                            else ChatUtils.sendMsg(Component.literal("(List7)Possible build located! (" + lastblockfound7 + ")"));
                                        }
                                        LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                                        basefound=true;
                                        newlyFound=true;
                                    }
                                }
                                addTrigger(basepos, "List 7");
                                blockpositions7.clear();
                                found7 = 0;
                                checkingchunk7=false;
                            } else if (checkingchunk7 && found7<blowkfind7.get()){
                                blockpositions7.clear();
                                found7 = 0;
                                checkingchunk7=false;
                            }
                        }
                        } // end !chunkExcluded
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if (spawnerfound && !spawnernaturalblocks){
                    if (!baseChunks.contains(basepos) && !suppressedChunks.contains(basepos)){
                        baseChunks.add(basepos);
                        addTrigger(basepos, "Spawner");
                        if (save.get()) {
                            saveBaseChunkData(basepos);
                        }
                        if (basefoundspamTicks== 0) {
                            if (chatFeedback.get()){
                                if (displaycoords.get())ChatUtils.sendMsg(Component.literal("Possible modified spawner located near X"+basepos.getMiddleBlockX()+", Y"+spawnerY+", Z"+basepos.getMiddleBlockZ()));
                                else ChatUtils.sendMsg(Component.literal("Possible modified spawner located!"));
                            }
                            LastBaseFound= new ChunkPos(basepos.x(), basepos.z());
                            basefound=true;
                        }
                    }
                    spawnerfound=false;
                    spawnernaturalblocks=false;
                } else if ((spawnerfound && spawnernaturalblocks) || (!spawnerfound && spawnernaturalblocks) || (!spawnerfound && !spawnernaturalblocks)){
                    spawnerfound=false;
                    spawnernaturalblocks=false;
                }
                if (newlyFound && baseNotifier.get()) {
                    MutableComponent openGuiBtn = Component.literal("[Open GUI]")
                        .setStyle(Style.EMPTY
                            .withColor(ChatFormatting.DARK_GRAY)
                            .withUnderlined(true)
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Open flagged chunks GUI")))
                            .withClickEvent(new ClickEvent.RunCommand(".openflaggedchunks")));
                    MutableComponent message = Component.literal("Base was found ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(openGuiBtn);
                    ChatUtils.sendMsg(message);
                }
            }
        }
    }
    private void loadData() {
        Path baseDir = FabricLoader.getInstance().getGameDir()
                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
        Path filePath = baseDir.resolve("BaseChunkData.txt");

        try {
            if (!Files.exists(filePath)) return;
            List<String> allLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

            for (String line : allLines) {
                String s = line;
                String[] array = s.split(",");
                int X = Integer.parseInt(array[0].trim());
                int Z = Integer.parseInt(array[1].trim());
                basepos = new ChunkPos(X, Z);
                baseChunks.add(basepos);
                addTrigger(basepos, "Loaded from File");
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void saveBaseChunkData(ChunkPos basepos) {
        taskExecutor.submit(() -> {
            try {
                Path baseDir = FabricLoader.getInstance().getGameDir()
                        .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                Files.createDirectories(baseDir);
                Path filePath = baseDir.resolve("BaseChunkData.txt");
                String data = basepos.x() + "," + basepos.z() + System.lineSeparator();
                Files.write(filePath, data.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private boolean filterBlocks(Block block) {
        return isNaturalLagCausingBlock(block);
    }
    private boolean isNaturalLagCausingBlock(Block block) {
        return  block instanceof Block &&
                !(block ==Blocks.AIR) &&
                !(block ==Blocks.STONE) &&
                !(block ==Blocks.DIRT) &&
                !(block ==Blocks.GRASS_BLOCK) &&
                !(block ==Blocks.SAND) &&
                !(block ==Blocks.GRAVEL) &&
                !(block ==Blocks.DEEPSLATE) &&
                !(block ==Blocks.WATER) &&
                !(block ==Blocks.NETHERRACK) &&
                !(block ==Blocks.LAVA);
    }
    private void removeChunksOutsideRenderDistance() {
        int topY = renderHeightY.get();
        int bottomY = renderHeightYbottom.get();
        int midpoint = (topY + bottomY) / 2;
        BlockPos playerPos = new BlockPos(mc.player.getBlockX(), midpoint, mc.player.getBlockZ());
        double renderDistanceBlocks = renderDistance.get() * 16;

        removeChunksOutsideRenderDistance(baseChunks, playerPos, renderDistanceBlocks, midpoint);
        if (!(playerPos.distSqr(new BlockPos(closestBase.getMiddleBlockX(), midpoint, closestBase.getMiddleBlockZ())) <= renderDistanceBlocks * renderDistanceBlocks))
            closestBase = new ChunkPos(2000000000, 2000000000);
    }
    private void removeChunksOutsideRenderDistance(Set<ChunkPos> chunkSet, BlockPos playerPos, double renderDistanceBlocks, int midpoint) {
        List<ChunkPos> chunksToRemove = new ArrayList<>();
        for (ChunkPos c : chunkSet) {
            if (!(playerPos.distSqr(new BlockPos(c.getMiddleBlockX(), midpoint, c.getMiddleBlockZ())) <= renderDistanceBlocks * renderDistanceBlocks)) {
                chunksToRemove.add(c);
            }
        }
        chunkSet.removeAll(chunksToRemove);
    }

    private final List<LoggedBase> loggedBases = new ArrayList<>();
    private final Set<ChunkPos> loggedBasePositions = new HashSet<>();
    private static final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        for (ChunkPos pos : new ArrayList<>(baseChunks)) {
            if (!loggedBasePositions.contains(pos) && (locLogging.get() || locLoggingCSV.get())) {
                loggedBasePositions.add(pos);
                int x = pos.getMiddleBlockX();
                int z = pos.getMiddleBlockZ();
                int y = (renderHeightY.get() + renderHeightYbottom.get()) / 2;
                loggedBases.add(new LoggedBase(x, y, z));
                if (locLogging.get()) saveJsonLog();
                if (locLoggingCSV.get()) saveCsvLog();
            }
        }

        if (autoChunkDelete.get() && mc.player != null) {
            handleAutoChunkDelete();
        }

        purgeUnloadedSuppressedChunks();
    }

    /**
     * Removes any suppressed LevelChunk from tracking the moment it's no longer
     * loaded on the client. This is what makes suppression temporary instead
     * of permanent - once a LevelChunk unloads, all memory of it having been
     * auto-deleted is gone, so it scans completely fresh if it loads again.
     */
    private void purgeUnloadedSuppressedChunks() {
        if (mc.level == null || suppressedChunks.isEmpty()) return;

        List<ChunkPos> toPurge = new ArrayList<>();
        for (ChunkPos pos : new ArrayList<>(suppressedChunks)) {
            if (!mc.level.getChunkSource().hasChunk(pos.x(), pos.z())) {
                toPurge.add(pos);
            }
        }
        suppressedChunks.removeAll(toPurge);
    }

    /**
     * Checks every currently flagged LevelChunk independently: if the player is
     * within Delete radius of a flagged LevelChunk, its progress counter
     * increments; otherwise it resets to 0. Chunks that reach the full
     * Delete time (in ticks) get removed. Multiple chunks can be in
     * range and progressing/deleting at the same time — only chunks the
     * player is actually within radius of are ever touched.
     */
    private void handleAutoChunkDelete() {
        ChunkPos playerChunk = mc.player.chunkPosition();
        int radius = chunkDeleteRadius.get();
        int delayTicks = chunkDeleteDelay.get() * 20;

        List<ChunkPos> toDelete = new ArrayList<>();

        for (ChunkPos pos : new ArrayList<>(baseChunks)) {
            int dx = pos.x() - playerChunk.x();
            int dz = pos.z() - playerChunk.z();
            double dist = Math.sqrt((double) dx * dx + (double) dz * dz);

            if (dist <= radius) {
                int progress = chunkDeleteProgress.getOrDefault(pos, 0) + 1;
                if (progress >= delayTicks) {
                    toDelete.add(pos);
                } else {
                    chunkDeleteProgress.put(pos, progress);
                }
            } else {
                chunkDeleteProgress.remove(pos);
            }
        }

        for (ChunkPos pos : toDelete) {
            deleteBaseChunk(pos);
        }
    }

    /**
     * Fully removes a flagged LevelChunk: from baseChunks, its trigger reasons,
     * its block counts, its delete-progress tracking, and rewrites the save
     * file if Save is enabled — mirroring the manual RemoveBase button but
     * also cleaning up trigger/block-count data instead of leaving it stale.
     */
    private void deleteBaseChunk(ChunkPos pos) {
        baseChunks.remove(pos);
        chunkTriggerReasons.remove(pos);
        chunkBlockCounts.remove(pos);
        chunkEntityCounts.remove(pos);
        chunkDeleteProgress.remove(pos);
        suppressedChunks.add(pos);

        if (save.get()) {
            try {
                Path baseDir = FabricLoader.getInstance().getGameDir()
                        .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
                Files.createDirectories(baseDir);
                Path filePath = baseDir.resolve("BaseChunkData.txt");
                Files.deleteIfExists(filePath);
                List<String> chunkDataLines = baseChunks.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, chunkDataLines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveCsvLog() {
        try {
            File file = getCsvFile();
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            writer.write("X,Y,Z\n");
            for(LoggedBase lb : loggedBases) {
                lb.write(writer);
            }
            writer.close();
        } catch (IOException e) {e.printStackTrace();}
    }

    private void saveJsonLog() {
        try {
            File file = getJsonFile();
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            gson.toJson(loggedBases, writer);
            writer.close();
        } catch (IOException e) {e.printStackTrace();}
    }
    private File getJsonFile() {
        Path baseDir = FabricLoader.getInstance().getGameDir()
                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
        return baseDir.resolve("bases.json").toFile();
    }

    private File getCsvFile() {
        Path baseDir = FabricLoader.getInstance().getGameDir()
                .resolve("ChuckPack").resolve("BaseChunks").resolve(serverip).resolve(worldName);
        return baseDir.resolve("bases.csv").toFile();
    }

    private static class LoggedBase {
        public int x;
        public int y;
        public int z;
        public LoggedBase(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public void write(java.io.Writer writer) throws java.io.IOException {
            writer.write(x + "," + y + "," + z + "\n");
        }
        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            LoggedBase that = (LoggedBase) o;
            return x == that.x && y == that.y && z == that.z;
        }
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}