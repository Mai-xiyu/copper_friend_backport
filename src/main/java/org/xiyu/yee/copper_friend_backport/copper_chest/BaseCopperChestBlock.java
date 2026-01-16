package org.xiyu.yee.copper_friend_backport.copper_chest;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockTags;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Base class for all Copper Chest variants.
 * Handles common functionality like chest merging, oxidation state management, and block entity creation.
 */
public abstract class BaseCopperChestBlock extends ChestBlock {

    private static final Map<Block, Supplier<Block>> COPPER_TO_CHEST_MAPPING = Map.of(
            Blocks.COPPER_BLOCK, () -> ModBlocks.COPPER_CHEST.get(),
            Blocks.EXPOSED_COPPER, () -> ModBlocks.EXPOSED_COPPER_CHEST.get(),
            Blocks.WEATHERED_COPPER, () -> ModBlocks.WEATHERED_COPPER_CHEST.get(),
            Blocks.OXIDIZED_COPPER, () -> ModBlocks.OXIDIZED_COPPER_CHEST.get(),
            Blocks.WAXED_COPPER_BLOCK, () -> ModBlocks.COPPER_CHEST.get(),
            Blocks.WAXED_EXPOSED_COPPER, () -> ModBlocks.EXPOSED_COPPER_CHEST.get(),
            Blocks.WAXED_WEATHERED_COPPER, () -> ModBlocks.WEATHERED_COPPER_CHEST.get(),
            Blocks.WAXED_OXIDIZED_COPPER, () -> ModBlocks.OXIDIZED_COPPER_CHEST.get()
    );

    protected final WeatheringCopper.WeatherState weatherState;
    protected final SoundEvent openSound;
    protected final SoundEvent closeSound;

    protected BaseCopperChestBlock(
            WeatheringCopper.WeatherState weatherState,
            SoundEvent openSound,
            SoundEvent closeSound,
            Properties properties
    ) {
        super(properties, () -> ModBlockEntity.COPPER_CHEST.get());
        this.weatherState = weatherState;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }

    /**
     * 更新箱子方块状态，同时处理大箱子的另一半（如果有）。
     * 保持 BlockEntity 的数据（如物品）不丢失。
     */
    protected void updateChestState(BlockState currentState, Level level, BlockPos pos, Block newBlock, @Nullable Player player) {
        // 1. 获取要变成的新状态（保留原有属性，如朝向、类型、含水等）
        BlockState newState = newBlock.withPropertiesOf(currentState);

        // 2. 执行自身的更新
        replaceBlockSafe(level, pos, newState);
        // 触发游戏事件
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

        // 3. 检查是否有相连的箱子 (Double Chest)
        ChestType type = currentState.getValue(TYPE);
        if (type != ChestType.SINGLE) {
            Direction connectedDir = getConnectedDirection(currentState);
            BlockPos connectedPos = pos.relative(connectedDir);
            BlockState connectedState = level.getBlockState(connectedPos);

            // 确认相连的确实是同一个类型的箱子（防止异常情况）
            if (connectedState.is(currentState.getBlock())) {
                BlockState newConnectedState = newBlock.withPropertiesOf(connectedState);
                replaceBlockSafe(level, connectedPos, newConnectedState);

                // 触发游戏事件（针对相连箱子）
                level.gameEvent(GameEvent.BLOCK_CHANGE, connectedPos, GameEvent.Context.of(player, newConnectedState));
            }
        }
    }

    /**
     * 安全替换方块，保留 BlockEntity 数据
     */
    private void replaceBlockSafe(Level level, BlockPos pos, BlockState newState) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        net.minecraft.nbt.CompoundTag nbt = null;
        if (blockEntity != null) {
            nbt = blockEntity.saveWithFullMetadata();
            level.removeBlockEntity(pos); // 移除旧 BE
        }

        // 设置新方块，不触发掉落
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);

        if (nbt != null) {
            BlockEntity newBlockEntity = level.getBlockEntity(pos);
            if (newBlockEntity != null) {
                newBlockEntity.load(nbt);
                newBlockEntity.setChanged();
            }
        }
    }

    /**
     * Returns the oxidation state of this chest.
     */
    public WeatheringCopper.WeatherState getWeatherState() {
        return this.weatherState;
    }

    /**
     * Creates the BlockEntity for this chest.
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CopperChestBlockEntity(pos, state);
    }

    /**
     * Returns whether this chest is waxed (cannot oxidize further).
     */
    public abstract boolean isWaxed();

    /**
     * Handle honeycomb (waxing) and axe (scraping/dewaxing) interactions.
     * Requires player to be sneaking for wax/scrape operations.
     */
    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Handle honeycomb - apply wax (works even when not crouching for consistency with vanilla copper blocks)
        if (stack.is(Items.HONEYCOMB) && !this.isWaxed()) {
            Block waxedBlock = getWaxedBlock(state.getBlock());
            if (waxedBlock != null) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                } else {
                    // 使用 updateChestState 同步更新双箱子
                    updateChestState(state, level, pos, waxedBlock, player);

                    // Play wax on sound
                    level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // Wax on particle effect
                    level.levelEvent(player, 3003, pos, 0);

                    // Grant advancement
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }

                    // Consume item
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    return InteractionResult.CONSUME;
                }
            }
        }

        // Handle axe - scrape or dewax (works even when not crouching for consistency with vanilla copper blocks)
        if (stack.is(net.minecraft.tags.ItemTags.AXES)) {
            Block scrapedBlock = null;
            boolean isRemovingWax = false;

            // Try to remove wax first
            if (this.isWaxed()) {
                scrapedBlock = getDewaxedBlock(state.getBlock());
                isRemovingWax = true;
            }
            // If not waxed, try to decrease oxidation
            else if (this instanceof WeatheringCopper) {
                Optional<Block> previous = WeatheringCopper.getPrevious(state.getBlock());
                if (previous.isPresent()) {
                    scrapedBlock = previous.get();
                    isRemovingWax = false;
                }
            }

            if (scrapedBlock != null) {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                } else {
                    // 使用 updateChestState 同步更新双箱子
                    updateChestState(state, level, pos, scrapedBlock, player);

                    // Play scraping sound
                    level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // Particle effect (wax off or scrape)
                    if (isRemovingWax) {
                        level.levelEvent(player, 3004, pos, 0); // Wax off particles
                    } else {
                        level.levelEvent(player, 3005, pos, 0); // Scrape particles
                    }

                    // Grant advancement
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }

                    // Damage tool
                    if (!player.getAbilities().instabuild) {
                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                    }

                    return InteractionResult.CONSUME;
                }
            }
        }

        // If player is crouching, prevent chest opening
        if (player.isCrouching()) {
            return InteractionResult.FAIL;
        }

        // Not crouching - allow normal chest interaction
        return super.use(state, level, pos, player, hand, hitResult);
    }

    /**
     * Get the waxed version of a copper chest block.
     */
    private static Block getWaxedBlock(Block block) {
        if (block == ModBlocks.COPPER_CHEST.get()) {
            return ModBlocks.WAXED_COPPER_CHEST.get();
        } else if (block == ModBlocks.EXPOSED_COPPER_CHEST.get()) {
            return ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get();
        } else if (block == ModBlocks.WEATHERED_COPPER_CHEST.get()) {
            return ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get();
        } else if (block == ModBlocks.OXIDIZED_COPPER_CHEST.get()) {
            return ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get();
        }
        return null;
    }

    /**
     * Get the dewaxed version of a waxed copper chest block.
     */
    private static Block getDewaxedBlock(Block block) {
        if (block == ModBlocks.WAXED_COPPER_CHEST.get()) {
            return ModBlocks.COPPER_CHEST.get();
        } else if (block == ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get()) {
            return ModBlocks.EXPOSED_COPPER_CHEST.get();
        } else if (block == ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get()) {
            return ModBlocks.WEATHERED_COPPER_CHEST.get();
        } else if (block == ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get()) {
            return ModBlocks.OXIDIZED_COPPER_CHEST.get();
        }
        return null;
    }

    /**
     * Handle lightning strike - remove oxidation completely.
     */
    public void onLightningStrike(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            // Get the first (unoxidized) state
            Block firstBlock = WeatheringCopper.getFirst(state.getBlock());

            // If waxed, dewax it first
            if (this.isWaxed()) {
                firstBlock = getDewaxedBlock(firstBlock);
                if (firstBlock == null) {
                    // If we can't dewax, just remove current wax
                    firstBlock = getDewaxedBlock(state.getBlock());
                }
            }

            if (firstBlock != null && firstBlock != state.getBlock()) {
                // 使用 updateChestState 同步更新双箱子 (Player 为 null)
                updateChestState(state, level, pos, firstBlock, null);
            }
        }
    }

    /**
     * Checks if this chest can connect to another chest block.
     */
    protected boolean chestCanConnectTo(BlockState state) {
        return state.is(ModBlockTags.COPPER_CHESTS) && state.hasProperty(ChestBlock.TYPE);
    }

    /**
     * Gets the proper block state when placing the chest, handling merging with adjacent chests.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return getLeastOxidizedChestState(state, context.getLevel(), context.getClickedPos());
    }

    /**
     * Updates block state when neighboring blocks change, handling chest merging.
     */
    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        BlockState newState = super.updateShape(state, direction, neighborState, level, pos, neighborPos);

        if (this.chestCanConnectTo(neighborState)) {
            ChestType chestType = newState.getValue(ChestBlock.TYPE);
            if (!chestType.equals(ChestType.SINGLE) && getConnectedDirection(newState) == direction) {
                return neighborState.getBlock().withPropertiesOf(newState);
            }
        }

        return newState;
    }

    /**
     * Determines which oxidation level to use when two chests merge.
     * Uses the least oxidized state between the two chests.
     */
    private static BlockState getLeastOxidizedChestState(BlockState state, Level level, BlockPos pos) {
        BlockState neighborState = level.getBlockState(pos.relative(getConnectedDirection(state)));

        if (!state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE)
                && state.getBlock() instanceof BaseCopperChestBlock thisChest
                && neighborState.getBlock() instanceof BaseCopperChestBlock neighborChest) {

            BlockState unwaxedState = state;
            BlockState unwaxedNeighborState = neighborState;

            // If wax states differ, unwax both for comparison
            if (thisChest.isWaxed() != neighborChest.isWaxed()) {
                unwaxedState = unwaxBlock(thisChest, state).orElse(state);
                unwaxedNeighborState = unwaxBlock(neighborChest, neighborState).orElse(neighborState);
            }

            // Use the least oxidized chest's block type
            Block leastOxidizedBlock = thisChest.weatherState.ordinal() <= neighborChest.weatherState.ordinal()
                    ? unwaxedState.getBlock()
                    : unwaxedNeighborState.getBlock();

            return leastOxidizedBlock.withPropertiesOf(unwaxedState);
        }

        return state;
    }

    /**
     * Unwaxes a copper chest block if it's waxed.
     */
    private static Optional<BlockState> unwaxBlock(BaseCopperChestBlock chest, BlockState state) {
        if (!chest.isWaxed()) {
            return Optional.of(state);
        }

        BiMap<Block, Block> waxOffMap = HoneycombItem.WAX_OFF_BY_BLOCK.get();
        Block unwaxedBlock = waxOffMap.get(state.getBlock());

        return Optional.ofNullable(unwaxedBlock)
                .map(block -> block.withPropertiesOf(state));
    }

    /**
     * Creates a copper chest from a copper block, used when spawning Copper Golem.
     */
    public static BlockState createFromCopperBlock(Block copperBlock, Direction facing, Level level, BlockPos pos) {
        BaseCopperChestBlock chestBlock = (BaseCopperChestBlock) COPPER_TO_CHEST_MAPPING
                .getOrDefault(copperBlock, () -> ModBlocks.COPPER_CHEST.get())
                .get();

        BlockState state = chestBlock.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(TYPE, ChestType.SINGLE);

        return getLeastOxidizedChestState(state, level, pos);
    }

    /**
     * Allows the block to keep its block entity when changed to another copper chest state.
     */
    public boolean shouldChangedStateKeepBlockEntity(BlockState state) {
        return state.is(ModBlockTags.COPPER_CHESTS);
    }

    /**
     * Play sound when chest is opened or closed.
     * This method can be called from the BlockEntity or from a Mixin.
     */
    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
        if (state.getBlock() instanceof BaseCopperChestBlock copperChest) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * Combiner for chest openness animation.
     * Used by the renderer to get the interpolated openness value.
     */
    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(LidBlockEntity lidBlockEntity) {
        return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
            @Override
            public Float2FloatFunction acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
                return (partialTick) -> Math.max(first.getOpenNess(partialTick), second.getOpenNess(partialTick));
            }

            @Override
            public Float2FloatFunction acceptSingle(ChestBlockEntity single) {
                return single::getOpenNess;
            }

            @Override
            public Float2FloatFunction acceptNone() {
                return lidBlockEntity::getOpenNess;
            }
        };
    }
}