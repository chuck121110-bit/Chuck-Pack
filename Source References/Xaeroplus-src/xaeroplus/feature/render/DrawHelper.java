package xaeroplus.feature.render;

import net.minecraft.class_4588;

public class DrawHelper {
   public static void addColoredLineQuadToExistingBuffer(class_4588 vertexBuffer, float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
      vertexBuffer.method_22912(x1, y1, 0.0F).method_22915(r, g, b, a).method_22913(x2, y2);
      vertexBuffer.method_22912(x1, y1, 0.0F).method_22915(r, g, b, a).method_22913(x2, y2);
      vertexBuffer.method_22912(x1, y1, 0.0F).method_22915(r, g, b, a).method_22913(x2, y2);
      vertexBuffer.method_22912(x1, y1, 0.0F).method_22915(r, g, b, a).method_22913(x2, y2);
   }

   public static void addColoredEllipseQuadToExistingBuffer(class_4588 vertexBuffer, float centerX, float centerZ, float radiusX, float radiusZ, float r, float g, float b, float a) {
      for(int i = 0; i < 4; ++i) {
         vertexBuffer.method_22912(centerX, centerZ, 0.0F).method_22913(radiusX, radiusZ).method_22915(r, g, b, a);
      }

   }
}
