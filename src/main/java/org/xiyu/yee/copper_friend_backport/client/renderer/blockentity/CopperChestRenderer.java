package org.xiyu.yee.copper_friend_backport.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.copper_chest.BaseCopperChestBlock;
import org.xiyu.yee.copper_friend_backport.copper_chest.CopperChestBlockEntity;

import static net.minecraft.client.renderer.Sheets.CHEST_SHEET;
import static net.minecraft.world.level.block.ChestBlock.FACING;
import static net.minecraft.world.level.block.ChestBlock.TYPE;

/**
 * Custom renderer for Copper Chests.
 * Handles rendering of all copper chest variants with proper textures based on oxidation state.
 */
public class CopperChestRenderer implements BlockEntityRenderer<CopperChestBlockEntity> {
    
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;

    public CopperChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart chestModel = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = chestModel.getChild("bottom");
        this.lid = chestModel.getChild("lid");
        this.lock = chestModel.getChild("lock");
        
        ModelPart doubleChestModel = context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = doubleChestModel.getChild("bottom");
        this.doubleLeftLid = doubleChestModel.getChild("lid");
        this.doubleLeftLock = doubleChestModel.getChild("lock");
        
        ModelPart doubleChestRightModel = context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = doubleChestRightModel.getChild("bottom");
        this.doubleRightLid = doubleChestRightModel.getChild("lid");
        this.doubleRightLock = doubleChestRightModel.getChild("lock");
    }

    @Override
    public void render(
        CopperChestBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay
    ) {
        Level level = blockEntity.getLevel();
        boolean hasLevel = level != null;
        BlockState blockState = hasLevel ? blockEntity.getBlockState() : 
            blockEntity.getBlockState().setValue(FACING, Direction.SOUTH);
        
        Block block = blockState.getBlock();
        if (!(block instanceof BaseCopperChestBlock copperChestBlock)) {
            return;
        }

        ChestType chestType = blockState.hasProperty(TYPE) ? blockState.getValue(TYPE) : ChestType.SINGLE;
        
        // Get the material (texture) for this chest
        Material material = getMaterial(copperChestBlock, chestType);
        
        poseStack.pushPose();
        
        // Position and rotate the chest
        float yRotation = blockState.getValue(FACING).toYRot();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRotation));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // Get combined light from the DoubleBlockCombiner
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combineResult;
        if (hasLevel) {
            combineResult = copperChestBlock.combine(blockState, level, blockEntity.getBlockPos(), true);
        } else {
            combineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }
        
        Float2FloatFunction opennessFunction = combineResult.apply(BaseCopperChestBlock.opennessCombiner((LidBlockEntity) blockEntity));
        float openness = opennessFunction.get(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;
        
        int blockLight = combineResult.apply(new BrightnessCombiner<>()).applyAsInt(combinedLight);
        
        VertexConsumer vertexConsumer = material.buffer(buffer, RenderType::entityCutout);
        
        // Render the appropriate chest model
        if (chestType == ChestType.LEFT) {
            renderPiece(poseStack, vertexConsumer, this.doubleLeftLid, this.doubleLeftLock, 
                       this.doubleLeftBottom, openness, blockLight, combinedOverlay);
        } else if (chestType == ChestType.RIGHT) {
            renderPiece(poseStack, vertexConsumer, this.doubleRightLid, this.doubleRightLock, 
                       this.doubleRightBottom, openness, blockLight, combinedOverlay);
        } else {
            renderPiece(poseStack, vertexConsumer, this.lid, this.lock, 
                       this.bottom, openness, blockLight, combinedOverlay);
        }
        
        poseStack.popPose();
    }

    /**
     * Get the material (texture) for a copper chest based on its oxidation state and chest type.
     */
    private Material getMaterial(BaseCopperChestBlock block, ChestType chestType) {
        WeatheringCopper.WeatherState weatherState = block.getWeatherState();
        String baseName = switch (weatherState) {
            case UNAFFECTED -> "copper";
            case EXPOSED -> "exposed_copper";
            case WEATHERED -> "weathered_copper";
            case OXIDIZED -> "oxidized_copper";
        };
        
        String materialName = switch (chestType) {
            case LEFT -> baseName + "_left";
            case RIGHT -> baseName + "_right";
            default -> baseName;
        };
        
        return new Material(CHEST_SHEET, ResourceLocation.fromNamespaceAndPath("minecraft", "entity/chest/" + materialName));
    }

    /**
     * Render the chest model parts.
     */
    private void renderPiece(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        ModelPart lid,
        ModelPart lock,
        ModelPart bottom,
        float openness,
        int combinedLight,
        int combinedOverlay
    ) {
        lid.xRot = -(openness * ((float) Math.PI / 2F));
        lock.xRot = lid.xRot;
        lid.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);
        lock.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);
        bottom.render(poseStack, vertexConsumer, combinedLight, combinedOverlay);
    }
}
