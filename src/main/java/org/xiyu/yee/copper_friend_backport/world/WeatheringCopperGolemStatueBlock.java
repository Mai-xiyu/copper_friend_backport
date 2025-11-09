package org.xiyu.yee.copper_friend_backport.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;

import static org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock.propertiesCodec;

public class WeatheringCopperGolemStatueBlock extends CopperGolemStatueBlock implements WeatheringCopper {
	public static final MapCodec<WeatheringCopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				WeatherState.CODEC.fieldOf("weathering_state").forGetter(CopperGolemStatueBlock::getWeatheringState),
				propertiesCodec()
			)
			.apply(instance, WeatheringCopperGolemStatueBlock::new)
	);

	public WeatheringCopperGolemStatueBlock(
		WeatherState weatherState, Properties properties
	) {
		super(weatherState, properties);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return WeatheringCopper.getNext(blockState.getBlock()).isPresent();
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.changeOverTime(blockState, serverLevel, blockPos, randomSource);
	}

	public WeatherState getAge() {
		return this.getWeatheringState();
	}

	public boolean isWaxed() {
		return false;
	}
}
