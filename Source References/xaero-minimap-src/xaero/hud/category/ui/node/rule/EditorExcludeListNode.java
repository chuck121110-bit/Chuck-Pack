package xaero.hud.category.ui.node.rule;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.rule.ExcludeListMode;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.EditorSimpleDeletableWrapperNode;
import xaero.hud.category.ui.node.options.EditorOptionNode;
import xaero.hud.category.ui.node.options.list.EditorCompactListOptionsNode;
import xaero.hud.category.ui.node.options.text.EditorTextFieldOptionsNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorExcludeListNode extends EditorListNode {
   private EditorCompactListOptionsNode<ExcludeListMode> excludeMode;

   private EditorExcludeListNode(@Nonnull List<EditorSimpleDeletableWrapperNode<String>> list, ListFactory listFactory, @Nonnull EditorCompactListOptionsNode<ExcludeListMode> excludeMode, @Nonnull EditorTextFieldOptionsNode topAdder, @Nonnull EditorTextFieldOptionsNode bottomAdder, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, @Nonnull EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback, @Nonnull IEditorDataTooltipSupplier helpTooltipSupplier) {
      super(list, listFactory, topAdder, bottomAdder, movable, listEntryFactory, tooltipSupplier, deletionCallback, helpTooltipSupplier);
      this.excludeMode = excludeMode;
   }

   public ExcludeListMode getExcludeMode() {
      return (ExcludeListMode)this.excludeMode.getCurrentValue().getValue();
   }

   public class_2561 getDisplayName() {
      return class_2561.method_43471("gui.xaero_category_exclude_list");
   }

   public List<EditorNode> getSubNodes() {
      List<EditorNode> result = super.getSubNodes();
      result.add(0, this.excludeMode);
      return result;
   }

   public static final class Builder<E, P> extends EditorListNode.Builder<E, P, EditorExcludeListNode, Builder<E, P>> {
      private final EditorCompactListOptionsNode.Builder<ExcludeListMode> excludeModeBuilder;

      private Builder(ListFactory listFactory) {
         super(listFactory);
         this.excludeModeBuilder = EditorCompactListOptionsNode.Builder.<ExcludeListMode>begin(listFactory);
      }

      public Builder<E, P> setDefault() {
         this.excludeModeBuilder.setDefault().setDisplayName(class_2561.method_43471("gui.xaero_category_exclude_list_mode"));

         for(ExcludeListMode mode : ExcludeListMode.values()) {
            this.excludeModeBuilder.addOptionBuilder(EditorOptionNode.Builder.begin().setValue(mode));
         }

         this.setExcludeMode(ExcludeListMode.ONLY);
         return (Builder)super.setDefault();
      }

      public Builder<E, P> setExcludeMode(ExcludeListMode excludeMode) {
         this.excludeModeBuilder.setCurrentValue(excludeMode);
         return this;
      }

      public EditorExcludeListNode build() {
         return (EditorExcludeListNode)super.build();
      }

      protected EditorExcludeListNode buildInternally() {
         return new EditorExcludeListNode(this.buildList(), this.listFactory, this.excludeModeBuilder.build(), this.adderBuilder.build(), this.adderBuilder.build(), this.movable, this.listEntryFactory, this.tooltipSupplier, this.deletionCallback, this.helpTooltipSupplier);
      }

      public static <E, P> Builder<E, P> begin(ListFactory listFactory) {
         return (new Builder(listFactory)).setDefault();
      }
   }
}
