package org.xiyu.yee.copper_friend_backport;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.xiyu.yee.copper_friend_backport.registry.EntityDataSerializers;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;
import org.xiyu.yee.copper_friend_backport.registry.ModCreativeTabs;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModItems;
import org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;


@Mod(CopperFriendBackport.MOD_ID)
public class CopperFriendBackport {
    public static final String MOD_ID = "copper_friend_backport";
    public static final Logger LOGGER = LogUtils.getLogger();
    public CopperFriendBackport(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        EntityDataSerializers.init();
        
        // Register memory modules
        ModMemoryModules.MEMORY_MODULE_TYPES.register(modEventBus);
        
        // Register entities
        ModEntity.ENTITIES.register(modEventBus);
        
        // Register blocks
        ModBlocks.BLOCKS.register(modEventBus);
        
        // Register block entities
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
        
        // Register items
        ModItems.ITEMS.register(modEventBus);
        
        // Register creative tabs
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        
        // Register sound events
        ModSoundEvents.SOUND_EVENTS.register(modEventBus);
    }
}
