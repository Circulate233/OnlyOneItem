package com.circulation.only_one_item.crt;

import com.circulation.only_one_item.emun.Type;
import com.circulation.only_one_item.util.BlackMatchItem;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Set;

@ZenRegister
@ZenClass("mods.ooi.BlackList")
public class CrtBlackList {

    public static final Set<BlackMatchItem> list = new ObjectOpenHashSet<>();

    @ZenMethod
    public static void addMatchItem(IItemStack stack) {
        list.add(BlackMatchItem.getInstance(CraftTweakerMC.getItemStack(stack)));
    }

    @ZenMethod
    public static void addMatchItem(IOreDictEntry oreDictEntry) {
        list.add(BlackMatchItem.getInstance(Type.OreDict, oreDictEntry.getName()));
    }

    @ZenMethod
    public static void addMatchItem(String modid) {
        list.add(BlackMatchItem.getInstance(Type.ModID, modid));
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
