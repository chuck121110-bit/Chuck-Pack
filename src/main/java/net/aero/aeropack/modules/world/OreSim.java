/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.modules.world;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.aero.aeropack.util.config.Ore;
import net.aero.aeropack.util.config.Seeds;
import net.aero.aeropack.util.config.Seeds.Seed;
import net.aero.aeropack.util.config.Seeds.SeedChangedEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OreSim extends Module {
    private final Map<Long, Map<Ore, Set<Vec3>>> chunkRenderers = new ConcurrentHashMap<>();
    private Seed worldSeed;
    private Map<ResourceKey<Biome>, List<Ore>> oreConfig;
    private String lastWorldName;
    private ResourceKey<Level> lastWorldKey;

    public enum AirCheck {
        ON_LOAD,
        RECHECK,
        OFF
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("LevelChunk-range")
        .description("Range of chunks to render around the player.")
        .defaultValue(5)
        .min(1)
        .sliderMax(10)
        .build());

    private final Setting<AirCheck> airCheck = sgGeneral.add(new EnumSetting.Builder<AirCheck>()
        .name("air-check-mode")
        .description("Checks for air blocks when validating simulated ore positions.")
        .defaultValue(AirCheck.RECHECK)
        .build());

    public OreSim() {
        super(meteordevelopment.meteorclient.systems.modules.Categories.Render, "ore-sim", "Simulates vanilla ore generation using the Level seed.");
        SettingGroup sgOres = settings.createGroup("Ores");
        Ore.oreSettings.forEach(sgOres::add);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || oreConfig == null) return;
        if (Seeds.get().getSeed() == null) return;

        int chunkX = mc.player.chunkPosition().x();
        int chunkZ = mc.player.chunkPosition().z();
        int rangeVal = horizontalRadius.get();

        for (int range = 0; range <= rangeVal; range++) {
            for (int x = -range + chunkX; x <= range + chunkX; x++) {
                renderChunk(x, chunkZ + range - rangeVal, event);
            }
            for (int x = -range + 1 + chunkX; x < range + chunkX; x++) {
                renderChunk(x, chunkZ - range + rangeVal + 1, event);
            }
        }
    }

    private void renderChunk(int x, int z, Render3DEvent event) {
        long chunkKey = ChunkPos.pack(x, z);
        Map<Ore, Set<Vec3>> LevelChunk = chunkRenderers.get(chunkKey);
        if (LevelChunk == null) return;

        for (Map.Entry<Ore, Set<Vec3>> entry : LevelChunk.entrySet()) {
            Ore ore = entry.getKey();
            if (!ore.active.get()) continue;
            for (Vec3 pos : entry.getValue()) {
                event.renderer.boxLines(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 1, pos.z + 1, ore.color, 0);
            }
        }
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (airCheck.get() != AirCheck.RECHECK || event.newState.canOcclude()) return;
        long chunkKey = ChunkPos.pack(event.pos);
        Map<Ore, Set<Vec3>> LevelChunk = chunkRenderers.get(chunkKey);
        if (LevelChunk == null) return;
        Vec3 pos = new Vec3(event.pos.getX(), event.pos.getY(), event.pos.getZ());
        for (Set<Vec3> ores : LevelChunk.values()) {
            ores.remove(pos);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || oreConfig == null) return;

        detectWorldChange();
    }

    @Override
    public void onActivate() {
        if (Seeds.get().getSeed() == null) {
            error("No seed found. Run .seed <seed> to set one.");
            toggle();
            return;
        }
        updateWorldTracking();
        reload();
    }

    @Override
    public void onDeactivate() {
        chunkRenderers.clear();
        oreConfig = null;
        lastWorldName = null;
        lastWorldKey = null;
    }

    @EventHandler
    private void onSeedChanged(SeedChangedEvent event) {
        reload();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        calculateChunk(event.chunk());
    }

    private void reload() {
        Seed seed = Seeds.get().getSeed();
        if (seed == null) return;
        worldSeed = seed;
        oreConfig = Ore.getRegistry(PlayerUtils.getDimension());
        chunkRenderers.clear();
        if (mc.level != null) {
            loadVisibleChunks();
        }
    }

    private void detectWorldChange() {
        if (mc.level == null) return;
        String currentWorld = Utils.getWorldName();
        ResourceKey<Level> currentKey = mc.level.dimension();
        if (!Objects.equals(currentWorld, lastWorldName) || !Objects.equals(currentKey, lastWorldKey)) {
            lastWorldName = currentWorld;
            lastWorldKey = currentKey;
            reload();
        }
    }

    private void updateWorldTracking() {
        if (mc.level == null) {
            lastWorldName = null;
            lastWorldKey = null;
        } else {
            lastWorldName = Utils.getWorldName();
            lastWorldKey = mc.level.dimension();
        }
    }

    private void loadVisibleChunks() {
        if (mc.player == null) return;
        for (var chunk : Utils.chunks(false)) {
            if (chunk instanceof LevelChunk lc) calculateChunk(lc);
        }
    }

    private void calculateChunk(LevelChunk chunk) {
        if (chunk == null || mc.level == null || oreConfig == null || worldSeed == null) return;

        ChunkPos chunkPos = chunk.getPos();
        long chunkKey = chunkPos.pack();
        if (chunkRenderers.containsKey(chunkKey)) return;

        Set<ResourceKey<Biome>> Biomes = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos neighborPos = new ChunkPos(chunkPos.x() + dx, chunkPos.z() + dz);
                ChunkAccess ca = mc.level.getChunk(neighborPos.x(), neighborPos.z(), ChunkStatus.BIOMES, false);
                if (!(ca instanceof LevelChunk neighbour)) continue;
                for (LevelChunkSection section : neighbour.getSections()) {
                    section.getBiomes().getAll(entry -> entry.unwrapKey().ifPresent(Biomes::add));
                }
            }
        }

        Set<Ore> ores = Biomes.stream()
            .flatMap(biome -> getOresForBiome(biome).stream())
            .collect(Collectors.toSet());

        int chunkX = chunkPos.x() << 4;
        int chunkZ = chunkPos.z() << 4;
        RandomSource random = RandomSource.create();
        long populationSeed = random.nextLong();

        Map<Ore, Set<Vec3>> orePositions = new HashMap<>();
        for (Ore ore : ores) {
            HashSet<Vec3> positions = new HashSet<>();
            random.setSeed(populationSeed + ore.index * 6364136223846793005L + ore.step * 1442695040888963407L);
            int repeat = ore.count.sample(random);

            for (int i = 0; i < repeat; i++) {
                if (ore.rarity != 1.0F && random.nextFloat() >= 1.0F / ore.rarity) continue;

                int x = random.nextInt(16) + chunkX;
                int z = random.nextInt(16) + chunkZ;
                int y = ore.heightProvider.sample(random, ore.placementCtx);
                BlockPos origin = new BlockPos(x, y, z);

                ResourceKey<Biome> biome = chunk.getNoiseBiome(x, y, z).unwrapKey().get();
                if (!getOresForBiome(biome).contains(ore)) continue;

                if (ore.scattered) {
                    positions.addAll(generateHidden(mc.level, random, origin, ore.size));
                } else {
                    positions.addAll(generateNormal(mc.level, random, origin, ore.size, ore.discardOnAirChance));
                }
            }

            if (!positions.isEmpty()) {
                orePositions.put(ore, positions);
            }
        }

        if (!orePositions.isEmpty()) {
            chunkRenderers.put(chunkKey, orePositions);
        }
    }

    private List<Ore> getOresForBiome(ResourceKey<Biome> biomeKey) {
        if (oreConfig == null) return Collections.emptyList();
        List<Ore> ores = oreConfig.get(biomeKey);
        if (ores != null) return ores;
        return oreConfig.values().stream().findAny().orElse(Collections.emptyList());
    }

    private List<Vec3> generateNormal(ClientLevel Level, RandomSource random, BlockPos blockPos, int veinSize, float discardOnAir) {
        List<Vec3> positions = new ArrayList<>();
        float angle = random.nextFloat() * (float) Math.PI;
        float spread = (float) veinSize / 8.0F;
        int padding = Mth.ceil(((float) veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
        double startX = blockPos.getX() + Math.sin(angle) * spread;
        double endX = blockPos.getX() - Math.sin(angle) * spread;
        double startZ = blockPos.getZ() + Math.cos(angle) * spread;
        double endZ = blockPos.getZ() - Math.cos(angle) * spread;
        double startY = blockPos.getY() + random.nextInt(3) - 2;
        double endY = blockPos.getY() + random.nextInt(3) - 2;
        int minX = blockPos.getX() - Mth.ceil(spread) - padding;
        int minY = blockPos.getY() - 2 - padding;
        int minZ = blockPos.getZ() - Mth.ceil(spread) - padding;
        int sizeX = 2 * (Mth.ceil(spread) + padding);
        int sizeY = 2 * (2 + padding);

        for (int x = minX; x <= minX + sizeX; x++) {
            for (int z = minZ; z <= minZ + sizeX; z++) {
                if (minY <= Level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)) {
                    return generateVein(Level, random, veinSize, startX, endX, startZ, endZ, startY, endY, minX, minY, minZ, sizeX, sizeY, discardOnAir);
                }
            }
        }

        return positions;
    }

    private List<Vec3> generateVein(ClientLevel Level, RandomSource random, int veinSize, double startX, double endX, double startZ, double endZ, double startY, double endY, int minX, int minY, int minZ, int sizeX, int sizeY, float discardOnAir) {
        BitSet bitSet = new BitSet(sizeX * sizeY * sizeX);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double[] buffer = new double[veinSize * 4];
        List<Vec3> positions = new ArrayList<>();

        for (int i = 0; i < veinSize; i++) {
            float progress = (float) i / (float) veinSize;
            double x = Mth.lerp(progress, startX, endX);
            double y = Mth.lerp(progress, startY, endY);
            double z = Mth.lerp(progress, startZ, endZ);
            double scale = random.nextDouble() * veinSize / 16.0D;
            double radius = (Mth.sin((float) Math.PI * progress) + 1.0F) * scale + 1.0D;
            buffer[i * 4] = x;
            buffer[i * 4 + 1] = y;
            buffer[i * 4 + 2] = z;
            buffer[i * 4 + 3] = radius / 2.0D;
        }

        for (int i = 0; i < veinSize - 1; i++) {
            if (buffer[i * 4 + 3] <= 0.0D) continue;
            for (int j = i + 1; j < veinSize; j++) {
                if (buffer[j * 4 + 3] <= 0.0D) continue;
                double dx = buffer[i * 4] - buffer[j * 4];
                double dy = buffer[i * 4 + 1] - buffer[j * 4 + 1];
                double dz = buffer[i * 4 + 2] - buffer[j * 4 + 2];
                double dr = buffer[i * 4 + 3] - buffer[j * 4 + 3];
                if (dr * dr > dx * dx + dy * dy + dz * dz) {
                    if (dr > 0.0D) buffer[j * 4 + 3] = -1.0D;
                    else buffer[i * 4 + 3] = -1.0D;
                }
            }
        }

        for (int i = 0; i < veinSize; i++) {
            double radius = buffer[i * 4 + 3];
            if (radius < 0.0D) continue;
            double centerX = buffer[i * 4];
            double centerY = buffer[i * 4 + 1];
            double centerZ = buffer[i * 4 + 2];
            int minBlockX = Math.max(Mth.floor(centerX - radius), minX);
            int minBlockY = Math.max(Mth.floor(centerY - radius), minY);
            int minBlockZ = Math.max(Mth.floor(centerZ - radius), minZ);
            int maxBlockX = Math.max(Mth.floor(centerX + radius), minBlockX);
            int maxBlockY = Math.max(Mth.floor(centerY + radius), minBlockY);
            int maxBlockZ = Math.max(Mth.floor(centerZ + radius), minBlockZ);

            for (int x = minBlockX; x <= maxBlockX; x++) {
                double normX = ((double) x + 0.5D - centerX) / radius;
                if (normX * normX >= 1.0D) continue;
                for (int y = minBlockY; y <= maxBlockY; y++) {
                    double normY = ((double) y + 0.5D - centerY) / radius;
                    if (normX * normX + normY * normY >= 1.0D) continue;
                    for (int z = minBlockZ; z <= maxBlockZ; z++) {
                        double normZ = ((double) z + 0.5D - centerZ) / radius;
                        if (normX * normX + normY * normY + normZ * normZ >= 1.0D) continue;
                        int index = x - minX + (y - minY) * sizeX + (z - minZ) * sizeX * sizeY;
                        if (bitSet.get(index)) continue;
                        bitSet.set(index);
                        mutable.set(x, y, z);
                        if (y < -64 || y >= 320) continue;
                        if (airCheck.get() != AirCheck.OFF && !Level.getBlockState(mutable).canOcclude()) continue;
                        if (shouldPlace(Level, mutable, discardOnAir, random)) {
                            positions.add(new Vec3(x, y, z));
                        }
                    }
                }
            }
        }

        return positions;
    }

    private boolean shouldPlace(ClientLevel Level, BlockPos pos, float discardOnAir, RandomSource random) {
        if (discardOnAir == 0 || (discardOnAir != 1.0F && random.nextFloat() >= discardOnAir)) return true;
        for (Direction direction : Direction.values()) {
            if (!Level.getBlockState(pos.relative(direction)).canOcclude() && discardOnAir != 1.0F) return false;
        }
        return true;
    }

    private List<Vec3> generateHidden(ClientLevel Level, RandomSource random, BlockPos origin, int size) {
        List<Vec3> positions = new ArrayList<>();
        int limit = random.nextInt(size + 1);
        for (int i = 0; i < limit; i++) {
            int range = Math.min(i, 7);
            int x = randomCoord(random, range) + origin.getX();
            int y = randomCoord(random, range) + origin.getY();
            int z = randomCoord(random, range) + origin.getZ();
            BlockPos pos = new BlockPos(x, y, z);
            if (airCheck.get() != AirCheck.OFF && !Level.getBlockState(pos).canOcclude()) continue;
            if (shouldPlace(Level, pos, 1.0F, random)) {
                positions.add(new Vec3(x, y, z));
            }
        }
        return positions;
    }

    private int randomCoord(RandomSource random, int size) {
        return Math.round((random.nextFloat() - random.nextFloat()) * size);
    }
}
