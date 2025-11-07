# Copper Friend Backport

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)
![Forge](https://img.shields.io/badge/Forge-47.4.0-orange.svg)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)

一个将铜傀儡（Copper Golem）从 Minecraft 1.21+ 向后移植到 1.20.1 的 Forge 模组。

## 📖 简介

本模组将铜傀儡完整功能移植到 1.20.1 版本，让玩家可以在当前主流版本中体验这个可爱的机械助手。

## ✨ 特性

### 核心功能
- **完整的铜傀儡实体**：包含完整的 AI 行为系统
- **氧化机制**：随时间推移会经历四个氧化阶段（未氧化 → 轻微氧化 → 半氧化 → 氧化）
- **雕像转化**：完全氧化后有几率变成铜傀儡雕像方块
- **闪电去氧化**：被闪电击中可以逆转氧化过程
- **蜂蜡保护**：使用蜂蜡可以停止氧化进程
- **斧头刮蜡/去氧化**：使用斧头可以移除蜂蜡或降低氧化等级

### 交互系统
- **物品交互**：可以给铜傀儡物品，它会拿在手中
- **容器互动**：铜傀儡可以打开箱子并搬运物品
- **按钮交互**：铜傀儡会随机按下铜按钮
- **剪羊毛**：使用剪刀可以移除铜傀儡头上的罂粟天线

### 视觉效果
- **完整的 3D 模型**：基于官方 1.21.10 模型数据，UV 坐标精确
- **动画系统**：
  - 空闲时的头部晃动动画
  - 转头动画（带有旋转音效）
  - 拾取/放下物品的交互动画
  - 行走动画（持有/不持有物品时不同）
- **音效系统**：
  - 生成音效
  - 行走音效（不同氧化等级音调不同）
  - 受伤/死亡音效（随氧化等级变化）
  - 转头音效（4 种氧化等级各不相同）
  - 剪羊毛音效

### 氧化等级系统
每个氧化等级都有独特的：
- 声音音调（氧化程度越高音调越低）
- 专属的转头音效
- 对应的方块雕像形态

## 🎮 如何使用

### 生成铜傀儡
- 铜傀儡会自然生成（取决于模组配置）
- 可通过命令生成：`/summon copper_friend_backport:copper_golem`

### 与铜傀儡互动
1. **给予物品**：手持物品右键点击铜傀儡
2. **取回物品**：空手右键点击持有物品的铜傀儡
3. **涂蜡**：使用蜂蜡右键点击可停止氧化
4. **刮蜡/去氧化**：使用斧头右键点击可降低氧化等级
5. **剪毛**：使用剪刀右键点击可移除天线（罂粟）

### 氧化管理
- 自然氧化：504,000 - 552,000 游戏刻（约 7-7.5 小时）
- 闪电击中：降低一个氧化等级
- 使用蜂蜡：永久停止氧化
- 使用斧头：手动降低氧化等级

## 🔧 技术细节

### 开发信息
- **Minecraft 版本**: 1.20.1
- **Forge 版本**: 47.4.0
- **Java 版本**: 17
- **映射**: Official Mappings 1.20.1

### 项目结构
```
src/main/java/org/xiyu/yee/copper_friend_backport/
├── coppergolem/           # 铜傀儡实体相关
│   ├── CopperGolem.java          # 主实体类
│   ├── CopperGolemAi.java        # AI 行为系统
│   ├── CopperGolemState.java     # 状态枚举
│   └── CopperGolemOxidationLevels.java  # 氧化等级定义
├── client/
│   ├── model/
│   │   └── CopperGolemModel.java # 3D 模型和动画
│   └── renderer/
│       └── CopperGolemRenderer.java  # 渲染器
├── world/                 # 方块相关
│   ├── CopperGolemStatueBlock.java
│   └── CopperGolemStatueBlockEntity.java
└── registry/              # 注册表
    ├── ModBlocks.java
    ├── ModSoundEvents.java
    ├── ModMemoryModules.java
    └── EntityDataSerializers.java
```

### 主要技术实现
- **DeferredRegister 系统**：用于 Forge 模组内容注册
- **Brain/Behavior AI**：使用 Minecraft 原版的行为树系统
- **AnimationState**：1.20.1 原版动画状态系统
- **自定义 EntityDataSerializer**：序列化氧化状态和实体状态
- **WeatheringCopper 接口**：复用原版铜氧化机制

## 🏗️ 构建项目

### 前置要求
- JDK 17 或更高版本
- Gradle（已包含 Wrapper）

### 构建步骤
```bash
# 克隆仓库
git clone <repository-url>
cd copper_friend_backport

# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

构建完成后，模组 JAR 文件将位于 `build/libs/` 目录。

### 开发环境
```bash
# 生成 IDE 配置
./gradlew genIntellijRuns  # IntelliJ IDEA
./gradlew genEclipseRuns   # Eclipse

# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer
```

## 👥 作者

- **fho4565**
- **mai_xiyu**
- **Blue_rose**

## 📜 许可证

All Rights Reserved

## 🐛 问题反馈

如遇到问题或有建议，请在 GitHub Issues 中反馈。

## 📝 更新日志

### v1.0
- ✅ 完整的铜傀儡实体实现
- ✅ 四阶段氧化系统
- ✅ 雕像转化机制
- ✅ 完整的音效系统（30+ 音效）
- ✅ 动画系统（空闲、转头、交互）
- ✅ 容器交互和物品搬运 AI
- ✅ 按钮按压行为
- ✅ 蜂蜡/斧头交互
- ✅ 剪羊毛功能

## 🎯 已知问题

无重大已知问题。如发现 bug 请提交 issue。

## 🔮 未来计划

- [ ] 添加更多交互动画
- [ ] 优化 AI 性能
- [ ] 添加配置文件支持
- [ ] 添加剩余内容

---

**注意**：本模组是粉丝制作的向后移植版本，并非 Mojang 官方内容。
