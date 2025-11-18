package org.xiyu.yee.copper_friend_backport.coppergolem;

import com.mojang.serialization.Dynamic;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.CopperGolemConfig;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModEntityDataSerializers;
import org.xiyu.yee.copper_friend_backport.registry.ModMemoryModules;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class CopperGolem extends AbstractGolem implements Shearable {
    public static final EquipmentSlot EQUIPMENT_SLOT_ANTENNA = EquipmentSlot.CHEST;
    private static final EntityDataAccessor<WeatheringCopper.WeatherState> DATA_WEATHER_STATE = SynchedEntityData.defineId(
            CopperGolem.class, ModEntityDataSerializers.WEATHERING_COPPER_STATE
    );
    private static final EntityDataAccessor<CopperGolemState> COPPER_GOLEM_STATE = SynchedEntityData.defineId(
            CopperGolem.class, ModEntityDataSerializers.COPPER_GOLEM_STATE
    );
    private static final EntityDataAccessor<Boolean> DATA_IS_LANTERN = SynchedEntityData.defineId(
            CopperGolem.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<Boolean> DATA_HAS_POPPY = SynchedEntityData.defineId(
            CopperGolem.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<Integer> DATA_DANCE = SynchedEntityData.defineId(
            CopperGolem.class, EntityDataSerializers.INT
    );
    private final AnimationState idleAnimationState = new AnimationState();
    private final AnimationState headSpinAnimationState = new AnimationState();
    private final AnimationState dance1AnimationState = new AnimationState();
    public final AnimationState dance2AnimationState = new AnimationState();
    private final AnimationState interactionGetItemAnimationState = new AnimationState();
    private final AnimationState interactionGetNoItemAnimationState = new AnimationState();
    private final AnimationState interactionDropItemAnimationState = new AnimationState();
    private final AnimationState interactionDropNoItemAnimationState = new AnimationState();
    @Nullable BlockPos openedChestPos;
    @Nullable
    private UUID lastLightningBoltUUID;
    private long nextWeatheringTick = -1L;
    private int idleAnimationStartTick = 0;

    public CopperGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.setPersistenceRequired();
        this.setState(CopperGolemState.IDLE);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.getBrain().setMemory(ModMemoryModules.TRANSPORT_ITEMS_COOLDOWN_TICKS.get(), this.getRandom().nextInt(60, 100));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.MAX_HEALTH, 12.0);
    }

    public static BlockPos getConnectedBlockPos(BlockPos blockPos, BlockState blockState) {
        Direction direction = ChestBlock.getConnectedDirection(blockState);
        return blockPos.relative(direction);
    }

    public int jukeboxPlaying() {
        return this.entityData.get(DATA_DANCE);
    }

    public boolean dancing() {
        return this.entityData.get(DATA_DANCE) != 0;
    }

    public void setJukeboxPlaying() {
        if (jukeboxPlaying() == 0) {
            this.entityData.set(DATA_DANCE, this.random.nextIntBetweenInclusive(0, 4));
        }
    }

    public void setJukeboxNotPlaying() {
        if (jukeboxPlaying() != 0) {
            this.entityData.set(DATA_DANCE, 0);
        }
    }

    public CopperGolemState getState() {
        return this.entityData.get(COPPER_GOLEM_STATE);
    }

    public void setState(CopperGolemState copperGolemState) {
        this.entityData.set(COPPER_GOLEM_STATE, copperGolemState);
    }

    public WeatheringCopper.WeatherState getWeatherState() {
        return this.entityData.get(DATA_WEATHER_STATE);
    }

    public void setWeatherState(WeatheringCopper.WeatherState weatherState) {
        this.entityData.set(DATA_WEATHER_STATE, weatherState);
    }

    /**
     * Sets this golem as waxed (prevents oxidation).
     */
    public void setWaxed() {
        this.nextWeatheringTick = -2L;
    }

    /**
     * Checks if this golem is waxed.
     */
    public boolean isWaxed() {
        return this.nextWeatheringTick == -2L;
    }

    /**
     * Checks if this golem is a lantern (emits light).
     */
    public boolean isLantern() {
        return this.entityData.get(DATA_IS_LANTERN);
    }

    /**
     * Sets whether this golem is a lantern (emits light).
     */
    public void setLantern(boolean isLantern) {
        this.entityData.set(DATA_IS_LANTERN, isLantern);
    }

    /**
     * Sets whether this golem has a poppy on its head.
     */
    public void setHasPoppy(boolean hasPoppy) {
        this.entityData.set(DATA_HAS_POPPY, hasPoppy);
    }

    /**
     * Checks if this golem has a poppy on its head.
     */
    public boolean hasPoppy() {
        return this.entityData.get(DATA_HAS_POPPY);
    }

    /**
     * Returns the light emission level for this golem.
     * Jack O'Lantern golems emit light level 14.
     */
    public int getLightEmission() {
        return this.isLantern() ? 14 : 0;
    }

    public void setOpenedChestPos(BlockPos blockPos) {
        this.openedChestPos = blockPos;
    }

    public void clearOpenedChestPos() {
        this.openedChestPos = null;
    }

    public AnimationState getHeadSpinAnimationState() {
        return this.headSpinAnimationState;
    }

    public AnimationState getInteractionGetItemAnimationState() {
        return this.interactionGetItemAnimationState;
    }

    public AnimationState getInteractionGetNoItemAnimationState() {
        return this.interactionGetNoItemAnimationState;
    }

    public AnimationState getInteractionDropItemAnimationState() {
        return this.interactionDropItemAnimationState;
    }

    public AnimationState getInteractionDropNoItemAnimationState() {
        return this.interactionDropNoItemAnimationState;
    }

    @Override
    protected Brain.Provider<CopperGolem> brainProvider() {
        return CopperGolemAi.brainProvider();
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CopperGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public @NotNull Brain<CopperGolem> getBrain() {
        return (Brain<CopperGolem>) super.getBrain();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WEATHER_STATE, WeatheringCopper.WeatherState.UNAFFECTED);
        this.entityData.define(COPPER_GOLEM_STATE, CopperGolemState.IDLE);
        this.entityData.define(DATA_IS_LANTERN, false);
        this.entityData.define(DATA_HAS_POPPY, false);
        this.entityData.define(DATA_DANCE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putLong("next_weather_age", this.nextWeatheringTick);
        compoundTag.putInt("weather_state", this.getWeatherState().ordinal());
        compoundTag.putBoolean("is_lantern", this.isLantern());
        compoundTag.putBoolean("has_poppy", this.hasPoppy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.nextWeatheringTick = compoundTag.getLong("next_weather_age");
        if (compoundTag.contains("is_lantern")) {
            this.setLantern(compoundTag.getBoolean("is_lantern"));
        }
        if (compoundTag.contains("has_poppy")) {
            this.setHasPoppy(compoundTag.getBoolean("has_poppy"));
        }
        if (compoundTag.contains("weather_state", 99)) { // 99 = any numeric type
            int weatherStateId = compoundTag.getInt("weather_state");
            WeatheringCopper.WeatherState state = WeatheringCopper.WeatherState.BY_ID.apply(weatherStateId);
            this.setWeatherState(state);
        } else if (compoundTag.contains("weather_state", 8)) { // 8 = string (兼容旧数据)
            String weatherStateName = compoundTag.getString("weather_state");
            for (WeatheringCopper.WeatherState state : WeatheringCopper.WeatherState.values()) {
                if (state.getSerializedName().equals(weatherStateName)) {
                    this.setWeatherState(state);
                    return;
                }
            }
            this.setWeatherState(WeatheringCopper.WeatherState.UNAFFECTED);
        } else {
            this.setWeatherState(WeatheringCopper.WeatherState.UNAFFECTED);
        }
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("copperGolemBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("copperGolemActivityUpdate");
        CopperGolemAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (!this.isNoAi()) {
                this.setupAnimationStates();
            }
        } else {
            this.updateWeathering((ServerLevel) this.level(), this.level().getRandom(), this.level().getGameTime());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.isEmpty()) {
            ItemStack itemStack2 = this.getMainHandItem();
            if (!itemStack2.isEmpty()) {
                BehaviorUtils.throwItem(this, itemStack2, player.position());
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        }

        Level level = this.level();
        if (itemStack.is(Items.SHEARS)) {
            if (this.hasPoppy()) {
                if (level instanceof ServerLevel serverLevel) {
                    ItemStack poppyStack = new ItemStack(Items.POPPY);
                    this.spawnAtLocation(poppyStack);
                    this.setHasPoppy(false);

                    // Play sound
                    serverLevel.playSound(null, this, SoundEvents.SHEEP_SHEAR, this.getSoundSource(), 3.0F, 1.0F);
                    this.gameEvent(GameEvent.SHEAR, player);
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                }
                return InteractionResult.SUCCESS;
            }
            // If no poppy, check for oxidation shearing
            else if (this.readyForShearing()) {
                if (level instanceof ServerLevel) {
                    this.shear(SoundSource.PLAYERS);
                    this.gameEvent(GameEvent.SHEAR, player);
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (itemStack.is(Items.POPPY) && !this.hasPoppy()) {
            this.setHasPoppy(true);
            level.playSound(null, this, SoundEvents.ITEM_PICKUP, this.getSoundSource(), 3.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        // Handle honeycomb - apply wax (requires sneaking)
        if (itemStack.is(Items.HONEYCOMB) && this.nextWeatheringTick != -2L && player.isCrouching()) {
            // Play wax on sound
            level.playSound(null, this, SoundEvents.HONEYCOMB_WAX_ON, this.getSoundSource(), 3.0F, 1.0F);
            level.levelEvent(player, 3003, this.blockPosition(), 0);
            this.nextWeatheringTick = -2L;

            // Grant advancement
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, this.blockPosition(), itemStack);
            }

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // Handle axe - dewax (requires sneaking)
        if (itemStack.is(ItemTags.AXES) && this.nextWeatheringTick == -2L && player.isCrouching()) {
            level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 3.0F, 1.0F);
            level.levelEvent(player, 3004, this.blockPosition(), 0); // Wax off particles
            this.nextWeatheringTick = -1L;

            // Grant advancement
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, this.blockPosition(), itemStack);
            }

            itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
            return InteractionResult.SUCCESS;
        }

        // Handle axe - scrape oxidation (requires sneaking)
        if (itemStack.is(ItemTags.AXES) && player.isCrouching()) {
            WeatheringCopper.WeatherState weatherState = this.getWeatherState();
            if (weatherState != WeatheringCopper.WeatherState.UNAFFECTED) {
                level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 3.0F, 1.0F);
                level.levelEvent(player, 3005, this.blockPosition(), 0); // Scrape particles
                this.nextWeatheringTick = -1L;
                this.entityData.set(DATA_WEATHER_STATE, weatherState.previous());

                // Grant advancement
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, this.blockPosition(), itemStack);
                }

                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, interactionHand);
    }

    private void updateWeathering(ServerLevel serverLevel, RandomSource randomSource, long l) {
        if (this.nextWeatheringTick != -2L) {
            if (this.nextWeatheringTick == -1L) {
                this.nextWeatheringTick = l + randomSource.nextIntBetweenInclusive(
                        CopperGolemConfig.getWeatheringTickMin(),
                        CopperGolemConfig.getWeatheringTickMax()
                );
            } else {
                WeatheringCopper.WeatherState weatherState = this.entityData.get(DATA_WEATHER_STATE);
                boolean bl = weatherState.equals(WeatheringCopper.WeatherState.OXIDIZED);
                if (l >= this.nextWeatheringTick && !bl) {
                    WeatheringCopper.WeatherState weatherState2 = weatherState.next();
                    boolean bl2 = weatherState2.equals(WeatheringCopper.WeatherState.OXIDIZED);
                    this.setWeatherState(weatherState2);
                    this.nextWeatheringTick = bl2 ? 0L : this.nextWeatheringTick + randomSource.nextIntBetweenInclusive(CopperGolemConfig.getWeatheringTickMin(), CopperGolemConfig.getWeatheringTickMax());
                }

                if (bl && this.canTurnToStatue(serverLevel)) {
                    this.turnToStatue(serverLevel);
                }
            }
        }
    }

    private boolean canTurnToStatue(Level level) {
        return level.getBlockState(this.blockPosition()).is(Blocks.AIR) && level.random.nextFloat() <= CopperGolemConfig.getTurnToStatueChance();
    }

    private void turnToStatue(ServerLevel serverLevel) {
        // Stop all navigation and pathfinding
        this.getNavigation().stop();
        // Clear brain goals and memories to stop AI behaviors
        this.getBrain().stopAll(serverLevel, this);
        // Set to idle state
        this.setState(CopperGolemState.IDLE);
    }

    private void setupAnimationStates() {
        switch (this.getState()) {
            case IDLE:
                this.interactionGetNoItemAnimationState.stop();
                this.interactionGetItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                if (dancing()) {
                    switch (jukeboxPlaying()) {
                        case 1, 2 -> this.dance1AnimationState.startIfStopped(this.tickCount);
                        default -> this.dance2AnimationState.startIfStopped(this.tickCount);
                    }
                } else {
                    stopDance();
                    if (this.idleAnimationStartTick == this.tickCount) {
                        this.idleAnimationState.start(this.tickCount);
                    } else if (this.idleAnimationStartTick == 0) {
                        // 使用完整的冷却时间，让头部旋转不那么频繁
                        int minCooldown = CopperGolemConfig.getSpinAnimationMinCooldown();
                        int maxCooldown = CopperGolemConfig.getSpinAnimationMaxCooldown();
                        this.idleAnimationStartTick = this.tickCount + ThreadLocalRandom.current().nextInt(minCooldown, maxCooldown);
                    }
                    if (this.tickCount == this.idleAnimationStartTick + 10) {
                        this.playHeadSpinSound();
                        this.getHeadSpinAnimationState().start(this.tickCount);
                        this.idleAnimationStartTick = 0;
                    }
                }
                break;
            case GETTING_ITEM:
                this.idleAnimationState.stop();
                // 在获取物品后有较小机会触发头部旋转
                if (this.idleAnimationStartTick == 0 && this.random.nextFloat() < 0.15F) {
                    this.playHeadSpinSound();
                    this.getHeadSpinAnimationState().start(this.tickCount);
                    this.idleAnimationStartTick = -1; // 标记已触发
                }
                stopDance();
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionGetItemAnimationState.startIfStopped(this.tickCount);
                break;
            case GETTING_NO_ITEM:
                this.idleAnimationState.stop();
                // 没有获取到物品时也可能转头
                if (this.idleAnimationStartTick == 0 && this.random.nextFloat() < 0.2F) {
                    this.playHeadSpinSound();
                    this.getHeadSpinAnimationState().start(this.tickCount);
                    this.idleAnimationStartTick = -1;
                }
                stopDance();
                this.interactionGetItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.startIfStopped(this.tickCount);
                break;
            case DROPPING_ITEM:
                this.idleAnimationState.stop();
                // 放置物品后可能转头
                if (this.idleAnimationStartTick == 0 && this.random.nextFloat() < 0.15F) {
                    this.playHeadSpinSound();
                    this.getHeadSpinAnimationState().start(this.tickCount);
                    this.idleAnimationStartTick = -1;
                }
                stopDance();
                this.interactionGetItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.startIfStopped(this.tickCount);
                break;
            case DROPPING_NO_ITEM:
                this.idleAnimationState.stop();
                // 没有放置物品时也可能转头
                if (this.idleAnimationStartTick == 0 && this.random.nextFloat() < 0.2F) {
                    this.playHeadSpinSound();
                    this.getHeadSpinAnimationState().start(this.tickCount);
                    this.idleAnimationStartTick = -1;
                }
                stopDance();
                this.interactionGetItemAnimationState.stop();
                this.interactionGetNoItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void stopDance() {
        this.dance1AnimationState.stop();
        this.dance2AnimationState.stop();
    }

    public void spawn(WeatheringCopper.WeatherState weatherState) {
        this.setWeatherState(weatherState);
        this.playSpawnSound();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag
    ) {
        this.playSpawnSound();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public void playSpawnSound() {
        this.playSound(ModSoundEvents.COPPER_GOLEM_SPAWN.get(), 3.0F, 1.0F);
    }

    private void playHeadSpinSound() {
        if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundSource(), 3.0F, 1.0F, false);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).hurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).deathSound();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).stepSound(), 1.0F, 1.0F);
    }

    private SoundEvent getSpinHeadSound() {
        return CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).spinHeadSound();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75F * this.getEyeHeight(), 0.0);
    }

    public boolean hasContainerOpen(ContainerOpenersCounter containerOpenersCounter, BlockPos blockPos) {
        if (this.openedChestPos == null) {
            return false;
        } else {
            BlockState blockState = this.level().getBlockState(this.openedChestPos);
            return this.openedChestPos.equals(blockPos)
                    || blockState.getBlock() instanceof ChestBlock
                    && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                    && getConnectedBlockPos(this.openedChestPos, blockState).equals(blockPos);
        }
    }

    @Override
    public void shear(SoundSource soundSource) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this, ModSoundEvents.COPPER_GOLEM_SHEAR.get(), soundSource, 3.0F, 1.0F);
            ItemStack itemStack2 = this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA);
            this.setItemSlot(EQUIPMENT_SLOT_ANTENNA, ItemStack.EMPTY);
            this.spawnAtLocation(itemStack2, 1.5F);
        }
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA).is(Items.POPPY);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.dropPreservedEquipment(serverLevel, i -> true);
        }
    }

    public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel serverLevel, Predicate<ItemStack> predicate) {
        Set<EquipmentSlot> set = new HashSet<>();

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                if (!predicate.test(itemStack)) {
                    set.add(equipmentSlot);
                } else {
                    // In 1.20.1, we check drop chance directly
                    if (this.getEquipmentDropChance(equipmentSlot) > 1.0F) {
                        this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                        this.spawnAtLocation(itemStack);
                    }
                }
            }
        }

        return set;
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float f) {
        super.actuallyHurt(damageSource, f);
        this.setState(CopperGolemState.IDLE);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSource) {
        if (this.hasPoppy()) {
            ItemStack poppyStack = new ItemStack(Items.POPPY);
            this.spawnAtLocation(poppyStack);
        }
        super.dropAllDeathLoot(damageSource);
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        super.thunderHit(serverLevel, lightningBolt);
        UUID uUID = lightningBolt.getUUID();
        if (!uUID.equals(this.lastLightningBoltUUID)) {
            this.lastLightningBoltUUID = uUID;
            WeatheringCopper.WeatherState weatherState = this.getWeatherState();
            if (weatherState != WeatheringCopper.WeatherState.UNAFFECTED) {
                this.nextWeatheringTick = -1L;
                this.entityData.set(DATA_WEATHER_STATE, weatherState.previous());
            }
        }
    }

    public AnimationState shrugAnimationState() {
        return dance1AnimationState;
    }
}
