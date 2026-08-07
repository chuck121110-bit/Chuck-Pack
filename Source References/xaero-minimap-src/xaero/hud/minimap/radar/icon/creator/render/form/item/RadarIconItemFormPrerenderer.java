package xaero.hud.minimap.radar.icon.creator.render.form.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.class_10017;
import net.minecraft.class_10444;
import net.minecraft.class_11566;
import net.minecraft.class_11684;
import net.minecraft.class_1297;
import net.minecraft.class_1533;
import net.minecraft.class_1542;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_583;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import net.minecraft.class_897;
import net.minecraft.class_308.class_11274;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.radar.icon.creator.RadarIconCreator;
import xaero.hud.minimap.radar.icon.creator.render.form.IRadarIconFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.form.item.RadarIconItemForm;

public class RadarIconItemFormPrerenderer implements IRadarIconFormPrerenderer {
   public boolean requiresEntityModel() {
      return false;
   }

   public boolean isFlipped() {
      return false;
   }

   public boolean isOutlined() {
      return true;
   }

   public <S extends class_10017> boolean prerender(MinimapElementGraphics guiGraphics, class_897<?, ? super S> entityRenderer, S entityRenderState, @Nullable class_583<S> entityModel, class_1297 entity, @Nullable List<ModelRenderTrace> traceResult, RadarIconCreator.Parameters parameters) {
      RadarIconItemForm itemForm = (RadarIconItemForm)parameters.form;
      class_1799 itemStack = this.getItemToRender(entity, itemForm);
      if (itemStack != null && !itemStack.method_7960()) {
         class_10444 itemStackRenderState = new class_10444();
         class_310.method_1551().method_65386().method_65598(itemStackRenderState, itemStack, class_811.field_4317, (class_1937)null, (class_11566)null, 0);
         class_310.method_1551().field_1773.method_71114().method_71034(itemStackRenderState.method_65608() ? class_11274.field_60027 : class_11274.field_60026);
         class_4587 matrixStack = guiGraphics.pose();
         int halfIcon = 32;
         matrixStack.method_46416((float)halfIcon, (float)halfIcon, 1.0F);
         float scale = parameters.scale;
         if (scale < 1.0F) {
            matrixStack.method_22905(scale, scale, 1.0F);
         }

         matrixStack.method_46416(0.0F, 0.0F, -300.0F);
         matrixStack.method_22905(16.0F, -16.0F, 16.0F);
         class_11684 renderDispatcher = class_310.method_1551().field_1773.method_72911();
         itemStackRenderState.method_65604(matrixStack, renderDispatcher.method_73003(), 15728880, class_4608.field_21444, 0);
         renderDispatcher.method_73002();
         guiGraphics.flush();
         return true;
      } else {
         return false;
      }
   }

   private class_1799 getItemToRender(class_1297 entity, RadarIconItemForm itemForm) {
      class_2960 itemKey = itemForm.getItemKey();
      if (itemKey == null) {
         class_1799 selfStack = this.getSelfItem(entity);
         return selfStack == null ? null : new class_1799(selfStack.method_7909());
      } else {
         class_6880.class_6883<class_1792> itemReference = (class_6880.class_6883)class_7923.field_41178.method_10223(itemKey).orElse((Object)null);
         class_1792 item = itemReference != null && itemReference.method_40227() ? (class_1792)itemReference.comp_349() : null;
         return item == null ? null : new class_1799(item);
      }
   }

   private class_1799 getSelfItem(class_1297 entity) {
      if (entity instanceof class_1542 itemEntity) {
         return itemEntity.method_6983();
      } else if (entity instanceof class_1533 itemFrame) {
         return itemFrame.method_6940();
      } else {
         return entity.method_31480();
      }
   }
}
