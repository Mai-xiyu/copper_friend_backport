package org.xiyu.yee.copper_friend_backport.mixin;

import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.yee.copper_friend_backport.coppergolem.ai.IronGolemOfferFlowerGoal;

/**
 * Mixin to add copper golem flower offering behavior to iron golems
 */
@Mixin(IronGolem.class)
public class IronGolemMixin {
    
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addOfferFlowerGoal(CallbackInfo ci) {
        IronGolem ironGolem = (IronGolem) (Object) this;
        ironGolem.goalSelector.addGoal(2, new IronGolemOfferFlowerGoal(ironGolem));
    }
}
