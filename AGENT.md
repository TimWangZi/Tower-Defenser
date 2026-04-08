# AGENT.md

## 项目概览

这是一个基于 NeoForge 的 Minecraft Java Edition 模组仓库，当前目标版本和核心环境如下：

- Minecraft: `1.21.1`
- NeoForge: `21.1.219`
- Java: `21`
- GeckoLib: `4.8.3`
- Mod ID: `minecraft_tower_defenser`
- 主包名: `com.timwang.mc_tower_defenser`

当前实现重点不是通用模板功能，而是以下几条业务主线：

- 国家 / 领地系统
- `UrbanCore` 据点方块及其领地判定
- 工作方块与工作图
- `CitizenEntity` 市民实体与 `FarmerProfession` 职业状态机
- 建国界面、工作方块界面、国家数据同步

注意：代码目录里长期使用了 `fundation` 这一拼写，而不是 `foundation`。除非要做一次完整包迁移，否则新增代码时应保持现有拼写，避免破坏资源路径、Mixin 配置和包引用。

## 快速启动

Windows 下优先使用：

```powershell
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTestServer
.\gradlew.bat runData
.\gradlew.bat build
```

如果在类 Unix 环境运行，则使用：

```bash
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
./gradlew runData
./gradlew build
```

补充说明：

- `runClient` 用于本地客户端联调 GUI、渲染、实体、交互。
- `runServer` 用于验证服务端权威逻辑，例如国家管理、领地保护、工作方块注册。
- `runGameTestServer` 已配置，但仓库里当前没有 `src/test` 或 `src/gameTest` 测试源码。
- `runData` 会把数据生成结果输出到 `src/generated/resources/`。`build.gradle` 已把这个目录并入资源源集，但当前仓库里该目录尚未生成。
- CI 工作流 `.github/workflows/build.yml` 当前只执行 `./gradlew build`。

## 仓库地图

### 根目录

- `build.gradle`
  Gradle 构建入口，定义 NeoForge、GeckoLib、运行配置和 datagen 输出目录。
- `gradle.properties`
  Minecraft、NeoForge、GeckoLib、mod 元数据版本声明。
- `README.md`
  混合了项目 TODO 和 NeoForge 模板残留说明，不能作为唯一事实来源。
- `docs/`
  已有几份专题设计文档，扩展功能前应先读对应文档。
- `run/`、`run-client-a/`、`run-client-b/`、`run-server-test/`
  本地开发运行目录，不是源码目录。

### Java 代码

- `src/main/java/com/timwang/mc_tower_defenser/MinecraftTowerDefenser.java`
  模组公共入口，负责注册方块、方块实体、实体、菜单、物品、网络。
- `src/main/java/com/timwang/mc_tower_defenser/MinecraftTowerDefenserClient.java`
  客户端入口，负责屏幕注册和客户端扩展点挂接。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blocks`
  方块注册与方块实现，当前重点是 `UrbanCoreBlock` 和 `FarmerWorkBlock`。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blockEntities`
  方块实体实现，包含据点绑定、工作方块库存、工作图节点注册等逻辑。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/entities`
  实体注册和 `CitizenEntity`。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/ai`
  Goal、职业系统、任务状态机。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/system`
  国家管理、世界持久化、事件处理、工作图。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/network`
  Payload、Handler、同步服务、客户端缓存。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/gui`
  菜单类型、屏幕注册、实际界面。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/mixin`
  客户端 Mixin，当前用于往原版背包界面注入建国按钮。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/render`
  GeckoLib / 客户端渲染注册。
- `src/main/java/com/timwang/mc_tower_defenser/fundation/model`
  Geo 模型定义。

### 资源

- `src/main/resources/assets/minecraft_tower_defenser`
  语言、模型、Geo、动画、贴图等客户端资源。
- `src/main/resources/minecraft_tower_defenser.mixins.json`
  Mixin 配置入口。
- `src/main/templates/META-INF/neoforge.mods.toml`
  构建时展开 mod 元数据模板。
- `src/generated/resources`
  预留给 datagen 结果，目前仓库内尚不存在。

## 核心系统说明

### 1. 注册层

模组注册入口集中在 `MinecraftTowerDefenser`：

- `ModBlocks`
- `ModBlockEntities`
- `ModEntities`
- `ModGuiMenu`
- `ModItems`
- `ModNetwork`

客户端专属内容集中在 `MinecraftTowerDefenserClient` 和 `ModGuiScreen`。

注意：`MinecraftTowerDefenser` 里仍保留了 NeoForge 模板自带的 `EXAMPLE_BLOCK`、`EXAMPLE_ITEM`、`EXAMPLE_TAB` 等示例内容。做业务功能时不要把这些模板代码误当成正式系统的一部分；如果后续要清理模板残留，应独立处理。

### 2. 国家系统与同步

国家系统是当前仓库最重要的服务端权威系统：

- `NationManager`
  表示单个国家的数据快照，包含国家名、成员、塔坐标、工作图。
- `GlobalNationManager`
  继承 `SavedData`，管理全部国家和玩家归属索引。
- `NationSyncService`
  负责把服务端国家快照同步给玩家。
- `ClientNationState`
  客户端只读缓存，不应被当成真实数据源。

工作约束：

- 真正的数据修改必须发生在服务端。
- 任何国家数据被修改后，都应考虑立即补同步。
- 当前已有同步入口包括玩家登录、建国请求完成、`UrbanCore` 注册/注销后同步国家成员。
- 玩家身份必须优先信任服务端 `context.player()` 或服务端事件里的真实玩家对象，不要信任客户端上传的玩家名。

### 3. 领地与据点

`UrbanCoreBlockEntities` 会把据点注册到国家系统，并以此支撑领地判定。

当前行为特点：

- 领地范围基于塔坐标半径判定，当前半径逻辑是 `10` 格。
- `BlockEventHandler` 会阻止非本国玩家在领地内破坏方块。
- `GlobalNationManager` 保存塔列表，并提供按玩家、国家名、领地坐标的查询。

如果调整领地逻辑，至少要同步检查：

- `NationManager.isInTerritory`
- `GlobalNationManager.getNationByTerritory`
- `UrbanCoreBlockEntities`
- `BlockEventHandler`
- 相关同步或 UI 展示逻辑

### 4. 工作方块与工作图

当前工作系统围绕工作节点图展开：

- `WorkGraphManager`
  保存节点和有向边。
- `WorkBlockEntities`
  工作方块基类，负责库存、节点注册/注销、上下游物资请求和投递。
- `FarmerWorkBlock` / `FarmerWorkBlockEntities`
  目前已落地的工作方块实现。

新增工作方块时通常至少要补齐：

- 方块注册 `ModBlocks`
- 方块实体注册 `ModBlockEntities`
- 物品注册 `ModItems`
- 方块类 + 方块实体类
- 菜单 / 屏幕注册（如果需要交互界面）
- 语言键、模型、贴图、可能的 Geo / 动画资源
- 放置和拆除时的国家工作图注册 / 注销逻辑

### 5. 实体、职业与任务状态机

当前实体主线是 `CitizenEntity`，职业系统主线是 `FarmerProfession`。

关键关系：

- `CitizenEntity`
  持有背包、绑定工作方块坐标、职业类型 ID、职业实例。
- `CitizenProfessionTypes`
  职业类型工厂入口。
- `ProfessionBase`
  职业基类。
- `FarmerProfession`
  基于 `StateMachine` 驱动多个任务状态。
- `fundation/ai/profession/task/*`
  具体任务实现，例如采摘、补给、逃跑、闲逛。

修改职业系统时要注意：

- 若职业需要持久化，必须检查实体 NBT 读写和 `restoreProfessionFromSavedState()`。
- 若职业依赖工作方块，需保证实体与工作方块坐标绑定逻辑一致。
- 若新增任务切换条件，优先在状态机层表达，而不是分散在实体 tick 里硬编码。

### 6. GUI 与交互入口

当前有两类 GUI：

- `CreateCountryScreen`
  纯客户端屏幕，不通过容器同步物品。
- `FarmerWorkMenu` + `FarmerWorkScreen`
  标准容器菜单，带服务端交互。

建国入口不是单独方块，而是通过 Mixin 注入到原版背包界面：

- `InventoryScreenInjector`

因此修改建国 UI 时，不只要看 `CreateCountryScreen`，还要看：

- `ClientNationState`
- `InventoryScreenInjector`
- `RegisterNationPayloads`
- `RegisterNationHandler`

## 资源与命名约定

请优先遵循现有资源命名和模组命名空间：

- namespace: `minecraft_tower_defenser`
- 文件名与资源路径使用小写、`snake_case`
- Geo / 动画 / 贴图路径尽量与注册名保持一致

资源目录规范的详细说明已经单独写在：

- `docs/MINECRAFT_RESOURCE_CONVENTIONS.md`

如果要新增资源，先检查实际引用路径是否与代码中的 `ResourceLocation` 约定一致，再提交代码修改。

## 推荐阅读顺序

遇到不同改动方向，先看这些文档：

- 国家数据、持久化、客户端缓存、Payload 同步：
  `docs/NATION_MANAGER_AND_NETWORK_SYNC.md`
- 实体、渲染、阵营归属、AI 结构：
  `docs/ENTITY_AND_AI_OVERVIEW.md`
- 资源目录、命名空间、Geo / assets / data 规范：
  `docs/MINECRAFT_RESOURCE_CONVENTIONS.md`
- 原版村民职业实现参考：
  `docs/VILLAGER_PROFESSION_IMPLEMENTATION.md`

## 开发时的实际约束

- 这个仓库目前是单模块 Gradle 项目，不存在多子项目拆分。
- `README.md` 里包含待办信息，但也夹带 NeoForge 模板说明，做判断时以源码和 `docs/` 为准。
- 当前没有正式测试源码目录，因此验证通常依赖 `build`、`runClient`、`runServer` 和手动联调。
- `build.gradle` 已启用 Java 21 toolchain，不要按旧版 Java 语法写兼容代码。
- 资源和类注册普遍依赖字符串 ID，重命名时要同时检查注册名、语言键、模型路径、贴图路径、网络 payload type 和存档兼容性。

## 建议的改动检查清单

在提交较大变更前，至少自行检查以下项目：

1. 是否遗漏注册入口。
2. 是否遗漏客户端屏幕、渲染或语言资源。
3. 是否遗漏 NBT 持久化或网络同步。
4. 是否在服务端修改了国家数据但没有触发同步。
5. 是否破坏了工作图节点注册 / 注销的闭环。
6. 是否误改了 `fundation` 包路径、`mod_id` 命名空间或已有资源路径。
