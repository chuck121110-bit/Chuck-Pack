/*
 * Originally from Nora Tweaks (Noratweek) — https://github.com/Noratweek/nora-tweaks
 * Credit: Nora Tweaks
 * Modified for Chuck Pack — added shader mode support.
 */
package net.chuck.chuckpack.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import meteordevelopment.orbit.EventHandler;
import net.chuck.chuckpack.render.AeroRenderMode;
import net.chuck.chuckpack.render.AeroShaderHelper;
import net.chuck.chuckpack.render.AeroShaderSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepslateESP extends Module implements AeroShaderSource {

    // ── AABB ESP settings ───────────────────────────────────────────────
    private final Setting<SettingColor> espColor = settings.getDefaultGroup().add(new ColorSetting.Builder()
        .name("color")
        .description("ESP color (lines alpha used; fill alpha is softer).")
        .defaultValue(new SettingColor(255, 80, 80, 200))
        .build()
    );

    // ── Shader settings ────────────────────────────────────────────────
    private final Setting<AeroRenderMode> renderMode = settings.getDefaultGroup().add(new EnumSetting.Builder<AeroRenderMode>()
        .name("ChuckPack-render-mode")
        .description("AABB ESP draws normal boxes. Shader uses the same post-process outline shader as Storage ESP.")
        .defaultValue(AeroRenderMode.BoxESP)
        .build()
    );

    // ── State ──────────────────────────────────────────────────────────
    private final Map<Long, Map<BlockPos, Boolean>> chunkToFlaggedPositions = new ConcurrentHashMap<>();
    private final List<BlockPos> toRender = Collections.synchronizedList(new ArrayList<>());
    private final BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ChuckPack-DeepslateESP");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean renderListDirty = true;

    // ── Shader mesh ────────────────────────────────────────────────────
    private final MeshBuilder mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    private final MeshBuilderVertexConsumerProvider vcp = new MeshBuilderVertexConsumerProvider(mesh);

    public DeepslateESP() {
        super(Categories.Render, "deepslate-esp", "Highlights deepslate with non-Y axis (likely player-placed). Ported from Nora Tweaks by Noratweek.");
    }

    @Override
    public void onActivate() {
        chunkToFlaggedPositions.clear();
        toRender.clear();
        renderListDirty = true;

        for (var c : Utils.chunks()) {
            if (c instanceof LevelChunk wc) scanChunkAsync(wc);
        }
    }

    @Override
    public void onDeactivate() {
        chunkToFlaggedPositions.clear();
        toRender.clear();
        renderListDirty = false;
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        scanChunkAsync(event.chunk());
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (!isActive()) return;

        int bx = event.pos.getX();
        int by = event.pos.getY();
        int bz = event.pos.getZ();
        long key = ChunkPos.pack(bx >> 4, bz >> 4);

        var oldState = event.oldState;
        var newState = event.newState;

        boolean wasDs = oldState.getBlock() == Blocks.DEEPSLATE;
        boolean isDs = newState.getBlock() == Blocks.DEEPSLATE;

        worker.submit(() -> {
            Map<BlockPos, Boolean> map = chunkToFlaggedPositions.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
            BlockPos pos = new BlockPos(bx, by, bz);

            if (isDs) {
                Boolean flagged = isNonNaturalDeepslate(newState);
                if (flagged != null && flagged) map.put(pos, Boolean.TRUE); else map.remove(pos);
            } else if (wasDs) {
                map.remove(pos);
            }

            renderListDirty = true;
        });
    }

    private void scanChunkAsync(LevelChunk chunk) {
        worker.submit(() -> scanChunk(chunk));
    }

    private void scanChunk(LevelChunk chunk) {
        if (!isActive() || mc.level == null) return;

        long key = chunk.getPos().pack();
        Map<BlockPos, Boolean> map = new ConcurrentHashMap<>();

        int startX = chunk.getPos().getMinBlockX();
        int endX = chunk.getPos().getMaxBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int endZ = chunk.getPos().getMaxBlockZ();

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x - startX, z - startZ);
                for (int y = mc.level.getMinY(); y < top; y++) {
                    scanPos.set(x, y, z);
                    BlockState state = chunk.getBlockState(scanPos);
                    if (state.getBlock() == Blocks.DEEPSLATE) {
                        Boolean flagged = isNonNaturalDeepslate(state);
                        if (flagged != null && flagged) map.put(scanPos.immutable(), Boolean.TRUE);
                    }
                }
            }
        }

        if (!map.isEmpty()) {
            chunkToFlaggedPositions.put(key, map);
            renderListDirty = true;
        }
    }

    private Boolean isNonNaturalDeepslate(BlockState state) {
        if (!state.hasProperty(BlockStateProperties.AXIS)) return null;
        return state.getValue(BlockStateProperties.AXIS) != net.minecraft.core.Direction.Axis.Y;
    }

    // ── Rendering ──────────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (renderListDirty) rebuildRenderList();

        if (renderMode.get() == AeroRenderMode.Shader) {
            renderShader(event);
        } else {
            renderBoxESP(event);
        }
    }

    private void renderBoxESP(Render3DEvent event) {
        Color lc = new Color(espColor.get());
        Color sc = new Color(espColor.get().r, espColor.get().g, espColor.get().b, Math.max(25, espColor.get().a / 4));

        for (int i = 0; i < toRender.size(); i++) {
            BlockPos pos = toRender.get(i);
            double x1 = pos.getX();
            double y1 = pos.getY();
            double z1 = pos.getZ();
            double x2 = x1 + 1;
            double y2 = y1 + 1;
            double z2 = z1 + 1;

            event.renderer.box(x1, y1, z1, x2, y2, z2, sc, lc, ShapeMode.Both, 0);
        }
    }

    private void renderShader(Render3DEvent event) {
        if (toRender.isEmpty()) return;

        Color espCol = espColor.get();
        Color color = new Color(espCol.r, espCol.g, espCol.b, 255);

        mesh.begin();
        vcp.setColor(color);

        for (int i = 0; i < toRender.size(); i++) {
            BlockPos pos = toRender.get(i);
            BlockState state = mc.level.getBlockState(pos);
            if (state.isAir()) continue;

            vcp.setOffset(pos.getX(), pos.getY(), pos.getZ());
            SimpleBlockRenderer.render(pos, state, vcp);
        }

        vcp.setOffset(0, 0, 0);

        MeshRenderer.begin()
            .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
            .pipeline(MeteorRenderPipelines.WORLD_COLORED)
            .mesh(mesh, event.matrices)
            .end();

        AeroShaderHelper.markDirty();
    }

    private void rebuildRenderList() {
        renderListDirty = false;
        toRender.clear();

        for (Map<BlockPos, Boolean> map : chunkToFlaggedPositions.values()) {
            toRender.addAll(map.keySet());
        }
    }

    // ── AeroShaderSource ───────────────────────────────────────────────

    @Override
    public boolean chuckpack$isShaderActive() {
        return isActive() && renderMode.get() == AeroRenderMode.Shader;
    }
}
