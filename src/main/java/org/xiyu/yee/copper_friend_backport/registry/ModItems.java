package org.xiyu.yee.copper_friend_backport.registry;

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

    public static final RegistryObject<Item> COPPER_GOLEM_SPAWN_EGG = ITEMS.register(
            "copper_golem_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntity.COPPER_GOLEM,
                    0xED8E5B,  // 主色 (铜色)
                    0x9C5D3A,  // 次色 (深铜色)
                    new Item.Properties()
            )
    );
}
