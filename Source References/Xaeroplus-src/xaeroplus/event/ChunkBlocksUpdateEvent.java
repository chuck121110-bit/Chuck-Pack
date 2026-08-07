package xaeroplus.event;

import net.minecraft.class_2637;

public class ChunkBlocksUpdateEvent extends PhasedEvent {
   private final class_2637 packet;

   public ChunkBlocksUpdateEvent(final class_2637 packet) {
      this.packet = packet;
   }

   public class_2637 packet() {
      return this.packet;
   }
}
