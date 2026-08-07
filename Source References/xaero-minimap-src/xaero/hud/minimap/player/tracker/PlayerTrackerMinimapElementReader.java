package xaero.hud.minimap.player.tracker;

import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_640;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;

public class PlayerTrackerMinimapElementReader extends MinimapElementReader<PlayerTrackerMinimapElement<?>, PlayerTrackerMinimapElementRenderContext> {
   public boolean isHidden(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context) {
      return context.renderEntityDimId != element.getDimension() && context.mapDimId != element.getDimension();
   }

   public double getRenderX(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      class_310 mc = class_310.method_1551();
      class_1657 clientPlayer = mc.field_1687.method_18470(element.getPlayerId());
      return clientPlayer == null ? element.getX() : EntityUtils.getEntityX(clientPlayer, partialTicks);
   }

   public double getRenderY(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      class_310 mc = class_310.method_1551();
      class_1657 clientPlayer = mc.field_1687.method_18470(element.getPlayerId());
      return clientPlayer == null ? element.getY() : EntityUtils.getEntityY(clientPlayer, partialTicks);
   }

   public double getRenderZ(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      class_310 mc = class_310.method_1551();
      class_1657 clientPlayer = mc.field_1687.method_18470(element.getPlayerId());
      return clientPlayer == null ? element.getZ() : EntityUtils.getEntityZ(clientPlayer, partialTicks);
   }

   public double getCoordinateScale(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, MinimapElementRenderInfo renderInfo) {
      return element.getDimension() == renderInfo.renderEntityDimension ? renderInfo.renderEntityDimensionScale : renderInfo.backgroundCoordinateScale;
   }

   public int getInteractionBoxLeft(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return -10;
   }

   public int getInteractionBoxRight(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return 10;
   }

   public int getInteractionBoxTop(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return -10;
   }

   public int getInteractionBoxBottom(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return 10;
   }

   public int getRenderBoxLeft(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return -20;
   }

   public int getRenderBoxRight(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return 20;
   }

   public int getRenderBoxTop(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return -20;
   }

   public int getRenderBoxBottom(PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context, float partialTicks) {
      return 20;
   }

   public int getLeftSideLength(PlayerTrackerMinimapElement<?> element, class_310 mc) {
      class_640 info = class_310.method_1551().method_1562().method_2871(element.getPlayerId());
      return info == null ? 9 : 9 + mc.field_1772.method_1727(info.method_2966().name());
   }

   public String getMenuName(PlayerTrackerMinimapElement<?> element) {
      class_640 info = class_310.method_1551().method_1562().method_2871(element.getPlayerId());
      return info == null ? "" + String.valueOf(element.getPlayerId()) : info.method_2966().name();
   }

   public String getFilterName(PlayerTrackerMinimapElement<?> element) {
      return this.getMenuName(element);
   }

   public int getMenuTextFillLeftPadding(PlayerTrackerMinimapElement<?> element) {
      return 0;
   }

   public int getRightClickTitleBackgroundColor(PlayerTrackerMinimapElement<?> element) {
      return -11184641;
   }

   public boolean shouldScaleBoxWithOptionalScale() {
      return true;
   }

   public float getBoxScale(MinimapElementRenderLocation location, PlayerTrackerMinimapElement<?> element, PlayerTrackerMinimapElementRenderContext context) {
      return context.iconScale;
   }

   public boolean isInteractable(MinimapElementRenderLocation location, PlayerTrackerMinimapElement<?> element) {
      return true;
   }
}
