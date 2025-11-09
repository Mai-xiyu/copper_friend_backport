package org.xiyu.yee.copper_friend_backport.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlock;
import org.xiyu.yee.copper_friend_backport.world.CopperGolemStatueBlockEntity;

/**
 * Renderer for Copper Golem Statue block entities.
 * Uses the same model and textures as the Copper Golem entity.
 */
public class CopperGolemStatueRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity> {
    
    private final CopperGolemModel model;
    
    // Texture locations matching the entity textures
    private static final ResourceLocation TEXTURE_COPPER = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/copper_golem/copper_golem.png");
    private static final ResourceLocation TEXTURE_EXPOSED = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/copper_golem/exposed_copper_golem.png");
    private static final ResourceLocation TEXTURE_WEATHERED = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/copper_golem/weathered_copper_golem.png");
    private static final ResourceLocation TEXTURE_OXIDIZED = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/copper_golem/oxidized_copper_golem.png");
    
    public CopperGolemStatueRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CopperGolemModel(context.bakeLayer(org.xiyu.yee.copper_friend_backport.client.ClientSetup.COPPER_GOLEM_LAYER));
    }

    @Override
    public void render(
        CopperGolemStatueBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay
    ) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof CopperGolemStatueBlock statueBlock)) {
            return;
        }

        poseStack.pushPose();
        
        // Center the model
        poseStack.translate(0.5, 0.0, 0.5);
        
        // Rotate based on facing direction
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float rotation = switch (facing) {
            case NORTH -> 180.0F;
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // Get the appropriate texture based on oxidation state
        ResourceLocation texture = getTextureLocation(statueBlock.getWeatheringState());
        
        // Render the model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(texture));
        this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        
        poseStack.popPose();
    }
    
    /**
     * Gets the appropriate texture based on the oxidation state.
     */
    private ResourceLocation getTextureLocation(WeatheringCopper.WeatherState weatherState) {
        return switch (weatherState) {
            case UNAFFECTED -> TEXTURE_COPPER;
            case EXPOSED -> TEXTURE_EXPOSED;
            case WEATHERED -> TEXTURE_WEATHERED;
            case OXIDIZED -> TEXTURE_OXIDIZED;
        };
    }
}
