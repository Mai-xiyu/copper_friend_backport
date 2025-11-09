package org.xiyu.yee.copper_friend_backport.copper_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

public class CopperChestBlockEntity extends ChestBlockEntity {
    
    public CopperChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.COPPER_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.copper_chest");
    }

    /**
     * Override the sound playing to use custom copper chest sounds.
     * This is called by the openersCounter when players open/close the chest.
     */
    private static void playCopperChestSound(Level level, BlockPos pos, BlockState state, SoundEvent defaultSound) {
        if (state.getBlock() instanceof BaseCopperChestBlock copperChest) {
            WeatheringCopper.WeatherState weatherState = copperChest.getWeatherState();
            
            // Select sound based on oxidation state and whether opening or closing
            SoundEvent soundToPlay;
            if (defaultSound == SoundEvents.CHEST_OPEN) {
                soundToPlay = switch (weatherState) {
                    case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_OPEN.get();
                    case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get();
                    case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get();
                };
            } else { // CHEST_CLOSE
                soundToPlay = switch (weatherState) {
                    case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_CLOSE.get();
                    case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get();
                    case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get();
                };
            }
            
            // Play the sound
            level.playSound(null, pos, soundToPlay, SoundSource.BLOCKS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }
    
    /**
     * Public method for Mixin to call
     */
    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent defaultSound) {
        playCopperChestSound(level, pos, state, defaultSound);
    }
}
