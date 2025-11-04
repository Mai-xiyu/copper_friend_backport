package org.xiyu.yee.copper_friend_backport.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

public class CopperGolemStatueBlockEntity extends BlockEntity {
	@Nullable
	private Component customName;
	
	public CopperGolemStatueBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BANNER, blockPos, blockState); // TODO: Replace with proper BlockEntityType
	}

	public void createStatue(CopperGolem copperGolem) {
		this.customName = copperGolem.getCustomName();
		this.setChanged();
	}

	@Nullable
	public CopperGolem removeStatue(BlockState blockState) {
		if (this.level == null) return null;
		
		EntityType<?> entityType = EntityType.ZOMBIE; // TODO: Replace with COPPER_GOLEM EntityType when registered
		CopperGolem copperGolem = (CopperGolem) entityType.create(this.level);
		if (copperGolem != null) {
			copperGolem.setCustomName(this.customName);
			return this.initCopperGolem(blockState, copperGolem);
		} else {
			return null;
		}
	}

	private CopperGolem initCopperGolem(BlockState blockState, CopperGolem copperGolem) {
		BlockPos blockPos = this.getBlockPos();
		double x = blockPos.getX() + 0.5;
		double y = blockPos.getY();
		double z = blockPos.getZ() + 0.5;
		float yRot = blockState.getValue(CopperGolemStatueBlock.FACING).toYRot();
		
		copperGolem.moveTo(x, y, z, yRot, 0.0F);
		copperGolem.yHeadRot = yRot;
		copperGolem.yBodyRot = yRot;
		copperGolem.playSpawnSound();
		return copperGolem;
	}

	@Nullable
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.customName != null) {
			tag.putString("CustomName", Component.Serializer.toJson(this.customName));
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("CustomName", 8)) {
			this.customName = Component.Serializer.fromJson(tag.getString("CustomName"));
		}
	}

	public ItemStack getItem(ItemStack itemStack, CopperGolemStatueBlock.Pose pose) {
		// In 1.20.1, we use NBT instead of DataComponents
		if (this.customName != null) {
			itemStack.setHoverName(this.customName);
		}
		return itemStack;
	}
}
