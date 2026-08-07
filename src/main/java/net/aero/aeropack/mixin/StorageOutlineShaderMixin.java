package net.aero.aeropack.mixin;

import net.aero.aeropack.render.AeroShaderSource;

import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import net.aero.aeropack.modules.render.DeepslateESP;
import net.aero.aeropack.modules.render.HoleTunnelStairsESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.world.Excavator;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.systems.modules.world.VeinMiner;
import meteordevelopment.meteorclient.utils.render.postprocess.OutlineUniforms;
import meteordevelopment.meteorclient.utils.render.postprocess.StorageOutlineShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StorageOutlineShader.class)
public class StorageOutlineShaderMixin {

    @Inject(method = "shouldDraw", at = @At("RETURN"), cancellable = true)
    private void aeropack$shouldDraw(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (aeropack$activeSource() != null) cir.setReturnValue(true);
    }

    @Inject(method = "setupPass", at = @At("HEAD"), cancellable = true)
    private void aeropack$setupPass(MeshRenderer renderer, CallbackInfo ci) {
        StorageESP storageESP = Modules.get().get(StorageESP.class);
        if (storageESP.isShader()) return;

        AeroShaderSource source = aeropack$activeSource();
        if (source == null) return;

        renderer.uniform("OutlineData", OutlineUniforms.write(
            storageESP.outlineWidth.get(),
            storageESP.fillOpacity.get() / 255.0f,
            storageESP.shapeMode.get().ordinal(),
            storageESP.glowMultiplier.get().floatValue()
        ));
        ci.cancel();
    }

    private static AeroShaderSource aeropack$activeSource() {
        Object blockESP = Modules.get().get(BlockESP.class);
        if (blockESP instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        Object nuker = Modules.get().get(Nuker.class);
        if (nuker instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        Object veinMiner = Modules.get().get(VeinMiner.class);
        if (veinMiner instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        Object excavator = Modules.get().get(Excavator.class);
        if (excavator instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        Object deepslateESP = Modules.get().get(DeepslateESP.class);
        if (deepslateESP instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        Object holeTunnelStairsESP = Modules.get().get(HoleTunnelStairsESP.class);
        if (holeTunnelStairsESP instanceof AeroShaderSource src && src.aeropack$isShaderActive()) return src;

        return null;
    }
}
