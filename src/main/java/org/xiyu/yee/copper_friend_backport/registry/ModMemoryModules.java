package org.xiyu.yee.copper_friend_backport.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;

import java.util.Optional;
import java.util.Set;

public class ModMemoryModules {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = 
            DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, CopperFriendBackport.MOD_ID);

    public static final RegistryObject<MemoryModuleType<Integer>> TRANSPORT_ITEMS_COOLDOWN_TICKS = 
            MEMORY_MODULE_TYPES.register("transport_items_cooldown_ticks", 
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Set<GlobalPos>>> VISITED_BLOCK_POSITIONS = 
            MEMORY_MODULE_TYPES.register("visited_block_positions", 
                    () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC.listOf().xmap(Sets::newHashSet, Lists::newArrayList))));

    public static final RegistryObject<MemoryModuleType<Set<GlobalPos>>> UNREACHABLE_TRANSPORT_BLOCK_POSITIONS = 
            MEMORY_MODULE_TYPES.register("unreachable_transport_block_positions", 
                    () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC.listOf().xmap(Sets::newHashSet, Lists::newArrayList))));
}
