package xaero.hud;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_746;
import xaero.common.HudMod;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.core.XaeroMinimapCore;
import xaero.hud.controls.key.KeyMappingTickHandler;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.module.HudModule;
import xaero.hud.module.ModuleSession;
import xaero.hud.module.ModuleSessionHandler;

public class HudSession {
   protected final HudMod modMain;
   protected KeyMappingTickHandler keyMappingTickHandler;
   private final Map<HudModule<?>, ModuleSession<?>> moduleSessions;
   private HudModule<?> lastModuleSessionRequest;
   private ModuleSession<?> lastModuleSessionPassed;
   protected boolean usable;

   public HudSession(HudMod modMain) {
      this.modMain = modMain;
      this.moduleSessions = new HashMap();
   }

   public <MS extends ModuleSession<MS>> MS getSession(HudModule<MS> module) {
      if (module == this.lastModuleSessionRequest) {
         return (MS)this.lastModuleSessionPassed;
      } else {
         MS mappedSession = (MS)(this.moduleSessions.get(module));
         this.lastModuleSessionRequest = module;
         this.lastModuleSessionPassed = mappedSession;
         return mappedSession;
      }
   }

   public void init(class_634 connection) throws IOException {
      this.lastModuleSessionRequest = null;
      this.lastModuleSessionPassed = null;
      this.keyMappingTickHandler = new KeyMappingTickHandler(this.modMain.getKeyMappingControllers());
      ModuleSessionHandler var10000 = this.modMain.getHud().getSessionHandler();
      HudMod var10001 = this.modMain;
      Map var10003 = this.moduleSessions;
      Objects.requireNonNull(var10003);
      var10000.resetSessions(var10001, connection, var10003::put);
      this.usable = true;
      MinimapLogs.LOGGER.info("New Xaero hud session initialized!");
   }

   public final void tryCleanup() {
      try {
         this.cleanup();
         MinimapLogs.LOGGER.info("Xaero hud session finalized.");
      } catch (Throwable t) {
         MinimapLogs.LOGGER.error("Xaero hud session failed to finalize properly.", t);
      }

      this.moduleSessions.clear();
      this.usable = false;
   }

   protected void cleanup() {
      this.lastModuleSessionRequest = null;
      this.lastModuleSessionPassed = null;
      this.modMain.getHud().getSessionHandler().closeSessions(this.modMain);
   }

   public static HudSession getCurrentSession() {
      HudSession session = getForPlayer(class_310.method_1551().field_1724);
      if (session == null && XaeroMinimapCore.currentSession != null && XaeroMinimapCore.currentSession.usable) {
         session = XaeroMinimapCore.currentSession;
      }

      return session;
   }

   public static HudSession getForPlayer(class_746 player) {
      return player != null && player.field_3944 != null ? ((IXaeroMinimapClientPlayNetHandler)player.field_3944).getXaero_minimapSession() : null;
   }

   public KeyMappingTickHandler getKeyMappingTickHandler() {
      return this.keyMappingTickHandler;
   }

   public HudMod getHudMod() {
      return this.modMain;
   }
}
