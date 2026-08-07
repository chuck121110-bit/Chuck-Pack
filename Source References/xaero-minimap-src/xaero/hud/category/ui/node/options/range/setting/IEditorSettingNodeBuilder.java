package xaero.hud.category.ui.node.options.range.setting;

import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.node.options.EditorOptionsNode;

public interface IEditorSettingNodeBuilder<V, SD extends EditorOptionsNode<Integer> & IEditorSettingNode<V>> {
   IEditorSettingNodeBuilder<V, SD> setSetting(ObjectCategorySetting<V> var1);

   IEditorSettingNodeBuilder<V, SD> setSettingValue(V var1);

   IEditorSettingNodeBuilder<V, SD> setRootSettings(boolean var1);

   SD build();
}
