package xaeroplus.feature.render.line;

import net.minecraft.class_3532;

public record Line(int x1, int z1, int x2, int z2) {
   public double length() {
      return Math.sqrt(Math.pow((double)(this.x2 - this.x1), (double)2.0F) + Math.pow((double)(this.z2 - this.z1), (double)2.0F));
   }

   public Line extrapolateToWorldBorder() {
      return this.extrapolateToMaxCoord(30000000);
   }

   public Line extrapolateToMaxCoord(int coord) {
      int dx = this.x2() - this.x1();
      if (dx == 0) {
         return new Line(this.x1(), -coord, this.x2(), coord);
      } else {
         int dz = this.z2() - this.z1();
         if (dz == 0) {
            return new Line(-coord, this.z1(), coord, this.z2());
         } else {
            double slope = (double)dz / (double)dx;
            double intercept = (double)this.z1() - slope * (double)this.x1();
            double x1 = (double)(-coord);
            double z1 = slope * x1 + intercept;
            if (z1 < (double)(-coord)) {
               z1 = (double)(-coord);
               x1 = (z1 - intercept) / slope;
            } else if (z1 > (double)coord) {
               z1 = (double)coord;
               x1 = (z1 - intercept) / slope;
            }

            double x2 = (double)coord;
            double z2 = slope * x2 + intercept;
            if (z2 < (double)(-coord)) {
               z2 = (double)(-coord);
               x2 = (z2 - intercept) / slope;
            } else if (z2 > (double)coord) {
               z2 = (double)coord;
               x2 = (z2 - intercept) / slope;
            }

            return new Line((int)Math.round(x1), (int)Math.round(z1), (int)Math.round(x2), (int)Math.round(z2));
         }
      }
   }

   public boolean lineClip(int rxMin, int rxMax, int rzMin, int rzMax) {
      if ((this.x1() >= rxMin || this.x2() >= rxMin) && (this.x1() <= rxMax || this.x2() <= rxMax) && (this.z1() >= rzMin || this.z2() >= rzMin) && (this.z1() <= rzMax || this.z2() <= rzMax)) {
         double xx1 = (double)this.x1();
         double xx2 = (double)this.x2();
         double zz1 = (double)this.z1();
         double zz2 = (double)this.z2();
         double m = (zz2 - zz1) / (xx2 - xx1);
         double n = (double)1.0F / m;
         if (xx1 < (double)rxMin) {
            xx1 = (double)rxMin;
            zz1 = m * (double)(rxMin - this.x1()) + (double)this.z1();
         } else if (xx1 > (double)rxMax) {
            xx1 = (double)rxMax;
            zz1 = m * (double)(rxMax - this.x1()) + (double)this.z1();
         }

         if (zz1 < (double)rzMin) {
            zz1 = (double)rzMin;
            xx1 = n * (double)(rzMin - this.z1()) + (double)this.x1();
         } else if (zz1 > (double)rzMax) {
            zz1 = (double)rzMax;
            xx1 = n * (double)(rzMax - this.z1()) + (double)this.x1();
         }

         if (xx2 < (double)rxMin) {
            xx2 = (double)rxMin;
            zz2 = m * (double)(rxMin - this.x1()) + (double)this.z1();
         } else if (xx2 > (double)rxMax) {
            xx2 = (double)rxMax;
            zz2 = m * (double)(rxMax - this.x1()) + (double)this.z1();
         }

         if (zz2 < (double)rzMin) {
            zz2 = (double)rzMin;
            xx2 = n * (double)(rzMin - this.z1()) + (double)this.x1();
         } else if (zz2 > (double)rzMax) {
            zz2 = (double)rzMax;
            xx2 = n * (double)(rzMax - this.z1()) + (double)this.x1();
         }

         if ((!(xx1 < (double)rxMin) || !(xx2 < (double)rxMin)) && (!(xx1 > (double)rxMax) || !(xx2 > (double)rxMax))) {
            return true;
         }
      }

      return false;
   }

   public double angle() {
      int dx = this.x2() - this.x1();
      int dz = this.z2() - this.z1();
      double angleRadians = Math.atan2((double)dz, (double)dx);
      double degrees = Math.toDegrees(angleRadians) - (double)90.0F;
      return class_3532.method_15338(degrees);
   }
}
