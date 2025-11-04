package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.xiyu.yee.copper_friend_backport.world.WeatheringCopperChestBlock;

import java.util.function.Function;

public class ModBlocks {
    public static final Block COPPER_CHEST = register(
            "copper_chest",
            properties -> new WeatheringCopperChestBlock(
                    WeatheringCopper.WeatherState.UNAFFECTED, ModSoundEvents.COPPER_CHEST_OPEN, SoundEvents.COPPER_CHEST_CLOSE, properties
            ),
            BlockBehaviour.Properties.of().mapColor(COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()
    );
    public static final Block EXPOSED_COPPER_CHEST = register(
            "exposed_copper_chest",
            properties -> new WeatheringCopperChestBlock(WeatheringCopper.WeatherState.EXPOSED, SoundEvents.COPPER_CHEST_OPEN, SoundEvents.COPPER_CHEST_CLOSE, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_CHEST).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final Block WEATHERED_COPPER_CHEST = register(
            "weathered_copper_chest",
            properties -> new WeatheringCopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, SoundEvents.COPPER_CHEST_WEATHERED_OPEN, SoundEvents.COPPER_CHEST_WEATHERED_CLOSE, properties
            ),
            BlockBehaviour.Properties.ofFullCopy(COPPER_CHEST).mapColor(MapColor.WARPED_STEM)
    );
    public static final Block OXIDIZED_COPPER_CHEST = register(
            "oxidized_copper_chest",
            properties -> new WeatheringCopperChestBlock(
                    WeatheringCopper.WeatherState.OXIDIZED, SoundEvents.COPPER_CHEST_OXIDIZED_OPEN, SoundEvents.COPPER_CHEST_OXIDIZED_CLOSE, properties
            ),
            BlockBehaviour.Properties.ofFullCopy(COPPER_CHEST).mapColor(MapColor.WARPED_NYLIUM)
    );
    public static final Block WAXED_COPPER_CHEST = register(
            "waxed_copper_chest",
            properties -> new CopperChestBlock(WeatheringCopper.WeatherState.UNAFFECTED, SoundEvents.COPPER_CHEST_OPEN, SoundEvents.COPPER_CHEST_CLOSE, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_CHEST)
    );
    public static final Block WAXED_EXPOSED_COPPER_CHEST = register(
            "waxed_exposed_copper_chest",
            properties -> new CopperChestBlock(WeatheringCopper.WeatherState.EXPOSED, SoundEvents.COPPER_CHEST_OPEN, SoundEvents.COPPER_CHEST_CLOSE, properties),
            BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_CHEST)
    );
    public static final Block WAXED_WEATHERED_COPPER_CHEST = register(
            "waxed_weathered_copper_chest",
            properties -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.WEATHERED, SoundEvents.COPPER_CHEST_WEATHERED_OPEN, SoundEvents.COPPER_CHEST_WEATHERED_CLOSE, properties
            ),
            BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_CHEST)
    );
    public static final Block WAXED_OXIDIZED_COPPER_CHEST = register(
            "waxed_oxidized_copper_chest",
            properties -> new CopperChestBlock(
                    WeatheringCopper.WeatherState.OXIDIZED, SoundEvents.COPPER_CHEST_OXIDIZED_OPEN, SoundEvents.COPPER_CHEST_OXIDIZED_CLOSE, properties
            ),
            BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_CHEST)
    );
    public static final Block COPPER_GOLEM_STATUE = register(
            "copper_golem_statue",
            properties -> new WeatheringCopperGolemStatueBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
            BlockBehaviour.Properties.of()
                    .mapColor(COPPER_BLOCK.defaultMapColor())
                    .strength(3.0F, 6.0F)
                    .sound(ModSoundType.COPPER_GOLEM_STATUE)
                    .pushReaction(PushReaction.DESTROY)
    );
    public static final Block EXPOSED_COPPER_GOLEM_STATUE = register(
            "exposed_copper_golem_statue",
            properties -> new WeatheringCopperGolemStatueBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_GOLEM_STATUE).mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
    );
    public static final Block WEATHERED_COPPER_GOLEM_STATUE = register(
            "weathered_copper_golem_statue",
            properties -> new WeatheringCopperGolemStatueBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_GOLEM_STATUE).mapColor(MapColor.WARPED_STEM)
    );
    public static final Block OXIDIZED_COPPER_GOLEM_STATUE = register(
            "oxidized_copper_golem_statue",
            properties -> new WeatheringCopperGolemStatueBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_GOLEM_STATUE).mapColor(MapColor.WARPED_NYLIUM)
    );
    public static final Block WAXED_COPPER_GOLEM_STATUE = register(
            "waxed_copper_golem_statue",
            properties -> new CopperGolemStatueBlock(WeatheringCopper.WeatherState.UNAFFECTED, properties),
            BlockBehaviour.Properties.ofFullCopy(COPPER_GOLEM_STATUE)
    );
    public static final Block WAXED_EXPOSED_COPPER_GOLEM_STATUE = register(
            "waxed_exposed_copper_golem_statue",
            properties -> new CopperGolemStatueBlock(WeatheringCopper.WeatherState.EXPOSED, properties),
            BlockBehaviour.Properties.ofFullCopy(EXPOSED_COPPER_GOLEM_STATUE)
    );
    public static final Block WAXED_WEATHERED_COPPER_GOLEM_STATUE = register(
            "waxed_weathered_copper_golem_statue",
            properties -> new CopperGolemStatueBlock(WeatheringCopper.WeatherState.WEATHERED, properties),
            BlockBehaviour.Properties.ofFullCopy(WEATHERED_COPPER_GOLEM_STATUE)
    );
    public static final Block WAXED_OXIDIZED_COPPER_GOLEM_STATUE = register(
            "waxed_oxidized_copper_golem_statue",
            properties -> new CopperGolemStatueBlock(WeatheringCopper.WeatherState.OXIDIZED, properties),
            BlockBehaviour.Properties.ofFullCopy(OXIDIZED_COPPER_GOLEM_STATUE)
    );

    private static Block register(String string, Function<BlockBehaviour.Properties, Block> function, BlockBehaviour.Properties properties) {
        return register(vanillaBlockId(string).toString(), function, properties);
    }

    private static Block register(String string, BlockBehaviour.Properties properties) {
        return register(string, Block::new, properties);
    }
    private static ResourceKey<Block> vanillaBlockId(String string) {
        return ResourceKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace(string));
    }
}
