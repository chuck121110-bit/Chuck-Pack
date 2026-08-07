package xaero.common.mods;

import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;

public class SupportModsFabric extends SupportMods {
   public SupportAmecs amecs = null;
   public SupportWraithWaystones wraithWaystones = null;

   public SupportModsFabric(IXaeroMinimap modMain) {
      super(modMain);

      try {
         Class mmClassTest = Class.forName("de.siphalor.amecs.api.KeyModifiers");
         this.amecs = new SupportAmecs(MinimapLogs.LOGGER);
      } catch (ClassNotFoundException var3) {
      }

   }

   public boolean amecs() {
      return this.amecs != null;
   }

   public boolean wraithWaystones() {
      return this.wraithWaystones != null;
   }

   public void registerClientEvents() {
      super.registerClientEvents();
   }

   public void onClientTick() {
      super.onClientTick();
   }

   public void onMinimapSessionStarted(MinimapSession minimapSession) {
      super.onMinimapSessionStarted(minimapSession);
   }
}
