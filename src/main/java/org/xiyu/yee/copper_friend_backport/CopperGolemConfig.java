package org.xiyu.yee.copper_friend_backport;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Configuration class for Copper Golem AI behavior.
 * Controls various parameters for how the Copper Golem operates.
 */
public class CopperGolemConfig {
    
    public static final ForgeConfigSpec COMMON_SPEC;
    
    // AI Behavior Settings
    public static final ForgeConfigSpec.DoubleValue PANIC_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue IDLE_SPEED_MULTIPLIER;
    
    // Transport Item Settings
    public static final ForgeConfigSpec.IntValue TRANSPORT_HORIZONTAL_SEARCH_RADIUS;
    public static final ForgeConfigSpec.IntValue TRANSPORT_VERTICAL_SEARCH_RADIUS;
    public static final ForgeConfigSpec.IntValue TICK_TO_START_INTERACTION;
    public static final ForgeConfigSpec.IntValue TICK_TO_PLAY_SOUND;
    
    // Random Stroll Settings
    public static final ForgeConfigSpec.DoubleValue RANDOM_STROLL_SPEED;
    public static final ForgeConfigSpec.IntValue RANDOM_STROLL_MIN_DISTANCE;
    public static final ForgeConfigSpec.IntValue RANDOM_STROLL_MAX_DISTANCE;
    
    // DoNothing Behavior Settings
    public static final ForgeConfigSpec.IntValue DO_NOTHING_MIN_DURATION;
    public static final ForgeConfigSpec.IntValue DO_NOTHING_MAX_DURATION;
    
    // Look Settings
    public static final ForgeConfigSpec.IntValue LOOK_AT_PLAYER_MIN_LOOK_DURATION;
    public static final ForgeConfigSpec.IntValue LOOK_AT_PLAYER_MAX_LOOK_DURATION;
    public static final ForgeConfigSpec.DoubleValue LOOK_AT_PLAYER_PROBABILITY;
    
    // Chest Interaction Settings
    public static final ForgeConfigSpec.IntValue CHEST_INTERACTION_DURATION;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.comment("Copper Golem AI Configuration||铜傀儡AI配置")
               .push("ai_behavior");
        
        // AI Behavior
        builder.comment("Speed and movement settings||速度和移动设置")
               .push("speed");
        
        PANIC_SPEED_MULTIPLIER = builder
            .comment("Speed multiplier when the Copper Golem is panicking (default: 1.5)||当铜傀儡处于惊慌状态时的速度倍增器（默认值：1.5）")
            .defineInRange("panicSpeedMultiplier", 1.5, 0.1, 10.0);
        
        IDLE_SPEED_MULTIPLIER = builder
            .comment("Speed multiplier when the Copper Golem is idle/walking (default: 1.0)||当铜傀儡处于闲置/行走状态时的速度倍增器（默认值：1.0）")
            .defineInRange("idleSpeedMultiplier", 1.0, 0.1, 10.0);
        
        builder.pop();
        
        // Transport Item Settings
        builder.comment("Item transportation settings||物品运输设置")
               .push("transport");
        
        TRANSPORT_HORIZONTAL_SEARCH_RADIUS = builder
            .comment("Horizontal search radius (in blocks) for finding chests to transport items (default: 32)||寻找箱子以运输物品的水平搜索半径（以方块为单位）（默认值：32）")
            .defineInRange("horizontalSearchRadius", 32, 1, 128);
        
        TRANSPORT_VERTICAL_SEARCH_RADIUS = builder
            .comment("Vertical search radius (in blocks) for finding chests to transport items (default: 8)||寻找箱子以运输物品的垂直搜索半径（以方块为单位）（默认值：8）")
            .defineInRange("verticalSearchRadius", 8, 1, 64);
        
        TICK_TO_START_INTERACTION = builder
            .comment("Number of ticks before starting chest interaction animation (default: 1)||在开始与箱子交互动画之前的刻数（默认值：1）")
            .defineInRange("tickToStartInteraction", 1, 0, 100);
        
        TICK_TO_PLAY_SOUND = builder
            .comment("Number of ticks before playing interaction sound (default: 9)||在播放交互声音之前的刻数（默认值：9）")
            .defineInRange("tickToPlaySound", 9, 0, 100);
        
        CHEST_INTERACTION_DURATION = builder
            .comment("Total duration (in ticks) of chest interaction before closing (default: 60, 3 seconds)||与箱子交互的总持续时间（以刻为单位），然后关闭（默认值：60，3秒）")
            .defineInRange("chestInteractionDuration", 60, 1, 200);
        
        builder.pop();
        
        // Random Stroll Settings
        builder.comment("Random wandering behavior settings||随机漫步行为设置")
               .push("stroll");
        
        RANDOM_STROLL_SPEED = builder
            .comment("Speed multiplier for random strolling (default: 1.0)||随机漫步的速度倍增器（默认值：1.0）")
            .defineInRange("strollSpeed", 1.0, 0.1, 10.0);
        
        RANDOM_STROLL_MIN_DISTANCE = builder
            .comment("Minimum distance (in blocks) for random stroll targets (default: 2)||随机漫步目标的最小距离（以方块为单位）（默认值：2）")
            .defineInRange("minStrollDistance", 2, 1, 32);
        
        RANDOM_STROLL_MAX_DISTANCE = builder
            .comment("Maximum distance (in blocks) for random stroll targets (default: 2)||随机漫步目标的最大距离（以方块为单位）（默认值：2）")
            .defineInRange("maxStrollDistance", 2, 1, 32);
        
        builder.pop();
        
        // Idle Behavior
        builder.comment("Idle behavior settings||行为设置")
               .push("idle");
        
        DO_NOTHING_MIN_DURATION = builder
            .comment("Minimum duration (in ticks) for standing still (default: 30, 1.5 seconds)||最短静止时间（以刻为单位）（默认值：30，1.5秒）")
            .defineInRange("doNothingMinDuration", 30, 1, 1200);
        
        DO_NOTHING_MAX_DURATION = builder
            .comment("Maximum duration (in ticks) for standing still (default: 60, 3 seconds)||最长静止时间（以刻为单位）（默认值：60，3秒）")
            .defineInRange("doNothingMaxDuration", 60, 1, 1200);
        
        builder.pop();
        
        // Look Settings
        builder.comment("Looking at player settings||看向玩家的设置")
               .push("look");
        
        LOOK_AT_PLAYER_PROBABILITY = builder
            .comment("Maximum distance (in blocks) at which golem will look at players (default: 6.0)||最大距离（以方块为单位），在此范围内傀儡会注视玩家（默认值：6.0）")
            .defineInRange("lookAtPlayerDistance", 6.0, 1.0, 32.0);
        
        LOOK_AT_PLAYER_MIN_LOOK_DURATION = builder
            .comment("Minimum duration (in ticks) for looking at player (default: 40, 2 seconds)||最短注视玩家时间（以刻为单位）（默认值：40，2秒）")
            .defineInRange("lookAtPlayerMinDuration", 40, 1, 1200);
        
        LOOK_AT_PLAYER_MAX_LOOK_DURATION = builder
            .comment("Maximum duration (in ticks) for looking at player (default: 80, 4 seconds)||最长注视玩家时间（以刻为单位）（默认值：80，4秒）")
            .defineInRange("lookAtPlayerMaxDuration", 80, 1, 1200);
        
        builder.pop();
        builder.pop();
        
        COMMON_SPEC = builder.build();
    }
    
    /**
     * Register the configuration file.
     * Should be called during mod initialization.
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "copper_friend_backport-common.toml");
    }
    
    // Convenience getters for use in code
    public static float getPanicSpeedMultiplier() {
        return PANIC_SPEED_MULTIPLIER.get().floatValue();
    }
    
    public static float getIdleSpeedMultiplier() {
        return IDLE_SPEED_MULTIPLIER.get().floatValue();
    }
    
    public static int getTransportHorizontalSearchRadius() {
        return TRANSPORT_HORIZONTAL_SEARCH_RADIUS.get();
    }
    
    public static int getTransportVerticalSearchRadius() {
        return TRANSPORT_VERTICAL_SEARCH_RADIUS.get();
    }
    
    public static int getTickToStartInteraction() {
        return TICK_TO_START_INTERACTION.get();
    }
    
    public static int getTickToPlaySound() {
        return TICK_TO_PLAY_SOUND.get();
    }
    
    public static int getChestInteractionDuration() {
        return CHEST_INTERACTION_DURATION.get();
    }
    
    public static float getRandomStrollSpeed() {
        return RANDOM_STROLL_SPEED.get().floatValue();
    }
    
    public static int getRandomStrollMinDistance() {
        return RANDOM_STROLL_MIN_DISTANCE.get();
    }
    
    public static int getRandomStrollMaxDistance() {
        return RANDOM_STROLL_MAX_DISTANCE.get();
    }
    
    public static int getDoNothingMinDuration() {
        return DO_NOTHING_MIN_DURATION.get();
    }
    
    public static int getDoNothingMaxDuration() {
        return DO_NOTHING_MAX_DURATION.get();
    }
    
    public static float getLookAtPlayerDistance() {
        return LOOK_AT_PLAYER_PROBABILITY.get().floatValue();
    }
    
    public static int getLookAtPlayerMinDuration() {
        return LOOK_AT_PLAYER_MIN_LOOK_DURATION.get();
    }
    
    public static int getLookAtPlayerMaxDuration() {
        return LOOK_AT_PLAYER_MAX_LOOK_DURATION.get();
    }
}
