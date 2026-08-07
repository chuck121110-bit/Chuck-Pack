package net.aero.aeropack.mixin;

import net.aero.aeropack.render.AeroShaderHelper;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StorageESP.class)
public class StorageESPMixin {

    @Redirect(
        method = "onRender",
        at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/renderer/MeshRenderer;clearColor(Lmeteordevelopment/meteorclient/utils/render/color/Color;)Lmeteordevelopment/meteorclient/renderer/MeshRenderer;")
    )
    private MeshRenderer aeropack$skipClear(MeshRenderer renderer, Color color) {
        return renderer;
    }

    @Redirect(
        method = "onRender",
        at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/utils/render/postprocess/PostProcessShader;render()V")
    )
    private void aeropack$deferRender(PostProcessShader shader) {
        AeroShaderHelper.markDirty();
    }
}
