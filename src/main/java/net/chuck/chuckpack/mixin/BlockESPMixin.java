package net.chuck.chuckpack.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.chuck.chuckpack.render.AeroRenderMode;
import net.chuck.chuckpack.render.AeroShaderHelper;
import net.chuck.chuckpack.render.AeroShaderSource;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
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

import net.minecraft.world.level.block.RenderShape;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = BlockESP.class, remap = false)
public abstract class BlockESPMixin implements AeroShaderSource {

    @Shadow private Long2ObjectMap<ESPChunk> chunks;
    @Shadow private Setting<Map<Block, ESPBlockData>> blockConfigs;
    @Shadow private Setting<ESPBlockData> defaultBlockConfig;
    @Shadow private Setting<Boolean> tracers;
    @Shadow private Set<ESPGroup> groups;

    @Unique private Setting<AeroRenderMode> chuckpack\$renderMode;

    @Unique private final MeshBuilder chuckpack\$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider chuckpack\$vcp = new MeshBuilderVertexConsumerProvider(chuckpack\$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void chuckpack\$addShaderSettings(CallbackInfo ci) {
        SettingGroup sgShader = ((BlockESP)(Object)this).settings.createGroup("Shader");

        chuckpack\$renderMode = sgShader.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("ChuckPack-render-mode")
            .description("AABB ESP draws normal boxes. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void chuckpack\$renderShader(Render3DEvent event, CallbackInfo ci) {
        if (chuckpack\$renderMode.get() != AeroRenderMode.Shader) return;

        HashSet<Long> trackedSet = new HashSet<>();

        synchronized (chunks) {
            for (ESPChunk LevelChunk : chunks.values()) {
                if (LevelChunk.blocks == null) continue;
                for (ESPBlock block : LevelChunk.blocks.values()) {
                    trackedSet.add(ESPBlock.getKey(block.x, block.y, block.z));
                }
            }

            chuckpack\$mesh.begin();

            for (ESPChunk LevelChunk : chunks.values()) {
                if (LevelChunk.blocks == null) continue;

                for (ESPBlock block : LevelChunk.blocks.values()) {
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
                    chuckpack\$vcp.setColor(new Color(lc.r, lc.g, lc.b, 255));

                    if (state.getRenderShape() != RenderShape.MODEL) {
                        chuckpack\$vcp.setOffset(0, 0, 0);
                        chuckpack\$renderFullBlock(bx, by, bz, new Color(lc.r, lc.g, lc.b, 255));
                    } else {
                        chuckpack\$vcp.setOffset(bx, by, bz);
                        SimpleBlockRenderer.render(pos, state, chuckpack\$vcp);
                    }
                }
            }
        }

        chuckpack\$vcp.setOffset(0, 0, 0);

        MeshRenderer.begin()
            .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
            .pipeline(MeteorRenderPipelines.WORLD_COLORED)
            .mesh(chuckpack\$mesh, event.matrices)
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
    public boolean chuckpack\$isShaderActive() {
        return ((BlockESP)(Object)this).isActive() && chuckpack\$renderMode.get() == AeroRenderMode.Shader;
    }

    @Unique
    private void chuckpack\$renderFullBlock(int bx, int by, int bz, Color color) {
        chuckpack\$mesh.ensureCapacity(24, 36);

        int v0 = chuckpack\$mesh.vec3(bx, by, bz).color(color).next();
        int v1 = chuckpack\$mesh.vec3(bx + 1, by, bz).color(color).next();
        int v2 = chuckpack\$mesh.vec3(bx + 1, by + 1, bz).color(color).next();
        int v3 = chuckpack\$mesh.vec3(bx, by + 1, bz).color(color).next();
        int v4 = chuckpack\$mesh.vec3(bx, by, bz + 1).color(color).next();
        int v5 = chuckpack\$mesh.vec3(bx + 1, by, bz + 1).color(color).next();
        int v6 = chuckpack\$mesh.vec3(bx + 1, by + 1, bz + 1).color(color).next();
        int v7 = chuckpack\$mesh.vec3(bx, by + 1, bz + 1).color(color).next();

        chuckpack\$mesh.quad(v0, v1, v2, v3);
        chuckpack\$mesh.quad(v5, v4, v7, v6);
        chuckpack\$mesh.quad(v4, v0, v3, v7);
        chuckpack\$mesh.quad(v1, v5, v6, v2);
        chuckpack\$mesh.quad(v3, v2, v6, v7);
        chuckpack\$mesh.quad(v4, v5, v1, v0);
    }
}
