package org.xiyu.yee.copper_friend_backport.registry;

import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

import java.util.Set;

public class ModBlockEntity {
    public static final BlockEntityType<CopperGolemStatueBlockEntity> COPPER_GOLEM_STATUE = register(
            "copper_golem_statue",
            CopperGolemStatueBlockEntity::new,
            ModBlocks.COPPER_GOLEM_STATUE,
            ModBlocks.EXPOSED_COPPER_GOLEM_STATUE,
            ModBlocks.WEATHERED_COPPER_GOLEM_STATUE,
            ModBlocks.OXIDIZED_COPPER_GOLEM_STATUE,
            ModBlocks.WAXED_COPPER_GOLEM_STATUE,
            ModBlocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE,
            ModBlocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
            ModBlocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE
    );
    private static <T extends BlockEntity> BlockEntityType<T> register(
            String string, BlockEntityType.BlockEntitySupplier<? extends T> blockEntitySupplier, Block... blocks
    ) {
        if (blocks.length == 0) {
            CopperFriendBackport.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", string);
        }

        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, string);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, string, new BlockEntityType<>(blockEntitySupplier, Set.of(blocks),type));
    }
}
