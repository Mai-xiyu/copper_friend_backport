package org.xiyu.yee.copper_friend_backport.copper_chest;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.xiyu.yee.copper_friend_backport.client.renderer.item.CopperChestItemRenderer;

import java.util.function.Consumer;

/**
 * Custom BlockItem for copper chests that uses a special renderer for item display
 */
public class CopperChestBlockItem extends BlockItem {
    
    public CopperChestBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new CopperChestItemRenderer();
            }
        });
    }
}
