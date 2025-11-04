package org.xiyu.yee.copper_friend_backport.world;

import io.netty.buffer.ByteBuf;
import org.xiyu.yee.copper_friend_backport.StreamCodec;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static java.lang.Math.floor;

public interface ByteBufCodecs {
	int MAX_INITIAL_COLLECTION_SIZE = 65536;
	StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
		public Boolean decode(ByteBuf byteBuf) {
			return byteBuf.readBoolean();
		}

		public void encode(ByteBuf byteBuf, Boolean boolean_) {
			byteBuf.writeBoolean(boolean_);
		}
	};
	StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
		public Byte decode(ByteBuf byteBuf) {
			return byteBuf.readByte();
		}

		public void encode(ByteBuf byteBuf, Byte byte_) {
			byteBuf.writeByte(byte_);
		}
	};
    public static byte packDegrees(float f) {
        return (byte)floor(f * 256.0F / 360.0F);
    }

    public static float unpackDegrees(byte b) {
        return b * 360 / 256.0F;
    }
    static <T> StreamCodec<ByteBuf, T> idMapper(IntFunction<T> intFunction, ToIntFunction<T> toIntFunction) {
        return new StreamCodec<ByteBuf, T>() {
            public T decode(ByteBuf byteBuf) {
                int i = VarInt.read(byteBuf);
                return (T)intFunction.apply(i);
            }

            public void encode(ByteBuf byteBuf, T object) {
                int i = toIntFunction.applyAsInt(object);
                VarInt.write(byteBuf, i);
            }
        };
    }
}
