package xaeroplus.module.impl;

import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.util.ChunkUtils;

public class SpawnChunksPlayer extends SpawnChunksBase {
   @EventHandler
   public void onClientTick(ClientTickEvent.Post event) {
      this.onClientTick();
   }

   public class_5321<class_1937> dimension() {
      return ChunkUtils.getActualDimension();
   }

   int getSpawnRadius() {
      class_638 level = this.mc.field_1687;
      return level == null ? 0 : level.method_39024();
   }

   long getSpawnChunkPos() {
      return ChunkUtils.chunkPosToLong(ChunkUtils.actualPlayerChunkX(), ChunkUtils.actualPlayerChunkZ());
   }
}
