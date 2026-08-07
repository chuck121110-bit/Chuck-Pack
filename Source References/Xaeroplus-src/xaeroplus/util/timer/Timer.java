package xaeroplus.util.timer;

public interface Timer {
   void reset();

   void skip();

   boolean tick(long delay);

   boolean tick(long delay, boolean resetIfTick);
}
