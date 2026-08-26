package net.chuck.chuckpack.modules.misc.villagerroller;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;

public class EnchantmentSelectScreen extends WindowScreen {
    private final GuiTheme theme;
    private final EnchantmentSelectCallback callback;
    private String filterText = "";
    private final boolean onlyTradeable;

    public EnchantmentSelectScreen(GuiTheme theme, boolean onlyTradeable, EnchantmentSelectCallback callback) {
        super(theme, "Select enchantment");
        this.theme = theme;
        this.callback = callback;
        this.onlyTradeable = onlyTradeable;
    }

    @Override
    public void initWidgets() {
        WTable table = theme.table();
        table.minWidth = 400.0F;
        WTextBox filter = add(theme.textBox(filterText, "Search")).minWidth(400.0F).expandX().widget();
        filter.setFocused(true);
        filter.setCursorMax();
        filter.action = () -> {
            filterText = filter.get().trim();
            table.clear();
            fillTable(table);
        };
        WHorizontalList customList = add(theme.horizontalList()).expandX().widget();
        WTextBox cc = customList.add(theme.textBox("", "Custom")).expandX().expandWidgetX().widget();
        WButton ca = customList.add(theme.button("Select")).widget();
        ca.action = () -> {
            String idtext = cc.get();
            if (idtext.isEmpty()) return;
            Identifier id = Identifier.tryParse(idtext);
            if (id == null) return;
            callback.selection(new RollingEnchantment(id, 0, 0, true));
            onClose();
        };
        add(table);
        fillTable(table);
    }

    private void fillTable(WTable table) {
        if (MeteorClient.mc.level == null) return;
        var reg = MeteorClient.mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        List<Holder<Enchantment>> available = new ArrayList<>();
        if (onlyTradeable) {
            var l = reg.getTagOrEmpty(EnchantmentTags.TRADEABLE);
            l.forEach(available::add);
        } else {
            for (var a : reg.asHolderIdMap()) {
                available.add(a);
            }
        }

        for (Holder<Enchantment> e : available.stream().sorted((o1, o2) ->
                Names.get(o1).compareToIgnoreCase(Names.get(o2))).toList()) {
            if (filterText.isEmpty() || Names.get(e).toLowerCase().startsWith(filterText.toLowerCase())) {
                table.add(theme.label(Names.get(e))).expandCellX();
                WButton a = table.add(theme.button("Select")).widget();
                a.action = () -> {
                    callback.selection(new RollingEnchantment(reg.getKey(e.value()), e.value().getMaxLevel(),
                        RollingEnchantment.getMinimumPrice(e), true));
                    onClose();
                };
                table.row();
            }
        }
    }

    public interface EnchantmentSelectCallback {
        void selection(RollingEnchantment enchantment);
    }
}
