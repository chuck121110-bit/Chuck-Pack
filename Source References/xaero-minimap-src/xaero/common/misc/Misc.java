package xaero.common.misc;

import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import net.minecraft.class_2561;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_6880;
import net.minecraft.class_327.class_6415;

public class Misc {
   public static double getMouseX(class_310 mc, boolean raw) {
      return raw ? mc.field_1729.method_1603() : mc.field_1729.method_1603() * (double)mc.method_22683().method_4489() / (double)mc.method_22683().method_4480();
   }

   public static double getMouseY(class_310 mc, boolean raw) {
      return raw ? mc.field_1729.method_1604() : mc.field_1729.method_1604() * (double)mc.method_22683().method_4506() / (double)mc.method_22683().method_4507();
   }

   public static void drawNormalText(class_4587 matrices, String name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      class_310.method_1551().field_1772.method_27521(name, x, y, color, shadow, matrices.method_23760().method_23761(), renderTypeBuffer, class_6415.field_33993, 0, 15728880);
   }

   public static void drawNormalText(class_4587 matrices, class_2561 name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      class_310.method_1551().field_1772.method_27522(name, x, y, color, shadow, matrices.method_23760().method_23761(), renderTypeBuffer, class_6415.field_33993, 0, 15728880);
   }

   public static void drawPiercingText(class_4587 matrices, String name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      class_310.method_1551().field_1772.method_27521(name, x, y, color, shadow, matrices.method_23760().method_23761(), renderTypeBuffer, class_6415.field_33994, 0, 15728880);
   }

   public static void drawPiercingText(class_4587 matrices, class_2561 name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      class_310.method_1551().field_1772.method_27522(name, x, y, color, shadow, matrices.method_23760().method_23761(), renderTypeBuffer, class_6415.field_33994, 0, 15728880);
   }

   public static void drawCenteredPiercingText(class_4587 matrices, String name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      drawPiercingText(matrices, name, x - (float)(class_310.method_1551().field_1772.method_1727(name) / 2), y, color, shadow, renderTypeBuffer);
   }

   public static void drawCenteredPiercingText(class_4587 matrices, class_2561 name, float x, float y, int color, boolean shadow, class_4597 renderTypeBuffer) {
      drawPiercingText(matrices, name, x - (float)(class_310.method_1551().field_1772.method_27525(name) / 2), y, color, shadow, renderTypeBuffer);
   }

   public static long getChunkPosAsLong(class_2818 chunk) {
      return chunk.method_12004().method_8324();
   }

   public static boolean hasItem(class_1657 player, class_1792 item) {
      class_1661 inventory = player.method_31548();

      for(int i = 0; i < 9; ++i) {
         if (inventory.method_5438(i).method_7909() == item) {
            return true;
         }
      }

      for(int i = 36; i < inventory.method_5439(); ++i) {
         if (inventory.method_5438(i).method_7909() == item) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasItem(class_2371<class_1799> inventory, int limit, class_1792 item) {
      for(int i = 0; i < inventory.size() && (limit == -1 || i < limit); ++i) {
         if (inventory.get(i) != null && ((class_1799)inventory.get(i)).method_7909() == item) {
            return true;
         }
      }

      return false;
   }

   public static class_2561 getFixedDisplayName(class_1297 e) {
      class_2561 baseName = e.method_5477();
      if (baseName == null) {
         return null;
      } else {
         return e.method_5781() == null ? baseName.method_27661() : e.method_5781().method_1198(baseName.method_27661());
      }
   }

   public static boolean hasEffect(class_1657 player, class_6880<class_1291> effect) {
      return effect != null && player != null && player.method_6059(effect);
   }

   public static boolean hasEffect(class_6880<class_1291> effect) {
      return hasEffect(class_310.method_1551().field_1724, effect);
   }

   public static boolean isValidResourceLocationString(String resourceLocationString) {
      if (resourceLocationString.isEmpty()) {
         return false;
      } else {
         for(int i = 0; i < resourceLocationString.length(); ++i) {
            if (!class_2960.method_12831(resourceLocationString.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }
}
