package org.xiyu.yee.copper_friend_backport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.xiyu.yee.copper_friend_backport.GroundPathNavigationMixinInterface;

@Mixin(GroundPathNavigation.class)
public abstract class GroundPathNavigationMixin implements GroundPathNavigationMixinInterface {
    @Shadow
    public abstract Path createPath(BlockPos pPos, int pAccuracy);

    @Unique
    boolean copper_friend_backport$canPathToTargetsBelowSurface = false;

/*    @ModifyVariable(method = "createPath(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"),
            index = 1, argsOnly = true)
    private BlockPos copper_friend_backport$modifyTargetPos(BlockPos blockPos) {
        if (copper_friend_backport$canPathToTargetsBelowSurface) {
            return blockPos;
        }

        LevelChunk levelChunk = getThis().level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(blockPos.getX()),
                SectionPos.blockToSectionCoord(blockPos.getZ())
        );

        if (levelChunk == null) {
            return blockPos;
        }

        return copper_friend_backport$findSurfacePosition(levelChunk, blockPos, 0);
    }*/

    @Unique
    final BlockPos copper_friend_backport$findSurfacePosition(LevelChunk levelChunk, BlockPos blockPos, int i) {
        if (levelChunk.getBlockState(blockPos).isAir()) {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);

            while (mutableBlockPos.getY() >= getThis().level.getMinBuildHeight() && levelChunk.getBlockState(mutableBlockPos).isAir()) {
                mutableBlockPos.move(Direction.DOWN);
            }

            if (mutableBlockPos.getY() >= getThis().level.getMinBuildHeight()) {
                return mutableBlockPos.above();
            }

            mutableBlockPos.setY(blockPos.getY() + 1);

            while (mutableBlockPos.getY() <= getThis().level.getMaxBuildHeight() && levelChunk.getBlockState(mutableBlockPos).isAir()) {
                mutableBlockPos.move(Direction.UP);
            }

            blockPos = mutableBlockPos;
        }

        if (!levelChunk.getBlockState(blockPos).isSolid()) {
            return blockPos;
        } else {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);

            while (mutableBlockPos.getY() <= getThis().level.getMaxBuildHeight() && levelChunk.getBlockState(mutableBlockPos).isSolid()) {
                mutableBlockPos.move(Direction.UP);
            }

            return mutableBlockPos.immutable();
        }
    }

    @Override
    public boolean copper_friend_backport$get() {
        return copper_friend_backport$canPathToTargetsBelowSurface;
    }

    @Override
    public void copper_friend_backport$set(boolean value) {
        copper_friend_backport$canPathToTargetsBelowSurface = value;
    }

    @Unique
    private GroundPathNavigation getThis() {
        return (GroundPathNavigation) (Object) this;
    }
}
