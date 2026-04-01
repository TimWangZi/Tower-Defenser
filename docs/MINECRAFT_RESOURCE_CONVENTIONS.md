# Minecraft 资源文件规范说明

本文档面向当前仓库，整理 Minecraft Java Edition 在 `MC 1.21.1 + NeoForge 21.1.x` 下常见资源文件的目录规范、命名规则、引用方式，以及本项目适合采用的组织方式。

适用前提：

- Minecraft 版本：`1.21.1`
- NeoForge 版本：`21.1.219`
- 模组命名空间：`minecraft_tower_defenser`
- 当前项目同时使用原版资源系统和 GeckoLib 资源系统

## 1. 先理解两个根目录：`assets` 和 `data`

Minecraft 的绝大多数“资源文件”都可以先分成两类：

- `assets/<namespace>/...`
  偏客户端表现资源，例如贴图、模型、语言文件、声音定义、字体、粒子、着色器等。
- `data/<namespace>/...`
  偏游戏逻辑和数据驱动内容，例如配方、战利品表、进度、标签、函数、谓词、世界生成等。

在模组项目里，这些文件通常放在：

```text
src/main/resources/
  assets/<modid>/...
  data/<modid>/...
```

当前仓库的 `build.gradle` 已经把 `src/generated/resources` 也并入了主资源源集，因此 datagen 生成的资源也会被游戏读取：

```text
src/generated/resources/
  assets/<modid>/...
  data/<modid>/...
```

这意味着：

- 手写资源一般放在 `src/main/resources`
- datagen 输出一般放在 `src/generated/resources`
- 运行时两者会一起参与打包和加载

## 2. 最重要的基础：`ResourceLocation`

Minecraft 大量资源引用都建立在 `namespace:path` 这个资源定位符上。

例如：

- `minecraft:stone`
- `minecraft_tower_defenser:urban_core`
- `minecraft_tower_defenser:item/example_item`
- `minecraft_tower_defenser:geo/urban_core.geo.json`

命名规则建议严格遵守：

- `namespace` 一般就是你的 `modid`
- `namespace` 和 `path` 都使用小写
- 推荐使用 `snake_case`
- `path` 里可以包含 `/` 作为子目录分隔

常见合法字符：

- 小写字母 `a-z`
- 数字 `0-9`
- 下划线 `_`
- 中划线 `-`
- 点 `.`
- 路径中允许 `/`

因此，下面这种是推荐的：

```text
minecraft_tower_defenser:block/urban_core
minecraft_tower_defenser:item/create_country_scroll
minecraft_tower_defenser:entity/normal_soldier
```

而下面这些容易出问题：

- 大写文件名
- 中文文件名
- 带空格的文件名
- Java 注册名和资源文件名不一致

## 3. 项目里最常见的资源目录结构

对于当前这种 NeoForge 模组项目，一个比较稳妥的资源树通常长这样：

```text
src/main/resources/
  assets/
    minecraft_tower_defenser/
      blockstates/
      items/
      models/
        block/
        item/
      textures/
        block/
        item/
        entity/
        gui/
      lang/
      sounds/
      particles/
      geo/
        block/
        entity/
        item/
      animations/
        block/
        entity/
        item/
      sounds.json
  data/
    minecraft_tower_defenser/
      recipe/
      loot_table/
      advancement/
      predicate/
      item_modifier/
      tags/
        block/
        item/
        entity_type/
        fluid/
      worldgen/
```

注意两个容易和旧教程混淆的点：

1. `MC 1.21.x` 的 item 渲染链里，`assets/<modid>/items/*.json` 是独立的一层。
2. 当前 NeoForge 1.21.1 文档中的很多数据目录是单数形式，例如 `recipe`、`loot_table`、`advancement`，不要机械照抄旧版教程中的复数目录。

## 4. 原版资源系统里各类文件怎么放

### 4.1 语言文件 `lang`

语言文件路径：

```text
assets/<modid>/lang/en_us.json
assets/<modid>/lang/zh_cn.json
```

文件内容是“翻译键 -> 文本”的映射，例如：

```json
{
  "item.minecraft_tower_defenser.example_item": "Example Item",
  "block.minecraft_tower_defenser.urban_core": "Urban Core",
  "itemGroup.minecraft_tower_defenser": "Minecraft Tower Defenser"
}
```

通常建议：

- 至少保留 `en_us.json`
- 如果主要开发语言是中文，可以额外维护 `zh_cn.json`
- 翻译键只用于显示，不要把它当成逻辑主键

### 4.2 Item 的资源链路

这是 `1.21.x` 里最值得单独记住的部分。

一个普通物品通常至少涉及三层文件：

```text
assets/<modid>/items/<item_id>.json
assets/<modid>/models/item/<item_id>.json
assets/<modid>/textures/item/<item_id>.png
```

例如物品 `minecraft_tower_defenser:example_item`：

`assets/minecraft_tower_defenser/items/example_item.json`

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "minecraft_tower_defenser:item/example_item"
  }
}
```

`assets/minecraft_tower_defenser/models/item/example_item.json`

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft_tower_defenser:item/example_item"
  }
}
```

贴图文件：

```text
assets/minecraft_tower_defenser/textures/item/example_item.png
```

如果是剑、斧、镐等手持工具，模型通常改成：

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft_tower_defenser:item/example_item"
  }
}
```

需要特别注意：

- `items/<id>.json` 是 client item 定义
- `models/item/<id>.json` 才是实际被提交渲染的模型 JSON
- `textures/item/<id>.png` 是模型中引用的贴图

很多旧教程只写 `models/item` 和 `textures/item`，那通常是旧版本内容。对你当前这个 `1.21.1` 项目，建议显式补上 `assets/<modid>/items/<id>.json`。

### 4.3 Block 的资源链路

一个普通方块至少常见这几层：

```text
assets/<modid>/blockstates/<block_id>.json
assets/<modid>/models/block/<block_id>.json
assets/<modid>/textures/block/<block_id>.png
```

如果该方块还有对应 `BlockItem`，通常还会有：

```text
assets/<modid>/items/<block_id>.json
```

最简单的情况可以直接让 item 使用 block 模型：

`assets/minecraft_tower_defenser/items/urban_core.json`

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "minecraft_tower_defenser:block/urban_core"
  }
}
```

而 `blockstates/urban_core.json` 负责根据方块状态选择模型。无状态或单状态方块常见最小写法类似：

```json
{
  "variants": {
    "": {
      "model": "minecraft_tower_defenser:block/urban_core"
    }
  }
}
```

如果方块是完全自定义渲染或使用 `BlockEntityRenderer` / GeckoLib，也依然建议把“背包内的 BlockItem 显示方式”单独考虑清楚。

### 4.4 贴图 `textures`

常见建议分类：

```text
textures/block/
textures/item/
textures/entity/
textures/gui/
```

基本约定：

- 图片格式通常为 `.png`
- 路径引用时一般不写 `textures/` 前缀和 `.png` 后缀
- 例如 `minecraft_tower_defenser:item/example_item` 实际对应：
  `assets/minecraft_tower_defenser/textures/item/example_item.png`

如果要做原版支持的动画贴图，可以额外放置：

```text
example_item.png.mcmeta
```

### 4.5 模型 `models`

原版模型 JSON 一般放在：

```text
assets/<modid>/models/block/
assets/<modid>/models/item/
```

常见引用规则：

- 模型引用写资源定位符，不写 `.json`
- `minecraft_tower_defenser:item/example_item`
  对应 `assets/minecraft_tower_defenser/models/item/example_item.json`

常见父模型：

- `minecraft:item/generated`
  适合普通 2D 物品
- `minecraft:item/handheld`
  适合手持工具

### 4.6 声音 `sounds.json` 和 `sounds/*.ogg`

声音资源通常至少包含：

```text
assets/<modid>/sounds.json
assets/<modid>/sounds/<path>.ogg
```

一个最小 `sounds.json` 例子：

```json
{
  "ui.open_country_screen": {
    "subtitle": "subtitles.minecraft_tower_defenser.ui.open_country_screen",
    "sounds": [
      "minecraft_tower_defenser:ui/open_country_screen"
    ]
  }
}
```

对应声音文件：

```text
assets/minecraft_tower_defenser/sounds/ui/open_country_screen.ogg
```

这里要分清几个概念：

- `SoundEvent` 是代码里注册和触发的对象
- `sounds.json` 是“声音事件 -> 声音文件列表”的映射
- `sounds/*.ogg` 才是真正音频资源

### 4.7 数据包侧资源 `data`

如果是逻辑型 JSON，一般放在 `data/<modid>/...`。

当前版本常见目录：

```text
data/<modid>/recipe/
data/<modid>/loot_table/
data/<modid>/advancement/
data/<modid>/predicate/
data/<modid>/item_modifier/
data/<modid>/tags/block/
data/<modid>/tags/item/
data/<modid>/tags/entity_type/
```

用途大致如下：

- `recipe`
  工作台、熔炉等配方
- `loot_table`
  方块掉落、箱子战利品、生物掉落
- `advancement`
  进度系统
- `tags`
  逻辑分组，例如某物品是否属于某标签
- `predicate`
  条件判断数据
- `item_modifier`
  战利品产出后的修饰

## 5. GeckoLib 资源规范

当前项目已经在用 GeckoLib，仓库里已有这些目录：

```text
assets/minecraft_tower_defenser/geo/
assets/minecraft_tower_defenser/animations/
assets/minecraft_tower_defenser/textures/
```

并且在代码中通过 `GeoModel` 返回资源路径，例如：

- `geo/urban_core.geo.json`
- `animations/urban_core.animation.json`
- `textures/urban_core.png`

对 GeckoLib 来说，常见资源分工是：

- `geo/*.geo.json`
  模型几何结构
- `animations/*.animation.json`
  动画定义
- `textures/*.png`
  贴图

虽然你现在的项目把 GeckoLib 贴图直接放在 `textures/` 根下也能工作，但从长期维护角度，更建议进一步分组：

```text
geo/block/
geo/entity/
geo/item/
animations/block/
animations/entity/
animations/item/
textures/block/
textures/entity/
textures/item/
```

例如：

```text
geo/block/urban_core.geo.json
animations/block/urban_core.animation.json
textures/block/urban_core.png

geo/entity/normal_soldier.geo.json
animations/entity/normal_soldier.animation.json
textures/entity/normal_soldier.png
```

这样做的好处是：

- 原版资源与 GeckoLib 资源命名更统一
- 后续资源数量增长后不容易混乱
- 一眼能看出资源属于 block、entity 还是 item

## 6. 本项目当前状态和建议

结合当前仓库，比较重要的观察如下：

### 6.1 当前已有资源

目前资源目录里已经存在：

- `assets/minecraft_tower_defenser/lang/en_us.json`
- `assets/minecraft_tower_defenser/textures/block/urban_core.png`
- `assets/minecraft_tower_defenser/textures/urban_core.png`
- `assets/minecraft_tower_defenser/textures/test_text.png`
- `assets/minecraft_tower_defenser/geo/urban_core.geo.json`
- `assets/minecraft_tower_defenser/geo/test_soldier.geo.json`
- `assets/minecraft_tower_defenser/animations/urban_core.animation.json`

这说明：

- 你已经在使用 GeckoLib 资源
- 原版 block texture 目录已经开始使用 `textures/block`
- 但 GeckoLib 的 texture 目录还没有完全分类
- 普通 item 资源链当前还不完整

### 6.2 当前缺少的典型目录

如果后续要继续扩展普通物品或常规方块资源，建议补齐：

```text
assets/minecraft_tower_defenser/items/
assets/minecraft_tower_defenser/models/block/
assets/minecraft_tower_defenser/models/item/
assets/minecraft_tower_defenser/textures/item/
assets/minecraft_tower_defenser/textures/entity/
data/minecraft_tower_defenser/
```

### 6.3 推荐的仓库规范

建议本项目后续统一采用以下约定：

1. 所有资源文件名使用小写 `snake_case`
2. 原版资源按职责分类到 `block` / `item` / `entity` / `gui`
3. GeckoLib 资源也按 `block` / `entity` / `item` 分目录
4. 所有普通 item 都显式创建 `assets/<modid>/items/<id>.json`
5. 优先保证 Java 注册名、语言键、资源文件名三者一致
6. 手写资源放 `src/main/resources`，datagen 产物放 `src/generated/resources`

推荐目录模板：

```text
assets/minecraft_tower_defenser/
  blockstates/
  items/
  models/
    block/
    item/
  textures/
    block/
    item/
    entity/
    gui/
  lang/
  geo/
    block/
    entity/
    item/
  animations/
    block/
    entity/
    item/
  sounds/
  sounds.json

data/minecraft_tower_defenser/
  recipe/
  loot_table/
  advancement/
  tags/
    block/
    item/
    entity_type/
```

## 7. 最容易踩坑的地方

### 7.1 版本教程混用

最常见的问题是把 `1.16`、`1.18`、`1.20` 的教程直接搬到 `1.21.1`。

尤其是：

- item 现在多了一层 `assets/<modid>/items/<id>.json`
- 一些数据目录名称和旧版本教程不一致

所以遇到教程时，先确认它到底对应哪个 MC 版本。

### 7.2 路径写成了磁盘路径思维

资源引用不是写磁盘绝对路径，而是写 `ResourceLocation`。

例如模型里写：

```json
"layer0": "minecraft_tower_defenser:item/example_item"
```

不要写成：

- `assets/minecraft_tower_defenser/textures/item/example_item.png`
- `src/main/resources/assets/...`
- 带文件扩展名的完整磁盘路径

注意例外：

- 在 Java 代码里某些 API 直接要求你写完整相对资源路径时，可能会带扩展名
- 例如 GeckoLib 的 `GeoModel` 常直接返回 `geo/xxx.geo.json`、`animations/xxx.animation.json`

### 7.3 注册名和资源文件名不一致

如果你注册的是：

```java
ITEMS.registerSimpleItem("example_item", new Item.Properties());
```

那最稳妥的资源文件名也应该是：

- `example_item.json`
- `example_item.png`

不要 Java 里叫 `example_item`，资源里叫 `ExampleItem` 或 `exampleitem`。

### 7.4 忘记语言文件

就算模型和贴图都对了，如果 `lang` 里没有翻译键，游戏里也会直接显示翻译 key，本质上仍算资源未补全。

### 7.5 资源分组不清晰

当前项目已经同时用了：

- 原版 block texture
- GeckoLib geo
- GeckoLib animation
- 自定义实体
- 自定义方块实体

如果继续把所有 png 都扔到 `textures/` 根下，后面维护成本会迅速升高。

## 8. 什么时候该用 datagen

当下面任一情况变多时，就应该考虑 datagen：

- 方块数量变多
- item 数量变多
- 标签数量变多
- 配方数量变多
- 语言项数量变多

NeoForge 常见 datagen provider 包括：

- `LanguageProvider`
- 模型相关 provider
- `RecipeProvider`
- `TagsProvider`
- 战利品表 provider

当前仓库的 `build.gradle` 已经配置了 `data` run，并将输出目录设为 `src/generated/resources`，说明项目已经具备接入 datagen 的基础条件，只是目前代码里还没有对应的 provider 类。

## 9. 对这个项目的直接落地建议

如果你接下来要继续做“物品 + 方块 + 实体”三条线，我建议按下面的资源规范直接推进：

### 9.1 普通物品

```text
assets/minecraft_tower_defenser/items/<item_id>.json
assets/minecraft_tower_defenser/models/item/<item_id>.json
assets/minecraft_tower_defenser/textures/item/<item_id>.png
```

### 9.2 普通方块

```text
assets/minecraft_tower_defenser/blockstates/<block_id>.json
assets/minecraft_tower_defenser/models/block/<block_id>.json
assets/minecraft_tower_defenser/textures/block/<block_id>.png
assets/minecraft_tower_defenser/items/<block_id>.json
```

### 9.3 GeckoLib 实体

```text
assets/minecraft_tower_defenser/geo/entity/<entity_id>.geo.json
assets/minecraft_tower_defenser/animations/entity/<entity_id>.animation.json
assets/minecraft_tower_defenser/textures/entity/<entity_id>.png
```

### 9.4 GeckoLib 方块实体

```text
assets/minecraft_tower_defenser/geo/block/<block_id>.geo.json
assets/minecraft_tower_defenser/animations/block/<block_id>.animation.json
assets/minecraft_tower_defenser/textures/block/<block_id>.png
```

这套划分和你当前项目方向最契合，后续无论是查找文件、写 datagen，还是交给别人协作维护，都更清晰。

## 10. 参考资料

以下资料用于整理本文规范，优先参考 NeoForge 官方文档，其次参考 Minecraft Wiki 与 GeckoLib Wiki：

- NeoForge Resources Overview
  https://docs.neoforged.net/docs/1.21.1/resources/
- NeoForge ResourceLocation
  https://docs.neoforged.net/docs/1.21.1/misc/resourcelocation
- NeoForge Models
  https://docs.neoforged.net/docs/resources/client/models/
- NeoForge Client Items
  https://docs.neoforged.net/docs/resources/client/models/items
- NeoForge I18n and L10n
  https://docs.neoforged.net/docs/resources/client/i18n
- NeoForge Sounds
  https://docs.neoforged.net/docs/1.21.1/resources/client/sounds
- NeoForge Recipes
  https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/
- NeoForge Tags
  https://docs.neoforged.net/docs/1.21.1/resources/server/tags/
- Minecraft Wiki: Resource location
  https://minecraft.wiki/w/Resource_location
- Minecraft Wiki: Tutorial: Models
  https://minecraft.wiki/w/Tutorial%3AModels
- Minecraft Wiki: Items model definition
  https://minecraft.wiki/w/Items_model_definition
- GeckoLib Wiki: Geo Models (Geckolib4)
  https://github.com/bernie-g/geckolib/wiki/Geo-Models-%28Geckolib4%29

## 11. 一句话总结

对于当前这个 `MC 1.21.1 + NeoForge + GeckoLib` 项目，可以把资源系统理解成三条并行线：

- 原版客户端表现资源走 `assets/<modid>/...`
- 原版逻辑数据资源走 `data/<modid>/...`
- GeckoLib 额外模型动画走 `geo/ + animations/ + textures/`

只要你始终保持“注册名一致、目录分类清楚、引用用 `ResourceLocation` 思维”，后续扩展资源会顺很多。
