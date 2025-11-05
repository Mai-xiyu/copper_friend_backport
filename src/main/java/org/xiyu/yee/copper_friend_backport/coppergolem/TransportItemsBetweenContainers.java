package org.xiyu.yee.copper_friend_backport.coppergolem;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Backport of TransportItemsBetweenContainers for Minecraft 1.20.1
 * This is a simplified version that provides basic item transport behavior.
 */
public class TransportItemsBetweenContainers extends Behavior<PathfinderMob> {
    private final float speedModifier;
    private final Predicate<BlockState> sourceBlockPredicate;
    private final Predicate<BlockState> destinationBlockPredicate;
    private final int horizontalSearchRadius;
    private final int verticalSearchRadius;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> targetReachedInteractions;
    private final Consumer<PathfinderMob> onTravelling;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;

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
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT
        ));
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
        // Simplified check - just verify we're not on cooldown
        return true;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        // Simplified behavior - just make the mob wander
        // Full implementation would search for containers and create transport tasks
        onTravelling.accept(mob);
    }

    public enum ContainerInteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM
    }

    @FunctionalInterface
    public interface OnTargetReachedInteraction {
        void onTargetReached(PathfinderMob mob, TransportItemTarget target, int tick);
    }

    public record TransportItemTarget(GlobalPos pos, Container container, @Nullable BlockEntity blockEntity) {
    }
}
