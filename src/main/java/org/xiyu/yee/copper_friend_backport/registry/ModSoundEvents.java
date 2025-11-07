package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, CopperFriendBackport.MOD_ID
    );

    public static final RegistryObject<SoundEvent> COPPER_CHEST_CLOSE = register("block.copper_chest.close");
    public static final RegistryObject<SoundEvent> COPPER_CHEST_OPEN = register("block.copper_chest.open");
    public static final RegistryObject<SoundEvent> COPPER_CHEST_WEATHERED_CLOSE = register("block.copper_chest_weathered.close");
    public static final RegistryObject<SoundEvent> COPPER_CHEST_WEATHERED_OPEN = register("block.copper_chest_weathered.open");
    public static final RegistryObject<SoundEvent> COPPER_CHEST_OXIDIZED_CLOSE = register("block.copper_chest_oxidized.close");
    public static final RegistryObject<SoundEvent> COPPER_CHEST_OXIDIZED_OPEN = register("block.copper_chest_oxidized.open");
    public static final RegistryObject<SoundEvent> COPPER_DOOR_CLOSE = register("block.copper_door.close");
    public static final RegistryObject<SoundEvent> COPPER_DOOR_OPEN = register("block.copper_door.open");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STEP = register("entity.copper_golem.step");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_HURT = register("entity.copper_golem.hurt");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_DEATH = register("entity.copper_golem.death");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_WEATHERED_STEP = register("entity.copper_golem_weathered.step");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_WEATHERED_HURT = register("entity.copper_golem_weathered.hurt");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_WEATHERED_DEATH = register("entity.copper_golem_weathered.death");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_OXIDIZED_STEP = register("entity.copper_golem_oxidized.step");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_OXIDIZED_HURT = register("entity.copper_golem_oxidized.hurt");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_OXIDIZED_DEATH = register("entity.copper_golem_oxidized.death");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_SPIN = register("entity.copper_golem.spin");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_WEATHERED_SPIN = register("entity.copper_golem_weathered.spin");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_OXIDIZED_SPIN = register("entity.copper_golem_oxidized.spin");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_ITEM_GET = register("entity.copper_golem.no_item_get");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_ITEM_NO_GET = register("entity.copper_golem.no_item_no_get");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_ITEM_DROP = register("entity.copper_golem.item_drop");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_ITEM_NO_DROP = register("entity.copper_golem.item_no_drop");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_BECOME_STATUE = register("entity.copper_golem.become_statue");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STATUE_BREAK = register("block.copper_golem_statue.break");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STATUE_PLACE = register("block.copper_golem_statue.place");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STATUE_HIT = register("block.copper_golem_statue.hit");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STATUE_STEP = register("block.copper_golem_statue.step");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_STATUE_FALL = register("block.copper_golem_statue.fall");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_SPAWN = register("entity.copper_golem.spawn");
    public static final RegistryObject<SoundEvent> COPPER_GOLEM_SHEAR = register("entity.copper_golem.shear");
    public static final RegistryObject<SoundEvent> COPPER_GRATE_BREAK = register("block.copper_grate.break");
    public static final RegistryObject<SoundEvent> COPPER_GRATE_STEP = register("block.copper_grate.step");
    public static final RegistryObject<SoundEvent> COPPER_GRATE_PLACE = register("block.copper_grate.place");
    public static final RegistryObject<SoundEvent> COPPER_GRATE_HIT = register("block.copper_grate.hit");
    public static final RegistryObject<SoundEvent> COPPER_GRATE_FALL = register("block.copper_grate.fall");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation location = ResourceLocation.tryBuild(CopperFriendBackport.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }
}