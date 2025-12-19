package com.circulation.only_one_item.mixin.mc;

import com.circulation.only_one_item.util.OOIItemStack;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Random;

@SuppressWarnings("DiscouragedShift")
@Mixin(LootEntryItem.class)
public class MixinLootEntryItem {

    @Shadow
    @Final
    protected Item item;

    @Inject(method = "addLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/loot/functions/LootFunction;apply(Lnet/minecraft/item/ItemStack;Ljava/util/Random;Lnet/minecraft/world/storage/loot/LootContext;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE))
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context, CallbackInfo ci, @Local(ordinal = 0) ItemStack itemstack) {
        OOIItemStack i = OOIItemStack.forItem(itemstack);
        if (i.ooi$isBeReplaced()) {
            i.setItem(this.item);
        }
    }
}
