package net.aero.aeropack.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.aero.aeropack.render.AeroRenderMode;
import net.aero.aeropack.render.AeroShaderHelper;
import net.aero.aeropack.render.AeroShaderSource;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPChunk;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPGroup;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.BlockRenderType;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = BlockESP.class, remap = false)
public abstract class BlockESPMixin implements AeroShaderSource {

    @Shadow private Long2ObjectMap<ESPChunk> chunks;
    @Shadow private Setting<Map<Block, ESPBlockData>> blockConfigs;
    @Shadow private Setting<ESPBlockData> defaultBlockConfig;
    @Shadow private Setting<Boolean> tracers;
    @Shadow private Set<ESPGroup> groups;

    @Unique private Setting<AeroRenderMode> aeropack$renderMode;

    @Unique private final MeshBuilder aeropack$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider aeropack$vcp = new MeshBuilderVertexConsumerProvider(aeropack$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void aeropack$addShaderSettings(CallbackInfo ci) {
        SettingGroup sgShader = ((BlockESP)(Object)this).settings.createGroup("Shader");

        aeropack$renderMode = sgShader.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("aeropack-render-mode")
            .description("Box ESP draws normal boxes. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void aeropack$renderShader(Render3DEvent event, CallbackInfo ci) {
        if (aeropack$renderMode.get() != AeroRenderMode.Shader) return;

        HashSet<Long> trackedSet = new HashSet<>();

        synchronized (chunks) {
            for (ESPChunk chunk : chunks.values()) {
                if (chunk.blocks == null) continue;
                for (ESPBlock block : chunk.blocks.values()) {
                    trackedSet.add(ESPBlock.getKey(block.x, block.y, block.z));
                }
            }

            aeropack$mesh.begin();

            for (ESPChunk chunk : chunks.values()) {
                if (chunk.blocks == null) continue;

                for (ESPBlock block : chunk.blocks.values()) {
                    int bx = block.x, by = block.y, bz = block.z;

                    if (trackedSet.contains(ESPBlock.getKey(bx + 1, by, bz))
                        && trackedSet.contains(ESPBlock.getKey(bx - 1, by, bz))
                        && trackedSet.contains(ESPBlock.getKey(bx, by + 1, bz))
                        && trackedSet.contains(ESPBlock.getKey(bx, by - 1, bz))
                        && trackedSet.contains(ESPBlock.getKey(bx, by, bz + 1))
                        && trackedSet.contains(ESPBlock.getKey(bx, by, bz - 1))) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(bx, by, bz);
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue;

                    ESPBlockData blockData = blockConfigs.get().get(state.getBlock());
                    if (blockData == null) blockData = defaultBlockConfig.get();

                    Color lc = blockData.lineColor;
                    aeropack$vcp.setColor(new Color(lc.r, lc.g, lc.b, 255));

                    if (state.getRenderType() != BlockRenderType.MODEL) {
                        aeropack$vcp.setOffset(0, 0, 0);
                        aeropack$renderFullBlock(bx, by, bz, new Color(lc.r, lc.g, lc.b, 255));
                    } else {
                        aeropack$vcp.setOffset(bx, by, bz);
                        SimpleBlockRenderer.render(pos, state, aeropack$vcp);
                    }
                }
            }
        }

        aeropack$vcp.setOffset(0, 0, 0);

        MeshRenderer.begin()
            .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
            .pipeline(MeteorRenderPipelines.WORLD_COLORED)
            .mesh(aeropack$mesh, event.matrices)
            .end();

        AeroShaderHelper.markDirty();

        synchronized (chunks) {
            if (tracers.get() && !groups.isEmpty()) {
                java.util.List<ESPGroup> groupList = new java.util.ArrayList<>(groups);
                AeroShaderHelper.setPendingTracers(() -> {
                    for (ESPGroup group : groupList) {
                        group.render(event);
                    }
                });
            }
        }

        ci.cancel();
    }

    // ── AeroShaderSource ─────────────────────────────────────────────────

    @Override
    public boolean aeropack$isShaderActive() {
        return ((BlockESP)(Object)this).isActive() && aeropack$renderMode.get() == AeroRenderMode.Shader;
    }

    @Unique
    private void aeropack$renderFullBlock(int bx, int by, int bz, Color color) {
        aeropack$mesh.ensureCapacity(24, 36);

        int v0 = aeropack$mesh.vec3(bx, by, bz).color(color).next();
        int v1 = aeropack$mesh.vec3(bx + 1, by, bz).color(color).next();
        int v2 = aeropack$mesh.vec3(bx + 1, by + 1, bz).color(color).next();
        int v3 = aeropack$mesh.vec3(bx, by + 1, bz).color(color).next();
        int v4 = aeropack$mesh.vec3(bx, by, bz + 1).color(color).next();
        int v5 = aeropack$mesh.vec3(bx + 1, by, bz + 1).color(color).next();
        int v6 = aeropack$mesh.vec3(bx + 1, by + 1, bz + 1).color(color).next();
        int v7 = aeropack$mesh.vec3(bx, by + 1, bz + 1).color(color).next();

        aeropack$mesh.quad(v0, v1, v2, v3);
        aeropack$mesh.quad(v5, v4, v7, v6);
        aeropack$mesh.quad(v4, v0, v3, v7);
        aeropack$mesh.quad(v1, v5, v6, v2);
        aeropack$mesh.quad(v3, v2, v6, v7);
        aeropack$mesh.quad(v4, v5, v1, v0);
    }
}
