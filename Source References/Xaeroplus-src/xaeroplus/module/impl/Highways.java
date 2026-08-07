package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.List;
import kaptainwutax.mathutils.util.Mth;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ColorHelper;

public class Highways extends Module {
   private int highwaysColor = ColorHelper.getColor(0, 0, 255, 100);
   private int width = 2;
   private final List<Line> OVERWORLD_END_LINES;
   private final List<Line> NETHER_LINES;
   private static final IntList ringRoads = IntList.of(new int[]{200, 500, 750, 1000, 1500, 2000, 2500, 3131, 3500, 5000, 6250, 7500, 10000, 12500, 15000, 20000, 25000, 50000, 55000, 62500, 75000, 100000, 125000, 250000, 500000, 750000, 1000000, 1250000, 1568852, 1875000, 2500000, 3750000});
   private static final IntList diamonds = IntList.of(new int[]{2500, 5000, 10000, 25000, 50000, 125000, 250000, 500000, 3750000});

   public Highways() {
      this.OVERWORLD_END_LINES = this.generateHighwayLines(class_1937.field_25179);
      this.NETHER_LINES = this.generateHighwayLines(class_1937.field_25180);
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("Highways", this::getHighwayLines, this::getHighwayColor, this::getLineWidth, 50));
   }

   private List<Line> getHighwayLines(int windowRegionX, int windowRegionZ, int windowSize, class_5321<class_1937> dimension) {
      if (dimension != class_1937.field_25179 && dimension != class_1937.field_25181) {
         return (List<Line>)(dimension == class_1937.field_25180 ? this.NETHER_LINES : new ArrayList());
      } else {
         return this.OVERWORLD_END_LINES;
      }
   }

   private List<Line> generateHighwayLines(class_5321<class_1937> dimension) {
      ArrayList<Line> lines = new ArrayList(500);
      int stride = 500000;

      for(int i = -30000000; i < 30000000; i += stride) {
         lines.add(new Line(i, 0, i + stride, 0));
         lines.add(new Line(0, i, 0, i + stride));
      }

      for(int i = -30000000; i < 30000000; i += stride) {
         lines.add(new Line(i, i, i + stride, i + stride));
         lines.add(new Line(-i, i, -i - stride, i + stride));
      }

      if (dimension == class_1937.field_25180) {
         IntListIterator var8 = ringRoads.iterator();

         while(var8.hasNext()) {
            int ringRoad = (Integer)var8.next();

            for(int i = -ringRoad; i < ringRoad; i += stride) {
               lines.add(new Line(i, -ringRoad, Mth.min(i + stride, ringRoad), -ringRoad));
               lines.add(new Line(i, ringRoad, Mth.min(i + stride, ringRoad), ringRoad));
               lines.add(new Line(-ringRoad, i, -ringRoad, Mth.min(i + stride, ringRoad)));
               lines.add(new Line(ringRoad, i, ringRoad, Mth.min(i + stride, ringRoad)));
            }
         }

         var8 = diamonds.iterator();

         while(var8.hasNext()) {
            int diamond = (Integer)var8.next();
            lines.add(new Line(diamond, 0, 0, diamond));
            lines.add(new Line(0, -diamond, diamond, 0));
            lines.add(new Line(0, -diamond, -diamond, 0));
            lines.add(new Line(-diamond, 0, 0, diamond));
         }

         for(int i = -50000; i < 50000; i += 5000) {
            if (i != 0) {
               lines.add(new Line(i, -50000, i, 50000));
               lines.add(new Line(-50000, i, 50000, i));
            }
         }

         lines.add(new Line(-125000, -50000, -50000, -50000));
         lines.add(new Line(-125000, 50000, -50000, 50000));
         lines.add(new Line(125000, -50000, 50000, -50000));
         lines.add(new Line(125000, 50000, 50000, 50000));
         lines.add(new Line(-50000, -125000, -50000, -50000));
         lines.add(new Line(-50000, 125000, -50000, 50000));
         lines.add(new Line(50000, -125000, 50000, -50000));
         lines.add(new Line(50000, 125000, 50000, 50000));
      }

      return lines;
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("Highways");
   }

   public int getHighwayColor() {
      return this.highwaysColor;
   }

   public void setRgbColor(final int color) {
      this.highwaysColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.highwaysColorAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.highwaysColor = ColorHelper.getColorWithAlpha(this.highwaysColor, (int)a);
   }

   public void setWidth(final Settings.HighwayWidth w) {
      this.width = w.getWidth();
   }

   private float getLineWidth() {
      return (float)this.width;
   }
}
