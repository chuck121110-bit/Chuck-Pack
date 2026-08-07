package net.aero.aeropack.render;

/**
 * Shared render-mode enum used by Nuker/VeinMiner/Excavator's new "render-mode"
 * setting, since the real Meteor Client classes don't have their own RenderMode
 * enum (unlike StorageESP, which already ships with Box/Shader natively).
 */
public enum AeroRenderMode {
    BoxESP,
    Shader
}
