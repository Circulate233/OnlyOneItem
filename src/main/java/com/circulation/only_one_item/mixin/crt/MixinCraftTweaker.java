package com.circulation.only_one_item.mixin.crt;

import com.circulation.only_one_item.OOIConfig;
import com.circulation.only_one_item.OnlyOneItem;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = crafttweaker.mc1120.CraftTweaker.class, remap = false)
public class MixinCraftTweaker {

    @Inject(method = "onPostInit", at = @At("HEAD"))
    private void ooi$initializeBeforeCraftTweakerActions(FMLPostInitializationEvent event, CallbackInfo ci) {
        if (OOIConfig.initializeBeforeCraftTweakerActions) {
            OnlyOneItem.initializeStacksOnce("CraftTweaker postInit");
        }
    }
}
