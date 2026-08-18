package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.render.AeroShaderHelper;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StorageESP.class)
public class StorageESPMixin {

    @Redirect(
        method = "onRender",
        at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/utils/render/postprocess/PostProcessShader;render()V")
    )
    private void chuckpack\$deferRender(PostProcessShader shader) {
        AeroShaderHelper.markDirty();
    }
}
