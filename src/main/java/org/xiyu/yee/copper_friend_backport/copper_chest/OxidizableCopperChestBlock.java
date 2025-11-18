package org.xiyu.yee.copper_friend_backport.copper_chest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;

import static org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock.propertiesCodec;

/**
 * Oxidizable Copper Chest that can change its oxidation state over time.
 * Does not apply to waxed variants.
 */
public class OxidizableCopperChestBlock extends BaseCopperChestBlock implements WeatheringCopper {
    
    public static final MapCodec<OxidizableCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            WeatherState.CODEC.fieldOf("weathering_state")
                .forGetter(BaseCopperChestBlock::getWeatherState),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound")
                .forGetter(block -> block.openSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound")
                .forGetter(block -> block.closeSound),
            propertiesCodec()
        ).apply(instance, OxidizableCopperChestBlock::new)
    );

    public OxidizableCopperChestBlock(
        WeatherState weatherState,
        SoundEvent openSound,
        SoundEvent closeSound,
        Properties properties
    ) {
        super(weatherState, openSound, closeSound, properties);
    }

    public MapCodec<OxidizableCopperChestBlock> codec() {
        return CODEC;
    }

    /**
     * This chest can oxidize, so it's not waxed.
     */
    @Override
    public boolean isWaxed() {
        return false;
    }

    /**
     * Enable random ticking for oxidation if this chest can oxidize further.
     */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    /**
     * Handle random tick for oxidation.
     * Only oxidize the left chest to avoid double processing.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChestType chestType = state.getValue(TYPE);
        
        // Only process left or single chests to avoid double-updating large chests
        if (chestType != ChestType.RIGHT) {
            this.changeOverTime(state, level, pos, random);
        }
    }

    /**
     * Required by WeatheringCopper interface.
     */
    @Override
    public WeatherState getAge() {
        return this.weatherState;
    }
}
