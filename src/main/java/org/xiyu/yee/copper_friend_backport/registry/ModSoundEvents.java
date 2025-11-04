package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSoundEvents {
    public static final SoundEvent COPPER_CHEST_CLOSE = register("block.copper_chest.close");
    public static final SoundEvent COPPER_CHEST_OPEN = register("block.copper_chest.open");
    public static final SoundEvent COPPER_CHEST_WEATHERED_CLOSE = register("block.copper_chest_weathered.close");
    public static final SoundEvent COPPER_CHEST_WEATHERED_OPEN = register("block.copper_chest_weathered.open");
    public static final SoundEvent COPPER_CHEST_OXIDIZED_CLOSE = register("block.copper_chest_oxidized.close");
    public static final SoundEvent COPPER_CHEST_OXIDIZED_OPEN = register("block.copper_chest_oxidized.open");
    public static final SoundEvent COPPER_DOOR_CLOSE = register("block.copper_door.close");
    public static final SoundEvent COPPER_DOOR_OPEN = register("block.copper_door.open");
    public static final SoundEvent COPPER_GOLEM_STEP = register("entity.copper_golem.step");
    public static final SoundEvent COPPER_GOLEM_HURT = register("entity.copper_golem.hurt");
    public static final SoundEvent COPPER_GOLEM_DEATH = register("entity.copper_golem.death");
    public static final SoundEvent COPPER_GOLEM_WEATHERED_STEP = register("entity.copper_golem_weathered.step");
    public static final SoundEvent COPPER_GOLEM_WEATHERED_HURT = register("entity.copper_golem_weathered.hurt");
    public static final SoundEvent COPPER_GOLEM_WEATHERED_DEATH = register("entity.copper_golem_weathered.death");
    public static final SoundEvent COPPER_GOLEM_OXIDIZED_STEP = register("entity.copper_golem_oxidized.step");
    public static final SoundEvent COPPER_GOLEM_OXIDIZED_HURT = register("entity.copper_golem_oxidized.hurt");
    public static final SoundEvent COPPER_GOLEM_OXIDIZED_DEATH = register("entity.copper_golem_oxidized.death");
    public static final SoundEvent COPPER_GOLEM_SPIN = register("entity.copper_golem.spin");
    public static final SoundEvent COPPER_GOLEM_WEATHERED_SPIN = register("entity.copper_golem_weathered.spin");
    public static final SoundEvent COPPER_GOLEM_OXIDIZED_SPIN = register("entity.copper_golem_oxidized.spin");
    public static final SoundEvent COPPER_GOLEM_ITEM_GET = register("entity.copper_golem.no_item_get");
    public static final SoundEvent COPPER_GOLEM_ITEM_NO_GET = register("entity.copper_golem.no_item_no_get");
    public static final SoundEvent COPPER_GOLEM_ITEM_DROP = register("entity.copper_golem.item_drop");
    public static final SoundEvent COPPER_GOLEM_ITEM_NO_DROP = register("entity.copper_golem.item_no_drop");
    public static final SoundEvent COPPER_GOLEM_BECOME_STATUE = register("entity.copper_golem_become_statue");
    public static final SoundEvent COPPER_GOLEM_STATUE_BREAK = register("block.copper_golem_statue.break");
    public static final SoundEvent COPPER_GOLEM_STATUE_PLACE = register("block.copper_golem_statue.place");
    public static final SoundEvent COPPER_GOLEM_STATUE_HIT = register("block.copper_golem_statue.hit");
    public static final SoundEvent COPPER_GOLEM_STATUE_STEP = register("block.copper_golem_statue.step");
    public static final SoundEvent COPPER_GOLEM_STATUE_FALL = register("block.copper_golem_statue.fall");
    public static final SoundEvent COPPER_GOLEM_SPAWN = register("entity.copper_golem.spawn");
    public static final SoundEvent COPPER_GOLEM_SHEAR = register("entity.copper_golem.shear");
    public static final SoundEvent COPPER_GRATE_BREAK = register("block.copper_grate.break");
    public static final SoundEvent COPPER_GRATE_STEP = register("block.copper_grate.step");
    public static final SoundEvent COPPER_GRATE_PLACE = register("block.copper_grate.place");
    public static final SoundEvent COPPER_GRATE_HIT = register("block.copper_grate.hit");
    public static final SoundEvent COPPER_GRATE_FALL = register("block.copper_grate.fall");

    private static SoundEvent register(String string) {
        return register(ResourceLocation.withDefaultNamespace(string));
    }

    private static SoundEvent register(ResourceLocation resourceLocation) {
        return register(resourceLocation, resourceLocation);
    }

    private static Holder.Reference<SoundEvent> registerForHolder(String string) {
        return registerForHolder(ResourceLocation.withDefaultNamespace(string));
    }

    private static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation resourceLocation) {
        return registerForHolder(resourceLocation, resourceLocation);
    }

    private static SoundEvent register(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, resourceLocation, SoundEvent.createVariableRangeEvent(resourceLocation2));
    }

    private static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, resourceLocation, SoundEvent.createVariableRangeEvent(resourceLocation2));
    }
}
