# 国家管理器与网络同步说明

本文档整理当前项目里“国家数据的服务端管理、持久化、网络同步、客户端缓存”这一整条链路，方便以后继续给国家系统加字段、加玩法、加同步逻辑时直接对照修改。

适用范围：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/system`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/network`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blockEntities/Core`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blocks/Core`

## 1. 整体设计

当前国家系统是“服务端权威，客户端只读缓存”的结构。

可以把它理解成四层：

1. `NationManager`
   单个国家的数据对象，保存国家名称、成员列表、UrbanCore 坐标列表。
2. `GlobalNationManager`
   世界级 `SavedData`，保存全部国家列表和玩家 -> 国家名索引，并负责落盘。
3. `NationSyncService + payload/handler`
   把服务端国家快照发给客户端。
4. `ClientNationState`
   客户端本地缓存，只保存最近一次同步下来的数据，供 GUI 或客户端逻辑读取。

核心原则：

- 真正的数据源只在服务端。
- 客户端不要直接改国家真实数据。
- 国家数据只要发生服务端变更，就应该显式补一轮同步。
- 任何依赖玩家身份的逻辑，都应该优先信任 `context.player()` 或服务端事件里的真实玩家对象，而不是信任客户端上传的玩家名。

## 2. 目录与职责

### 2.1 `NationManager`

文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/system/NationManager.java`

职责：

- 表示一个国家的完整数据快照。
- 保存这个国家的名称。
- 保存成员玩家名列表。
- 保存 UrbanCore 坐标列表。
- 提供 NBT 序列化 / 反序列化。
- 提供网络传输用 `StreamCodec`。

当前重要字段：

- `name`
- `memberNames`
- `towerPositions`

当前重要方法：

- `addMember(String memberName)`
- `hasMember(String memberName)`
- `registerTower(String memberName, BlockPos pos)`
- `unregisterTower(BlockPos pos)`
- `isInTerritory(BlockPos pos)`
- `serializeNBT()`
- `deserializeNBT(CompoundTag tag)`
- `copy()`

### 2.2 `GlobalNationManager`

文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/system/GlobalNationManager.java`

职责：

- 作为世界级 `SavedData` 持久化全部国家数据。
- 管理国家列表。
- 管理玩家 -> 国家名索引。
- 提供常用查询入口。
- 提供建国、成员绑定、塔注册/注销等操作。
- 在发生持久化层变更时调用 `setDirty()`。

当前重要数据：

- `nationList`
- `playerNationality`

当前重要方法：

- `createNation(String playerName, String nationName)`
- `getNationByPlayer(String playerName)`
- `getNationByName(String nationName)`
- `getNationByTerritory(BlockPos pos)`
- `bindPlayerToNation(String playerName, NationManager nation)`
- `registerTower(NationManager nation, BlockPos pos, String memberName)`
- `unregisterTowerAndGetNation(BlockPos pos)`
- `save(CompoundTag tag, HolderLookup.Provider registries)`
- `load(CompoundTag tag, HolderLookup.Provider provider)`

### 2.3 网络层

目录：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/network`

职责拆分：

- `ModNetwork`
  统一注册 payload 和 handler。
- `NationSyncService`
  服务端同步工具，把国家快照推给在线玩家。
- `payloads/*`
  具体网络包定义与 codec。
- `handler/*`
  收包后的服务端 / 客户端处理逻辑。
- `ClientNationState`
  客户端只读缓存。

### 2.4 国家数据与方块实体的连接点

相关文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/blocks/Core/UrbanCoreBlock.java`
- `src/main/java/com/timwang/mc_tower_defenser/fundation/blockEntities/Core/UrbanCoreBlockEntities.java`

当前职责：

- 方块放下时，读取放置玩家所属国家。
- 把 owner 和 nation 绑定到方块实体。
- 将当前 UrbanCore 坐标注册到对应国家。
- 方块移除时注销对应的塔坐标。
- 注册 / 注销后同步对应国家的在线成员。

## 3. 当前数据流

### 3.1 建国流程

链路：

客户端发送 `RegisterNationPayloads`
-> `RegisterNationHandler.server_handler`
-> `GlobalNationManager.createNation(...)`
-> `NationSyncService.syncPlayer(player)`
-> 客户端 `ClientNationState.update(...)`

注意：

- 虽然 `RegisterNationPayloads` 里有 `player_name` 字段，但服务端目前并不信任它。
- 服务端使用 `context.player().getGameProfile().getName()` 作为真实玩家身份。

### 3.2 玩家登录同步

链路：

`PlayerEventHandler.onPlayerLoggedIn`
-> `NationSyncService.syncPlayer(player)`
-> `SyncPlayerNationPayload`
-> `PlayerNationPayloadHandler.clientHandler`
-> `ClientNationState.update(...)`

作用：

- 保证客户端刚进服时就拿到一份当前国家快照。
- GUI 不需要先手动猜测国家状态。

### 3.3 客户端主动请求同步

链路：

`ClientNationState.requestSync()`
-> 发送 `RequestPlayerNationPayload`
-> `PlayerNationPayloadHandler.serverHandler`
-> `NationSyncService.syncPlayer(player)`
-> `PlayerNationPayloadHandler.clientHandler`
-> `ClientNationState.update(...)`

适用场景：

- GUI 打开时主动刷新。
- 客户端觉得本地缓存过期时重新拉取。

### 3.4 UrbanCore 注册流程

链路：

`UrbanCoreBlock.setPlacedBy(...)`
-> 通过放置玩家名查 `GlobalNationManager.getNationByPlayer(...)`
-> `UrbanCoreBlockEntities.bindToNation(ownerPlayerName, nationName)`
-> `UrbanCoreBlockEntities.registerBoundNation(serverLevel)`
-> `GlobalNationManager.registerTower(...)`
-> `NationSyncService.syncNationMembers(server, nation)`

补充说明：

- `UrbanCoreBlockEntities` 会把 `ownerPlayerName` 和 `nationName` 写进自身 NBT。
- 区块重新加载时，`onLoad()` 会再次执行 `registerBoundNation(serverLevel)`，确保存档恢复后塔依然挂回正确国家。
- 恢复时优先使用保存下来的 `nationName`，不会因为玩家后来改了国家就把旧塔自动迁过去。

### 3.5 UrbanCore 注销流程

链路：

`UrbanCoreBlock.onRemove(...)`
-> `UrbanCoreBlockEntities.unregisterBoundNation(serverLevel)`
-> `GlobalNationManager.unregisterTowerAndGetNation(pos)`
-> `NationSyncService.syncNationMembers(server, affectedNation)`

作用：

- 塔被真正移除时，把领地中心从对应国家里删掉。
- 国家在线成员收到新的国家快照。

### 3.6 其他服务端逻辑读取国家数据

例如：

- `BlockEventHandler`
  通过 `getNationByTerritory(pos)` 找出领地所属国家，再通过 `getNationByPlayer(playerName)` 判断玩家是否属于同一国家。
- `NormalSoldier.finalizeSpawn(...)`
  出生时用 `getNationByTerritory(blockPosition)` 自动继承出生地阵营。
- `AttackOtherGoal`
  通过玩家名查询玩家所属国家，判断是否为敌对目标。

## 4. 网络包说明

### 4.1 `RegisterNationPayloads`

文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/network/payloads/RegisterNationPayloads.java`

方向：

- 当前主要是客户端 -> 服务端。

用途：

- 提交“创建国家”的请求。

字段：

- `player_name`
- `nation_name`

注意：

- `player_name` 目前只是兼容性字段。
- 服务端逻辑不应信任这个字段。

### 4.2 `RequestPlayerNationPayload`

文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/network/payloads/RequestPlayerNationPayload.java`

方向：

- 客户端 -> 服务端。

用途：

- 请求“把我自己的国家快照发给我”。

字段：

- 无字段，空 payload。

### 4.3 `SyncPlayerNationPayload`

文件：

- `src/main/java/com/timwang/mc_tower_defenser/fundation/network/payloads/SyncPlayerNationPayload.java`

方向：

- 服务端 -> 客户端。

用途：

- 把玩家所属国家快照发给客户端。

字段：

- `playerName`
- `Optional<NationManager> nation`

说明：

- `nation` 为空时，表示玩家当前没有国家。
- 这里直接发送 `NationManager` 快照，不是发送一个国家名再让客户端自己拼装。

## 5. `NationManager` 的序列化与网络同步方式

这是后续开发里最关键的一点。

`NationManager` 当前网络同步并不是手写每个字段的二进制编码，而是：

- 先把 `NationManager` 序列化成 `CompoundTag`
- 再通过 `ByteBufCodecs.COMPOUND_TAG` 进行网络传输
- 客户端再反序列化回 `NationManager`

对应代码：

- `NationManager.STREAM_CODEC`
- `NationManager.OPTIONAL_STREAM_CODEC`

这带来的直接结果是：

- 如果你只是给 `NationManager` 新增字段，很多时候不需要再改 payload 结构本身。
- 你只需要把新字段接入 `NationManager.serializeNBT()` / `deserializeNBT()`，这个字段就能跟着国家快照一起过网络。

也就是说：

- 改 `NationManager` 的字段
-> 改 NBT 序列化
-> `SyncPlayerNationPayload` 会自动带上这份新数据

这是当前项目里“国家类字段变多时，仍然相对好维护”的核心原因。

## 6. 以后新增国家字段时该怎么改

这里分两种情况。

### 6.1 新字段属于单个国家

例如：

- 税率
- 外交关系
- 科技等级
- 国家公告
- 主城坐标

应优先加在 `NationManager`。

通常要改这些地方：

1. `NationManager` 字段定义。
2. `NationManager` 的 getter / setter / 业务方法。
3. `NationManager.serializeNBT()`。
4. `NationManager.deserializeNBT()`。
5. 如果 GUI 要显示，就给 `ClientNationState` 增加对应读取入口，或者直接从 `getNation()` 取。

通常不需要改的地方：

- `SyncPlayerNationPayload` 的字段结构。
- `NationSyncService.createPayload(...)` 的基本结构。

因为当前 payload 发送的是整个 `NationManager` 快照。

### 6.2 新字段属于“世界级国家系统”，不是单个国家

例如：

- 全局外交表
- 国家间战争状态总表
- 国家 id -> 名称映射
- 全局随机事件索引

这种字段应加在 `GlobalNationManager`。

通常要改这些地方：

1. `GlobalNationManager` 字段定义。
2. `GlobalNationManager.save(...)`。
3. `GlobalNationManager.load(...)`。
4. 所有会修改该字段的服务端方法里调用 `setDirty()`。
5. 如果客户端也需要读，就额外设计新的同步包，不要假设 `SyncPlayerNationPayload` 一定适合承载所有全局数据。

原因：

- `SyncPlayerNationPayload` 现在语义是“某个玩家所属国家的快照”。
- 它不是“全世界国家总表同步包”。

## 7. 服务端改国家数据后，什么时候必须同步

只要客户端需要立即看到结果，就应该同步。

当前可直接套用的规则：

### 7.1 改动只影响一个玩家自己的国家视图

例如：

- 玩家刚建国
- 玩家切换到另一个国家
- 玩家刚进服，想拿初始快照

可以调用：

- `NationSyncService.syncPlayer(player)`

### 7.2 改动影响一个国家的全体成员

例如：

- UrbanCore 新增或删除
- 国家成员列表变化
- 国家内部某个共享字段变化
- 国家领地范围变化

可以调用：

- `NationSyncService.syncNationMembers(server, nation)`

或者：

- `NationSyncService.syncNationMembers(server, nationName)`

### 7.3 改动影响两个国家

例如：

- 玩家从 A 国转到 B 国
- 外交状态是双向显示

这时通常要同步两边：

- 原国家成员
- 新国家成员
- 必要时额外同步当前玩家自己

当前项目还没有把这种多国同步封装成统一工具，所以写新玩法时要自己明确补发。

## 8. 以后新增“客户端请求修改国家数据”的正确方式

当前 `ClientNationState.requestNationChange(...)` 只是预留入口，没有真正发包。

如果以后要支持客户端提交修改，应按下面的结构做：

1. 新建一个客户端 -> 服务端 payload。
2. 在 `ModNetwork.register(...)` 注册这个 payload。
3. 在服务端 handler 里：
   - 通过 `context.player()` 获取真实玩家
   - 校验权限
   - 修改 `GlobalNationManager` / `NationManager`
   - 必要时 `setDirty()`
   - 调用 `NationSyncService` 补发结果
4. 客户端不要直接把本地缓存当成真实状态。

不要这样做：

- 客户端先改 `ClientNationState`
- 再假设服务端会“自动一致”

这是错误方向，因为 `ClientNationState` 只是镜像缓存。

## 9. 常见开发清单

### 9.1 我在 `NationManager` 里加了一个新字段，为什么客户端没看到

先查这几个点：

1. 有没有写进 `serializeNBT()`。
2. 有没有在 `deserializeNBT()` 读回来。
3. 服务器在修改这个字段后，有没有调用 `NationSyncService`。
4. 客户端读的是不是 `ClientNationState.getNation()` 的最新快照。

### 9.2 我在 `GlobalNationManager` 里加了字段，重进世界后丢了

通常是漏了：

- `save(...)`
- `load(...)`
- `setDirty()`

### 9.3 我把 payload 发过去了，但玩家身份不对

优先检查：

- 服务端是不是错误地信任了客户端上传的玩家名

正确做法：

- 一律优先使用 `context.player()` 拿真实连接玩家

### 9.4 我改了服务端国家数据，但客户端 GUI 没刷新

优先检查：

- 有没有主动调用 `NationSyncService.syncPlayer(...)`
- 或 `NationSyncService.syncNationMembers(...)`

当前项目没有“数据自动脏同步总线”，服务端改完不补发，客户端就不会自动知道。

## 10. 推荐的后续编码习惯

为了避免国家系统越写越散，建议以后保持以下习惯。

### 10.1 单个国家的数据尽量收敛进 `NationManager`

如果一个字段天然属于某个国家，就尽量放在 `NationManager`，不要拆成很多平行结构。

好处：

- 存档集中
- 网络同步集中
- 客户端快照天然完整

### 10.2 世界级索引和辅助映射放 `GlobalNationManager`

例如：

- 玩家 -> 国家
- 国家名 -> 国家对象
- 领地查询入口

这样调用方不用自己到处扫列表。

### 10.3 一次服务端变更，立刻决定“要同步给谁”

写服务端逻辑时不要只想着“数据改完了”，还要立刻想：

- 是同步给当前玩家
- 同步给这个国家全员
- 还是同步给多个国家

把“补发同步”当成业务代码的一部分，而不是事后再补。

### 10.4 客户端缓存只做展示，不做权威判定

任何关键判定，例如：

- 能不能建国
- 能不能破坏方块
- 能不能操作别国数据

都应该放在服务端。

## 11. 当前实现的一个重要边界

目前客户端拿到的是“自己所属国家的快照”，不是“全世界所有国家的完整列表”。

这意味着：

- 做本国面板、成员列表、塔列表，很适合现在这套结构。
- 如果以后要做“世界地图外交总览”或“所有国家排行榜”，大概率要新增专门的全局同步包，而不是继续往 `SyncPlayerNationPayload` 里硬塞。

## 12. 可以直接照抄的开发模板

### 12.1 新增国家字段

步骤：

1. 在 `NationManager` 新增字段。
2. 写业务读写方法。
3. 接到 `serializeNBT()` / `deserializeNBT()`。
4. 服务端修改后调用 `NationSyncService.syncNationMembers(...)`。
5. 客户端从 `ClientNationState.getNation()` 读取。

### 12.2 新增客户端发起的国家操作

步骤：

1. 新建 payload。
2. 在 `ModNetwork` 注册。
3. 写服务端 handler。
4. 服务端校验权限并改真实数据。
5. `setDirty()`。
6. 调用 `NationSyncService`。
7. 客户端只收结果，不直接改真实状态。

### 12.3 新增一个会影响领地的服务端玩法

步骤：

1. 修改 `NationManager` 里的塔或领地相关数据。
2. 确保 `GlobalNationManager` 已 `setDirty()`。
3. 调用 `NationSyncService.syncNationMembers(...)`。
4. 如果还有服务端判定逻辑，例如方块保护或 AI 仇恨，确认它们读的是最新数据入口。

## 13. 总结

当前国家系统的重点不是“包有多少个”，而是这三件事：

- 服务端的真实数据集中在 `NationManager` 和 `GlobalNationManager`
- 客户端只拿快照，不做权威写入
- 任何服务端国家数据变更后，都要主动补同步

以后你如果继续扩国家类，优先把数据收拢进 `NationManager`，再依赖它的 NBT 序列化去带动网络同步。这比给每个新字段都单独写一套网络包更稳，也更容易维护。
