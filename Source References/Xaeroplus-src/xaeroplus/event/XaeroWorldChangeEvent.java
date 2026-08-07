package xaeroplus.event;

import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;

public record XaeroWorldChangeEvent(WorldChangeType worldChangeType, @Nullable class_5321<class_1937> from, @Nullable class_5321<class_1937> to) {
   public static enum WorldChangeType {
      ENTER_WORLD,
      EXIT_WORLD,
      ACTUAL_DIMENSION_SWITCH,
      VIEWED_DIMENSION_SWITCH,
      MULTIWORLD_SWITCH;

      // $FF: synthetic method
      private static WorldChangeType[] $values() {
         return new WorldChangeType[]{ENTER_WORLD, EXIT_WORLD, ACTUAL_DIMENSION_SWITCH, VIEWED_DIMENSION_SWITCH, MULTIWORLD_SWITCH};
      }
   }
}
