package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.copper_chest.OxidizableCopperChestBlock;
import org.xiyu.yee.copper_friend_backport.copper_chest.WaxedCopperChestBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS, CopperFriendBackport.MOD_ID
    );

    // Copper Chests - Oxidizable variants
    public static final RegistryObject<Block> COPPER_CHEST = BLOCKS.register(
            "copper_chest",
            () -> new OxidizableCopperChestBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .requiresCorrectToolForDrops()
            )
    );
    
    public static final RegistryObject<Block> EXPOSED_COPPER_CHEST = BLOCKS.register(
            "exposed_copper_chest",
            () -> new OxidizableCopperChestBlock(
                    WeatheringCopper.WeatherState.EXPOSED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
                            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            )
    );
    
    public static final RegistryObject<Block> WEATHERED_COPPER_CHEST = BLOCKS.register(
            "weathered_copper_chest",
            () -> new OxidizableCopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
                            .mapColor(MapColor.WARPED_STEM)
            )
    );
    
    public static final RegistryObject<Block> OXIDIZED_COPPER_CHEST = BLOCKS.register(
            "oxidized_copper_chest",
            () -> new OxidizableCopperChestBlock(
                    WeatheringCopper.WeatherState.OXIDIZED, 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
                            .mapColor(MapColor.WARPED_NYLIUM)
            )
    );
    
    // Copper Chests - Waxed variants
    public static final RegistryObject<Block> WAXED_COPPER_CHEST = BLOCKS.register(
            "waxed_copper_chest",
            () -> new WaxedCopperChestBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .requiresCorrectToolForDrops()
            )
    );
    
    public static final RegistryObject<Block> WAXED_EXPOSED_COPPER_CHEST = BLOCKS.register(
            "waxed_exposed_copper_chest",
            () -> new WaxedCopperChestBlock(
                    WeatheringCopper.WeatherState.EXPOSED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .requiresCorrectToolForDrops()
            )
    );
    
    public static final RegistryObject<Block> WAXED_WEATHERED_COPPER_CHEST = BLOCKS.register(
            "waxed_weathered_copper_chest",
            () -> new WaxedCopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get(), 
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WARPED_STEM)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .requiresCorrectToolForDrops()
            )
    );
    
    public static final RegistryObject<Block> WAXED_OXIDIZED_COPPER_CHEST = BLOCKS.register(
            "waxed_oxidized_copper_chest",
            () -> new WaxedCopperChestBlock(
                    WeatheringCopper.WeatherState.OXIDIZED, 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get(), 
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WARPED_NYLIUM)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .requiresCorrectToolForDrops()
            )
    );
}
