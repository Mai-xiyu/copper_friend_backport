package org.xiyu.yee.copper_friend_backport;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(CopperFriendBackport.MOD_ID)
public class CopperFriendBackport {
    public static final String MOD_ID = "copper_friend_backport";
    private static final Logger LOGGER = LogUtils.getLogger();
    public CopperFriendBackport(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
    }
}
