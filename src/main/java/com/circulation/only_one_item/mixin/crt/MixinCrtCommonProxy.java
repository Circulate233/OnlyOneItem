package com.circulation.only_one_item.mixin.crt;

import com.circulation.only_one_item.crt.CrtConversionFluidTarget;
import com.circulation.only_one_item.handler.InitHandler;
import com.circulation.only_one_item.handler.MatchFluidHandler;
import com.circulation.only_one_item.handler.MatchItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = crafttweaker.mc1120.proxies.CommonProxy.class, remap = false)
public class MixinCrtCommonProxy {

    @Inject(method = "registerReloadListener", at = @At("HEAD"))
    public void registerReloadListener(CallbackInfo ci) {
        MatchItemHandler.CrtInit();
        MatchFluidHandler.Init(CrtConversionFluidTarget.list);
        InitHandler.allPreInit();
    }
}
