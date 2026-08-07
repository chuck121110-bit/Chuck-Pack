package net.aero.aeropack.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.aero.aeropack.modules.render.TrueSight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class TrueSightEntityMixin {
    @ModifyReturnValue(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"))
    private boolean aero$revealInvisible(boolean original) {
        if (!Utils.canUpdate()) return original;
        TrueSight trueSight = Modules.get().get(TrueSight.class);
        if (trueSight.isActive() && trueSight.shouldBeVisible((Entity) (Object) this)) return false;
        return original;
    }
}
