package xaero.hud.category.ui.node.options;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.EditorListEntryExpandingOptions;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.entry.widget.EditorButton;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public class EditorExpandingOptionsNode<V> extends EditorOptionsNode<V> {
   protected final List<EditorOptionNode<V>> options;

   protected EditorExpandingOptionsNode(@Nonnull class_2561 displayName, @Nonnull EditorOptionNode<V> currentValue, @Nonnull List<EditorOptionNode<V>> options, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier) {
      super(displayName, movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
      this.options = options;
      this.currentValue = currentValue;
   }

   public boolean onSelected(EditorOptionNode<V> option) {
      this.setCurrentValue(option);
      this.setExpanded(false);
      return true;
   }

   public List<EditorNode> getSubNodes() {
      return this.options;
   }

   public class_2561 getDisplayName() {
      return class_2561.method_43470("");
   }

   public abstract static class Builder<V, B extends Builder<V, B>> extends EditorOptionsNode.Builder<V, B> {
      protected final List<EditorOptionNode.Builder<V>> optionBuilders;
      protected final ListFactory listFactory;

      protected Builder(ListFactory listFactory) {
         this.optionBuilders = listFactory.<EditorOptionNode.Builder<V>>get();
         this.listFactory = listFactory;
      }

      public B setDefault() {
         super.setDefault();
         this.optionBuilders.clear();
         return this.self;
      }

      protected EditorListRootEntry.CenteredEntryFactory getCenteredEntryFactory(EditorNode data, EditorNode parent, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
         return (x, y, width, height, root) -> {
            EditorExpandingOptionsNode<?> eoData = (EditorExpandingOptionsNode)data;
            boolean isActive = eoData.getIsActiveSupplier().get(parent, eoData);
            EditorButton button = new EditorButton(parent, () -> class_2561.method_43470(""), isActive, 216, 20, (b) -> data.getExpandAction(rowList).run(), rowList);
            if (rowList.readOnly) {
               button.field_22763 = false;
            }

            return new EditorListEntryExpandingOptions(x, y, width, height, index, rowList, root, button, eoData.getMessageSupplier(), data.getTooltipSupplier(parent));
         };
      }

      public B addOptionBuilderFor(V option) {
         this.optionBuilders.add(EditorOptionNode.Builder.begin().setValue(option));
         return this.self;
      }

      public B addOptionBuilder(EditorOptionNode.Builder<V> optionBuilder) {
         this.optionBuilders.add(optionBuilder);
         return this.self;
      }

      public EditorExpandingOptionsNode<V> build() {
         if (this.listFactory == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            return (EditorExpandingOptionsNode)super.build();
         }
      }

      protected EditorOptionsNode<V> buildInternally() {
         Stream var10000 = this.optionBuilders.stream().map(EditorOptionNode.Builder::build);
         ListFactory var10001 = this.listFactory;
         Objects.requireNonNull(var10001);
         List<EditorOptionNode<V>> options = (List)var10000.collect(var10001::get, List::add, List::addAll);
         EditorOptionNode<V> currentValueData = null;

         for(EditorOptionNode<V> optionData : options) {
            if (optionData.getValue() == this.currentValue) {
               currentValueData = optionData;
               break;
            }
         }

         if (currentValueData == null) {
            throw new IllegalStateException("current value is not one of the options! " + String.valueOf(this.currentValue));
         } else {
            return this.buildInternally(currentValueData, options);
         }
      }

      protected abstract EditorOptionsNode<V> buildInternally(EditorOptionNode<V> var1, List<EditorOptionNode<V>> var2);
   }

   public static final class FinalBuilder<V> extends Builder<V, FinalBuilder<V>> {
      private FinalBuilder(ListFactory listFactory) {
         super(listFactory);
      }

      public static <V> FinalBuilder<V> begin(ListFactory listFactory) {
         return (FinalBuilder)(new FinalBuilder(listFactory)).setDefault();
      }

      protected EditorOptionsNode<V> buildInternally(EditorOptionNode<V> currentValueData, List<EditorOptionNode<V>> options) {
         return new EditorExpandingOptionsNode<V>(this.displayName, currentValueData, options, this.movable, this.listEntryFactory, this.tooltipSupplier, this.isActiveSupplier);
      }
   }
}
