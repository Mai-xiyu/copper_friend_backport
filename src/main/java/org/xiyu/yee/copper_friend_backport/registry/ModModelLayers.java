package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation COPPER_GOLEM = register("copper_golem");
    public static final ModelLayerLocation COPPER_GOLEM_EYES = register("copper_golem", "eyes");
    public static final ModelLayerLocation COPPER_GOLEM_RUNNING = register("copper_golem_running");
    public static final ModelLayerLocation COPPER_GOLEM_SITTING = register("copper_golem_sitting");
    public static final ModelLayerLocation COPPER_GOLEM_STAR = register("copper_golem_star");
    private static ModelLayerLocation register(String string, String string2) {
        return createLocation(string, string2);
    }
    private static ModelLayerLocation register(String string) {
        return createLocation(string, "main");
    }
    private static ModelLayerLocation createLocation(String string, String string2) {
        return new ModelLayerLocation(ResourceLocation.withDefaultNamespace(string), string2);
    }
}
