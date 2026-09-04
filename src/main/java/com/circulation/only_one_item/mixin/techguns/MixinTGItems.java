package com.circulation.only_one_item.mixin.techguns;

import com.circulation.only_one_item.util.OOIItemStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import techguns.TGItems;

@Mixin(value = TGItems.class, remap = false)
public class MixinTGItems {

    @SuppressWarnings("MixinAnnotationTarget")
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemDamage()I", remap = true))
    private static int onGetItemDamage(ItemStack itemStack) {
        return OOIItemStack.forItem(itemStack).ooi$getOldMetaData();
    }

}
