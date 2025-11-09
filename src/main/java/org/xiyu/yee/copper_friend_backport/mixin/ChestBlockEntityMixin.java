package org.xiyu.yee.copper_friend_backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.yee.copper_friend_backport.copper_chest.CopperChestBlockEntity;

/**
 * Mixin to intercept chest sound playback for copper chests
 */
@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin {
    
    @Inject(
        method = "playSound(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/sounds/SoundEvent;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void onPlaySound(Level level, BlockPos pos, BlockState state, SoundEvent sound, CallbackInfo ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        // If this is a copper chest BlockEntity, use custom sound logic
        if (blockEntity instanceof CopperChestBlockEntity) {
            CopperChestBlockEntity.playSound(level, pos, state, sound);
            ci.cancel();
        }
    }
}
