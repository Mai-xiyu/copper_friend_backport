package org.xiyu.yee.copper_friend_backport.events;

import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;

import java.util.function.BiFunction;

@Mod.EventBusSubscriber
public class ForgeCommonEvent {
    static BiFunction<Vec3,Integer,AABB> function = (center, i) ->
            new AABB(center.add(-i, -i, -i), center.add(i,i,i));
    @SubscribeEvent
    public static void onVanillaGame(VanillaGameEvent event) {
        if (event.getVanillaEvent() == GameEvent.JUKEBOX_PLAY) {
            AABB aabb = function.apply(event.getEventPosition(),16);
            event.getLevel().getEntities(ModEntity.COPPER_GOLEM.get(), aabb, copperGolem ->true).forEach(CopperGolem::setJukeboxPlaying
            );
        } else if (event.getVanillaEvent() == GameEvent.JUKEBOX_STOP_PLAY) {
            AABB aabb = function.apply(event.getEventPosition(),16);
            event.getLevel().getEntities(ModEntity.COPPER_GOLEM.get(), aabb, copperGolem ->true).forEach(CopperGolem::setJukeboxNotPlaying
            );
        }
    }

}
