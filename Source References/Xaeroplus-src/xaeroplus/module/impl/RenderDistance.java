package xaeroplus.module.impl;

import java.util.Collections;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class RenderDistance extends Module {
   private final int color = ColorHelper.getColor(255, 255, 0, 204);

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("RenderDistance", this::getLines, this::getColor, this::getLineWidth, 50));
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("RenderDistance");
   }

   List<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      class_310 mc = class_310.method_1551();
      class_1297 player = mc.method_1560();
      if (player == null) {
         return Collections.emptyList();
      } else if (dimension != ChunkUtils.getActualDimension()) {
         return Collections.emptyList();
      } else {
         int viewDistance = mc.field_1690.field_34784;
         int width = viewDistance * 2 + 1;
         int middleChunkX = ChunkUtils.coordToChunkCoord((double)class_3532.method_15357(player.method_23317()));
         int middleChunkZ = ChunkUtils.coordToChunkCoord((double)class_3532.method_15357(player.method_23321()));
         int chunkLeftX = middleChunkX - width / 2;
         int chunkTopZ = middleChunkZ - width / 2;
         int chunkRightX = chunkLeftX + width;
         int chunkBottomZ = chunkTopZ + width;
         int minBlockX = ChunkUtils.chunkCoordToCoord(chunkLeftX);
         int minBlockZ = ChunkUtils.chunkCoordToCoord(chunkTopZ);
         int maxBlockX = ChunkUtils.chunkCoordToCoord(chunkRightX);
         int maxBlockZ = ChunkUtils.chunkCoordToCoord(chunkBottomZ);
         return List.of(new Line(minBlockX, minBlockZ, maxBlockX, minBlockZ), new Line(maxBlockX, minBlockZ, maxBlockX, maxBlockZ), new Line(maxBlockX, maxBlockZ, minBlockX, maxBlockZ), new Line(minBlockX, minBlockZ, minBlockX, maxBlockZ));
      }
   }

   int getColor() {
      return this.color;
   }

   float getLineWidth() {
      return 0.1F;
   }
}
