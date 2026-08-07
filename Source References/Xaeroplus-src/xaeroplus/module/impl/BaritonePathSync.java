package xaeroplus.module.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_5321;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.BaritoneHelper;
import xaeroplus.util.BaritonePathHelper;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class BaritonePathSync extends Module {
   private int color = ColorHelper.getColor(255, 0, 0, 150);
   private List<Line> pathLines = new ArrayList();
   private float lineWidth = 0.1F;

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("BaritonePath", this::getPathLines, this::getColor, this::getLineWidth, 50));
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("BaritonePath");
   }

   @EventHandler
   public void syncPath(final ClientTickEvent.Post event) {
      if (BaritoneHelper.isBaritonePresent()) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
            if (currentWorld != null) {
               try {
                  List<class_2338> baritonePath = BaritonePathHelper.getBaritonePath();
                  if (baritonePath.isEmpty()) {
                     this.pathLines = Collections.emptyList();
                     return;
                  }

                  List<Line> lines = new ArrayList();
                  class_2338 prevPos = (class_2338)baritonePath.get(0);

                  for(int i = 1; i < baritonePath.size(); ++i) {
                     class_2338 currentPos = (class_2338)baritonePath.get(i);
                     if (currentPos.method_10263() != prevPos.method_10263() || currentPos.method_10260() != prevPos.method_10260()) {
                        Line line = new Line(prevPos.method_10263(), prevPos.method_10260(), currentPos.method_10263(), currentPos.method_10260());
                        lines.add(line);
                        prevPos = currentPos;
                     }
                  }

                  this.pathLines = lines;
               } catch (Exception e) {
                  XaeroPlus.LOGGER.error("Error in Baritone path sync", e);
               }

            }
         }
      }
   }

   public int getColor() {
      return this.color;
   }

   public void setColor(final int color) {
      this.color = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.baritonePathSyncOpacity.getAsInt());
   }

   public void setOpacity(final int opacity) {
      this.color = ColorHelper.getColorWithAlpha(this.color, opacity);
   }

   public float getLineWidth() {
      return this.lineWidth;
   }

   public List<Line> getPathLines(int windowRegionX, int windowRegionZ, int windowSize, class_5321<class_1937> dimension) {
      return dimension != ChunkUtils.getActualDimension() ? Collections.emptyList() : this.pathLines;
   }
}
