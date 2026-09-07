package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Setting.class)
public interface SettingMutatorAccessor {
    @Mutable
    @Accessor("visible")
    void chuckpack$setVisible(IVisible visible);

    @Mutable
    @Accessor("name")
    void chuckpack$setName(String name);
}
