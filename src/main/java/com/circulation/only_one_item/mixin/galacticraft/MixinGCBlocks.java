package com.circulation.only_one_item.mixin.galacticraft;

import com.circulation.only_one_item.util.OOIItemStack;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
