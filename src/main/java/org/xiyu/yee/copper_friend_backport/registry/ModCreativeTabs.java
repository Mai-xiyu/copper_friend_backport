package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, CopperFriendBackport.MOD_ID
    );

    public static final RegistryObject<CreativeModeTab> COPPER_FRIEND_TAB = CREATIVE_MODE_TABS.register(
            "copper_friend_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.copper_friend_backport"))
                    .icon(() -> new ItemStack(ModItems.COPPER_GOLEM_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        // Copper Golem
                        output.accept(ModItems.COPPER_GOLEM_SPAWN_EGG.get());
                        
                        // Copper Chests - Oxidizable variants
                        output.accept(ModItems.COPPER_CHEST.get());
                        output.accept(ModItems.EXPOSED_COPPER_CHEST.get());
                        output.accept(ModItems.WEATHERED_COPPER_CHEST.get());
                        output.accept(ModItems.OXIDIZED_COPPER_CHEST.get());
                        
                        // Copper Chests - Waxed variants
                        output.accept(ModItems.WAXED_COPPER_CHEST.get());
                        output.accept(ModItems.WAXED_EXPOSED_COPPER_CHEST.get());
                        output.accept(ModItems.WAXED_WEATHERED_COPPER_CHEST.get());
                        output.accept(ModItems.WAXED_OXIDIZED_COPPER_CHEST.get());
                        
                        // Copper Golem Statues - COMMENTED OUT
                        /*
                        output.accept(ModItems.COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.EXPOSED_COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.WEATHERED_COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.OXIDIZED_COPPER_GOLEM_STATUE.get());
                        
                        // Copper Golem Statues - Waxed variants
                        output.accept(ModItems.WAXED_COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.WAXED_EXPOSED_COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.WAXED_WEATHERED_COPPER_GOLEM_STATUE.get());
                        output.accept(ModItems.WAXED_OXIDIZED_COPPER_GOLEM_STATUE.get());
                        */
                    })
                    .build()
    );
}
