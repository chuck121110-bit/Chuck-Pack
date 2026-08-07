package xaero.hud.category.ui.node.rule;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.ConnectionLineType;
import xaero.hud.category.ui.entry.EditorListEntryWidget;
import xaero.hud.category.ui.entry.EditorListEntryWrapper;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.entry.widget.EditorButton;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.EditorSimpleDeletableWrapperNode;
import xaero.hud.category.ui.node.options.text.EditorTextFieldOptionsNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.common.gui.widget.TooltipInfo;

abstract class EditorListNode extends EditorNode {
   protected final List<EditorSimpleDeletableWrapperNode<String>> list;
   private final EditorTextFieldOptionsNode topAdder;
   private final EditorTextFieldOptionsNode bottomAdder;
   private final ListFactory listFactory;
   private final EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback;
   private final IEditorDataTooltipSupplier helpTooltipSupplier;

   protected EditorListNode(@Nonnull List<EditorSimpleDeletableWrapperNode<String>> list, @Nonnull ListFactory listFactory, @Nonnull EditorTextFieldOptionsNode topAdder, @Nonnull EditorTextFieldOptionsNode bottomAdder, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, @Nonnull EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback, @Nonnull IEditorDataTooltipSupplier helpTooltipSupplier) {
      super(movable, listEntryFactory, tooltipSupplier);
      this.list = list;
      this.listFactory = listFactory;
      this.topAdder = topAdder;
      this.bottomAdder = bottomAdder;
      this.deletionCallback = deletionCallback;
      this.helpTooltipSupplier = helpTooltipSupplier;
   }

   public List<EditorSimpleDeletableWrapperNode<String>> getList() {
      return this.list;
   }

   public EditorSimpleDeletableWrapperNode.DeletionCallback getDeletionCallback() {
      return this.deletionCallback;
   }

   private Consumer<EditorTextFieldOptionsNode> getAdderHandler() {
      return (adder) -> {
         String adderRequest = adder.getResult();
         if (!adderRequest.isEmpty()) {
            EditorSimpleDeletableWrapperNode<String> element = ((EditorSimpleDeletableWrapperNode.Builder)EditorSimpleDeletableWrapperNode.Builder.begin().setElement(adderRequest)).setDeletionCallback(this.getDeletionCallback()).build();
            int sortedIndex = Collections.binarySearch(this.list, element);
            if (sortedIndex < 0) {
               sortedIndex = ~sortedIndex;
               this.list.add(sortedIndex, element);
            }

            adder.resetInput("");
         }
      };
   }

   public List<EditorNode> getSubNodes() {
      Consumer<EditorTextFieldOptionsNode> adderHandler = this.getAdderHandler();
      adderHandler.accept(this.topAdder);
      adderHandler.accept(this.bottomAdder);
      List<EditorNode> result = this.listFactory.<EditorNode>get();
      if (this.list.size() > 0) {
         result.add(this.topAdder);
      }

      result.addAll(this.list);
      result.add(this.bottomAdder);
      return result;
   }

   public abstract static class Builder<E, P, ED extends EditorListNode, B extends Builder<E, P, ED, B>> extends EditorNode.Builder<B> {
      private final B self = (B)this;
      protected final List<EditorSimpleDeletableWrapperNode.Builder<String>> list;
      protected final EditorTextFieldOptionsNode.Builder adderBuilder;
      protected ListFactory listFactory;
      protected EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback;
      private Predicate<String> inputRuleTypeStringValidator;
      protected IEditorDataTooltipSupplier helpTooltipSupplier;
      private ObjectCategoryListRuleType<E, P, ?> defaultListRuleType;
      private Iterable<ObjectCategoryListRuleType<E, P, ?>> listRuleTypes;
      private String listRuleTypePrefixSeparator;

      protected Builder(ListFactory listFactory) {
         this.list = listFactory.<EditorSimpleDeletableWrapperNode.Builder<String>>get();
         this.listFactory = listFactory;
         this.adderBuilder = EditorTextFieldOptionsNode.Builder.begin(listFactory);
      }

      public B setDefault() {
         super.setDefault();
         this.list.clear();
         this.setDeletionCallback((EditorSimpleDeletableWrapperNode.DeletionCallback)null);
         this.adderBuilder.setDefault().setAllowCustomInput(false).setAutoConfirm(false).setDisplayName(class_2561.method_43471("gui.xaero_category_list_add"));
         this.setDeletionCallback((parent, element, rowList) -> {
            EditorListNode listData = (EditorListNode)parent;
            if (listData.getList().remove(element)) {
               rowList.restoreScrollAfterUpdate();
               return true;
            } else {
               return false;
            }
         });
         this.setHelpTooltipSupplier((parent, data) -> null);
         this.setDefaultListRuleType((ObjectCategoryListRuleType)null);
         this.setListRuleTypes((Iterable)null);
         this.setListRuleTypePrefixSeparator((String)null);
         this.setInputRuleTypeStringValidator((Predicate)null);
         return this.self;
      }

      protected EditorListRootEntry mainEntryFactory(EditorNode data, EditorNode parent, int index, ConnectionLineType lineType, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, int screenWidth, boolean isFinalExpanded) {
         EditorListNode elData = (EditorListNode)data;
         return new EditorListEntryWrapper(this.getCenteredEntryFactory(data, parent, index, rowList), screenWidth, index, rowList, lineType, data, (Supplier)elData.helpTooltipSupplier.apply(parent, elData));
      }

      protected EditorListRootEntry.CenteredEntryFactory getCenteredEntryFactory(EditorNode data, EditorNode parent, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
         return (x, y, width, height, root) -> {
            EditorButton button = new EditorButton(parent, true, 216, 20, data, rowList);
            return new EditorListEntryWidget(x, y, width, height, index, rowList, root, button, data.getTooltipSupplier(parent));
         };
      }

      public B addListElement(String element) {
         this.list.add((EditorSimpleDeletableWrapperNode.Builder)EditorSimpleDeletableWrapperNode.Builder.begin().setElement(element));
         return this.self;
      }

      public B setDeletionCallback(EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback) {
         this.deletionCallback = deletionCallback;
         return this.self;
      }

      public EditorTextFieldOptionsNode.Builder getAdderBuilder() {
         return this.adderBuilder;
      }

      protected List<EditorSimpleDeletableWrapperNode<String>> buildList() {
         Stream var10000 = this.list.stream().map((builder) -> builder.setDeletionCallback(this.deletionCallback).build()).sorted();
         ListFactory var10001 = this.listFactory;
         Objects.requireNonNull(var10001);
         return (List)var10000.collect(var10001::get, List::add, List::addAll);
      }

      public B setInputRuleTypeStringValidator(Predicate<String> inputRuleTypeStringValidator) {
         this.inputRuleTypeStringValidator = inputRuleTypeStringValidator;
         return this.self;
      }

      public B setHelpTooltipSupplier(IEditorDataTooltipSupplier helpTooltipSupplier) {
         this.helpTooltipSupplier = helpTooltipSupplier;
         return this.self;
      }

      public B setHelpTooltipInfoSupplier(BiFunction<EditorNode, EditorNode, TooltipInfo> tooltipInfoSupplier) {
         return (B)this.setHelpTooltipSupplier((parent, data) -> new Tooltip((TooltipInfo)tooltipInfoSupplier.apply(parent, data)));
      }

      public B setDefaultListRuleType(ObjectCategoryListRuleType<E, P, ?> defaultListRuleType) {
         this.defaultListRuleType = defaultListRuleType;
         return this.self;
      }

      public B setListRuleTypes(Iterable<ObjectCategoryListRuleType<E, P, ?>> listRuleTypes) {
         this.listRuleTypes = listRuleTypes;
         return this.self;
      }

      public B setListRuleTypePrefixSeparator(String listRuleTypePrefixSeparator) {
         this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
         return this.self;
      }

      public EditorNode build() {
         if (this.deletionCallback != null && this.helpTooltipSupplier != null && this.defaultListRuleType != null && this.listRuleTypes != null && this.listRuleTypePrefixSeparator != null) {
            String listRuleTypePrefixSeparator = this.listRuleTypePrefixSeparator;
            Predicate<String> inputRuleTypeStringValidator = this.inputRuleTypeStringValidator;
            Iterable<ObjectCategoryListRuleType<E, P, ?>> listRuleTypes = this.listRuleTypes;
            Predicate<String> inputStringValidator = (s) -> {
               for(ObjectCategoryListRuleType<E, P, ?> listRuleType : listRuleTypes) {
                  if (listRuleType.getStringValidator().test(s)) {
                     return true;
                  }
               }

               return false;
            };
            if (this.adderBuilder.needsInputStringValidator()) {
               this.adderBuilder.setInputStringValidator((s) -> {
                  int separatorIndex = s.indexOf(listRuleTypePrefixSeparator);
                  if (separatorIndex == -1) {
                     return inputStringValidator.test(s);
                  } else {
                     String listRuleTypeString = s.substring(0, separatorIndex);
                     if (inputRuleTypeStringValidator != null && !inputRuleTypeStringValidator.test(listRuleTypeString)) {
                        return false;
                     } else {
                        String elementString = s.substring(separatorIndex + 1);
                        return inputStringValidator.test(elementString);
                     }
                  }
               });
            }

            for(ObjectCategoryListRuleType<E, P, ?> listRuleType : listRuleTypes) {
               String prefix = listRuleType == this.defaultListRuleType ? "" : listRuleType.getId() + listRuleTypePrefixSeparator;
               this.addSuggestionsForListRuleType(listRuleType, prefix);
            }

            return super.build();
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      private <S> void addSuggestionsForListRuleType(ObjectCategoryListRuleType<E, P, S> listRuleType, String prefix) {
         ((Iterable)listRuleType.getAllElementSupplier().get()).forEach((e) -> this.adderBuilder.addOptionBuilderFor(prefix + (String)listRuleType.getSerializer().apply(e)));
      }

      protected abstract ED buildInternally();
   }
}
