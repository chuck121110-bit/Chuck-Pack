package xaeroplus.feature.render.shaders;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import xaero.lib.client.graphics.shader.BuiltInCustomUniformValueTypes;
import xaero.lib.client.graphics.shader.CustomUniform;

public class XaeroPlusShaders {
   public static final RenderPipeline HIGHLIGHT_PIPELINE;
   public static final RenderPipeline MULTI_COLOR_HIGHLIGHT_PIPELINE;
   public static final RenderPipeline LINES_PIPELINE;
   public static final RenderPipeline ELLIPSES_PIPELINE;
   public static Integer cachedTransparentBackground;
   public static final CustomUniform<Integer> TRANSPARENT_WM_BACKGROUND_UNIFORM;
   public static final float[] LINES_FRAME_SIZE;
   public static final float[] ELLIPSES_FRAME_SIZE;

   public static void setTransparentWMBackground(boolean value) {
      int intValue = value ? 1 : 0;
      if (cachedTransparentBackground == null || cachedTransparentBackground != intValue) {
         cachedTransparentBackground = intValue;
         TRANSPARENT_WM_BACKGROUND_UNIFORM.setValue(intValue);
      }

   }

   public static void setLinesFrameSize(float width, float height) {
      LINES_FRAME_SIZE[0] = width;
      LINES_FRAME_SIZE[1] = height;
   }

   public static void setEllipsesFrameSize(final float width, final float height) {
      ELLIPSES_FRAME_SIZE[0] = width;
      ELLIPSES_FRAME_SIZE[1] = height;
   }

   public static void setFrameSize(final float width, final float height) {
      setLinesFrameSize(width, height);
      setEllipsesFrameSize(width, height);
   }

   static {
      HIGHLIGHT_PIPELINE = RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655("xaeroplus", "pipeline/highlights")).withVertexShader(class_2960.method_60655("xaeroplus", "highlights")).withFragmentShader(class_2960.method_60655("xaeroplus", "highlights")).withVertexFormat(class_290.field_1592, class_5596.field_27382).withPolygonMode(PolygonMode.FILL).withUniform("HighlightTransforms", class_10789.field_60031).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)).build();
      MULTI_COLOR_HIGHLIGHT_PIPELINE = RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655("xaeroplus", "pipeline/multi_color_highlights")).withVertexShader(class_2960.method_60655("xaeroplus", "multi_color_highlights")).withFragmentShader(class_2960.method_60655("xaeroplus", "multi_color_highlights")).withVertexFormat(class_290.field_1576, class_5596.field_27382).withPolygonMode(PolygonMode.FILL).withUniform("MultiColorHighlightTransforms", class_10789.field_60031).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)).build();
      LINES_PIPELINE = RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655("xaeroplus", "pipeline/lines")).withVertexShader(class_2960.method_60655("xaeroplus", "lines")).withFragmentShader(class_2960.method_60655("xaeroplus", "lines")).withVertexFormat(class_290.field_1575, class_5596.field_27382).withPolygonMode(PolygonMode.FILL).withUniform("LinesTransforms", class_10789.field_60031).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)).withCull(false).build();
      ELLIPSES_PIPELINE = RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655("xaeroplus", "pipeline/ellipses")).withVertexShader(class_2960.method_60655("xaeroplus", "ellipses")).withFragmentShader(class_2960.method_60655("xaeroplus", "ellipses")).withVertexFormat(class_290.field_1575, class_5596.field_27382).withPolygonMode(PolygonMode.FILL).withUniform("EllipsesTransforms", class_10789.field_60031).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)).withCull(false).build();
      cachedTransparentBackground = null;
      TRANSPARENT_WM_BACKGROUND_UNIFORM = new CustomUniform(new RenderPipeline.UniformDescription("TransparentBackgroundBlock", class_10789.field_60031), BuiltInCustomUniformValueTypes.INT, 32);
      LINES_FRAME_SIZE = new float[2];
      ELLIPSES_FRAME_SIZE = new float[2];
   }
}
