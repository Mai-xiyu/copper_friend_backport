package org.xiyu.yee.copper_friend_backport.coppergolem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ModCountDownCooldownTicks extends CountDownCooldownTicks {
    String name;

    public ModCountDownCooldownTicks(MemoryModuleType<Integer> pCooldownTicks,String name) {
        super(pCooldownTicks);
        this.name = name;
    }

    @Override
    protected void tick(ServerLevel pLevel, LivingEntity pOwner, long pGameTime) {
        super.tick(pLevel, pOwner, pGameTime);
    }

    @Override
    protected void stop(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
        super.stop(pLevel, pEntity, pGameTime);
    }
}