package org.xiyu.yee.copper_friend_backport.coppergolem.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.ContainerIterator;
import org.xiyu.yee.copper_friend_backport.GroundPathNavigationMixinInterface;
import org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules;

public class TransportItemsBetweenContainers extends Behavior<PathfinderMob> {
    private final float speedModifier;
    private final int horizontalSearchDistance;
    private final int verticalSearchDistance;
    private final Predicate<BlockState> sourceBlockType;
    private final Predicate<BlockState> destinationBlockType;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    private final Consumer<PathfinderMob> onStartTravelling;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions;
    @Nullable
    private TransportItemsBetweenContainers.TransportItemTarget target = null;
    private TransportItemState state;
    @Nullable
    private TransportItemsBetweenContainers.ContainerInteractionState interactionState;
    private int ticksSinceReachingTarget;

    public TransportItemsBetweenContainers(
            float speedModifier,
            Predicate<BlockState> sourceBlockType,
            Predicate<BlockState> destinationBlockType,
            int horizontalSearchDistance,
            int verticalSearchDistance,
            Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions,
            Consumer<PathfinderMob> onStartTravelling,
            Predicate<TransportItemTarget> shouldQueueForTarget
    ) {
        super(
                ImmutableMap.of(
                        ModMemoryModules.VISITED_BLOCK_POSITIONS.get(),
                        MemoryStatus.REGISTERED,
                        ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get(),
                        MemoryStatus.REGISTERED,
                        ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(),
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.IS_PANICKING,
                        MemoryStatus.VALUE_ABSENT
                )
        );
        this.speedModifier = speedModifier;
        this.sourceBlockType = sourceBlockType;
        this.destinationBlockType = destinationBlockType;
        this.horizontalSearchDistance = horizontalSearchDistance;
        this.verticalSearchDistance = verticalSearchDistance;
        this.onStartTravelling = onStartTravelling;
        this.shouldQueueForTarget = shouldQueueForTarget;
        this.onTargetInteractionActions = onTargetInteractionActions;
        this.state = TransportItemState.TRAVELLING;
        System.out.println("horizontalSearchDistance = " + horizontalSearchDistance);
        System.out.println("verticalSearchDistance = " + verticalSearchDistance);
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        if (pathfinderMob.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
            ((GroundPathNavigationMixinInterface)groundPathNavigation).copper_friend_backport$set(true);
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        boolean b = !pathfinderMob.isLeashed();
        return b;
    }

    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return pathfinderMob.getBrain().getMemory(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get()).isEmpty()
                && !isPanicking(pathfinderMob)
                && !pathfinderMob.isLeashed();
    }

    public boolean isPanicking(PathfinderMob pathfinderMob) {
        if (pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING)) {
            return pathfinderMob.getBrain().getMemory(MemoryModuleType.IS_PANICKING).isPresent();
        } else {
            for (WrappedGoal wrappedGoal : pathfinderMob.goalSelector.getAvailableGoals()) {
                if (wrappedGoal.isRunning() && wrappedGoal.getGoal() instanceof PanicGoal) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        boolean bl = this.updateInvalidTarget(serverLevel, pathfinderMob);
        if (this.target == null) {
            this.stop(serverLevel, pathfinderMob, l);
        } else if (!bl) {
            if (this.state.equals(TransportItemState.QUEUING)) {
                this.onQueuingForTarget(this.target, serverLevel, pathfinderMob);
            }

            if (this.state.equals(TransportItemState.TRAVELLING)) {
                this.onTravelToTarget(this.target, serverLevel, pathfinderMob);
            }

            if (this.state.equals(TransportItemState.INTERACTING)) {
                this.onReachedTarget(this.target, serverLevel, pathfinderMob);
            }
        }
    }

    private boolean updateInvalidTarget(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        if (!this.hasValidTarget(serverLevel, pathfinderMob)) {
            this.stopTargetingCurrentTarget(pathfinderMob);
            Optional<TransportItemTarget> optional = this.getTransportTarget(serverLevel, pathfinderMob);
            if (optional.isPresent()) {
                this.target = optional.get();
                this.onStartTravelling(pathfinderMob);
                this.setVisitedBlockPos(pathfinderMob, serverLevel, this.target.pos);
                return true;
            } else {
                this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
                return true;
            }
        } else {
            return false;
        }
    }

    private void onQueuingForTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (!this.isAnotherMobInteractingWithTarget(transportItemTarget, level)) {
            this.resumeTravelling(pathfinderMob);
        }
    }

    protected void onTravelToTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (this.isWithinTargetDistance(3.0, transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob))
                && this.isAnotherMobInteractingWithTarget(transportItemTarget, level)) {
            this.startQueuing(pathfinderMob);
        } else if (this.isWithinTargetDistance(getInteractionRange(pathfinderMob), transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob))) {
            this.startOnReachedTargetInteraction(transportItemTarget, pathfinderMob);
        } else {
            this.walkTowardsTarget(pathfinderMob);
        }
    }

    private Vec3 getCenterPos(PathfinderMob pathfinderMob) {
        return this.setMiddleYPosition(pathfinderMob, pathfinderMob.position());
    }

    protected void onReachedTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (!this.isWithinTargetDistance(2.0, transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob))) {
            this.onStartTravelling(pathfinderMob);
        } else {
            this.ticksSinceReachingTarget++;
            this.onTargetInteraction(transportItemTarget, pathfinderMob);
            if (this.ticksSinceReachingTarget >= 60) {
                this.doReachedTargetInteraction(
                        pathfinderMob,
                        transportItemTarget.container,
                        this::pickUpItems,
                        (pathfinderMob2, container) -> this.stopTargetingCurrentTarget(pathfinderMob),
                        this::putDownItem,
                        (pathfinderMob2, container) -> this.stopTargetingCurrentTarget(pathfinderMob)
                );
                this.onStartTravelling(pathfinderMob);
            }
        }
    }

    private void startQueuing(PathfinderMob pathfinderMob) {
        this.stopInPlace(pathfinderMob);
        this.setTransportingState(TransportItemState.QUEUING);
    }

    private void resumeTravelling(PathfinderMob pathfinderMob) {
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.walkTowardsTarget(pathfinderMob);
    }

    private void walkTowardsTarget(PathfinderMob pathfinderMob) {
        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories(pathfinderMob, this.target.pos, this.speedModifier, 0);
        }
    }

    private void startOnReachedTargetInteraction(TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        this.doReachedTargetInteraction(
                pathfinderMob,
                transportItemTarget.container,
                this.onReachedInteraction(ContainerInteractionState.PICKUP_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PICKUP_NO_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PLACE_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PLACE_NO_ITEM)
        );
        this.setTransportingState(TransportItemState.INTERACTING);
    }

    private void onStartTravelling(PathfinderMob pathfinderMob) {
        this.onStartTravelling.accept(pathfinderMob);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private BiConsumer<PathfinderMob, Container> onReachedInteraction(ContainerInteractionState containerInteractionState) {
        return (pathfinderMob, container) -> this.setInteractionState(containerInteractionState);
    }

    private void setTransportingState(TransportItemState transportItemState) {
        this.state = transportItemState;
    }

    private void setInteractionState(ContainerInteractionState containerInteractionState) {
        this.interactionState = containerInteractionState;
    }

    private void onTargetInteraction(TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        pathfinderMob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(transportItemTarget.pos));
        this.stopInPlace(pathfinderMob);
        if (this.interactionState != null) {
            Optional.ofNullable((OnTargetReachedInteraction)this.onTargetInteractionActions.get(this.interactionState))
                    .ifPresent(onTargetReachedInteraction -> onTargetReachedInteraction.accept(pathfinderMob, transportItemTarget, this.ticksSinceReachingTarget));
        }
    }

    private void doReachedTargetInteraction(
            PathfinderMob pathfinderMob,
            Container container,
            BiConsumer<PathfinderMob, Container> biConsumer,
            BiConsumer<PathfinderMob, Container> biConsumer2,
            BiConsumer<PathfinderMob, Container> biConsumer3,
            BiConsumer<PathfinderMob, Container> biConsumer4
    ) {
        if (isPickingUpItems(pathfinderMob)) {
            if (matchesGettingItemsRequirement(container)) {
                biConsumer.accept(pathfinderMob, container);
            } else {
                biConsumer2.accept(pathfinderMob, container);
            }
        } else if (matchesLeavingItemsRequirement(pathfinderMob, container)) {
            biConsumer3.accept(pathfinderMob, container);
        } else {
            biConsumer4.accept(pathfinderMob, container);
        }
    }

    private Optional<TransportItemTarget> getTransportTarget(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        AABB aABB = this.getTargetSearchArea(pathfinderMob);
        Set<GlobalPos> set = getVisitedPositions(pathfinderMob);
        Set<GlobalPos> set2 = getUnreachablePositions(pathfinderMob);
        List<ChunkPos> list = ChunkPos.rangeClosed(
                        new ChunkPos(pathfinderMob.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(pathfinderMob), 16) + 1
                )
                .toList();
        TransportItemTarget transportItemTarget = null;
        double d = Float.MAX_VALUE;

        for (ChunkPos chunkPos : list) {
            LevelChunk levelChunk = serverLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (levelChunk != null) {
                for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                    if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                        double e = chestBlockEntity.getBlockPos().distToCenterSqr(pathfinderMob.position());
                        if (e < d) {
                            TransportItemTarget transportItemTarget2 = this.isTargetValidToPick(
                                    pathfinderMob, serverLevel, chestBlockEntity, set, set2, aABB
                            );
                            if (transportItemTarget2 != null) {
                                transportItemTarget = transportItemTarget2;
                                d = e;
                            }
                        }
                    }
                }
            }
        }

        return transportItemTarget == null ? Optional.empty() : Optional.of(transportItemTarget);
    }

    @Nullable
    private TransportItemsBetweenContainers.TransportItemTarget isTargetValidToPick(
            PathfinderMob pathfinderMob, Level level, BlockEntity blockEntity, Set<GlobalPos> set, Set<GlobalPos> set2, AABB aABB
    ) {
        BlockPos blockPos = blockEntity.getBlockPos();
        boolean bl = aABB.contains(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (!bl) {
            return null;
        } else {
            TransportItemTarget transportItemTarget = TransportItemTarget.tryCreatePossibleTarget(
                    blockEntity, level
            );
            if (transportItemTarget == null) {
                return null;
            } else {
                boolean bl2 = this.isWantedBlock(pathfinderMob, transportItemTarget.state)
                        && !this.isPositionAlreadyVisited(set, set2, transportItemTarget, level)
                        && !this.isContainerLocked(transportItemTarget);
                return bl2 ? transportItemTarget : null;
            }
        }
    }

    private boolean isContainerLocked(TransportItemTarget transportItemTarget) {
        return transportItemTarget.blockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity && isLocked(baseContainerBlockEntity);
    }
    public boolean isLocked(BaseContainerBlockEntity containerBlock) {
        return !containerBlock.lockKey.equals(LockCode.NO_LOCK);
    }

    private boolean hasValidTarget(Level level, PathfinderMob pathfinderMob) {
        boolean bl = this.target != null && this.isWantedBlock(pathfinderMob, this.target.state) && this.targetHasNotChanged(level, this.target);
        if (bl && !this.isTargetBlocked(level, this.target)) {
            if (!this.state.equals(TransportItemState.TRAVELLING)) {
                return true;
            }

            if (this.hasValidTravellingPath(level, this.target, pathfinderMob)) {
                return true;
            }

            this.markVisitedBlockPosAsUnreachable(pathfinderMob, level, this.target.pos);
        }

        return false;
    }

    private boolean hasValidTravellingPath(Level level, TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        Path path = pathfinderMob.getNavigation().getPath() == null
                ? pathfinderMob.getNavigation().createPath(transportItemTarget.pos, 0)
                : pathfinderMob.getNavigation().getPath();
        Vec3 vec3 = this.getPositionToReachTargetFrom(path, pathfinderMob);
        boolean bl = this.isWithinTargetDistance(getInteractionRange(pathfinderMob), transportItemTarget, level, pathfinderMob, vec3);
        boolean bl2 = path == null && !bl;
        return bl2 || this.targetIsReachableFromPosition(level, bl, vec3, transportItemTarget, pathfinderMob);
    }

    private Vec3 getPositionToReachTargetFrom(@Nullable Path path, PathfinderMob pathfinderMob) {
        boolean bl = path == null || path.getEndNode() == null;
        Vec3 vec3 = bl ? pathfinderMob.position() : Vec3.atBottomCenterOf(path.getEndNode().asBlockPos());
        return this.setMiddleYPosition(pathfinderMob, vec3);
    }

    private Vec3 setMiddleYPosition(PathfinderMob pathfinderMob, Vec3 vec3) {
        return vec3.add(0.0, pathfinderMob.getBoundingBox().getYsize() / 2.0, 0.0);
    }

    private boolean isTargetBlocked(Level level, TransportItemTarget transportItemTarget) {
        return ChestBlock.isChestBlockedAt(level, transportItemTarget.pos);
    }

    private boolean targetHasNotChanged(Level level, TransportItemTarget transportItemTarget) {
        return transportItemTarget.blockEntity.equals(level.getBlockEntity(transportItemTarget.pos));
    }

    private Stream<TransportItemTarget> getConnectedTargets(
            TransportItemTarget transportItemTarget, Level level
    ) {
        if (transportItemTarget.state.getOptionalValue(ChestBlock.TYPE).orElse(ChestType.SINGLE) != ChestType.SINGLE) {
            TransportItemTarget transportItemTarget2 = TransportItemTarget.tryCreatePossibleTarget(
                    transportItemTarget.pos.relative(ChestBlock.getConnectedDirection(transportItemTarget.state)), level
            );
            return transportItemTarget2 != null ? Stream.of(transportItemTarget, transportItemTarget2) : Stream.of(transportItemTarget);
        } else {
            return Stream.of(transportItemTarget);
        }
    }

    private AABB getTargetSearchArea(PathfinderMob pathfinderMob) {
        int i = this.getHorizontalSearchDistance(pathfinderMob);
        return new AABB(pathfinderMob.blockPosition()).inflate(i, this.getVerticalSearchDistance(pathfinderMob), i);
    }

    private int getHorizontalSearchDistance(PathfinderMob pathfinderMob) {
        return pathfinderMob.isPassenger() ? 1 : this.horizontalSearchDistance;
    }

    private int getVerticalSearchDistance(PathfinderMob pathfinderMob) {
        return pathfinderMob.isPassenger() ? 1 : this.verticalSearchDistance;
    }

    private static Set<GlobalPos> getVisitedPositions(PathfinderMob pathfinderMob) {
        return (Set<GlobalPos>)pathfinderMob.getBrain().getMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get()).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathfinderMob pathfinderMob) {
        return (Set<GlobalPos>)pathfinderMob.getBrain().getMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get()).orElse(Set.of());
    }

    private boolean isPositionAlreadyVisited(
            Set<GlobalPos> set, Set<GlobalPos> set2, TransportItemTarget transportItemTarget, Level level
    ) {
        return this.getConnectedTargets(transportItemTarget, level)
                .map(transportItemTargetx -> GlobalPos.of(level.dimension(), transportItemTargetx.pos))
                .anyMatch(globalPos -> set.contains(globalPos) || set2.contains(globalPos));
    }

    private static boolean hasFinishedPath(PathfinderMob pathfinderMob) {
        return pathfinderMob.getNavigation().getPath() != null && pathfinderMob.getNavigation().getPath().isDone();
    }

    protected void setVisitedBlockPos(PathfinderMob pathfinderMob, Level level, BlockPos blockPos) {
        Set<GlobalPos> set = new HashSet(getVisitedPositions(pathfinderMob));
        set.add(GlobalPos.of(level.dimension(), blockPos));
        if (set.size() > 10) {
            this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
        } else {
            pathfinderMob.getBrain().setMemoryWithExpiry(ModMemoryModules.VISITED_BLOCK_POSITIONS.get(), set, 6000L);
        }
    }

    protected void markVisitedBlockPosAsUnreachable(PathfinderMob pathfinderMob, Level level, BlockPos blockPos) {
        Set<GlobalPos> set = new HashSet(getVisitedPositions(pathfinderMob));
        set.remove(GlobalPos.of(level.dimension(), blockPos));
        Set<GlobalPos> set2 = new HashSet(getUnreachablePositions(pathfinderMob));
        set2.add(GlobalPos.of(level.dimension(), blockPos));
        if (set2.size() > 50) {
            this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
        } else {
            pathfinderMob.getBrain().setMemoryWithExpiry(ModMemoryModules.VISITED_BLOCK_POSITIONS.get(), set, 6000L);
            pathfinderMob.getBrain().setMemoryWithExpiry(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get(), set2, 6000L);
        }
    }

    private boolean isWantedBlock(PathfinderMob pathfinderMob, BlockState blockState) {
        return isPickingUpItems(pathfinderMob) ? this.sourceBlockType.test(blockState) : this.destinationBlockType.test(blockState);
    }

    private static double getInteractionRange(PathfinderMob pathfinderMob) {
        return hasFinishedPath(pathfinderMob) ? 1.0 : 0.5;
    }

    private boolean isWithinTargetDistance(
            double d, TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob, Vec3 vec3
    ) {
        AABB aABB = pathfinderMob.getBoundingBox();
        AABB aABB2 = AABB.ofSize(vec3, aABB.getXsize(), aABB.getYsize(), aABB.getZsize());
        return transportItemTarget.state
                .getCollisionShape(level, transportItemTarget.pos)
                .bounds()
                .inflate(d, 0.5, d)
                .move(transportItemTarget.pos)
                .intersects(aABB2);
    }

    private boolean targetIsReachableFromPosition(
            Level level, boolean bl, Vec3 vec3, TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob
    ) {
        return bl && this.canSeeAnyTargetSide(transportItemTarget, level, pathfinderMob, vec3);
    }

    private boolean canSeeAnyTargetSide(
            TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob, Vec3 vec3
    ) {
        Vec3 vec32 = transportItemTarget.pos.getCenter();
        return Direction.stream()
                .map(direction -> vec32.add(0.5 * direction.getStepX(), 0.5 * direction.getStepY(), 0.5 * direction.getStepZ()))
                .map(vec32x -> level.clip(new ClipContext(vec3, vec32x, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pathfinderMob)))
                .anyMatch(blockHitResult -> blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult.getBlockPos().equals(transportItemTarget.pos));
    }

    private boolean isAnotherMobInteractingWithTarget(TransportItemTarget transportItemTarget, Level level) {
        return this.getConnectedTargets(transportItemTarget, level).anyMatch(this.shouldQueueForTarget);
    }

    private static boolean isPickingUpItems(PathfinderMob pathfinderMob) {
        return pathfinderMob.getMainHandItem().isEmpty();
    }

    private static boolean matchesGettingItemsRequirement(Container container) {
        return !container.isEmpty();
    }

    private static boolean matchesLeavingItemsRequirement(PathfinderMob pathfinderMob, Container container) {
        return container.isEmpty() || hasItemMatchingHandItem(pathfinderMob, container);
    }

    private static boolean hasItemMatchingHandItem(PathfinderMob pathfinderMob, Container container) {
        ItemStack itemStack = pathfinderMob.getMainHandItem();
        ContainerIterator iterator = new ContainerIterator(container);
        while (iterator.hasNext()) {
            ItemStack itemStack2 = iterator.next();
            if (ItemStack.isSameItem(itemStack2, itemStack)) {
                return true;
            }
        }

        return false;
    }

    private void pickUpItems(PathfinderMob pathfinderMob, Container container) {
        pathfinderMob.setItemSlot(EquipmentSlot.MAINHAND, pickupItemFromContainer(container));
        pathfinderMob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        container.setChanged();
        this.clearMemoriesAfterMatchingTargetFound(pathfinderMob);
    }

    private void putDownItem(PathfinderMob pathfinderMob, Container container) {
        ItemStack itemStack = addItemsToContainer(pathfinderMob, container);
        container.setChanged();
        pathfinderMob.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        if (itemStack.isEmpty()) {
            this.clearMemoriesAfterMatchingTargetFound(pathfinderMob);
        } else {
            this.stopTargetingCurrentTarget(pathfinderMob);
        }
    }

    private static ItemStack pickupItemFromContainer(Container container) {
        int i = 0;

        ContainerIterator iterator = new ContainerIterator(container);
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            if (!itemStack.isEmpty()) {
                int j = Math.min(itemStack.getCount(), 16);
                return container.removeItem(i, j);
            }

            i++;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack addItemsToContainer(PathfinderMob pathfinderMob, Container container) {
        int i = 0;
        ItemStack itemStack = pathfinderMob.getMainHandItem();
        ContainerIterator iterator = new ContainerIterator(container);
        while (iterator.hasNext()) {
            ItemStack itemStack2 = iterator.next();
            if (itemStack2.isEmpty()) {
                container.setItem(i, itemStack);
                return ItemStack.EMPTY;
            }

            if (ItemStack.isSameItemSameTags(itemStack2, itemStack) && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
                int j = itemStack2.getMaxStackSize() - itemStack2.getCount();
                int k = Math.min(j, itemStack.getCount());
                itemStack2.setCount(itemStack2.getCount() + k);
                itemStack.setCount(itemStack.getCount() - j);
                container.setItem(i, itemStack2);
                if (itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }

            i++;
        }

        return itemStack;
    }

    protected void stopTargetingCurrentTarget(PathfinderMob pathfinderMob) {
        this.ticksSinceReachingTarget = 0;
        this.target = null;
        pathfinderMob.getNavigation().stop();
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void clearMemoriesAfterMatchingTargetFound(PathfinderMob pathfinderMob) {
        this.stopTargetingCurrentTarget(pathfinderMob);
        pathfinderMob.getBrain().eraseMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get());
        pathfinderMob.getBrain().eraseMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get());
    }

    private void enterCooldownAfterNoMatchingTargetFound(PathfinderMob pathfinderMob) {
        this.stopTargetingCurrentTarget(pathfinderMob);
        pathfinderMob.getBrain().setMemory(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), 140);
        pathfinderMob.getBrain().eraseMemory(ModMemoryModules.VISITED_BLOCK_POSITIONS.get());
        pathfinderMob.getBrain().eraseMemory(ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get());
    }

    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.onStartTravelling(pathfinderMob);
        if (pathfinderMob.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
            ((GroundPathNavigationMixinInterface) groundPathNavigation).copper_friend_backport$set(false);
        }
    }

    private void stopInPlace(PathfinderMob pathfinderMob) {
        pathfinderMob.getNavigation().stop();
        pathfinderMob.setXxa(0.0F);
        pathfinderMob.setYya(0.0F);
        pathfinderMob.setSpeed(0.0F);
        pathfinderMob.setDeltaMovement(0.0, pathfinderMob.getDeltaMovement().y, 0.0);
    }

    public static enum ContainerInteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM;
    }

    @FunctionalInterface
    public interface OnTargetReachedInteraction extends TriConsumer<PathfinderMob, TransportItemTarget, Integer> {
    }

    public static enum TransportItemState {
        TRAVELLING,
        QUEUING,
        INTERACTING;
    }

    public record TransportItemTarget(BlockPos pos, Container container, BlockEntity blockEntity, BlockState state) {

        @Nullable
        public static TransportItemsBetweenContainers.TransportItemTarget tryCreatePossibleTarget(BlockEntity blockEntity, Level level) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = blockEntity.getBlockState();
            Container container = getBlockEntityContainer(blockEntity, blockState, level, blockPos);
            return container != null ? new TransportItemTarget(blockPos, container, blockEntity, blockState) : null;
        }

        @Nullable
        public static TransportItemsBetweenContainers.TransportItemTarget tryCreatePossibleTarget(BlockPos blockPos, Level level) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            return blockEntity == null ? null : tryCreatePossibleTarget(blockEntity, level);
        }

        @Nullable
        private static Container getBlockEntityContainer(BlockEntity blockEntity, BlockState blockState, Level level, BlockPos blockPos) {
            if (blockState.getBlock() instanceof ChestBlock chestBlock) {
                return ChestBlock.getContainer(chestBlock, blockState, level, blockPos, false);
            } else {
                return blockEntity instanceof Container container ? container : null;
            }
        }
    }
}
