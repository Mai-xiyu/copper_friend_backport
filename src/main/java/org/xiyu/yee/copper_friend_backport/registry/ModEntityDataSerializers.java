package org.xiyu.yee.copper_friend_backport.registry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.fml.common.Mod;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.StreamCodec;
import org.xiyu.yee.copper_friend_backport.WeatheringCopper;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolemState;


@Mod.EventBusSubscriber(modid = CopperFriendBackport.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityDataSerializers {
//实体的序列化nbt---------------
    public static final EntityDataSerializer<WeatheringCopper.WeatherState> WEATHERING_COPPER_STATE;
    public static final EntityDataSerializer<CopperGolemState> COPPER_GOLEM_STATE;

    static {
        WEATHERING_COPPER_STATE = forValueType(WeatheringCopper.WeatherState.STREAM_CODEC);
        COPPER_GOLEM_STATE = forValueType(CopperGolemState.STREAM_CODEC);

        net.minecraft.network.syncher.EntityDataSerializers.registerSerializer(WEATHERING_COPPER_STATE);
        net.minecraft.network.syncher.EntityDataSerializers.registerSerializer(COPPER_GOLEM_STATE);
    }

    private static <T> EntityDataSerializer<T> forValueType(StreamCodec<ByteBuf, T> streamCodec) {
        return new EntityDataSerializer<T>() {
            @Override
            public void write(FriendlyByteBuf pBuffer, T pValue) {
                streamCodec.encode(pBuffer, pValue);
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

    public static void init() {
        //这是石山
    }
}
