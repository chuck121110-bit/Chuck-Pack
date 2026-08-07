package net.aero.mapintegration.gui;

import net.aero.mapintegration.config.ModConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import xaero.lib.client.gui.GuiSettings;

public class MapIntegrationSettings extends GuiSettings {

    public MapIntegrationSettings(Screen parent) {
        super(Text.literal("Map Integration Settings"), parent, null);

        ModConfig c = ModConfig.get();

        this.entries = new xaero.lib.client.gui.ISettingEntry[]{
            new ToggleEntry("Skip Delete Confirmation", c.skipDeleteConfirmation, v -> { c.skipDeleteConfirmation = v; ModConfig.save(); }),
            new ToggleEntry("Remove Teleport", c.removeTeleport, v -> { c.removeTeleport = v; ModConfig.save(); }),
            new ToggleEntry("Remove Export Button", c.removeExport, v -> { c.removeExport = v; ModConfig.save(); }),
            new ToggleEntry("Remove Settings Button", c.removeSettings, v -> { c.removeSettings = v; ModConfig.save(); }),
            new ToggleEntry("Remove Share Button", c.removeShare, v -> { c.removeShare = v; ModConfig.save(); }),
            new ToggleEntry("Improve Coordinate Display", c.improveCoordinateDisplay, v -> { c.improveCoordinateDisplay = v; ModConfig.save(); }),
            new ToggleEntry("Highlight Chat Coordinates", c.highlightChatCoordinates, v -> { c.highlightChatCoordinates = v; ModConfig.save(); }),
        };
    }
}
