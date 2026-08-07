package net.aero.aeropack.mixin;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfirmScreen.class)
public interface ConfirmScreenAccessor {
    @Accessor("yesButton")
    ButtonWidget aeropack$getYesButton();

    @Mutable
    @Accessor("yesButton")
    void aeropack$setYesButton(ButtonWidget button);
}
