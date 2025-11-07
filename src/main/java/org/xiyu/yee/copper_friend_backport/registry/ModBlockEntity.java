package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.copper_chest.CopperChestBlockEntity;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, CopperFriendBackport.MOD_ID
    );

    public static final RegistryObject<BlockEntityType<CopperGolemStatueBlockEntity>> COPPER_GOLEM_STATUE = BLOCK_ENTITY_TYPES.register(
            "copper_golem_statue",
            () -> BlockEntityType.Builder.of(
                    CopperGolemStatueBlockEntity::new,
                    ModBlocks.COPPER_GOLEM_STATUE.get(),
                    ModBlocks.EXPOSED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.WEATHERED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.OXIDIZED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.WAXED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE.get(),
                    ModBlocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE.get()
            ).build(null)
    );

    public static final RegistryObject<BlockEntityType<CopperChestBlockEntity>> COPPER_CHEST = BLOCK_ENTITY_TYPES.register(
            "copper_chest",
            () -> BlockEntityType.Builder.of(
                    CopperChestBlockEntity::new,
                    ModBlocks.COPPER_CHEST.get(),
                    ModBlocks.EXPOSED_COPPER_CHEST.get(),
                    ModBlocks.WEATHERED_COPPER_CHEST.get(),
                    ModBlocks.OXIDIZED_COPPER_CHEST.get(),
                    ModBlocks.WAXED_COPPER_CHEST.get(),
                    ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get(),
                    ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get(),
                    ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get()
            ).build(null)
    );
}
