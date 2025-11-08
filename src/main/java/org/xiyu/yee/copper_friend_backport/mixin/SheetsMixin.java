package org.xiyu.yee.copper_friend_backport.mixin;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.copper_friend_backport.world.CopperChestBlock;

@Mixin(Sheets.class)
public abstract class SheetsMixin {
    @Inject(method = "chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;", at = @At("HEAD"), cancellable = true)
    private static void Inject15(BlockEntity blockEntity, ChestType chestType, boolean christmas, CallbackInfoReturnable<Material> cir) {
        if(blockEntity.getBlockState().getBlock() instanceof CopperChestBlock){

        }
    }
    @Unique
    private Sheets getThis() {
        return (Sheets) (Object) this;
    }
}