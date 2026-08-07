package xaero.common.gui;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import xaero.common.IXaeroMinimap;
import xaero.hud.category.ui.EditorCategoryNodeConverter;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.radar.category.ui.EditorEntityRadarCategoryNodeConverter;
import xaero.hud.minimap.radar.category.ui.node.EditorEntityRadarCategoryNode;
import xaero.hud.minimap.radar.category.ui.node.EditorEntityRadarCategorySettingsNode;
import xaero.lib.client.gui.config.EditConfigScreen;

public class GuiEntityRadarCategoryEditor extends GuiCategoryEditor<EntityRadarCategory, EditorEntityRadarCategoryNode, EntityRadarCategory.Builder, EditorEntityRadarCategorySettingsNode<?>, EditorEntityRadarCategorySettingsNode.Builder, EditorEntityRadarCategoryNode.Builder> {
   public static final class_2561 SERVER_ENFORCED_COMPONENT;
   private final EntityRadarCategoryManager entityRadarCategoryManager;
   private final boolean clientSide;
   private final Runnable onChange;
   private final boolean viewingEnforced;

   public GuiEntityRadarCategoryEditor(IXaeroMinimap modMain, EditConfigScreen parent, class_437 escape, Runnable onChange, boolean viewingEnforced) {
      super(modMain, parent, escape, class_2561.method_43471("gui.xaero_entity_radar_categories"), EditorEntityRadarCategoryNodeConverter.Builder.begin().build(), viewingEnforced);
      this.entityRadarCategoryManager = modMain.getEntityRadarCategoryManager();
      this.clientSide = parent.getContext().isClientSide();
      this.onChange = onChange;
      this.viewingEnforced = viewingEnforced;
   }

   protected EditorEntityRadarCategoryNode constructEditorData(EditorCategoryNodeConverter<EntityRadarCategory, EditorEntityRadarCategoryNode, EntityRadarCategory.Builder, EditorEntityRadarCategorySettingsNode<?>, EditorEntityRadarCategorySettingsNode.Builder, EditorEntityRadarCategoryNode.Builder> dataConverter) {
      EntityRadarCategory editedCategory = this.readOnly ? this.modMain.getEntityRadarCategoryManager().getSyncedRootCategory() : this.modMain.getEntityRadarCategoryManager().getEditedCategory();
      return dataConverter.convert(editedCategory, !this.readOnly && ((EditConfigScreen)this.parent).getContext().isClientSide());
   }

   protected EditorEntityRadarCategoryNode constructDefaultData(EditorCategoryNodeConverter<EntityRadarCategory, EditorEntityRadarCategoryNode, EntityRadarCategory.Builder, EditorEntityRadarCategorySettingsNode<?>, EditorEntityRadarCategorySettingsNode.Builder, EditorEntityRadarCategoryNode.Builder> dataConverter) {
      this.modMain.getSettings().resetEntityRadarBackwardsCompatibilityConfig();
      EntityRadarCategory rootCategory = this.clientSide ? this.entityRadarCategoryManager.fetchDefaultClientCategory() : this.entityRadarCategoryManager.fetchDefaultServerCategory();
      return dataConverter.convert(rootCategory, this.clientSide);
   }

   protected void onConfigConfirmed(EntityRadarCategory confirmedRootCategory) {
      this.entityRadarCategoryManager.storeEditedCategory(confirmedRootCategory, this.clientSide);
      this.onChange.run();
   }

   public void method_25394(class_332 guiGraphics, int i, int j, float f) {
      super.method_25394(guiGraphics, i, j, f);
      if (this.viewingEnforced) {
         guiGraphics.method_27534(this.field_22793, SERVER_ENFORCED_COMPONENT, this.field_22789 / 2, 15, -1);
      }

   }

   static {
      SERVER_ENFORCED_COMPONENT = class_2561.method_43471("gui.xaero_entity_category_editor_server_enforced").method_27692(class_124.field_1054);
   }
}
