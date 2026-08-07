package xaero.common.graphics.renderer.multitexture;

import com.mojang.blaze3d.textures.GpuTexture;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.function.Consumer;
import net.minecraft.class_10865;
import net.minecraft.class_1921;
import xaero.common.graphics.OpenGlHelper;
import xaero.lib.client.graphics.util.IPlatformRenderDeviceHelper;
import xaero.lib.platform.Services;

public class MultiTextureRenderTypeRendererProvider {
   private Deque<MultiTextureRenderTypeRenderer> availableRenderers = new ArrayDeque();
   private HashSet<MultiTextureRenderTypeRenderer> usedRenderers;

   public MultiTextureRenderTypeRendererProvider(int rendererCount) {
      for(int i = 0; i < rendererCount; ++i) {
         this.availableRenderers.add(new MultiTextureRenderTypeRenderer());
      }

      this.usedRenderers = new HashSet();
   }

   public MultiTextureRenderTypeRenderer getRenderer(Consumer<GpuTexture> textureBinder, class_1921 renderType) {
      return this.getRenderer(textureBinder, (Consumer)null, renderType);
   }

   public MultiTextureRenderTypeRenderer getRenderer(Consumer<GpuTexture> textureBinder, Consumer<GpuTexture> textureFinalizer, class_1921 renderType) {
      if (this.availableRenderers.isEmpty()) {
         throw new RuntimeException("No renderers available!");
      } else {
         MultiTextureRenderTypeRenderer renderer = (MultiTextureRenderTypeRenderer)this.availableRenderers.removeFirst();
         renderer.init(textureBinder, textureFinalizer, renderType);
         this.usedRenderers.add(renderer);
         return renderer;
      }
   }

   public void draw(MultiTextureRenderTypeRenderer renderer) {
      if (this.usedRenderers.remove(renderer)) {
         renderer.draw();
         this.availableRenderers.add(renderer);
      } else {
         throw new RuntimeException("The renderer requested for drawing was not provided by this provider!");
      }
   }

   public static void defaultTextureBind(GpuTexture texture) {
      IPlatformRenderDeviceHelper renderDeviceUtil = Services.PLATFORM.getRenderDeviceHelper();
      if (!(renderDeviceUtil.getRealDevice() instanceof class_10865)) {
         throw new IllegalStateException("Unsupported non-OpenGL rendering detected!");
      } else {
         GpuTexture realTexture = renderDeviceUtil.getRealTexture(texture);
         OpenGlHelper.bindTexture(0, realTexture);
      }
   }
}
