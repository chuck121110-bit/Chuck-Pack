package xaero.hud.category.ui.node.options.list;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.options.EditorCompactOptionsNode;
import xaero.hud.category.ui.node.options.EditorOptionNode;
import xaero.hud.category.ui.node.options.EditorOptionsNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorCompactListOptionsNode<V> extends EditorCompactOptionsNode<V> {
   private IntFunction<EditorOptionNode<V>> indexReader;
   private List<EditorOptionNode<V>> options;

   protected EditorCompactListOptionsNode(class_2561 displayName, @Nonnull EditorOptionNode<V> currentValue, @Nonnull List<EditorOptionNode<V>> options, boolean movable, EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier) {
      super(displayName, options.indexOf(currentValue), options.size(), movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
      this.currentValue = currentValue;
      this.options = options;
   }

   protected IntFunction<EditorOptionNode<V>> getIndexReader() {
      if (this.indexReader == null) {
         List var10001 = this.options;
         Objects.requireNonNull(var10001);
         this.indexReader = var10001::get;
      }

      return this.indexReader;
   }

   public static final class Builder<V> extends EditorCompactOptionsNode.Builder<V, Builder<V>> {
      protected final List<EditorOptionNode.Builder<V>> optionBuilders;
      protected final ListFactory listFactory;

      private Builder(ListFactory listFactory) {
         this.optionBuilders = listFactory.<EditorOptionNode.Builder<V>>get();
         this.listFactory = listFactory;
      }

      public Builder<V> setDefault() {
         this.optionBuilders.clear();
         return (Builder)super.setDefault();
      }

      public Builder<V> addOptionBuilder(EditorOptionNode.Builder<V> optionBuilder) {
         this.optionBuilders.add(optionBuilder);
         return this;
      }

      public EditorCompactListOptionsNode<V> build() {
         if (this.listFactory == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            return (EditorCompactListOptionsNode)super.build();
         }
      }

      protected EditorCompactListOptionsNode<V> buildInternally() {
         Stream var10000 = this.optionBuilders.stream().map(EditorOptionNode.Builder::build);
         ListFactory var10001 = this.listFactory;
         Objects.requireNonNull(var10001);
         List<EditorOptionNode<V>> options = (List)var10000.collect(var10001::get, List::add, List::addAll);
         EditorOptionNode<V> currentValueNode = null;

         for(EditorOptionNode<V> optionData : options) {
            if (optionData.getValue() == this.currentValue) {
               currentValueNode = optionData;
               break;
            }
         }

         if (currentValueNode == null) {
            throw new IllegalStateException("current value is not one of the options! " + String.valueOf(this.currentValue));
         } else {
            return new EditorCompactListOptionsNode<V>(this.displayName, currentValueNode, options, this.movable, this.listEntryFactory, this.tooltipSupplier, this.isActiveSupplier);
         }
      }

      public static <V> Builder<V> begin(ListFactory listFactory) {
         return (new Builder(listFactory)).setDefault();
      }
   }
}
