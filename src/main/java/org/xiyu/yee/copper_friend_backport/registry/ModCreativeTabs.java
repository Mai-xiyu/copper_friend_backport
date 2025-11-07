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
                        // Add all mod items
                        output.accept(ModItems.COPPER_GOLEM_SPAWN_EGG.get());
                    })
                    .build()
    );
}
