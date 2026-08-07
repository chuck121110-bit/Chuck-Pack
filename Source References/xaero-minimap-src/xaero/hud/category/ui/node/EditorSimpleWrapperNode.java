package xaero.hud.category.ui.node;

import com.google.common.base.Objects;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public class EditorSimpleWrapperNode<S extends Comparable<S>> extends EditorNode implements Comparable<EditorSimpleWrapperNode<S>> {
   private S element;

   protected EditorSimpleWrapperNode(@Nonnull S element, boolean movable, EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier) {
      super(movable, listEntryFactory, tooltipSupplier);
      this.element = element;
   }

   public S getElement() {
      return this.element;
   }

   public void setElement(S element) {
      this.element = element;
   }

   public List<EditorNode> getSubNodes() {
      return null;
   }

   public class_2561 getDisplayName() {
      return class_2561.method_43470(this.element.toString());
   }

   public boolean equals(Object obj) {
      if (obj != null && obj instanceof EditorSimpleWrapperNode<?> otherWrapper) {
         return Objects.equal(this.element, otherWrapper.element);
      } else {
         return false;
      }
   }

   public int compareTo(EditorSimpleWrapperNode<S> o) {
      if (this.element == o.element) {
         return 0;
      } else if (this.element == null) {
         return -1;
      } else {
         return o.element == null ? 1 : this.element.compareTo(o.element);
      }
   }

   public abstract static class Builder<S extends Comparable<S>, B extends Builder<S, B>> extends EditorNode.Builder<B> {
      protected S element;

      protected Builder() {
      }

      public B setDefault() {
         super.setDefault();
         this.setElement((Comparable)null);
         return this.self;
      }

      public B setElement(S element) {
         this.element = element;
         return this.self;
      }

      public EditorSimpleWrapperNode<S> build() {
         if (this.element == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            EditorSimpleWrapperNode<S> result = (EditorSimpleWrapperNode)super.build();
            return result;
         }
      }
   }

   public static final class FinalBuilder<S extends Comparable<S>> extends Builder<S, FinalBuilder<S>> {
      public static <S extends Comparable<S>> FinalBuilder<S> begin() {
         return (FinalBuilder)(new FinalBuilder()).setDefault();
      }

      protected EditorSimpleWrapperNode<S> buildInternally() {
         return new EditorSimpleWrapperNode<S>(this.element, this.movable, this.listEntryFactory, this.tooltipSupplier);
      }
   }
}
