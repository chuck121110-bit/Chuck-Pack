package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.lang.reflect.Method;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2960;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.apache.logging.log4j.Logger;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.radar.icon.definition.BuiltInRadarIconDefinitions;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;

public class RadarIconVariantHandler {
   private final StringBuilder legacyEntityStringBuilder = new StringBuilder();

   public <T extends class_1297> Object getEntityVariant(RadarIconDefinition iconDefinition, T entity, class_897<? super T, ?> entityRenderer, class_10017 entityRenderState) {
      Object variant = null;
      class_2960 entityTexture = null;

      try {
         class_2960 var10000;
         if (entityRenderer instanceof class_922 livingEntityRenderer) {
            var10000 = livingEntityRenderer.method_3885((class_10042)entityRenderState);
         } else {
            var10000 = null;
         }

         class_2960 entityTextureUnchecked = var10000;
         entityTexture = entityTextureUnchecked;
      } catch (Throwable e) {
         MinimapLogs.LOGGER.error("Exception while fetching entity texture to build its variant ID for " + String.valueOf(class_1299.method_5890(entity.method_5864())));
         MinimapLogs.LOGGER.error("The exception is most likely on another mod's end and suppressing it here could lead to more issues. Please report to appropriate mod devs.", e);
      }

      if (iconDefinition != null) {
         Method variantMethod = iconDefinition.getVariantMethod();
         if (variantMethod != null) {
            try {
               variant = variantMethod.invoke((Object)null, entityTexture, entityRenderer, entity);
            } catch (Throwable e) {
               class_2960 entityId = class_1299.method_5890(entity.method_5864());
               Logger var13 = MinimapLogs.LOGGER;
               String var10001 = iconDefinition.getVariantMethodString();
               var13.error("Exception while using the variant ID method " + var10001 + " defined for " + String.valueOf(entityId));
               MinimapLogs.LOGGER.error("If the exception is on another mod's end, suppressing it here could lead to more issues. Please report to appropriate mod devs.", e);
               iconDefinition.setVariantMethod((Method)null);
            }
         } else {
            variant = this.getLegacyVariantId(iconDefinition, entity, entityRenderer);
         }
      }

      if (variant == null) {
         variant = BuiltInRadarIconDefinitions.getVariant(entityTexture, entityRenderer, entity);
      }

      return variant;
   }

   private <T extends class_1297> String getLegacyVariantId(RadarIconDefinition iconDefinition, T entity, class_897<? super T, ?> entityRenderer) {
      Method variantIdBuilderMethod = iconDefinition.getVariantIdBuilderMethod();
      if (variantIdBuilderMethod != null && !variantIdBuilderMethod.equals(BuiltInRadarIconDefinitions.BUILD_VARIANT_ID_STRING_METHOD)) {
         this.legacyEntityStringBuilder.setLength(0);

         try {
            variantIdBuilderMethod.invoke((Object)null, this.legacyEntityStringBuilder, entityRenderer, entity);
            return this.legacyEntityStringBuilder.toString();
         } catch (Throwable e) {
            class_2960 entityId = class_1299.method_5890(entity.method_5864());
            Logger var10 = MinimapLogs.LOGGER;
            String var11 = iconDefinition.getVariantIdBuilderMethodString();
            var10.error("Exception while using the variant builder ID method " + var11 + " defined for " + String.valueOf(entityId));
            MinimapLogs.LOGGER.error("If the exception is on another mod's end, suppressing it here could lead to more issues. Please report to appropriate mod devs.", e);
            iconDefinition.setVariantIdBuilderMethod((Method)null);
            return null;
         }
      } else {
         Method variantOldIdMethod = iconDefinition.getOldVariantIdMethod();
         if (variantOldIdMethod != null && !variantOldIdMethod.equals(BuiltInRadarIconDefinitions.GET_VARIANT_ID_STRING_METHOD)) {
            try {
               return (String)variantOldIdMethod.invoke((Object)null, entityRenderer, entity);
            } catch (Throwable e) {
               class_2960 entityId = class_1299.method_5890(entity.method_5864());
               Logger var10000 = MinimapLogs.LOGGER;
               String var10001 = iconDefinition.getOldVariantIdMethodString();
               var10000.error("Exception while using the variant ID method " + var10001 + " defined for " + String.valueOf(entityId));
               MinimapLogs.LOGGER.error("If the exception is on another mod's end, suppressing it here could lead to more issues. Please report to appropriate mod devs.", e);
               iconDefinition.setOldVariantIdMethod((Method)null);
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
