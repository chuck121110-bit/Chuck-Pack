package xaero.hud.minimap.info;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_1074;
import net.minecraft.class_1944;
import net.minecraft.class_1959;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.info.widget.InfoDisplayCommonWidgetFactories;
import xaero.hud.minimap.info.widget.InfoDisplayCycleWidgetFactory;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.lib.common.config.option.value.io.serialization.BuiltInConfigValueIOCodecs;

public class BuiltInInfoDisplays {
   private static List<InfoDisplay<?>> ALL = new ArrayList();
   public static final InfoDisplay<Boolean> COORDINATES;
   public static final InfoDisplay<Boolean> OVERWORLD_COORDINATES;
   public static final InfoDisplay<Boolean> CHUNK_COORDINATES;
   public static final InfoDisplay<Boolean> ANGLES;
   public static final InfoDisplay<Boolean> DIMENSION;
   public static final InfoDisplay<Boolean> BIOME;
   public static final InfoDisplay<Boolean> WEATHER;
   public static final InfoDisplay<Integer> LIGHT_LEVEL;
   public static final InfoDisplay<Integer> TIME;
   private static final Calendar CALENDAR;
   public static final InfoDisplay<Integer> REAL_TIME;
   public static final InfoDisplay<Boolean> HIGHLIGHTS;
   public static final InfoDisplay<Boolean> LIGHT_OVERLAY_INDICATOR;
   public static final InfoDisplay<Boolean> MANUAL_CAVE_MODE_INDICATOR;
   public static final InfoDisplay<Boolean> CUSTOM_SUB_WORLD;

   public static void forEach(Consumer<InfoDisplay<?>> action) {
      ALL.forEach(action);
   }

   static {
      InfoDisplay.Builder var10000 = InfoDisplay.Builder.begin().setId("coords").setName(class_2561.method_43471("gui.xaero_infodisplay_coords")).setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            int var10000 = playerPos.method_10263();
            String coords = var10000 + ", " + playerPos.method_10264() + ", " + playerPos.method_10260();
            if (class_310.method_1551().field_1772.method_1727(coords) >= availableWidth) {
               String stringLevel = "" + playerPos.method_10264();
               var10000 = playerPos.method_10263();
               coords = var10000 + ", " + playerPos.method_10260();
               compiler.addLine(coords);
               compiler.addLine(stringLevel);
            } else {
               compiler.addLine(coords);
            }

         }
      }).setLegacyGetter((legacySettings) -> {
         Boolean result = legacySettings.showCoordsLegacy;
         legacySettings.showCoordsLegacy = null;
         return result;
      });
      List var10001 = ALL;
      Objects.requireNonNull(var10001);
      COORDINATES = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("overworld_coords").setName(class_2561.method_43471("gui.xaero_infodisplay_overworld_coords")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            class_310 mc = class_310.method_1551();
            double coordinateScale = mc.field_1687.method_8597().comp_646();
            if (coordinateScale != (double)1.0F) {
               int overworldPlayerX = class_3532.method_15357((double)playerPos.method_10263() * coordinateScale);
               int overworldPlayerZ = class_3532.method_15357((double)playerPos.method_10260() * coordinateScale);
               String coords = "Xo: " + overworldPlayerX + ", Zo: " + overworldPlayerZ;
               compiler.addWords(coords);
            }
         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      OVERWORLD_COORDINATES = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("chunk_coords").setName(class_2561.method_43471("gui.xaero_infodisplay_chunk_coords")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            int chunkX = playerPos.method_10263() >> 4;
            int chunkZ = playerPos.method_10260() >> 4;
            int insideX = playerPos.method_10263() & 15;
            int insideZ = playerPos.method_10260() & 15;
            String coords = "C " + chunkX + ", " + chunkZ + " (" + insideX + "; " + insideZ + ")";
            compiler.addWords(coords);
         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      CHUNK_COORDINATES = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("angles").setName(class_2561.method_43471("gui.xaero_infodisplay_angles")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            class_310 mc = class_310.method_1551();
            compiler.addLine((class_2561)class_2561.method_43470(String.format("%.1f / %.1f", class_3532.method_15393(mc.method_1560().method_36454()), class_3532.method_15393(mc.method_1560().method_36455()))));
         }
      }).setLegacyGetter((legacySettings) -> {
         Boolean result = legacySettings.showAnglesLegacy;
         legacySettings.showAnglesLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      ANGLES = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("dimension").setName(class_2561.method_43471("gui.xaero_infodisplay_dimension")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            class_2960 dimensionId = class_310.method_1551().field_1687.method_27983().method_29177();
            if (dimensionId != null) {
               String dimensionName = dimensionId.method_12836().equals("minecraft") ? dimensionId.method_12832() : dimensionId.toString();
               compiler.addLine(dimensionName);
            }

         }
      }).setLegacyGetter((legacySettings) -> {
         Boolean result = legacySettings.showDimensionNameLegacy;
         legacySettings.showDimensionNameLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      DIMENSION = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("biome").setName(class_2561.method_43471("gui.xaero_infodisplay_biome")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            class_6880<class_1959> biomeHolder = class_310.method_1551().field_1687.method_23753(playerPos);
            class_1959 biome = biomeHolder == null ? null : (class_1959)biomeHolder.comp_349();
            class_2960 biomeRL = biome == null ? null : class_310.method_1551().field_1687.method_30349().method_30530(class_7924.field_41236).method_10221(biome);
            String biomeText = biomeRL == null ? class_1074.method_4662("gui.xaero_unknown_biome", new Object[0]) : class_1074.method_4662("biome." + biomeRL.method_12836() + "." + biomeRL.method_12832(), new Object[0]);
            compiler.addWords(biomeText);
         }
      }).setLegacyGetter((legacySettings) -> {
         Boolean result = legacySettings.showBiomeLegacy;
         legacySettings.showBiomeLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      BIOME = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("weather").setName(class_2561.method_43471("gui.xaero_infodisplay_weather")).setDefaultState(false).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.OFF_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            if (class_310.method_1551().field_1687.method_8546()) {
               compiler.addLine((class_2561)class_2561.method_43471("gui.xaero_weather_thundering"));
            } else if (class_310.method_1551().field_1687.method_8419()) {
               compiler.addLine((class_2561)class_2561.method_43471("gui.xaero_weather_raining"));
            }

         }
      }).setLegacyGetter((legacySettings) -> {
         Boolean result = legacySettings.displayWeatherInfoLegacy;
         legacySettings.displayWeatherInfoLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      WEATHER = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("light_level").setName(class_2561.method_43471("gui.xaero_infodisplay_light_level")).setDefaultState(0).setCodec(BuiltInConfigValueIOCodecs.INTEGER).setWidgetFactory(new InfoDisplayCycleWidgetFactory(Lists.newArrayList(new Integer[]{0, 1, 2, 3, 4}), Lists.newArrayList(new class_2561[]{class_2561.method_43471("gui.xaero_off"), class_2561.method_43471("gui.xaero_light_block"), class_2561.method_43471("gui.xaero_light_sky"), class_2561.method_43471("gui.xaero_light_all"), class_2561.method_43471("gui.xaero_light_both2")}))).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         int showLightLevel = (Integer)displayInfo.getEffectiveState();
         if (showLightLevel != 0) {
            class_310 mc = class_310.method_1551();
            int blockLight = mc.field_1687.method_8314(class_1944.field_9282, playerPos);
            int skyLight = mc.field_1687.method_8314(class_1944.field_9284, playerPos);
            if (showLightLevel == 1) {
               compiler.addLine((class_2561)class_2561.method_43469("gui.xaero_block_light_value", new Object[]{blockLight}));
            } else if (showLightLevel == 2) {
               compiler.addLine((class_2561)class_2561.method_43469("gui.xaero_sky_light_value", new Object[]{skyLight}));
            } else if (showLightLevel == 3) {
               compiler.addLine((class_2561)class_2561.method_43469("gui.xaero_all_light_value", new Object[]{Math.max(blockLight, skyLight)}));
            } else {
               compiler.addLine((class_2561)class_2561.method_43469("gui.xaero_both_light_value", new Object[]{blockLight, skyLight}));
            }

         }
      }).setLegacyGetter((legacySettings) -> {
         Integer result = legacySettings.showLightLevelLegacy;
         legacySettings.showLightLevelLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      LIGHT_LEVEL = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("time").setName(class_2561.method_43471("gui.xaero_infodisplay_time")).setDefaultState(0).setCodec(BuiltInConfigValueIOCodecs.INTEGER).setWidgetFactory(new InfoDisplayCycleWidgetFactory(Lists.newArrayList(new Integer[]{0, 1, 2, 3, 4}), Lists.newArrayList(new class_2561[]{class_2561.method_43471("gui.xaero_off"), class_2561.method_43469("%s+%s", new Object[]{class_2561.method_43471("gui.xaero_day"), class_2561.method_43471("gui.xaero_24h")}), class_2561.method_43469("%s+%s", new Object[]{class_2561.method_43471("gui.xaero_day"), class_2561.method_43471("gui.xaero_12h")}), class_2561.method_43471("gui.xaero_24h"), class_2561.method_43471("gui.xaero_12h")}))).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         int showTime = (Integer)displayInfo.getEffectiveState();
         if (showTime > 0) {
            class_310 mc = class_310.method_1551();
            long totalTime = 6000L + mc.field_1687.method_8532();
            int dayNumber = (int)(totalTime / 24000L) + 1;
            int dayTimeSinceMidnight = (int)(totalTime % 24000L);
            int timeHours = dayTimeSinceMidnight / 1000;
            int minutes = (int)((double)(dayTimeSinceMidnight % 1000) / (double)1000.0F * (double)60.0F);
            if ((showTime - 1) % 2 == 0) {
               if (showTime < 3) {
                  String var10001 = class_1074.method_4662("gui.xaero_day", new Object[0]);
                  compiler.addWords(var10001 + String.format(" %d, %02d:%02d", dayNumber, timeHours, minutes));
               } else {
                  compiler.addWords(String.format("%02d:%02d", timeHours, minutes));
               }
            } else {
               String half = "AM";
               if (timeHours >= 12) {
                  timeHours -= 12;
                  half = "PM";
               }

               if (timeHours == 0) {
                  timeHours = 12;
               }

               if (showTime < 3) {
                  String var14 = class_1074.method_4662("gui.xaero_day", new Object[0]);
                  compiler.addWords(var14 + String.format(" %d, %02d:%02d %s", dayNumber, timeHours, minutes, half));
               } else {
                  compiler.addWords(String.format("%02d:%02d %s", timeHours, minutes, half));
               }
            }

         }
      }).setLegacyGetter((legacySettings) -> {
         Integer result = legacySettings.showTimeLegacy;
         legacySettings.showTimeLegacy = null;
         return result;
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      TIME = var10000.setDestination(var10001::add).build();
      CALENDAR = Calendar.getInstance();
      var10000 = InfoDisplay.Builder.begin().setId("real_time").setName(class_2561.method_43471("gui.xaero_infodisplay_real_time")).setDefaultState(0).setCodec(BuiltInConfigValueIOCodecs.INTEGER).setWidgetFactory(new InfoDisplayCycleWidgetFactory(Lists.newArrayList(new Integer[]{0, 1, 2}), Lists.newArrayList(new class_2561[]{class_2561.method_43471("gui.xaero_off"), class_2561.method_43471("gui.xaero_24h"), class_2561.method_43471("gui.xaero_12h")}))).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         int showTime = (Integer)displayInfo.getEffectiveState();
         if (showTime > 0) {
            CALENDAR.setTimeInMillis(System.currentTimeMillis());
            int timeHours = CALENDAR.get(11);
            int minutes = CALENDAR.get(12);
            if (showTime == 1) {
               compiler.addWords(String.format("%02d:%02d IRL", timeHours, minutes));
            } else {
               String half = "AM";
               if (timeHours >= 12) {
                  timeHours -= 12;
                  half = "PM";
               }

               if (timeHours == 0) {
                  timeHours = 12;
               }

               compiler.addWords(String.format("%02d:%02d %s IRL", timeHours, minutes, half));
            }

         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      REAL_TIME = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("highlights").setName(class_2561.method_43471("gui.xaero_infodisplay_highlights")).setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.ALWAYS_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            if (session.getProcessor().getMinimapWriter().getDimensionHighlightHandler() != null) {
               session.getProcessor().getMinimapWriter().getDimensionHighlightHandler().addBlockHighlightTooltips(compiler, playerPos.method_10263(), playerPos.method_10260(), availableWidth, true);
            }

         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      HIGHLIGHTS = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("light_overlay_indicator").setName(class_2561.method_43471("gui.xaero_infodisplay_light_overlay_indicator")).setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.ALWAYS_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            if ((Integer)session.getModMain().getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.LIGHT_OVERLAY_TYPE) > 0) {
               compiler.addWords(class_1074.method_4662("gui.xaero_light_overlay_status", new Object[0]));
            }

         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      LIGHT_OVERLAY_INDICATOR = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("manual_cave_mode_indicator").setName(class_2561.method_43471("gui.xaero_infodisplay_manual_cave_mode_indicator")).setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.ALWAYS_ON).setCompiler((displayInfo, compiler, session, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            if (session.getProcessor().isManualCaveMode()) {
               compiler.addWords(class_1074.method_4662("gui.xaero_manual_cave_mode", new Object[0]));
            }

         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      MANUAL_CAVE_MODE_INDICATOR = var10000.setDestination(var10001::add).build();
      var10000 = InfoDisplay.Builder.begin().setId("custom_sub_world").setName(class_2561.method_43471("gui.xaero_infodisplay_custom_sub_world")).setDefaultState(true).setCodec(BuiltInConfigValueIOCodecs.BOOLEAN).setWidgetFactory(InfoDisplayCommonWidgetFactories.ALWAYS_ON).setCompiler((displayInfo, compiler, hudSession, availableWidth, playerPos) -> {
         if ((Boolean)displayInfo.getEffectiveState()) {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            MinimapWorldManager minimapWorldManager = session.getWorldManager();
            MinimapWorld currentWorld = minimapWorldManager.getCurrentWorld();
            if (currentWorld != null && minimapWorldManager.getAutoWorld() != currentWorld) {
               compiler.addWords(class_1074.method_4662("gui.xaero_using_custom_subworld", new Object[]{currentWorld.getContainer().getSubName()}));
            }

         }
      });
      var10001 = ALL;
      Objects.requireNonNull(var10001);
      CUSTOM_SUB_WORLD = var10000.setDestination(var10001::add).build();
   }
}
