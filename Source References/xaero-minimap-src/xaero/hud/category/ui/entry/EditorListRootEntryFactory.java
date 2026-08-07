package xaero.hud.category.ui.entry;

import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.node.EditorNode;

public interface EditorListRootEntryFactory {
   EditorListRootEntry get(EditorNode var1, EditorNode var2, int var3, ConnectionLineType var4, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList var5, int var6, boolean var7);
}
