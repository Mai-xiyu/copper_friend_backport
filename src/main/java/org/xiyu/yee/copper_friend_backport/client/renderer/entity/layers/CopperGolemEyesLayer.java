package org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels;

@OnlyIn(Dist.CLIENT)
public class CopperGolemEyesLayer extends RenderLayer<CopperGolem, CopperGolemModel<CopperGolem>> {

    public CopperGolemEyesLayer(RenderLayerParent<CopperGolem, CopperGolemModel<CopperGolem>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CopperGolem entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation eyeTexture = CopperGolemOxidationLevels.getOxidationLevel(entity.getWeatherState()).eyeTexture();
        
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.eyes(eyeTexture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
