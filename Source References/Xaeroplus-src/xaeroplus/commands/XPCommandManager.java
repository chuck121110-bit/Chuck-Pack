package xaeroplus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_7157;
import xaero.common.HudMod;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.waypoint.WaypointAPI;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.Drawing;
import xaeroplus.module.impl.Pearls;
import xaeroplus.module.impl.SpawnPoint;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.settings.Settings;
import xaeroplus.util.AtlasWaypointImport;
import xaeroplus.util.DataFolderResolveUtil;

public class XPCommandManager {
   private XPCommandManager() {
   }

   static LiteralArgumentBuilder<XPClientCommandSource> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   static <T> RequiredArgumentBuilder<XPClientCommandSource, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   public static void registerCommands(CommandDispatcher<XPClientCommandSource> dispatcher, class_7157 context) {
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroDataDir").executes((c) -> {
         ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(DataFolderResolveUtil.getCurrentDataDirPath());
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroWaypointDir").executes((c) -> {
         ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(DataFolderResolveUtil.getCurrentWaypointDataDirPath());
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaero2b2tAtlasImport").executes((c) -> {
         ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("Atlas import started..."));
         AtlasWaypointImport.importAtlasWaypoints().whenCompleteAsync((addedCount, e) -> {
            if (e != null) {
               XaeroPlus.LOGGER.error("Atlas import failed", e);
               ((XPClientCommandSource)c.getSource()).xaeroplus$sendFailure(class_2561.method_43470("Atlas import failed! Check log for details."));
            } else {
               ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470(addedCount + " waypoints imported to the \"atlas\" waypoint set!"));
               boolean allSetsEnabled = (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.WAYPOINTS_ALL_SETS);
               boolean isAtlasSetActive = Optional.ofNullable(WaypointAPI.getCurrentWaypointSet()).map(WaypointSet::getName).filter((n) -> n.equals("atlas")).isPresent();
               if (!allSetsEnabled && !isAtlasSetActive) {
                  ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("To see the waypoints, enable rendering all waypoint sets or switch to the \"atlas\" set."));
               }
            }

            ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("Atlas Import Complete!"));
         }, TickTaskExecutor.INSTANCE);
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroplus:clearDrawings").executes((c) -> {
         TickTaskExecutor.INSTANCE.submit((Runnable)(() -> {
            ((Drawing)ModuleManager.getModule(Drawing.class)).clearAll();
            ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("All Drawings cleared!"));
         }));
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroplus:resetDrawOrder").executes((c) -> {
         TickTaskExecutor.INSTANCE.submit((Runnable)(() -> {
            Settings.REGISTRY.drawOrderSetting.setValue("");
            ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("Draw order reset!"));
         }));
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroplus:clearSpawnPoints").executes((c) -> {
         TickTaskExecutor.INSTANCE.submit((Runnable)(() -> {
            ((SpawnPoint)ModuleManager.getModule(SpawnPoint.class)).getLoadedSpawnPositions().clear();
            ((SpawnPoint)ModuleManager.getModule(SpawnPoint.class)).saveRespawnPoints();
            ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("All spawn points cleared!"));
         }));
         return 1;
      }));
      dispatcher.register((LiteralArgumentBuilder)literal("xaeroplus:clearPearls").executes((c) -> {
         TickTaskExecutor.INSTANCE.submit((Runnable)(() -> {
            ((Pearls)ModuleManager.getModule(Pearls.class)).getLoadedPearls().clear();
            ((Pearls)ModuleManager.getModule(Pearls.class)).savePearls();
            ((XPClientCommandSource)c.getSource()).xaeroplus$sendSuccess(class_2561.method_43470("All pearls cleared!"));
         }));
         return 1;
      }));
   }
}
