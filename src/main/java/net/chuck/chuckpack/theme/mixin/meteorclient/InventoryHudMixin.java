package net.chuck.chuckpack.theme.mixin.meteorclient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.chuck.chuckpack.theme.gui.renderer.WorldFramebufferCapture;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.InventoryHud;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = InventoryHud.class, remap = false)
public abstract class InventoryHudMixin {

    @Unique private static final Color WHITE = new Color(255, 255, 255, 255);

    @Unique private SettingGroup exeter$sgEffects;
    @Unique private Setting<Boolean> exeter$blur;
    @Unique private Setting<Boolean> exeter$outline;
    @Unique private Setting<SettingColor> exeter$outlineColor;
    @Unique private Setting<Double> exeter$outlineWidth;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        InventoryHud self = (InventoryHud) (Object) this;

        exeter$sgEffects = self.settings.createGroup("Effects");

        exeter$blur = exeter$sgEffects.add(new BoolSetting.Builder()
            .name("blur")
            .description("Applies a blur effect behind the inventory background.")
            .defaultValue(false)
            .build()
        );

        exeter$outline = exeter$sgEffects.add(new BoolSetting.Builder()
            .name("outline")
            .description("Draws an outline around the inventory.")
            .defaultValue(false)
            .build()
        );

        exeter$outlineColor = exeter$sgEffects.add(new ColorSetting.Builder()
            .name("outline-color")
            .description("Color of the outline.")
            .visible(exeter$outline::get)
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
        );

        exeter$outlineWidth = exeter$sgEffects.add(new DoubleSetting.Builder()
            .name("outline-width")
            .description("Width of the outline in pixels.")
            .visible(exeter$outline::get)
            .defaultValue(1)
            .min(0.5)
            .sliderRange(0.5, 5)
            .build()
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(HudRenderer renderer, CallbackInfo ci) {
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(HudRenderer renderer, CallbackInfo ci) {
        if (!exeter$outline.get()) return;

        InventoryHud self = (InventoryHud) (Object) this;
        double x = self.x, y = self.y, w = self.getWidth(), h = self.getHeight();
        Color color = exeter$outlineColor.get();
        double s = exeter$outlineWidth.get();

        renderer.quad(x - s, y - s, w + s * 2, s, color);
        renderer.quad(x - s, y + h,     w + s * 2, s, color);
        renderer.quad(x - s, y,         s, h,         color);
        renderer.quad(x + w, y,         s, h,         color);
    }
}
