package xaero.map;

import net.minecraft.class_4587;
import xaero.map.graphics.CustomRenderTypes;
import xaero.map.region.texture.BranchTextureRenderer;

public class WorldMapClientOnly {
   public BranchTextureRenderer branchTextureRenderer;
   private class_4587 mapScreenPoseStack;

   public void preInit(String modId) {
   }

   public void postInit() {
      CustomRenderTypes.applyFixedOrder();
      this.branchTextureRenderer = new BranchTextureRenderer();
      this.mapScreenPoseStack = new class_4587();
   }

   public class_4587 getMapScreenPoseStack() {
      return this.mapScreenPoseStack;
   }
}
