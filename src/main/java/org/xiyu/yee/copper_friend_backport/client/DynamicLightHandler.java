package org.xiyu.yee.copper_friend_backport.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 动态光源处理器 - 基于 LambDynamicLights 的实现
 */
public class DynamicLightHandler {
    private static final int ENTITY_LIGHT_LEVEL = 14;
    private static final double DYNAMIC_LIGHT_RANGE = 7.75; // LambDynamicLights 使用的范围
    private static final Int2ObjectOpenHashMap<BlockPos> entityLastPositions = new Int2ObjectOpenHashMap<>();
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 50; // 每 50ms 更新一次(20 TPS = 50ms per tick)
    
    private static boolean hasLoggedLanternGolem = false;
    /**
     * 获取指定位置的动态光照等级（LambDynamicLights 方法）
     * 返回 double 以保持精度
     */
    public static double getDynamicLightLevel(BlockAndTintGetter level, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.isClientSide()) {
            return 0.0;
        }
        
        double maxLight = 0.0;
        
        // 创建扫描范围的 AABB
        AABB box = new AABB(pos).inflate(DYNAMIC_LIGHT_RANGE);
        
        // 获取范围内的所有实体
        List<Entity> entities = mc.level.getEntities(null, box);
        
        for (Entity entity : entities) {
            if (entity instanceof CopperGolem golem && golem.isLantern()) {
                if (!hasLoggedLanternGolem) {
                    System.out.println("[DynamicLight] Found lantern golem! ID=" + golem.getId() + ", hasLantern=" + golem.isLantern());
                    hasLoggedLanternGolem = true;
                }
                
                // 计算距离
                Vec3 entityPos = entity.position();
                double dx = entityPos.x - (pos.getX() + 0.5);
                double dy = entityPos.y - (pos.getY() + 0.5);
                double dz = entityPos.z - (pos.getZ() + 0.5);
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                
                // LambDynamicLights 的光照计算公式
                // lightLevel = (1.0 - dist / range) * luminance
                double multiplier = 1.0 - dist / DYNAMIC_LIGHT_RANGE;
                if (multiplier > 0.0) {
                    double lightLevel = multiplier * (double) ENTITY_LIGHT_LEVEL;
                    maxLight = Math.max(maxLight, lightLevel);
                }
            }
        }
        
        return maxLight;
    }
    
    /**
     * 获取整数光照等级（用于其他地方）
     */
    public static int getLightLevelFromEntities(Level level, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return 0;
        }
        double dynamicLight = getDynamicLightLevel(mc.level, pos);
        return (int) dynamicLight;
    }
    
    /**
     * 更新所有发光实体的光照并触发区块重建
     */
    public static void updateLights(Level level, LevelLightEngine lightEngine) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return; // 限制更新频率
        }
        lastUpdateTime = currentTime;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.levelRenderer == null) {
            return;
        }
        
        Set<Integer> processedEntities = new HashSet<>();
        Set<BlockPos> blocksToUpdate = new HashSet<>();
        
        // 遍历所有发光实体
        mc.level.entitiesForRendering().forEach(entity -> {
            if (entity instanceof CopperGolem golem && golem.isLantern()) {
                int entityId = entity.getId();
                BlockPos currentPos = entity.blockPosition();
                BlockPos lastPos = entityLastPositions.get(entityId);
                
                // 只在实体移动时更新区块
                if (lastPos == null || !lastPos.equals(currentPos)) {
                    // 标记实体当前位置及周围方块需要光照更新
                    BlockPos golemPos = currentPos;
                    int range = (int) Math.ceil(DYNAMIC_LIGHT_RANGE) + 1;
                    
                    for (int dx = -range; dx <= range; dx++) {
                        for (int dy = -range; dy <= range; dy++) {
                            for (int dz = -range; dz <= range; dz++) {
                                BlockPos updatePos = golemPos.offset(dx, dy, dz);
                                blocksToUpdate.add(updatePos);
                            }
                        }
                    }
                    
                    // 清除旧位置的光照影响
                    if (lastPos != null && !lastPos.equals(currentPos)) {
                        for (int dx = -range; dx <= range; dx++) {
                            for (int dy = -range; dy <= range; dy++) {
                                for (int dz = -range; dz <= range; dz++) {
                                    BlockPos updatePos = lastPos.offset(dx, dy, dz);
                                    blocksToUpdate.add(updatePos);
                                }
                            }
                        }
                    }
                    
                    entityLastPositions.put(entityId, currentPos.immutable());
                }
                
                // 即使没移动,也要确保周围方块被标记为需要检查
                else {
                    BlockPos golemPos = currentPos;
                    int range = (int) Math.ceil(DYNAMIC_LIGHT_RANGE) + 1;
                    for (int dx = -range; dx <= range; dx++) {
                        for (int dy = -range; dy <= range; dy++) {
                            for (int dz = -range; dz <= range; dz++) {
                                BlockPos updatePos = golemPos.offset(dx, dy, dz);
                                blocksToUpdate.add(updatePos);
                            }
                        }
                    }
                }
                
                processedEntities.add(entityId);
            }
        });
        
        // 清理已移除的实体
        entityLastPositions.keySet().removeIf(entityId -> {
            if (!processedEntities.contains(entityId)) {
                Entity entity = mc.level.getEntity(entityId);
                if (entity == null || entity.isRemoved()) {
                    BlockPos lastPos = entityLastPositions.get(entityId);
                    if (lastPos != null) {
                        int range = (int) Math.ceil(DYNAMIC_LIGHT_RANGE);
                        for (int dx = -range; dx <= range; dx++) {
                            for (int dy = -range; dy <= range; dy++) {
                                for (int dz = -range; dz <= range; dz++) {
                                    BlockPos blockPos = lastPos.offset(dx, dy, dz);
                                    blocksToUpdate.add(blockPos);
                                }
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        });
        
        // 触发光照引擎重新计算这些方块
        if (!blocksToUpdate.isEmpty()) {
            for (BlockPos pos : blocksToUpdate) {
                // 使用 lightEngine.checkBlock() 触发光照重新计算
                lightEngine.checkBlock(pos);
            }
            
            // 同时标记区块需要重新渲染
            Set<BlockPos> chunksToUpdate = new HashSet<>();
            for (BlockPos pos : blocksToUpdate) {
                chunksToUpdate.add(new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4));
            }
            for (BlockPos chunkPos : chunksToUpdate) {
                mc.levelRenderer.setSectionDirty(chunkPos.getX() >> 4, chunkPos.getY() >> 4, chunkPos.getZ() >> 4);
            }
        }
    }
    
    /**
     * 添加受影响的区块（范围内的所有区块）
     */
    private static void addAffectedChunks(Set<BlockPos> chunks, BlockPos center) {
        int range = (int) Math.ceil(DYNAMIC_LIGHT_RANGE / 16.0); // 转换为区块范围
        int chunkX = center.getX() >> 4;
        int chunkY = center.getY() >> 4;
        int chunkZ = center.getZ() >> 4;
        
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    chunks.add(new BlockPos((chunkX + dx) << 4, (chunkY + dy) << 4, (chunkZ + dz) << 4));
                }
            }
        }
    }
    
    /**
     * 清空所有光源数据
     */
    public static void clearAllLights() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.levelRenderer != null) {
            // 标记所有之前的位置需要更新
            for (BlockPos pos : entityLastPositions.values()) {
                mc.levelRenderer.setSectionDirty(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
            }
        }
        entityLastPositions.clear();
    }
    
    /**
     * 每 tick 调用此方法更新光照
     */
    public static void tick(Level level) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        updateLights(level, lightEngine);
    }
}
