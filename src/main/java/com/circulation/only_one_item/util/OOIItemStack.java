package com.circulation.only_one_item.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface OOIItemStack {

    static OOIItemStack forItem(ItemStack itemStack) {
        return (OOIItemStack) (Object) itemStack;
    }

    void ooi$init();

    void ooi$ooiInit();

    boolean ooi$isBeReplaced();

    ItemStack ooi$getThis();

    void setItem(Item item);
}
