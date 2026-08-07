package xaeroplus.util;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2826;
import net.minecraft.class_2841;
import net.minecraft.class_6490;

public class ChunkScanner {
   public static boolean chunkContainsBlocks(class_2791 chunk, ReferenceSet<class_2248> filter, int yLevelMin) {
      class_2826[] sectionArray = chunk.method_12006();

      for(int i = 0; i < sectionArray.length; ++i) {
         int sectionBottomY = chunk.method_31607() + i * 16;
         if (yLevelMin <= sectionBottomY + 15) {
            int yScanStart = yLevelMin > sectionBottomY ? yLevelMin % 16 : 0;
            class_2826 section = sectionArray[i];
            if (section != null && !section.method_38292()) {
               class_2841<class_2680> blockStateContainer = section.method_12265();
               class_2841.class_6561<class_2680> paletteData = blockStateContainer.field_34560;
               class_6490 array = paletteData.comp_118();
               if (array != null && blockStateContainer.method_19526((bs) -> filter.contains(bs.method_26204()))) {
                  for(int x = 0; x < 16; ++x) {
                     for(int z = 0; z < 16; ++z) {
                        for(int y = yScanStart; y < 16; ++y) {
                           class_2680 state = (class_2680)blockStateContainer.method_12321(x, y, z);
                           if (filter.contains(state.method_26204())) {
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public static void chunkScanBlockstatePredicate(class_2791 chunk, ReferenceSet<class_2248> filter, BlockStateScanPredicate statePredicate, int yLevelMin) {
      class_2826[] sectionArray = chunk.method_12006();

      for(int i = 0; i < sectionArray.length; ++i) {
         int sectionBottomY = chunk.method_31607() + i * 16;
         if (yLevelMin <= sectionBottomY + 15) {
            int yScanStart = yLevelMin > sectionBottomY ? yLevelMin % 16 : 0;
            class_2826 section = sectionArray[i];
            if (section != null && !section.method_38292()) {
               class_2841<class_2680> blockStateContainer = section.method_12265();
               class_2841.class_6561<class_2680> paletteData = blockStateContainer.field_34560;
               class_6490 array = paletteData.comp_118();
               if (array != null && blockStateContainer.method_19526((bs) -> filter.contains(bs.method_26204()))) {
                  for(int x = 0; x < 16; ++x) {
                     for(int z = 0; z < 16; ++z) {
                        for(int y = yScanStart; y < 16; ++y) {
                           class_2680 state = (class_2680)blockStateContainer.method_12321(x, y, z);
                           if (filter.contains(state.method_26204()) && statePredicate.test(chunk, state, x, sectionBottomY + y, z)) {
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public interface BlockStateScanPredicate {
      boolean test(class_2791 chunkAccess, class_2680 state, int relativeX, int y, int relativeZ);
   }
}
