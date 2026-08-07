package net.aero.aeropack.mixin;

import net.aero.aeropack.render.AeroRenderMode;
import net.aero.aeropack.render.AeroShaderHelper;
import net.aero.aeropack.render.AeroShaderSource;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.render.MeshBuilderVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = Nuker.class, remap = false)
public abstract class NukerMixin implements AeroShaderSource {

    private static final IVisible HIDDEN = () -> false;

    @Shadow private Setting<SettingColor> sideColor;
    @Shadow private Setting<SettingColor> lineColor;
    @Shadow private SettingGroup sgGeneral;
    @Shadow private SettingGroup sgRender;
    @Shadow private Setting<Boolean> enableRenderBreaking;

    @Unique private Setting<AeroRenderMode> aeropack$renderMode;

    @Unique private final Map<BlockPos, Integer> aeropack$shaderBlocks = new HashMap<>();
    @Unique private final MeshBuilder aeropack$mesh = new MeshBuilder(MeteorRenderPipelines.WORLD_COLORED);
    @Unique private final MeshBuilderVertexConsumerProvider aeropack$vcp = new MeshBuilderVertexConsumerProvider(aeropack$mesh);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void renameDuplicateSettings(CallbackInfo ci) {
        renameSetting(sideColor, "breaking-side-color", "Breaking Side Color");
        renameSetting(lineColor, "breaking-line-color", "Breaking Line Color");
        hideMaxBlocksPerTick();
        aeropack$addShaderSettings();
    }

    @Unique
    private void aeropack$addShaderSettings() {
        aeropack$renderMode = sgRender.add(new EnumSetting.Builder<AeroRenderMode>()
            .name("aeropack-render-mode")
            .description("Box ESP draws a normal box outline. Shader uses the same post-process outline shader as Storage ESP.")
            .defaultValue(AeroRenderMode.BoxESP)
            .build()
        );
    }

    // ── Redirect the box-mode "ticking block" render call ──────────────────
    // In Shader mode we don't call the real renderTickingBlock at all -
    // instead we track the position ourselves and draw it via the shared
    // outline shader in aeropack$renderShaderBlocks below.
    @Redirect(
        method = "lambda$onTickPre$13",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/utils/render/RenderUtils;renderTickingBlock(Lnet/minecraft/util/math/BlockPos;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/utils/render/color/Color;Lmeteordevelopment/meteorclient/renderer/ShapeMode;IIZZ)V"
        )
    )
    private void aeropack$redirectTickingBlock(BlockPos pos, Color side, Color line, ShapeMode shapeMode, int startFade, int endFade, boolean bool1, boolean bool2) {
        if (aeropack$renderMode.get() == AeroRenderMode.Shader) {
            aeropack$shaderBlocks.put(pos.toImmutable(), 8);
        } else {
            RenderUtils.renderTickingBlock(pos, side, line, shapeMode, startFade, endFade, bool1, bool2);
        }
    }

    @Inject(method = "onTickPre", at = @At("TAIL"))
    private void aeropack$expireShaderBlocks(TickEvent.Pre event, CallbackInfo ci) {
        if (aeropack$shaderBlocks.isEmpty()) return;
        aeropack$shaderBlocks.replaceAll((pos, ticksLeft) -> ticksLeft - 1);
        aeropack$shaderBlocks.values().removeIf(ticksLeft -> ticksLeft <= 0);
    }

    @Inject(method = "onRender", at = @At("TAIL"))
    private void aeropack$renderShaderBlocks(Render3DEvent event, CallbackInfo ci) {
        if (!enableRenderBreaking.get() || aeropack$renderMode.get() != AeroRenderMode.Shader || aeropack$shaderBlocks.isEmpty()) return;

        aeropack$mesh.begin();
        Color lc = lineColor.get();
        aeropack$vcp.setColor(new Color(lc.r, lc.g, lc.b, 255));

        for (BlockPos pos : aeropack$shaderBlocks.keySet()) {
            BlockState state = mc.level.getBlockState(pos);
            if (state.isAir()) continue;

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
    }

    // ── AeroShaderSource ─────────────────────────────────────────────────

    @Override
    public boolean aeropack$isShaderActive() {
        return enableRenderBreaking.get() && aeropack$renderMode.get() == AeroRenderMode.Shader;
    }

    // ── Existing setting-rename / hide logic ────────────────────────────

    private void hideMaxBlocksPerTick() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            Setting<?> maxBlocks = sgGeneral.get("max-blocks-per-tick");
            if (maxBlocks == null) return;

            Field visibleField = Setting.class.getDeclaredField("visible");
            long visibleOffset = unsafe.objectFieldOffset(visibleField);
            unsafe.putObject(maxBlocks, visibleOffset, HIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void renameSetting(Setting<?> setting, String newName, String newTitle) {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            Field nameField = Setting.class.getDeclaredField("name");
            long nameOffset = unsafe.objectFieldOffset(nameField);
            unsafe.putObject(setting, nameOffset, newName);

            Field titleField = Setting.class.getDeclaredField("title");
            long titleOffset = unsafe.objectFieldOffset(titleField);
            unsafe.putObject(setting, titleOffset, newTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
