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
import org.xiyu.yee.copper_friend_backport.client.renderer.entity.layers.CopperGolemPoppyLayer;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemOxidationLevels;

@OnlyIn(Dist.CLIENT)
public class CopperGolemRenderer extends MobRenderer<CopperGolem, CopperGolemModel<CopperGolem>> {
    
    public CopperGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperGolemModel<>(context.bakeLayer(ClientSetup.COPPER_GOLEM_LAYER)), 0.5F);
        this.addLayer(new CopperGolemEyesLayer(this));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CopperGolemAntennaBlockLayer(this, context.getBlockRenderDispatcher()));
        this.addLayer(new CopperGolemPoppyLayer(this)); // 虞美人渲染层
    }

    @Override
    public ResourceLocation getTextureLocation(CopperGolem entity) {
        return CopperGolemOxidationLevels.getOxidationLevel(entity.getWeatherState()).texture();
    }

    @Override
    public void render(CopperGolem entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // If the golem is a lantern, use maximum brightness
        if (entity.isLantern()) {
            packedLight = 15728880; // Maximum light level (sky light 15, block light 15)
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected int getBlockLightLevel(CopperGolem entity, net.minecraft.core.BlockPos pos) {
        // If the golem is a lantern, it emits light level 14
        return entity.isLantern() ? 14 : super.getBlockLightLevel(entity, pos);
    }
}
