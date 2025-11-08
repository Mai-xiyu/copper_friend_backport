package org.xiyu.yee.copper_friend_backport.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.xiyu.yee.copper_friend_backport.registry.ModModelLayers;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

import java.util.Map;

public class CopperGolemStatueModel implements BlockEntityRenderer<CopperGolemStatueBlockEntity> {
    static final Map<CopperGolemStatueBlock.Pose, ModelLayerLocation> MODELS = Map.of(
            CopperGolemStatueBlock.Pose.STANDING,
            ModModelLayers.COPPER_GOLEM,
            CopperGolemStatueBlock.Pose.SITTING,
            ModModelLayers.COPPER_GOLEM_SITTING,
            CopperGolemStatueBlock.Pose.STAR,
            ModModelLayers.COPPER_GOLEM_STAR,
            CopperGolemStatueBlock.Pose.RUNNING,
            ModModelLayers.COPPER_GOLEM_RUNNING
    );
    private final CopperGolemStatueBlock.Pose pose;
    private final ModelPart root;

    public CopperGolemStatueModel(CopperGolemStatueBlock.Pose pose, ModelPart modelPart) {
        this.pose = pose;
        this.root = modelPart;
	}

	public void setupAnim(Direction direction) {
		this.root.y = 0.0F;
		this.root.yRot = direction.getOpposite().toYRot() * (float) (Math.PI / 180.0);
		this.root.zRot = (float) Math.PI;
	}

    @Override
    public void render(CopperGolemStatueBlockEntity blockEntity, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
        this.root.render(p_112309_,p_112310_.getBuffer(RenderType.entityCutoutNoCull(MODELS.get(this.pose).getModel())),p_112311_,p_112311_);
    }
}