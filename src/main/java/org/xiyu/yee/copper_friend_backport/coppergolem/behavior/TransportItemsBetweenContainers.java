package org.xiyu.yee.copper_friend_backport.coppergolem.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TransportItemsBetweenContainers extends Behavior<PathfinderMob> {
    private static final int TARGET_INTERACTION_TIME = 60;
    private static final int VISITED_POSITIONS_MEMORY_TIME = 6000;
    private static final int MAX_VISITED_POSITIONS = 10;
    private static final int IDLE_COOLDOWN = 140;
    
    private final float speedModifier;
    private final Predicate<BlockState> sourceBlockPredicate;
    private final Predicate<BlockState> destinationBlockPredicate;
    private final int horizontalSearchRadius;
    private final int verticalSearchRadius;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> targetReachedInteractions;
    private final Consumer<PathfinderMob> onTravelling;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    
    @Nullable
    private TransportItemTarget currentTarget;
    private int ticksSinceReached;
    @Nullable
    private ContainerInteractionState interactionState;

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
            ModMemoryModules.VISITED_BLOCK_POSITIONS.get(), MemoryStatus.REGISTERED,
            ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get(), MemoryStatus.REGISTERED,
            ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT
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
        return !mob.isLeashed();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        return mob.getBrain().getMemory(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get()).isEmpty() 
            && mob.getBrain().getMemory(MemoryModuleType.IS_PANICKING).isEmpty() 
            && !mob.isLeashed();
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        super.start(level, mob, gameTime);
        this.currentTarget = null;
        this.ticksSinceReached = 0;
        this.interactionState = null;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        if (this.currentTarget != null && !isTargetValid(level, this.currentTarget, mob)) {
            markAsVisited(level, mob, this.currentTarget.pos());
            this.currentTarget = null;
            this.ticksSinceReached = 0;
        }
        
        if (this.currentTarget != null && mob.blockPosition().equals(this.currentTarget.pos())) {
            this.ticksSinceReached++;
            
            if (this.interactionState != null) {
                OnTargetReachedInteraction interaction = this.targetReachedInteractions.get(this.interactionState);
                if (interaction != null) {
                    interaction.onReached(mob, this.currentTarget, this.ticksSinceReached);
                }
            }
            
            if (this.ticksSinceReached == 30) {
                performItemTransfer(mob, this.currentTarget.container());
            }
            
            if (this.ticksSinceReached >= TARGET_INTERACTION_TIME) {
                clearMemoriesAfterMatchingTargetFound(mob);
            }
        } else {
            if (this.currentTarget == null) {
                findAndSetTarget(level, mob);
            }
            
            if (this.currentTarget != null) {
                mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
                    new WalkTarget(this.currentTarget.pos(), this.speedModifier, 1));
                mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, 
                    new BlockPosTracker(this.currentTarget.pos()));
                this.onTravelling.accept(mob);
            } else {
                enterCooldown(mob);
            }
        }
    }

    private void findAndSetTarget(ServerLevel level, PathfinderMob mob) {
        BlockPos mobPos = mob.blockPosition();
        boolean hasItem = !mob.getMainHandItem().isEmpty();
        
        Set<GlobalPos> visited = getVisitedPositions(mob);
        Set<GlobalPos> unreachable = getUnreachablePositions(mob);
        
        List<BlockPos> candidates = BlockPos.betweenClosedStream(
            mobPos.offset(-horizontalSearchRadius, -verticalSearchRadius, -horizontalSearchRadius),
            mobPos.offset(horizontalSearchRadius, verticalSearchRadius, horizontalSearchRadius)
        ).map(BlockPos::immutable).toList();
        
        TransportItemTarget bestTarget = null;
        double bestDistance = Double.MAX_VALUE;
        boolean foundMatchingItem = false;
        
        for (BlockPos pos : candidates) {
            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            
            if (visited.contains(globalPos) || unreachable.contains(globalPos)) {
                continue;
            }
            
            BlockState state = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);
            
            if (!(be instanceof Container container)) {
                continue;
            }
            
            boolean isSource = !hasItem && this.sourceBlockPredicate.test(state);
            boolean isDestination = hasItem && this.destinationBlockPredicate.test(state);
            
            if (!isSource && !isDestination) {
                continue;
            }
            
            TransportItemTarget target = new TransportItemTarget(pos, container, be);
            
            if (this.shouldQueueForTarget.test(target)) {
                continue;
            }
            
            if (isSource && container.isEmpty()) {
                continue;
            }
            
            if (isDestination) {
                ItemStack heldItem = mob.getMainHandItem();
                boolean hasMatchingItem = false;
                boolean hasEmptySlot = false;
                
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack slotStack = container.getItem(i);
                    if (slotStack.isEmpty()) {
                        hasEmptySlot = true;
                    } else if (ItemStack.isSameItemSameTags(slotStack, heldItem) && 
                              slotStack.getCount() < slotStack.getMaxStackSize()) {
                        hasMatchingItem = true;
                        break;
                    }
                }
                
                if (!hasMatchingItem && !hasEmptySlot) {
                    continue;
                }
                
                double distance = mobPos.distSqr(pos);
                if (hasMatchingItem && !foundMatchingItem) {
                    bestTarget = target;
                    bestDistance = distance;
                    foundMatchingItem = true;
                    this.interactionState = ContainerInteractionState.PLACE_ITEM;
                } else if (hasMatchingItem && foundMatchingItem && distance < bestDistance) {
                    bestTarget = target;
                    bestDistance = distance;
                    this.interactionState = ContainerInteractionState.PLACE_ITEM;
                } else if (!foundMatchingItem && hasEmptySlot && distance < bestDistance) {
                    bestTarget = target;
                    bestDistance = distance;
                    this.interactionState = ContainerInteractionState.PLACE_ITEM;
                }
            } else {
                double distance = mobPos.distSqr(pos);
                if (distance < bestDistance) {
                    bestTarget = target;
                    bestDistance = distance;
                    this.interactionState = ContainerInteractionState.PICKUP_ITEM;
                }
            }
        }
        
        if (bestTarget != null) {
            this.currentTarget = bestTarget;
            this.ticksSinceReached = 0;
        }
    }

    private void performItemTransfer(PathfinderMob mob, Container container) {
        if (this.interactionState == ContainerInteractionState.PICKUP_ITEM) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    ItemStack taken = container.removeItem(i, Math.min(16, stack.getCount()));
                    if (!taken.isEmpty()) {
                        mob.setItemSlot(EquipmentSlot.MAINHAND, taken);
                        mob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                        container.setChanged();
                        this.interactionState = ContainerInteractionState.PICKUP_ITEM;
                        return;
                    }
                }
            }
            this.interactionState = ContainerInteractionState.PICKUP_NO_ITEM;
        } else if (this.interactionState == ContainerInteractionState.PLACE_ITEM) {
            ItemStack heldItem = mob.getMainHandItem();
            if (!heldItem.isEmpty()) {
                ItemStack remaining = addItemsToContainer(heldItem, container);
                container.setChanged();
                mob.setItemSlot(EquipmentSlot.MAINHAND, remaining);
                
                if (remaining.isEmpty()) {
                    this.interactionState = ContainerInteractionState.PLACE_ITEM;
                } else if (remaining.getCount() < heldItem.getCount()) {
                    this.interactionState = ContainerInteractionState.PLACE_ITEM;
                } else {
                    this.interactionState = ContainerInteractionState.PLACE_NO_ITEM;
                }
            } else {
                this.interactionState = ContainerInteractionState.PLACE_NO_ITEM;
            }
        }
    }

    private ItemStack addItemsToContainer(ItemStack stack, Container container) {
        ItemStack remaining = stack.copy();
        
        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slotStack = container.getItem(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, remaining)) {
                int maxStack = Math.min(container.getMaxStackSize(), slotStack.getMaxStackSize());
                int canAdd = maxStack - slotStack.getCount();
                if (canAdd > 0) {
                    int toAdd = Math.min(canAdd, remaining.getCount());
                    slotStack.grow(toAdd);
                    remaining.shrink(toAdd);
                    container.setItem(i, slotStack);
                }
            }
        }
        
        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slotStack = container.getItem(i);
            if (slotStack.isEmpty()) {
                int maxStack = Math.min(container.getMaxStackSize(), remaining.getMaxStackSize());
                int toAdd = Math.min(maxStack, remaining.getCount());
                ItemStack toPlace = remaining.split(toAdd);
                container.setItem(i, toPlace);
            }
        }
        
        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    private boolean isTargetValid(ServerLevel level, TransportItemTarget target, PathfinderMob mob) {
        BlockState state = level.getBlockState(target.pos());
        boolean isCorrectType = mob.getMainHandItem().isEmpty() 
            ? this.sourceBlockPredicate.test(state) 
            : this.destinationBlockPredicate.test(state);
        
        return isCorrectType && target.blockEntity().equals(level.getBlockEntity(target.pos()));
    }

    private void markAsVisited(ServerLevel level, PathfinderMob mob, BlockPos pos) {
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        Set<GlobalPos> visited = new HashSet<>(getVisitedPositions(mob));
        visited.add(globalPos);
        
        if (visited.size() > MAX_VISITED_POSITIONS) {
            enterCooldown(mob);
        } else {
            mob.getBrain().setMemoryWithExpiry(
                ModMemoryModules.VISITED_BLOCK_POSITIONS.get(), 
                visited, 
                VISITED_POSITIONS_MEMORY_TIME
            );
        }
    }

    private void clearMemoriesAfterMatchingTargetFound(PathfinderMob mob) {
        this.currentTarget = null;
        this.ticksSinceReached = 0;
        this.interactionState = null;
        mob.getNavigation().stop();
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        mob.getBrain().eraseMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get());
        mob.getBrain().eraseMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get());
    }

    private void enterCooldown(PathfinderMob mob) {
        this.currentTarget = null;
        this.ticksSinceReached = 0;
        this.interactionState = null;
        mob.getNavigation().stop();
        mob.getBrain().setMemory(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), IDLE_COOLDOWN);
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        mob.getBrain().eraseMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get());
        mob.getBrain().eraseMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get());
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.onTravelling.accept(mob);
        mob.getNavigation().stop();
    }

    private static Set<GlobalPos> getVisitedPositions(PathfinderMob mob) {
        return mob.getBrain().getMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get()).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathfinderMob mob) {
        return mob.getBrain().getMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get()).orElse(Set.of());
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
