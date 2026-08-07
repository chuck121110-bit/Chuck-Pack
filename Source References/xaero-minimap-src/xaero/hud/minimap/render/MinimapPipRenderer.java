package xaero.hud.minimap.render;

import net.minecraft.class_11239;
import net.minecraft.class_11246;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;

public class MinimapPipRenderer<T extends MinimapPipRenderState> extends class_11239<T> {
   private final Class<T> stateClass;

   public MinimapPipRenderer(Class<T> stateClass, class_4597.class_4598 bufferSource) {
      super(bufferSource);
      this.stateClass = stateClass;
   }

   public Class<T> method_70903() {
      return this.stateClass;
   }

   public void prepare(T state, class_11246 guiRenderState, int guiScale) {
      if (!state.prepared) {
         state.prepared = true;
         super.method_70913(state, guiRenderState, guiScale);
      }
   }

   protected void renderToTexture(T state, class_4587 poseStack) {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         int horizontalPadding = state.getScaledHorizontalPadding();
         int verticalPadding = state.getScaledVerticalPadding();
         minimapSession.getProcessor().onRender(horizontalPadding, verticalPadding, state.getWidth(), state.getHeight(), state.getScale(), state.getMinimapScale(), state.getSize(), state.getBoxSize(), state.getPartial(), state.getCvc(), state.comp_4124() - state.comp_4122(), state.comp_4125() - state.comp_4123());
      }
   }

   protected String method_70906() {
      return "minimap";
   }
}
