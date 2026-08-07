package net.aero.aeropack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meteordevelopment.meteorclient.systems.modules.render.ESP;

@Mixin(value = ESP.class, remap = false)
public abstract class ESPMixin {

    @Inject(method = "shouldSkip(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void aeropack$frustumCullEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        Entity camera = mc.getCameraEntity();
        if (camera == null) return;

        Box box = entity.getBoundingBox();

        double camX = camera.getX();
        double camY = camera.getY();
        double camZ = camera.getZ();

        double nearestX = Math.max(box.minX, Math.min(camX, box.maxX));
        double nearestY = Math.max(box.minY, Math.min(camY, box.maxY));
        double nearestZ = Math.max(box.minZ, Math.min(camZ, box.maxZ));

        double dx = nearestX - camX;
        double dy = nearestY - camY;
        double dz = nearestZ - camZ;

        double yaw = Math.toRadians(camera.getYRot());
        double pitch = Math.toRadians(camera.getXRot());

        double forwardX = -Math.sin(yaw) * Math.cos(pitch);
        double forwardY = -Math.sin(pitch);
        double forwardZ = Math.cos(yaw) * Math.cos(pitch);

        double dot = dx * forwardX + dy * forwardY + dz * forwardZ;

        if (dot < -1.0) {
            cir.setReturnValue(true);
        }
    }
}
