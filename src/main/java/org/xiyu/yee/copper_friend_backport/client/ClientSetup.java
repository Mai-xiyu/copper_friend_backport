package org.xiyu.yee.copper_friend_backport.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.client.model.CopperGolemModel;
import org.xiyu.yee.copper_friend_backport.client.renderer.entity.CopperGolemRenderer;
import org.xiyu.yee.copper_friend_backport.registry.ModBlockEntity;
import org.xiyu.yee.copper_friend_backport.registry.ModEntity;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CopperFriendBackport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    public static final ModelLayerLocation COPPER_GOLEM_LAYER = new ModelLayerLocation(
            Objects.requireNonNull(ResourceLocation.tryBuild(CopperFriendBackport.MOD_ID, "copper_golem")), "main"
    );

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(COPPER_GOLEM_LAYER, CopperGolemModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register entity renderer
        event.registerEntityRenderer(ModEntity.COPPER_GOLEM.get(), CopperGolemRenderer::new);
        
        // Register block entity renderers
        event.registerBlockEntityRenderer(ModBlockEntity.COPPER_GOLEM_STATUE.get(), CopperGolemStatueRenderer::new);
    }
}
