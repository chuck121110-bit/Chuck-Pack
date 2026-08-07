package xaero.hud.minimap.radar.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_270;
import net.minecraft.class_638;
import xaero.common.HudMod;
import xaero.common.effect.Effects;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.common.misc.Misc;
import xaero.hud.category.rule.resolver.ObjectCategoryRuleResolver;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.lib.client.config.ClientConfigManager;

public class RadarStateUpdater {
   private final EntityRadarCategoryManager categoryManager;
   private final RadarState state;
   private class_1297 lastRenderEntity;
   private final Map<EntityRadarCategory, Map<EntityRadarCategory, RadarList>> updateMap;

   public RadarStateUpdater(EntityRadarCategoryManager categoryManager, RadarState state) {
      this.categoryManager = categoryManager;
      this.state = state;
      this.updateMap = new HashMap();
   }

   public void update(class_638 world, class_1297 renderEntity, class_1657 player) {
      if (renderEntity == null) {
         renderEntity = this.lastRenderEntity;
      }

      List<RadarList> radarLists = this.state.getUpdatableLists();
      EntityRadarCategory rootCategory = this.categoryManager.getRootCategory();
      EntityRadarCategory syncedRootCategory = this.categoryManager.getEffectiveSyncedRootCategory();
      this.ensureCategories(this.state, rootCategory, syncedRootCategory, radarLists);
      radarLists.forEach(RadarList::clearEntities);
      if (!HudMod.INSTANCE.isFairPlay()) {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         boolean displayRadar = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.DISPLAY_RADAR);
         if (displayRadar || this.isWorldMapRadarEnabled()) {
            if (world != null) {
               if (renderEntity != null) {
                  if (player != null) {
                     if (!Misc.hasEffect(player, Effects.NO_RADAR)) {
                        if (!Misc.hasEffect(player, Effects.NO_RADAR_HARMFUL)) {
                           if (HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getServerSynced().isChannelPresentOnServer() || MinimapClientWorldDataHelper.getWorldData(world).getSyncedRules().allowRadarOnServer) {
                              ObjectCategoryRuleResolver categoryRuleResolver = this.categoryManager.getRuleResolver();
                              Iterable<class_1297> worldEntities = world.method_18112();
                              boolean shouldHideInvisible = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.RADAR_HIDE_INVISIBLE);

                              for(class_1297 entity : worldEntities) {
                                 if (entity != null && (!shouldHideInvisible || !this.isInvisibleTo(entity, player))) {
                                    EntityRadarCategory entityCategory = (EntityRadarCategory)categoryRuleResolver.resolve(rootCategory, entity, player);
                                    if (entityCategory != null) {
                                       EntityRadarCategory syncedEntityCategory;
                                       if (syncedRootCategory == null) {
                                          syncedEntityCategory = null;
                                       } else {
                                          syncedEntityCategory = (EntityRadarCategory)categoryRuleResolver.resolve(syncedRootCategory, entity, player);
                                          if (syncedEntityCategory == null) {
                                             continue;
                                          }
                                       }

                                       RadarList radarList = (RadarList)((Map)this.updateMap.get(entityCategory)).get(syncedEntityCategory == null ? entityCategory : syncedEntityCategory);
                                       if ((Boolean)radarList.getEffective(EntityRadarCategorySettings.DISPLAYED)) {
                                          double offY = renderEntity.method_23318() - entity.method_23318();
                                          int heightLimit = ((Double)radarList.getEffective(EntityRadarCategorySettings.HEIGHT_LIMIT)).intValue();
                                          if (!(offY * offY > (double)(heightLimit * heightLimit))) {
                                             int entityNumber = ((Double)radarList.getEffective(EntityRadarCategorySettings.ENTITY_NUMBER)).intValue();
                                             if (entityNumber == 0 || radarList.size() < entityNumber) {
                                                radarList.add(entity);
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void ensureCategories(RadarState state, EntityRadarCategory rootCategory, EntityRadarCategory syncedRootCategory, List<RadarList> radarLists) {
      boolean reversedOrder = MinimapKeyMappings.REVERSE_ENTITY_RADAR.method_1434();
      if (state.getListsGeneratedForConfig() != rootCategory || state.getListsGeneratedForSyncedConfig() != syncedRootCategory) {
         this.updateMap.clear();
         radarLists.clear();
         this.traceAddCategories(rootCategory, syncedRootCategory, radarLists);
         Collections.sort(radarLists);
         state.setListsGeneratedForConfig(rootCategory);
         state.setListsGeneratedForSyncedConfig(syncedRootCategory);
         state.setListsReversedOrder(false);
      }

      if (state.getListsReversedOrder() != reversedOrder) {
         Collections.reverse(radarLists);
         state.setListsReversedOrder(reversedOrder);
      }
   }

   private void traceAddCategories(EntityRadarCategory category, EntityRadarCategory syncedRootCategory, List<RadarList> radarLists) {
      category.getDirectSubCategoryIterator().forEachRemaining((sb) -> this.traceAddCategories(sb, syncedRootCategory, radarLists));
      if (syncedRootCategory == null) {
         RadarList radarList = RadarList.Builder.getDefault().build().setClientCategory(category).setSyncedCategory((EntityRadarCategory)null);
         this.putOnUpdateMap(category, category, radarList);
         radarLists.add(radarList);
      } else {
         this.traceAddSyncedCategories(category, syncedRootCategory, radarLists);
      }
   }

   private void traceAddSyncedCategories(EntityRadarCategory category, EntityRadarCategory syncedCategory, List<RadarList> radarLists) {
      syncedCategory.getDirectSubCategoryIterator().forEachRemaining((sb) -> this.traceAddSyncedCategories(category, sb, radarLists));
      RadarList radarList = RadarList.Builder.getDefault().build().setClientCategory(category).setSyncedCategory(syncedCategory);
      this.putOnUpdateMap(category, syncedCategory, radarList);
      radarLists.add(radarList);
   }

   private void putOnUpdateMap(EntityRadarCategory category, EntityRadarCategory syncedCategory, RadarList radarList) {
      Map<EntityRadarCategory, RadarList> syncedToListMap = (Map)this.updateMap.get(category);
      if (syncedToListMap == null) {
         this.updateMap.put(category, syncedToListMap = new HashMap());
      }

      syncedToListMap.put(syncedCategory, radarList);
   }

   private boolean isWorldMapRadarEnabled() {
      return !HudMod.INSTANCE.getSupportMods().worldmap() ? false : HudMod.INSTANCE.getSupportMods().worldmapSupport.worldMapIsRenderingRadar();
   }

   private boolean isInvisibleTo(class_1297 entity, class_1657 player) {
      return entity.method_5756(player) || this.shouldHideForSneaking(entity, player);
   }

   private boolean shouldHideForSneaking(class_1297 e, class_1657 p) {
      if (!e.method_5715()) {
         return false;
      } else {
         class_270 team = e.method_5781();
         return team == null || team != p.method_5781();
      }
   }

   public void setLastRenderViewEntity(class_1297 lastRenderEntity) {
      this.lastRenderEntity = lastRenderEntity;
   }
}
