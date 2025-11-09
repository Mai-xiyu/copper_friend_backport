package org.xiyu.yee.copper_friend_backport.copper_chest;

/**
 * 铜箱子使用示例
 * 
 * 这个包演示了如何通过清晰的继承结构创建不同变种的铜箱子
 */
public class CopperChestExample {
    
    /**
     * 示例1: 创建可氧化的铜箱子
     * 
     * 用法：在 ModBlocks 中注册
     * 
     * public static final Block COPPER_CHEST = register(
     *     "copper_chest",
     *     properties -> new OxidizableCopperChestBlock(
     *         WeatheringCopper.WeatherState.UNAFFECTED,
     *         ModSoundEvents.COPPER_CHEST_OPEN.get(),
     *         ModSoundEvents.COPPER_CHEST_CLOSE.get(),
     *         properties
     *     ),
     *     BlockBehaviour.Properties.of()
     *         .mapColor(MapColor.COLOR_ORANGE)
     *         .strength(3.0F, 6.0F)
     *         .sound(SoundType.COPPER)
     *         .requiresCorrectToolForDrops()
     * );
     * 
     * 特点：
     * - 会随时间自然氧化
     * - 可以用蜂蜜打蜡防止氧化
     * - 可以用斧头刮掉氧化层
     */
    
    /**
     * 示例2: 创建打蜡的铜箱子
     * 
     * 用法：在 ModBlocks 中注册
     * 
     * public static final Block WAXED_COPPER_CHEST = register(
     *     "waxed_copper_chest",
     *     properties -> new WaxedCopperChestBlock(
     *         WeatheringCopper.WeatherState.UNAFFECTED,
     *         ModSoundEvents.COPPER_CHEST_OPEN.get(),
     *         ModSoundEvents.COPPER_CHEST_CLOSE.get(),
     *         properties
     *     ),
     *     BlockBehaviour.Properties.copy(COPPER_CHEST)
     * );
     * 
     * 特点：
     * - 不会氧化
     * - 保持当前的氧化状态
     * - 可以用斧头去除蜡层
     */
    
    /**
     * 示例3: 从铜块创建箱子（铜傀儡生成时使用）
     * 
     * BlockState chestState = BaseCopperChestBlock.createFromCopperBlock(
     *     Blocks.COPPER_BLOCK,     // 要转换的铜块
     *     Direction.NORTH,         // 箱子朝向
     *     level,                   // 世界
     *     blockPos                 // 位置
     * );
     * level.setBlock(blockPos, chestState, 3);
     * 
     * 特点：
     * - 自动根据铜块类型选择对应的箱子
     * - 处理箱子合并逻辑
     * - 保持氧化状态一致性
     */
    
    /**
     * 示例4: 扩展新的箱子类型
     * 
     * 如果需要添加特殊功能的铜箱子，可以继承 BaseCopperChestBlock：
     * 
     * public class CustomCopperChestBlock extends BaseCopperChestBlock {
     *     
     *     public CustomCopperChestBlock(
     *         WeatheringCopper.WeatherState weatherState,
     *         SoundEvent openSound,
     *         SoundEvent closeSound,
     *         BlockBehaviour.Properties properties
     *     ) {
     *         super(weatherState, openSound, closeSound, properties);
     *     }
     *     
     *     @Override
     *     public boolean isWaxed() {
     *         return false; // 或根据需要返回
     *     }
     *     
     *     // 添加自定义功能...
     *     // 例如：特殊的容器逻辑、不同的开关动画等
     * }
     * 
     * 优势：
     * - 自动继承所有箱子合并逻辑
     * - 自动支持氧化状态管理
     * - 自动支持 BlockEntity 保持
     * - 只需实现特定的自定义功能
     */
    
    /**
     * 架构优势总结：
     * 
     * 1. 清晰的职责分离
     *    - BaseCopperChestBlock: 通用箱子功能
     *    - OxidizableCopperChestBlock: 氧化逻辑
     *    - WaxedCopperChestBlock: 防氧化
     * 
     * 2. 易于扩展
     *    - 新的箱子类型只需继承 BaseCopperChestBlock
     *    - 不需要修改现有代码
     *    - 遵循开闭原则（对扩展开放，对修改关闭）
     * 
     * 3. 可维护性强
     *    - 每个类都很小（< 200行）
     *    - 功能单一，易于理解
     *    - 完整的注释文档
     * 
     * 4. 无回调地狱
     *    - 不使用 Function/Supplier 回调
     *    - 直接实例化，代码清晰
     *    - 类型安全，编译时检查
     */
}

