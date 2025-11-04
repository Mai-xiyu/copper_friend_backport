package org.xiyu.yee.copper_friend_backport.registry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import org.xiyu.yee.copper_friend_backport.StreamCodec;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemState;


public class EntityDataSerializers {
    public static final EntityDataSerializer<WeatheringCopper.WeatherState> WEATHERING_COPPER_STATE = forValueType(
            WeatheringCopper.WeatherState.STREAM_CODEC
    );
    public static final EntityDataSerializer<CopperGolemState> COPPER_GOLEM_STATE = forValueType(CopperGolemState.STREAM_CODEC);
    static <T> EntityDataSerializer<T> forValueType(StreamCodec<ByteBuf, T> streamCodec) {
        return new EntityDataSerializer<T>() {
            @Override
            public void write(FriendlyByteBuf pBuffer, T pValue) {
                streamCodec.encode(pBuffer,pValue);
            }

            @Override
            public T read(FriendlyByteBuf pBuffer) {
                return streamCodec.decode(pBuffer);
            }

            @Override
            public T copy(T pValue) {
                return pValue;
            }
        };
    }
}
