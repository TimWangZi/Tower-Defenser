# 原版村民职业实现整理

本文档基于本地可读取的 Minecraft 1.21.1 / NeoForge 1.21.1 反编译源码整理，目标是回答两个容易混在一起的问题：

1. 原版如何支持“多种村民职业类型”
2. 原版是否支持“单个村民同时拥有多个职业”

先说结论：

- 原版支持很多职业类型。
- 原版不支持单个村民同时拥有多个职业。
- 原版的设计是“一个村民当前只持有一个 `VillagerProfession`，但全局可以注册很多不同的职业”。

这点很重要，因为如果你后续想做的是“农民、工匠、工程师、士兵”等多种职业村民，原版架构可以直接复用；如果你想做的是“一个村民同时兼任多个职业”，那就已经超出了原版的核心假设。

## 当前仓库里的情况

当前项目仓库里没有村民职业的业务代码，只有 README 里有一个待办项：

- `README.md`：`完成农民职业`

所以这份文档重点是给后续实现提供源码级参考，而不是解释仓库内现有逻辑。

## 核心结论

原版的职业系统可以概括成一句话：

`VillagerData` 里保存一个当前职业，职业本身是注册表中的条目，职业切换由 POI 和 AI 驱动，交易、渲染、礼物、工作行为都围绕这个单一职业字段展开。

也就是说：

- “多职业”在原版里指的是“职业种类多”
- 不是“一个村民身上挂一个职业列表”

## 关键类与职责

下面这些类是理解原版职业系统最重要的入口。

| 类 | 作用 | 关键点 |
| --- | --- | --- |
| `net.minecraft.world.entity.npc.VillagerData` | 村民职业数据对象 | 只保存 `type`、`profession`、`level` 三个字段 |
| `net.minecraft.world.entity.npc.VillagerProfession` | 职业定义 | 每个职业绑定工作站判定、工作音效、请求物品等 |
| `net.minecraft.world.entity.ai.village.poi.PoiTypes` | 工作站和 POI 定义 | 把具体方块状态映射到某个 POI 类型 |
| `net.minecraft.world.entity.npc.Villager` | 村民实体本体 | 保存 `VillagerData`，切换职业时刷新 Brain，交易也按职业取表 |
| `net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite` | 分配职业 | 根据占用到的工作站 POI 推导职业 |
| `net.minecraft.world.entity.ai.behavior.ResetProfession` | 重置职业 | 无工作站且等级低、无经验时回退为 `NONE` |
| `net.minecraft.world.entity.ai.behavior.VillagerGoalPackages` | 村民 AI 包装入口 | 把工作站获取、职业分配、职业重置挂到核心行为包 |
| `net.minecraft.world.entity.npc.VillagerTrades` | 交易表 | 交易以 `VillagerProfession -> level -> offers` 的形式组织 |
| `net.neoforged.neoforge.common.VillagerTradingManager` | NeoForge 扩展交易入口 | 为每个职业发出 `VillagerTradesEvent`，方便模组追加交易 |

## 1. 村民数据结构为什么天然是“单职业”

`VillagerData` 是第一层证据。它只保存三个字段：

- `VillagerType type`
- `VillagerProfession profession`
- `int level`

没有职业列表，没有主副职业字段，也没有职业槽位概念。

从源码设计上看，它还是一个不可变对象：

- `setType(...)` 会返回新的 `VillagerData`
- `setProfession(...)` 会返回新的 `VillagerData`
- `setLevel(...)` 会返回新的 `VillagerData`

这意味着原版切换职业的方式是“整个职业字段替换”，不是“往已有职业集合里加一个新职业”。

另外，`VillagerData` 还同时承担了：

- 网络同步
- NBT 存档
- 村民 UI 等级显示

所以只要你还沿用原版 `Villager`，就几乎绕不开这个单职业结构。

## 2. 职业并不是写死在实体里，而是注册表条目

`VillagerProfession` 本质上是一个注册表对象。原版定义了：

- `NONE`
- `ARMORER`
- `BUTCHER`
- `CARTOGRAPHER`
- `CLERIC`
- `FARMER`
- `FISHERMAN`
- `FLETCHER`
- `LEATHERWORKER`
- `LIBRARIAN`
- `MASON`
- `NITWIT`
- `SHEPHERD`
- `TOOLSMITH`
- `WEAPONSMITH`

每个职业条目里至少包含这些内容：

- `name`
- `heldJobSite`
- `acquirableJobSite`
- `requestedItems`
- `secondaryPoi`
- `workSound`

这说明原版实现“多种职业”的方法不是在 `Villager` 里写一堆 if/else，而是：

1. 先注册很多 `VillagerProfession`
2. 再让村民的当前职业字段指向其中一个

这也是后续做模组时最应该复用的部分。

## 3. 工作站是怎么映射到职业的

原版并不是直接写“堆肥桶 = 农民职业”。中间还有一层 POI。

`PoiTypes` 负责把具体方块状态注册成 POI 类型。例如：

- `Blocks.COMPOSTER` -> `PoiTypes.FARMER`
- `Blocks.BLAST_FURNACE` -> `PoiTypes.ARMORER`
- `Blocks.LECTERN` -> `PoiTypes.LIBRARIAN`

`PoiTypes` 内部还有一个“方块状态 -> POI 类型”的全局映射表。也就是说，村民先感知到一个 POI，再由职业定义判断这个 POI 是否是自己的工作站。

原版为什么这样设计：

- 一个职业不需要直接依赖具体方块类
- 一个 POI 可以用更抽象的方式参与 AI 判定
- 以后扩展工作站时，职业和方块映射的耦合更低

另外有一个细节很关键：

- `VillagerProfession.NONE` 的 `acquirableJobSite` 使用的是 `ALL_ACQUIRABLE_JOBS`
- 它本质上允许无业村民去认领“任何可获取的工作站”

所以无业村民不是直接去找“某个具体职业”，而是先抢到一个可获取的工作站，再由后续逻辑决定职业。

## 4. 职业分配流程

职业分配的关键不在构造函数，而在 AI。

`VillagerGoalPackages.getCorePackage(...)` 里会给村民挂上这些和职业相关的行为：

- `ValidateNearbyPoi`
- `AcquirePoi`
- `GoToPotentialJobSite`
- `YieldJobSite`
- `AssignProfessionFromJobSite`
- `ResetProfession`

其中最关键的是下面两步。

### 4.1 `AcquirePoi`

这一阶段的作用是：

- 搜索附近符合条件的工作站 POI
- 把它写入村民记忆模块

在村民的记忆模块里，和职业直接相关的有：

- `JOB_SITE`
- `POTENTIAL_JOB_SITE`
- `SECONDARY_JOB_SITE`

换句话说，职业不是凭空切换的，前置条件是 Brain 里已经认领了某个工作站。

### 4.2 `AssignProfessionFromJobSite`

这个行为真正负责“把工作站转成职业”。

它的大致过程是：

1. 从 `POTENTIAL_JOB_SITE` 取出目标工作站
2. 把它转正成 `JOB_SITE`
3. 如果村民当前职业不是 `NONE`，直接保留
4. 如果当前职业是 `NONE`，就读取该位置实际对应的 `PoiType`
5. 遍历所有已注册的 `VillagerProfession`
6. 找到第一个 `heldJobSite().test(poiType)` 为真的职业
7. 调用 `setVillagerData(getVillagerData().setProfession(...))`
8. 调用 `refreshBrain(...)`

这个流程说明：

- 原版职业切换是“POI 驱动”
- 职业类型来源于注册表
- 选中的职业只有一个

## 5. 职业为什么一改，AI 和交易都会跟着变

`Villager` 自己也明确围绕“当前唯一职业”组织逻辑。

### 5.1 `setVillagerData(...)`

当职业变化时，`Villager.setVillagerData(...)` 会清掉当前 `offers`：

- 旧职业和新职业不一致时，`offers = null`

这意味着交易缓存是按单个当前职业绑定的，不是多职业聚合。

### 5.2 `refreshBrain(...)`

职业变化后会调用 `refreshBrain(...)`，重新注册行为包。

`Villager.registerBrainGoals(...)` 会先读：

- `VillagerProfession villagerprofession = this.getVillagerData().getProfession();`

然后把这个职业传给：

- `getCorePackage(...)`
- `getWorkPackage(...)`
- `getMeetPackage(...)`
- `getRestPackage(...)`
- `getIdlePackage(...)`
- 其他活动包

也就是说：

- Brain 是围绕“当前这一个职业”生成的
- 不是给同一个村民同时挂多套职业工作流

## 6. 原版里职业特化逻辑分散在很多地方

这也是为什么“让一个村民同时拥有多个职业”会很难。

下面这些地方都默认“当前职业只有一个”：

- `VillagerGoalPackages.getWorkPackage(...)`
  农民职业会走 `WorkAtComposter`，其他职业默认走 `WorkAtPoi`
- `HarvestFarmland`
  明确要求 `getProfession() == VillagerProfession.FARMER`
- `TradeWithVillager`
  会读取当前职业的 `requestedItems()`
- `GiveGiftToHero`
  礼物表按 `VillagerProfession` 取
- `VillagerProfessionLayer`
  渲染时只读取一个 `villagerdata.getProfession()`，再叠一层对应职业贴图

这说明原版的很多系统都不是“查询职业集合”，而是“查询当前职业枚举值/注册项”。

## 7. 职业丢失时怎么回退

`ResetProfession` 负责职业回退。

它的判断条件是：

- 当前职业不是 `NONE`
- 当前职业不是 `NITWIT`
- 没有 `JOB_SITE`
- `villagerXp == 0`
- `level <= 1`

满足这些条件时，职业会被重置回 `VillagerProfession.NONE`，然后再次 `refreshBrain(...)`。

也就是说，原版把“新手未锁定职业的村民”视为可重新就业对象；但一旦已经有经验和职业等级，就不会随便回退。

## 8. 交易表是怎么按职业组织的

`VillagerTrades.TRADES` 的结构是：

- `Map<VillagerProfession, Int2ObjectMap<ItemListing[]>>`

可以理解成：

- 第一层 key：职业
- 第二层 key：职业等级 1 到 5
- value：这个等级下可抽取的交易列表

例如农民的交易就是：

- `VillagerProfession.FARMER`
  - level 1 对应一组 `ItemListing[]`
  - level 2 对应一组 `ItemListing[]`
  - 以此类推

而 `Villager.updateTrades()` 的逻辑也很直接：

1. 读取当前 `VillagerData`
2. 先用当前职业去 `VillagerTrades.TRADES` 里拿职业表
3. 再用当前等级取该等级的交易列表
4. 从这一层里加入交易项

这里没有“把两个职业的交易表合并”的逻辑。

## 9. NeoForge 给了什么扩展点

如果你只是想给已有职业加交易，或者给自定义职业配置交易，NeoForge 已经提供了扩展点，不需要改原版类。

最关键的是：

- `net.neoforged.neoforge.common.VillagerTradingManager`
- `net.neoforged.neoforge.event.village.VillagerTradesEvent`

`VillagerTradingManager` 会在数据重载时：

1. 遍历所有已注册的 `VillagerProfession`
2. 为每个职业构造可变交易表
3. 发出一次 `VillagerTradesEvent`
4. 把事件修改后的结果写回 `VillagerTrades.TRADES`

这意味着：

- 模组可以按职业追加交易
- 重点仍然是“一个职业一份交易表”
- 不是“一个村民维护多个职业交易池”

## 10. 对“多个职业”这个需求该怎么理解

### 情况 A：你想支持很多职业种类

这是原版支持得很好的方向。

思路是：

1. 注册新的 `PoiType`
2. 让某个方块状态映射到这个 `PoiType`
3. 注册新的 `VillagerProfession`
4. 让它的 `heldJobSite` / `acquirableJobSite` 指向这个 POI
5. 用 `VillagerTradesEvent` 给该职业添加交易
6. 如果需要特殊工作行为，再做职业特化 AI

这条路和原版设计完全一致。

### 情况 B：你想让一个村民同时拥有多个职业

这是原版不擅长的方向。

难点不只是在数据结构，而是在整套系统假设都偏向单职业：

- `VillagerData` 只有一个 `profession`
- `Villager` 的 Brain 构建只接收一个职业
- `updateTrades()` 只按一个职业取表
- 工作站记忆模块只有一套主职业工作站逻辑
- 渲染层只画一个职业外观
- 礼物、工作音效、请求物品也都按单个职业取

如果硬改原版 `Villager` 去支持“职业列表”，你通常需要同时重做：

- 同步数据结构
- NBT 存档
- Brain 构建
- 工作站认领
- 交易生成
- 村民外观渲染
- 职业相关行为判断

这个成本通常比直接写一个自定义实体更高。

## 11. 对当前项目的建议

结合你这个仓库的目标，建议分成两种路线。

### 路线 1：做“多种职业村民”

如果你的意思是：

- 先做农民
- 后面再做工匠、工程师、兵工师之类

那就按原版职业系统做，收益最大，维护成本最低。

建议顺序：

1. 先用原版 `Villager` 逻辑理解职业切换
2. 补一个自定义 `PoiType`
3. 补一个自定义 `VillagerProfession`
4. 用 `VillagerTradesEvent` 接交易
5. 再按职业定制特殊行为

### 路线 2：做“单个 NPC 同时拥有多个职业能力”

如果你的目标是：

- 同一个 NPC 既能种地又能交易军火又能修建筑

那不建议强行复用原版 `Villager` 的职业字段。

更稳的做法是：

1. 自己做一个自定义实体
2. 自己定义职业槽位或能力列表
3. 自己组织任务系统和交易池
4. 只复用原版里你需要的局部能力，例如交易 UI 或 POI 搜索思路

一句话说：

- 要“很多职业类型”，复用原版
- 要“单个实体同时多职业”，自定义实体更合理

## 12. 最值得直接去看的源码包

如果后续你要继续实现，建议优先看这些类：

- `net.minecraft.world.entity.npc.VillagerData`
- `net.minecraft.world.entity.npc.VillagerProfession`
- `net.minecraft.world.entity.npc.Villager`
- `net.minecraft.world.entity.ai.village.poi.PoiTypes`
- `net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite`
- `net.minecraft.world.entity.ai.behavior.ResetProfession`
- `net.minecraft.world.entity.ai.behavior.VillagerGoalPackages`
- `net.minecraft.world.entity.npc.VillagerTrades`
- `net.neoforged.neoforge.common.VillagerTradingManager`
- `net.neoforged.neoforge.event.village.VillagerTradesEvent`

## 总结

原版“多个职业”的实现方式，不是给一个村民保存多个职业，而是：

1. 在注册表里定义很多职业类型
2. 用工作站 POI 和 AI 让村民在这些职业之间切换
3. 让交易、工作行为、渲染、礼物等系统都围绕当前唯一职业运行

所以如果你后续要在这个模组里扩展职业系统，首先要决定的是：

- 你要的是“多职业种类”
- 还是“单个单位多职业并存”

这两个方向在原版架构里的实现成本差很多，不能混为一谈。
