package net.aero.aeropack.uiutils;

import net.aero.aeropack.modules.misc.UiUtilsMod;
import meteordevelopment.meteorclient.systems.modules.Modules;

public final class UiUtilsModAccess {
    private UiUtilsModAccess() {
    }

    public static UiUtilsMod get() {
        Modules modules = Modules.get();
        if (modules == null) return null;
        return modules.get(UiUtilsMod.class);
    }
}
