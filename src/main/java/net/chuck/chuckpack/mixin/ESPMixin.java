package net.chuck.chuckpack.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meteordevelopment.meteorclient.systems.modules.render.ESP;

@Mixin(value = ESP.class, remap = false)
public abstract class ESPMixin {

    @Inject(method = "shouldSkip(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void chuckpack$frustumCullEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        Entity camera = mc.getCameraEntity();
        if (camera == null) return;

        AABB AABB = entity.getBoundingBox();

        double camX = camera.getX();
        double camY = camera.getY();
        double camZ = camera.getZ();

        double nearestX = Math.max(AABB.minX, Math.min(camX, AABB.maxX));
        double nearestY = Math.max(AABB.minY, Math.min(camY, AABB.maxY));
        double nearestZ = Math.max(AABB.minZ, Math.min(camZ, AABB.maxZ));

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
