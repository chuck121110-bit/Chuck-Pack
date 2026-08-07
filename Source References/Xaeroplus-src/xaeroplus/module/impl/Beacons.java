package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_10633;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2580;
import net.minecraft.class_2586;
import net.minecraft.class_2791;
import net.minecraft.class_2812;
import net.minecraft.class_2818;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.event.ChunkBlockUpdateEvent;
import xaeroplus.event.ChunkBlocksUpdateEvent;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.event.Phase;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.util.ChunkScanner;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class Beacons extends Module {
   private int alpha = 255;
   private float lineWidth = 0.1F;
   private final WeakHashMap<class_2580, Boolean> beaconBlockEntityCache = new WeakHashMap();
   private final ReferenceSet<class_2248> BEACON_BLOCK_SET;

   public Beacons() {
      this.BEACON_BLOCK_SET = ReferenceSet.of(class_2246.field_10327);
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.multiColorLines("Beacons", this::getLines, this::getColor, this::getLineWidth, 250));
      this.searchAllLoadedChunks();
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister("Beacons");
      this.beaconBlockEntityCache.clear();
   }

   @EventHandler
   public void onChunkData(ChunkDataEvent event) {
      if (!event.seenChunk()) {
         this.searchChunkForBeacons(event.chunk());
      }
   }

   @EventHandler
   public void onChunkBlockUpdatePre(ChunkBlockUpdateEvent event) {
      if (event.phase() == Phase.PRE) {
         class_2586 existingBlockEntity = this.mc.field_1687.method_8321(event.packet().method_11309());
         if (existingBlockEntity instanceof class_2580) {
            class_2580 bbe = (class_2580)existingBlockEntity;
            this.beaconBlockEntityCache.remove(bbe);
         }
      } else if (event.phase() == Phase.POST) {
         class_2586 newBlockEntity = this.mc.field_1687.method_8321(event.packet().method_11309());
         if (newBlockEntity instanceof class_2580) {
            class_2580 bbe = (class_2580)newBlockEntity;
            this.beaconBlockEntityCache.put(bbe, true);
         }
      }

   }

   @EventHandler
   public void onChunkBlocksUpdate(ChunkBlocksUpdateEvent event) {
      if (event.phase() == Phase.PRE) {
         event.packet().method_30621((pos, newState) -> {
            class_2586 existingBlockEntity = this.mc.field_1687.method_8321(pos);
            if (existingBlockEntity instanceof class_2580 bbe) {
               this.beaconBlockEntityCache.remove(bbe);
            }

         });
      } else if (event.phase() == Phase.POST) {
         event.packet().method_30621((pos, newState) -> {
            class_2586 newBlockEntity = this.mc.field_1687.method_8321(pos);
            if (newBlockEntity instanceof class_2580 bbe) {
               this.beaconBlockEntityCache.put(bbe, true);
            }

         });
      }

   }

   @EventHandler
   public void onWorldChange(XaeroWorldChangeEvent event) {
      switch (event.worldChangeType()) {
         case EXIT_WORLD:
         case ACTUAL_DIMENSION_SWITCH:
            TickTaskExecutor var10000 = TickTaskExecutor.INSTANCE;
            WeakHashMap var10001 = this.beaconBlockEntityCache;
            Objects.requireNonNull(var10001);
            var10000.execute(var10001::clear);
         default:
      }
   }

   private void searchAllLoadedChunks() {
      if (this.mc.field_1687 != null) {
         int renderDist = (Integer)this.mc.field_1690.method_42503().method_41753();
         int xMin = ChunkUtils.actualPlayerChunkX() - renderDist;
         int xMax = ChunkUtils.actualPlayerChunkX() + renderDist;
         int zMin = ChunkUtils.actualPlayerChunkZ() - renderDist;
         int zMax = ChunkUtils.actualPlayerChunkZ() + renderDist;

         for(int x = xMin; x <= xMax; ++x) {
            for(int z = zMin; z <= zMax; ++z) {
               class_2818 chunk = this.mc.field_1687.method_2935().method_12126(x, z, false);
               if (!(chunk instanceof class_2812) && chunk != null) {
                  this.searchChunkForBeacons(chunk);
               }
            }
         }

      }
   }

   private void getLinesFromBeaconBlockEntity(class_2580 beaconBlockEntity, Object2IntMap<Line> linesCollector) {
      int levels = beaconBlockEntity.field_11803;
      if (levels > 0) {
         int lineColor = ColorHelper.getColor(255, 255, 255, 255);
         List<class_10633.class_2581> beamSections = beaconBlockEntity.method_10937();
         if (!beamSections.isEmpty()) {
            int beaconColorInt = ((class_10633.class_2581)beamSections.get(beamSections.size() - 1)).method_10944();
            lineColor = ColorHelper.getColor(ColorHelper.getIntR(beaconColorInt), ColorHelper.getIntG(beaconColorInt), ColorHelper.getIntB(beaconColorInt), 255);
         }

         int d = levels * 10 + 10;
         int x = beaconBlockEntity.method_11016().method_10263();
         int z = beaconBlockEntity.method_11016().method_10260();
         int minX = x - d;
         int maxX = x + d;
         int minZ = z - d;
         int maxZ = z + d;
         linesCollector.put(new Line(minX, minZ, maxX, minZ), lineColor);
         linesCollector.put(new Line(minX, maxZ, maxX, maxZ), lineColor);
         linesCollector.put(new Line(minX, minZ, minX, maxZ), lineColor);
         linesCollector.put(new Line(maxX, minZ, maxX, maxZ), lineColor);
      }

   }

   private void searchChunkForBeacons(final class_2791 chunk) {
      class_638 level = this.mc.field_1687;
      ChunkScanner.chunkScanBlockstatePredicate(chunk, this.BEACON_BLOCK_SET, (c, state, relX, y, relZ) -> {
         int x = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9181) + relX;
         int z = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9180) + relZ;
         if (!state.method_31709()) {
            return false;
         } else {
            class_2586 blockEntity = c.method_8321(new class_2338(x, y, z));
            if (blockEntity instanceof class_2580) {
               class_2580 beaconBlockEntity = (class_2580)blockEntity;
               this.beaconBlockEntityCache.put(beaconBlockEntity, true);
            }

            return false;
         }
      }, level.method_31607());
   }

   Object2IntMap<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      if (dimension != ChunkUtils.getActualDimension()) {
         return Object2IntMaps.emptyMap();
      } else {
         class_638 level = this.mc.field_1687;
         if (level != null && this.mc.field_1769.field_4112 != null) {
            if (this.beaconBlockEntityCache.isEmpty()) {
               return Object2IntMaps.emptyMap();
            } else {
               Object2IntOpenHashMap<Line> lines = new Object2IntOpenHashMap(this.beaconBlockEntityCache.size());
               Iterator<class_2580> it = this.beaconBlockEntityCache.keySet().iterator();

               while(it.hasNext()) {
                  class_2580 beaconBlockEntity = (class_2580)it.next();
                  if (beaconBlockEntity.method_11015()) {
                     it.remove();
                  } else {
                     this.getLinesFromBeaconBlockEntity(beaconBlockEntity, lines);
                  }
               }

               return lines;
            }
         } else {
            return Object2IntMaps.emptyMap();
         }
      }
   }

   int getColor(Line line, int color) {
      return ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   float getLineWidth() {
      return this.lineWidth;
   }
}
