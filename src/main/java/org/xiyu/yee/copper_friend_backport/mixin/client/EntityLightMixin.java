package org.xiyu.yee.copper_friend_backport.mixin.client;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 测试 Mixin 是否工作
 */
@Mixin(LightTexture.class)
public class EntityLightMixin {
    
    private static boolean hasLogged = false;
    
    /**
     * pack 方法将方块光照和天空光照打包成一个整数
     * 这个方法会被频繁调用,所以我们只记录一次
     */
    @Inject(
        method = "pack",
        at = @At("HEAD")
    )
    private static void onPackLight(int pBlockLight, int pSkyLight, CallbackInfoReturnable<Integer> cir) {
        if (!hasLogged) {
            System.out.println("[DynamicLight] LightTexture.pack() WORKING! blockLight=" + pBlockLight + ", skyLight=" + pSkyLight);
            hasLogged = true;
        }
    }
}
