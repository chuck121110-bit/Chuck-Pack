package xaero.hud.category.ui.node.options;

import java.util.function.IntFunction;
import net.minecraft.class_2561;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public class EditorCompactBooleanOptionsNode extends EditorCompactOptionsNode<Boolean> {
   private final EditorOptionNode<Boolean> trueOption;
   private final EditorOptionNode<Boolean> falseOption;
   private IntFunction<EditorOptionNode<Boolean>> indexReader;

   protected EditorCompactBooleanOptionsNode(class_2561 displayName, int currentIndex, int optionCount, boolean movable, EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier, EditorOptionNode<Boolean> trueOption, EditorOptionNode<Boolean> falseOption) {
      super(displayName, currentIndex, optionCount, movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
      this.trueOption = trueOption;
      this.falseOption = falseOption;
      this.currentValue = (EditorOptionNode)this.getIndexReader().apply(currentIndex);
   }

   protected IntFunction<EditorOptionNode<Boolean>> getIndexReader() {
      if (this.indexReader == null) {
         this.indexReader = (i) -> i != 0 ? this.trueOption : this.falseOption;
      }

      return this.indexReader;
   }

   public static final class Builder extends EditorCompactOptionsNode.Builder<Boolean, Builder> {
      private final EditorOptionNode.Builder<Boolean> trueOptionBuilder = EditorOptionNode.Builder.<Boolean>begin();
      private final EditorOptionNode.Builder<Boolean> falseOptionBuilder = EditorOptionNode.Builder.<Boolean>begin();

      private Builder() {
      }

      public Builder setDefault() {
         super.setDefault();
         this.trueOptionBuilder.setDefault().setDisplayName(class_2561.method_43471("gui.xaero_on")).setValue(true);
         this.falseOptionBuilder.setDefault().setDisplayName(class_2561.method_43471("gui.xaero_off")).setValue(false);
         this.setCurrentValue(false);
         return this.self;
      }

      public EditorOptionNode.Builder<Boolean> getTrueOptionBuilder() {
         return this.trueOptionBuilder;
      }

      public EditorOptionNode.Builder<Boolean> getFalseOptionBuilder() {
         return this.falseOptionBuilder;
      }

      public EditorCompactBooleanOptionsNode build() {
         if (this.currentValue == null) {
            throw new IllegalStateException();
         } else if (this.movable) {
            throw new IllegalStateException("toggles can't be movable!");
         } else {
            return (EditorCompactBooleanOptionsNode)super.build();
         }
      }

      protected EditorCompactBooleanOptionsNode buildInternally() {
         EditorOptionNode<Boolean> trueOption = this.trueOptionBuilder.build();
         EditorOptionNode<Boolean> falseOption = this.falseOptionBuilder.build();
         return new EditorCompactBooleanOptionsNode(this.displayName, (Boolean)this.currentValue ? 1 : 0, 2, this.movable, this.listEntryFactory, this.tooltipSupplier, this.isActiveSupplier, trueOption, falseOption);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
