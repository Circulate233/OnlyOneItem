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

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements OOIItemStack {

    @Unique
    private static boolean ooi$init = false;
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

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract void setCount(int size);

    @Inject(method = "forgeInit", at = @At("TAIL"), remap = false)
    private void forgeInit(CallbackInfo ci) {
        if (!this.isEmpty()) {
            ooi$ooiInit();
        }
    }

    @Inject(method = "setItemDamage", at = @At("TAIL"), remap = false)
    private void setMateData(int meta, CallbackInfo ci) {
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
    public void ooi$init() {
        ooi$init = true;
    }

    @Intrinsic
    public ItemStack ooi$getThis() {
        return (ItemStack) (Object) this;
    }

    @Intrinsic
    public void ooi$ooiInit() {
        ItemConversionTarget target = MatchItemHandler.match(item, itemDamage);

        if (target != null) {
            var item = target.getTarget();
            if (item != null) {
                if (item == Items.AIR) {
                    this.setCount(0);
                } else {
                    this.item = item;
                    delegate = item.delegate;
                    itemDamage = target.getTargetMeta();
                }
                ooi$isBeReplaced = true;
                return;
            }
        }
        if (!ooi$init) {
            MatchItemHandler.addPreItemStack(this);
        }
    }

    @Intrinsic
    public void setItem(Item item) {
        this.item = item;
        this.delegate = item.delegate;
    }
}