package xaero.hud.category.ui.node.options;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import net.minecraft.class_5244;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public abstract class EditorOptionsNode<V> extends EditorNode {
   protected EditorOptionNode<V> currentValue;
   protected Supplier<class_2561> messageSupplier;
   protected final class_2561 displayName;
   private final IOptionsNodeIsActiveSupplier isActiveSupplier;

   protected EditorOptionsNode(@Nonnull class_2561 displayName, boolean movable, EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, IOptionsNodeIsActiveSupplier isActiveSupplier) {
      super(movable, listEntryFactory, tooltipSupplier);
      this.displayName = displayName;
      this.isActiveSupplier = isActiveSupplier;
   }

   public EditorOptionNode<V> getCurrentValue() {
      return this.currentValue;
   }

   public void setCurrentValue(EditorOptionNode<V> currentValue) {
      this.currentValue = currentValue;
   }

   public final Supplier<class_2561> getMessageSupplier() {
      if (this.messageSupplier == null) {
         this.messageSupplier = () -> (class_2561)(this.isExpanded() ? this.displayName : class_5244.method_32700(this.displayName, this.currentValue.getDisplayName()));
      }

      return this.messageSupplier;
   }

   public IOptionsNodeIsActiveSupplier getIsActiveSupplier() {
      return this.isActiveSupplier;
   }

   public class_2561 getDisplayName() {
      return this.displayName;
   }

   public abstract static class Builder<V, B extends Builder<V, B>> extends EditorNode.Builder<B> {
      protected B self = (B)this;
      protected V currentValue;
      protected class_2561 displayName;
      protected IOptionsNodeIsActiveSupplier isActiveSupplier;

      protected Builder() {
      }

      public B setDefault() {
         super.setDefault();
         this.setCurrentValue((Object)null);
         this.setDisplayName((class_2561)null);
         this.setIsActiveSupplier((p, d) -> true);
         return this.self;
      }

      public B setCurrentValue(V currentValue) {
         this.currentValue = currentValue;
         return this.self;
      }

      public B setDisplayName(class_2561 displayName) {
         this.displayName = displayName;
         return this.self;
      }

      public B setIsActiveSupplier(IOptionsNodeIsActiveSupplier isActiveSupplier) {
         this.isActiveSupplier = isActiveSupplier;
         return this.self;
      }

      public EditorOptionsNode<V> build() {
         if (this.displayName == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            EditorOptionsNode<V> result = (EditorOptionsNode)super.build();
            return result;
         }
      }

      protected abstract EditorOptionsNode<V> buildInternally();
   }

   @FunctionalInterface
   public interface IOptionsNodeIsActiveSupplier {
      boolean get(EditorNode var1, EditorOptionsNode<?> var2);
   }
}
