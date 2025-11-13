package org.xiyu.yee.copper_friend_backport.coppergolem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.CopperGolemConfig;
import org.xiyu.yee.copper_friend_backport.copper_chest.CopperChestBlockEntity;
import org.xiyu.yee.copper_friend_backport.coppergolem.behavior.TransportItemsBetweenContainers;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockTags;
import org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CopperGolemAi {
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 1.5F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final int TRANSPORT_ITEM_HORIZONTAL_SEARCH_RADIUS = 32;
    private static final int TRANSPORT_ITEM_VERTICAL_SEARCH_RADIUS = 8;
    private static final int TICK_TO_START_ON_REACHED_INTERACTION = 1;
    private static final int TICK_TO_PLAY_ON_REACHED_SOUND = 9;
    private static final Predicate<BlockState> TRANSPORT_ITEM_SOURCE_BLOCK = blockState -> blockState.is(ModBlockTags.COPPER_CHESTS);
    private static final Predicate<BlockState> TRANSPORT_ITEM_DESTINATION_BLOCK = blockState -> blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST);
    private static final ImmutableList<SensorType<? extends Sensor<? super CopperGolem>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY
    );
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.GAZE_COOLDOWN_TICKS,
            ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(),
            ModMemoryModules.VISITED_BLOCK_POSITIONS.get(),
            ModMemoryModules.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS.get(),
            MemoryModuleType.DOORS_TO_CLOSE
    );

    public static Brain.Provider<CopperGolem> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<CopperGolem> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(CopperGolem copperGolem) {
        copperGolem.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    private static void initCoreActivity(Brain<CopperGolem> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new AnimalPanic(CopperGolemConfig.getPanicSpeedMultiplier()),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        InteractWithDoor.create(),
                        new ModCountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS,"GAZE_COOLDOWN_TICKS"),
                        new ModCountDownCooldownTicks(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(),"TRANSPORT_ITEMS_COOLDOWN_TICKS")
                )
        );
    }

    private static void initIdleActivity(Brain<CopperGolem> brain) {
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(
                                0,
                                new TransportItemsBetweenContainers(
                                        CopperGolemConfig.getIdleSpeedMultiplier(),
                                        TRANSPORT_ITEM_SOURCE_BLOCK,
                                        TRANSPORT_ITEM_DESTINATION_BLOCK,
                                        CopperGolemConfig.getTransportHorizontalSearchRadius(),
                                        CopperGolemConfig.getTransportVerticalSearchRadius(),
                                        getTargetReachedInteractions(),
                                        onTravelling(),
                                        shouldQueueForTarget()
                                )
                        ),
                        Pair.of(1, SetEntityLookTargetSometimes.create(EntityType.PLAYER, CopperGolemConfig.getLookAtPlayerDistance(), UniformInt.of(CopperGolemConfig.getLookAtPlayerMinDuration(), CopperGolemConfig.getLookAtPlayerMaxDuration()))),
                        Pair.of(
                                2,
                                new RunOne<>(
                                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_PRESENT),
                                        ImmutableList.of(Pair.of(RandomStroll.stroll(CopperGolemConfig.getRandomStrollSpeed(), CopperGolemConfig.getRandomStrollMinDistance(), CopperGolemConfig.getRandomStrollMaxDistance()), 1), Pair.of(new DoNothing(CopperGolemConfig.getDoNothingMinDuration(), CopperGolemConfig.getDoNothingMaxDuration()), 1))
                                )
                        )
                )
        );
    }

    private static Map<TransportItemsBetweenContainers.ContainerInteractionState, TransportItemsBetweenContainers.OnTargetReachedInteraction> getTargetReachedInteractions() {
        return Map.of(
                TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_ITEM,
                onReachedTargetInteraction(CopperGolemState.GETTING_ITEM, ModSoundEvents.COPPER_GOLEM_ITEM_GET.get()),
                TransportItemsBetweenContainers.ContainerInteractionState.PICKUP_NO_ITEM,
                onReachedTargetInteraction(CopperGolemState.GETTING_NO_ITEM, ModSoundEvents.COPPER_GOLEM_ITEM_NO_GET.get()),
                TransportItemsBetweenContainers.ContainerInteractionState.PLACE_ITEM,
                onReachedTargetInteraction(CopperGolemState.DROPPING_ITEM, ModSoundEvents.COPPER_GOLEM_ITEM_DROP.get()),
                TransportItemsBetweenContainers.ContainerInteractionState.PLACE_NO_ITEM,
                onReachedTargetInteraction(CopperGolemState.DROPPING_NO_ITEM, ModSoundEvents.COPPER_GOLEM_ITEM_NO_DROP.get())
        );
    }

    public static void incrementOpeners(ContainerOpenersCounter counter, Entity entity, Level level, BlockPos pos, BlockState blockState) {
        int oldCount = counter.openCount++;
        if (oldCount == 0) {
            counter.onOpen(level, pos, blockState);
            level.gameEvent(entity, GameEvent.CONTAINER_OPEN, pos);
            // Don't schedule recheck here - let the golem control when to close
            // ContainerOpenersCounter.scheduleRecheck(level, pos, blockState);
        }
        // Notify clients about the new open count (triggers animation)
        counter.openerCountChanged(level, pos, blockState, oldCount, counter.openCount);
    }
    
    public static void decrementOpeners(ContainerOpenersCounter counter, Entity entity, Level level, BlockPos pos, BlockState blockState) {
        int oldCount = counter.openCount--;
        if (counter.openCount == 0) {
            counter.onClose(level, pos, blockState);
            level.gameEvent(entity, GameEvent.CONTAINER_CLOSE, pos);
        }
        // Notify clients about the new open count (triggers animation)
        counter.openerCountChanged(level, pos, blockState, oldCount, counter.openCount);
    }

    private static TransportItemsBetweenContainers.OnTargetReachedInteraction onReachedTargetInteraction(
            CopperGolemState copperGolemState, @Nullable SoundEvent soundEvent
    ) {
        return (pathfinderMob, transportItemTarget, integer) -> {
            if (pathfinderMob instanceof CopperGolem copperGolem) {
                Container container = transportItemTarget.container();
                if (integer == CopperGolemConfig.getTickToStartInteraction()) {
                    // Get the actual BlockEntity to open
                    // For double chests, container is CompoundContainer, but we need the BlockEntity
                    BlockEntity blockEntity = transportItemTarget.blockEntity();
                    
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        if (!chest.isRemoved()) {
                            Level level = chest.getLevel();
                            BlockPos pos = chest.getBlockPos();
                            BlockState state = chest.getBlockState();
                            
                            // Special handling for copper chests - they handle double chests internally
                            if (chest instanceof CopperChestBlockEntity copperChest) {
                                copperChest.onEntityOpen(level, pos, state);
                            } else {
                                // For vanilla chests, use the standard opener system
                                incrementOpeners(chest.openersCounter, copperGolem, level, pos, state);
                            }
                        }
                    }
                    copperGolem.setOpenedChestPos(transportItemTarget.pos());
                    copperGolem.setState(copperGolemState);
                }

                if (integer == CopperGolemConfig.getTickToPlaySound() && soundEvent != null) {
                    copperGolem.playSound(soundEvent);
                }
                if (integer == 60) {
                    // Get the actual BlockEntity to close
                    BlockEntity blockEntity = transportItemTarget.blockEntity();
                    
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        BlockPos pos = chest.getBlockPos();
                        Level level = chest.getLevel();
                        BlockState state = chest.getBlockState();
                        
                        // Only close if this is the chest the golem actually opened
                        // For double chests, only close the main chest (the one stored in openedChestPos)
                        if (copperGolem.openedChestPos != null && copperGolem.openedChestPos.equals(pos)) {
                            if (!chest.isRemoved()) {
                                // Special handling for copper chests - they handle double chests internally
                                if (chest instanceof CopperChestBlockEntity copperChest) {
                                    copperChest.onEntityClose(level, pos, state);
                                } else {
                                    // For vanilla chests, use the standard opener system
                                    ContainerOpenersCounter openersCounter = chest.openersCounter;
                                    decrementOpeners(openersCounter, copperGolem, level, pos, state);
                                }
                            }
                        }
                    }
                    copperGolem.clearOpenedChestPos();
                }
            }
        };
    }

    private static Consumer<PathfinderMob> onTravelling() {
        return pathfinderMob -> {
            if (pathfinderMob instanceof CopperGolem copperGolem) {
                copperGolem.clearOpenedChestPos();
                copperGolem.setState(CopperGolemState.IDLE);
            }
        };
    }

    private static Predicate<TransportItemsBetweenContainers.TransportItemTarget> shouldQueueForTarget() {
        return transportItemTarget -> {
            boolean b = transportItemTarget.blockEntity() instanceof ChestBlockEntity chestBlockEntity && !(ChestBlockEntity.getOpenCount(chestBlockEntity.getLevel(), chestBlockEntity.getBlockPos()) <= 0);
            return b;
        };
    }
}
