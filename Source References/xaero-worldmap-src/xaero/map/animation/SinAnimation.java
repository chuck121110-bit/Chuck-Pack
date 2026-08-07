package xaero.map.animation;

public class SinAnimation extends Animation {
   public SinAnimation(double from, double to, long time) {
      super(from, to, time);
   }

   public double getCurrent() {
      double passed = Math.min((double)1.0F, (double)(System.currentTimeMillis() - this.start) / (double)this.time);
      double angle = (Math.PI / 2D) * passed;
      return this.from + this.off * Math.sin(angle);
   }
}
