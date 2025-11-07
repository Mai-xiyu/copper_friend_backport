package org.xiyu.yee.copper_friend_backport.client.animation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * 关键帧动画辅助类 - 提供角度和位置的Vector3f包装
 * 用于统一动画定义API
 */
@OnlyIn(Dist.CLIENT)
public class KeyframeAnimations {
    
    /**
     * 创建角度向量 (用于旋转)
     * @param xDeg X轴旋转角度
     * @param yDeg Y轴旋转角度  
     * @param zDeg Z轴旋转角度
     * @return Vector3f角度向量
     */
    public static Vector3f degreeVec(float xDeg, float yDeg, float zDeg) {
        return new Vector3f(xDeg, yDeg, zDeg);
    }
    
    /**
     * 创建位置向量 (用于平移)
     * @param x X轴位移
     * @param y Y轴位移
     * @param z Z轴位移
     * @return Vector3f位置向量
     */
    public static Vector3f posVec(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }
    
    /**
     * 创建缩放向量 (用于缩放)
     * @param x X轴缩放
     * @param y Y轴缩放
     * @param z Z轴缩放
     * @return Vector3f缩放向量
     */
    public static Vector3f scaleVec(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }
}
