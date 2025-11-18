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
     * Override changeOverTime to preserve chest contents when oxidizing.
     */
    @Override
    public void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float f = 0.05688889F;
        if (random.nextFloat() < f) {
            this.getNextState(state, level, pos, random).ifPresent(newState -> {
                // Save BlockEntity data before changing block
                net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
                net.minecraft.nbt.CompoundTag nbt = null;
                if (blockEntity != null) {
                    nbt = blockEntity.saveWithFullMetadata();
                    // Remove BlockEntity without dropping items
                    level.removeBlockEntity(pos);
                }
                
                // Change block without triggering neighbor updates that cause drops
                level.setBlock(pos, newState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS | net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
                
                // Restore BlockEntity data
                if (nbt != null) {
                    net.minecraft.world.level.block.entity.BlockEntity newBlockEntity = level.getBlockEntity(pos);
                    if (newBlockEntity != null) {
                        newBlockEntity.load(nbt);
                        newBlockEntity.setChanged();
                    }
                }
            });
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
