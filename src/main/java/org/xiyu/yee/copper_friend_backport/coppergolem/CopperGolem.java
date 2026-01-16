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
import java.util.function.Predicate;

public class CopperGolem extends AbstractGolem implements Shearable {
    // --- Mojang 官方常量定义 ---
    private static final long IGNORE_WEATHERING_TICK = -2L;
    private static final long UNSET_WEATHERING_TICK = -1L;
    private static final int WEATHERING_TICK_FROM = 504000;
    private static final int WEATHERING_TICK_TO = 552000;
    private static final int SPIN_ANIMATION_MIN_COOLDOWN = 200;
    private static final int SPIN_ANIMATION_MAX_COOLDOWN = 240;
    private static final float SPIN_SOUND_TIME_INTERVAL_OFFSET = 10.0F; // 动画先动，声音后响
    private static final float TURN_TO_STATUE_CHANCE = 0.0058F;

    // --- 数据同步键 ---
    private static final EntityDataAccessor<WeatheringCopper.WeatherState> DATA_WEATHER_STATE = SynchedEntityData.defineId(
            CopperGolem.class, ModEntityDataSerializers.WEATHERING_COPPER_STATE
    );
    private static final EntityDataAccessor<CopperGolemState> COPPER_GOLEM_STATE = SynchedEntityData.defineId(
            CopperGolem.class, ModEntityDataSerializers.COPPER_GOLEM_STATE
    );
    // Mod 特有数据
    private static final EntityDataAccessor<Boolean> DATA_IS_LANTERN = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_POPPY = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DANCE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_STATUE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.BOOLEAN);

    public static final EquipmentSlot EQUIPMENT_SLOT_ANTENNA = EquipmentSlot.CHEST; // 对应官方 SADDLE 插槽

    // --- 状态字段 ---
    @Nullable
    public BlockPos openedChestPos;
    @Nullable
    private UUID lastLightningBoltUUID;
    private long nextWeatheringTick = UNSET_WEATHERING_TICK;
    private int idleAnimationStartTick = 0;

    // --- 动画状态 ---
    // 官方 idleAnimationState 对应你的 headSpinAnimationState
    private final AnimationState headSpinAnimationState = new AnimationState();
    private final AnimationState interactionGetItemAnimationState = new AnimationState();
    private final AnimationState interactionGetNoItemAnimationState = new AnimationState();
    private final AnimationState interactionDropItemAnimationState = new AnimationState();
    private final AnimationState interactionDropNoItemAnimationState = new AnimationState();
    // Mod 特有动画
    public final AnimationState dance1AnimationState = new AnimationState();
    public final AnimationState dance2AnimationState = new AnimationState();

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

    // --- 核心逻辑: 动画状态机 (Strictly Mojang) ---

    private void setupAnimationStates() {
        // 如果是雕像模式(NoAI)，不运行动画逻辑
        if (this.isNoAi()) return;

        switch (this.getState()) {
            case IDLE:
                // 停止所有交互动画
                this.interactionGetNoItemAnimationState.stop();
                this.interactionGetItemAnimationState.stop();
                this.interactionDropItemAnimationState.stop();
                this.interactionDropNoItemAnimationState.stop();

                // 跳舞逻辑 (Mod 特有) - 如果在跳舞，覆盖掉原本的 IDLE 逻辑
                if (dancing()) {
                    switch (jukeboxPlaying()) {
                        case 1, 2 -> this.dance1AnimationState.startIfStopped(this.tickCount);
                        default -> this.dance2AnimationState.startIfStopped(this.tickCount);
                    }
                } else {
                    stopDance(); // 停止跳舞

                    // --- Mojang 转头逻辑 ---
                    if (this.idleAnimationStartTick == this.tickCount) {
                        // 1. 计时器触发：立即开始动画 (Tick 0)
                        this.headSpinAnimationState.start(this.tickCount);
                    } else if (this.idleAnimationStartTick == 0) {
                        // 2. 计时器为0：设定下一次触发时间 (随机 200-240 ticks)
                        // 这里保留了 Config 接口，如果想完全还原官方数值，请确保 Config 返回 200/240
                        int min = CopperGolemConfig.getSpinAnimationMinCooldown();
                        int max = CopperGolemConfig.getSpinAnimationMaxCooldown();
                        this.idleAnimationStartTick = this.tickCount + this.random.nextInt(min, max);
                    }

                    // 3. 动画开始 10 ticks 后：播放声音并重置计时器
                    if ((float)this.tickCount == (float)this.idleAnimationStartTick + SPIN_SOUND_TIME_INTERVAL_OFFSET) {
                        this.playHeadSpinSound();
                        this.idleAnimationStartTick = 0;
                    }
                }
                break;

            case GETTING_ITEM:
                handleInteractionState(this.interactionGetItemAnimationState);
                break;
            case GETTING_NO_ITEM:
                handleInteractionState(this.interactionGetNoItemAnimationState);
                break;
            case DROPPING_ITEM:
                handleInteractionState(this.interactionDropItemAnimationState);
                break;
            case DROPPING_NO_ITEM:
                handleInteractionState(this.interactionDropNoItemAnimationState);
                break;
        }
    }

    // 辅助方法：处理交互状态的动画清理 (Mojang 不在交互时转头)
    private void handleInteractionState(AnimationState currentState) {
        this.headSpinAnimationState.stop(); // 停止转头
        this.idleAnimationStartTick = 0;    // 重置转头计时
        stopDance();                        // 停止跳舞

        // 停止其他交互动画
        if (currentState != this.interactionGetItemAnimationState) this.interactionGetItemAnimationState.stop();
        if (currentState != this.interactionGetNoItemAnimationState) this.interactionGetNoItemAnimationState.stop();
        if (currentState != this.interactionDropItemAnimationState) this.interactionDropItemAnimationState.stop();
        if (currentState != this.interactionDropNoItemAnimationState) this.interactionDropNoItemAnimationState.stop();

        currentState.startIfStopped(this.tickCount);
    }

    private void stopDance() {
        this.dance1AnimationState.stop();
        this.dance2AnimationState.stop();
    }

    // --- 核心逻辑: 交互 (Strictly Mojang Order) ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        // 1. [Mojang] 实体手中有物品 -> 扔出
        if (itemStack.isEmpty()) {
            ItemStack handItem = this.getMainHandItem();
            if (!handItem.isEmpty()) {
                BehaviorUtils.throwItem(this, handItem, player.position());
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        }

        Level level = this.level();

        // 2. [Mojang/Mod] 剪切逻辑 (Shearing)
        // Mod 优先: 剪虞美人
        if (itemStack.is(Items.SHEARS)) {
            if (this.hasPoppy()) {
                if (level instanceof ServerLevel serverLevel) {
                    this.spawnAtLocation(new ItemStack(Items.POPPY));
                    this.setHasPoppy(false);
                    serverLevel.playSound(null, this, SoundEvents.SHEEP_SHEAR, this.getSoundSource(), 1.0F, 1.0F);
                    this.gameEvent(GameEvent.SHEAR, player);
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                }
                return InteractionResult.SUCCESS;
            }
            // Mojang: 剪天线 (readyForShearing 检查)
            else if (this.readyForShearing()) {
                if (level instanceof ServerLevel) {
                    this.shear(SoundSource.PLAYERS);
                    this.gameEvent(GameEvent.SHEAR, player);
                    itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 3. [Mojang] 客户端检查
        if (level.isClientSide()) return InteractionResult.PASS;

        // 4. [Mod] 给予虞美人 (插队在涂蜡之前)
        if (itemStack.is(Items.POPPY) && !this.hasPoppy()) {
            this.setHasPoppy(true);
            level.playSound(null, this, SoundEvents.ITEM_PICKUP, this.getSoundSource(), 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        // 5. [Mojang] 涂蜡 (Waxing)
        if (itemStack.is(Items.HONEYCOMB) && this.nextWeatheringTick != IGNORE_WEATHERING_TICK) {
            level.levelEvent(player, 3003, this.blockPosition(), 0);
            this.nextWeatheringTick = IGNORE_WEATHERING_TICK;
            this.usePlayerItem(player, interactionHand, itemStack); // 辅助方法处理消耗和统计
            return InteractionResult.SUCCESS;
        }

        // 6. [Mojang] 刮蜡 (Dewaxing)
        if (itemStack.is(ItemTags.AXES) && this.nextWeatheringTick == IGNORE_WEATHERING_TICK) {
            level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
            level.levelEvent(player, 3004, this.blockPosition(), 0);
            this.nextWeatheringTick = UNSET_WEATHERING_TICK;
            itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
            return InteractionResult.SUCCESS;
        }

        // 7. [Mojang] 刮氧化 (Scraping Oxidation)
        if (itemStack.is(ItemTags.AXES)) {
            WeatheringCopper.WeatherState weatherState = this.getWeatherState();
            if (weatherState != WeatheringCopper.WeatherState.UNAFFECTED) {
                level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
                level.levelEvent(player, 3005, this.blockPosition(), 0);
                this.nextWeatheringTick = UNSET_WEATHERING_TICK;
                this.entityData.set(DATA_WEATHER_STATE, weatherState.previous());

                // Mod 特殊处理: 如果是雕像状态恢复为实体
                if (this.entityData.get(DATA_IS_STATUE)) {
                    this.entityData.set(DATA_IS_STATUE, false);
                    this.setNoAi(false); // 恢复 AI
                    this.setState(CopperGolemState.IDLE); // 恢复 IDLE 状态
                }

                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(interactionHand));
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, interactionHand);
    }

    // 辅助方法: 简化物品消耗逻辑
    private void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, this.blockPosition(), stack);
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    // --- 核心逻辑: 氧化 (Mojang Update Loop) ---

    private void updateWeathering(ServerLevel serverLevel, RandomSource random, long gameTime) {
        if (this.nextWeatheringTick != IGNORE_WEATHERING_TICK) {
            // 初始化 ticks
            if (this.nextWeatheringTick == UNSET_WEATHERING_TICK) {
                this.nextWeatheringTick = gameTime + random.nextIntBetweenInclusive(WEATHERING_TICK_FROM, WEATHERING_TICK_TO);
            } else {
                WeatheringCopper.WeatherState currentState = this.getWeatherState();
                boolean isOxidized = currentState.equals(WeatheringCopper.WeatherState.OXIDIZED);

                // 检查是否到了氧化时间
                if (gameTime >= this.nextWeatheringTick && !isOxidized) {
                    WeatheringCopper.WeatherState nextState = currentState.next();
                    this.setWeatherState(nextState);
                    // 如果变成了完全氧化，Tick 设为 0 (准备变雕像检查)，否则继续随机下一个区间
                    boolean isNowOxidized = nextState.equals(WeatheringCopper.WeatherState.OXIDIZED);
                    this.nextWeatheringTick = isNowOxidized ? 0L : this.nextWeatheringTick + random.nextIntBetweenInclusive(WEATHERING_TICK_FROM, WEATHERING_TICK_TO);
                }

                // 检查是否变雕像
                if (isOxidized && this.canTurnToStatue(serverLevel)) {
                    this.turnToStatue(serverLevel);
                }
            }
        }
    }

    private boolean canTurnToStatue(Level level) {
        // Mojang 逻辑：空气方块 + 极小概率
        return level.getBlockState(this.blockPosition()).is(Blocks.AIR) && level.random.nextFloat() <= TURN_TO_STATUE_CHANCE;
    }

    private void turnToStatue(ServerLevel serverLevel) {
        // Mod 实现：保持为 Entity，但禁用 AI
        this.entityData.set(DATA_IS_STATUE, true);
        this.setNoAi(true);
        this.getNavigation().stop();
        this.setState(CopperGolemState.IDLE);

        // 播放声音 (Mojang 1.21.9 有 COPPER_GOLEM_BECOME_STATUE)
        this.playSound(ModSoundEvents.COPPER_GOLEM_BECOME_STATUE.get(), 1.0F, 1.0F);

        // 处理拴绳 (Mojang 会掉落拴绳)
        if (this.isLeashed()) {
            this.dropLeash(true, true);
        }
    }

    // --- 杂项与Getter/Setter ---

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

    // ... 省略了部分简单的 Getter/Setter (如 getBrain, defineSynchedData 等)，保持原样即可 ...
    // 下面是关键的动画 Getter 和 Mod 特有方法

    public AnimationState getHeadSpinAnimationState() { return headSpinAnimationState; }
    public AnimationState getInteractionGetItemAnimationState() { return interactionGetItemAnimationState; }
    public AnimationState getInteractionGetNoItemAnimationState() { return interactionGetNoItemAnimationState; }
    public AnimationState getInteractionDropItemAnimationState() { return interactionDropItemAnimationState; }
    public AnimationState getInteractionDropNoItemAnimationState() { return interactionDropNoItemAnimationState; }
    public AnimationState shrugAnimationState() { return dance1AnimationState; }

    public CopperGolemState getState() { return this.entityData.get(COPPER_GOLEM_STATE); }
    public void setState(CopperGolemState state) { this.entityData.set(COPPER_GOLEM_STATE, state); }
    public WeatheringCopper.WeatherState getWeatherState() { return this.entityData.get(DATA_WEATHER_STATE); }
    public void setWeatherState(WeatheringCopper.WeatherState state) { this.entityData.set(DATA_WEATHER_STATE, state); }

    // Mod 特有 Getter/Setter
    public boolean dancing() { return this.entityData.get(DATA_DANCE) != 0; }
    public int jukeboxPlaying() { return this.entityData.get(DATA_DANCE); }
    public void setJukeboxPlaying() { if (jukeboxPlaying() == 0) this.entityData.set(DATA_DANCE, this.random.nextIntBetweenInclusive(0, 4)); }
    public void setJukeboxNotPlaying() { if (jukeboxPlaying() != 0) this.entityData.set(DATA_DANCE, 0); }
    public boolean hasPoppy() { return this.entityData.get(DATA_HAS_POPPY); }
    public void setHasPoppy(boolean val) { this.entityData.set(DATA_HAS_POPPY, val); }
    public boolean isLantern() { return this.entityData.get(DATA_IS_LANTERN); }
    public void setLantern(boolean val) { this.entityData.set(DATA_IS_LANTERN, val); }
    public int getLightEmission() { return this.isLantern() ? 14 : 0; }

    // 数据保存
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_WEATHER_STATE, WeatheringCopper.WeatherState.UNAFFECTED);
        this.entityData.define(COPPER_GOLEM_STATE, CopperGolemState.IDLE);
        this.entityData.define(DATA_IS_LANTERN, false);
        this.entityData.define(DATA_HAS_POPPY, false);
        this.entityData.define(DATA_DANCE, 0);
        this.entityData.define(DATA_IS_STATUE, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("next_weather_age", this.nextWeatheringTick);
        tag.putInt("weather_state", this.getWeatherState().ordinal());
        tag.putBoolean("is_lantern", this.isLantern());
        tag.putBoolean("has_poppy", this.hasPoppy());
        tag.putBoolean("is_statue", this.entityData.get(DATA_IS_STATUE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextWeatheringTick = tag.getLong("next_weather_age");
        if (tag.contains("is_lantern")) this.setLantern(tag.getBoolean("is_lantern"));
        if (tag.contains("has_poppy")) this.setHasPoppy(tag.getBoolean("has_poppy"));
        if (tag.contains("is_statue")) {
            boolean isStatue = tag.getBoolean("is_statue");
            this.entityData.set(DATA_IS_STATUE, isStatue);
            if (isStatue) this.setNoAi(true);
        }
        // 兼容旧数据
        if (tag.contains("weather_state", 99)) {
            this.setWeatherState(WeatheringCopper.WeatherState.BY_ID.apply(tag.getInt("weather_state")));
        } else {
            this.setWeatherState(WeatheringCopper.WeatherState.UNAFFECTED);
        }
    }

    // 音效与辅助
    public void spawn(WeatheringCopper.WeatherState state) {
        this.setWeatherState(state);
        this.playSpawnSound();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.playSpawnSound();
        return super.finalizeSpawn(level, difficulty, type, data, tag);
    }

    public void playSpawnSound() {
        this.playSound(ModSoundEvents.COPPER_GOLEM_SPAWN.get(), 1.0F, 1.0F);
    }

    private void playHeadSpinSound() {
        if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundSource(), 1.0F, 1.0F, false);
        }
    }

    // 声音映射
    @Override protected SoundEvent getHurtSound(DamageSource src) { return org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).hurtSound(); }
    @Override protected SoundEvent getDeathSound() { return org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).deathSound(); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { this.playSound(org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).stepSound(), 1.0F, 1.0F); }
    private SoundEvent getSpinHeadSound() { return org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels.getOxidationLevel(this.getWeatherState()).spinHeadSound(); }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75F * this.getEyeHeight(), 0.0);
    }

    // 容器检查
    public boolean hasContainerOpen(ContainerOpenersCounter counter, BlockPos pos) {
        if (this.openedChestPos == null) return false;
        BlockState state = this.level().getBlockState(this.openedChestPos);
        return this.openedChestPos.equals(pos) || (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE && getConnectedBlockPos(this.openedChestPos, state).equals(pos));
    }

    public static BlockPos getConnectedBlockPos(BlockPos pos, BlockState state) {
        return pos.relative(ChestBlock.getConnectedDirection(state));
    }

    public void setOpenedChestPos(BlockPos pos) { this.openedChestPos = pos; }
    public void clearOpenedChestPos() { this.openedChestPos = null; }

    @Override
    protected Brain.Provider<CopperGolem> brainProvider() { return CopperGolemAi.brainProvider(); }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) { return CopperGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic)); }

    @Override
    public @NotNull Brain<CopperGolem> getBrain() { return (Brain<CopperGolem>) super.getBrain(); }

    // 剪切接口实现
    @Override
    public void shear(SoundSource source) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this, ModSoundEvents.COPPER_GOLEM_SHEAR.get(), source, 1.0F, 1.0F);
            ItemStack itemStack = this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA);
            this.setItemSlot(EQUIPMENT_SLOT_ANTENNA, ItemStack.EMPTY);
            this.spawnAtLocation(itemStack, 1.5F);
        }
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.getItemBySlot(EQUIPMENT_SLOT_ANTENNA).isEmpty() && !this.hasPoppy();
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.dropPreservedEquipment(serverLevel, i -> true);
        }
    }

    public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel level, Predicate<ItemStack> match) {
        Set<EquipmentSlot> slots = new HashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = this.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                if (!match.test(stack)) {
                    slots.add(slot);
                } else if (this.getEquipmentDropChance(slot) > 1.0F) {
                    this.setItemSlot(slot, ItemStack.EMPTY);
                    this.spawnAtLocation(stack);
                }
            }
        }
        return slots;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
        if (!this.isNoAi()) { // 只有非雕像状态才重置为 IDLE
            this.setState(CopperGolemState.IDLE);
        }
    }

    @Override
    protected void dropAllDeathLoot(DamageSource source) {
        if (this.hasPoppy()) this.spawnAtLocation(new ItemStack(Items.POPPY));
        super.dropAllDeathLoot(source);
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        UUID uuid = bolt.getUUID();
        if (!uuid.equals(this.lastLightningBoltUUID)) {
            this.lastLightningBoltUUID = uuid;
            WeatheringCopper.WeatherState state = this.getWeatherState();
            if (state != WeatheringCopper.WeatherState.UNAFFECTED) {
                this.nextWeatheringTick = UNSET_WEATHERING_TICK;
                this.entityData.set(DATA_WEATHER_STATE, state.previous());
                if (this.entityData.get(DATA_IS_STATUE)) {
                    this.entityData.set(DATA_IS_STATUE, false);
                    this.setNoAi(false);
                }
            }
        }
    }
}