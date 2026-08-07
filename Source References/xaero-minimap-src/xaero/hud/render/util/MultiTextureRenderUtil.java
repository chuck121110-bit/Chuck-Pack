package xaero.hud.render.util;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_287;
import org.joml.Matrix4f;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;

public class MultiTextureRenderUtil {
   public static void prepareTexturedColoredRect(Matrix4f matrix, float x, float y, int textureX, int textureY, float width, float height, float theight, float factor, float r, float g, float b, float a, GpuTexture texture, MultiTextureRenderTypeRenderer renderer) {
      float f = 1.0F / factor;
      float textureX0 = ((float)textureX + 0.0F) * f;
      float textureX1 = ((float)textureX + width) * f;
      float textureY0 = ((float)textureY + 0.0F) * f;
      float textureY1 = ((float)textureY + theight) * f;
      prepareTexturedColoredRect(matrix, x, y, width, height, textureX0, textureX1, textureY0, textureY1, r, g, b, a, texture, renderer);
   }

   private static void prepareTexturedColoredRect(Matrix4f matrix, float x, float y, float width, float height, float textureX0, float textureX1, float textureY0, float textureY1, float r, float g, float b, float a, GpuTexture texture, MultiTextureRenderTypeRenderer renderer) {
      class_287 vertexBuffer = renderer.begin(texture);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + height, 0.0F).method_22915(r, g, b, a).method_22913(textureX0, textureY0);
      vertexBuffer.method_22918(matrix, x + width, y + height, 0.0F).method_22915(r, g, b, a).method_22913(textureX1, textureY0);
      vertexBuffer.method_22918(matrix, x + width, y + 0.0F, 0.0F).method_22915(r, g, b, a).method_22913(textureX1, textureY1);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + 0.0F, 0.0F).method_22915(r, g, b, a).method_22913(textureX0, textureY1);
   }

   public static void prepareTexturedRect(Matrix4f matrix, float x, float y, int textureX, int textureY, float width, float height, float theight, float factor, GpuTexture texture, MultiTextureRenderTypeRenderer renderer) {
      float f = 1.0F / factor;
      float textureX0 = ((float)textureX + 0.0F) * f;
      float textureX1 = ((float)textureX + width) * f;
      float textureY0 = ((float)textureY + 0.0F) * f;
      float textureY1 = ((float)textureY + theight) * f;
      class_287 vertexBuffer = renderer.begin(texture);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + height, 0.0F).method_22913(textureX0, textureY0);
      vertexBuffer.method_22918(matrix, x + width, y + height, 0.0F).method_22913(textureX1, textureY0);
      vertexBuffer.method_22918(matrix, x + width, y + 0.0F, 0.0F).method_22913(textureX1, textureY1);
      vertexBuffer.method_22918(matrix, x + 0.0F, y + 0.0F, 0.0F).method_22913(textureX0, textureY1);
   }
}
