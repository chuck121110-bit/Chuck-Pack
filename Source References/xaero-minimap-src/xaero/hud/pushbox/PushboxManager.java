package xaero.hud.pushbox;

import java.util.HashSet;
import java.util.Set;

public class PushboxManager {
   private final Set<PushBox> pushBoxes = new HashSet();

   public void add(PushBox pushBox) {
      this.pushBoxes.add(pushBox);
   }

   public Iterable<PushBox> getAll() {
      return this.pushBoxes;
   }
}
