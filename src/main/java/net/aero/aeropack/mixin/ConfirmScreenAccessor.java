package net.aero.aeropack.mixin;

import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfirmScreen.class)
public interface ConfirmScreenAccessor {
    @Accessor("yesButton")
    Button aeropack$getYesButton();

    @Mutable
    @Accessor("yesButton")
    void aeropack$setYesButton(Button button);
}
