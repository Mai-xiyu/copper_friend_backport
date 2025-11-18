package org.xiyu.yee.copper_friend_backport.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

import java.util.List;

/**
 * 注入到光照引擎,让它认为实体位置有光源
 * 这是实现动态光源的核心
 */
@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
    
    @Shadow
    private BlockPos.MutableBlockPos mutablePos;
    
    private static boolean hasLogged = false;
    
    /**
     * 注入到 getEmission 方法
     * 这个方法返回指定位置的光照发射等级
     */
    @Inject(
        method = "getEmission",
        at = @At("RETURN"),
        cancellable = true
    )
    private void injectEntityEmission(long packedPos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        int originalEmission = cir.getReturnValue();
        
        // 如果方块本身已经发光,直接返回
        if (originalEmission > 0) {
            return;
        }
        
        // 获取客户端世界
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        
        // 将 packed position 转换为 BlockPos
        BlockPos pos = BlockPos.of(packedPos);
        
        // 在附近搜索发光的铜傀儡
        AABB searchBox = new AABB(pos).inflate(1.0);
        List<Entity> entities = mc.level.getEntities(null, searchBox);
        
        for (Entity entity : entities) {
            if (entity instanceof CopperGolem golem && golem.isLantern()) {
                BlockPos entityPos = entity.blockPosition();
                
                // 如果实体就在这个方块位置,返回光照等级 14
                if (entityPos.equals(pos)) {
                    if (!hasLogged) {
                        System.out.println("[DynamicLight] BlockLightEngine.getEmission() injecting light at " + pos);
                        hasLogged = true;
                    }
                    cir.setReturnValue(14);
                    return;
                }
            }
        }
    }
}
