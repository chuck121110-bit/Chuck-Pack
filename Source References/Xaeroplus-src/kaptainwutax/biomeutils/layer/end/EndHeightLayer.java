package kaptainwutax.biomeutils.layer.end;

import kaptainwutax.biomeutils.layer.BoolBiomeLayer;
import kaptainwutax.biomeutils.layer.FloatBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class EndHeightLayer extends FloatBiomeLayer {
   public EndHeightLayer(MCVersion version, BoolBiomeLayer parent) {
      super(version, parent);
   }

   public float sample(int x, int y, int z) {
      return this.getNoiseValueAt(x, z);
   }

   public float getNoiseValueAt(int x, int z) {
      int scaledX = x / 2;
      int scaledZ = z / 2;
      int oddX = x % 2;
      int oddZ = z % 2;
      float height;
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_14)) {
         height = 100.0F - (float)Math.sqrt((double)((float)(x * x + z * z))) * 8.0F;
      } else {
         height = 100.0F - (float)Math.sqrt((double)((float)x * (float)x + (float)z * (float)z)) * 8.0F;
      }

      height = clamp(height);

      for(int rx = -12; rx <= 12; ++rx) {
         for(int rz = -12; rz <= 12; ++rz) {
            long shiftedX = (long)(scaledX + rx);
            long shiftedZ = (long)(scaledZ + rz);
            if (shiftedX * shiftedX + shiftedZ * shiftedZ > 4096L && ((BoolBiomeLayer)this.getParent(BoolBiomeLayer.class)).get((int)shiftedX, 0, (int)shiftedZ)) {
               float elevation = (Math.abs((float)shiftedX) * 3439.0F + Math.abs((float)shiftedZ) * 147.0F) % 13.0F + 9.0F;
               float smoothX = (float)(oddX - rx * 2);
               float smoothZ = (float)(oddZ - rz * 2);
               float noise = 100.0F - (float)Math.sqrt((double)(smoothX * smoothX + smoothZ * smoothZ)) * elevation;
               noise = clamp(noise);
               height = Math.max(height, noise);
            }
         }
      }

      return height;
   }

   protected static float clamp(float value) {
      return value < -100.0F ? -100.0F : Math.min(value, 80.0F);
   }
}
