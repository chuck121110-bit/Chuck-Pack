package xaero.hud.compat.mods;

import net.minecraft.class_4587;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

public class ImmediatelyFastHelper {
   public static void triggerBatchingBuffersFlush(class_4587 matrixStack) {
      ImmediateRenderUtil.coloredRectangle(matrixStack.method_23760().method_23761(), 0.0F, 0.0F, 0.0F, 0.0F, 0);
   }
}
