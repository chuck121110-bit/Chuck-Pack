package net.chuck.chuckpack.modules.movement.nofallplus;

public enum NoFallModes {
    Matrix_New,
    Vulcan,
    Vulcan_2dot7dot7,
    Verus,
    Elytra_Clip,
    Elytra_Fly,
    No_Ground,
    No_Ground_Elytra;

    @Override
    public String toString() {
        return name().replace('_', ' ').replace("dot", ".");
    }
}
