package org.xiyu.yee.copper_friend_backport.coppergolem.behavior;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
//半石山
public class TransportItemsBetweenContainers extends Behavior<PathfinderMob> {
    private final float speedModifier;
    private final Predicate<BlockState> sourceBlockPredicate;
    private final Predicate<BlockState> destinationBlockPredicate;
    private final int horizontalSearchRadius;
    private final int verticalSearchRadius;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> targetReachedInteractions;
    private final Consumer<PathfinderMob> onTravelling;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    
    private TransportItemTarget currentTarget;
    private int ticksSinceReached;
    private ContainerInteractionState currentState;
    private final SimpleContainer inventory = new SimpleContainer(1);

    public TransportItemsBetweenContainers(
        float speedModifier,
        Predicate<BlockState> sourceBlockPredicate,
        Predicate<BlockState> destinationBlockPredicate,
        int horizontalSearchRadius,
        int verticalSearchRadius,
        Map<ContainerInteractionState, OnTargetReachedInteraction> targetReachedInteractions,
        Consumer<PathfinderMob> onTravelling,
        Predicate<TransportItemTarget> shouldQueueForTarget
    ) {
        super(Map.of(
            MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 600);
        this.speedModifier = speedModifier;
        this.sourceBlockPredicate = sourceBlockPredicate;
        this.destinationBlockPredicate = destinationBlockPredicate;
        this.horizontalSearchRadius = horizontalSearchRadius;
        this.verticalSearchRadius = verticalSearchRadius;
        this.targetReachedInteractions = targetReachedInteractions;
        this.onTravelling = onTravelling;
        this.shouldQueueForTarget = shouldQueueForTarget;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        super.start(level, mob, gameTime);
        this.currentTarget = null;
        this.ticksSinceReached = 0;
        this.currentState = null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        return true;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        if (this.currentTarget != null && mob.blockPosition().equals(this.currentTarget.pos())) {
            // At target
            this.ticksSinceReached++;
            OnTargetReachedInteraction interaction = this.targetReachedInteractions.get(this.currentState);
            if (interaction != null) {
                interaction.onReached(mob, this.currentTarget, this.ticksSinceReached);
            }
            
            if (this.ticksSinceReached >= 60) {
                this.currentTarget = null;
                this.ticksSinceReached = 0;
            }
        } else {
            // Find new target
            if (this.currentTarget == null) {
                this.findAndSetTarget(level, mob);
            }
            
            if (this.currentTarget != null) {
                mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
                    new WalkTarget(this.currentTarget.pos(), this.speedModifier, 1));
                mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, 
                    new BlockPosTracker(this.currentTarget.pos()));
                this.onTravelling.accept(mob);
            }
        }
    }

    private void findAndSetTarget(ServerLevel level, PathfinderMob mob) {
        BlockPos mobPos = mob.blockPosition();
        boolean hasItem = !this.inventory.isEmpty();
        
        List<BlockPos> candidates = BlockPos.betweenClosedStream(
            mobPos.offset(-horizontalSearchRadius, -verticalSearchRadius, -horizontalSearchRadius),
            mobPos.offset(horizontalSearchRadius, verticalSearchRadius, horizontalSearchRadius)
        ).map(BlockPos::immutable).toList();
        
        for (BlockPos pos : candidates) {
            BlockState state = level.getBlockState(pos);
            
            if (hasItem) {
                // Looking for destination
                if (this.destinationBlockPredicate.test(state)) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Container container) {
                        TransportItemTarget target = new TransportItemTarget(pos, container, be);
                        if (!this.shouldQueueForTarget.test(target)) {
                            this.currentTarget = target;
                            this.currentState = ContainerInteractionState.PLACE_ITEM;
                            this.ticksSinceReached = 0;
                            return;
                        }
                    }
                }
            } else {
                // Looking for source
                if (this.sourceBlockPredicate.test(state)) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Container container) {
                        if (!container.isEmpty()) {
                            TransportItemTarget target = new TransportItemTarget(pos, container, be);
                            if (!this.shouldQueueForTarget.test(target)) {
                                this.currentTarget = target;
                                this.currentState = ContainerInteractionState.PICKUP_ITEM;
                                this.ticksSinceReached = 0;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.currentTarget = null;
        this.ticksSinceReached = 0;
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    public enum ContainerInteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM
    }

    @FunctionalInterface
    public interface OnTargetReachedInteraction {
        void onReached(PathfinderMob mob, TransportItemTarget target, int ticksSinceReached);
    }

    public record TransportItemTarget(BlockPos pos, Container container, @Nullable BlockEntity blockEntity) {
    }
}
