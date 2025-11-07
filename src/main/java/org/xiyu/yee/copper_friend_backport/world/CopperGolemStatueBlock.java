package org.xiyu.yee.copper_friend_backport.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockTags;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

public class CopperGolemStatueBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final Codec<BlockBehaviour.Properties> PropertiesCODEC = Codec.unit(Properties::of);
    
    public static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
        return PropertiesCODEC.fieldOf("properties").forGetter((Function<B, Properties>) Properties::copy);
    }
    
	public static final MapCodec<CopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperGolemStatueBlock::getWeatheringState), propertiesCodec()
			)
			.apply(instance, CopperGolemStatueBlock::new)
	);
    public static final EnumProperty<CopperGolemStatueBlock.Pose> COPPER_GOLEM_POSE = EnumProperty.create("copper_golem_pose", CopperGolemStatueBlock.Pose.class);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<CopperGolemStatueBlock.Pose> POSE = COPPER_GOLEM_POSE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE = column(10.0, 0.0, 14.0);

    public static VoxelShape column(double d, double e, double f) {
        return column(d, d, e, f);
    }

    public static VoxelShape column(double d, double e, double f, double g) {
        double h = d / 2.0;
        double i = e / 2.0;
        return box(8.0 - h, f, 8.0 - i, 8.0 + h, g, 8.0 + i);
    }
	private final WeatheringCopper.WeatherState weatheringState;


	public CopperGolemStatueBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
		super(properties);
		this.weatheringState = weatherState;
		this.registerDefaultState(
			this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POSE, CopperGolemStatueBlock.Pose.STANDING).setValue(WATERLOGGED, false)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, POSE, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		return this.defaultBlockState()
			.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}


	public WeatheringCopper.WeatherState getWeatheringState() {
		return this.weatheringState;
	}

    @Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (player.getItemInHand(interactionHand).is(ItemTags.AXES)) {
			return InteractionResult.PASS;
		} else {
			this.updatePose(level, blockState, blockPos, player);
			return InteractionResult.SUCCESS;
		}
	}

	void updatePose(Level level, BlockState blockState, BlockPos blockPos, Player player) {
		level.playSound(null, blockPos, ModSoundEvents.COPPER_GOLEM_BECOME_STATUE.get(), SoundSource.BLOCKS);
		level.setBlock(blockPos, blockState.setValue(POSE, ((CopperGolemStatueBlock.Pose)blockState.getValue(POSE)).getNextPose()), 3);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
	}

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter pLevel, BlockPos pPos, PathComputationType pathComputationType) {
        return pathComputationType == PathComputationType.WATER && blockState.getFluidState().is(FluidTags.WATER);
    }

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new CopperGolemStatueBlockEntity(blockPos, blockState);
	}

	public boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
		return blockState.is(ModBlockTags.COPPER_GOLEM_STATUES);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return ((CopperGolemStatueBlock.Pose)blockState.getValue(POSE)).ordinal() + 1;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return blockGetter.getBlockEntity(blockPos) instanceof CopperGolemStatueBlockEntity copperGolemStatueBlockEntity
			? copperGolemStatueBlockEntity.getItem(this.asItem().getDefaultInstance(), blockState.getValue(POSE))
			: super.getCloneItemStack(blockGetter, blockPos, blockState);
	}

	protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
		serverLevel.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
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
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, neighborState, levelAccessor, blockPos, neighborPos);
	}

	public static enum Pose implements StringRepresentable {
		STANDING("standing"),
		SITTING("sitting"),
		RUNNING("running"),
		STAR("star");

		public static final IntFunction<CopperGolemStatueBlock.Pose> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
		public static final Codec<CopperGolemStatueBlock.Pose> CODEC = StringRepresentable.fromEnum(CopperGolemStatueBlock.Pose::values);
		private final String name;

		private Pose(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public CopperGolemStatueBlock.Pose getNextPose() {
			return (CopperGolemStatueBlock.Pose)BY_ID.apply(this.ordinal() + 1);
		}
	}
}
