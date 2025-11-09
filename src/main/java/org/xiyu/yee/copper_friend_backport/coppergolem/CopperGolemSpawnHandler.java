package org.xiyu.yee.copper_friend_backport.coppergolem;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Handles the spawning of Copper Golems when a pumpkin is placed on top of a copper block.
 * Similar to Iron Golem and Snow Golem spawning mechanics.
 */
@Mod.EventBusSubscriber(modid = CopperFriendBackport.MOD_ID)
public class CopperGolemSpawnHandler {

    /**
     * Called when a block is placed in the world.
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockState placedState = event.getPlacedBlock();
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();

        // Check if a pumpkin or carved pumpkin was placed
        if (placedState.getBlock() instanceof CarvedPumpkinBlock || placedState.is(Blocks.CARVED_PUMPKIN) || placedState.is(Blocks.JACK_O_LANTERN)) {
            trySpawnCopperGolem(level, pos);
        }
    }

    /**
     * Attempts to spawn a Copper Golem when a pumpkin is placed.
     * Structure: Copper Block + Carved Pumpkin/Jack O'Lantern on any face
     */
    private static void trySpawnCopperGolem(Level level, BlockPos pumpkinPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockState pumpkinState = level.getBlockState(pumpkinPos);
        boolean isJackOLantern = pumpkinState.is(Blocks.JACK_O_LANTERN);

        // Check all 6 adjacent positions for a copper block
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos copperBlockPos = pumpkinPos.relative(direction);
            BlockState copperBlockState = level.getBlockState(copperBlockPos);
            Block copperBlock = copperBlockState.getBlock();

            // Check if it's a copper block (vanilla or waxed)
            CopperBlockInfo copperInfo = getCopperBlockInfo(copperBlock);
            if (copperInfo != null) {
                // Found a copper block! Spawn the golem
                
                // Remove both blocks
                level.removeBlock(pumpkinPos, false);
                level.removeBlock(copperBlockPos, false);

                // FIRST: Place corresponding copper chest at copper block position
                Block chestBlock = getCorrespondingChest(copperInfo);
                // For vertical directions (UP/DOWN), chest facing must be horizontal
                // Default to NORTH for vertical placements
                net.minecraft.core.Direction chestFacing = direction.getOpposite();
                if (direction.getAxis() == net.minecraft.core.Direction.Axis.Y) {
                    chestFacing = net.minecraft.core.Direction.NORTH;
                }

                BlockState chestState = chestBlock.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.ChestBlock.FACING, chestFacing)
                        .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.SINGLE);
                level.setBlock(copperBlockPos, chestState, 3);

                // Play chest placement sound
                level.playSound(null, copperBlockPos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

                // SECOND: Spawn Copper Golem at pumpkin position
                CopperGolem copperGolem = ModEntity.COPPER_GOLEM.get().create(serverLevel);
                if (copperGolem != null) {
                    copperGolem.moveTo(pumpkinPos.getX() + 0.5, pumpkinPos.getY(), pumpkinPos.getZ() + 0.5, 0.0F, 0.0F);
                    
                    // Set oxidation state (but NOT waxed status - waxed copper blocks create unwaxed golems)
                    copperGolem.setWeatherState(copperInfo.weatherState);
                    
                    // If spawned with Jack O'Lantern, make the golem emit light level 14
                    if (isJackOLantern) {
                        copperGolem.setLantern(true);
                    }
                    
                    serverLevel.addFreshEntity(copperGolem);

                    // Play golem spawn sound
                    level.playSound(null, pumpkinPos, org.xiyu.yee.copper_friend_backport.registry.ModSoundEvents.COPPER_GOLEM_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                
                return; // Found and processed, exit
            }
        }
    }

    /**
     * Gets information about a copper block.
     */
    @Nullable
    private static CopperBlockInfo getCopperBlockInfo(Block block) {
        // Vanilla copper blocks (unwaxed)
        if (block == Blocks.COPPER_BLOCK) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.UNAFFECTED, false);
        } else if (block == Blocks.EXPOSED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.EXPOSED, false);
        } else if (block == Blocks.WEATHERED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.WEATHERED, false);
        } else if (block == Blocks.OXIDIZED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.OXIDIZED, false);
        }
        // Vanilla copper blocks (waxed)
        else if (block == Blocks.WAXED_COPPER_BLOCK) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.UNAFFECTED, true);
        } else if (block == Blocks.WAXED_EXPOSED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.EXPOSED, true);
        } else if (block == Blocks.WAXED_WEATHERED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.WEATHERED, true);
        } else if (block == Blocks.WAXED_OXIDIZED_COPPER) {
            return new CopperBlockInfo(WeatheringCopper.WeatherState.OXIDIZED, true);
        }

        return null;
    }

    /**
     * Gets the corresponding chest block for the copper block info.
     */
    private static @NotNull Block getCorrespondingChest(CopperBlockInfo info) {
        if (info.isWaxed) {
            return switch (info.weatherState) {
                case UNAFFECTED -> ModBlocks.WAXED_COPPER_CHEST.get();
                case EXPOSED -> ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get();
                case WEATHERED -> ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get();
                case OXIDIZED -> ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get();
            };
        } else {
            return switch (info.weatherState) {
                case UNAFFECTED -> ModBlocks.COPPER_CHEST.get();
                case EXPOSED -> ModBlocks.EXPOSED_COPPER_CHEST.get();
                case WEATHERED -> ModBlocks.WEATHERED_COPPER_CHEST.get();
                case OXIDIZED -> ModBlocks.OXIDIZED_COPPER_CHEST.get();
            };
        }
    }

    /**
     * Helper class to store copper block information.
     */
    private static class CopperBlockInfo {
        final WeatheringCopper.WeatherState weatherState;
        final boolean isWaxed;

        CopperBlockInfo(WeatheringCopper.WeatherState weatherState, boolean isWaxed) {
            this.weatherState = weatherState;
            this.isWaxed = isWaxed;
        }
    }
}
