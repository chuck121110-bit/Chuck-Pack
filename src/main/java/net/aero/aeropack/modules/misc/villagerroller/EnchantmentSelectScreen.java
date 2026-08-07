package net.aero.aeropack.modules.misc.villagerroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

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
            if (!idtext.isEmpty()) {
                Identifier id = Identifier.of(idtext);
                if (id != null) {
                    callback.selection(new RollingEnchantment(id, 0, 0, true));
                    close();
                }
            }
        };
        add(table);
        fillTable(table);
    }

    private void fillTable(WTable table) {
        if (MeteorClient.mc.world == null) return;
        Registry<Enchantment> reg = MeteorClient.mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        List<RegistryEntry<Enchantment>> available = new ArrayList<>();
        for (RegistryEntry<Enchantment> e : reg.streamEntries().toList()) {
            if (!onlyTradeable || e.isIn(net.minecraft.registry.tag.EnchantmentTags.TRADEABLE)) {
                available.add(e);
            }
        }

        for (RegistryEntry<Enchantment> e : available.stream().sorted((o1, o2) ->
                Names.get(o1).compareToIgnoreCase(Names.get(o2))).toList()) {
            if (filterText.isEmpty() || Names.get(e).toLowerCase().startsWith(filterText.toLowerCase())) {
                table.add(theme.label(Names.get(e))).expandCellX();
                WButton a = table.add(theme.button("Select")).widget();
                a.action = () -> {
                    Identifier id = e.getKey().map(RegistryKey::getValue).orElse(null);
                    if (id != null) {
                        callback.selection(new RollingEnchantment(id, e.value().getMaxLevel(),
                            RollingEnchantment.getMinimumPrice(e), true));
                    }
                    close();
                };
                table.row();
            }
        }
    }

    public interface EnchantmentSelectCallback {
        void selection(RollingEnchantment enchantment);
    }
}
