package com.circulation.only_one_item.mixin.mc;

import com.circulation.only_one_item.conversion.FluidConversionTarget;
import com.circulation.only_one_item.handler.MatchFluidHandler;
import com.circulation.only_one_item.util.OOIFluidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidStack.class)
public abstract class MixinFluidStack implements OOIFluidStack {

    @Shadow(remap = false)
    private IRegistryDelegate<Fluid> fluidDelegate;
    @Unique
    private boolean ooi$isBeReplaced;
    @Unique
    private Fluid ooi$originalFluid;

    @Unique
    private static IRegistryDelegate<Fluid> ooi$makeDelegate(Fluid fluid) {
        return FluidRegistry.makeDelegate(fluid);
    }

    @Inject(method = "<init>(Lnet/minecraftforge/fluids/Fluid;I)V", at = @At("TAIL"), remap = false)
    private void onInit(Fluid fluid, int amount, CallbackInfo ci) {
        ooi$ooiInit(fluid);
    }

    @Inject(method = "copy", at = @At("TAIL"), remap = false)
    private void copy(CallbackInfoReturnable<FluidStack> cir) {
        if (ooi$isBeReplaced) {
            ((OOIFluidStack) (Object) cir.getReturnValue())
                .ooi$inheritReplacementState(ooi$originalFluid, fluidDelegate);
        }
    }

    @Override
    public void ooi$ooiInit(Fluid fluid) {
        FluidConversionTarget conversionTarget = MatchFluidHandler.match(fluid);
        if (conversionTarget == null) {
            return;
        }

        Fluid target = conversionTarget.getTarget();
        if (target == null) {
            MatchFluidHandler.addPreFluidStack(conversionTarget, this);
            return;
        }
        ooi$replace(target);
    }

    @Override
    public void ooi$replace(Fluid target) {
        var delegate = ooi$makeDelegate(target);
        if (delegate != null) {
            if (!ooi$isBeReplaced) {
                ooi$originalFluid = fluidDelegate.get();
            }
            fluidDelegate = delegate;
            ooi$isBeReplaced = true;
        }
    }

    @Override
    public void ooi$inheritReplacementState(Fluid originalFluid, IRegistryDelegate<Fluid> currentDelegate) {
        this.ooi$originalFluid = originalFluid;
        this.ooi$isBeReplaced = true;
        this.fluidDelegate = currentDelegate;
    }
}
