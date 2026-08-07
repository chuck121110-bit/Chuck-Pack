package xaero.hud.category.ui.node.options;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.EditorListEntryExpandingOptions;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.entry.widget.EditorButton;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorSimpleButtonNode extends EditorNode {
   protected final class_2561 displayName;
   private ISimpleButtonCallback callback;
   private EditorButton.PressActionWithContext pressAction;
   private ISimpleButtonMessageSupplier messageSupplier;
   private final ISimpleButtonIsActiveSupplier isActiveSupplier;

   private EditorSimpleButtonNode(@Nonnull class_2561 displayName, @Nonnull IEditorDataTooltipSupplier tooltipSupplier, boolean movable, ISimpleButtonCallback callback, @Nonnull EditorListRootEntryFactory listEntryFactory, ISimpleButtonMessageSupplier messageSupplier, ISimpleButtonIsActiveSupplier isActiveSupplier) {
      super(movable, listEntryFactory, tooltipSupplier);
      this.displayName = displayName;
      this.callback = callback;
      this.messageSupplier = messageSupplier;
      this.isActiveSupplier = isActiveSupplier;
   }

   public Supplier<class_2561> getMessageSupplier(EditorNode parent, EditorSimpleButtonNode data) {
      return this.messageSupplier.get(parent, data);
   }

   public boolean getIsActiveSupplier(EditorNode parent, EditorSimpleButtonNode data, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
      return this.isActiveSupplier.get(parent, data, rowList);
   }

   public class_2561 getDisplayName() {
      return this.displayName;
   }

   public EditorButton.PressActionWithContext getPressAction() {
      if (this.pressAction == null) {
         this.pressAction = new EditorButton.PressActionWithContext() {
            public void onPress(EditorButton button, EditorNode parent, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
               if (EditorSimpleButtonNode.this.callback != null) {
                  EditorSimpleButtonNode.this.callback.onButtonPress(parent, EditorSimpleButtonNode.this, rowList);
               }

            }
         };
      }

      return this.pressAction;
   }

   public List<EditorNode> getSubNodes() {
      return null;
   }

   public static final class Builder extends EditorNode.Builder<Builder> {
      private class_2561 displayName;
      private ISimpleButtonCallback callback;
      private ISimpleButtonMessageSupplier messageSupplier;
      private ISimpleButtonIsActiveSupplier isActiveSupplier;

      private Builder() {
      }

      public Builder setDefault() {
         super.setDefault();
         this.setDisplayName((class_2561)null);
         this.setCallback((ISimpleButtonCallback)null);
         this.setMessageSupplier((parent, node) -> {
            Objects.requireNonNull(node);
            return node::getDisplayName;
         });
         this.setIsActiveSupplier((p, d, rowList) -> true);
         return this;
      }

      protected EditorListRootEntry.CenteredEntryFactory getCenteredEntryFactory(EditorNode node, EditorNode parent, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
         EditorSimpleButtonNode buttonNode = (EditorSimpleButtonNode)node;
         Supplier<class_2561> messageSupplier = buttonNode.getMessageSupplier(parent, buttonNode);
         return (x, y, width, height, root) -> {
            boolean isActive = buttonNode.getIsActiveSupplier(parent, buttonNode, rowList);
            EditorButton widget = new EditorButton(parent, messageSupplier, isActive, 216, 20, buttonNode.getPressAction(), rowList);
            return new EditorListEntryExpandingOptions(x, y, width, height, index, rowList, root, widget, messageSupplier, node.getTooltipSupplier(parent));
         };
      }

      public Builder setDisplayName(class_2561 displayName) {
         this.displayName = displayName;
         return this;
      }

      public Builder setCallback(ISimpleButtonCallback callback) {
         this.callback = callback;
         return this;
      }

      public Builder setMessageSupplier(ISimpleButtonMessageSupplier messageSupplier) {
         this.messageSupplier = messageSupplier;
         return this;
      }

      public Builder setIsActiveSupplier(ISimpleButtonIsActiveSupplier isActiveSupplier) {
         this.isActiveSupplier = isActiveSupplier;
         return this;
      }

      public EditorSimpleButtonNode build() {
         if (this.displayName != null && this.callback != null) {
            EditorSimpleButtonNode result = (EditorSimpleButtonNode)super.build();
            return result;
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      protected EditorNode buildInternally() {
         return new EditorSimpleButtonNode(this.displayName, this.tooltipSupplier, this.movable, this.callback, this.listEntryFactory, this.messageSupplier, this.isActiveSupplier);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }

   @FunctionalInterface
   public interface ISimpleButtonCallback {
      void onButtonPress(EditorNode var1, EditorSimpleButtonNode var2, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList var3);
   }

   @FunctionalInterface
   public interface ISimpleButtonIsActiveSupplier {
      boolean get(EditorNode var1, EditorSimpleButtonNode var2, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList var3);
   }

   @FunctionalInterface
   public interface ISimpleButtonMessageSupplier {
      Supplier<class_2561> get(EditorNode var1, EditorSimpleButtonNode var2);
   }
}
