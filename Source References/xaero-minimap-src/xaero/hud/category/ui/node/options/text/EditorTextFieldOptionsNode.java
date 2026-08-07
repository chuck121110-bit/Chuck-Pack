package xaero.hud.category.ui.node.options.text;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import xaero.common.misc.ListFactory;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.EditorListEntryExpandingOptions;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.entry.widget.EditorTextField;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.options.EditorExpandingOptionsNode;
import xaero.hud.category.ui.node.options.EditorOptionNode;
import xaero.hud.category.ui.node.options.EditorOptionsNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorTextFieldOptionsNode extends EditorExpandingOptionsNode<String> {
   private String input;
   private String result;
   private int cursorPos;
   private int highlightPos;
   private final int maxLength;
   private EditorTextField.UpdatedValueConsumer updatedValueConsumer;
   private List<EditorOptionNode<String>> suggestions;
   private final TextFieldSuggestionsResolver suggestionsResolver;
   private final boolean allowCustomInput;
   private final boolean autoConfirm;
   private final Predicate<String> inputStringValidator;

   protected EditorTextFieldOptionsNode(@Nonnull class_2561 displayName, @Nonnull String input, int maxLength, @Nonnull EditorOptionNode<String> currentValue, @Nonnull List<EditorOptionNode<String>> options, @Nonnull TextFieldSuggestionsResolver suggestionsResolver, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, boolean allowCustomInput, boolean autoConfirm, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier, Predicate<String> inputStringValidator) {
      super(displayName, currentValue, options, movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
      this.maxLength = maxLength;
      this.resetInput(input);
      this.suggestionsResolver = suggestionsResolver;
      this.allowCustomInput = allowCustomInput;
      this.autoConfirm = autoConfirm;
      this.inputStringValidator = inputStringValidator;
   }

   public void resetInput(String input) {
      this.input = input;
      this.result = input;
      this.cursorPos = this.highlightPos = input.length();
   }

   public void setCurrentValue(EditorOptionNode<String> currentValue) {
   }

   public EditorOptionNode<String> getCurrentValue() {
      return EditorOptionNode.Builder.begin().setValue(this.input).build();
   }

   public String getInput() {
      return this.input;
   }

   public String getResult() {
      return this.result;
   }

   public int getCursorPos() {
      return this.cursorPos;
   }

   public int getHighlightPos() {
      return this.highlightPos;
   }

   public int getMaxLength() {
      return this.maxLength;
   }

   public class_2561 getDisplayName() {
      return this.displayName;
   }

   public void setExpanded(boolean expanded) {
      if (!expanded) {
         this.resetInput(this.result);
         this.suggestions = null;
      }

      super.setExpanded(expanded);
   }

   public boolean onSelected(EditorOptionNode<String> option) {
      boolean result = super.onSelected(option);
      this.resetInput(((String)option.getValue()).toString());
      return result;
   }

   public EditorTextField.UpdatedValueConsumer getUpdatedValueConsumer() {
      if (this.updatedValueConsumer == null) {
         this.updatedValueConsumer = (s, c, h, rl) -> {
            this.cursorPos = c;
            this.highlightPos = h;
            String oldInput = this.input;
            if (oldInput == null || !oldInput.equals(s)) {
               this.input = s;
               this.suggestions = this.suggestionsResolver.getSuggestions(s, this.options);
               if (this.autoConfirm) {
                  this.result = s;
               }

               if (!this.autoConfirm && this.allowCustomInput && !s.isEmpty() && this.inputStringValidator.test(s)) {
                  this.suggestions.add(0, EditorOptionNode.Builder.begin().setValue(s).setDisplayName(class_2561.method_43469("gui.xaero_category_add_to_list_custom", new Object[]{s})).build());
               }

               if (!this.suggestions.isEmpty()) {
                  this.setExpanded(true);
               } else if (s.isEmpty()) {
                  this.setExpanded(false);
               }

               if (this.autoConfirm) {
                  rl.restoreScrollAfterUpdate();
               }

               rl.setLastExpandedData(this);
               rl.updateEntries();
            }
         };
      }

      return this.updatedValueConsumer;
   }

   public List<EditorNode> getSubNodes() {
      return this.suggestions;
   }

   public static final class Builder extends EditorExpandingOptionsNode.Builder<String, Builder> {
      private String input;
      private int maxLength;
      private final EditorOptionNode.Builder<String> currentInputOption = EditorOptionNode.Builder.<String>begin();
      private final TextFieldSuggestionsResolver.Builder suggestionsResolverBuilder;
      private boolean allowCustomInput;
      private boolean autoConfirm;
      private Predicate<String> inputStringValidator;

      private Builder(ListFactory listFactory) {
         super(listFactory);
         this.suggestionsResolverBuilder = TextFieldSuggestionsResolver.Builder.begin(listFactory);
      }

      public Builder setDefault() {
         super.setDefault();
         this.setInput("");
         this.setMaxLength(100);
         this.currentInputOption.setDefault().setDisplayName(class_2561.method_43470("null holder"));
         this.addOptionBuilder(this.currentInputOption);
         this.setAllowCustomInput(true);
         this.setAutoConfirm(true);
         this.setInputStringValidator((Predicate)null);
         this.suggestionsResolverBuilder.setDefault();
         return this;
      }

      protected EditorListRootEntry.CenteredEntryFactory getCenteredEntryFactory(EditorNode data, EditorNode parent, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
         return (x, y, width, height, root) -> {
            EditorTextFieldOptionsNode tfoData = (EditorTextFieldOptionsNode)data;
            EditorTextField widget = new EditorTextField(tfoData.getUpdatedValueConsumer(), tfoData.getInput(), tfoData.getCursorPos(), tfoData.getHighlightPos(), tfoData.getMaxLength(), class_310.method_1551().field_1772, 214, 18, data.getDisplayName(), tfoData.inputStringValidator, rowList);
            if (rowList.readOnly) {
               widget.field_22763 = false;
            }

            return new EditorListEntryExpandingOptions(x, y, width, height, index, rowList, root, widget, (Supplier)null, data.getTooltipSupplier(parent));
         };
      }

      public Builder setInput(String input) {
         this.input = input;
         return this;
      }

      public Builder setInputStringValidator(Predicate<String> inputStringValidator) {
         this.inputStringValidator = inputStringValidator;
         return this;
      }

      public boolean needsInputStringValidator() {
         return this.inputStringValidator == null;
      }

      public Builder setAllowCustomInput(boolean allowCustomInput) {
         this.allowCustomInput = allowCustomInput;
         return this;
      }

      public Builder setAutoConfirm(boolean autoConfirm) {
         this.autoConfirm = autoConfirm;
         return this;
      }

      public Builder setMaxLength(int maxLength) {
         this.maxLength = maxLength;
         return this;
      }

      public Builder setCurrentValue(String currentValue) {
         return this;
      }

      public EditorTextFieldOptionsNode build() {
         if (this.input != null && this.inputStringValidator != null) {
            return (EditorTextFieldOptionsNode)super.build();
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      protected EditorTextFieldOptionsNode buildInternally(EditorOptionNode<String> currentValueData, List<EditorOptionNode<String>> options) {
         return new EditorTextFieldOptionsNode(this.displayName, this.input, this.maxLength, currentValueData, options, this.suggestionsResolverBuilder.build(), this.movable, this.listEntryFactory, this.allowCustomInput, this.autoConfirm, this.tooltipSupplier, this.isActiveSupplier, this.inputStringValidator);
      }

      public static Builder begin(ListFactory listFactory) {
         return (new Builder(listFactory)).setDefault();
      }
   }
}
