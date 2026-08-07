package xaero.common;

import java.io.IOException;
import net.minecraft.class_634;
import net.minecraft.class_746;
import xaero.common.minimap.MinimapProcessor;
import xaero.hud.HudSession;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;

/** @deprecated */
@Deprecated
public class XaeroMinimapSession extends HudSession {
   public XaeroMinimapSession(HudMod modMain) {
      super(modMain);
   }

   public void init(class_634 connection) throws IOException {
      super.init(connection);
   }

   protected void cleanup() {
      super.cleanup();
   }

   public MinimapProcessor getMinimapProcessor() {
      return ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getProcessor();
   }

   public static XaeroMinimapSession getCurrentSession() {
      return (XaeroMinimapSession)HudSession.getCurrentSession();
   }

   public static XaeroMinimapSession getForPlayer(class_746 player) {
      return (XaeroMinimapSession)HudSession.getForPlayer(player);
   }

   public IXaeroMinimap getModMain() {
      return this.getHudMod();
   }
}
