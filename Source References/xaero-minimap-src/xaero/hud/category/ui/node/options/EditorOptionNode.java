package xaero.hud.category.ui.node.options;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.EditorListEntryExpandingOption;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorOptionNode<V> extends EditorNode {
   private final V value;
   private final class_2561 displayName;

   public EditorOptionNode(V index, class_2561 displayName, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier) {
      super(movable, listEntryFactory, tooltipSupplier);
      this.value = index;
      this.displayName = displayName;
   }

   public V getValue() {
      return this.value;
   }

   public class_2561 getDisplayName() {
      return this.displayName;
   }

   public List<EditorNode> getSubNodes() {
      return null;
   }

   public static final class Builder<V> extends EditorNode.Builder<Builder<V>> {
      private V value;
      private class_2561 displayName;

      private Builder() {
      }

      public Builder<V> setDefault() {
         super.setDefault();
         this.setValue((Object)null);
         this.setDisplayName((class_2561)null);
         return this;
      }

      protected EditorListRootEntry.CenteredEntryFactory getCenteredEntryFactory(EditorNode data, EditorNode parent, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
         return (x, y, width, height, root) -> {
            EditorExpandingOptionsNode<V> optionsData = (EditorExpandingOptionsNode)parent;
            return new EditorListEntryExpandingOption(x, y, width, height, index, rowList, optionsData, root, data.getTooltipSupplier(parent));
         };
      }

      public Builder<V> setValue(V value) {
         this.value = value;
         return this;
      }

      public Builder<V> setDisplayName(class_2561 displayName) {
         this.displayName = displayName;
         return this;
      }

      public EditorOptionNode<V> build() {
         if (this.displayName == null) {
            this.displayName = class_2561.method_43470(this.value == null ? "N/A" : this.value.toString());
         }

         EditorOptionNode<V> result = (EditorOptionNode)super.build();
         return result;
      }

      public static <V> Builder<V> begin() {
         return (new Builder()).setDefault();
      }

      protected EditorOptionNode<V> buildInternally() {
         return new EditorOptionNode<V>(this.value, this.displayName, this.movable, this.listEntryFactory, this.tooltipSupplier);
      }
   }
}
