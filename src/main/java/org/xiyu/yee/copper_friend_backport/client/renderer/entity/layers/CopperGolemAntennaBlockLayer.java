package org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

@OnlyIn(Dist.CLIENT)
public class CopperGolemAntennaBlockLayer extends RenderLayer<CopperGolem, CopperGolemModel<CopperGolem>> {
    private final BlockRenderDispatcher blockRenderer;

    public CopperGolemAntennaBlockLayer(RenderLayerParent<CopperGolem, CopperGolemModel<CopperGolem>> renderer, BlockRenderDispatcher blockRenderer) {
        super(renderer);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CopperGolem entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = entity.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA);
        
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState blockState = blockItem.getBlock().defaultBlockState();
            
            poseStack.pushPose();
            this.getParentModel().applyBlockOnAntennaTransform(poseStack);
            poseStack.translate(0.0F, -0.5F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            this.blockRenderer.renderSingleBlock(blockState, poseStack, buffer, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            
            poseStack.popPose();
        }
    }
}
