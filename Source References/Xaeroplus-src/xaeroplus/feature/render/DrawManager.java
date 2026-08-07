package xaeroplus.feature.render;

import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import xaero.common.HudMod;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.module.impl.TickTaskExecutor;

public class DrawManager {
   private final DrawFeatureRegistry registry = new DrawFeatureRegistry();

   public final DrawFeatureRegistry registry() {
      return this.registry;
   }

   public DrawManager() {
      XaeroPlus.EVENT_BUS.register(this);
   }

   @EventHandler
   public void onXaeroWorldChange(XaeroWorldChangeEvent event) {
      TickTaskExecutor.INSTANCE.execute(() -> this.registry.forEach(DrawFeature::invalidateCache));
   }

   public void drawMinimapFeatures(int chunkX, int chunkZ, int tileX, int tileZ, int insideX, int insideZ, final double zoom, final class_4587 matrixStack, final XaeroBufferProvider renderTypeBuffers) {
      if (!HudMod.INSTANCE.isFairPlay()) {
         int cameraBlockX = chunkX * 64 + tileX * 16 + insideX;
         int cameraBlockZ = chunkZ * 64 + tileZ * 16 + insideZ;
         DrawContext ctx = new DrawContext(matrixStack, renderTypeBuffers, zoom, false, new Matrix4f(matrixStack.method_23760().method_23761()), cameraBlockX, cameraBlockZ);
         matrixStack.method_22903();
         matrixStack.method_46416((float)(-cameraBlockX), (float)(-cameraBlockZ), 0.0F);
         this.registry.forEach((feature) -> feature.render(ctx));
         matrixStack.method_22909();
      }
   }

   public void drawWorldMapFeatures(final int flooredCameraX, final int flooredCameraZ, final class_4587 matrixStack, final double fboScale, final XaeroBufferProvider renderTypeBuffers) {
      if (!HudMod.INSTANCE.isFairPlay()) {
         Matrix4f untranslatedMapViewMatrix = (new Matrix4f(matrixStack.method_23760().method_23761())).translate(0.0F, 0.0F, 1.0F);
         DrawContext ctx = new DrawContext(matrixStack, renderTypeBuffers, fboScale, true, untranslatedMapViewMatrix, flooredCameraX, flooredCameraZ);
         matrixStack.method_22903();
         matrixStack.method_46416((float)(-flooredCameraX), (float)(-flooredCameraZ), 1.0F);
         this.registry.forEach((feature) -> feature.render(ctx));
         matrixStack.method_22909();
      }
   }
}
