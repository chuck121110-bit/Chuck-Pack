package net.aero.aeropack.swarm;

import meteordevelopment.meteorclient.settings.Setting;

public interface IAutoSwarmConnect {
    Setting<Boolean> aeropack$autoConnectSetting();
    Setting<Boolean> aeropack$autoAccountSetting();
}
