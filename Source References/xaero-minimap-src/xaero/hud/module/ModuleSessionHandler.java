package xaero.hud.module;

import java.util.function.BiConsumer;
import net.minecraft.class_634;
import xaero.common.HudMod;
import xaero.hud.render.module.IModuleRenderer;

public class ModuleSessionHandler {
   private final ModuleManager manager;

   public ModuleSessionHandler(ModuleManager manager) {
      this.manager = manager;
   }

   public void resetSessions(HudMod modMain, class_634 packetListener, BiConsumer<HudModule<?>, ModuleSession<?>> sessionDest) {
      for(HudModule<?> module : this.manager.getModules()) {
         this.resetSession(module, modMain, packetListener, sessionDest);
      }

   }

   public void closeSessions(HudMod modMain) {
      for(HudModule<?> module : this.manager.getModules()) {
         this.closeSession(module, modMain);
      }

   }

   private <MS extends ModuleSession<MS>> void resetSession(HudModule<MS> module, HudMod modMain, class_634 packetListener, BiConsumer<HudModule<?>, ModuleSession<?>> sessionDest) {
      this.closeSession(module, modMain);
      sessionDest.accept(module, (ModuleSession)module.getSessionFactory().apply(modMain, module, packetListener));
      HudMod.LOGGER.debug("Initialized new session for module {}!", module.getId());
   }

   private <MS extends ModuleSession<MS>> void closeSession(HudModule<MS> module, HudMod modMain) {
      if (module.getCurrentSession() != null) {
         try {
            module.getCurrentSession().close();
            HudMod.LOGGER.debug("Finalized session for module {}!", module.getId());
         } catch (Throwable t) {
            HudMod.LOGGER.error("Failed to finalize session for module {}!", module.getId(), t);
         }
      }

      module.setRenderer((IModuleRenderer)null);
   }
}
