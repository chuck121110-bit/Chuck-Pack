package xaero.common;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import xaero.common.cache.BlockStateShortShapeCache;
import xaero.common.events.ClientEvents;
import xaero.common.events.ClientEventsFabric;
import xaero.common.events.CommonEvents;
import xaero.common.events.CommonEventsFabric;
import xaero.common.events.ModClientEvents;
import xaero.common.events.ModClientEventsFabric;
import xaero.common.events.ModCommonEvents;
import xaero.common.events.ModCommonEventsFabric;
import xaero.common.minimap.highlight.HighlighterRegistry;
import xaero.common.minimap.write.MinimapWriter;
import xaero.common.minimap.write.MinimapWriterFabric;
import xaero.common.mods.SupportMods;
import xaero.common.mods.SupportModsFabric;
import xaero.common.server.mods.SupportServerMods;
import xaero.common.server.mods.SupportServerModsFabric;
import xaero.hud.minimap.module.MinimapSession;

public class PlatformContextFabric extends PlatformContext {
   private final HudMod modMain;
   public boolean loadLaterNeeded;
   public boolean loadLaterDone;
   public Throwable firstStageError;
   private PlatformContextLoaderClientOnlyFabric loaderClientOnly;
   private PlatformContextLoaderCommonFabric loaderCommon;

   public PlatformContextFabric(HudMod modMain) {
      this.modMain = modMain;
   }

   public void postLoadClient() {
      this.loadLaterNeeded = true;
   }

   public void postLoadCommon() {
   }

   public void postLoadServer() {
      this.loadLaterNeeded = true;
   }

   public boolean preTryLoadLater() {
      if (this.loadLaterDone) {
         return true;
      } else if (this.firstStageError != null) {
         throw new RuntimeException(this.firstStageError);
      } else if (!this.loadLaterNeeded) {
         return true;
      } else {
         this.loadLaterDone = true;
         return false;
      }
   }

   public boolean preTryLoadLaterServer() {
      if (this.loadLaterDone) {
         return true;
      } else if (this.firstStageError != null) {
         throw new RuntimeException(this.firstStageError);
      } else if (!this.loadLaterNeeded) {
         return true;
      } else {
         this.loadLaterDone = true;
         return false;
      }
   }

   public ClientEvents createClientEvents(HudMod modMain) {
      return new ClientEventsFabric(modMain);
   }

   public CommonEvents createCommonEvents(HudMod modMain) {
      return new CommonEventsFabric(modMain);
   }

   public PlatformContextLoaderClientOnlyFabric getLoaderClientOnly() {
      if (this.loaderClientOnly == null) {
         this.loaderClientOnly = new PlatformContextLoaderClientOnlyFabric();
      }

      return this.loaderClientOnly;
   }

   public PlatformContextLoaderCommonFabric getLoaderCommon() {
      if (this.loaderCommon == null) {
         this.loaderCommon = new PlatformContextLoaderCommonFabric();
      }

      return this.loaderCommon;
   }

   public ModClientEvents createModClientEvents(IXaeroMinimap modMain) {
      return new ModClientEventsFabric(modMain);
   }

   public ModCommonEvents createModCommonEvents(IXaeroMinimap modMain) {
      return new ModCommonEventsFabric(modMain);
   }

   public SupportMods createSupportMods(IXaeroMinimap modMain) {
      return new SupportModsFabric(modMain);
   }

   public SupportServerMods createSupportServerMods(HudMod hudMod) {
      return new SupportServerModsFabric();
   }

   public MinimapWriter createMinimapWriter(IXaeroMinimap modMain, MinimapSession xaeroMinimapSession, BlockStateShortShapeCache blockStateShortShapeCache, HighlighterRegistry highlighterRegistry) {
      return new MinimapWriterFabric(modMain, xaeroMinimapSession, blockStateShortShapeCache, highlighterRegistry);
   }

   public String getModInfoVersion() {
      ModContainer modContainer = (ModContainer)FabricLoader.getInstance().getModContainer(this.modMain.getModId()).get();
      return modContainer.getMetadata().getVersion().getFriendlyString() + "_fabric";
   }

   public static PlatformContextFabric get() {
      return (PlatformContextFabric)HudMod.INSTANCE.getPlatformContext();
   }
}
