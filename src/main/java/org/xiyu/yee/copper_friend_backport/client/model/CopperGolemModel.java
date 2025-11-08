package org.xiyu.yee.copper_friend_backport.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.animation.CopperGolemAnimation;
import org.xiyu.yee.copper_friend_backport.client.animation.CopperGolemAnimations;
import org.xiyu.yee.copper_friend_backport.client.animation.KeyframeAnimation;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

@OnlyIn(Dist.CLIENT)
public class CopperGolemModel<T extends LivingEntity> extends HierarchicalModel<T> implements ArmedModel, HeadedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
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
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        
        // 烘焙动画
        this.walkAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_WALK);
        this.walkWithItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_WALK_ITEM);
        this.spinHeadAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_IDLE);
        this.gettingItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_GET);
        this.gettingNoItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_NOGET);
        this.droppingItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_DROP);
        this.droppingNoItemAnimation = KeyframeAnimation.bake(root, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_NODROP);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 身体 - 修正UV坐标从14改为15
        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // 头部
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -5.0F, -5.0F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.015F))
                        .texOffs(56, 0)
                        .addBox(-1.0F, -2.0F, -6.0F, 2.0F, 3.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(37, 8)
                        .addBox(-1.0F, -9.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))
                        .texOffs(37, 0)
                        .addBox(-2.0F, -13.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F)),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        // 右臂 - 修正UV坐标从14改为16
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(36, 16)
                        .addBox(-3.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-4.0F, -6.0F, 0.0F));

        // 左臂 - 修正UV坐标从14改为16
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(50, 16)
                        .addBox(0.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(4.0F, -6.0F, 0.0F));

        // 右腿 - 修正UV坐标从26改为27
        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .addBox(-4.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // 左腿 - 修正UV坐标从26改为27
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
        // 重置所有旋转
        this.root().getAllParts().forEach(ModelPart::resetPose);
        
        // 铜傀儡特有动画
        if (entity instanceof CopperGolem copperGolem) {
            // 头部朝向 - 对应 1.21 的 pitch 和 yaw
            // 必须在动画应用之前设置，让动画可以覆盖（与原版一致）
            this.head.xRot = headPitch * ((float)Math.PI / 180F);
            this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
            
            // 行走动画 - 根据是否持有物品使用不同动画
            boolean hasItem = !copperGolem.getMainHandItem().isEmpty() || !copperGolem.getOffhandItem().isEmpty();
            
            if (hasItem) {
                // 持有物品时的行走动画
                this.walkWithItemAnimation.applyWalk(limbSwing, limbSwingAmount, 2.0F, 2.5F);
                this.poseHeldItemArmsIfStill();
            } else {
                // 无物品时的行走动画
                this.walkAnimation.applyWalk(limbSwing, limbSwingAmount, 2.0F, 2.5F);
            }
            
            // 应用各种动画状态 - 使用关键帧动画系统
            // 转头动画会覆盖上面设置的 head.yRot
            this.spinHeadAnimation.apply(copperGolem.getHeadSpinAnimationState(), ageInTicks);
            this.gettingItemAnimation.apply(copperGolem.getInteractionGetItemAnimationState(), ageInTicks);
            this.gettingNoItemAnimation.apply(copperGolem.getInteractionGetNoItemAnimationState(), ageInTicks);
            this.droppingItemAnimation.apply(copperGolem.getInteractionDropItemAnimationState(), ageInTicks);
            this.droppingNoItemAnimation.apply(copperGolem.getInteractionDropNoItemAnimationState(), ageInTicks);
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        ModelPart armPart = arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        armPart.translateAndRotate(poseStack);
        
        // 根据铜傀儡状态调整物品渲染位置（对应 1.21 的 setArmAngle 逻辑）
        // 在 IDLE 状态下，物品会以不同角度渲染
        // 注意：这里无法直接获取 CopperGolem 实例，所以使用通用逻辑
        // 如果需要状态感知，需要在渲染器中处理
        poseStack.scale(0.55F, 0.55F, 0.55F);
        poseStack.translate(-0.125F, 0.3125F, -0.1875F);
    }

    @Override
    public ModelPart getHead() {
        return this.head;
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
