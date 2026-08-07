package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import java.util.concurrent.atomic.AtomicBoolean;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.mcutils.version.MCVersion;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2812;
import net.minecraft.class_2818;
import net.minecraft.class_2841;
import net.minecraft.class_2960;
import net.minecraft.class_3561;
import net.minecraft.class_5321;
import net.minecraft.class_6669;
import net.minecraft.class_6880;
import net.minecraft.class_7522;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class OldBiomes extends Module {
   public final SavableHighlightCacheInstance oldBiomesCache = new SavableHighlightCacheInstance("XaeroPlusOldBiomes");
   private static final long seed = -4172144997902289642L;
   private static final MCVersion mcVersion;
   private final OverworldBiomeSource biomeSource;
   private int oldBiomesColor;

   public OldBiomes() {
      this.biomeSource = new OverworldBiomeSource(mcVersion, -4172144997902289642L);
      this.oldBiomesColor = ColorHelper.getColor(0, 255, 0, 100);
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("OldBiomes", this::getHighlightsState, this::getOldBiomesColor, 250));
      this.oldBiomesCache.onEnable();

      try {
         this.searchAllLoadedChunks();
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error searching all loaded chunks", e);
      }

   }

   public void onDisable() {
      this.oldBiomesCache.onDisable();
      Globals.drawManager.registry().unregister("OldBiomes");
   }

   @EventHandler
   public void onChunkDataEvent(final ChunkDataEvent event) {
      if (!event.seenChunk()) {
         try {
            if (event.chunk().method_12200().method_27983() != class_1937.field_25179) {
               return;
            }

            this.searchBiome(event.chunk());
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error checking for OldBiome at chunk pos: [{}, {}]", new Object[]{event.chunk().method_12004().field_9181, event.chunk().method_12004().field_9180, e});
         }

      }
   }

   private void searchBiome(final class_2818 chunk) {
      class_5321<class_1937> dim = chunk.method_12200().method_27983();
      int x = chunk.method_12004().field_9181;
      int z = chunk.method_12004().field_9180;
      if (!this.oldBiomesCache.get().isHighlighted(x, z, dim)) {
         int blockPosX = ChunkUtils.chunkCoordToCoord(x) + 1;
         int blockPosZ = ChunkUtils.chunkCoordToCoord(z) + 1;
         int blockPosY = 50;
         Biome biome = this.biomeSource.getBiome(blockPosX, blockPosY, blockPosZ);
         if (biome == null) {
            XaeroPlus.LOGGER.error("Null biome returned from source at: {} {} {}", new Object[]{blockPosX, blockPosY, blockPosZ});
         } else {
            String oldBiomeName = biome.getName();
            String oldBiomeFixup = this.fixupOldBiome(oldBiomeName);
            int sectionIndex = chunk.method_31602(blockPosY);
            AtomicBoolean containsBiome = new AtomicBoolean(false);
            class_7522<class_6880<class_1959>> palettedContainerR0 = chunk.method_38259(sectionIndex).method_38294();
            if (palettedContainerR0 instanceof class_2841) {
               class_2841<class_6880<class_1959>> palettedContainer = (class_2841)palettedContainerR0;
               palettedContainer.field_34560.comp_118().method_21739((i) -> {
                  if (!containsBiome.get()) {
                     class_6880<class_1959> holder = (class_6880)palettedContainer.field_34560.comp_119().method_12288(i);
                     boolean match = holder.method_40230().map(class_5321::method_29177).map(class_2960::method_12832).filter((path) -> path.equals(oldBiomeFixup)).isPresent();
                     containsBiome.set(match);
                  }
               });
            }

            if (containsBiome.get()) {
               this.oldBiomesCache.get().addHighlight(x, z, dim);
            }

         }
      }
   }

   private String fixupOldBiome(String oldBiome) {
      String oldBiomeFixupNamespaced = "minecraft:" + oldBiome;
      oldBiomeFixupNamespaced = (String)class_3561.field_15810.getOrDefault(oldBiomeFixupNamespaced, oldBiomeFixupNamespaced);
      oldBiomeFixupNamespaced = (String)class_6669.field_35100.getOrDefault(oldBiomeFixupNamespaced, oldBiomeFixupNamespaced);
      return oldBiomeFixupNamespaced.split("minecraft:")[1];
   }

   private void searchAllLoadedChunks() {
      if (this.mc.field_1687 != null && this.mc.field_1687.method_27983() == class_1937.field_25179) {
         int renderDist = (Integer)this.mc.field_1690.method_42503().method_41753();
         int xMin = ChunkUtils.actualPlayerChunkX() - renderDist;
         int xMax = ChunkUtils.actualPlayerChunkX() + renderDist;
         int zMin = ChunkUtils.actualPlayerChunkZ() - renderDist;
         int zMax = ChunkUtils.actualPlayerChunkZ() + renderDist;

         for(int x = xMin; x <= xMax; ++x) {
            for(int z = zMin; z <= zMax; ++z) {
               class_2818 chunk = this.mc.field_1687.method_2935().method_12126(x, z, false);
               if (!(chunk instanceof class_2812) && chunk != null) {
                  this.searchBiome(chunk);
               }
            }
         }

      }
   }

   public int getOldBiomesColor() {
      return this.oldBiomesColor;
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.oldBiomesCache.get().getCacheMap(dimension);
   }

   public void setDiskCache(final boolean b) {
      this.oldBiomesCache.setDiskCache(b, this.isEnabled());
   }

   public void setAlpha(final double b) {
      this.oldBiomesColor = ColorHelper.getColorWithAlpha(this.oldBiomesColor, (int)b);
   }

   public void setRgbColor(final int color) {
      this.oldBiomesColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.oldBiomesAlphaSetting.getAsInt());
   }

   static {
      mcVersion = MCVersion.v1_12_2;
   }
}
