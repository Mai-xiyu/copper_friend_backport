package org.xiyu.yee.copper_friend_backport.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.copper_friend_backport.client.DynamicLightHandler;

/**
 * Mixin for dynamic lighting using lightmap coordinates method
 * Based on LambDynamicLights approach
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    private static boolean hasLoggedLight = false;
    
    /**
     * 注入到 getLightColor 来修改光照图坐标
     * 这个方法用于获取方块和实体的渲染光照
     */
    @Inject(
        method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private static void injectDynamicLight(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        int vanillaLight = cir.getReturnValue();
        
        // 提取方块光照等级 (blockLevel << 4)
        int blockLight = (vanillaLight >> 4) & 0xF;
        int skyLight = (vanillaLight >> 20) & 0xF;
        
        // 获取动态光源的光照等级
        double dynamicLight = DynamicLightHandler.getDynamicLightLevel(level, pos);
        
        if (dynamicLight > 0.0) {
            // 转换为 0-15 的光照等级
            int dynamicBlockLight = Math.min(15, (int) Math.ceil(dynamicLight));
            
            if (!hasLoggedLight) {
                System.out.println("[DynamicLight] Dynamic lighting active! DynamicBlockLight: " + dynamicBlockLight);
                hasLoggedLight = true;
            }
            
            // 取较大值
            if (dynamicBlockLight > blockLight) {
                // 重新组合光照图坐标: (skyLight << 20) | (blockLight << 4)
                int newLight = (skyLight << 20) | (dynamicBlockLight << 4);
                cir.setReturnValue(newLight);
            }
        }
    }
}
