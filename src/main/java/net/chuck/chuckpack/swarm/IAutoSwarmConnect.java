package net.chuck.chuckpack.swarm;

import meteordevelopment.meteorclient.settings.Setting;

public interface IAutoSwarmConnect {
    Setting<Boolean> chuckpack$autoConnectSetting();
    Setting<Boolean> chuckpack$autoAccountSetting();
}
