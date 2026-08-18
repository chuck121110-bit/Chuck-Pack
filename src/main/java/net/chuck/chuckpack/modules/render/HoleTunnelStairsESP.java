/*
    Ported from Trouser Streak by etianl.
    Original by etianl with inspiration from Meteor Client.
    Modified for Chuck Pack - added shader mode support.
*/
package net.chuck.chuckpack.modules.render;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import meteordevelopment.orbit.EventHandler;
import net.chuck.chuckpack.render.AeroRenderMode;
import net.chuck.chuckpack.render.AeroShaderHelper;
import net.chuck.chuckpack.render.AeroShaderSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class HoleTunnelStairsESP extends Module implements AeroShaderSource {
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
    private static final Direction[] DIRECTIONS_FULL = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN};
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHParams = settings.createGroup("Hole Parameters");
    private final SettingGroup sgTParams = settings.createGroup("Tunnel Parameters");
    private final SettingGroup sgSParams = settings.createGroup("Stairs Parameters");
    private final SettingGroup sgRender = settings.createGroup("Rendering");
    private final SettingGroup sgWorld = settings.createGroup("Level Toggle");

    private final Setting<AeroRenderMode> renderMode = sgGeneral.add(new EnumSetting.Builder<AeroRenderMode>()
        .name("ChuckPack-render-mode")
        .description("AABB ESP draws normal boxes. Shader uses the same post-process outline shader as Storage ESP.")
        .defaultValue(AeroRenderMode.BoxESP)
        .build()
    );

    private final Setting<DetectionMode> detectionMode = sgGeneral.add(new EnumSetting.Builder<DetectionMode>()
        .name("detection-mode")
        .description("Choose what to detect: holes, tunnels, stairs, or all.")
        .defaultValue(DetectionMode.ALL)
        .build()
    );
    private final Setting<Integer> maxChunks = sgGeneral.add(new IntSetting.Builder()
        .name("chunks-per-tick")
        .description("Amount of chunks to process per tick.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 100)
        .build()
    );
    private final Setting<Boolean> airBlocks = sgGeneral.add(new BoolSetting.Builder()
        .name("air-only")
        .description("Only marks tunnels or holes if their blocks are air as opposed to passable.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("y-min-offset")
        .description("Scans blocks above or at this many blocks from minimum build limit.")
        .min(0)
        .sliderRange(0, 319)
        .defaultValue(0)
        .build()
    );
    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("y-max-offset")
        .description("Scans blocks below or at this many blocks from maximum build limit.")
        .min(0)
        .sliderRange(0, 319)
        .defaultValue(0)
        .build()
    );

    private final Setting<Integer> minHoleDepth = sgHParams.add(new IntSetting.Builder()
        .name("min-hole-depth")
        .description("Minimum depth for a hole to be detected.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> minTunnelLength = sgTParams.add(new IntSetting.Builder()
        .name("min-tunnel-length")
        .description("Minimum length for a tunnel to be detected.")
        .defaultValue(3)
        .min(1)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> minTunnelHeight = sgTParams.add(new IntSetting.Builder()
        .name("min-tunnel-height")
        .description("Minimum height of the tunnels to be detected.")
        .defaultValue(2)
        .min(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> maxTunnelHeight = sgTParams.add(new IntSetting.Builder()
        .name("max-tunnel-height")
        .description("Maximum height of the tunnels to be detected.")
        .defaultValue(3)
        .min(2)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> diagonals = sgTParams.add(new BoolSetting.Builder()
        .name("detect-diagonal-tunnels")
        .description("Detects diagonal tunnels when tunnels are selected to be detected.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> minDiagonalLength = sgTParams.add(new IntSetting.Builder()
        .name("min-diagonal-tunnel-length")
        .description("Minimum length for diagonal tunnels to be detected.")
        .defaultValue(3)
        .min(1)
        .sliderMax(20)
        .visible(diagonals::get)
        .build()
    );
    private final Setting<Integer> minDiagonalWidth = sgTParams.add(new IntSetting.Builder()
        .name("min-diagonal-tunnel-width")
        .description("Minimum width for diagonal tunnels to be detected.")
        .defaultValue(2)
        .min(2)
        .sliderMax(10)
        .visible(diagonals::get)
        .build()
    );
    private final Setting<Integer> maxDiagonalWidth = sgTParams.add(new IntSetting.Builder()
        .name("max-diagonal-tunnel-width")
        .description("Maximum width for diagonal tunnels to be detected.")
        .defaultValue(4)
        .min(2)
        .sliderMax(10)
        .visible(diagonals::get)
        .build()
    );

    private final Setting<Integer> minStaircaseLength = sgSParams.add(new IntSetting.Builder()
        .name("min-staircase-length")
        .description("Minimum length for a staircase to be detected.")
        .defaultValue(3)
        .min(1)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> minStaircaseHeight = sgSParams.add(new IntSetting.Builder()
        .name("min-staircase-height")
        .description("Minimum height of the staircase to be detected.")
        .defaultValue(3)
        .min(2)
        .sliderMax(10)
        .build()
    );
    private final Setting<Integer> maxStaircaseHeight = sgSParams.add(new IntSetting.Builder()
        .name("max-staircase-height")
        .description("Maximum height of the staircase to be detected.")
        .defaultValue(5)
        .min(2)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> frustumCulling = sgRender.add(new BoolSetting.Builder()
        .name("frustum-culling")
        .description("Only render boxes visible on screen using Minecraft's native frustum culling.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(() -> renderMode.get() == AeroRenderMode.BoxESP)
        .build()
    );
    private final Setting<SettingColor> holeLineColor = sgRender.add(new ColorSetting.Builder()
        .name("hole-line-color")
        .description("The color of the lines for the holes being rendered.")
        .defaultValue(new SettingColor(255, 0, 0, 95))
        .build()
    );
    private final Setting<SettingColor> holeSideColor = sgRender.add(new ColorSetting.Builder()
        .name("hole-side-color")
        .description("The color of the sides for the holes being rendered.")
        .defaultValue(new SettingColor(255, 0, 0, 30))
        .build()
    );
    private final Setting<SettingColor> tunnelLineColor = sgRender.add(new ColorSetting.Builder()
        .name("tunnel-line-color")
        .description("The color of the lines for the tunnels being rendered.")
        .defaultValue(new SettingColor(0, 0, 255, 95))
        .build()
    );
    private final Setting<SettingColor> tunnelSideColor = sgRender.add(new ColorSetting.Builder()
        .name("tunnel-side-color")
        .description("The color of the sides for the tunnels being rendered.")
        .defaultValue(new SettingColor(0, 0, 255, 30))
        .build()
    );
    private final Setting<SettingColor> staircaseLineColor = sgRender.add(new ColorSetting.Builder()
        .name("staircase-line-color")
        .description("The color of the lines for the staircases being rendered.")
        .defaultValue(new SettingColor(255, 0, 255, 95))
        .build()
    );
    private final Setting<SettingColor> staircaseSideColor = sgRender.add(new ColorSetting.Builder()
        .name("staircase-side-color")
        .description("The color of the sides for the staircases being rendered.")
        .defaultValue(new SettingColor(255, 0, 255, 30))
        .build()
    );

    private final Setting<Boolean> overworld = sgWorld.add(new BoolSetting.Builder()
        .name("Overworld")
        .description("Detect in the Overworld.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> nether = sgWorld.add(new BoolSetting.Builder()
        .name("Nether")
        .description("Detect in the Nether.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> end = sgWorld.add(new BoolSetting.Builder()
        .name("End")
        .description("Detect in the End.")
        .defaultValue(true)
        .build()
    );

    private final Long2ObjectMap<TChunk> chunks = new Long2ObjectOpenHashMap<>();
    private final Queue<LevelChunk> chunkQueue = new LinkedList<>();
    private final Set<AABB> holes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<AABB> tunnels = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<AABB> staircases = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    private final MeshBuilderVertexConsumerProvider vcp = new MeshBuilderVertexConsumerProvider(mesh);

    public HoleTunnelStairsESP() {
        super(Categories.Render, "Hole/Tunnel/StairsESP", "Finds and highlights holes, tunnels, and staircases. Ported from Trouser Streak by etianl.");
    }

    @Override
    public void onDeactivate() {
        this.clearData();
    }

    private void clearData() {
        chunks.clear();
        chunkQueue.clear();
        holes.clear();
        tunnels.clear();
        staircases.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.level != null) {
            ResourceKey<Level> dim = mc.level.dimension();

            if (
                (dim == Level.OVERWORLD && !overworld.get()) ||
                (dim == Level.NETHER && !nether.get()) ||
                (dim == Level.END && !end.get())
            ) {
                this.clearData();
                return;
            }
        }

        synchronized (chunks) {
            for (TChunk tChunk : chunks.values()) tChunk.marked = false;

            for (var chunk : Utils.chunks(true)) {
                long key = ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z());

                if (chunks.containsKey(key)) chunks.get(key).marked = true;
                else if (chunk instanceof LevelChunk wc && !chunkQueue.contains(wc)) {
                    chunkQueue.add(wc);
                }
            }

            processChunkQueue();
            chunks.values().removeIf(tChunk -> !tChunk.marked);
        }
        removeBoxesOutsideRenderDistance();
    }

    private void removeBoxesOutsideRenderDistance() {
        Set<LevelChunk> chunkSet = new HashSet<>();
        for (var c : Utils.chunks(true)) {
            if (c instanceof LevelChunk wc) chunkSet.add(wc);
        }
        removeBoxesOutsideRenderDistance(holes, chunkSet);
        removeBoxesOutsideRenderDistance(tunnels, chunkSet);
        removeBoxesOutsideRenderDistance(staircases, chunkSet);
    }

    private void removeBoxesOutsideRenderDistance(Set<AABB> boxSet, Set<LevelChunk> worldChunks) {
        boxSet.removeIf(AABB -> {
            BlockPos boxPos = new BlockPos((int) Math.floor(AABB.getCenter().x), (int) Math.floor(AABB.getCenter().y), (int) Math.floor(AABB.getCenter().z));
            assert mc.level != null;
            return !worldChunks.contains(mc.level.getChunk(boxPos));
        });
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (renderMode.get() == AeroRenderMode.Shader) {
            renderShader(event);
        } else {
            renderBoxESP(event);
        }
    }

    private void renderBoxESP(Render3DEvent event) {
        Renderer3D renderer = event.renderer;

        switch (detectionMode.get()) {
            case ALL:
                renderHoles(renderer);
                renderTunnels(renderer);
                renderStaircases(renderer);
                break;
            case HOLES_AND_TUNNELS:
                renderHoles(renderer);
                renderTunnels(renderer);
                break;
            case HOLES_AND_STAIRCASES:
                renderHoles(renderer);
                renderStaircases(renderer);
                break;
            case TUNNELS_AND_STAIRCASES:
                renderTunnels(renderer);
                renderStaircases(renderer);
                break;
            case HOLES:
                renderHoles(renderer);
                break;
            case TUNNELS:
                renderTunnels(renderer);
                break;
            case STAIRCASES:
                renderStaircases(renderer);
                break;
        }
    }

    private void renderHoles(Renderer3D renderer) {
        for (AABB AABB : holes) {
            if (frustumCulling.get() && !isBoxVisible(AABB)) continue;
            renderer.box(AABB.minX, AABB.minY, AABB.minZ, AABB.maxX, AABB.maxY, AABB.maxZ, holeSideColor.get(), holeLineColor.get(), shapeMode.get(), 0);
        }
    }

    private void renderTunnels(Renderer3D renderer) {
        for (AABB AABB : tunnels) {
            if (frustumCulling.get() && !isBoxVisible(AABB)) continue;
            renderer.box(AABB.minX, AABB.minY, AABB.minZ, AABB.maxX, AABB.maxY, AABB.maxZ, tunnelSideColor.get(), tunnelLineColor.get(), shapeMode.get(), 0);
        }
    }

    private void renderStaircases(Renderer3D renderer) {
        for (AABB AABB : staircases) {
            if (frustumCulling.get() && !isBoxVisible(AABB)) continue;
            renderer.box(AABB.minX, AABB.minY, AABB.minZ, AABB.maxX, AABB.maxY, AABB.maxZ, staircaseSideColor.get(), staircaseLineColor.get(), shapeMode.get(), 0);
        }
    }

    private boolean isBoxVisible(AABB AABB) {
        Frustum frustum = mc.gameRenderer.getMainCamera().getCullFrustum();
        if (frustum == null) return true;
        return frustum.isVisible(AABB);
    }

    private void renderShader(Render3DEvent event) {
        if (holes.isEmpty() && tunnels.isEmpty() && staircases.isEmpty()) return;

        if (!mesh.isBuilding()) mesh.begin();

        switch (detectionMode.get()) {
            case ALL:
                renderBoxesShader(mesh, holes, holeSideColor.get());
                renderBoxesShader(mesh, tunnels, tunnelSideColor.get());
                renderBoxesShader(mesh, staircases, staircaseSideColor.get());
                break;
            case HOLES_AND_TUNNELS:
                renderBoxesShader(mesh, holes, holeSideColor.get());
                renderBoxesShader(mesh, tunnels, tunnelSideColor.get());
                break;
            case HOLES_AND_STAIRCASES:
                renderBoxesShader(mesh, holes, holeSideColor.get());
                renderBoxesShader(mesh, staircases, staircaseSideColor.get());
                break;
            case TUNNELS_AND_STAIRCASES:
                renderBoxesShader(mesh, tunnels, tunnelSideColor.get());
                renderBoxesShader(mesh, staircases, staircaseSideColor.get());
                break;
            case HOLES:
                renderBoxesShader(mesh, holes, holeSideColor.get());
                break;
            case TUNNELS:
                renderBoxesShader(mesh, tunnels, tunnelSideColor.get());
                break;
            case STAIRCASES:
                renderBoxesShader(mesh, staircases, staircaseSideColor.get());
                break;
        }

        if (!mesh.isBuilding()) return;
        if (mesh.getIndicesCount() == 0) return;

        MeshRenderer.begin()
            .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
            .pipeline(MeteorRenderPipelines.WORLD_COLORED)
            .mesh(mesh, event.matrices)
            .end();

        AeroShaderHelper.markDirty();
    }

    private void renderBoxesShader(MeshBuilder mesh, Set<AABB> boxSet, Color color) {
        Color shaderColor = new Color(color.r, color.g, color.b, 255);
        Frustum frustum = frustumCulling.get() ? mc.gameRenderer.getMainCamera().getCullFrustum() : null;
        for (AABB AABB : boxSet) {
            if (frustum != null && !frustum.isVisible(AABB)) continue;
            meshBox(mesh, AABB.minX, AABB.minY, AABB.minZ, AABB.maxX, AABB.maxY, AABB.maxZ, shaderColor);
        }
    }

    private void meshBox(MeshBuilder mesh, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        meshQuad(mesh, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color);
        meshQuad(mesh, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, color);
        meshQuad(mesh, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, color);
        meshQuad(mesh, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        meshQuad(mesh, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, color);
        meshQuad(mesh, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color);
    }

    private void meshQuad(MeshBuilder mesh, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, Color color) {
        mesh.ensureQuadCapacity();
        mesh.quad(
            mesh.vec3(x0, y0, z0).color(color).next(),
            mesh.vec3(x1, y1, z1).color(color).next(),
            mesh.vec3(x2, y2, z2).color(color).next(),
            mesh.vec3(x3, y3, z3).color(color).next()
        );
    }

    private Set<AABB> getActiveBoxes() {
        Set<AABB> active = new HashSet<>();
        switch (detectionMode.get()) {
            case ALL:
                active.addAll(holes);
                active.addAll(tunnels);
                active.addAll(staircases);
                break;
            case HOLES_AND_TUNNELS:
                active.addAll(holes);
                active.addAll(tunnels);
                break;
            case HOLES_AND_STAIRCASES:
                active.addAll(holes);
                active.addAll(staircases);
                break;
            case TUNNELS_AND_STAIRCASES:
                active.addAll(tunnels);
                active.addAll(staircases);
                break;
            case HOLES:
                active.addAll(holes);
                break;
            case TUNNELS:
                active.addAll(tunnels);
                break;
            case STAIRCASES:
                active.addAll(staircases);
                break;
        }
        return active;
    }

    private void renderBoxBlocks(AABB AABB) {
        int minX = (int) Math.floor(AABB.minX);
        int minY = (int) Math.floor(AABB.minY);
        int minZ = (int) Math.floor(AABB.minZ);
        int maxX = (int) Math.floor(AABB.maxX);
        int maxY = (int) Math.floor(AABB.maxY);
        int maxZ = (int) Math.floor(AABB.maxZ);

        Set<BlockPos> rendered = new HashSet<>();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!isPassableBlock(pos)) continue;

                    for (Direction dir : DIRECTIONS_FULL) {
                        BlockPos neighbor = pos.relative(dir);
                        if (!rendered.add(neighbor)) continue;

                        BlockState state = mc.level.getBlockState(neighbor);
                        if (state.isAir()) continue;

                        vcp.setOffset(neighbor.getX(), neighbor.getY(), neighbor.getZ());
                        SimpleBlockRenderer.render(neighbor, state, vcp);
                    }
                }
            }
        }
    }

    private void processChunkQueue() {
        int maxChunksPerTick = maxChunks.get();
        int processed = 0;

        while (!chunkQueue.isEmpty() && processed < maxChunksPerTick) {
            LevelChunk chunk = chunkQueue.poll();
            if (chunk != null) {
                TChunk tChunk = new TChunk(chunk.getPos().x(), chunk.getPos().z());
                chunks.put(tChunk.getKey(), tChunk);

                MeteorExecutor.execute(() -> searchChunk(chunk, tChunk));
                processed++;
            }
        }
    }

    private void searchChunk(LevelChunk chunk, TChunk tChunk) {
        var sections = chunk.getSections();
        int Ymin = mc.level.getMinY() + minY.get();
        int Ymax = mc.level.getMinY() + mc.level.getHeight() - maxY.get();
        int Y = mc.level.getMinY();
        for (LevelChunkSection section : sections) {
            if (section != null && !section.hasOnlyAir()) {
                for (int z = 0; z <= 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            int currentY = Y + y;
                            if (currentY <= Ymin || currentY >= Ymax) continue;
                            BlockPos pos = chunk.getPos().getBlockAt(x, currentY, z);
                            if (isPassableBlock(pos)) {
                                switch (detectionMode.get()) {
                                    case ALL:
                                        checkHole(pos, holes);
                                        checkTunnel(pos);
                                        if (diagonals.get()) checkDiagonalTunnel(pos);
                                        checkStaircase(pos);
                                        break;
                                    case HOLES_AND_TUNNELS:
                                        checkHole(pos, holes);
                                        checkTunnel(pos);
                                        if (diagonals.get()) checkDiagonalTunnel(pos);
                                        break;
                                    case HOLES_AND_STAIRCASES:
                                        checkHole(pos, holes);
                                        checkStaircase(pos);
                                        break;
                                    case TUNNELS_AND_STAIRCASES:
                                        checkTunnel(pos);
                                        if (diagonals.get()) checkDiagonalTunnel(pos);
                                        checkStaircase(pos);
                                        break;
                                    case HOLES:
                                        checkHole(pos, holes);
                                        break;
                                    case TUNNELS:
                                        checkTunnel(pos);
                                        if (diagonals.get()) checkDiagonalTunnel(pos);
                                        break;
                                    case STAIRCASES:
                                        checkStaircase(pos);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            Y += 16;
        }
    }

    private void checkHole(BlockPos pos, Set<AABB> holes) {
        if (isValidHoleSection(pos)) {
            BlockPos.MutableBlockPos currentPos = pos.mutable();
            while (isValidHoleSection(currentPos)) {
                currentPos.move(Direction.UP);
            }
            if (currentPos.getY() - pos.getY() >= minHoleDepth.get()) {
                AABB holeBox = new AABB(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, currentPos.getY(), pos.getZ() + 1
                );
                if (!holes.contains(holeBox) && holes.stream().noneMatch(existingHole -> existingHole.intersects(holeBox))) {
                    holes.add(holeBox);
                }
            }
        }
    }

    private boolean isValidHoleSection(BlockPos pos) {
        return isPassableBlock(pos) && !isPassableBlock(pos.north()) && !isPassableBlock(pos.south()) && !isPassableBlock(pos.east()) && !isPassableBlock(pos.west());
    }

    private void checkTunnel(BlockPos pos) {
        for (Direction dir : DIRECTIONS) {
            BlockPos.MutableBlockPos currentPos = pos.mutable();
            int stepCount = 0;
            BlockPos startPos = null;
            BlockPos endPos = null;
            int maxHeight = 0;
            if (startPos == null && isTunnelSection(currentPos, dir)) {
                startPos = currentPos.immutable();
            }
            while (isTunnelSection(currentPos, dir)) {
                maxHeight = Math.max(maxHeight, getTunnelHeight(currentPos));

                endPos = currentPos.immutable();

                currentPos.move(dir);
                stepCount++;
            }

            if (stepCount >= minTunnelLength.get() && maxHeight >= minTunnelHeight.get() && maxHeight <= maxTunnelHeight.get()) {
                AABB tunnelBox = new AABB(
                    Math.min(startPos.getX(), endPos.getX()),
                    startPos.getY(),
                    Math.min(startPos.getZ(), endPos.getZ()),
                    Math.max(startPos.getX(), endPos.getX()) + 1,
                    startPos.getY() + maxHeight,
                    Math.max(startPos.getZ(), endPos.getZ()) + 1
                );

                if (!tunnels.contains(tunnelBox) && tunnels.stream().noneMatch(existingTunnel -> existingTunnel.intersects(tunnelBox))) {
                    tunnels.add(tunnelBox);
                }
            }
        }
    }

    private boolean isTunnelSection(BlockPos pos, Direction dir) {
        int height = getTunnelHeight(pos);
        if (height < minTunnelHeight.get() || height > maxTunnelHeight.get()) return false;
        if (isPassableBlock(pos.below()) || isPassableBlock(pos.above(height))) return false;
        Direction[] perpDirs = dir.getAxis() == Direction.Axis.X ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.EAST, Direction.WEST};
        for (Direction perpDir : perpDirs) {
            for (int i = 0; i < height; i++) {
                if (isPassableBlock(pos.above(i).relative(perpDir))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkDiagonalTunnel(BlockPos pos) {
        for (Direction dir : DIRECTIONS) {
            for (int i = minDiagonalWidth.get() - 1; i < maxDiagonalWidth.get(); i++) {
                BlockPos.MutableBlockPos currentPos = pos.mutable();
                int stepCount = 0;
                List<AABB> potentialBoxes = new ArrayList<>();

                Direction checkingDir = dir;
                boolean turnRight = true;

                while (isDiagonalTunnelSection(currentPos, checkingDir)) {
                    int height = getTunnelHeight(currentPos);
                    AABB tunnelBox = new AABB(
                        currentPos.getX(),
                        currentPos.getY(),
                        currentPos.getZ(),
                        currentPos.getX() + 1,
                        currentPos.getY() + height,
                        currentPos.getZ() + 1
                    );
                    if (!potentialBoxes.contains(tunnelBox) && !potentialBoxes.stream().anyMatch(existingDiagonal -> existingDiagonal.intersects(tunnelBox))) {
                        potentialBoxes.add(tunnelBox);
                    }

                    if (turnRight) {
                        checkingDir = checkingDir.getClockWise(Direction.Axis.Y);
                        currentPos.move(checkingDir.getClockWise(Direction.Axis.Y), i);
                        turnRight = false;
                    } else {
                        checkingDir = checkingDir.getCounterClockWise(Direction.Axis.Y);
                        currentPos.move(checkingDir.getCounterClockWise(Direction.Axis.Y), i);
                        turnRight = true;
                    }
                    stepCount++;
                }

                if (stepCount / minDiagonalWidth.get() >= minDiagonalLength.get()) {
                    potentialBoxes.forEach(potentialBox -> {
                        if (!tunnels.contains(potentialBox) && tunnels.stream().noneMatch(existingDiagonal -> existingDiagonal.intersects(potentialBox))) {
                            tunnels.add(potentialBox);
                        }
                    });
                }
            }
        }
    }

    private boolean isDiagonalTunnelSection(BlockPos pos, Direction dir) {
        int height = getTunnelHeight(pos);
        if (height < minTunnelHeight.get() || height > maxTunnelHeight.get()) return false;
        if (isPassableBlock(pos.below()) || isPassableBlock(pos.above(height))) return false;

        boolean waspassableblockfound = false;
        for (int i = 0; i < height; i++) {
            if (isPassableBlock(pos.above(i).relative(dir))) waspassableblockfound = true;
        }
        return !waspassableblockfound;
    }

    private int getTunnelHeight(BlockPos pos) {
        int height = 0;
        while (isPassableBlock(pos.above(height)) && height < maxTunnelHeight.get()) {
            height++;
        }
        return height;
    }

    private void checkStaircase(BlockPos pos) {
        for (Direction dir : DIRECTIONS) {
            BlockPos.MutableBlockPos currentPos = pos.mutable();
            int stepCount = 0;
            List<AABB> potentialStaircaseBoxes = new ArrayList<>();

            while (isStaircaseSection(currentPos, dir)) {
                int height = getStaircaseHeight(currentPos);
                AABB stairsBox = new AABB(
                    currentPos.getX(),
                    currentPos.getY(),
                    currentPos.getZ(),
                    currentPos.getX() + 1,
                    currentPos.getY() + height,
                    currentPos.getZ() + 1
                );
                if (!potentialStaircaseBoxes.contains(stairsBox) && !potentialStaircaseBoxes.stream().anyMatch(existingStaircase -> existingStaircase.intersects(stairsBox))) {
                    potentialStaircaseBoxes.add(stairsBox);
                }
                currentPos.move(dir);
                currentPos.move(Direction.UP);
                stepCount++;
            }

            for (AABB stairsBox : potentialStaircaseBoxes) {
                if (stepCount >= minStaircaseLength.get() && !staircases.contains(stairsBox) && !staircases.stream().anyMatch(existingStaircase -> existingStaircase.intersects(stairsBox))) {
                    staircases.add(stairsBox);
                }
            }
        }
    }

    private int getStaircaseHeight(BlockPos pos) {
        int height = 0;
        while (isPassableBlock(pos.above(height)) && height < maxStaircaseHeight.get()) {
            height++;
        }
        return height;
    }

    private boolean isStaircaseSection(BlockPos pos, Direction dir) {
        int height = getStaircaseHeight(pos);
        if (height < minStaircaseHeight.get() || height > maxStaircaseHeight.get()) return false;
        if (isPassableBlock(pos.below()) || isPassableBlock(pos.above(height))) return false;
        Direction[] perpDirs = dir.getAxis() == Direction.Axis.X ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.EAST, Direction.WEST};
        for (Direction perpDir : perpDirs) {
            for (int i = 0; i < height; i++) {
                if (isPassableBlock(pos.above(i).relative(perpDir))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPassableBlock(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (airBlocks.get()) {
            return state.isAir();
        } else {
            VoxelShape shape = state.getCollisionShape(mc.level, pos);
            return shape.isEmpty() || !Shapes.block().equals(shape);
        }
    }

    @Override
    public boolean chuckpack\$isShaderActive() {
        return isActive() && renderMode.get() == AeroRenderMode.Shader;
    }

    public enum DetectionMode {
        ALL,
        HOLES_AND_TUNNELS,
        HOLES_AND_STAIRCASES,
        TUNNELS_AND_STAIRCASES,
        HOLES,
        TUNNELS,
        STAIRCASES
    }

    private class TChunk {
        private final int x, z;
        public boolean marked;

        public TChunk(int x, int z) {
            this.x = x;
            this.z = z;
            this.marked = true;
        }

        public long getKey() {
            return ChunkPos.pack(x, z);
        }
    }
}