package org.xiyu.yee.copper_friend_backport.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * 动画通道 - 定义单个骨骼的单个属性动画
 */
@OnlyIn(Dist.CLIENT)
public final class AnimationChannel {
    private final Target target;
    private final Keyframe[] keyframes;

    /**
     *
     */
    public AnimationChannel(Target target, Keyframe ...keyframes) {
        this.target = target;
        this.keyframes = keyframes;
    }


    public static AnimationChannel rotation(Keyframe... keyframes) {
        return new AnimationChannel(Target.ROTATION, keyframes);
    }

    public static AnimationChannel position(Keyframe... keyframes) {
        return new AnimationChannel(Target.POSITION, keyframes);
    }

    public static AnimationChannel scale(Keyframe... keyframes) {
        return new AnimationChannel(Target.SCALE, keyframes);
    }

    public Target target() {
        return target;
    }

    public Keyframe[] keyframes() {
        return keyframes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnimationChannel) obj;
        return Objects.equals(this.target, that.target) &&
                Objects.equals(this.keyframes, that.keyframes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, keyframes);
    }

    @Override
    public String toString() {
        return "AnimationChannel[" +
                "target=" + target + ", " +
                "keyframes=" + keyframes + ']';
    }


    /**
     * 动画目标 - 定义动画影响模型部件的哪个属性
     */
    public enum Target {
        POSITION {
            @Override
            public void apply(ModelPart part, Vector3f vector) {
                part.x += vector.x;
                part.y += vector.y;
                part.z += vector.z;
            }
        },
        ROTATION {
            @Override
            public void apply(ModelPart part, Vector3f vector) {
                part.xRot += vector.x;
                part.yRot += vector.y;
                part.zRot += vector.z;
            }
        },
        SCALE {
            @Override
            public void apply(ModelPart part, Vector3f vector) {
                part.xScale *= 1.0F + vector.x;
                part.yScale *= 1.0F + vector.y;
                part.zScale *= 1.0F + vector.z;
            }
        };

        public abstract void apply(ModelPart part, Vector3f vector);
    }
}
