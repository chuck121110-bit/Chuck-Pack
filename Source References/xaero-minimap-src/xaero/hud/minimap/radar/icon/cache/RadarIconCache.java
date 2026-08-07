package xaero.hud.minimap.radar.icon.cache;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1299;

public class RadarIconCache {
   private final Map<class_1299<?>, RadarIconEntityCache> iconCacheMap = new HashMap();

   public RadarIconEntityCache getEntityCache(class_1299<?> entityType) {
      RadarIconEntityCache result = (RadarIconEntityCache)this.iconCacheMap.get(entityType);
      if (result == null) {
         this.iconCacheMap.put(entityType, result = new RadarIconEntityCache(entityType));
      }

      return result;
   }

   public void clear() {
      this.iconCacheMap.clear();
   }
}
