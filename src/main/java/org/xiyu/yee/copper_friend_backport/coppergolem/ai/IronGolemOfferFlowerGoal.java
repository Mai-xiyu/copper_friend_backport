package org.xiyu.yee.copper_friend_backport.coppergolem.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.phys.AABB;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

import java.util.EnumSet;
import java.util.List;

/**
 * AI Goal for iron golems to offer flowers to copper golems
 * 铁傀儡向铜傀儡赠送虞美人的AI目标
 * 参考原版IronGolem的OfferFlowerGoal实现
 */
public class IronGolemOfferFlowerGoal extends Goal {
    private final IronGolem ironGolem;
    private CopperGolem targetCopperGolem;
    private int offerFlowerTick;
    
    public IronGolemOfferFlowerGoal(IronGolem ironGolem) {
        this.ironGolem = ironGolem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        // 如果铁傀儡没有在送花冷却中
        if (!this.ironGolem.level().isClientSide() && this.ironGolem.isAlive()) {
            // 寻找附近的铜傀儡（16格范围内）
            AABB searchBox = this.ironGolem.getBoundingBox().inflate(6.0D, 2.0D, 6.0D);
            List<CopperGolem> nearbyGolems = this.ironGolem.level().getEntitiesOfClass(
                CopperGolem.class,
                searchBox,
                golem -> !golem.hasPoppy() && golem.isAlive()
            );
            
            if (!nearbyGolems.isEmpty()) {
                this.targetCopperGolem = nearbyGolems.get(this.ironGolem.getRandom().nextInt(nearbyGolems.size()));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canContinueToUse() {
        return this.offerFlowerTick > 0;
    }
    
    @Override
    public void start() {
        this.offerFlowerTick = this.adjustedTickDelay(400);
        // 不给铁傀儡手持虞美人，保持原版行为
    }
    
    @Override
    public void stop() {
        this.targetCopperGolem = null;
    }
    
    @Override
    public void tick() {
        if (this.targetCopperGolem != null) {
            // 看向铜傀儡
            this.ironGolem.getLookControl().setLookAt(
                this.targetCopperGolem,
                30.0F,
                30.0F
            );
        }
        
        this.offerFlowerTick--;
        
        // 在特定时刻给予虞美人
        if (this.offerFlowerTick == 0 && this.targetCopperGolem != null) {
            if (this.targetCopperGolem.isAlive() && !this.targetCopperGolem.hasPoppy()) {
                double distance = this.ironGolem.distanceToSqr(this.targetCopperGolem);
                if (distance < 6.25D) { // 在2.5格范围内
                    // 铜傀儡接受虞美人
                    this.targetCopperGolem.setHasPoppy(true);
                    
                    // 播放声音（使用原版铁傀儡送花的音效逻辑）
                    this.ironGolem.level().broadcastEntityEvent(this.ironGolem, (byte) 11);
                }
            }
        }
    }
    
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
