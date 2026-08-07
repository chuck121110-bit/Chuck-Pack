package net.aero.mapintegration.mixin;

import net.aero.mapintegration.gui.MapIntegrationSettings;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.IXaeroMinimap;
import xaero.common.gui.GuiMinimapSettings;
import xaero.common.gui.ScreenSwitchSettingEntry;
import java.util.Arrays;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

@Mixin(targets = "xaero.common.gui.GuiMinimapViewSettings", remap = false)
public abstract class MixinGuiMinimapViewSettings extends GuiMinimapSettings {

    private MixinGuiMinimapViewSettings(net.minecraft.text.Text title, Screen parent,
            Screen escapeScreen, IEditConfigScreenContext context) {
        super(title, parent, escapeScreen, context);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void mapintegration$addSettingsPage(CallbackInfo ci) {
        try {
            int oldLen = this.entries.length;
            ISettingEntry[] newEntries = new ISettingEntry[oldLen + 1];

            ScreenSwitchSettingEntry settingsEntry = new ScreenSwitchSettingEntry(
                "Map Integration",
                (current, escape) -> new MapIntegrationSettings((Screen) current),
                null,
                true
            );

            newEntries[0] = settingsEntry;
            System.arraycopy(this.entries, 0, newEntries, 1, oldLen);

            this.entries = Arrays.stream(newEntries)
                .filter(e -> e == null || !e.getStringForSearch().toLowerCase(java.util.Locale.ROOT).contains("teleport"))
                .toArray(ISettingEntry[]::new);
        } catch (Throwable ignored) {
        }
    }
}
