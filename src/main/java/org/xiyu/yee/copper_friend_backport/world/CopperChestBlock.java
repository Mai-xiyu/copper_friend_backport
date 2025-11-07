package org.xiyu.yee.copper_friend_backport.world;

import com.google.common.collect.BiMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockTags;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

import static org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock.propertiesCodec;

public class CopperChestBlock extends ChestBlock {
	public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState),
				BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter( chestBlock -> ModSoundEvents.COPPER_CHEST_OPEN.get()),
				BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(chestBlock -> ModSoundEvents.COPPER_CHEST_CLOSE.get()),
				propertiesCodec()
			)
			.apply(instance, CopperChestBlock::new)
	);
	private static final Map<Block, Supplier<Block>> COPPER_TO_COPPER_CHEST_MAPPING = Map.of(
            Blocks.COPPER_BLOCK,
		(Supplier)() -> ModBlocks.COPPER_CHEST.get(),
            Blocks.EXPOSED_COPPER,
		(Supplier)() -> ModBlocks.EXPOSED_COPPER_CHEST.get(),
            Blocks.WEATHERED_COPPER,
		(Supplier)() -> ModBlocks.WEATHERED_COPPER_CHEST.get(),
            Blocks.OXIDIZED_COPPER,
		(Supplier)() -> ModBlocks.OXIDIZED_COPPER_CHEST.get(),
            Blocks.WAXED_COPPER_BLOCK,
		(Supplier)() -> ModBlocks.COPPER_CHEST.get(),
            Blocks.WAXED_EXPOSED_COPPER,
		(Supplier)() -> ModBlocks.EXPOSED_COPPER_CHEST.get(),
            Blocks.WAXED_WEATHERED_COPPER,
		(Supplier)() -> ModBlocks.WEATHERED_COPPER_CHEST.get(),
            Blocks.WAXED_OXIDIZED_COPPER,
		(Supplier)() -> ModBlocks.OXIDIZED_COPPER_CHEST.get()
	);
	private final WeatheringCopper.WeatherState weatherState;

	public MapCodec<? extends CopperChestBlock> codec() {
		return CODEC;
	}

	public CopperChestBlock(WeatheringCopper.WeatherState weatherState, SoundEvent soundEvent, SoundEvent soundEvent2, BlockBehaviour.Properties properties) {
		super(properties, () -> BlockEntityType.CHEST);
		this.weatherState = weatherState;
	}

	public boolean chestCanConnectTo(BlockState blockState) {
		return blockState.is(ModBlockTags.COPPER_CHESTS) && blockState.hasProperty(ChestBlock.TYPE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getStateForPlacement(blockPlaceContext);
		return getLeastOxidizedChestOfConnectedBlocks(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}

	private static BlockState getLeastOxidizedChestOfConnectedBlocks(BlockState blockState, Level level, BlockPos blockPos) {
		BlockState blockState2 = level.getBlockState(blockPos.relative(getConnectedDirection(blockState)));
		if (!((ChestType)blockState.getValue(ChestBlock.TYPE)).equals(ChestType.SINGLE)
			&& blockState.getBlock() instanceof CopperChestBlock copperChestBlock
			&& blockState2.getBlock() instanceof CopperChestBlock copperChestBlock2) {
			BlockState blockState3 = blockState;
			BlockState blockState4 = blockState2;
			if (copperChestBlock.isWaxed() != copperChestBlock2.isWaxed()) {
				blockState3 = (BlockState)unwaxBlock(copperChestBlock, blockState).orElse(blockState);
				blockState4 = (BlockState)unwaxBlock(copperChestBlock2, blockState2).orElse(blockState2);
			}

			Block block = copperChestBlock.weatherState.ordinal() <= copperChestBlock2.weatherState.ordinal() ? blockState3.getBlock() : blockState4.getBlock();
			return block.withPropertiesOf(blockState3);
		} else {
			return blockState;
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState,
		Direction direction,
		BlockState neighborState,
		net.minecraft.world.level.LevelAccessor levelAccessor,
		BlockPos blockPos,
		BlockPos neighborPos
	) {
		BlockState blockState3 = super.updateShape(blockState, direction, neighborState, levelAccessor, blockPos, neighborPos);
		if (this.chestCanConnectTo(neighborState)) {
			ChestType chestType = blockState3.getValue(ChestBlock.TYPE);
			if (!chestType.equals(ChestType.SINGLE) && getConnectedDirection(blockState3) == direction) {
				return neighborState.getBlock().withPropertiesOf(blockState3);
			}
		}

		return blockState3;
	}

	private static Optional<BlockState> unwaxBlock(CopperChestBlock copperChestBlock, BlockState blockState) {
		return !copperChestBlock.isWaxed()
			? Optional.of(blockState)
			: Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
	}

	public WeatheringCopper.WeatherState getState() {
		return this.weatherState;
	}

	public static BlockState getFromCopperBlock(Block block, Direction direction, Level level, BlockPos blockPos) {
		CopperChestBlock copperChestBlock = (CopperChestBlock)((Supplier)COPPER_TO_COPPER_CHEST_MAPPING.getOrDefault(block, () -> ModBlocks.COPPER_CHEST.get())).get();
		ChestType chestType = ChestType.SINGLE;
		BlockState blockState = copperChestBlock.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chestType);
		return getLeastOxidizedChestOfConnectedBlocks(blockState, level, blockPos);
	}

	public boolean isWaxed() {
		return true;
	}

	public boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
		return blockState.is(ModBlockTags.COPPER_CHESTS);
	}
}
