package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import net.chuck.chuckpack.swarm.IAutoSwarmConnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Swarm.class)
public abstract class SwarmAutoConnectMixin extends Module implements IAutoSwarmConnect {

    protected SwarmAutoConnectMixin(meteordevelopment.meteorclient.systems.modules.Category category, String name, String description) {
        super(category, name, description);
    }

    @Unique
    private Setting<Boolean> chuckpack$autoSwarmConnect;

    @Unique
    private Setting<Boolean> chuckpack$autoAccount;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chuckpack$addAutoConnectSetting(CallbackInfo ci) {
        SettingGroup sg = this.settings.getDefaultGroup();

        chuckpack$autoSwarmConnect = sg.add(new BoolSetting.Builder()
            .name("Auto Swarm Connect")
            .description("Automatically starts the host server or connects as a worker every 5 seconds until successful (Chuck Pack).")
            .defaultValue(false)
            .build()
        );

        chuckpack$autoAccount = sg.add(new BoolSetting.Builder()
            .name("Auto Account")
            .description("Automatically switches to the next available cracked account when joining a server via .swarm server (Chuck Pack).")
            .defaultValue(false)
            .build()
        );
    }

    @Override
    public Setting<Boolean> chuckpack$autoConnectSetting() {
        return chuckpack$autoSwarmConnect;
    }

    @Override
    public Setting<Boolean> chuckpack$autoAccountSetting() {
        return chuckpack$autoAccount;
    }
}
