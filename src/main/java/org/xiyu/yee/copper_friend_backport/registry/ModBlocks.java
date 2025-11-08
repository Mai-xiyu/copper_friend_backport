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
import org.xiyu.yee.copper_friend_backport.world.CopperChestBlock;
import org.xiyu.yee.copper_friend_backport.world.WeatheringCopperGolemStatueBlock;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS, CopperFriendBackport.MOD_ID
    );

    // Copper Chests - Oxidizable variants
    public static final RegistryObject<Block> COPPER_CHEST = BLOCKS.register(
            "copper_chest",
            () -> new CopperChestBlock(
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
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.EXPOSED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
                            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            )
    );
    
    public static final RegistryObject<Block> WEATHERED_COPPER_CHEST = BLOCKS.register(
            "weathered_copper_chest",
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
                            .mapColor(MapColor.WARPED_STEM)
            )
    );
    
    public static final RegistryObject<Block> OXIDIZED_COPPER_CHEST = BLOCKS.register(
            "oxidized_copper_chest",
            () -> new CopperChestBlock(
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
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(COPPER_CHEST.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_EXPOSED_COPPER_CHEST = BLOCKS.register(
            "waxed_exposed_copper_chest",
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.EXPOSED, 
                    ModSoundEvents.COPPER_CHEST_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(EXPOSED_COPPER_CHEST.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_WEATHERED_COPPER_CHEST = BLOCKS.register(
            "waxed_weathered_copper_chest",
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_WEATHERED_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(WEATHERED_COPPER_CHEST.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_OXIDIZED_COPPER_CHEST = BLOCKS.register(
            "waxed_oxidized_copper_chest",
            () -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.OXIDIZED, 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_OPEN.get(), 
                    ModSoundEvents.COPPER_CHEST_OXIDIZED_CLOSE.get(), 
                    BlockBehaviour.Properties.copy(OXIDIZED_COPPER_CHEST.get())
            )
    );
    
    // Copper Golem Statues - Oxidizable variants
    public static final RegistryObject<Block> COPPER_GOLEM_STATUE = BLOCKS.register(
            "copper_golem_statue",
            () -> new WeatheringCopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .pushReaction(PushReaction.DESTROY)
            )
    );
    
    public static final RegistryObject<Block> EXPOSED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "exposed_copper_golem_statue",
            () -> new WeatheringCopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.EXPOSED,
                    BlockBehaviour.Properties.copy(COPPER_GOLEM_STATUE.get())
                            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            )
    );
    
    public static final RegistryObject<Block> WEATHERED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "weathered_copper_golem_statue",
            () -> new WeatheringCopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.WEATHERED,
                    BlockBehaviour.Properties.copy(COPPER_GOLEM_STATUE.get())
                            .mapColor(MapColor.WARPED_STEM)
            )
    );
    
    public static final RegistryObject<Block> OXIDIZED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "oxidized_copper_golem_statue",
            () -> new WeatheringCopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.OXIDIZED,
                    BlockBehaviour.Properties.copy(COPPER_GOLEM_STATUE.get())
                            .mapColor(MapColor.WARPED_NYLIUM)
            )
    );
    
    // Copper Golem Statues - Waxed variants
    public static final RegistryObject<Block> WAXED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "waxed_copper_golem_statue",
            () -> new CopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED,
                    BlockBehaviour.Properties.copy(COPPER_GOLEM_STATUE.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_EXPOSED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "waxed_exposed_copper_golem_statue",
            () -> new CopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.EXPOSED,
                    BlockBehaviour.Properties.copy(EXPOSED_COPPER_GOLEM_STATUE.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_WEATHERED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "waxed_weathered_copper_golem_statue",
            () -> new CopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.WEATHERED,
                    BlockBehaviour.Properties.copy(WEATHERED_COPPER_GOLEM_STATUE.get())
            )
    );
    
    public static final RegistryObject<Block> WAXED_OXIDIZED_COPPER_GOLEM_STATUE = BLOCKS.register(
            "waxed_oxidized_copper_golem_statue",
            () -> new CopperGolemStatueBlock(
                    WeatheringCopper.WeatherState.OXIDIZED,
                    BlockBehaviour.Properties.copy(OXIDIZED_COPPER_GOLEM_STATUE.get())
            )
    );
}
