package org.xiyu.yee.copper_friend_backport.copper_chest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents;

public class CopperChestBlockEntity extends ChestBlockEntity {
    
    public CopperChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.COPPER_CHEST.get(), pos, state);
        // Override the openersCounter to handle double chests properly for both players and entities
        this.openersCounter = new CopperChestOpenersCounter();
    }
    
    /**
     * Custom ContainerOpenersCounter that properly handles double copper chests
     * for both players and entities (like Copper Golem)
     */
    private class CopperChestOpenersCounter extends ContainerOpenersCounter {
        
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
                // Check if this BlockEntity is the container OR part of a CompoundContainer
                return container == CopperChestBlockEntity.this 
                    || (container instanceof net.minecraft.world.CompoundContainer compoundContainer 
                        && compoundContainer.contains(CopperChestBlockEntity.this));
            }
            return false;
        }
    }    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.copper_chest");
    }
    
    /**
     * Called when a non-player entity (like Copper Golem) opens the chest
     * Handles both single and double chests properly
     */
    public void onEntityOpen(Level level, BlockPos pos, BlockState state) {
        if (!this.remove) {
            // Increment open count
            int oldCount = this.openersCounter.openCount++;
            
            // If this is the first opener, trigger sound and game event
            if (oldCount == 0) {
                this.openersCounter.onOpen(level, pos, state);
                level.gameEvent(null, GameEvent.CONTAINER_OPEN, pos);
            }
            
            // Trigger animation
            this.openersCounter.openerCountChanged(level, pos, state, oldCount, this.openersCounter.openCount);
            
            // For double chests, also update the connected chest (entities don't use CompoundContainer)
            updateConnectedChestForEntity(level, pos, state, true);
        }
    }

    /**
     * Called when a non-player entity (like Copper Golem) closes the chest
     * Handles both single and double chests properly
     */
    public void onEntityClose(Level level, BlockPos pos, BlockState state) {
        if (!this.remove) {
            // Decrement open count
            int oldCount = this.openersCounter.openCount--;
            
            // If this is the last closer, trigger sound and game event
            if (this.openersCounter.openCount == 0) {
                this.openersCounter.onClose(level, pos, state);
                level.gameEvent(null, GameEvent.CONTAINER_CLOSE, pos);
            }
            
            // Trigger animation
            this.openersCounter.openerCountChanged(level, pos, state, oldCount, this.openersCounter.openCount);
            
            // For double chests, also update the connected chest (entities don't use CompoundContainer)
            updateConnectedChestForEntity(level, pos, state, false);
        }
    }
    
    /**
     * Updates the connected chest when an entity opens/closes a double chest
     * This is needed because entities don't use CompoundContainer like players do
     */
    private void updateConnectedChestForEntity(Level level, BlockPos pos, BlockState state, boolean isOpening) {
        ChestType chestType = state.getValue(ChestBlock.TYPE);
        if (chestType != ChestType.SINGLE) {
            Direction connectedDirection = ChestBlock.getConnectedDirection(state);
            BlockPos connectedPos = pos.relative(connectedDirection);
            BlockState connectedState = level.getBlockState(connectedPos);
            
            if (connectedState.getBlock() instanceof BaseCopperChestBlock 
                && connectedState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                
                if (level.getBlockEntity(connectedPos) instanceof CopperChestBlockEntity connectedChest) {
                    // Update the connected chest WITHOUT playing sound
                    if (isOpening) {
                        int oldCount = connectedChest.openersCounter.openCount++;
                        // Don't call onOpen - it would play sound again
                        if (oldCount == 0) {
                            level.gameEvent(null, GameEvent.CONTAINER_OPEN, connectedPos);
                        }
                        connectedChest.openersCounter.openerCountChanged(level, connectedPos, connectedState, 
                            oldCount, connectedChest.openersCounter.openCount);
                    } else {
                        int oldCount = connectedChest.openersCounter.openCount--;
                        // Don't call onClose - it would play sound again
                        if (connectedChest.openersCounter.openCount == 0) {
                            level.gameEvent(null, GameEvent.CONTAINER_CLOSE, connectedPos);
                        }
                        connectedChest.openersCounter.openerCountChanged(level, connectedPos, connectedState, 
                            oldCount, connectedChest.openersCounter.openCount);
                    }
                }
            }
        }
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
/*
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private static final int EVENT_SET_OPEN_COUNT = 1;
    private NonNullList<ItemStack> items;
    public ContainerOpenersCounter openersCounter;
    private final ChestLidController chestLidController;
    private LazyOptional<IItemHandlerModifiable> chestHandler;

    protected ChestBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level p_155357_, BlockPos p_155358_, BlockState p_155359_) {
                ChestBlockEntity.playSound(p_155357_, p_155358_, p_155359_, SoundEvents.CHEST_OPEN);
            }

            protected void onClose(Level p_155367_, BlockPos p_155368_, BlockState p_155369_) {
                ChestBlockEntity.playSound(p_155367_, p_155368_, p_155369_, SoundEvents.CHEST_CLOSE);
            }

            protected void openerCountChanged(Level p_155361_, BlockPos p_155362_, BlockState p_155363_, int p_155364_, int p_155365_) {
                ChestBlockEntity.this.signalOpenCount(p_155361_, p_155362_, p_155363_, p_155364_, p_155365_);
            }

            protected boolean isOwnContainer(Player p_155355_) {
                if (!(p_155355_.containerMenu instanceof ChestMenu)) {
                    return false;
                } else {
                    Container container = ((ChestMenu)p_155355_.containerMenu).getContainer();
                    return container == ChestBlockEntity.this || container instanceof CompoundContainer && ((CompoundContainer)container).contains(ChestBlockEntity.this);
                }
            }
        };
        this.chestLidController = new ChestLidController();
    }

    public ChestBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(BlockEntityType.CHEST, pPos, pBlockState);
    }

    public int getContainerSize() {
        return 27;
    }

    protected Component getDefaultName() {
        return Component.translatable("container.chest");
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(pTag)) {
            ContainerHelper.loadAllItems(pTag, this.items);
        }

    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (!this.trySaveLootTable(pTag)) {
            ContainerHelper.saveAllItems(pTag, this.items);
        }

    }

    public static void lidAnimateTick(Level pLevel, BlockPos pPos, BlockState pState, ChestBlockEntity pBlockEntity) {
        pBlockEntity.chestLidController.tickLid();
    }

    static void playSound(Level pLevel, BlockPos pPos, BlockState pState, SoundEvent pSound) {
        ChestType chesttype = (ChestType)pState.getValue(ChestBlock.TYPE);
        if (chesttype != ChestType.LEFT) {
            double d0 = (double)pPos.getX() + (double)0.5F;
            double d1 = (double)pPos.getY() + (double)0.5F;
            double d2 = (double)pPos.getZ() + (double)0.5F;
            if (chesttype == ChestType.RIGHT) {
                Direction direction = ChestBlock.getConnectedDirection(pState);
                d0 += (double)direction.getStepX() * (double)0.5F;
                d2 += (double)direction.getStepZ() * (double)0.5F;
            }

            pLevel.playSound((Player)null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
        }

    }

    public boolean triggerEvent(int pId, int pType) {
        if (pId == 1) {
            this.chestLidController.shouldBeOpen(pType > 0);
            return true;
        } else {
            return super.triggerEvent(pId, pType);
        }
    }

    public void startOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void stopOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> pItems) {
        this.items = pItems;
    }

    public float getOpenNess(float pPartialTicks) {
        return this.chestLidController.getOpenness(pPartialTicks);
    }

    public static int getOpenCount(BlockGetter pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        if (blockstate.hasBlockEntity()) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof ChestBlockEntity) {
                return ((ChestBlockEntity)blockentity).openersCounter.getOpenerCount();
            }
        }

        return 0;
    }

    public static void swapContents(ChestBlockEntity pChest, ChestBlockEntity pOtherChest) {
        NonNullList<ItemStack> nonnulllist = pChest.getItems();
        pChest.setItems(pOtherChest.getItems());
        pOtherChest.setItems(nonnulllist);
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
        return ChestMenu.threeRows(pId, pPlayer, this);
    }

    public void setBlockState(BlockState p_155251_) {
        super.setBlockState(p_155251_);
        if (this.chestHandler != null) {
            LazyOptional<?> oldHandler = this.chestHandler;
            this.chestHandler = null;
            oldHandler.invalidate();
        }

    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && !this.remove) {
            if (this.chestHandler == null) {
                this.chestHandler = LazyOptional.of(this::createHandler);
            }

            return this.chestHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    private IItemHandlerModifiable createHandler() {
        BlockState state = this.getBlockState();
        Block var3 = state.getBlock();
        if (var3 instanceof ChestBlock chestBlock) {
            Container var4 = ChestBlock.getContainer(chestBlock, state, this.getLevel(), this.getBlockPos(), true);
            return new InvWrapper((Container)(var4 == null ? this : var4));
        } else {
            return new InvWrapper(this);
        }
    }

    public void invalidateCaps() {
        super.invalidateCaps();
        if (this.chestHandler != null) {
            this.chestHandler.invalidate();
            this.chestHandler = null;
        }

    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    protected void signalOpenCount(Level pLevel, BlockPos pPos, BlockState pState, int pEventId, int pEventParam) {
        Block block = pState.getBlock();
        pLevel.blockEvent(pPos, block, 1, pEventParam);
    }
}

*/