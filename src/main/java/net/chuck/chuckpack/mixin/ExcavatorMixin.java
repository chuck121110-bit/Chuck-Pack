package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.render.AeroRenderMode;
import net.chuck.chuckpack.render.AeroShaderHelper;
import net.chuck.chuckpack.render.AeroShaderSource;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Excavator;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.SimpleBlockRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = Excavator.class, remap = false)
public abstract class ExcavatorMixin implements AeroShaderSource {

    @Shadow private SettingGroup sgRendering;

    @Unique private Setting<AeroRenderMode> chuckpack\$renderMode;

    @Unique private Render3DEvent chuckpack\$currentEvent;
    @Unique private final MeshBuilder chuckpack\$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider chuckpack\$vcp = new MeshBuilderVertexConsumerProvider(chuckpack\$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void chuckpack\$addShaderSettings(CallbackInfo ci) {
        chuckpack\$renderMode = sgRendering.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("ChuckPack-render-mode")
            .description("AABB ESP draws a normal AABB outline. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    @Inject(method = "onRender3D", at = @At("HEAD"))
    private void chuckpack\$captureEvent(Render3DEvent event, CallbackInfo ci) {
        chuckpack\$currentEvent = event;
    }

    @Redirect(
        method = "onRender3D",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/renderer/Renderer3D;AABB(Lnet/minecraft/util/math/BlockPos;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/renderer/ShapeMode;I)V"
        )
    )
    private void chuckpack\$redirectBox(Renderer3D renderer, BlockPos pos, Color side, Color line, ShapeMode shapeMode, int excludeDir) {
        if (chuckpack\$renderMode.get() != AeroRenderMode.Shader) {
            renderer.box(pos, side, line, shapeMode, excludeDir);
            return;
        }

        BlockState state = mc.level.getBlockState(pos);

        if (state.isAir()) {
            chuckpack\$mesh.begin();
            chuckpack\$mesh.ensureQuadCapacity();
            double x1 = pos.getX(), y1 = pos.getY(), z1 = pos.getZ();
            double x2 = x1 + 1, y2 = y1 + 1, z2 = z1 + 1;

            // Bottom face (y = y1)
            int b0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z1).color(line);
            int b1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z2).color(line);
            int b2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z2).color(line);
            int b3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z1).color(line);
            chuckpack\$mesh.quad(b0, b1, b2, b3);

            // Top face (y = y2)
            int t0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z1).color(line);
            int t1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z1).color(line);
            int t2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z2).color(line);
            int t3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z2).color(line);
            chuckpack\$mesh.quad(t0, t1, t2, t3);

            // North face (z = z1)
            int n0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z1).color(line);
            int n1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z1).color(line);
            int n2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z1).color(line);
            int n3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z1).color(line);
            chuckpack\$mesh.quad(n0, n1, n2, n3);

            // South face (z = z2)
            int s0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z2).color(line);
            int s1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z2).color(line);
            int s2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z2).color(line);
            int s3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z2).color(line);
            chuckpack\$mesh.quad(s0, s1, s2, s3);

            // West face (x = x1)
            int w0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z2).color(line);
            int w1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y1, z1).color(line);
            int w2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z1).color(line);
            int w3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x1, y2, z2).color(line);
            chuckpack\$mesh.quad(w0, w1, w2, w3);

            // East face (x = x2)
            int e0 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z1).color(line);
            int e1 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y1, z2).color(line);
            int e2 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z2).color(line);
            int e3 = chuckpack\$mesh.next(); chuckpack\$mesh.vec3(x2, y2, z1).color(line);
            chuckpack\$mesh.quad(e0, e1, e2, e3);

            MeshRenderer.begin()
                .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
                .pipeline(MeteorRenderPipelines.WORLD_COLORED)
                .mesh(chuckpack\$mesh, chuckpack\$currentEvent.matrices)
                .end();
        } else {
            chuckpack\$mesh.begin();
            chuckpack\$vcp.setColor(new Color(line.r, line.g, line.b, 255));
            chuckpack\$vcp.setOffset(pos.getX(), pos.getY(), pos.getZ());
            SimpleBlockRenderer.render(pos, state, chuckpack\$vcp);
            chuckpack\$vcp.setOffset(0, 0, 0);

            MeshRenderer.begin()
                .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
                .pipeline(MeteorRenderPipelines.WORLD_COLORED)
                .mesh(chuckpack\$mesh, chuckpack\$currentEvent.matrices)
                .end();
        }

        AeroShaderHelper.markDirty();
    }

    // ── AeroShaderSource ─────────────────────────────────────────────────

    @Override
    public boolean chuckpack\$isShaderActive() {
        return chuckpack\$renderMode.get() == AeroRenderMode.Shader;
    }
}
