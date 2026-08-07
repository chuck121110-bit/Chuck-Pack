package net.aero.aeropack.mixin;

import net.aero.aeropack.render.AeroRenderMode;
import net.aero.aeropack.render.AeroShaderHelper;
import net.aero.aeropack.render.AeroShaderSource;

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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
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

    @Unique private Setting<AeroRenderMode> aeropack$renderMode;

    @Unique private Render3DEvent aeropack$currentEvent;
    @Unique private final MeshBuilder aeropack$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider aeropack$vcp = new MeshBuilderVertexConsumerProvider(aeropack$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void aeropack$addShaderSettings(CallbackInfo ci) {
        aeropack$renderMode = sgRendering.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("aeropack-render-mode")
            .description("Box ESP draws a normal box outline. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    @Inject(method = "onRender3D", at = @At("HEAD"))
    private void aeropack$captureEvent(Render3DEvent event, CallbackInfo ci) {
        aeropack$currentEvent = event;
    }

    @Redirect(
        method = "onRender3D",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/renderer/Renderer3D;box(Lnet/minecraft/util/math/BlockPos;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/renderer/ShapeMode;I)V"
        )
    )
    private void aeropack$redirectBox(Renderer3D renderer, BlockPos pos, Color side, Color line, ShapeMode shapeMode, int excludeDir) {
        if (aeropack$renderMode.get() != AeroRenderMode.Shader) {
            renderer.box(pos, side, line, shapeMode, excludeDir);
            return;
        }

        BlockState state = mc.world.getBlockState(pos);

        if (state.isAir()) {
            aeropack$mesh.begin();
            aeropack$mesh.ensureQuadCapacity();
            double x1 = pos.getX(), y1 = pos.getY(), z1 = pos.getZ();
            double x2 = x1 + 1, y2 = y1 + 1, z2 = z1 + 1;

            // Bottom face (y = y1)
            int b0 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z1).color(line);
            int b1 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z2).color(line);
            int b2 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z2).color(line);
            int b3 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z1).color(line);
            aeropack$mesh.quad(b0, b1, b2, b3);

            // Top face (y = y2)
            int t0 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z1).color(line);
            int t1 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z1).color(line);
            int t2 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z2).color(line);
            int t3 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z2).color(line);
            aeropack$mesh.quad(t0, t1, t2, t3);

            // North face (z = z1)
            int n0 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z1).color(line);
            int n1 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z1).color(line);
            int n2 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z1).color(line);
            int n3 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z1).color(line);
            aeropack$mesh.quad(n0, n1, n2, n3);

            // South face (z = z2)
            int s0 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z2).color(line);
            int s1 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z2).color(line);
            int s2 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z2).color(line);
            int s3 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z2).color(line);
            aeropack$mesh.quad(s0, s1, s2, s3);

            // West face (x = x1)
            int w0 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z2).color(line);
            int w1 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y1, z1).color(line);
            int w2 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z1).color(line);
            int w3 = aeropack$mesh.next(); aeropack$mesh.vec3(x1, y2, z2).color(line);
            aeropack$mesh.quad(w0, w1, w2, w3);

            // East face (x = x2)
            int e0 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z1).color(line);
            int e1 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y1, z2).color(line);
            int e2 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z2).color(line);
            int e3 = aeropack$mesh.next(); aeropack$mesh.vec3(x2, y2, z1).color(line);
            aeropack$mesh.quad(e0, e1, e2, e3);

            MeshRenderer.begin()
                .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
                .pipeline(MeteorRenderPipelines.WORLD_COLORED)
                .mesh(aeropack$mesh, aeropack$currentEvent.matrices)
                .end();
        } else {
            aeropack$mesh.begin();
            aeropack$vcp.setColor(new Color(line.r, line.g, line.b, 255));
            aeropack$vcp.setOffset(pos.getX(), pos.getY(), pos.getZ());
            SimpleBlockRenderer.render(pos, state, aeropack$vcp);
            aeropack$vcp.setOffset(0, 0, 0);

            MeshRenderer.begin()
                .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
                .pipeline(MeteorRenderPipelines.WORLD_COLORED)
                .mesh(aeropack$mesh, aeropack$currentEvent.matrices)
                .end();
        }

        AeroShaderHelper.markDirty();
    }

    // ── AeroShaderSource ─────────────────────────────────────────────────

    @Override
    public boolean aeropack$isShaderActive() {
        return aeropack$renderMode.get() == AeroRenderMode.Shader;
    }
}
