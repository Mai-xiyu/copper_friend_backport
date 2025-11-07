package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS, CopperFriendBackport.MOD_ID
    );

    // Copper Golem Spawn Egg
    public static final RegistryObject<Item> COPPER_GOLEM_SPAWN_EGG = ITEMS.register(
            "copper_golem_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntity.COPPER_GOLEM,
                    0xED8E5B,  // 主色 (铜色)
                    0x9C5D3A,  // 次色 (深铜色)
                    new Item.Properties()
            )
    );

    // Copper Chest Block Items - Oxidizable variants
    public static final RegistryObject<Item> COPPER_CHEST = ITEMS.register(
            "copper_chest",
            () -> new BlockItem(ModBlocks.COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> EXPOSED_COPPER_CHEST = ITEMS.register(
            "exposed_copper_chest",
            () -> new BlockItem(ModBlocks.EXPOSED_COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WEATHERED_COPPER_CHEST = ITEMS.register(
            "weathered_copper_chest",
            () -> new BlockItem(ModBlocks.WEATHERED_COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> OXIDIZED_COPPER_CHEST = ITEMS.register(
            "oxidized_copper_chest",
            () -> new BlockItem(ModBlocks.OXIDIZED_COPPER_CHEST.get(), new Item.Properties())
    );

    // Copper Chest Block Items - Waxed variants
    public static final RegistryObject<Item> WAXED_COPPER_CHEST = ITEMS.register(
            "waxed_copper_chest",
            () -> new BlockItem(ModBlocks.WAXED_COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_EXPOSED_COPPER_CHEST = ITEMS.register(
            "waxed_exposed_copper_chest",
            () -> new BlockItem(ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_WEATHERED_COPPER_CHEST = ITEMS.register(
            "waxed_weathered_copper_chest",
            () -> new BlockItem(ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_OXIDIZED_COPPER_CHEST = ITEMS.register(
            "waxed_oxidized_copper_chest",
            () -> new BlockItem(ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get(), new Item.Properties())
    );

    // Copper Golem Statue Block Items
    public static final RegistryObject<Item> COPPER_GOLEM_STATUE = ITEMS.register(
            "copper_golem_statue",
            () -> new BlockItem(ModBlocks.COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> EXPOSED_COPPER_GOLEM_STATUE = ITEMS.register(
            "exposed_copper_golem_statue",
            () -> new BlockItem(ModBlocks.EXPOSED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WEATHERED_COPPER_GOLEM_STATUE = ITEMS.register(
            "weathered_copper_golem_statue",
            () -> new BlockItem(ModBlocks.WEATHERED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> OXIDIZED_COPPER_GOLEM_STATUE = ITEMS.register(
            "oxidized_copper_golem_statue",
            () -> new BlockItem(ModBlocks.OXIDIZED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_COPPER_GOLEM_STATUE = ITEMS.register(
            "waxed_copper_golem_statue",
            () -> new BlockItem(ModBlocks.WAXED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_EXPOSED_COPPER_GOLEM_STATUE = ITEMS.register(
            "waxed_exposed_copper_golem_statue",
            () -> new BlockItem(ModBlocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_WEATHERED_COPPER_GOLEM_STATUE = ITEMS.register(
            "waxed_weathered_copper_golem_statue",
            () -> new BlockItem(ModBlocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> WAXED_OXIDIZED_COPPER_GOLEM_STATUE = ITEMS.register(
            "waxed_oxidized_copper_golem_statue",
            () -> new BlockItem(ModBlocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE.get(), new Item.Properties())
    );
}

