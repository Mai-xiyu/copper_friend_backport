package org.xiyu.yee.copper_friend_backport.mixin;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;

@Mixin(OfferFlowerGoal.class)
public abstract class OfferFlowerGoalMixin {
    @Shadow
    private int tick;

    @Shadow
    @Final
    private IronGolem golem;
    @Unique
    private Entity entity;
    @Inject(method = "stop", at = @At("TAIL"))
    public void Inject15(CallbackInfo ci) {
        if (this.tick == 0
                && this.entity instanceof CopperGolem mob
                && mob.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA).isEmpty()
                && this.getGolemBoundingBox().intersects(mob.getBoundingBox())) {
            mob.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, Items.POPPY.getDefaultInstance());
            mob.setGuaranteedDrop(CopperGolem.EQUIPMENT_SLOT_ANTENNA);
        }
        this.entity = null;
    }
    private AABB getGolemBoundingBox() {
        return this.golem.getBoundingBox().inflate(6.0, 2.0, 6.0);
    }
    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearestEntity(Ljava/lang/Class;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDDLnet/minecraft/world/phys/AABB;)Lnet/minecraft/world/entity/LivingEntity;"), cancellable = true)
    public void Inject44(CallbackInfoReturnable<Boolean> cir) {
        this.entity = this.golem.level()
                .getNearestEntity(
                        CopperGolem.class,
                        TargetingConditions.forNonCombat().range(6.0),
                        this.golem,
                        this.golem.getX(),
                        this.golem.getY(),
                        this.golem.getZ(),
                        this.getGolemBoundingBox()
                );
    }
    @Inject(method = "tick", at = @At("HEAD"))
    public void Inject59(CallbackInfo ci) {
        if (this.entity != null) {
            this.golem.getLookControl().setLookAt(this.entity, 30.0F, 30.0F);
        }
    }
}