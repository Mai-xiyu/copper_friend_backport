package org.xiyu.yee.copper_friend_backport.copper_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

public class CopperChestBlockEntity extends ChestBlockEntity {
    
    // Custom openersCounter that plays copper chest sounds
    private final ContainerOpenersCounter customOpenersCounter = new ContainerOpenersCounter() {
        @Override
        public void onOpen(Level level, BlockPos pos, BlockState state) {
            playCopperChestSound(level, pos, state, SoundEvents.CHEST_OPEN);
        }

        @Override
        public void onClose(Level level, BlockPos pos, BlockState state) {
            playCopperChestSound(level, pos, state, SoundEvents.CHEST_CLOSE);
        }

        @Override
        public void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu) {
                net.minecraft.world.Container container = ((net.minecraft.world.inventory.ChestMenu) player.containerMenu).getContainer();
                return container == CopperChestBlockEntity.this;
            }
            return false;
        }
    };
    
    public CopperChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.COPPER_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.copper_chest");
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.customOpenersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.customOpenersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void recheckOpen() {
        if (!this.remove) {
            this.customOpenersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private static void playCopperChestSound(Level level, BlockPos pos, BlockState state, SoundEvent defaultSound) {
        if (state.getBlock() instanceof BaseCopperChestBlock copperChest) {
            WeatheringCopper.WeatherState weatherState = copperChest.getWeatherState();
            SoundEvent soundToPlay;
            if (defaultSound == SoundEvents.CHEST_OPEN) {
                soundToPlay = switch (weatherState) {
                    case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_OPEN.get();
                    case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get();
                    case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get();
                };
            } else {
                soundToPlay = switch (weatherState) {
                    case UNAFFECTED, EXPOSED -> ModSoundEvents.COPPER_CHEST_CLOSE.get();
                    case WEATHERED -> ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get();
                    case OXIDIZED -> ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get();
                };
            }

            level.playSound(null, pos, soundToPlay, SoundSource.BLOCKS, 0.5F, 
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }
}
