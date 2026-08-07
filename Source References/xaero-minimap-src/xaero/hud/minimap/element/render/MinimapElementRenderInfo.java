package xaero.hud.minimap.element.render;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaero.hud.entity.EntityUtils;

public class MinimapElementRenderInfo {
   public final MinimapElementRenderLocation location;
   public final class_1297 renderEntity;
   public final class_243 renderEntityPos;
   public final class_1657 player;
   public final class_243 renderPos;
   public final boolean cave;
   public final float partialTicks;
   public final class_276 framebuffer;
   public final double renderEntityDimensionScale;
   public final class_5321<class_1937> renderEntityDimension;
   public final double backgroundCoordinateScale;
   public final class_5321<class_1937> mapDimension;

   public MinimapElementRenderInfo(MinimapElementRenderLocation location, class_1297 renderEntity, class_1657 player, class_243 renderPos, boolean cave, float partialTicks, class_276 framebuffer, double backgroundCoordinateScale, class_5321<class_1937> mapDimension) {
      this.location = location;
      this.renderEntity = renderEntity;
      this.renderEntityPos = EntityUtils.getEntityPos(renderEntity, partialTicks);
      this.player = player;
      this.renderPos = renderPos;
      this.cave = cave;
      this.partialTicks = partialTicks;
      this.framebuffer = framebuffer;
      this.renderEntityDimensionScale = class_310.method_1551().field_1687.method_8597().comp_646();
      this.renderEntityDimension = class_310.method_1551().field_1687.method_27983();
      this.backgroundCoordinateScale = backgroundCoordinateScale;
      this.mapDimension = mapDimension;
   }
}
