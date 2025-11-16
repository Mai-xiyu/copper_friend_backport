package org.xiyu.yee.copper_friend_backport.coppergolem;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.xiyu.yee.copper_friend_backport.StreamCodec;
import org.xiyu.yee.copper_friend_backport.world.ByteBufCodecs;

public enum CopperGolemState implements StringRepresentable {
	IDLE("idle", 0),
	GETTING_ITEM("getting_item", 1),
	GETTING_NO_ITEM("getting_no_item", 2),
	DROPPING_ITEM("dropping_item", 3),
	DROPPING_NO_ITEM("dropping_no_item", 4),
    SPIN_HEAD("spin_head",5);

	public static final Codec<CopperGolemState> CODEC = StringRepresentable.fromEnum(CopperGolemState::values);
	private static final IntFunction<CopperGolemState> BY_ID = ByIdMap.continuous(CopperGolemState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final StreamCodec<ByteBuf, CopperGolemState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CopperGolemState::id);
	private final String name;
	private final int id;

	private CopperGolemState(final String string2, final int j) {
		this.name = string2;
		this.id = j;
	}

	@NotNull
	@Override
	public String getSerializedName() {
		return this.name;
	}

	private int id() {
		return this.id;
	}
}
