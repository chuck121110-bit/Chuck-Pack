package xaeroplus.module.impl;

import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1928;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.util.ChunkUtils;

public class SpawnChunks extends SpawnChunksBase {
   @EventHandler
   public void onClientTick(ClientTickEvent.Post event) {
      this.onClientTick();
   }

   public class_5321<class_1937> dimension() {
      return class_1937.field_25179;
   }

   int getSpawnRadius() {
      int spawnChunkRadius = 2;
      if (this.mc.method_1496()) {
         try {
            spawnChunkRadius = (Integer)this.mc.method_1576().method_3847(class_1937.field_25179).method_64395().method_76185(class_1928.field_19403);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Failed to get spawn radius from singleplayer server", e);
         }
      }

      return spawnChunkRadius;
   }

   long getSpawnChunkPos() {
      class_638 level = this.mc.field_1687;
      if (level == null) {
         return ChunkUtils.chunkPosToLong(0, 0);
      } else {
         class_638.class_5271 levelData = level.method_28104();
         if (levelData == null) {
            return ChunkUtils.chunkPosToLong(0, 0);
         } else {
            int spawnBlockX = levelData.method_74893().method_74897().method_10263();
            int spawnBlockZ = levelData.method_74893().method_74897().method_10260();
            int spawnChunkX = ChunkUtils.posToChunkPos(spawnBlockX);
            int spawnChunkZ = ChunkUtils.posToChunkPos(spawnBlockZ);
            return ChunkUtils.chunkPosToLong(spawnChunkX, spawnChunkZ);
         }
      }
   }
}
