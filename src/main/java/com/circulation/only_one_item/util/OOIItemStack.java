package com.circulation.only_one_item.util;

import com.circulation.only_one_item.conversion.ItemConversionTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IRegistryDelegate;

public interface OOIItemStack {

    static OOIItemStack forItem(ItemStack itemStack) {
        return (OOIItemStack) (Object) itemStack;
    }

    void ooi$ooiInit();

    void ooi$replace(ItemConversionTarget target, Item targetItem);

    void ooi$inheritReplacementState(Item originalItem, int originalMeta, Item currentItem,
        IRegistryDelegate<Item> currentDelegate, int currentMeta);

    boolean ooi$isBeReplaced();

    ItemStack ooi$getThis();

    void ooi$restoreOriginalItem();

    int ooi$getOldMetaData();
}
