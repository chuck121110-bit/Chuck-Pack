package xaero.hud.category.ui.node;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.common.misc.MapFactory;
import xaero.hud.category.rule.ObjectCategoryHardRule;
import xaero.hud.category.rule.ObjectCategoryRule;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.options.EditorExpandingOptionsNode;
import xaero.hud.category.ui.node.options.EditorOptionsNode;
import xaero.hud.category.ui.node.options.EditorSimpleButtonNode;
import xaero.hud.category.ui.node.options.range.setting.IEditorSettingNode;
import xaero.hud.category.ui.node.options.text.EditorTextFieldOptionsNode;
import xaero.hud.category.ui.node.rule.EditorExcludeListNode;
import xaero.hud.category.ui.node.rule.EditorIncludeListNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;
import xaero.hud.category.ui.setting.SettingNodeBuilderFactoryManager;

public class EditorFilterSettingsNode<E, P, SETTING_DATA extends EditorOptionsNode<?> & IEditorSettingNode<?>> extends EditorSettingsNode<SETTING_DATA> {
   private final EditorExpandingOptionsNode<ObjectCategoryRule<E, P>> baseRule;
   private final EditorIncludeListNode includeList;
   private final EditorExcludeListNode excludeList;

   protected EditorFilterSettingsNode(Map<ObjectCategorySetting<?>, SETTING_DATA> settings, List<SETTING_DATA> settingList, @Nonnull EditorSimpleButtonNode deleteButton, @Nonnull EditorSimpleButtonNode protectionButton, @Nonnull EditorTextFieldOptionsNode nameOption, ListFactory listFactory, boolean rootSettings, @Nonnull EditorExpandingOptionsNode<ObjectCategoryRule<E, P>> baseRule, @Nonnull EditorIncludeListNode includeList, @Nonnull EditorExcludeListNode excludeList, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, boolean protection) {
      super(settings, settingList, deleteButton, protectionButton, nameOption, listFactory, rootSettings, movable, listEntryFactory, tooltipSupplier, protection);
      this.baseRule = baseRule;
      this.includeList = includeList;
      this.excludeList = excludeList;
   }

   public ObjectCategoryRule<E, P> getBaseRule() {
      return (ObjectCategoryRule)this.baseRule.getCurrentValue().getValue();
   }

   public EditorIncludeListNode getIncludeList() {
      return this.includeList;
   }

   public EditorExcludeListNode getExcludeList() {
      return this.excludeList;
   }

   public List<EditorNode> getSubNodes() {
      List<EditorNode> result = super.getSubNodes();
      result.add(4, this.excludeList);
      result.add(4, this.includeList);
      result.add(4, this.baseRule);
      return result;
   }

   public abstract static class Builder<E, P, SD extends EditorFilterSettingsNode<E, P, ?>, SDB extends Builder<E, P, SD, SDB>> extends EditorSettingsNode.Builder<SD, SDB> {
      protected final EditorExpandingOptionsNode.Builder<ObjectCategoryRule<E, P>, ?> baseRuleBuilder;
      private final EditorIncludeListNode.Builder<E, P> includeListBuilder;
      private final EditorExcludeListNode.Builder<E, P> excludeListBuilder;
      private final List<ObjectCategoryHardRule<E, P>> allRules;
      private String listRuleTypePrefixSeparator;
      private Predicate<String> inputRuleTypeStringValidator;

      protected Builder(MapFactory mapFactory, ListFactory listFactory, List<ObjectCategorySetting<?>> allSettings, List<ObjectCategoryHardRule<E, P>> allRules, SettingNodeBuilderFactoryManager settingNodeBuilderFactoryManager) {
         super(mapFactory, listFactory, allSettings, settingNodeBuilderFactoryManager);
         this.baseRuleBuilder = EditorExpandingOptionsNode.FinalBuilder.begin(listFactory);
         this.includeListBuilder = EditorIncludeListNode.Builder.<E, P>begin(listFactory);
         this.excludeListBuilder = EditorExcludeListNode.Builder.<E, P>begin(listFactory);
         this.allRules = allRules;
      }

      public SDB setDefault() {
         super.setDefault();
         this.setBaseRule((ObjectCategoryRule)null);
         ((EditorExpandingOptionsNode.Builder)this.baseRuleBuilder.setDefault().setDisplayName(class_2561.method_43471("gui.xaero_category_hard_include"))).setIsActiveSupplier((parent, data) -> !((EditorSettingsNode)parent).getProtection());

         for(ObjectCategoryRule<E, P> rule : this.allRules) {
            this.baseRuleBuilder.addOptionBuilderFor(rule);
         }

         this.includeListBuilder.setDefault();
         this.includeListBuilder.getIncludeInSuperToggleDataBuilder().setCurrentValue(true);
         this.excludeListBuilder.setDefault();
         this.setListRuleTypePrefixSeparator((String)null);
         this.setInputRuleTypeStringValidator((Predicate)null);
         return this.self;
      }

      public void setBaseRule(ObjectCategoryRule<E, P> baseRule) {
         this.baseRuleBuilder.setCurrentValue(baseRule);
      }

      public SDB setListRuleTypePrefixSeparator(String listRuleTypePrefixSeparator) {
         this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
         return this.self;
      }

      public SDB setInputRuleTypeStringValidator(Predicate<String> inputRuleTypeStringValidator) {
         this.inputRuleTypeStringValidator = inputRuleTypeStringValidator;
         return this.self;
      }

      public EditorIncludeListNode.Builder<E, P> getIncludeListBuilder() {
         return this.includeListBuilder;
      }

      public EditorExcludeListNode.Builder<E, P> getExcludeListBuilder() {
         return this.excludeListBuilder;
      }

      protected EditorIncludeListNode buildIncludeList() {
         return this.includeListBuilder.build();
      }

      protected EditorExcludeListNode buildExcludeList() {
         return this.excludeListBuilder.build();
      }

      public SD build() {
         if (this.baseRuleBuilder != null && this.listRuleTypePrefixSeparator != null) {
            ((EditorIncludeListNode.Builder)this.includeListBuilder.setListRuleTypePrefixSeparator(this.listRuleTypePrefixSeparator)).setInputRuleTypeStringValidator(this.inputRuleTypeStringValidator);
            ((EditorExcludeListNode.Builder)this.excludeListBuilder.setListRuleTypePrefixSeparator(this.listRuleTypePrefixSeparator)).setInputRuleTypeStringValidator(this.inputRuleTypeStringValidator);
            SD result = (SD)(super.build());
            return result;
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }
   }

   public static final class FinalBuilder<E, P> extends Builder<E, P, EditorFilterSettingsNode<E, P, ?>, FinalBuilder<E, P>> {
      private FinalBuilder(MapFactory mapFactory, ListFactory listFactory, List<ObjectCategorySetting<?>> allSettings, List<ObjectCategoryHardRule<E, P>> allRules, SettingNodeBuilderFactoryManager settingNodeBuilderFactoryManager) {
         super(mapFactory, listFactory, allSettings, allRules, settingNodeBuilderFactoryManager);
      }

      protected EditorFilterSettingsNode<E, P, ?> buildInternally(List<IEditorSettingNode<?>> builtSettingData, Map<ObjectCategorySetting<?>, IEditorSettingNode<?>> builtSettingsDataMap) {
         EditorFilterSettingsNode<E, P, ?> result = new EditorFilterSettingsNode(builtSettingsDataMap, builtSettingData, this.deleteButtonBuilder.build(), this.protectionButtonBuilder.build(), this.nameOptionBuilder.build(), this.listFactory, this.rootSettings, this.baseRuleBuilder.build(), this.buildIncludeList(), this.buildExcludeList(), this.movable, this.listEntryFactory, this.tooltipSupplier, this.protection);
         return result;
      }

      public static <E, P, S> FinalBuilder<E, P> begin(MapFactory mapFactory, ListFactory listFactory, List<ObjectCategorySetting<?>> allSettings, List<ObjectCategoryHardRule<E, P>> allRules, SettingNodeBuilderFactoryManager settingNodeBuilderFactoryManager) {
         return (FinalBuilder)(new FinalBuilder(mapFactory, listFactory, allSettings, allRules, settingNodeBuilderFactoryManager)).setDefault();
      }
   }
}
