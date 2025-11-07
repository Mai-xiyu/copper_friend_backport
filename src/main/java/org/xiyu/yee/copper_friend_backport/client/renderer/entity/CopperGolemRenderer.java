package org.xiyu.yee.copper_friend_backport.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xiyu.yee.copper_friend_backport.client.ClientSetup;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers.CopperGolemAntennaBlockLayer;
import org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers.CopperGolemEyesLayer;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels;

@OnlyIn(Dist.CLIENT)
public class CopperGolemRenderer extends MobRenderer<CopperGolem, CopperGolemModel<CopperGolem>> {
    
    public CopperGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperGolemModel<>(context.bakeLayer(ClientSetup.COPPER_GOLEM_LAYER)), 0.5F);
        this.addLayer(new CopperGolemEyesLayer(this));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CopperGolemAntennaBlockLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public ResourceLocation getTextureLocation(CopperGolem entity) {
        return CopperGolemOxidationLevels.getOxidationLevel(entity.getWeatherState()).texture();
    }

    @Override
    public void render(CopperGolem entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
