package com.circulation.only_one_item.mixin.galacticraft;

import com.circulation.only_one_item.util.OOIItemStack;
import micdoodle8.mods.galacticraft.core.GCItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * GCItems 与 GCBlocks 同样维护创造模式排序表，另外还遍历 itemList 注册物品。
 * 这些栈都可能已被 OOI 替换，读取时需要还原成星系自己的物品，否则排序表出现
 * 重复键而崩溃，物品注册也会注册成别的物品。
 */
@Mixin(value = GCItems.class, remap = false)
public class MixinGCItems {

    @Redirect(
        method = "registerSorted",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
            remap = true))
    private static Item ooi$getSortItem(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldItem();
    }

    // getCategory 与 StackSorted 构造各调用一次，因此不限定注入数量
    @Redirect(
        method = "registerSorted",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItemDamage()I",
            remap = true),
        expect = -1)
    private static int ooi$getSortMeta(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldMetaData();
    }

    @Redirect(
        method = "registerItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
            remap = true))
    private static Item ooi$getRegisteredItem(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldItem();
    }
}
