package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class Breadcrumbs extends Module {
   public final SavableHighlightCacheInstance breadcrumbsCache = new SavableHighlightCacheInstance("XaeroPlusBreadcrumbs");
   private int breadcrumbsColor = ColorHelper.getColor(0, 255, 0, 100);
   private int chunkRadius = 0;

   public void setDiskCache(final boolean disk) {
      this.breadcrumbsCache.setDiskCache(disk, this.isEnabled());
   }

   @EventHandler
   public void onTick(ClientTickEvent.Post event) {
      if (Settings.REGISTRY.breadcrumbsModeSetting.get() == Settings.BreadcrumbsMode.CHUNK_RADIUS) {
         class_5321<class_1937> dim = ChunkUtils.getActualDimension();
         int playerChunkX = ChunkUtils.actualPlayerChunkX();
         int playerChunkZ = ChunkUtils.actualPlayerChunkZ();

         for(int x = playerChunkX - this.chunkRadius; x <= playerChunkX + this.chunkRadius; ++x) {
            for(int z = playerChunkZ - this.chunkRadius; z <= playerChunkZ + this.chunkRadius; ++z) {
               if (!this.breadcrumbsCache.get().isHighlighted(x, z, dim)) {
                  this.breadcrumbsCache.get().addHighlight(x, z, dim);
               }
            }
         }
      }

   }

   @EventHandler
   public void onChunkLoad(ChunkDataEvent event) {
      if (Settings.REGISTRY.breadcrumbsModeSetting.get() == Settings.BreadcrumbsMode.SEEN_CHUNKS) {
         class_5321<class_1937> dim = event.chunk().method_12200().method_27983();
         class_1923 chunkPos = event.chunk().method_12004();
         int x = chunkPos.field_9181;
         int z = chunkPos.field_9180;
         if (!this.breadcrumbsCache.get().isHighlighted(x, z, dim)) {
            this.breadcrumbsCache.get().addHighlight(x, z, dim);
         }
      }

   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("Breadcrumbs", this::getHighlightsState, this::getBreadcrumbsColor, 50));
      this.breadcrumbsCache.onEnable();
   }

   public void onDisable() {
      this.breadcrumbsCache.onDisable();
      Globals.drawManager.registry().unregister("Breadcrumbs");
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.breadcrumbsCache.get().getCacheMap(dimension);
   }

   public int getBreadcrumbsColor() {
      return this.breadcrumbsColor;
   }

   public void setRgbColor(int color) {
      this.breadcrumbsColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.breadcrumbsOpacitySetting.getAsInt());
   }

   public void setAlpha(double alpha) {
      this.breadcrumbsColor = ColorHelper.getColorWithAlpha(this.breadcrumbsColor, (int)alpha);
   }

   public void setChunkRadius(double chunkRadius) {
      this.chunkRadius = (int)chunkRadius;
   }
}
