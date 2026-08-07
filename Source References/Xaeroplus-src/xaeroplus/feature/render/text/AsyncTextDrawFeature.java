package xaeroplus.feature.render.text;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import xaero.map.gui.GuiMap;
import xaeroplus.Globals;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.GuiMapHelper;

public class AsyncTextDrawFeature extends AbstractTextDrawFeature {
   private final TextSupplier textSupplier;
   private final String id;
   private final AsyncLoadingCache<Long, List<Text>> textRenderCache;

   public AsyncTextDrawFeature(String id, TextSupplier textSupplier, final int refreshIntervalMs) {
      this.id = id;
      this.textSupplier = textSupplier;
      this.textRenderCache = Caffeine.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).refreshAfterWrite((long)refreshIntervalMs, TimeUnit.MILLISECONDS).executor(TickTaskExecutor.INSTANCE).buildAsync((CacheLoader)((k) -> this.loadTextInWindow()));
   }

   private List<Text> loadTextInWindow() {
      List<Text> out = new ArrayList();
      Long2ObjectMap<Text> texts = this.provideTextInWindow();
      ObjectIterator<Long2ObjectMap.Entry<Text>> it = Long2ObjectMaps.fastIterator(texts);

      while(it.hasNext()) {
         Text text = (Text)((Long2ObjectMap.Entry)it.next()).getValue();
         out.add(text);
      }

      return out;
   }

   private Long2ObjectMap<Text> provideTextInWindow() {
      Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
      int windowX;
      int windowZ;
      int windowSize;
      if (guiMapOptional.isPresent()) {
         GuiMap guiMap = (GuiMap)guiMapOptional.get();
         windowX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
         windowZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
         windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
      } else {
         windowX = ChunkUtils.getPlayerRegionX();
         windowZ = ChunkUtils.getPlayerRegionZ();
         windowSize = Math.max(3, Globals.minimapScaleMultiplier);
      }

      return this.textSupplier.getText(windowX, windowZ, windowSize, Globals.getCurrentDimensionId());
   }

   public List<Text> getTexts() {
      return (List)this.textRenderCache.get(0L).getNow(Collections.emptyList());
   }

   public String id() {
      return this.id;
   }

   public void invalidateCache() {
      this.textRenderCache.synchronous().invalidateAll();
   }

   public void close() {
      this.textRenderCache.synchronous().invalidateAll();
   }
}
