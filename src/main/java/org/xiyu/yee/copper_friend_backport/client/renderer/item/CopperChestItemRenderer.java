package org.xiyu.yee.copper_friend_backport.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.xiyu.yee.copper_friend_backport.copper_chest.BaseCopperChestBlock;
import org.xiyu.yee.copper_friend_backport.copper_chest.CopperChestBlockEntity;

/**
 * Custom item renderer for copper chests to display them properly in inventory
 */
public class CopperChestItemRenderer extends BlockEntityWithoutLevelRenderer {
    
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public CopperChestItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                            MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Block block = Block.byItem(stack.getItem());
        
        if (block instanceof BaseCopperChestBlock chestBlock) {
            // Create a temporary BlockEntity for rendering
            BlockEntity blockEntity = new CopperChestBlockEntity(BlockPos.ZERO, chestBlock.defaultBlockState());
            
            // Render the block entity
            this.blockEntityRenderDispatcher.renderItem(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
        }
    }
}
