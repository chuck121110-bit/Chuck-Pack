package xaeroplus.event;

import net.minecraft.class_2626;

public class ChunkBlockUpdateEvent extends PhasedEvent {
   private final class_2626 packet;

   public ChunkBlockUpdateEvent(final class_2626 packet) {
      this.packet = packet;
   }

   public class_2626 packet() {
      return this.packet;
   }
}
