# Only One Item

Only One Item is a Minecraft 1.12.2 mod for reducing duplicate items and fluids in modpacks by redirecting equivalent stacks to a common target. It requires MixinBooter `>= 8.0` at runtime.

The project was inspired by [OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem). It is not a fork and does not use code from that project. Its purpose overlaps with [UniDict](https://github.com/WanionCane/UniDict), but it is implemented independently.

1. [简体中文](#简体中文)
2. [English](#english)

## 简体中文

### 项目定位

Only One Item 在物品或流体栈创建、复制和修改时应用映射，让功能重复但注册名不同的内容可以使用统一目标。它适合用于减少整合包中的重复物品、流体和由此产生的配方碎片。

本项目受到 [OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem) 启发，但不是其分支，也没有使用其代码。本项目可以作为 [UniDict](https://github.com/WanionCane/UniDict) 的独立替代方案。

### 配置文件

配置目录是 `config/ooi`，包含以下三个文件：

- `ooi_item.json`：物品和矿辞 source 到目标物品的映射。
- `ooi_fluid.json`：流体 source 到目标流体的映射。
- `ooi_item_black_list.json`：不参与物品替换的物品、模组或矿辞。

#### `ooi_item.json`

```json
[
  {
    "matchItems": [
      {
        "id": "minecraft:gold_ingot",
        "meta": 0
      },
      {
        "oreName": "ingotGold"
      }
    ],
    "targetID": "minecraft:gold_ingot",
    "targetMeta": 0
  }
]
```

`matchItems` 可以包含具体物品（`id` + `meta`）或矿辞（`oreName`）。上例把 `minecraft:gold_ingot:0` 和 `ingotGold` 枚举出的 source 指向 `minecraft:gold_ingot:0`；目标相同时只是语法示例。

#### `ooi_fluid.json`

```json
[
  {
    "matchFluids": [
      "water",
      "some_mod:water"
    ],
    "targetID": "water"
  }
]
```

`matchFluids` 和 `targetID` 使用流体注册表名称；最终阶段会检查目标流体是否存在，不存在的目标映射会被丢弃。

#### `ooi_item_black_list.json`

```json
[
  {
    "type": "Item",
    "name": "minecraft:gold_ingot",
    "meta": 0
  },
  {
    "type": "ModID",
    "name": "minecraft"
  },
  {
    "type": "OreDict",
    "name": "ingotGold"
  }
]
```

`Item` 按物品 ID 和 meta 排除，`ModID` 按模组 ID 排除，`OreDict` 按矿辞名称排除。黑名单会阻止匹配到的物品或矿辞参与统一。

### 注意事项

最终有效配置会写回 `config/ooi/ooi_item.json`、`config/ooi/ooi_fluid.json` 和 `config/ooi/ooi_item_black_list.json`。物品映射会同时保留矿辞规则和矿辞最终展开出的具体物品 ID/meta。文件会被规范化覆盖，手工注释和原始排版不会保留，编辑前请备份。无效目标会被忽略并记录日志。

CRT 在本 mod 中主要用于生成或补全这三份 JSON 配置，脚本注册的有效条目会写入 JSON。首次加入或修改 CRT 脚本的那次启动，运行中的替换结果可能不完整或不准确；启动完成后请检查生成的 JSON 并重启游戏或服务端。确认 JSON 无误后，后续启动以 JSON 为准，不要求继续保留 CRT 脚本。

### CraftTweaker API

当前 ZenScript 类名和方法如下：

- `mods.ooi.ConversionItem`：`create(IItemStack)`、`addMatchItem(IItemStack)`、`addMatchItem(IOreDictEntry)`、`addMatchItem(Object...)`、`register()`。
- `mods.ooi.ConversionFluid`：`create(ILiquidStack)`、`addMatchFluid(String)`、`addMatchFluid(ILiquidStack)`、`addMatchFluid(Object...)`、`register()`。
- `mods.ooi.BlackList`：`addMatchItem(IItemStack)`、`addMatchItem(IOreDictEntry)`、`addMatchItem(String)`、`addMatchItem(Object...)`。

`ConversionItem` 和 `ConversionFluid` 先用 `addMatch...` 添加 source，再调用 `register()` 提交目标；`BlackList.addMatchItem(...)` 可直接添加黑名单项。

以下脚本可用于批量生成上述 JSON；首次运行请先阅读注意事项。

```zenscript
import mods.ooi.ConversionItem;
import mods.ooi.ConversionFluid;
import mods.ooi.BlackList;

BlackList.addMatchItem("chisel");

static modid as string[] = [
    "minecraft",
    "thermalfoundation",
    "enderio",
    "tconstruct",
    "ic2",
    "mekanism",
    "taiga"
];

function getODItem(od as IOreDictEntry) as IItemStack {
    if (isNull(od.firstItem)) return <minecraft:stone>;

    var selected as IItemStack = null;
    for i in 0 to modid.length {
        for item in od.items {
            if (item.definition.owner == modid[i]) {
                selected = item;
                break;
            }
        }
        if (!isNull(selected)) break;
    }

    return isNull(selected) ? od.firstItem : selected;
}

if (!(<ore:itemSilicon>.empty)) {
    ConversionItem.create(getODItem(<ore:itemSilicon>))
        .addMatchItem(<ore:itemSilicon>)
        .register();
}

for od in oreDict.entries {
    var odName = od.name;
    if (odName.startsWith("ore")
        || odName.startsWith("dust")
        || odName.startsWith("ingot")
        || odName.startsWith("gem")
        || odName.startsWith("nugget")
        || odName.startsWith("plate")
        || odName.startsWith("gear")
        || odName.startsWith("stick")
    ) {
        ConversionItem.create(getODItem(od))
            .addMatchItem(od)
            .register();

        if (odName.startsWith("gem")) {
            val od0 = oreDict.get("block" + odName.substring("gem".length));
            if (!od0.empty) {
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        } else if (odName.startsWith("ingot")) {
            val od0 = oreDict.get("block" + odName.substring("ingot".length));
            if (!od0.empty) {
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        }
    }
}
```

复杂的批量规则可以使用同一套 API 编写循环或函数。替换某些模组物品可能改变其配方或机器行为，请在目标整合包中验证。

### 配方处理

Only One Item 会分析工作台中的重复配方，并在满足条件时重建合并后的配方。它不是对所有物品和流体配方无条件直接合并。

如果检测到 UniDict 已加载，Only One Item 会跳过自身的工作台重复配方处理，避免重复处理。

## English

### Project

Only One Item is an independent Minecraft 1.12.2 mod for unifying equivalent items and fluids in modpacks. It redirects created, copied, or modified stacks to configured targets and requires MixinBooter `>= 8.0` at runtime.

The project was inspired by [OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem), but is neither a fork nor a code derivative. It is an independent alternative to [UniDict].

### Configuration

The three files are `config/ooi/ooi_item.json`, `config/ooi/ooi_fluid.json`, and `config/ooi/ooi_item_black_list.json`. The item file accepts item ID/meta and ore-dictionary match sources, the fluid file maps fluid names, and the blacklist accepts `Item`, `ModID`, and `OreDict` entries. See the [JSON examples](#配置文件).

The final effective JSON and CRT configuration is written back to all three files. Item mappings include both ore-dictionary rules and the concrete item ID/meta entries expanded from them. Comments and original formatting are not preserved, so back up the files before editing. Invalid targets are ignored and logged.

CraftTweaker is mainly used to generate or complete these three JSON files, and valid registered entries are written to JSON. On the first launch after adding or changing a CRT script, replacement results may be incomplete or inaccurate. Check the generated JSON after startup and restart the game or server. Once the JSON is confirmed, later launches can use JSON directly; the CRT script does not have to be kept.

### CraftTweaker

The current API consists of `mods.ooi.ConversionItem` (`create(IItemStack)`, the `addMatchItem(...)` overloads, and `register()`), `mods.ooi.ConversionFluid` (`create(ILiquidStack)`, the `addMatchFluid(...)` overloads, and `register()`), and `mods.ooi.BlackList` (the `addMatchItem(...)` overloads for items, ore dictionary entries, mod IDs, and varargs). See the [shared ZenScript example](#crafttweaker-api). Add match sources before calling `register()`; blacklist entries can be added directly. CRT is mainly a way to generate or complete the JSON configuration, and valid registrations are persisted there.

### Recipes

Ore dictionary entries can be used as item match sources. Only One Item analyzes duplicate workstation recipes and merges them when they meet its merge rules; it does not unconditionally merge every item or fluid recipe. If UniDict is loaded, Only One Item skips its own workstation recipe cleanup.

[UniDict]: https://github.com/WanionCane/UniDict
