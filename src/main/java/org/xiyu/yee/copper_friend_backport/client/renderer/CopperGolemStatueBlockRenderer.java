package org.xiyu.yee.copper_friend_backport.client.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.xiyu.yee.copper_friend_backport.client.CopperGolemStatueRenderState;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemStatueModel;
import org.xiyu.yee.copper_friend_backport.registry.ModModelLayers;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class CopperGolemStatueBlockRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity> {
	private final Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> models = new HashMap();

	public CopperGolemStatueBlockRenderer(BlockEntityRendererProvider.Context context) {
		EntityModelSet entityModelSet = context.getModelSet();
		this.models.put(CopperGolemStatueBlock.Pose.STANDING, new CopperGolemStatueModel(CopperGolemStatueBlock.Pose.STANDING,entityModelSet.bakeLayer(ModModelLayers.COPPER_GOLEM)));
		this.models.put(CopperGolemStatueBlock.Pose.RUNNING, new CopperGolemStatueModel(CopperGolemStatueBlock.Pose.RUNNING,entityModelSet.bakeLayer(ModModelLayers.COPPER_GOLEM_RUNNING)));
		this.models.put(CopperGolemStatueBlock.Pose.SITTING, new CopperGolemStatueModel(CopperGolemStatueBlock.Pose.SITTING,entityModelSet.bakeLayer(ModModelLayers.COPPER_GOLEM_SITTING)));
		this.models.put(CopperGolemStatueBlock.Pose.STAR, new CopperGolemStatueModel(CopperGolemStatueBlock.Pose.STAR,entityModelSet.bakeLayer(ModModelLayers.COPPER_GOLEM_STAR)));
	}

	public CopperGolemStatueRenderState createRenderState() {
		return new CopperGolemStatueRenderState();
	}

	public void extractRenderState(
		CopperGolemStatueBlockEntity copperGolemStatueBlockEntity,
		CopperGolemStatueRenderState copperGolemStatueRenderState,
		float f,
		Vec3 vec3,
		ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(copperGolemStatueBlockEntity, copperGolemStatueRenderState, f, vec3, crumblingOverlay);
		copperGolemStatueRenderState.direction = (Direction)copperGolemStatueBlockEntity.getBlockState().getValue(CopperGolemStatueBlock.FACING);
		copperGolemStatueRenderState.pose = (Pose)copperGolemStatueBlockEntity.getBlockState().getValue(BlockStateProperties.COPPER_GOLEM_POSE);
	}

	public void submit(
            CopperGolemStatueRenderState copperGolemStatueRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState
	) {
		if (copperGolemStatueRenderState.blockState.getBlock() instanceof CopperGolemStatueBlock copperGolemStatueBlock) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.0F, 0.5F);
			CopperGolemStatueModel copperGolemStatueModel = (CopperGolemStatueModel)this.models.get(copperGolemStatueRenderState.pose);
			Direction direction = copperGolemStatueRenderState.direction;
			RenderType renderType = RenderType.entityCutoutNoCull(CopperGolemOxidationLevels.getOxidationLevel(copperGolemStatueBlock.getWeatheringState()).texture());
			submitNodeCollector.submitModel(
				copperGolemStatueModel,
				direction,
				poseStack,
				renderType,
				copperGolemStatueRenderState.lightCoords,
				OverlayTexture.NO_OVERLAY,
				0,
				copperGolemStatueRenderState.breakProgress
			);
			poseStack.popPose();
		}
	}

    @Override
    public void render(CopperGolemStatueBlockEntity p_112307_, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        this.root.render(p_112309_,p_112310_.getBuffer(RenderType.entityCutoutNoCull(MODELS.get(this.pose).getModel())),p_112311_,p_112311_);
    }
}
