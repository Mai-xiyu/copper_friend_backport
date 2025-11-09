package org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

/**
 * 铜傀儡虞美人渲染层
 * 当铜傀儡头上有虞美人时，在天线顶部渲染虞美人方块
 */
@OnlyIn(Dist.CLIENT)
public class CopperGolemPoppyLayer extends RenderLayer<CopperGolem, CopperGolemModel<CopperGolem>> {
    
    public CopperGolemPoppyLayer(RenderLayerParent<CopperGolem, CopperGolemModel<CopperGolem>> renderer) {
        super(renderer);
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CopperGolem copperGolem,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        // 只有当铜傀儡有虞美人时才渲染
        if (!copperGolem.hasPoppy()) {
            return;
        }

        poseStack.pushPose();
        
        		// 应用天线的变换
		this.getParentModel().getAntenna().translateAndRotate(poseStack);
		
		// 天线顶部位置（天线模型中天线头的顶部在Y=-8/16）
		poseStack.translate(0, -0.5F, 0);
		
		// 旋转180度让虞美人正向朝上
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		
		// 缩小虞美人
		poseStack.scale(0.5F, 0.5F, 0.5F);
		
		// 调整位置：将虞美人底部放在天线顶部，消除间隙
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		
		// 渲染虞美人方块
        BlockState poppyState = Blocks.POPPY.defaultBlockState();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        blockRenderer.renderSingleBlock(
            poppyState,
            poseStack,
            buffer,
            packedLight,
            OverlayTexture.NO_OVERLAY
        );
        
        poseStack.popPose();
    }
}
