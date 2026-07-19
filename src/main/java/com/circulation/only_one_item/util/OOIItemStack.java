package com.circulation.only_one_item.util;

import com.circulation.only_one_item.conversion.ItemConversionTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface OOIItemStack {

    static OOIItemStack forItem(ItemStack itemStack) {
        return (OOIItemStack) (Object) itemStack;
    }

    void ooi$ooiInit();

    void ooi$replace(ItemConversionTarget target, Item targetItem);

    boolean ooi$isBeReplaced();

    ItemStack ooi$getThis();

    void ooi$restoreOriginalItem();
}
