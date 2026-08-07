package net.aero.mapintegration.mixin;

import net.aero.mapintegration.gui.MapIntegrationSettings;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.Arrays;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.EditConfigScreen;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.map.gui.GuiWorldMapSettings;
import xaero.map.gui.ScreenSwitchSettingEntry;

@Mixin(value = GuiWorldMapSettings.class, remap = false)
public abstract class MixinGuiWorldMapSettings extends EditConfigScreen {

    private MixinGuiWorldMapSettings(net.minecraft.text.Text title, Screen parent,
            Screen escapeScreen, IEditConfigScreenContext context,
            xaero.lib.common.config.channel.ConfigChannel channel) {
        super(title, parent, escapeScreen, context, channel);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/class_437;Lnet/minecraft/class_437;Lxaero/lib/client/gui/config/context/IEditConfigScreenContext;)V",
        at = @At("RETURN"),
        remap = true
    )
    public void mapintegration$addSettingsPage(Screen parent, Screen escapeScreen,
            IEditConfigScreenContext context, CallbackInfo ci) {
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
