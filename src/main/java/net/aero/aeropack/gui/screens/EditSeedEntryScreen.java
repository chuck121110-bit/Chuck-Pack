package net.aero.aeropack.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.aero.aeropack.util.config.WorldSeedDatabase;

public class EditSeedEntryScreen extends WindowScreen {
    private final WorldSeedDatabase.SeedEntry entry;
    private final Runnable onComplete;

    public EditSeedEntryScreen(GuiTheme theme, WorldSeedDatabase.SeedEntry entry, Runnable onComplete) {
        super(theme, "Edit Seed Entry");
        this.entry = entry;
        this.onComplete = onComplete;
    }

    @Override
    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();

        table.add(theme.label("Server Address:"));
        WTextBox addressBox = table.add(theme.textBox(entry.address)).expandX().widget();
        table.row();

        table.add(theme.label("World Name:"));
        WTextBox worldBox = table.add(theme.textBox(entry.worldName)).expandX().widget();
        table.row();

        table.add(theme.label("Seed:"));
        WTextBox seedBox = table.add(theme.textBox(entry.seed)).expandX().widget();
        table.row();

        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        WButton saveBtn = table.add(theme.button("Save")).expandX().widget();
        saveBtn.action = () -> {
            entry.address = addressBox.get();
            entry.worldName = worldBox.get();
            entry.seed = seedBox.get();
            onComplete.run();
            close();
        };
    }
}
