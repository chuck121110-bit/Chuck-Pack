package xaeroplus.module.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class MapArtGrid extends Module {
   private int alpha = 255;
   private int color;
   private float lineWidth;

   public MapArtGrid() {
      this.color = ColorHelper.getColor(255, 0, 0, this.alpha);
      this.lineWidth = 0.1F;
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("MapArtGrid", this::getLines, this::getColor, this::getLineWidth, 1000));
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("MapArtGrid");
   }

   public List<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      ArrayList<Line> lines = new ArrayList();
      int padding = 10000;
      int minBlockX = ChunkUtils.regionCoordToCoord(windowRegionX - windowRegionSize) - padding;
      int minBlockZ = ChunkUtils.regionCoordToCoord(windowRegionZ - windowRegionSize) - padding;
      int maxBlockX = ChunkUtils.regionCoordToCoord(windowRegionX + windowRegionSize) + padding;
      int maxBlockZ = ChunkUtils.regionCoordToCoord(windowRegionZ + windowRegionSize) + padding;
      int minXGridPos = minBlockX - minBlockX % 128 - 64;
      int minZGridPos = minBlockZ - minBlockZ % 128 - 64;
      int maxXGridPos = maxBlockX - maxBlockX % 128 + 64;
      int maxZGridPos = maxBlockZ - maxBlockZ % 128 + 64;

      for(int x = minXGridPos; x <= maxXGridPos; x += 128) {
         lines.add(new Line(x, minBlockZ, x, maxBlockZ));
      }

      for(int z = minZGridPos; z <= maxZGridPos; z += 128) {
         lines.add(new Line(minBlockX, z, maxBlockX, z));
      }

      return lines;
   }

   public int getColor() {
      return this.color;
   }

   public void setRgbColor(final int color) {
      this.color = ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   public void setAlpha(final int alpha) {
      this.alpha = alpha;
      this.color = ColorHelper.getColorWithAlpha(this.color, alpha);
   }

   public float getLineWidth() {
      return this.lineWidth;
   }

   public void setLineWidth(final float lineWidth) {
      this.lineWidth = lineWidth;
   }
}
