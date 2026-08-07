package xaeroplus.module.impl;

import java.util.Collections;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_2784;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.util.ColorHelper;

public class WorldBorder extends Module {
   private final int color = ColorHelper.getColor(0, 255, 255, 204);

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("WorldBorder", this::getLines, this::getColor, this::getLineWidth, 1000));
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("WorldBorder");
   }

   List<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      class_310 mc = class_310.method_1551();
      class_638 level = mc.field_1687;
      if (level == null) {
         return Collections.emptyList();
      } else if (level.method_27983() != dimension) {
         return Collections.emptyList();
      } else {
         class_2784 worldBorder = level.method_8621();
         int minX = class_3532.method_15357(worldBorder.method_11976());
         int minZ = class_3532.method_15357(worldBorder.method_11958());
         int maxX = class_3532.method_15357(worldBorder.method_11963());
         int maxZ = class_3532.method_15357(worldBorder.method_11977());
         return List.of(new Line(minX, minZ, maxX, minZ), new Line(maxX, minZ, maxX, maxZ), new Line(maxX, maxZ, minX, maxZ), new Line(minX, minZ, minX, maxZ));
      }
   }

   int getColor() {
      return this.color;
   }

   float getLineWidth() {
      return 0.1F;
   }
}
