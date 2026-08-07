package xaero.common.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xaero.common.HudMod;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.util.KeySortableByOther;

public class GuiWaypointContainers extends GuiDropdownHelper<String> {
   public GuiWaypointContainers(HudMod modMain, MinimapWorldManager manager, XaeroPath currentContainer, XaeroPath autoWorldPath) {
      List<KeySortableByOther<String>> sortableKeyList = new ArrayList();
      ClientConfigManager configManager = modMain.getHudConfigs().getClientConfigManager();
      int hideWorldNamesConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.HIDE_WORLD_NAMES);

      for(MinimapWorldRootContainer rootContainer : manager.getRootContainers()) {
         String rootContainerNode = rootContainer.getPath().getLastNode();
         String[] details = rootContainerNode.split("_");
         String sortName;
         if (details.length > 1 && details[0].equals("Realms")) {
            String var10000 = details[1];
            int var10001 = details[1].indexOf(".");
            sortName = "Realm ID " + var10000.substring(var10001 + 1);
         } else {
            sortName = details[details.length - 1].replace("%us%", "_").replace("%fs%", "/").replace("%bs%", "\\").replace("§", ":").replace("%lb%", "[").replace("%rb%", "]").replace(",", ".");
         }

         if (hideWorldNamesConfig == 1 && details.length > 1 && details[0].equals("Multiplayer")) {
            String[] dotSplit = sortName.split("(\\.|:+)");
            StringBuilder builder = new StringBuilder();

            for(int o = 0; o < dotSplit.length; ++o) {
               if (o < dotSplit.length - 2) {
                  builder.append("-.");
               } else if (o < dotSplit.length - 1) {
                  builder.append(dotSplit[o].isEmpty() ? "" : dotSplit[o].charAt(0)).append("-.");
               } else {
                  builder.append(dotSplit[o]);
               }
            }

            sortName = builder.toString();
         }

         sortableKeyList.add(new KeySortableByOther(rootContainerNode, new Comparable[]{rootContainerNode.startsWith("Multiplayer_") ? 1 : (rootContainerNode.startsWith("Realms_") ? 2 : 0), sortName.toLowerCase(), sortName}));
      }

      Collections.sort(sortableKeyList);
      this.current = -1;
      this.auto = -1;
      List<String> keyList = new ArrayList();
      List<String> optionList = new ArrayList();
      String currentRoot = currentContainer == null ? null : currentContainer.getLastNode();
      String autoRoot = autoWorldPath == null ? null : autoWorldPath.getRoot().getLastNode();

      for(int i = 0; i < sortableKeyList.size(); ++i) {
         KeySortableByOther<String> k = (KeySortableByOther)sortableKeyList.get(i);
         String containerKey = (String)k.getKey();
         if (this.current == -1 && containerKey.equals(currentRoot)) {
            this.current = i;
         }

         String option = (String)k.getDataToSortBy()[2];
         if (hideWorldNamesConfig == 2 || option.isEmpty()) {
            option = "hidden " + optionList.size();
         }

         if (this.auto == -1 && containerKey.equals(autoRoot)) {
            this.auto = i;
            option = option + " (auto)";
         }

         keyList.add(containerKey);
         optionList.add(option);
      }

      this.keys = (T[])keyList.toArray(new String[0]);
      this.options = (String[])optionList.toArray(new String[0]);
   }
}
