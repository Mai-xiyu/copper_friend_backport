package org.xiyu.yee.copper_friend_backport.client.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/**
 * 动画定义 - 包含动画的所有信息
 */
@OnlyIn(Dist.CLIENT)
public record AnimationDefinition(
    float lengthInSeconds,
    boolean looping,
    Map<String, List<AnimationChannel>> boneAnimations
) {
    public static Builder builder() {
        return new Builder();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private float length;
        private boolean looping = true;
        private final Map<String, List<AnimationChannel>> boneAnimations = new java.util.HashMap<>();

        public Builder length(float lengthInSeconds) {
            this.length = lengthInSeconds;
            return this;
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addAnimation(String boneName, AnimationChannel channel) {
            this.boneAnimations.computeIfAbsent(boneName, k -> new java.util.ArrayList<>()).add(channel);
            return this;
        }

        public AnimationDefinition build() {
            return new AnimationDefinition(length, looping, Map.copyOf(boneAnimations));
        }
    }
}
