# Only One Item (1.12)

Requires mixinbooter version 8.0 or higher
There may be some minor errors when using UniDicty together

1. [简体中文](#简体中文)
2. [English](#English)

## 简体中文
这个mod本身是受到[OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem)启发而开发的。
这并不是它的分支，也没有使用它的任何代码。

实质上，这个mod的目的是成为[UniDict](https://github.com/WanionCane/UniDict)的一个替代品，
用于解决modpack内重复物品过多的问题（我自己的modpack内有足足6种铜矿石！太过分了！）

这些物品在功能上完全重复，却使用不同的获取方法，甚至可能用途本该一样但是无法通用。

这个mod可以在物品创建时替换它们，让不同的物品指向同一个物品。
允许使用config/ooi内的json文件配置，也可以使用CraftTweaker来进行配置（显然，我认为crt更好）

对于json:

ooi_item.json:
```
[
  {
    "matchItems": [
      {
        "meta": 0,
        "id": "minecraft:gold_ingot"
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

这是一个简单的示例，将minecraft:gold_ingot:0和ingotGold指向了minecraft:gold_ingot:0(当然，我们知道这其实什么都没有做)

ooi_item_black_list.json:
```
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
    "name": "ingotGlod"
  }
]
```

这是另一个示例，这个示例的作用是阻止包含在这个黑名单内的物品被替换，让批量替换时的情况更加可控

ooi_fluid.json:
```
[
  {
    "matchFluids": [
      "water"
    ],
    "targetID": "water"
  }
]
```
也许你会发现一些mod里的原油，石油，或是更奇怪的流体，它们有相似的作用，通用或是不通用，同样的，你也可以替换它们

无论是物品还是流体，替换后的配方将会是直接合并所有配方

对于crt:

```
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
    "mets",
    "taiga"
];
function getODItem(od as IOreDictEntry) as IItemStack{
    if (isNull(od.firstItem))return <minecraft:stone>;
    var clq as IItemStack = null;
    for i in 0 to modid.length{
        for item in od.items{
            if (item.definition.owner == modid[i]){
                clq = item;
                break;
            }
        }
        if (!isNull(clq))break;
    }

    return isNull(clq) ? od.firstItem : clq;
};

if (!(<ore:itemSilicon>.empty)){
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
    ){
        ConversionItem.create(getODItem(od))
            .addMatchItem(od)
            .register();
        if (odName.startsWith("gem")){
            val od0 = oreDict.get("block" + odName.substring("gem".length));
            if (!od0.empty){
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        } else if (odName.startsWith("ingot")){
            val od0 = oreDict.get("block" + odName.substring("ingot".length));
            if (!od0.empty){
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        }
    }
}
```

类似于json，crt同样的支持物品和矿物辞典替换，黑名单也可以直接使用modid进行过滤，这里提供一份尽可能通用的crt文件来简单的处理可能遇见的情况

需要注意的是，一些mod的物品被替换可能会导致错误，这需要根据modpack自己判断情况

特别的，这个mod还会处理重复的工作台配方，不同的mod可能会出现不同的铜锭合成相同的铜块，
这个mod会自动的分析这个情况，合并所有重复的配方并且将输入全部替换为矿物辞典，这是自动的，不需要进行额外配置

如果整合包需要由 CraftTweaker 或其他配方管理器控制配方删除，可以在 `config/only_one_item.cfg` 中关闭这个步骤：

```
recipes {
    B:clearDuplicateRecipes=false
}
```

## English

Here's the English translation of the provided mod description and configuration details:

This mod was inspired by OneEnoughItem.

It is not a fork and does not use any code from that project.

Its core purpose is to serve as an alternative to UniDict for resolving excessive item duplication in modpacks. (My own pack has 6 different copper ores! It’s insane!)

These items are functionally identical but use different registries, causing recipes and storage systems to treat them as distinct items (e.g., incompatible crafting, separate storage).

This mod replaces items during creation, redirecting duplicates to reference a single unified item.

Configurations can be managed via JSON files in /config/ooi/or through CraftTweaker scripts (the latter is recommended for flexibility).

JSON Configuration Examples
Item Replacement (ooi_item.json)

Redirects specified items/oredict entries to a target item:
```
[
  {
    "matchItems": [
      {
        "meta": 0,
        "id": "minecraft:gold_ingot"
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

(This example does nothing functionally, as both entries point to the same item. It demonstrates syntax.)

Blacklist (ooi_item_black_list.json)

Prevents listed items, mods, or oredict entries from being replaced:
```
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
    "name": "ingotGlod"
  }
]
```

Fluid Replacement (ooi_fluid.json)

Unifies fluids (e.g., oil variants from different mods):

```
[
  {
    "matchFluids": [
      "water"
    ],
    "targetID": "water"
  }
]
```

CraftTweaker Script Example
More powerful dynamic configuration:

```
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
    "mets",
    "taiga"
];
function getODItem(od as IOreDictEntry) as IItemStack{
    if (isNull(od.firstItem))return <minecraft:stone>;
    var clq as IItemStack = null;
    for i in 0 to modid.length{
        for item in od.items{
            if (item.definition.owner == modid[i]){
                clq = item;
                break;
            }
        }
        if (!isNull(clq))break;
    }

    return isNull(clq) ? od.firstItem : clq;
};

if (!(<ore:itemSilicon>.empty)){
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
    ){
        ConversionItem.create(getODItem(od))
            .addMatchItem(od)
            .register();
        if (odName.startsWith("gem")){
            val od0 = oreDict.get("block" + odName.substring("gem".length));
            if (!od0.empty){
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        } else if (odName.startsWith("ingot")){
            val od0 = oreDict.get("block" + odName.substring("ingot".length));
            if (!od0.empty){
                ConversionItem.create(getODItem(od0))
                    .addMatchItem(od0)
                    .register();
            }
        }
    }
}
```

keynotes

Recipe Merging: After unification, all recipesusing replaced items/fluids will use the unified target.

Automatic Duplicate Handling: Duplicate workstation recipes (e.g., copper ingots -> copper blocks from different mods) are automatically detected and merged with OreDict inputs. No extra config needed!

If a modpack needs CraftTweaker or another recipe manager to control recipe removals, this step can be disabled in `config/only_one_item.cfg`:

```
recipes {
    B:clearDuplicateRecipes=false
}
```

Caution: Replacing items from certain mods may cause errors. Test replacements thoroughly within your modpack.

Preference: CraftTweaker scripts (*.zs) are recommended over static JSON for complex/mass replacements.

This mod simplifies modpack item management by merging duplicates and fixing recipe fragmentation—let your pack breathe!
