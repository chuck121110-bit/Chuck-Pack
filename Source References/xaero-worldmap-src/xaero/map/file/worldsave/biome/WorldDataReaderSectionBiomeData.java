package xaero.map.file.worldsave.biome;

import net.minecraft.class_2499;
import net.minecraft.class_3508;
import net.minecraft.class_3532;
import net.minecraft.class_6490;

public class WorldDataReaderSectionBiomeData {
   private final class_2499 paletteTag;
   private final long[] biomesLongArray;
   private class_6490 biomesBitArray;
   private boolean triedReadingData;

   public WorldDataReaderSectionBiomeData(class_2499 paletteTag, long[] biomesLongArray) {
      this.paletteTag = paletteTag;
      this.biomesLongArray = biomesLongArray;
   }

   public boolean hasDifferentBiomes() {
      return this.biomesLongArray != null;
   }

   public String get(int quadX, int sectionQuadY, int quadZ) {
      if (!this.hasDifferentBiomes()) {
         return this.paletteTag.isEmpty() ? null : (String)this.paletteTag.method_10608(0).orElse((Object)null);
      } else {
         if (!this.triedReadingData && this.biomesBitArray == null && this.biomesLongArray != null) {
            this.triedReadingData = true;
            int bits = class_3532.method_15342(this.paletteTag.size());

            try {
               this.biomesBitArray = new class_3508(bits, 64, this.biomesLongArray);
            } catch (class_3508.class_6685 var6) {
            }
         }

         if (this.biomesBitArray == null) {
            return this.paletteTag.isEmpty() ? null : (String)this.paletteTag.method_10608(0).orElse((Object)null);
         } else {
            int pos3D = sectionQuadY << 4 | quadZ << 2 | quadX;
            int biomePaletteIndex = this.biomesBitArray.method_15211(pos3D);
            return biomePaletteIndex >= this.paletteTag.size() ? null : (String)this.paletteTag.method_10608(biomePaletteIndex).orElse((Object)null);
         }
      }
   }
}
