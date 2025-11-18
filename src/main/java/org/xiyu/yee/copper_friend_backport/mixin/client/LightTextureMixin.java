package org.xiyu.yee.copper_friend_backport.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.copper_friend_backport.client.DynamicLightHandler;

/**
 * Alternative approach: Inject into LightTexture instead of LevelRenderer
 * This might work better for 1.20.1
 */
@Mixin(value = LightTexture.class, priority = 1001)
public class LightTextureMixin {
    
    private static boolean hasLoggedMixin = false;
    
    static {
        System.out.println("[DynamicLight] LightTextureMixin class loaded!");
    }
    
    /**
     * 尝试注入到 lightmap 计算中
     * 使用 require = 0 避免找不到方法时崩溃
     */
    @Inject(
        method = "getBrightness",
        at = @At("RETURN"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private static void modifyBrightness(CallbackInfoReturnable<Float> cir) {
        if (!hasLoggedMixin) {
            System.out.println("[DynamicLight] LightTextureMixin.getBrightness() called!");
            hasLoggedMixin = true;
        }
    }
}
