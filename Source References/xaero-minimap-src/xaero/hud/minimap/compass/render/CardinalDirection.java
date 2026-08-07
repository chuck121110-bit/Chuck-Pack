package xaero.hud.minimap.compass.render;

import net.minecraft.class_2561;

public enum CardinalDirection {
   NORTH(class_2561.method_43471("gui.xaero_compass_north")),
   EAST(class_2561.method_43471("gui.xaero_compass_east")),
   SOUTH(class_2561.method_43471("gui.xaero_compass_south")),
   WEST(class_2561.method_43471("gui.xaero_compass_west"));

   private final class_2561 initials;

   private CardinalDirection(class_2561 initials) {
      this.initials = initials;
   }

   public class_2561 getInitials() {
      return this.initials;
   }

   // $FF: synthetic method
   private static CardinalDirection[] $values() {
      return new CardinalDirection[]{NORTH, EAST, SOUTH, WEST};
   }
}
