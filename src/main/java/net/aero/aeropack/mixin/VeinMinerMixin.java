package net.aero.aeropack.mixin;

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
import meteordevelopment.meteorclient.systems.modules.world.VeinMiner;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = VeinMiner.class, remap = false)
public abstract class VeinMinerMixin implements AeroShaderSource {

    @Shadow private SettingGroup sgRender;
    @Shadow private Setting<SettingColor> sideColor;
    @Shadow private Setting<SettingColor> lineColor;
    @Shadow private Setting<Boolean> render;
    @Shadow private List blocks;

    @Unique private Setting<AeroRenderMode> aeropack$renderMode;

    @Unique private final MeshBuilder aeropack$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider aeropack$vcp = new MeshBuilderVertexConsumerProvider(aeropack$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void aeropack$addShaderSettings(CallbackInfo ci) {
        aeropack$renderMode = sgRender.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("aeropack-render-mode")
            .description("Box ESP draws a normal box outline. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void aeropack$renderShaderBlocks(Render3DEvent event, CallbackInfo ci) {
        if (!render.get() || blocks.isEmpty()) return;
        if (aeropack$renderMode.get() != AeroRenderMode.Shader) return;

        aeropack$mesh.begin();
        for (Object obj : blocks) {
            BlockPos pos = ((VeinMinerMyBlockAccessor) obj).aeropack$getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            if (state.isAir()) continue;

            Color lc = lineColor.get();
            aeropack$vcp.setColor(new Color(lc.r, lc.g, lc.b, 255));
            aeropack$vcp.setOffset(pos.getX(), pos.getY(), pos.getZ());
            SimpleBlockRenderer.render(pos, state, aeropack$vcp);
        }
        aeropack$vcp.setOffset(0, 0, 0);

        MeshRenderer.begin()
            .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
            .pipeline(MeteorRenderPipelines.WORLD_COLORED)
            .mesh(aeropack$mesh, event.matrices)
            .end();

        AeroShaderHelper.markDirty();
        ci.cancel();
    }

    // ── AeroShaderSource ─────────────────────────────────────────────────

    @Override
    public boolean aeropack$isShaderActive() {
        return ((VeinMiner)(Object)this).isActive() && render.get() && aeropack$renderMode.get() == AeroRenderMode.Shader;
    }
}
