package com.circulation.only_one_item.crt;

import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.util.MatchItem;
import com.circulation.only_one_item.util.SimpleItem;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;
import java.util.Set;

@ZenRegister
@ZenClass("mods.ooi.ConversionItem")
@SuppressWarnings("UnusedReturnValue")
public class CrtConversionItemTarget {

    public static final List<ItemConversionTarget> list = new ObjectArrayList<>();

    private final Set<MatchItem> matchItems = new ObjectOpenHashSet<>();

    private final String targetID;
    private final int targetMeta;

    public CrtConversionItemTarget(String id, int meta) {
        this.targetID = id;
        this.targetMeta = meta;
    }

    @ZenMethod
    public static CrtConversionItemTarget create(IItemStack target) {
        if (target == null) {
            var i = SimpleItem.getNoNBTInstance(ItemStack.EMPTY);
            return new CrtConversionItemTarget(i.getItemID(), i.getMeta());
        }
        return new CrtConversionItemTarget(target.getDefinition().getId(), target.getMetadata());
    }

    @ZenMethod
    public CrtConversionItemTarget addMatchItem(IItemStack stack) {
        matchItems.add(MatchItem.getInstance(CraftTweakerMC.getItemStack(stack)));
        return this;
    }

    @ZenMethod
    public CrtConversionItemTarget addMatchItem(IOreDictEntry oreDictEntry) {
        matchItems.add(MatchItem.getInstance(oreDictEntry.getName()));
        return this;
    }

    @ZenMethod
    public CrtConversionItemTarget addMatchItem(Object... odOrItems) {
        for (Object odOrItem : odOrItems) {
            if (odOrItem instanceof IOreDictEntry iOreDictEntry) {
                addMatchItem(iOreDictEntry);
            } else if (odOrItem instanceof IItemStack stack) {
                addMatchItem(stack);
            }
        }
        return this;
    }

    @ZenMethod
    public void register() {
        list.add(new ItemConversionTarget(targetID, targetMeta).setMatchItem(matchItems));
    }
}
