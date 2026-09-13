package com.circulation.only_one_item.mixin.galacticraft;

import com.circulation.only_one_item.util.OOIItemStack;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * GCBlocks 用自己的方块子物品栈构建创造模式排序表，并在 finalizeSort 中把它交给
 * Ordering.explicit。这些临时栈经过 OOI 替换后会塌缩成同一个目标身份，导致
 * ImmutableMap 出现重复键并抛 IllegalArgumentException。这里在读取处取回原物品。
 */
@Mixin(value = GCBlocks.class, remap = false)
public class MixinGCBlocks {

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
}
