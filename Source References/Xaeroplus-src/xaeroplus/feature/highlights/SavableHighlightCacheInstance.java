package xaeroplus.feature.highlights;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.lenni0451.lambdaevents.EventHandler;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.XaeroWorldChangeEvent;

public class SavableHighlightCacheInstance {
   private static final Set<SavableHighlightCacheInstance> INSTANCES = ConcurrentHashMap.newKeySet();
   private ChunkHighlightCache cache;
   private final String dbName;

   public SavableHighlightCacheInstance(String dbName) {
      this.dbName = dbName;
      this.cache = new ChunkHighlightLocalCache();
   }

   public static Set<SavableHighlightCacheInstance> instances() {
      return INSTANCES;
   }

   public ChunkHighlightCache get() {
      return this.cache;
   }

   public synchronized void onEnable() {
      XaeroPlus.EVENT_BUS.register(this);
      this.cache.onEnable();
      INSTANCES.add(this);
   }

   public synchronized void onDisable() {
      this.cache.onDisable();
      XaeroPlus.EVENT_BUS.unregister(this);
      INSTANCES.remove(this);
   }

   public synchronized void setDiskCache(final boolean disk, final boolean enabled) {
      try {
         this.cache.onDisable();
         ChunkHighlightCache var4 = this.cache;
         if (var4 instanceof ChunkHighlightSavingCache savingCache) {
            savingCache.close();
         }

         if (disk) {
            this.cache = new ChunkHighlightSavingCache(this.dbName);
         } else {
            this.cache = new ChunkHighlightLocalCache();
         }

         if (enabled) {
            this.cache.onEnable();
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error setting {} cache [{} {}]", new Object[]{this.dbName, disk, enabled, e});
      }

   }

   @EventHandler
   public void onXaeroWorldChange(XaeroWorldChangeEvent event) {
      try {
         this.cache.handleWorldChange(event);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error handling world change event for cache: {} event: {}", new Object[]{this.dbName, event, e});
      }

   }

   @EventHandler
   public void onClientTickEvent(ClientTickEvent.Post event) {
      try {
         this.cache.handleTick();
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error handling tick event for cache: {}", this.dbName, e);
      }

   }

   public CompletableFuture<Void> shutdown() {
      if (!INSTANCES.contains(this)) {
         return CompletableFuture.completedFuture((Object)null);
      } else {
         XaeroPlus.EVENT_BUS.unregister(this);
         INSTANCES.remove(this);
         ChunkHighlightCache var2 = this.cache;
         if (var2 instanceof ChunkHighlightSavingCache) {
            ChunkHighlightSavingCache savingCache = (ChunkHighlightSavingCache)var2;
            return savingCache.onShutdown();
         } else {
            return CompletableFuture.completedFuture((Object)null);
         }
      }
   }
}
