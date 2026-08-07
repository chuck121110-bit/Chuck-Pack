package xaero.hud.minimap.radar.color;

import net.minecraft.class_1297;
import net.minecraft.class_270;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.hud.minimap.radar.state.RadarList;

public class RadarColorHelper {
   public int getTeamColor(class_1297 e) {
      Integer teamColor = null;
      class_270 team = e.method_5781();
      if (team != null) {
         teamColor = team.method_1202().method_532();
      }

      return teamColor == null ? -1 : teamColor;
   }

   public int getEntityColor(class_1297 entity, float offY, boolean cave, int heightLimit, int startFadingAt, boolean heightBasedFade, RadarColor radarColor, RadarColor fallbackColor) {
      int color = this.getRadarColorHex(entity, radarColor, fallbackColor);
      float heightFade = heightBasedFade ? this.getEntityHeightFade(offY, heightLimit, startFadingAt) : 1.0F;
      if (heightFade >= 1.0F) {
         return color;
      } else {
         int red = color >> 16 & 255;
         int green = color >> 8 & 255;
         int blue = color & 255;
         int alpha = 255;
         if (cave) {
            alpha = (int)((float)alpha * heightFade);
         } else {
            red = (int)((float)red * heightFade);
            green = (int)((float)green * heightFade);
            blue = (int)((float)blue * heightFade);
         }

         return alpha << 24 | red << 16 | green << 8 | blue;
      }
   }

   private int getRadarColorHex(class_1297 entity, RadarColor radarColor, RadarColor fallbackColor) {
      if (radarColor != null) {
         return radarColor.getHex();
      } else {
         int entityTeamColour = this.getTeamColor(entity);
         return entityTeamColour != -1 ? -16777216 | entityTeamColour : fallbackColor.getHex();
      }
   }

   public RadarColor getFallbackColor(RadarList radarList) {
      return this.getFallbackColor(radarList.getClientCategory(), radarList.getSyncedCategory());
   }

   public RadarColor getFallbackColor(EntityRadarCategory category, EntityRadarCategory syncedCategory) {
      EntityRadarCategory fallbackCategory = syncedCategory == null ? category : syncedCategory;

      while(true) {
         EntityRadarCategory superCategory = (EntityRadarCategory)fallbackCategory.getSuperCategory();
         if (superCategory == null) {
            if (fallbackCategory != syncedCategory) {
               return RadarColor.WHITE;
            }

            superCategory = category;
         }

         fallbackCategory = superCategory;
         Double categorySettingValue = (Double)superCategory.getSettingValue(EntityRadarCategorySettings.COLOR);
         if (categorySettingValue != null) {
            int colorSetting = categorySettingValue.intValue();
            if (colorSetting != -1) {
               return RadarColor.fromIndex(colorSetting);
            }
         }
      }
   }

   public float getEntityHeightFade(float offY, int heightLimit, int startFadingAt) {
      float level = (float)heightLimit - offY;
      if (level < 0.0F) {
         level = 0.0F;
      }

      float brightness = 1.0F;
      int threshold = startFadingAt == 0 ? heightLimit * 3 / 4 : heightLimit - startFadingAt;
      if (level <= (float)threshold) {
         brightness = 0.25F + 0.5F * level / (float)threshold;
      }

      return brightness;
   }
}
