package org.xiyu.yee.copper_friend_backport;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.xiyu.yee.copper_friend_backport.registry.EntityDataSerializers;
import org.xiyu.yee.copper_friend_backport.registry.ModCreativeTabs;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModItems;


@Mod(CopperFriendBackport.MOD_ID)
public class CopperFriendBackport {
    public static final String MOD_ID = "copper_friend_backport";
    public static final Logger LOGGER = LogUtils.getLogger();
    public CopperFriendBackport(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        
        // Initialize entity data serializers (force static initialization)
        EntityDataSerializers.class.getName();
        
        // Register entities
        ModEntity.ENTITIES.register(modEventBus);
        
        // Register items
        ModItems.ITEMS.register(modEventBus);
        
        // Register creative tabs
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
