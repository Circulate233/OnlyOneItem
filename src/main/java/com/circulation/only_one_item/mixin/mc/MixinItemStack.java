package com.circulation.only_one_item.mixin.mc;

import com.circulation.only_one_item.conversion.ItemConversionTarget;
import com.circulation.only_one_item.handler.MatchItemHandler;
import com.circulation.only_one_item.util.OOIItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements OOIItemStack {

    @Shadow
    int itemDamage;
    @Mutable
    @Shadow
    @Final
    private Item item;
    @Shadow(remap = false)
    private net.minecraftforge.registries.IRegistryDelegate<Item> delegate;
    @Unique
    private boolean ooi$isBeReplaced = false;
    @Unique
    private Item ooi$originalItem;
    @Unique
    private int ooi$originalMeta;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract void setCount(int size);

    @Shadow public abstract int getItemDamage();

    @Inject(method = "forgeInit", at = @At("TAIL"), remap = false)
    private void ooiInit(CallbackInfo ci) {
        if (!this.isEmpty()) {
            ooi$ooiInit();
        }
    }

    @Inject(method = "copy", at = @At("TAIL"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        if (ooi$isBeReplaced) {
            OOIItemStack.forItem(cir.getReturnValue()).ooi$inheritReplacementState(
                ooi$originalItem, ooi$originalMeta, item, delegate, itemDamage);
        }
    }

    @Inject(method = "setItemDamage", at = @At("TAIL"))
    private void setMateData(int meta, CallbackInfo ci) {
        if (this.getItem().isDamageable()) return;
        ooi$restoreOriginalItem();
        if (this.getItem().getHasSubtypes()) {
            ooi$ooiInit();
        }
    }

    @Unique
    @Override
    public boolean ooi$isBeReplaced() {
        return ooi$isBeReplaced;
    }

    @Intrinsic
    public ItemStack ooi$getThis() {
        return (ItemStack) (Object) this;
    }

    @Intrinsic
    public void ooi$ooiInit() {
        ItemConversionTarget target = MatchItemHandler.match(item, itemDamage);
        if (target == null) {
            return;
        }

        Item targetItem = target.getTarget();
        if (targetItem == null) {
            MatchItemHandler.addPreItemStack(target, this);
            return;
        }
        ooi$replace(target, targetItem);
    }

    @Intrinsic
    public void ooi$replace(ItemConversionTarget target, Item targetItem) {
        if (!ooi$isBeReplaced) {
            ooi$originalItem = this.item;
            ooi$originalMeta = itemDamage;
        }
        if (targetItem == Items.AIR) {
            this.setCount(0);
        } else {
            this.item = targetItem;
            delegate = targetItem.delegate;
            itemDamage = target.getTargetMeta();
        }
        ooi$isBeReplaced = true;
    }

    @Intrinsic
    public void ooi$inheritReplacementState(Item originalItem, int originalMeta, Item currentItem,
        net.minecraftforge.registries.IRegistryDelegate<Item> currentDelegate, int currentMeta) {
        this.ooi$originalItem = originalItem;
        this.ooi$originalMeta = originalMeta;
        this.ooi$isBeReplaced = true;
        this.item = currentItem;
        this.delegate = currentDelegate;
        this.itemDamage = currentMeta;
    }

    @Intrinsic
    public void ooi$restoreOriginalItem() {
        if (ooi$originalItem != null && this.item != ooi$originalItem) {
            this.item = ooi$originalItem;
            this.delegate = ooi$originalItem.delegate;
            this.itemDamage = ooi$originalMeta;
        }
    }

    @Intrinsic
    public int ooi$getOldMetaData() {
        if (ooi$isBeReplaced) {
            return ooi$originalMeta;
        }
        return getItemDamage();
    }
}
