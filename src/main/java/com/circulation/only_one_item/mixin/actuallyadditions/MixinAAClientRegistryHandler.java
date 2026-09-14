package com.circulation.only_one_item.mixin.actuallyadditions;

import com.circulation.only_one_item.util.OOIItemStack;
import de.ellpeck.actuallyadditions.mod.ClientRegistryHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientRegistryHandler.class, remap = false)
public class MixinAAClientRegistryHandler {

    @Redirect(
        method = "onModelRegistry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
            remap = true))
    private Item onGetItem(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldItem();
    }

    @Redirect(
        method = "onModelRegistry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItemDamage()I",
            remap = true))
    private int onGetItemDamage(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldMetaData();
    }
}
