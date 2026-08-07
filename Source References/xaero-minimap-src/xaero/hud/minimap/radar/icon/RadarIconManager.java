package xaero.hud.minimap.radar.icon;

import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.lwjgl.opengl.GL11;
import xaero.common.icon.XaeroIcon;
import xaero.common.icon.XaeroIconAtlas;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.radar.icon.cache.RadarIconCache;
import xaero.hud.minimap.radar.icon.cache.RadarIconEntityCache;
import xaero.hud.minimap.radar.icon.cache.id.RadarIconKey;
import xaero.hud.minimap.radar.icon.cache.id.armor.RadarIconArmor;
import xaero.hud.minimap.radar.icon.cache.id.armor.RadarIconArmorHandler;
import xaero.hud.minimap.radar.icon.cache.id.variant.RadarIconVariantHandler;
import xaero.hud.minimap.radar.icon.creator.RadarIconCreator;
import xaero.hud.minimap.radar.icon.definition.BuiltInRadarIconDefinitions;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinitionManager;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconBasicForms;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;

public class RadarIconManager {
   public static final XaeroIcon FAILED = new XaeroIcon((XaeroIconAtlas)null, 0, 0);
   public static final XaeroIcon DOT = new XaeroIcon((XaeroIconAtlas)null, 0, 0);
   private boolean canPrerender;
   private final RadarIconCreator iconCreator;
   private final RadarIconModelConfig defaultModelConfig;
   private final RadarIconDefinitionManager definitionManager;
   private final RadarIconVariantHandler variantHandler;
   private final RadarIconArmorHandler armorHandler;
   private final RadarIconCache iconCache;

   public RadarIconManager(RadarIconCreator iconCreator) {
      this.iconCreator = iconCreator;
      this.definitionManager = new RadarIconDefinitionManager();
      this.variantHandler = new RadarIconVariantHandler();
      this.iconCache = new RadarIconCache();
      this.definitionManager.reloadResources();
      this.defaultModelConfig = new RadarIconModelConfig();
      this.armorHandler = new RadarIconArmorHandler();
   }

   public <T extends class_1297> XaeroIcon get(T entity, float scale, boolean debug, boolean debugEntityVariantIds, MinimapElementGraphics guiGraphics, class_276 defaultFramebuffer) {
      class_1299<?> entityType = entity.method_5864();
      RadarIconDefinition iconDefinition = this.definitionManager.get(class_1299.method_5890(entityType));
      class_898 renderManager = class_310.method_1551().method_1561();
      class_897<? super T, ?> entityRenderer = renderManager.method_3953(entity);
      return this.get(entity, entityType, iconDefinition, entityRenderer, scale, debug, debugEntityVariantIds, guiGraphics, defaultFramebuffer);
   }

   private <T extends class_1297, S extends class_10017> XaeroIcon get(T entity, class_1299<?> entityType, RadarIconDefinition iconDefinition, class_897<? super T, S> entityRenderer, float scale, boolean debug, boolean debugEntityVariantIds, MinimapElementGraphics guiGraphics, class_276 defaultFramebuffer) {
      S entityRenderState = entityRenderer.method_62425(entity, 1.0F);
      BuiltInRadarIconDefinitions.prepareEntityRenderState(entityRenderState);
      Object variant = this.variantHandler.getEntityVariant(iconDefinition, entity, entityRenderer, entityRenderState);

      while(GL11.glGetError() != 0) {
      }

      if (variant == null) {
         return null;
      } else {
         RadarIconArmor armor = null;
         if (entity instanceof class_1309 && !(entity instanceof class_1657)) {
            armor = this.armorHandler.getArmor((class_1309)entity);
         }

         RadarIconEntityCache entityIconCache = this.iconCache.getEntityCache(entityType);
         RadarIconKey iconKey = new RadarIconKey(variant, armor);
         XaeroIcon cachedValue = entityIconCache.get(iconKey);
         if (entityIconCache.isInvalidVariantClass()) {
            return FAILED;
         } else if (cachedValue != null) {
            return cachedValue;
         } else {
            String entityVariantString = entityIconCache.getVariantString(iconKey);
            RadarIconForm iconForm;
            if (iconDefinition != null) {
               iconForm = entityVariantString == null ? null : iconDefinition.getVariantForm(entityVariantString);
               if (iconForm == null) {
                  String variantMapKey = "default";
                  iconForm = iconDefinition.getVariantForm(variantMapKey);
               }
            } else {
               iconForm = (RadarIconForm)(entity instanceof class_1309 ? RadarIconBasicForms.DEFAULT_MODEL : RadarIconBasicForms.SELF_ITEM);
            }

            if (debugEntityVariantIds && entityVariantString != null && (this.canPrerender || iconForm == RadarIconBasicForms.DOT)) {
               class_310.method_1551().field_1705.method_1743().method_1812(class_2561.method_43470(entityVariantString));
            }

            if (iconForm == RadarIconBasicForms.DOT) {
               entityIconCache.add(iconKey, DOT);
               return DOT;
            } else if (!this.canPrerender) {
               return null;
            } else {
               RadarIconCreator.Parameters parameters = new RadarIconCreator.Parameters(variant, this.defaultModelConfig, iconForm, scale, debug);
               cachedValue = this.iconCreator.create(guiGraphics, entityRenderer, entityRenderState, entity, defaultFramebuffer, parameters);
               entityIconCache.add(iconKey, cachedValue);
               this.canPrerender = false;
               return cachedValue;
            }
         }
      }
   }

   public void reset() {
      this.iconCreator.reset();
      this.iconCache.clear();
      MinimapLogs.LOGGER.info("Radar icon manager reset!");
   }

   public void resetResources() {
      this.definitionManager.reloadResources();
   }

   public void allowPrerender() {
      this.canPrerender = true;
   }

   public void onModelRenderTrace(class_3879 model, class_4588 vertexConsumer, int color) {
      this.iconCreator.getRenderTracer().onModelRender(model, vertexConsumer, color);
   }

   public void onModelPartRenderTrace(class_630 modelRenderer, int color) {
      this.iconCreator.getRenderTracer().onModelPartRender(modelRenderer, color);
   }
}
