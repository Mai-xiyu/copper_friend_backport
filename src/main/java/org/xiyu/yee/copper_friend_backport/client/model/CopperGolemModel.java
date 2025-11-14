package org.xiyu.yee.copper_friend_backport.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.animation.CopperGolemAnimation;
import org.xiyu.yee.copper_friend_backport.client.animation.KeyframeAnimation;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

@OnlyIn(Dist.CLIENT)
public class CopperGolemModel<T extends LivingEntity> extends HierarchicalModel<T> implements ArmedModel, HeadedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart antenna; // 天线作为独立部件
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    
    // 关键帧动画
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation walkWithItemAnimation;
    private final KeyframeAnimation spinHeadAnimation;
    private final KeyframeAnimation gettingItemAnimation;
    private final KeyframeAnimation gettingNoItemAnimation;
    private final KeyframeAnimation droppingItemAnimation;
    private final KeyframeAnimation droppingNoItemAnimation;

    public CopperGolemModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.antenna = this.head.getChild("antenna");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");

        this.walkAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_WALK);
        this.walkWithItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_WALK_ITEM);
        this.spinHeadAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_SPIN_HEAD);
        this.gettingItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_GET);
        this.gettingNoItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_NOGET);
        this.droppingItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_DROP);
        this.droppingNoItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_NODROP);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -5.0F, -5.0F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.015F))
                        .texOffs(56, 0)
                        .addBox(-1.0F, -2.0F, -6.0F, 2.0F, 3.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition antenna = head.addOrReplaceChild("antenna",
                CubeListBuilder.create()
                        .texOffs(37, 8)
                        .addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))  // 天线杆
                        .texOffs(37, 0)
                        .addBox(-2.0F, -8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F)), // 天线头
                PartPose.offset(0.0F, -5.0F, 0.0F)); // 天线位置在头部顶部（Y=-5）

        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(36, 16)
                        .addBox(-3.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-4.0F, -6.0F, 0.0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(50, 16)
                        .addBox(0.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(4.0F, -6.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(16, 27)
                        .addBox(0.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity instanceof CopperGolem copperGolem) {
            boolean hasItem = !copperGolem.getMainHandItem().isEmpty() || !copperGolem.getOffhandItem().isEmpty();
            
            if (hasItem) {
                this.walkWithItemAnimation.applyWalk(limbSwing, limbSwingAmount, 2.0F, 2.5F);
                this.poseHeldItemArmsIfStill();
            } else {
                this.walkAnimation.applyWalk(limbSwing, limbSwingAmount, 2.0F, 2.5F);
            }

            this.spinHeadAnimation.apply(copperGolem.getHeadSpinAnimationState(), ageInTicks);
            this.gettingItemAnimation.apply(copperGolem.getInteractionGetItemAnimationState(), ageInTicks);
            this.gettingNoItemAnimation.apply(copperGolem.getInteractionGetNoItemAnimationState(), ageInTicks);
            this.droppingItemAnimation.apply(copperGolem.getInteractionDropItemAnimationState(), ageInTicks);
            this.droppingNoItemAnimation.apply(copperGolem.getInteractionDropNoItemAnimationState(), ageInTicks);

            /*boolean isInteracting = copperGolem.getInteractionGetItemAnimationState().isStarted() ||
                                    copperGolem.getInteractionGetNoItemAnimationState().isStarted() ||
                                    copperGolem.getInteractionDropItemAnimationState().isStarted() ||
                                    copperGolem.getInteractionDropNoItemAnimationState().isStarted();
            
            if (!isInteracting) {
            }*/
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        ModelPart armPart = arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        armPart.translateAndRotate(poseStack);

        poseStack.scale(0.55F, 0.55F, 0.55F);
        poseStack.translate(-0.125F, 0.3125F, -0.1875F);
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    public ModelPart getAntenna() {
        return this.antenna;
    }

    public ModelPart getRightArm() {
        return this.rightArm;
    }

    public ModelPart getLeftArm() {
        return this.leftArm;
    }

    public void applyBlockOnAntennaTransform(PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.8125F, 0.0F);
    }

    private void poseHeldItemArmsIfStill() {
        this.rightArm.xRot = Math.min(this.rightArm.xRot, -0.87266463F);
        this.leftArm.xRot = Math.min(this.leftArm.xRot, -0.87266463F);
        this.rightArm.yRot = Math.min(this.rightArm.yRot, -0.1134464F);
        this.leftArm.yRot = Math.max(this.leftArm.yRot, 0.1134464F);
        this.rightArm.zRot = Math.min(this.rightArm.zRot, -0.064577185F);
        this.leftArm.zRot = Math.max(this.leftArm.zRot, 0.064577185F);
    }
}
