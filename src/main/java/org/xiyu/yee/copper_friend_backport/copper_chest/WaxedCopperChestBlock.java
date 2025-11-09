package org.xiyu.yee.copper_friend_backport.copper_chest;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;

import static org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock.propertiesCodec;

/**
 * Waxed Copper Chest that cannot oxidize further.
 * Maintains its current oxidation state permanently.
 */
public class WaxedCopperChestBlock extends BaseCopperChestBlock {
    
    public static final MapCodec<WaxedCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")
                .forGetter(BaseCopperChestBlock::getWeatherState),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound")
                .forGetter(block -> block.openSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound")
                .forGetter(block -> block.closeSound),
            propertiesCodec()
        ).apply(instance, WaxedCopperChestBlock::new)
    );

    public WaxedCopperChestBlock(
        WeatheringCopper.WeatherState weatherState,
        SoundEvent openSound,
        SoundEvent closeSound,
        BlockBehaviour.Properties properties
    ) {
        super(weatherState, openSound, closeSound, properties);
    }

    public MapCodec<WaxedCopperChestBlock> codec() {
        return CODEC;
    }

    /**
     * This chest is waxed and cannot oxidize.
     */
    @Override
    public boolean isWaxed() {
        return true;
    }
}
