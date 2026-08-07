package xaero.hud.minimap.radar.render.element;

import java.util.Iterator;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.hud.minimap.radar.state.RadarList;

public final class RadarRenderProvider extends MinimapElementRenderProvider<class_1297, RadarRenderContext> {
   private boolean used;
   private class_1297 renderEntity;
   private Iterator<RadarList> entityLists;
   private RadarList currentList;
   private RadarList listForContext;
   private int currentListIndex;

   public void begin(MinimapElementRenderLocation location, RadarRenderContext context) {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      this.used = true;
      this.renderEntity = class_310.method_1551().method_1560();
      context.reversedOrder = MinimapKeyMappings.REVERSE_ENTITY_RADAR.method_1434();
      class_437 screenBU = class_310.method_1551().field_1755;
      class_310.method_1551().field_1755 = null;
      context.playerListDown = class_310.method_1551().field_1690.field_1907.method_1434() || MinimapKeyMappings.ALTERNATIVE_LIST_PLAYERS.method_1434();
      class_310.method_1551().field_1755 = screenBU;
      this.entityLists = minimapSession.getRadarSession().getState().getRadarLists().iterator();
      this.currentList = null;
      this.listForContext = null;
      this.currentListIndex = 0;
   }

   private void ensureList(MinimapElementRenderLocation location, RadarRenderContext context) {
      label38:
      while(this.currentList == null || this.currentListIndex >= this.currentList.size() || this.currentListIndex < 0) {
         while(this.entityLists.hasNext()) {
            this.currentList = (RadarList)this.entityLists.next();
            this.currentListIndex = context.reversedOrder ? this.currentList.size() - 1 : 0;
            if (location != MinimapElementRenderLocation.IN_MINIMAP && location != MinimapElementRenderLocation.OVER_MINIMAP || location == MinimapElementRenderLocation.OVER_MINIMAP == this.shouldRenderOverMinimap(context)) {
               continue label38;
            }
         }

         this.currentList = null;
         this.currentListIndex = 0;
         break;
      }

   }

   private boolean shouldRenderOverMinimap(RadarRenderContext context) {
      int settingValue = ((Double)this.currentList.getEffective(EntityRadarCategorySettings.RENDER_OVER_MINIMAP)).intValue();
      return settingValue == 2 || settingValue == 1 && context.playerListDown;
   }

   public boolean hasNext(MinimapElementRenderLocation location, RadarRenderContext context) {
      this.ensureList(location, context);
      if (this.currentList == null) {
         return false;
      } else {
         return !context.reversedOrder && this.currentListIndex < this.currentList.size() || context.reversedOrder && this.currentListIndex >= 0;
      }
   }

   public class_1297 setupContextAndGetNext(MinimapElementRenderLocation location, RadarRenderContext context) {
      this.ensureList(location, context);
      if (this.listForContext != this.currentList) {
         context.radarList = this.currentList;
         this.setupContextForList(this.currentList, context);
         this.listForContext = this.currentList;
      }

      class_1297 result = this.getNext(location, context);
      if (result == null) {
         return null;
      } else {
         this.setupContextForEntity(result, context);
         return result;
      }
   }

   public class_1297 getNext(MinimapElementRenderLocation location, RadarRenderContext context) {
      class_1297 result = this.currentList.get(this.currentListIndex);
      this.currentListIndex += context.reversedOrder ? -1 : 1;
      return this.renderEntity == result ? null : result;
   }

   public void end(MinimapElementRenderLocation location, RadarRenderContext context) {
      this.used = false;
      this.renderEntity = null;
      context.radarList = null;
   }

   public void setupContextForList(RadarList radarList, RadarRenderContext context) {
      context.iconScale = (Double)radarList.getEffective(EntityRadarCategorySettings.ICON_SCALE);
      context.dotSize = context.isMainDot ? (Integer)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.RADAR_MAIN_DOT_SIZE) : ((Double)radarList.getEffective(EntityRadarCategorySettings.DOT_SIZE)).intValue();
      context.dotScale = (double)1.0F + (double)0.5F * (double)(context.dotSize - 1);
      int icons = ((Double)radarList.getEffective(EntityRadarCategorySettings.ICONS)).intValue();
      context.iconsForCategory = icons == 1 && context.playerListDown || icons == 2;
   }

   public void setupContextForEntity(class_1297 entity, RadarRenderContext context) {
      context.icon = context.iconsForCategory;
   }

   public boolean isUsed() {
      return this.used;
   }
}
