package xaeroplus.module;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Arrays;
import xaeroplus.module.impl.BaritoneGoalSync;
import xaeroplus.module.impl.BaritonePathSync;
import xaeroplus.module.impl.Beacons;
import xaeroplus.module.impl.Breadcrumbs;
import xaeroplus.module.impl.Drawing;
import xaeroplus.module.impl.FpsLimiter;
import xaeroplus.module.impl.Highways;
import xaeroplus.module.impl.LavaColumns;
import xaeroplus.module.impl.LiquidNewChunks;
import xaeroplus.module.impl.MapArtGrid;
import xaeroplus.module.impl.OldBiomes;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;
import xaeroplus.module.impl.Pearls;
import xaeroplus.module.impl.PortalSkipDetection;
import xaeroplus.module.impl.Portals;
import xaeroplus.module.impl.RegionGrid;
import xaeroplus.module.impl.RenderDistance;
import xaeroplus.module.impl.SpawnChunks;
import xaeroplus.module.impl.SpawnChunksPlayer;
import xaeroplus.module.impl.SpawnPoint;
import xaeroplus.module.impl.TeleportFailNotifier;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.module.impl.WorldBorder;
import xaeroplus.module.impl.WorldTools;

public class ModuleManager {
   private static final Reference2ObjectMap<Class<? extends Module>, Module> modulesClassMap = new Reference2ObjectOpenHashMap();

   public static void addModule(Module module) {
      modulesClassMap.put(module.getClass(), module);
   }

   public static <T extends Module> T getModule(Class<T> clazz) {
      return (T)(modulesClassMap.get(clazz));
   }

   static {
      Arrays.asList(new BaritoneGoalSync(), new BaritonePathSync(), new Beacons(), new Breadcrumbs(), new Drawing(), new FpsLimiter(), new Highways(), new LavaColumns(), new LiquidNewChunks(), new MapArtGrid(), new OldChunks(), new OldBiomes(), new PaletteNewChunks(), new Pearls(), new Portals(), new PortalSkipDetection(), new RegionGrid(), new RenderDistance(), new SpawnChunks(), new SpawnChunksPlayer(), new SpawnPoint(), new TeleportFailNotifier(), new TickTaskExecutor(), new WorldBorder(), new WorldTools()).forEach(ModuleManager::addModule);
   }
}
