package xaero.hud.minimap.radar.icon.creator.render.trace;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1058;
import net.minecraft.class_12245;
import net.minecraft.class_12250;
import net.minecraft.class_3879;
import net.minecraft.class_630;
import xaero.lib.client.graphics.ITextureBinding;

public class ModelRenderTrace {
   public final class_3879 model;
   public final Map<String, ITextureBinding> textures;
   public final class_12250 textureTransform;
   public final boolean useOverlay;
   public final boolean affectsCrumbling;
   public final boolean sortOnUpload;
   public final class_12245 layeringTransform;
   public final class_1058 renderAtlasSprite;
   public final RenderPipeline layerPipeline;
   public int color;
   public boolean allVisible;
   private HashMap<class_630, ModelPartRenderTrace> visibleParts;

   public ModelRenderTrace(class_3879 model, Map<String, ITextureBinding> textures, class_12250 textureTransform, boolean useOverlay, boolean affectsCrumbling, boolean sortOnUpload, class_12245 layeringTransform, class_1058 renderAtlasSprite, RenderPipeline layerPipeline, int color) {
      this.model = model;
      this.textures = textures;
      this.textureTransform = textureTransform;
      this.useOverlay = useOverlay;
      this.affectsCrumbling = affectsCrumbling;
      this.sortOnUpload = sortOnUpload;
      this.layeringTransform = layeringTransform;
      this.renderAtlasSprite = renderAtlasSprite;
      this.layerPipeline = layerPipeline;
      this.color = color;
   }

   public String toString() {
      String var10000 = String.valueOf(this.model);
      return var10000 + " " + String.valueOf(this.layerPipeline.getLocation());
   }

   public void addVisibleModelPart(class_630 part, int color) {
      if (this.visibleParts == null) {
         this.visibleParts = new HashMap();
      }

      this.visibleParts.put(part, new ModelPartRenderTrace(part, color));
   }

   public ModelPartRenderTrace getModelPartRenderInfo(class_630 part) {
      ModelPartRenderTrace mprdi = this.visibleParts == null ? null : (ModelPartRenderTrace)this.visibleParts.get(part);
      if (mprdi == null && this.allVisible) {
         mprdi = new ModelPartRenderTrace(part, this.color);
      }

      return mprdi;
   }

   public boolean isEmpty() {
      return !this.allVisible && (this.visibleParts == null || this.visibleParts.isEmpty());
   }

   public boolean sameVisibility(ModelRenderTrace other) {
      HashMap<class_630, ModelPartRenderTrace> otherVisibleParts = other.visibleParts;
      if (this.visibleParts == null != (otherVisibleParts == null)) {
         return false;
      } else if (this.visibleParts == null) {
         return true;
      } else if (this.visibleParts.size() != otherVisibleParts.size()) {
         return false;
      } else {
         for(class_630 key : this.visibleParts.keySet()) {
            if (!otherVisibleParts.containsKey(key)) {
               return false;
            }
         }

         return true;
      }
   }
}
