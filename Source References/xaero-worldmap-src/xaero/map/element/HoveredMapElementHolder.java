package xaero.map.element;

import java.util.ArrayList;
import net.minecraft.class_310;
import xaero.lib.client.config.ClientConfigManager;
import xaero.map.WorldMap;
import xaero.map.element.render.ElementRenderer;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.util.DistanceUtils;

public class HoveredMapElementHolder<E, C> implements IRightClickableElement {
   private final E element;
   private final ElementRenderer<E, C, ?> renderer;

   public HoveredMapElementHolder(E element, ElementRenderer<E, C, ?> renderer) {
      this.element = element;
      this.renderer = renderer;
   }

   public ArrayList<RightClickOption> getRightClickOptions() {
      ArrayList<RightClickOption> options = this.renderer.getReader().getRightClickOptions(this.element, this);
      if (options == null) {
         return null;
      } else {
         ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
         class_310 minecraft = class_310.method_1551();
         float partialTicks = minecraft.method_61966().method_60637(true);
         double elementX = this.renderer.getReader().getRenderX(this.element, this.renderer.getContext(), partialTicks);
         double elementY = this.renderer.getReader().getRenderY(this.element, this.renderer.getContext(), partialTicks);
         double elementZ = this.renderer.getReader().getRenderZ(this.element, this.renderer.getContext(), partialTicks);
         DistanceUtils.addDistanceRightClickOption(elementX, elementY, elementZ, this.renderer.getReader().hasYCoordinate(), this, minecraft.method_1560(), partialTicks, configManager, options);
         return options;
      }
   }

   public boolean isRightClickValid() {
      return this.renderer != null && this.renderer.getReader().isRightClickValid(this.element);
   }

   public int getRightClickTitleBackgroundColor() {
      return this.renderer.getReader().getRightClickTitleBackgroundColor(this.element);
   }

   public E getElement() {
      return this.element;
   }

   public ElementRenderer<E, C, ?> getRenderer() {
      return this.renderer;
   }

   public boolean is(Object o) {
      return this.element == o;
   }
}
