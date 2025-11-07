package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public class ModBlockTags {
    public static final TagKey<Block> COPPER_CHESTS = create("copper_chests");
    public static final TagKey<Block> COPPER_GOLEM_STATUES = create("copper_golem_statues");
    
    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Objects.requireNonNull(ResourceLocation.tryBuild("copper_friend_backport", name)));
    }
}
