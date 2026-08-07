package kaptainwutax.noiseutils.simplex;

import kaptainwutax.noiseutils.noise.Noise;
import kaptainwutax.noiseutils.utils.MathHelper;
import kaptainwutax.seedutils.rand.JRand;

public class SimplexNoiseSampler extends Noise {
   private static final double SQRT_3 = Math.sqrt((double)3.0F);
   private static final double SKEW_FACTOR_2D;
   private static final double UNSKEW_FACTOR_2D;
   private static final double F3 = 0.3333333333333333;
   private static final double G3 = 0.16666666666666666;

   public SimplexNoiseSampler(JRand rand) {
      super(rand);
   }

   public double sample2D(double x, double y) {
      double hairyFactor = (x + y) * SKEW_FACTOR_2D;
      int hairyX = MathHelper.floor(x + hairyFactor);
      int hairyZ = MathHelper.floor(y + hairyFactor);
      double mixedHairyXz = (double)(hairyX + hairyZ) * UNSKEW_FACTOR_2D;
      double diffXToXz = (double)hairyX - mixedHairyXz;
      double diffZToXz = (double)hairyZ - mixedHairyXz;
      double x0 = x - diffXToXz;
      double y0 = y - diffZToXz;
      byte offsetSecondCornerX;
      byte offsetSecondCornerZ;
      if (x0 > y0) {
         offsetSecondCornerX = 1;
         offsetSecondCornerZ = 0;
      } else {
         offsetSecondCornerX = 0;
         offsetSecondCornerZ = 1;
      }

      double x1 = x0 - (double)offsetSecondCornerX + UNSKEW_FACTOR_2D;
      double y1 = y0 - (double)offsetSecondCornerZ + UNSKEW_FACTOR_2D;
      double x3 = x0 - (double)1.0F + (double)2.0F * UNSKEW_FACTOR_2D;
      double y3 = y0 - (double)1.0F + (double)2.0F * UNSKEW_FACTOR_2D;
      int ii = hairyX & 255;
      int jj = hairyZ & 255;
      int gi0 = this.lookup(ii + this.lookup(jj)) % 12;
      int gi1 = this.lookup(ii + offsetSecondCornerX + this.lookup(jj + offsetSecondCornerZ)) % 12;
      int gi2 = this.lookup(ii + 1 + this.lookup(jj + 1)) % 12;
      double t0 = this.cornerNoise3d(gi0, x0, y0, (double)0.0F, (double)0.5F);
      double t1 = this.cornerNoise3d(gi1, x1, y1, (double)0.0F, (double)0.5F);
      double t2 = this.cornerNoise3d(gi2, x3, y3, (double)0.0F, (double)0.5F);
      return (double)70.0F * (t0 + t1 + t2);
   }

   public double sample3D(double x, double y, double z) {
      double skewFactor = (x + y + z) * 0.3333333333333333;
      int i = MathHelper.floor(x + skewFactor);
      int j = MathHelper.floor(y + skewFactor);
      int k = MathHelper.floor(z + skewFactor);
      double unskewFactor = (double)(i + j + k) * 0.16666666666666666;
      double x0 = (double)i - unskewFactor;
      double y0 = (double)j - unskewFactor;
      double z0 = (double)k - unskewFactor;
      x0 = x - x0;
      y0 = y - y0;
      z0 = z - z0;
      byte i1;
      byte j1;
      byte k1;
      byte i2;
      byte j2;
      byte k2;
      if (x0 >= y0) {
         if (y0 >= z0) {
            i1 = 1;
            j1 = 0;
            k1 = 0;
            i2 = 1;
            j2 = 1;
            k2 = 0;
         } else if (x0 >= z0) {
            i1 = 1;
            j1 = 0;
            k1 = 0;
            i2 = 1;
            j2 = 0;
            k2 = 1;
         } else {
            i1 = 0;
            j1 = 0;
            k1 = 1;
            i2 = 1;
            j2 = 0;
            k2 = 1;
         }
      } else if (y0 < z0) {
         i1 = 0;
         j1 = 0;
         k1 = 1;
         i2 = 0;
         j2 = 1;
         k2 = 1;
      } else if (x0 < z0) {
         i1 = 0;
         j1 = 1;
         k1 = 0;
         i2 = 0;
         j2 = 1;
         k2 = 1;
      } else {
         i1 = 0;
         j1 = 1;
         k1 = 0;
         i2 = 1;
         j2 = 1;
         k2 = 0;
      }

      double x1 = x0 - (double)i1 + 0.16666666666666666;
      double y1 = y0 - (double)j1 + 0.16666666666666666;
      double z1 = z0 - (double)k1 + 0.16666666666666666;
      double x2 = x0 - (double)i2 + 0.3333333333333333;
      double y2 = y0 - (double)j2 + 0.3333333333333333;
      double z2 = z0 - (double)k2 + 0.3333333333333333;
      double x3 = x0 - (double)1.0F + (double)0.5F;
      double y3 = y0 - (double)1.0F + (double)0.5F;
      double z3 = z0 - (double)1.0F + (double)0.5F;
      int ii = i & 255;
      int jj = j & 255;
      int kk = k & 255;
      int gi0 = this.lookup(ii + this.lookup(jj + this.lookup(kk))) % 12;
      int gi1 = this.lookup(ii + i1 + this.lookup(jj + j1 + this.lookup(kk + k1))) % 12;
      int gi2 = this.lookup(ii + i2 + this.lookup(jj + j2 + this.lookup(kk + k2))) % 12;
      int gi3 = this.lookup(ii + 1 + this.lookup(jj + 1 + this.lookup(kk + 1))) % 12;
      double t0 = this.cornerNoise3d(gi0, x0, y0, z0, 0.6);
      double t1 = this.cornerNoise3d(gi1, x1, y1, z1, 0.6);
      double t2 = this.cornerNoise3d(gi2, x2, y2, z2, 0.6);
      double t3 = this.cornerNoise3d(gi3, x3, y3, z3, 0.6);
      return (double)32.0F * (t0 + t1 + t2 + t3);
   }

   private double cornerNoise3d(int hash, double x, double y, double z, double max) {
      double contribution = max - x * x - y * y - z * z;
      double result;
      if (contribution < (double)0.0F) {
         result = (double)0.0F;
      } else {
         contribution *= contribution;
         result = contribution * contribution * MathHelper.grad(hash, x, y, z);
      }

      return result;
   }

   static {
      SKEW_FACTOR_2D = (double)0.5F * (SQRT_3 - (double)1.0F);
      UNSKEW_FACTOR_2D = ((double)3.0F - SQRT_3) / (double)6.0F;
   }
}
