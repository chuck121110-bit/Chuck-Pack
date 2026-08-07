package net.aero.aeropack.modules.render;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class TrueSight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyListedTypes = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Listed Types")
        .description("When enabled, only the entity types in the 'Entity Types' list are revealed. When disabled, all invisible entities are revealed (matching the original TrueSight behavior).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entityTypes = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("Entity Types")
        .description("The entity types to reveal when 'Only Listed Types' is enabled. Players are revealed so spectator players render their skin.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    public TrueSight() {
        super(Categories.Render, "True Sight",
            "Renders invisible entities' actual models instead of hiding them, so you see the player's skin or the mob's texture (including spectator players). No glow outline is added.");
    }

    public boolean shouldBeVisible(Entity entity) {
        return !onlyListedTypes.get() || entityTypes.get().contains(entity.getType());
    }
}
