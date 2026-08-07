package xaero.hud.render.pipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_10149;
import net.minecraft.class_2960;

public class RenderPipelineWrapper extends RenderPipeline {
   private final RenderPipeline original;
   private Optional<class_2960> vertexShaderOverride;
   private Optional<class_2960> fragmentShaderOverride;
   private Optional<class_10149> shaderDefinesOverride;
   private Optional<List<String>> samplersOverride;
   private Optional<List<RenderPipeline.UniformDescription>> uniformsOverride;
   private Optional<DepthTestFunction> depthTestFunctionOverride;
   private Optional<PolygonMode> polygonModeOverride;
   private Optional<Boolean> cullOverride;
   private Optional<LogicOp> colorLogicOverride;
   private Optional<Optional<BlendFunction>> blendFunctionOverride;
   private Optional<Boolean> writeColorOverride;
   private Optional<Boolean> writeAlphaOverride;
   private Optional<Boolean> writeDepthOverride;
   private Optional<VertexFormat> vertexFormatOverride;
   private Optional<VertexFormat.class_5596> vertexFormatModeOverride;
   private Optional<Float> depthBiasScaleFactorOverride;
   private Optional<Float> depthBiasConstantOverride;
   private Optional<Integer> sortKeyOverride;

   public RenderPipelineWrapper(class_2960 location, RenderPipeline original) {
      super(location, (class_2960)null, (class_2960)null, (class_10149)null, (List)null, (List)null, (Optional)null, (DepthTestFunction)null, (PolygonMode)null, false, false, false, false, (LogicOp)null, (VertexFormat)null, (VertexFormat.class_5596)null, 0.0F, 0.0F, 0);
      this.original = original;
   }

   public DepthTestFunction getDepthTestFunction() {
      return this.depthTestFunctionOverride != null ? (DepthTestFunction)this.depthTestFunctionOverride.get() : this.original.getDepthTestFunction();
   }

   public PolygonMode getPolygonMode() {
      return this.polygonModeOverride != null ? (PolygonMode)this.polygonModeOverride.get() : this.original.getPolygonMode();
   }

   public boolean isCull() {
      return this.cullOverride != null ? (Boolean)this.cullOverride.get() : this.original.isCull();
   }

   public LogicOp getColorLogic() {
      return this.colorLogicOverride != null ? (LogicOp)this.colorLogicOverride.get() : this.original.getColorLogic();
   }

   public Optional<BlendFunction> getBlendFunction() {
      return this.blendFunctionOverride != null ? (Optional)this.blendFunctionOverride.get() : this.original.getBlendFunction();
   }

   public boolean isWriteColor() {
      return this.writeColorOverride != null ? (Boolean)this.writeColorOverride.get() : this.original.isWriteColor();
   }

   public boolean isWriteAlpha() {
      return this.writeAlphaOverride != null ? (Boolean)this.writeAlphaOverride.get() : this.original.isWriteAlpha();
   }

   public boolean isWriteDepth() {
      return this.writeDepthOverride != null ? (Boolean)this.writeDepthOverride.get() : this.original.isWriteDepth();
   }

   public float getDepthBiasScaleFactor() {
      return this.depthBiasScaleFactorOverride != null ? (Float)this.depthBiasScaleFactorOverride.get() : this.original.getDepthBiasScaleFactor();
   }

   public float getDepthBiasConstant() {
      return this.depthBiasConstantOverride != null ? (Float)this.depthBiasConstantOverride.get() : this.original.getDepthBiasConstant();
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormatOverride != null ? (VertexFormat)this.vertexFormatOverride.get() : this.original.getVertexFormat();
   }

   public VertexFormat.class_5596 getVertexFormatMode() {
      return this.vertexFormatModeOverride != null ? (VertexFormat.class_5596)this.vertexFormatModeOverride.get() : this.original.getVertexFormatMode();
   }

   public class_2960 getVertexShader() {
      return this.vertexShaderOverride != null ? (class_2960)this.vertexShaderOverride.get() : this.original.getVertexShader();
   }

   public class_2960 getFragmentShader() {
      return this.fragmentShaderOverride != null ? (class_2960)this.fragmentShaderOverride.get() : this.original.getFragmentShader();
   }

   public class_10149 getShaderDefines() {
      return this.shaderDefinesOverride != null ? (class_10149)this.shaderDefinesOverride.get() : this.original.getShaderDefines();
   }

   public List<String> getSamplers() {
      return this.samplersOverride != null ? (List)this.samplersOverride.get() : this.original.getSamplers();
   }

   public List<RenderPipeline.UniformDescription> getUniforms() {
      return this.uniformsOverride != null ? (List)this.uniformsOverride.get() : this.original.getUniforms();
   }

   public int getSortKey() {
      return this.sortKeyOverride != null ? (Integer)this.sortKeyOverride.get() : this.original.getSortKey();
   }

   public boolean wantsDepthTexture() {
      if (this.depthTestFunctionOverride == null && this.depthBiasConstantOverride == null && this.depthBiasScaleFactorOverride == null && this.writeDepthOverride == null) {
         return this.original.wantsDepthTexture();
      } else {
         return this.getDepthTestFunction() != DepthTestFunction.NO_DEPTH_TEST || this.getDepthBiasConstant() != 0.0F || this.getDepthBiasScaleFactor() != 0.0F || this.isWriteDepth();
      }
   }

   public void setVertexShaderOverride(class_2960 vertexShaderOverride) {
      this.vertexShaderOverride = Optional.of(vertexShaderOverride);
   }

   public void setFragmentShaderOverride(class_2960 fragmentShaderOverride) {
      this.fragmentShaderOverride = Optional.of(fragmentShaderOverride);
   }

   public void setShaderDefinesOverride(class_10149 shaderDefinesOverride) {
      this.shaderDefinesOverride = Optional.of(shaderDefinesOverride);
   }

   public void setSamplersOverride(List<String> samplersOverride) {
      this.samplersOverride = Optional.of(samplersOverride);
   }

   public void setUniformsOverride(List<RenderPipeline.UniformDescription> uniformsOverride) {
      this.uniformsOverride = Optional.of(uniformsOverride);
   }

   public void setDepthTestFunctionOverride(DepthTestFunction depthTestFunctionOverride) {
      this.depthTestFunctionOverride = Optional.of(depthTestFunctionOverride);
   }

   public void setPolygonModeOverride(PolygonMode polygonModeOverride) {
      this.polygonModeOverride = Optional.of(polygonModeOverride);
   }

   public void setCullOverride(Boolean cullOverride) {
      this.cullOverride = Optional.of(cullOverride);
   }

   public void setColorLogicOverride(LogicOp colorLogicOverride) {
      this.colorLogicOverride = Optional.of(colorLogicOverride);
   }

   public void setBlendFunctionOverride(Optional<BlendFunction> blendFunctionOverride) {
      this.blendFunctionOverride = Optional.of(blendFunctionOverride);
   }

   public void setWriteColorOverride(boolean writeColorOverride) {
      this.writeColorOverride = Optional.of(writeColorOverride);
   }

   public void setWriteAlphaOverride(boolean writeAlphaOverride) {
      this.writeAlphaOverride = Optional.of(writeAlphaOverride);
   }

   public void setWriteDepthOverride(boolean writeDepthOverride) {
      this.writeDepthOverride = Optional.of(writeDepthOverride);
   }

   public void setVertexFormatOverride(VertexFormat vertexFormatOverride) {
      this.vertexFormatOverride = Optional.of(vertexFormatOverride);
   }

   public void setVertexFormatModeOverride(VertexFormat.class_5596 vertexFormatModeOverride) {
      this.vertexFormatModeOverride = Optional.of(vertexFormatModeOverride);
   }

   public void setDepthBiasScaleFactorOverride(float depthBiasScaleFactorOverride) {
      this.depthBiasScaleFactorOverride = Optional.of(depthBiasScaleFactorOverride);
   }

   public void setDepthBiasConstantOverride(float depthBiasConstantOverride) {
      this.depthBiasConstantOverride = Optional.of(depthBiasConstantOverride);
   }

   public void setSortKeyOverride(int sortKeyOverride) {
      this.sortKeyOverride = Optional.of(sortKeyOverride);
   }
}
