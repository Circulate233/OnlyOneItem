package com.circulation.only_one_item.crt;

import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.handler.MatchItemHandler;
import com.circulation.only_one_item.util.BlackMatchItem;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.ooi.BlackList")
public class CrtBlackList {

    @ZenMethod
    public static void addMatchItem(IItemStack stack) {
        MatchItemHandler.registerBlackList(BlackMatchItem.getInstance(CraftTweakerMC.getItemStack(stack)));
    }

    @ZenMethod
    public static void addMatchItem(IOreDictEntry oreDictEntry) {
        MatchItemHandler.registerBlackList(BlackMatchItem.getInstance(Type.OreDict, oreDictEntry.getName()));
    }

    @ZenMethod
    public static void addMatchItem(String modid) {
        MatchItemHandler.registerBlackList(BlackMatchItem.getInstance(Type.ModID, modid));
    }

    @ZenMethod
    public static void addMatchItem(Object... matchs) {
        for (Object match : matchs) {
            if (match instanceof IOreDictEntry iOreDictEntry) {
                addMatchItem(iOreDictEntry);
            } else if (match instanceof IItemStack stack) {
                addMatchItem(stack);
            } else if (match instanceof String modid) {
                addMatchItem(modid);
            }
        }
    }
}
