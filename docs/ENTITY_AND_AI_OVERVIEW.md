# 实体与生物 AI 代码整理

本文档整理当前项目里与“实体 / 生物 / 生成 / AI / 渲染”相关的核心代码，方便后续继续扩展 `NormalSoldier`、国家归属逻辑与自定义 Goal。

## 目录概览

- `src/main/java/com/timwang/mc_tower_defenser/fundation/entities`
  负责实体类型注册。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/entities/Mobs`
  具体实体实现，目前主要是 `NormalSoldier`。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/utils/ai/goal`
  自定义 Goal 放置目录，目前有占位类 `AttackOtherGoal`。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/render`
  客户端渲染注册与 Geckolib renderer。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/model`
  Geckolib 模型定义。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/system`
  国家系统、领地判定与 `SavedData` 持久化。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blockEntities/Core`
  `UrbanCore` 方块实体，负责把塔坐标接入国家系统。

## 代码关系

### 1. 实体注册

入口文件：

- `fundation/entities/ModEntities.java`

当前职责：

- 通过 `DeferredRegister<EntityType<?>>` 注册 `NORMAL_SOLDIER`
- 在 `EntityAttributeCreationEvent` 里把 `NormalSoldier.createAttributes()` 绑定到实体类型

当前注册结果：

- 实体 id：`minecraft_tower_defenser:normal_soldier`
- 分类：`MobCategory.MONSTER`

这意味着：

- 实体已经能被正常构造
- 只要通过标准 `Mob` 生成路径进入世界，就会拥有基础属性表

### 2. 普通士兵实体

入口文件：

- `fundation/entities/Mobs/NormalSoldier.java`

当前已经具备的能力：

- 继承 `PathfinderMob`
- 接入 Geckolib 的 `GeoEntity`
- 拥有基础近战 AI
- 在出生时根据领地自动记录所属国家
- 将国家归属写入同步数据和 NBT，保证服务端 / 客户端 / 存档一致

核心字段：

- `DATA_NATION_BELONG_TO`
  使用 `SynchedEntityData` 保存阵营名
- `NATION_TAG`
  存档用 NBT key

核心方法：

- `registerGoals()`
  当前只挂了三个原版 Goal
- `finalizeSpawn(...)`
  出生时读取当前位置，按塔领地决定国家归属
- `setNationBelongTo(...)`
  写入阵营名
- `getNationBelongTo()`
  读取阵营名
- `hasNationBelongTo()`
  判断是否已经归属某个国家
- `addAdditionalSaveData(...)` / `readAdditionalSaveData(...)`
  持久化国家归属

### 3. 当前普通士兵 AI

`NormalSoldier.registerGoals()` 目前挂接了：

1. `NearestAttackableTargetGoal<Player>`
   会把玩家当作攻击目标
2. `WaterAvoidingRandomStrollGoal`
   无目标时随机游走
3. `MeleeAttackGoal`
   近战追击并攻击目标

说明：

- 现在还没有接入“同阵营不互打”或“敌对阵营优先攻击”的逻辑
- 也没有把国家归属接入 target 选择条件
- `goalSelector` 里挂了 `NearestAttackableTargetGoal`，严格来说这个 Goal 更常见于 `targetSelector`
  当前代码能编译和运行，但后续最好整理到更标准的 selector 上

## 自定义 AI 预留点

入口文件：

- `fundation/utils/ai/goal/AttackOtherGoal.java`
- `fundation/utils/StateMachine.java`

当前状态：

- `AttackOtherGoal` 只是占位类，还没有任何实际判断
- `StateMachine` 是通用工具类，目前还没有接到 `NormalSoldier`

推荐后续扩展方向：

1. 让 `AttackOtherGoal` 接收“本实体所属国家”和“目标实体所属国家”的判断函数
2. 将“搜敌目标”逻辑移到 `targetSelector`
3. 用 `StateMachine` 把士兵状态分成：
   `IDLE` / `PATROL` / `CHASE` / `ATTACK` / `RETURN_TO_TERRITORY`
4. 如果后续要支持客户端特效或动画切换，再把状态同步到实体数据

## 国家与领地系统如何影响生物

### 1. 塔坐标来源

入口文件：

- `fundation/blockEntities/Core/UrbanCoreBlockEntities.java`

当前逻辑：

- `UrbanCore` 在 `onLoad()` 时把自己注册进 `GlobalNationManager`
- `setRemoved()` 时从全局管理器注销

注意：

- 当前 `UrbanCoreBlockEntities` 里仍然是测试逻辑：
  会把塔注册到 `"test"` 国家，成员名写 `"system"`
- 这意味着领地系统已经能跑，但国家归属来源还不是最终设计

### 2. 国家与领地数据

入口文件：

- `fundation/system/NationManager.java`
- `fundation/system/GlobalNationManager.java`

职责划分：

- `NationManager`
  单个国家的数据对象，保存国家名、成员列表、塔坐标
- `GlobalNationManager`
  世界级 `SavedData`，保存所有国家与玩家归属索引

关键方法：

- `NationManager.isInTerritory(BlockPos pos)`
  判断某个坐标是否落在本国任意塔的保护半径内
- `GlobalNationManager.isInAnyTerritory(BlockPos pos)`
  判断某坐标是否落在任意国家领地内
- `GlobalNationManager.getNationByTerritory(BlockPos pos)`
  返回该坐标所属的国家

### 3. 士兵如何继承国家

在 `NormalSoldier.finalizeSpawn(...)` 中：

1. 先调用 `super.finalizeSpawn(...)`
2. 读取当前出生点 `this.blockPosition()`
3. 调用 `GlobalNationManager.get(...).getNationByTerritory(...)`
4. 如果出生点落在某个国家领地内，则写入该国家名
5. 否则写空字符串，表示没有归属国家

这让“生物出生归属阵营”与“塔附近不可破坏领地”使用的是同一套判定规则。

## 生成流程整理

### 1. Minecraft / NeoForge 标准生成顺序

在 NeoForge 1.21.1 下，和 `Mob` 生成有关的逻辑大体是：

1. `SpawnPlacementCheck`
   检查生物是否允许在该位置/环境生成
2. 创建实体对象
3. `PositionCheck`
   检查当前坐标对实体是否合法
4. `FinalizeSpawnEvent`
   允许事件修改初始化参数或取消 spawn finalize
5. `Mob.finalizeSpawn(...)`
   执行实体自己的出生初始化
6. `EntityJoinLevelEvent`
   实体正式加入世界

对本项目最重要的点：

- `NormalSoldier` 的国家归属逻辑放在 `finalizeSpawn(...)`
- 因此只有“会经过标准 mob finalize 流程”的生成方式，才一定会自动归属国家

### 2. 当前会触发归属逻辑的生成来源

通常包括：

- 自然生成
- 结构生成
- `/summon`
- 多数通过 `EntityType.spawn(...)` 进入世界的生成

### 3. 当前需要注意的例外

并不是所有 `Mob` 的出现都会走 `finalizeSpawn(...)`。

典型例外：

- 某些“从已有生物派生出新生物”的特殊路径
- 一些 mod 自己直接 `new Entity + addFreshEntity(...)` 的路径

这意味着：

- 如果后续你要求“所有来源生成的士兵都必须自动继承阵营”
- 那么不能只依赖 `finalizeSpawn(...)`
- 还需要补一层更宽的兜底逻辑，例如：
  `EntityJoinLevelEvent` 上做一次服务端补判定

## 渲染与模型

### 1. 渲染注册

入口文件：

- `fundation/render/ModGeoRenderer.java`

职责：

- 注册 `UrbanCoreRenderer`
- 注册 `NormalSoldierRender`

### 2. 普通士兵渲染链

相关文件：

- `fundation/render/Mobs/NormalSoldierRender.java`
- `fundation/model/Mob/NormalSoldierModel.java`

当前状态：

- 几何模型已接上
- 贴图已接上
- 动画资源还是占位路径 `animations/null`
- `NormalSoldier.registerControllers(...)` 仍为空

这说明：

- 实体现在能渲染模型
- 但还没有真正的动作动画控制器

## 当前代码中的已知整理结果

本轮已做的轻量清理：

- 清掉了 `ModEntities`、`UrbanCoreRenderer`、`AttackOtherGoal`、`NormalSoldierModel`、`UrbanCoreBlockEntities` 等文件的无用 import
- 去掉了 `NormalSoldier` 中已经失效的注释代码块
- 把 `ModGeoRenderer` 的控制台输出改成统一的 `LOGGER`

这些改动不影响行为，只是让实体与 AI 相关代码更容易继续维护。

## 推荐的下一步

如果后续继续做“国家化士兵”，建议按这个顺序推进：

1. 把 `NearestAttackableTargetGoal` 调整到 `targetSelector`
2. 用国家归属过滤目标，避免同阵营互相攻击
3. 实现 `AttackOtherGoal`
4. 给 `NormalSoldier` 补 Geckolib 动画控制器
5. 把 `UrbanCoreBlockEntities` 里的 `"system" / "test"` 测试逻辑替换成真实国家绑定
6. 如果希望所有生成来源都归属国家，再补 `EntityJoinLevelEvent` 兜底

## 相关文件索引

- `src/main/java/com/timwang/mc_tower_defenser/fundation/entities/ModEntities.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/entities/Mobs/NormalSoldier.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/utils/ai/goal/AttackOtherGoal.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/utils/StateMachine.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/system/NationManager.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/system/GlobalNationManager.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blockEntities/Core/UrbanCoreBlockEntities.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/render/ModGeoRenderer.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/render/Mobs/NormalSoldierRender.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/model/Mob/NormalSoldierModel.java`
