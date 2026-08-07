package xaero.hud.minimap.waypoint;

import net.minecraft.class_1767;
import net.minecraft.class_2561;
import net.minecraft.class_3532;

public enum WaypointColor {
   BLACK(class_2561.method_43471("gui.xaero_black"), '0', -16777216),
   DARK_BLUE(class_2561.method_43471("gui.xaero_dark_blue"), '1', -16777046),
   DARK_GREEN(class_2561.method_43471("gui.xaero_dark_green"), '2', -16733696),
   DARK_AQUA(class_2561.method_43471("gui.xaero_dark_aqua"), '3', -16733526),
   DARK_RED(class_2561.method_43471("gui.xaero_dark_red"), '4', -5636096),
   DARK_PURPLE(class_2561.method_43471("gui.xaero_dark_purple"), '5', -5635926),
   GOLD(class_2561.method_43471("gui.xaero_gold"), '6', -22016),
   GRAY(class_2561.method_43471("gui.xaero_gray"), '7', -5592406),
   DARK_GRAY(class_2561.method_43471("gui.xaero_dark_gray"), '8', -11184811),
   BLUE(class_2561.method_43471("gui.xaero_blue"), '9', -11184641),
   GREEN(class_2561.method_43471("gui.xaero_green"), 'a', -11141291),
   AQUA(class_2561.method_43471("gui.xaero_aqua"), 'b', -11141121),
   RED(class_2561.method_43471("gui.xaero_red"), 'c', -65536),
   PURPLE(class_2561.method_43471("gui.xaero_purple"), 'd', -43521),
   YELLOW(class_2561.method_43471("gui.xaero_yellow"), 'e', -171),
   WHITE(class_2561.method_43471("gui.xaero_white"), 'f', -1),
   MAGENTA(class_2561.method_43471("gui.xaero_magenta"), 'f', class_1767.field_7958.method_16357() | -16777216),
   LIGHT_BLUE(class_2561.method_43471("gui.xaero_light_blue"), 'f', class_1767.field_7951.method_16357() | -16777216),
   LIME(class_2561.method_43471("gui.xaero_lime"), 'f', class_1767.field_7961.method_16357() | -16777216),
   PINK(class_2561.method_43471("gui.xaero_pink"), 'f', class_1767.field_7954.method_16357() | -16777216),
   BROWN(class_2561.method_43471("gui.xaero_brown"), 'f', class_1767.field_7957.method_16357() | -16777216);

   private final class_2561 name;
   private final char format;
   private final int hex;

   private WaypointColor(class_2561 name, char format, int hex) {
      this.name = name;
      this.format = format;
      this.hex = hex;
   }

   public class_2561 getName() {
      return this.name;
   }

   public char getFormat() {
      return this.format;
   }

   public int getHex() {
      return this.hex;
   }

   public static WaypointColor fromIndex(int index) {
      return values()[class_3532.method_15340(index, 0, values().length - 1)];
   }

   public static WaypointColor getRandom() {
      return fromIndex((int)(Math.random() * (double)values().length));
   }

   public static WaypointColor fromDye(class_1767 dyeColor) {
      switch (dyeColor) {
         case field_7946 -> {
            return GOLD;
         }
         case field_7958 -> {
            return MAGENTA;
         }
         case field_7951 -> {
            return LIGHT_BLUE;
         }
         case field_7947 -> {
            return YELLOW;
         }
         case field_7961 -> {
            return LIME;
         }
         case field_7954 -> {
            return PINK;
         }
         case field_7944 -> {
            return DARK_GRAY;
         }
         case field_7967 -> {
            return GRAY;
         }
         case field_7955 -> {
            return AQUA;
         }
         case field_7945 -> {
            return PURPLE;
         }
         case field_7966 -> {
            return BLUE;
         }
         case field_7957 -> {
            return BROWN;
         }
         case field_7942 -> {
            return GREEN;
         }
         case field_7964 -> {
            return RED;
         }
         case field_7963 -> {
            return BLACK;
         }
         default -> {
            return WHITE;
         }
      }
   }

   // $FF: synthetic method
   private static WaypointColor[] $values() {
      return new WaypointColor[]{BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, PURPLE, YELLOW, WHITE, MAGENTA, LIGHT_BLUE, LIME, PINK, BROWN};
   }
}
