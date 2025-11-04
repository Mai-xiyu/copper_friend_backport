package org.xiyu.yee.copper_friend_backport;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;
import org.xiyu.yee.copper_friend_backport.world.ByteBufCodecs;
import org.xiyu.yee.copper_friend_backport.world.ChangeOverTimeBlock;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
	Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(
		() -> ImmutableBiMap.<Block, Block>builder()
			.put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)
			.put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)
			.put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)
			.put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)
			.put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)
			.put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)
			.put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)
			.put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)
			.put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)
			.put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS)
			.put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS)
			.put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)
			.put(ModBlocks.COPPER_CHEST, ModBlocks.EXPOSED_COPPER_CHEST)
			.put(ModBlocks.EXPOSED_COPPER_CHEST, ModBlocks.WEATHERED_COPPER_CHEST)
			.put(ModBlocks.WEATHERED_COPPER_CHEST, ModBlocks.OXIDIZED_COPPER_CHEST)
			.put(ModBlocks.COPPER_GOLEM_STATUE, ModBlocks.EXPOSED_COPPER_GOLEM_STATUE)
			.put(ModBlocks.EXPOSED_COPPER_GOLEM_STATUE, ModBlocks.WEATHERED_COPPER_GOLEM_STATUE)
			.put(ModBlocks.WEATHERED_COPPER_GOLEM_STATUE, ModBlocks.OXIDIZED_COPPER_GOLEM_STATUE)
			.build()
	);
	Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> ((BiMap)NEXT_BY_BLOCK.get()).inverse());

	static Optional<Block> getPrevious(Block block) {
		return Optional.ofNullable((Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block));
	}

	static Block getFirst(Block block) {
		Block block2 = block;

		for (Block block3 = (Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block); block3 != null; block3 = (Block)((BiMap)PREVIOUS_BY_BLOCK.get()).get(block3)) {
			block2 = block3;
		}

		return block2;
	}

	static Optional<BlockState> getPrevious(BlockState blockState) {
		return getPrevious(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
	}

	static Optional<Block> getNext(Block block) {
		return Optional.ofNullable((Block)((BiMap)NEXT_BY_BLOCK.get()).get(block));
	}

	static BlockState getFirst(BlockState blockState) {
		return getFirst(blockState.getBlock()).withPropertiesOf(blockState);
	}

	@Override
	default Optional<BlockState> getNext(BlockState blockState) {
		return getNext(blockState.getBlock()).map(block -> block.withPropertiesOf(blockState));
	}

	@Override
	default float getChanceModifier() {
		return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
	}

	public static enum WeatherState implements StringRepresentable {
		UNAFFECTED("unaffected"),
		EXPOSED("exposed"),
		WEATHERED("weathered"),
		OXIDIZED("oxidized");

		public static final IntFunction<WeatheringCopper.WeatherState> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
		public static final Codec<WeatheringCopper.WeatherState> CODEC = StringRepresentable.fromEnum(WeatheringCopper.WeatherState::values);
		public static final StreamCodec<ByteBuf, WeatheringCopper.WeatherState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
		private final String name;

		private WeatherState(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public WeatheringCopper.WeatherState next() {
			return (WeatheringCopper.WeatherState)BY_ID.apply(this.ordinal() + 1);
		}

		public WeatheringCopper.WeatherState previous() {
			return (WeatheringCopper.WeatherState)BY_ID.apply(this.ordinal() - 1);
		}
	}
}
