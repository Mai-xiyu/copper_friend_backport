package org.xiyu.yee.copper_friend_backport.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1.20.1 的关键帧动画系统实现
 * 基于 1.21 的 KeyframeAnimation 类移植
 */
@OnlyIn(Dist.CLIENT)
public class KeyframeAnimation {
    private final AnimationDefinition definition;
    private final List<Entry> entries;

    private KeyframeAnimation(AnimationDefinition definition, List<Entry> entries) {
        this.definition = definition;
        this.entries = entries;
    }

    public static KeyframeAnimation bake(ModelPart root, AnimationDefinition definition) {
        List<Entry> entries = new ArrayList<>();

        for (Map.Entry<String, List<AnimationChannel>> boneEntry : definition.boneAnimations().entrySet()) {
            String boneName = boneEntry.getKey();
            List<AnimationChannel> channels = boneEntry.getValue();
            
            // 使用 getChildRecursive 查找部件
            ModelPart part = getChildRecursive(root, boneName);
            
            if (part == null) {
                // 在生产环境中,某些部件可能找不到,记录警告但不崩溃
                System.err.println("Warning: Cannot find model part '" + boneName + "' for animation. Animation will be skipped for this part.");
                continue; // 跳过这个部件的动画
            }

            for (AnimationChannel channel : channels) {
                entries.add(new Entry(part, channel.target(), channel.keyframes()));
            }
        }

        return new KeyframeAnimation(definition, List.copyOf(entries));
    }
    
    /**
     * 递归查找子部件
     * 支持 "body.head" 这样的路径，也支持直接名称（会递归查找）
     */
    private static ModelPart getChildRecursive(ModelPart root, String path) {
        if (path.isEmpty()) {
            return root;
        }
        
        // 如果包含点号，说明是完整路径
        if (path.contains(".")) {
            String[] parts = path.split("\\.", 2);
            try {
                ModelPart child = root.getChild(parts[0]);
                return getChildRecursive(child, parts[1]);
            } catch (Exception e) {
                return null;
            }
        }
        
        // 否则先尝试直接获取
        try {
            return root.getChild(path);
        } catch (Exception e) {
            // 如果直接获取失败，递归查找所有子部件
            return findChildInTree(root, path);
        }
    }
    
    /**
     * 在模型树中递归查找指定名称的部件
     */
    private static ModelPart findChildInTree(ModelPart parent, String name) {
        // 尝试使用getAllParts方法遍历所有部件
        try {
            // 首先尝试使用反射获取children字段
            java.lang.reflect.Field childrenField = ModelPart.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ModelPart> children = (Map<String, ModelPart>) childrenField.get(parent);
            
            // 检查直接子部件
            if (children != null && children.containsKey(name)) {
                return children.get(name);
            }
            
            // 递归查找
            if (children != null) {
                for (ModelPart child : children.values()) {
                    ModelPart found = findChildInTree(child, name);
                    if (found != null) {
                        return found;
                    }
                }
            }
        } catch (NoSuchFieldException e) {
            // 如果反射失败,尝试使用hasChild和getChild
            try {
                // 遍历可能的子部件名称
                for (String possibleParent : new String[]{"body", "head", "root"}) {
                    try {
                        ModelPart parentPart = parent.getChild(possibleParent);
                        if (parentPart != null) {
                            try {
                                return parentPart.getChild(name);
                            } catch (Exception ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            // Ignore other exceptions
        }
        
        return null;
    }

    public void applyStatic() {
        this.apply(0L, 1.0F);
    }

    public void applyWalk(float walkAnimationPos, float walkAnimationSpeed, float timeMultiplier, float speedMultiplier) {
        long timeInMillis = (long)(walkAnimationPos * 50.0F * timeMultiplier);
        float scale = Math.min(walkAnimationSpeed * speedMultiplier, 1.0F);
        this.apply(timeInMillis, scale);
    }

    public void apply(AnimationState animationState, float ageInTicks) {
        this.apply(animationState, ageInTicks, 1.0F);
    }

    public void apply(AnimationState animationState, float ageInTicks, float speedMultiplier) {
        animationState.ifStarted((startTime) -> {
            // 计算从动画开始到现在经过的ticks
            float elapsedTicks = ageInTicks - startTime.getAccumulatedTime();
            // 转换为毫秒 (1 tick = 50ms)
            long timeInMillis = (long)(elapsedTicks * 50.0F * speedMultiplier);
            this.apply(timeInMillis, 1.0F);
        });
    }

    public void apply(long timeInMillis, float scale) {
        float elapsedSeconds = this.getElapsedSeconds(timeInMillis);
        Vector3f scratchVector = new Vector3f();

        for (Entry entry : this.entries) {
            entry.apply(elapsedSeconds, scale, scratchVector);
        }
    }

    private float getElapsedSeconds(long timeInMillis) {
        float seconds = (float)timeInMillis / 1000.0F;
        return this.definition.looping() ? seconds % this.definition.lengthInSeconds() : seconds;
    }

    @OnlyIn(Dist.CLIENT)
    public record Entry(ModelPart part, AnimationChannel.Target target, Keyframe[] keyframes) {
        public void apply(float elapsedSeconds, float scale, Vector3f scratchVector) {
            int keyframeIndex = Math.max(0, Mth.binarySearch(0, this.keyframes.length, 
                (index) -> elapsedSeconds <= this.keyframes[index].timestamp()) - 1);
            int nextIndex = Math.min(this.keyframes.length - 1, keyframeIndex + 1);
            
            Keyframe currentKeyframe = this.keyframes[keyframeIndex];
            Keyframe nextKeyframe = this.keyframes[nextIndex];
            float timeSinceCurrent = elapsedSeconds - currentKeyframe.timestamp();
            
            float progress;
            if (nextIndex != keyframeIndex) {
                progress = Mth.clamp(timeSinceCurrent / (nextKeyframe.timestamp() - currentKeyframe.timestamp()), 0.0F, 1.0F);
            } else {
                progress = 0.0F;
            }

            nextKeyframe.interpolation().apply(scratchVector, progress, this.keyframes, keyframeIndex, nextIndex, scale);
            this.target.apply(this.part, scratchVector);
        }
    }
}
/*@OnlyIn(Dist.CLIENT)
public class KeyframeAnimation {
    private final AnimationDefinition definition;
    private final List<Entry> entries;

    private KeyframeAnimation(AnimationDefinition pDefinition, List<Entry> pEntries) {
        this.definition = pDefinition;
        this.entries = pEntries;
    }

    static KeyframeAnimation bake(ModelPart pRoot, AnimationDefinition pDefinition) {
        List<Entry> $$2 = new ArrayList();
        Function<String, ModelPart> $$3 = pRoot.createPartLookup();

        for(Map.Entry<String, List<AnimationChannel>> $$4 : pDefinition.boneAnimations().entrySet()) {
            String $$5 = (String)$$4.getKey();
            List<AnimationChannel> $$6 = (List)$$4.getValue();
            ModelPart $$7 = (ModelPart)$$3.apply($$5);
            if ($$7 == null) {
                throw new IllegalArgumentException("Cannot animate " + $$5 + ", which does not exist in model");
            }

            for(AnimationChannel $$8 : $$6) {
                $$2.add(new Entry($$7, $$8.target(), $$8.keyframes()));
            }
        }

        return new KeyframeAnimation(pDefinition, List.copyOf($$2));
    }

    public void applyStatic() {
        this.apply(0L, 1.0F);
    }

    public void applyWalk(float pWalkAnimationPos, float pWalkAnimationSpeed, float pTimeMultiplier, float pSpeedMultiplier) {
        long $$4 = (long)(pWalkAnimationPos * 50.0F * pTimeMultiplier);
        float $$5 = Math.min(pWalkAnimationSpeed * pSpeedMultiplier, 1.0F);
        this.apply($$4, $$5);
    }

    public void apply(AnimationState pAnimationState, float pAgeInTicks) {
        this.apply(pAnimationState, pAgeInTicks, 1.0F);
    }

    public void apply(AnimationState pAnimationState, float pAgeInTicks, float pSpeedMultiplier) {
        pAnimationState.ifStarted((p_408975_) -> this.apply((long)((float)p_408975_.getTimeInMillis(pAgeInTicks) * pSpeedMultiplier), 1.0F));
    }

    public void apply(long pTimeInMillis, float pScale) {
        float $$2 = this.getElapsedSeconds(pTimeInMillis);
        Vector3f $$3 = new Vector3f();

        for(Entry $$4 : this.entries) {
            $$4.apply($$2, pScale, $$3);
        }

    }

    private float getElapsedSeconds(long pTimeInMillis) {
        float $$1 = (float)pTimeInMillis / 1000.0F;
        return this.definition.looping() ? $$1 % this.definition.lengthInSeconds() : $$1;
    }

    @OnlyIn(Dist.CLIENT)
    static record Entry(ModelPart part, AnimationChannel.Target target, Keyframe[] keyframes) {
        Entry(ModelPart part, AnimationChannel.Target pTarget, Keyframe[] keyframes) {
            this.part = part;
            this.target = pTarget;
            this.keyframes = keyframes;
        }

        public void apply(float pElapsedSeconds, float pScale, Vector3f pScratchVector) {
            int $$3 = Math.max(0, Mth.binarySearch(0, this.keyframes.length, (p_406117_) -> pElapsedSeconds <= this.keyframes[p_406117_].timestamp()) - 1);
            int $$4 = Math.min(this.keyframes.length - 1, $$3 + 1);
            Keyframe $$5 = this.keyframes[$$3];
            Keyframe $$6 = this.keyframes[$$4];
            float $$7 = pElapsedSeconds - $$5.timestamp();
            float $$8;
            if ($$4 != $$3) {
                $$8 = Mth.clamp($$7 / ($$6.timestamp() - $$5.timestamp()), 0.0F, 1.0F);
            } else {
                $$8 = 0.0F;
            }

            $$6.interpolation().apply(pScratchVector, $$8, this.keyframes, $$3, $$4, pScale);
            this.target.apply(this.part, pScratchVector);
        }
    }
}
*/