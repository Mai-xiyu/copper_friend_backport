package org.xiyu.yee.copper_friend_backport.registry;

import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, CopperFriendBackport.MOD_ID
    );

    public static final RegistryObject<BlockEntityType<CopperGolemStatueBlockEntity>> COPPER_GOLEM_STATUE = register(
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
            )
    );
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String id, Supplier<BlockEntityType.Builder<T>> builder) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, id);
        return BLOCK_ENTITY_TYPES.register(id,()-> builder.get().build(type));
    }
}
