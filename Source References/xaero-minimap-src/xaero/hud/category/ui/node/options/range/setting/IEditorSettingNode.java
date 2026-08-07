package xaero.hud.category.ui.node.options.range.setting;

import xaero.hud.category.setting.ObjectCategorySetting;

public interface IEditorSettingNode<V> {
   ObjectCategorySetting<V> getSetting();

   V getSettingValue();

   boolean isRootSettings();
}
