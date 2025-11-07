package org.xiyu.yee.copper_friend_backport.client.animation;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * 关键帧 - 动画中的单个时间点
 */
@OnlyIn(Dist.CLIENT)
public record Keyframe(float timestamp, Vector3f target, Interpolation interpolation) {
    
    /**
     * 创建关键帧 - 使用Vector3f
     */
    public static Keyframe of(float timestamp, Vector3f target, Interpolation interpolation) {
        return new Keyframe(timestamp, target, interpolation);
    }
    
    /**
     * 创建关键帧 - 使用Vector3f,默认线性插值
     */
    public static Keyframe of(float timestamp, Vector3f target) {
        return new Keyframe(timestamp, target, Interpolation.LINEAR);
    }
    
    /**
     * 创建关键帧 - 使用独立的xyz值
     */
    public static Keyframe of(float timestamp, float x, float y, float z, Interpolation interpolation) {
        return new Keyframe(timestamp, new Vector3f(x, y, z), interpolation);
    }

    /**
     * 创建关键帧 - 使用独立的xyz值,默认线性插值
     */
    public static Keyframe of(float timestamp, float x, float y, float z) {
        return of(timestamp, x, y, z, Interpolation.LINEAR);
    }

    /**
     * 插值方式
     */
    @OnlyIn(Dist.CLIENT)
    public enum Interpolation {
        LINEAR {
            @Override
            public void apply(Vector3f result, float progress, Keyframe[] keyframes, int currentIndex, int nextIndex, float scale) {
                Vector3f current = keyframes[currentIndex].target();
                Vector3f next = keyframes[nextIndex].target();
                
                float x = Mth.lerp(progress, current.x, next.x) * scale;
                float y = Mth.lerp(progress, current.y, next.y) * scale;
                float z = Mth.lerp(progress, current.z, next.z) * scale;
                
                result.set(x, y, z);
            }
        },
        CATMULLROM {
            @Override
            public void apply(Vector3f result, float progress, Keyframe[] keyframes, int currentIndex, int nextIndex, float scale) {
                // 简化的 Catmull-Rom 样条插值
                Vector3f p0 = keyframes[Math.max(0, currentIndex - 1)].target();
                Vector3f p1 = keyframes[currentIndex].target();
                Vector3f p2 = keyframes[nextIndex].target();
                Vector3f p3 = keyframes[Math.min(keyframes.length - 1, nextIndex + 1)].target();
                
                float t = progress;
                float t2 = t * t;
                float t3 = t2 * t;
                
                float x = 0.5f * ((2.0f * p1.x) + (-p0.x + p2.x) * t + 
                         (2.0f * p0.x - 5.0f * p1.x + 4.0f * p2.x - p3.x) * t2 + 
                         (-p0.x + 3.0f * p1.x - 3.0f * p2.x + p3.x) * t3) * scale;
                         
                float y = 0.5f * ((2.0f * p1.y) + (-p0.y + p2.y) * t + 
                         (2.0f * p0.y - 5.0f * p1.y + 4.0f * p2.y - p3.y) * t2 + 
                         (-p0.y + 3.0f * p1.y - 3.0f * p2.y + p3.y) * t3) * scale;
                         
                float z = 0.5f * ((2.0f * p1.z) + (-p0.z + p2.z) * t + 
                         (2.0f * p0.z - 5.0f * p1.z + 4.0f * p2.z - p3.z) * t2 + 
                         (-p0.z + 3.0f * p1.z - 3.0f * p2.z + p3.z) * t3) * scale;
                
                result.set(x, y, z);
            }
        };

        public abstract void apply(Vector3f result, float progress, Keyframe[] keyframes, int currentIndex, int nextIndex, float scale);
    }
}
