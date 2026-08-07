package xaero.minimap;

import java.io.IOException;
import net.minecraft.class_634;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;

public class XaeroMinimapStandaloneSession extends XaeroMinimapSession {
   public XaeroMinimapStandaloneSession(HudMod modMain) {
      super(modMain);
   }

   public void init(class_634 connection) throws IOException {
      super.init(connection);
   }
}
