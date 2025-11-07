package org.xiyu.yee.copper_friend_backport.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;

@Mod.EventBusSubscriber(modid = CopperFriendBackport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntity.COPPER_GOLEM.get(), CopperGolem.createAttributes().build());
    }
}
