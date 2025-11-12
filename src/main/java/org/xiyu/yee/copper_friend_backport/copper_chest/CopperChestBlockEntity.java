package org.xiyu.yee.copper_friend_backport.copper_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

public class CopperChestBlockEntity extends ChestBlockEntity {
    
    public CopperChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.COPPER_CHEST.get(), pos, state);
        // Override the openersCounter to use custom sounds
        this.openersCounter = new ContainerOpenersCounter() {
            @Override
            public void onOpen(Level level, BlockPos pos, BlockState state) {
                playCopperChestOpenSound(level, pos, state);
            }

            @Override
            public void onClose(Level level, BlockPos pos, BlockState state) {
                playCopperChestCloseSound(level, pos, state);
            }

            @Override
            public void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
                level.blockEvent(pos, state.getBlock(), 1, newCount);
            }

            @Override
            protected boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu chestMenu) {
                    net.minecraft.world.Container container = chestMenu.getContainer();
                    return container == CopperChestBlockEntity.this;
                }
                return false;
            }
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.copper_chest");
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.getLevel() != null) {
                this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
            }
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.getLevel() != null) {
                this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
            }
        }
    }

    @Override
    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    /**
     * Called when a non-player entity (like Copper Golem) opens the chest
     * This properly increments the open count and triggers animations
     */
    public void onEntityOpen(Level level, BlockPos pos, BlockState state) {
        // Increment the open count
        int oldCount = this.openersCounter.openCount++;
        
        // If this is the first opener, trigger the open animation and sound
        if (oldCount == 0) {
            playCopperChestOpenSound(level, pos, state);
            // Broadcast the container open event to all clients
            level.gameEvent(null, GameEvent.CONTAINER_OPEN, pos);
        }
        
        // Notify clients about the new open count (triggers animation)
        this.openersCounter.openerCountChanged(level, pos, state, oldCount, this.openersCounter.openCount);
    }

    /**
     * Called when a non-player entity (like Copper Golem) closes the chest
     * This properly decrements the open count and triggers animations
     */
    public void onEntityClose(Level level, BlockPos pos, BlockState state) {
        // Decrement the open count
        int oldCount = this.openersCounter.openCount--;
        
        // If this is the last closer, trigger the close animation and sound
        if (this.openersCounter.openCount == 0) {
            playCopperChestCloseSound(level, pos, state);
            // Broadcast the container close event to all clients
            level.gameEvent(null, GameEvent.CONTAINER_CLOSE, pos);
        }
        
        // Notify clients about the new open count (triggers animation)
        this.openersCounter.openerCountChanged(level, pos, state, oldCount, this.openersCounter.openCount);
    }

    /**
     * Play the appropriate copper chest open sound based on oxidation level
     */
    private static void playCopperChestOpenSound(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseCopperChestBlock copperChest) {
            WeatheringCopper.WeatherState weatherState = copperChest.getWeatherState();
            SoundEvent soundToPlay = switch (weatherState) {
                case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_OPEN.get();
                case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get();
                case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get();
            };
            
            level.playSound(null, pos, soundToPlay, SoundSource.BLOCKS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * Play the appropriate copper chest close sound based on oxidation level
     */
    private static void playCopperChestCloseSound(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseCopperChestBlock copperChest) {
            WeatheringCopper.WeatherState weatherState = copperChest.getWeatherState();
            SoundEvent soundToPlay = switch (weatherState) {
                case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_CLOSE.get();
                case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get();
                case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get();
            };
            
            level.playSound(null, pos, soundToPlay, SoundSource.BLOCKS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }
}
