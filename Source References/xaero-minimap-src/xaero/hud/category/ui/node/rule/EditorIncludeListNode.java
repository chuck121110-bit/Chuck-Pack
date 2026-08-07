package xaero.hud.category.ui.node.rule;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.EditorSimpleDeletableWrapperNode;
import xaero.hud.category.ui.node.options.EditorCompactBooleanOptionsNode;
import xaero.hud.category.ui.node.options.text.EditorTextFieldOptionsNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorIncludeListNode extends EditorListNode {
   private final EditorCompactBooleanOptionsNode includeInSuperToggleData;

   private EditorIncludeListNode(@Nonnull List<EditorSimpleDeletableWrapperNode<String>> list, ListFactory listFactory, @Nonnull EditorTextFieldOptionsNode topAdder, @Nonnull EditorTextFieldOptionsNode bottomAdder, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, @Nonnull EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback, @Nonnull IEditorDataTooltipSupplier helpTooltipSupplier, EditorCompactBooleanOptionsNode includeInSuperToggleData) {
      super(list, listFactory, topAdder, bottomAdder, movable, listEntryFactory, tooltipSupplier, deletionCallback, helpTooltipSupplier);
      this.includeInSuperToggleData = includeInSuperToggleData;
   }

   public class_2561 getDisplayName() {
      return class_2561.method_43471("gui.xaero_category_include_list");
   }

   public List<EditorNode> getSubNodes() {
      List<EditorNode> result = super.getSubNodes();
      result.add(0, this.includeInSuperToggleData);
      return result;
   }

   public boolean getIncludeInSuper() {
      return (Boolean)this.includeInSuperToggleData.getCurrentValue().getValue();
   }

   public static final class Builder<E, P> extends EditorListNode.Builder<E, P, EditorIncludeListNode, Builder<E, P>> {
      private final EditorCompactBooleanOptionsNode.Builder includeInSuperToggleDataBuilder = EditorCompactBooleanOptionsNode.Builder.begin();

      private Builder(ListFactory listFactory) {
         super(listFactory);
      }

      public Builder<E, P> setDefault() {
         super.setDefault();
         this.includeInSuperToggleDataBuilder.setDefault().setDisplayName(class_2561.method_43471("gui.xaero_category_include_list_include_in_super"));
         return this;
      }

      public EditorCompactBooleanOptionsNode.Builder getIncludeInSuperToggleDataBuilder() {
         return this.includeInSuperToggleDataBuilder;
      }

      public EditorIncludeListNode build() {
         return (EditorIncludeListNode)super.build();
      }

      protected EditorIncludeListNode buildInternally() {
         return new EditorIncludeListNode(this.buildList(), this.listFactory, this.adderBuilder.build(), this.adderBuilder.build(), this.movable, this.listEntryFactory, this.tooltipSupplier, this.deletionCallback, this.helpTooltipSupplier, this.includeInSuperToggleDataBuilder.build());
      }

      public static <E, P> Builder<E, P> begin(ListFactory listFactory) {
         return (new Builder(listFactory)).setDefault();
      }
   }
}
